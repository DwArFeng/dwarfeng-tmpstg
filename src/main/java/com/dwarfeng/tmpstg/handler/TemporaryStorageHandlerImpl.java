package com.dwarfeng.tmpstg.handler;

import com.dwarfeng.subgrade.sdk.exception.HandlerExceptionHelper;
import com.dwarfeng.subgrade.sdk.interceptor.analyse.BehaviorAnalyse;
import com.dwarfeng.subgrade.sdk.interceptor.analyse.SkipRecord;
import com.dwarfeng.subgrade.stack.exception.HandlerException;
import com.dwarfeng.tmpstg.bean.dto.TemporaryStorageInfo;
import com.dwarfeng.tmpstg.exception.TemporaryStorageHandlerStoppedException;
import com.dwarfeng.tmpstg.exception.TemporaryStorageInvalidStatusException;
import com.dwarfeng.tmpstg.exception.TemporaryStorageNotExistsException;
import com.dwarfeng.tmpstg.exception.TemporaryStorageStreamOpenException;
import com.dwarfeng.tmpstg.struct.TemporaryStorageConfig;
import com.dwarfeng.tmpstg.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 临时存储处理器实现。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class TemporaryStorageHandlerImpl implements TemporaryStorageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemporaryStorageHandlerImpl.class);

    private final ThreadPoolTaskScheduler scheduler;

    private final TemporaryStorageConfig config;

    private final Map<String, TemporaryStorage> storageMap = new HashMap<>();

    private final ReadWriteLock handlerLock = new ReentrantReadWriteLock();
    private final Lock memoryAllocationLock = new ReentrantLock();

    private long totalAllocatedMemoryBufferSize = 0;

    private ScheduledFuture<?> clearDisposedTaskFuture;
    private ScheduledFuture<?> checkMemoryTaskFuture;
    private boolean startedFlag = false;

    public TemporaryStorageHandlerImpl(
            @Nonnull ThreadPoolTaskScheduler scheduler,
            @Nonnull TemporaryStorageConfig config
    ) {
        this.scheduler = scheduler;
        this.config = config;
    }

    @BehaviorAnalyse
    @Override
    public boolean isStarted() {
        handlerLock.readLock().lock();
        try {
            return startedFlag;
        } finally {
            handlerLock.readLock().unlock();
        }
    }

    @BehaviorAnalyse
    @Override
    public void start() throws HandlerException {
        handlerLock.writeLock().lock();
        try {
            if (startedFlag) {
                return;
            }

            if (config.getClearDisposedInterval() > 0) {
                clearDisposedTaskFuture = scheduler.scheduleAtFixedRate(
                        this::clearDisposedTask, config.getClearDisposedInterval()
                );
            }

            if (config.getCheckMemoryInterval() > 0) {
                checkMemoryTaskFuture = scheduler.scheduleAtFixedRate(
                        this::checkMemoryTask, config.getCheckMemoryInterval()
                );
            }

            startedFlag = true;
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        } finally {
            handlerLock.writeLock().unlock();
        }
    }

    @BehaviorAnalyse
    @Override
    public void stop() throws HandlerException {
        handlerLock.writeLock().lock();
        try {
            if (!startedFlag) {
                return;
            }

            if (Objects.nonNull(clearDisposedTaskFuture)) {
                clearDisposedTaskFuture.cancel(true);
                clearDisposedTaskFuture = null;
            }

            if (Objects.nonNull(checkMemoryTaskFuture)) {
                checkMemoryTaskFuture.cancel(true);
                checkMemoryTaskFuture = null;
            }

            // 在释放内存缓冲区之前，先检查一遍内存缓冲区是否正确。
            internalCheckMemory();

            for (TemporaryStorage temporaryStorage : storageMap.values()) {
                temporaryStorage.storageLock.writeLock().lock();
                try {
                    if (Objects.equals(temporaryStorage.status, Constants.TEMPORARY_STORAGE_STATUS_DISPOSED)) {
                        continue;
                    }
                    memoryAllocationLock.lock();
                    try {
                        temporaryStorage.dispose();
                    } finally {
                        memoryAllocationLock.unlock();
                    }
                } finally {
                    temporaryStorage.storageLock.writeLock().unlock();
                }
            }
            storageMap.clear();
            totalAllocatedMemoryBufferSize = 0;

            startedFlag = false;
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        } finally {
            handlerLock.writeLock().unlock();
        }
    }

    @BehaviorAnalyse
    @SkipRecord
    @Override
    public Collection<String> keys() throws HandlerException {
        handlerLock.readLock().lock();
        try {
            // 确认处理器已经启动。
            makeSureHandlerStart();
            return Collections.unmodifiableCollection(storageMap.keySet());
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        } finally {
            handlerLock.readLock().unlock();
        }
    }

    @BehaviorAnalyse
    @Override
    public boolean exists(String key) throws HandlerException {
        handlerLock.readLock().lock();
        try {
            // 确认处理器已经启动。
            makeSureHandlerStart();
            return storageMap.containsKey(key);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        } finally {
            handlerLock.readLock().unlock();
        }
    }

    @SuppressWarnings("DuplicatedCode")
    @BehaviorAnalyse
    @Override
    public TemporaryStorageInfo inspect(String key) throws HandlerException {
        TemporaryStorage temporaryStorage;
        handlerLock.readLock().lock();
        try {
            // 确认处理器已经启动。
            makeSureHandlerStart();
            // 确认临时存储存在。
            makeSureTemporaryStorageExists(key);
            temporaryStorage = storageMap.get(key);
        } finally {
            handlerLock.readLock().unlock();
        }
        temporaryStorage.storageLock.readLock().lock();
        try {
            return new TemporaryStorageInfo(
                    key, temporaryStorage.memoryBuffer.length, temporaryStorage.memoryBufferActualLength,
                    temporaryStorage.fileBufferUsed, temporaryStorage.fileBufferActualLength, temporaryStorage.status,
                    temporaryStorage.memoryBufferActualLength + temporaryStorage.fileBufferActualLength
            );
        } finally {
            temporaryStorage.storageLock.readLock().unlock();
        }
    }

    @BehaviorAnalyse
    @Override
    public String create() throws HandlerException {
        handlerLock.writeLock().lock();
        try {
            // 确认处理器已经启动。
            makeSureHandlerStart();
            String key = UUID.randomUUID().toString();
            TemporaryStorage temporaryStorage = new TemporaryStorage(key);
            storageMap.put(key, temporaryStorage);
            return key;
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        } finally {
            handlerLock.writeLock().unlock();
        }
    }

    @BehaviorAnalyse
    @Override
    public InputStream openInputStream(String key) throws HandlerException {
        try {
            return internalOpenInputStream(key);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private InputStream internalOpenInputStream(String key) throws Exception {
        TemporaryStorage temporaryStorage;
        handlerLock.readLock().lock();
        try {
            // 确认处理器已经启动。
            makeSureHandlerStart();
            // 确认临时存储存在。
            makeSureTemporaryStorageExists(key);
            temporaryStorage = storageMap.get(key);
        } finally {
            handlerLock.readLock().unlock();
        }
        temporaryStorage.storageLock.readLock().lock();
        try {
            // 确认临时存储状态有效。
            makeSureTemporaryStorageStatusValid(temporaryStorage, Constants.TEMPORARY_STORAGE_STATUS_WORKING);
            // 打开输入流并返回。
            return temporaryStorage.openInputStream();
        } catch (Exception e) {
            temporaryStorage.storageLock.readLock().unlock();
            throw new TemporaryStorageStreamOpenException(e, key);
        }
    }

    @BehaviorAnalyse
    @Override
    public OutputStream openOutputStream(String key) throws HandlerException {
        try {
            return internalOpenOutputStream(key, Long.MAX_VALUE);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @BehaviorAnalyse
    @Override
    public OutputStream openOutputStream(String key, long expectedLength) throws HandlerException {
        try {
            return internalOpenOutputStream(key, expectedLength);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private OutputStream internalOpenOutputStream(String key, long expectedLength) throws Exception {
        TemporaryStorage temporaryStorage;
        handlerLock.readLock().lock();
        try {
            // 确认处理器已经启动。
            makeSureHandlerStart();
            // 确认临时存储存在。
            makeSureTemporaryStorageExists(key);
            temporaryStorage = storageMap.get(key);
        } finally {
            handlerLock.readLock().unlock();
        }
        temporaryStorage.storageLock.writeLock().lock();
        try {
            // 确认临时存储状态有效。
            makeSureTemporaryStorageStatusValid(temporaryStorage, Constants.TEMPORARY_STORAGE_STATUS_WORKING);
            // 打开输出流并返回。
            memoryAllocationLock.lock();
            try {
                return temporaryStorage.openOutputStream(expectedLength);
            } finally {
                memoryAllocationLock.unlock();
            }
        } catch (Exception e) {
            temporaryStorage.storageLock.writeLock().unlock();
            throw new TemporaryStorageStreamOpenException(e, key);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    @BehaviorAnalyse
    @Override
    public void dispose(String key) throws HandlerException {
        TemporaryStorage temporaryStorage;
        handlerLock.readLock().lock();
        try {
            // 确认处理器已经启动。
            makeSureHandlerStart();
            // 确认临时存储存在。
            makeSureTemporaryStorageExists(key);
            temporaryStorage = storageMap.get(key);
        } finally {
            handlerLock.readLock().unlock();
        }
        temporaryStorage.storageLock.writeLock().lock();
        try {
            // 确认临时存储状态有效。
            makeSureTemporaryStorageStatusValid(temporaryStorage, Constants.TEMPORARY_STORAGE_STATUS_WORKING);
            // 释放内存缓冲区。
            memoryAllocationLock.lock();
            try {
                temporaryStorage.dispose();
            } finally {
                memoryAllocationLock.unlock();
            }
        } finally {
            temporaryStorage.storageLock.writeLock().unlock();
        }
    }

    @BehaviorAnalyse
    @Override
    public void remove(String key) throws HandlerException {
        handlerLock.writeLock().lock();
        try {
            // 确认处理器已经启动。
            makeSureHandlerStart();
            // 确认临时存储存在。
            makeSureTemporaryStorageExists(key);
            TemporaryStorage temporaryStorage = storageMap.get(key);
            // 确认临时存储状态有效。
            temporaryStorage.storageLock.readLock().lock();
            try {
                makeSureTemporaryStorageStatusValid(temporaryStorage, Constants.TEMPORARY_STORAGE_STATUS_DISPOSED);
            } finally {
                temporaryStorage.storageLock.readLock().unlock();
            }
            storageMap.remove(key);
        } finally {
            handlerLock.writeLock().unlock();
        }
    }

    @BehaviorAnalyse
    @Override
    public boolean removeIfDisposed(String key) throws HandlerException {
        handlerLock.writeLock().lock();
        try {
            // 确认处理器已经启动。
            makeSureHandlerStart();
            // 确认临时存储存在。
            makeSureTemporaryStorageExists(key);
            TemporaryStorage temporaryStorage = storageMap.get(key);
            // 获取临时存储是否已经释放。
            boolean disposedFlag;
            temporaryStorage.storageLock.readLock().lock();
            try {
                disposedFlag = Objects.equals(temporaryStorage.status, Constants.TEMPORARY_STORAGE_STATUS_DISPOSED);
            } finally {
                temporaryStorage.storageLock.readLock().unlock();
            }
            // 如果没有释放，则返回 false。
            if (!disposedFlag) {
                return false;
            }
            // 如果已经释放，则从存储映射中移除此临时存储，并返回 true。
            storageMap.remove(key);
            return true;
        } finally {
            handlerLock.writeLock().unlock();
        }
    }

    @Override
    public void disposeAndRemove(String key) throws HandlerException {
        handlerLock.writeLock().lock();
        try {
            // 确认处理器已经启动。
            makeSureHandlerStart();
            // 确认临时存储存在。
            makeSureTemporaryStorageExists(key);
            TemporaryStorage temporaryStorage = storageMap.get(key);
            // 释放临时存储。
            temporaryStorage.storageLock.writeLock().lock();
            try {
                // 确认临时存储状态有效。
                makeSureTemporaryStorageStatusValid(temporaryStorage, Constants.TEMPORARY_STORAGE_STATUS_WORKING);
                // 释放内存缓冲区。
                memoryAllocationLock.lock();
                try {
                    temporaryStorage.dispose();
                } finally {
                    memoryAllocationLock.unlock();
                }
                storageMap.remove(key);
            } finally {
                temporaryStorage.storageLock.writeLock().unlock();
            }
            // 从存储映射中移除此临时存储。
            storageMap.remove(key);
        } finally {
            handlerLock.writeLock().unlock();
        }
    }

    @Override
    public void clearDisposed() throws HandlerException {
        handlerLock.writeLock().lock();
        try {
            // 确认处理器已经启动。
            makeSureHandlerStart();
            internalClearDisposed();
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        } finally {
            handlerLock.writeLock().unlock();
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void internalClearDisposed() {
        Set<String> keySetToRemove = new HashSet<>();
        for (TemporaryStorage value : storageMap.values()) {
            if (!Objects.equals(value.status, Constants.TEMPORARY_STORAGE_STATUS_DISPOSED)) {
                continue;
            }
            keySetToRemove.add(value.key);
        }
        storageMap.keySet().removeAll(keySetToRemove);
    }

    @SuppressWarnings("DuplicatedCode")
    private void clearDisposedTask() {
        handlerLock.writeLock().lock();
        try {
            // 确认处理器已经启动。
            if (!startedFlag) {
                LOGGER.warn("处理器未启动, 忽略本次已释放临时存储清理任务");
                return;
            }

            Set<String> keySetToRemove = new HashSet<>();
            for (TemporaryStorage value : storageMap.values()) {
                if (!Objects.equals(value.status, Constants.TEMPORARY_STORAGE_STATUS_DISPOSED)) {
                    continue;
                }
                keySetToRemove.add(value.key);
            }
            storageMap.keySet().removeAll(keySetToRemove);
            LOGGER.info("释放临时存储清理任务执行结束, 共清理了 {} 个已释放的临时存储", keySetToRemove.size());
        } catch (Exception e) {
            LOGGER.warn("已释放临时存储清理任务执行时发生异常, 本次任务中止, 异常信息如下: ", e);
        } finally {
            handlerLock.writeLock().unlock();
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void checkMemoryTask() {
        handlerLock.writeLock().lock();
        try {
            if (!startedFlag) {
                LOGGER.warn("处理器未启动, 忽略本次内存检查任务");
                return;
            }

            long actualTotalAllocatedMemoryBufferSize = 0;
            for (TemporaryStorage temporaryStorage : storageMap.values()) {
                actualTotalAllocatedMemoryBufferSize += temporaryStorage.memoryBuffer.length;
            }
            if (Objects.equals(totalAllocatedMemoryBufferSize, actualTotalAllocatedMemoryBufferSize)) {
                return;
            }
            String message = "内存缓冲区大小不一致, " +
                    "totalAllocatedMemoryBufferSize: {}, actualTotalAllocatedMemoryBufferSize: {}, 请联系开发人员";
            LOGGER.warn(message, totalAllocatedMemoryBufferSize, actualTotalAllocatedMemoryBufferSize);
            LOGGER.warn("修正内存缓冲区大小...");
            totalAllocatedMemoryBufferSize = actualTotalAllocatedMemoryBufferSize;
        } finally {
            handlerLock.writeLock().unlock();
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void internalCheckMemory() {
        long actualTotalAllocatedMemoryBufferSize = 0;
        for (TemporaryStorage temporaryStorage : storageMap.values()) {
            actualTotalAllocatedMemoryBufferSize += temporaryStorage.memoryBuffer.length;
        }
        if (Objects.equals(totalAllocatedMemoryBufferSize, actualTotalAllocatedMemoryBufferSize)) {
            return;
        }
        String message = "内存缓冲区大小不一致, " +
                "totalAllocatedMemoryBufferSize: {}, actualTotalAllocatedMemoryBufferSize: {}, 请联系开发人员";
        LOGGER.warn(message, totalAllocatedMemoryBufferSize, actualTotalAllocatedMemoryBufferSize);
        LOGGER.warn("修正内存缓冲区大小...");
        totalAllocatedMemoryBufferSize = actualTotalAllocatedMemoryBufferSize;
    }

    private void makeSureHandlerStart() throws HandlerException {
        if (!startedFlag) {
            throw new TemporaryStorageHandlerStoppedException();
        }
    }

    private void makeSureTemporaryStorageExists(String key) throws HandlerException {
        if (!storageMap.containsKey(key)) {
            throw new TemporaryStorageNotExistsException(key);
        }
    }

    private void makeSureTemporaryStorageStatusValid(TemporaryStorage temporaryStorage, int expectedStatus)
            throws HandlerException {
        if (!Objects.equals(temporaryStorage.status, expectedStatus)) {
            throw new TemporaryStorageInvalidStatusException(
                    temporaryStorage.key, expectedStatus, temporaryStorage.status
            );
        }
    }

    private class TemporaryStorage {

        public final String key;

        public final ReadWriteLock storageLock = new ReentrantReadWriteLock();

        public byte[] memoryBuffer;
        public File fileBuffer;

        public int memoryBufferActualLength = 0;
        public long fileBufferActualLength = 0;
        public boolean fileBufferUsed = false;
        public int status = Constants.TEMPORARY_STORAGE_STATUS_WORKING;

        public TemporaryStorage(String key) {
            this.key = key;
            init();
        }

        private void init() {
            memoryBuffer = new byte[0];
            fileBuffer = new File(
                    config.getTemporaryFileDirectoryPath(),
                    config.getTemporaryFilePrefix() + key + config.getTemporaryFileSuffix()
            );
            fileBuffer.deleteOnExit();
        }

        /**
         * 打开输入流。
         *
         * <p>
         * 该方法本身没有线程安全性，调用该方法的线程应该持有临时存储的读锁，以及内存分配锁。
         *
         * @return 输入流。
         */
        public InputStream openInputStream() {
            // 确认状态为工作中。
            makeSureStatusWorking();
            return new TemporaryStorageInputStream(this);
        }

        /**
         * 打开输出流。
         *
         * <p>
         * 该方法本身没有线程安全性，调用该方法的线程应该持有临时存储的写锁，以及内存分配锁。
         *
         * @param expectedLength 向输出流写入的字节数的预期长度。
         * @return 输出流。
         */
        public OutputStream openOutputStream(long expectedLength) {
            // 确认状态为工作中。
            makeSureStatusWorking();
            // 释放旧的内存缓冲区。
            memoryBufferActualLength = 0;
            fileBufferActualLength = 0;
            deallocateMemoryBuffer();
            // 分配新的内存缓冲区。
            expectedLength = Math.min(expectedLength, Integer.MAX_VALUE);
            allocateMemoryBuffer((int) expectedLength);
            return new TemporaryStorageOutputStream(this);
        }

        /**
         * 释放临时存储。
         *
         * <p>
         * 该方法本身没有线程安全性，调用该方法的线程应该持有临时存储的写锁，以及内存分配锁。
         */
        public void dispose() {
            // 确认状态为工作中。
            makeSureStatusWorking();
            // 释放内存缓冲区。
            memoryBufferActualLength = 0;
            deallocateMemoryBuffer();
            // 删除文件缓冲区。
            fileBufferUsed = false;
            fileBufferActualLength = 0;
            if (Objects.nonNull(fileBuffer) && fileBuffer.exists()) {
                if (!fileBuffer.delete()) {
                    LOGGER.warn("删除文件缓冲区失败: {}", fileBuffer.getAbsolutePath());
                }
                fileBuffer = null;
            }
            // 状态处理。
            status = Constants.TEMPORARY_STORAGE_STATUS_DISPOSED;
        }

        /**
         * 申请内存缓冲区。
         *
         * <p>
         * 由于配置中的限制，实际上，申请的大小可能会小于 expectedAllocateSize。
         *
         * <p>
         * 该方法本身没有线程安全性，调用该方法的线程应该持有临时存储的写锁，以及内存分配锁。
         *
         * @param expectedAllocateSize 期望申请的大小。
         */
        public void allocateMemoryBuffer(int expectedAllocateSize) {
            // 展开变量。
            long maxBufferSizeTotal = config.getMaxBufferSizeTotal();
            // 确认状态为工作中。
            makeSureStatusWorking();
            // 定义变量，用于记录实际分配的缓冲区大小。
            int allocatedSize = expectedAllocateSize;
            //  memoryBuffer.length + allocatedSize 不得超过 maxBufferSizePerStorage。
            allocatedSize = Math.min(
                    config.getMaxBufferSizePerStorage() - memoryBuffer.length, allocatedSize
            );
            // 如果 maxBufferSizeTotal - totalAllocatedMemoryBufferSize 小于 allocatedSize:
            // 记录日志，并调整 allocatedSize。
            if (maxBufferSizeTotal - totalAllocatedMemoryBufferSize < allocatedSize) {
                String message = "无法为临时存储分配指定大小的缓冲区, 因为这会导致总的缓冲区大小超过最大限制, " +
                        "key: {}, totalAllocatedMemoryBufferSize: {}, allocatedSize: {}, maxBufferSizeTotal: {}";
                LOGGER.debug(message, key, totalAllocatedMemoryBufferSize, allocatedSize, maxBufferSizeTotal);
                allocatedSize = (int) (maxBufferSizeTotal - totalAllocatedMemoryBufferSize);
                LOGGER.debug("调整后的 allocatedSize: {}", allocatedSize);
            }
            // alreadyAllocatedBufferSize + allocatedBufferSize 不得超过 maxBufferSizeTotal。
            allocatedSize = (int) Math.min(
                    maxBufferSizeTotal - totalAllocatedMemoryBufferSize, allocatedSize
            );
            // 确保 allocatedSize 不小于 0。
            if (allocatedSize < 0) {
                LOGGER.warn("allocatedSize 小于 0, 请联系开发人员, key: {}, allocatedSize: {}", key, allocatedSize);
                allocatedSize = 0;
            }
            totalAllocatedMemoryBufferSize += allocatedSize;
            LOGGER.debug(
                    "allocateBufferSize, key: {}, allocatedSize: {}, totalAllocatedMemoryBufferSize: {}",
                    key, allocatedSize, totalAllocatedMemoryBufferSize
            );
            // allocatedSize 为 0，意味着不需要改变原有的缓冲区，因此直接返回。
            if (allocatedSize == 0) {
                return;
            }
            // 复制原有的缓冲区。
            if (memoryBuffer.length == 0) {
                memoryBuffer = new byte[allocatedSize];
                return;
            }
            byte[] neoMemoryBuffer = new byte[memoryBuffer.length + allocatedSize];
            System.arraycopy(memoryBuffer, 0, neoMemoryBuffer, 0, memoryBuffer.length);
            memoryBuffer = neoMemoryBuffer;
        }

        /**
         * 释放内存缓冲区。
         *
         * <p>
         * 该方法本身没有线程安全性，调用该方法的线程应该持有临时存储的写锁，以及内存分配锁。
         */
        public void deallocateMemoryBuffer() {
            // 确认状态为工作中。
            makeSureStatusWorking();
            // 计算 deallocateSize。
            int alreadyAllocatedBufferSize;
            if (Objects.isNull(memoryBuffer)) {
                alreadyAllocatedBufferSize = 0;
            } else {
                alreadyAllocatedBufferSize = memoryBuffer.length;
            }
            int deallocateSize = alreadyAllocatedBufferSize - memoryBufferActualLength;
            // 确保 deallocateSize 不小于 0。
            if (deallocateSize < 0) {
                LOGGER.warn("deallocateSize 小于 0, 请联系开发人员, key: {}, deallocateSize: {}", key, deallocateSize);
                deallocateSize = 0;
            }
            totalAllocatedMemoryBufferSize -= deallocateSize;
            LOGGER.debug(
                    "deallocateBufferSize, key: {}, deallocateSize: {}, totalAllocatedMemoryBufferSize: {}",
                    key, deallocateSize, totalAllocatedMemoryBufferSize
            );
            // deallocateSize 为 0，意味着不需要改变原有的缓冲区，因此直接返回。
            if (deallocateSize == 0) {
                return;
            }
            // 复制原有的缓冲区。
            if (memoryBufferActualLength == 0) {
                memoryBuffer = new byte[0];
                return;
            }
            byte[] neoMemoryBuffer = new byte[memoryBufferActualLength];
            System.arraycopy(memoryBuffer, 0, neoMemoryBuffer, 0, memoryBufferActualLength);
            memoryBuffer = neoMemoryBuffer;
        }

        private void makeSureStatusWorking() {
            if (!Objects.equals(status, Constants.TEMPORARY_STORAGE_STATUS_WORKING)) {
                String message = "临时存储 " + key + " 的状态应该是 " + Constants.TEMPORARY_STORAGE_STATUS_WORKING +
                        ", 实际是 " + status;
                throw new IllegalStateException(message);
            }
        }
    }

    private class TemporaryStorageInputStream extends InputStream {

        private final TemporaryStorage temporaryStorage;

        private boolean closed = false;

        private int memoryBufferAnchorIndex = 0;
        private InputStream fileBufferInputStream;

        public TemporaryStorageInputStream(TemporaryStorage temporaryStorage) {
            this.temporaryStorage = temporaryStorage;
        }

        @Override
        public int available() throws IOException {
            makeSureOpen("流已经关闭");
            // 如果 temporaryStorage.memoryBuffer 中没有数据了：
            if (memoryBufferAnchorIndex >= temporaryStorage.memoryBufferActualLength) {
                // 如果 fileBuffer 已经被使用，则直接返回 fileBufferInputStream 的 available 方法的返回值。
                if (temporaryStorage.fileBufferUsed) {
                    mayOpenFileBufferInputStream();
                    return fileBufferInputStream.available();
                }
                // 否则，返回 0。
                return 0;
            }
            // 如果 temporaryStorage.memoryBuffer 中还有数据：
            // 计算剩余数据长度。
            int memoryBufferRemainingLength = temporaryStorage.memoryBufferActualLength - memoryBufferAnchorIndex;
            // 如果 fileBuffer 已经被使用，则返回 fileBuffer 的可用长度 + temporaryStorage.memoryBuffer 的剩余长度。
            if (temporaryStorage.fileBufferUsed) {
                mayOpenFileBufferInputStream();
                return fileBufferInputStream.available() + memoryBufferRemainingLength;
            }
            // 如果 fileBuffer 没有被使用，则返回 temporaryStorage.memoryBuffer 的剩余长度。
            return memoryBufferRemainingLength;
        }

        @Override
        public int read(@Nonnull byte[] b, int off, int len) throws IOException {
            makeSureOpen("流已经关闭");
            return internalRead(b, off, len);
        }

        @Override
        public int read(@Nonnull byte[] b) throws IOException {
            makeSureOpen("流已经关闭");
            return internalRead(b, 0, b.length);
        }

        private int internalRead(byte[] b, int i, int len) throws IOException {
            // 如果 temporaryStorage.memoryBuffer 中没有数据了：
            if (memoryBufferAnchorIndex >= temporaryStorage.memoryBufferActualLength) {
                // 如果 fileBuffer 已经被使用，则直接从 fileBuffer 读取。
                if (temporaryStorage.fileBufferUsed) {
                    mayOpenFileBufferInputStream();
                    return fileBufferInputStream.read(b, i, len);
                }
                // 否则，读取结束。
                return -1;
            }
            // 如果 temporaryStorage.memoryBuffer 中还有数据：
            // 计算剩余数据长度。
            int memoryBufferRemainingLength = temporaryStorage.memoryBufferActualLength - memoryBufferAnchorIndex;
            // 如果剩余数据长度大于等于 len，则直接从 temporaryStorage.memoryBuffer 读取。
            if (memoryBufferRemainingLength >= len) {
                System.arraycopy(temporaryStorage.memoryBuffer, memoryBufferAnchorIndex, b, i, len);
                memoryBufferAnchorIndex += len;
                return len;
            }
            // 如果剩余长度小于 len：
            // 如果 fileBuffer 已经被使用：
            if (temporaryStorage.fileBufferUsed) {
                // 将 temporaryStorage.memoryBuffer 中的数据读取完毕，之后再从 fileBuffer 读取剩余部分。
                System.arraycopy(
                        temporaryStorage.memoryBuffer, memoryBufferAnchorIndex, b, i, memoryBufferRemainingLength
                );
                memoryBufferAnchorIndex = temporaryStorage.memoryBufferActualLength;
                mayOpenFileBufferInputStream();
                return memoryBufferRemainingLength + fileBufferInputStream.read(
                        b, i + memoryBufferRemainingLength, len - memoryBufferRemainingLength
                );
            }
            // 如果 fileBuffer 没有被使用，则直接从 temporaryStorage.memoryBuffer 读取剩余部分，返回真实的读取长度。
            System.arraycopy(
                    temporaryStorage.memoryBuffer, memoryBufferAnchorIndex, b, i, memoryBufferRemainingLength
            );
            memoryBufferAnchorIndex = temporaryStorage.memoryBufferActualLength;
            return memoryBufferRemainingLength;
        }

        @Override
        public int read() throws IOException {
            makeSureOpen("流已经关闭");
            // 如果 temporaryStorage.memoryBuffer 中没有数据了：
            if (memoryBufferAnchorIndex >= temporaryStorage.memoryBufferActualLength) {
                // 如果 fileBuffer 已经被使用，则直接从 fileBuffer 读取。
                if (temporaryStorage.fileBufferUsed) {
                    mayOpenFileBufferInputStream();
                    return fileBufferInputStream.read();
                }
                // 否则，读取结束。
                return -1;
            }
            // 如果 temporaryStorage.memoryBuffer 中还有数据：
            return temporaryStorage.memoryBuffer[memoryBufferAnchorIndex++] & 0xFF;
        }

        @Override
        public long skip(long n) throws IOException {
            makeSureOpen("流已经关闭");
            // 如果 temporaryStorage.memoryBuffer 中没有数据了：
            if (memoryBufferAnchorIndex >= temporaryStorage.memoryBufferActualLength) {
                // 如果 fileBuffer 已经被使用，则直接从 fileBuffer 跳过。
                if (temporaryStorage.fileBufferUsed) {
                    mayOpenFileBufferInputStream();
                    return fileBufferInputStream.skip(n);
                }
                // 否则，跳过结束。
                return 0;
            }
            // 如果 temporaryStorage.memoryBuffer 中还有数据：
            // 计算剩余数据长度。
            int memoryBufferRemainingLength = temporaryStorage.memoryBufferActualLength - memoryBufferAnchorIndex;
            // 如果剩余数据长度大于等于 n，则直接从 temporaryStorage.memoryBuffer 跳过。
            if (memoryBufferRemainingLength >= n) {
                memoryBufferAnchorIndex += (int) n;
                return n;
            }
            // 如果剩余长度小于 n：
            // 如果 fileBuffer 已经被使用：
            if (temporaryStorage.fileBufferUsed) {
                // 将 temporaryStorage.memoryBuffer 中的数据跳过完毕，之后再从 fileBuffer 跳过剩余部分。
                memoryBufferAnchorIndex = temporaryStorage.memoryBufferActualLength;
                mayOpenFileBufferInputStream();
                return memoryBufferRemainingLength + fileBufferInputStream.skip(n - memoryBufferRemainingLength);
            }
            // 如果 fileBuffer 没有被使用，则直接从 temporaryStorage.memoryBuffer 跳过剩余部分，返回真实的跳过长度。
            memoryBufferAnchorIndex = temporaryStorage.memoryBufferActualLength;
            return memoryBufferRemainingLength;
        }

        @Override
        public void close() throws IOException {
            makeSureOpen("不能多次关闭流");

            // 根据情况关闭文件缓冲区输入流。
            try {
                mayCloseFileBufferInputStream();
            } catch (Exception e) {
                LOGGER.debug("关闭文件缓冲区输入流时发生异常, 将抛出异常...");
                closed = true;
                temporaryStorage.storageLock.readLock().unlock();
                throw new IOException("关闭文件缓冲区输入流时发生异常", e);
            }

            // 状态处理。
            closed = true;
            temporaryStorage.storageLock.readLock().unlock();
        }

        private void makeSureOpen(String exceptionMessage) throws IllegalStateException {
            if (closed) {
                throw new IllegalStateException(exceptionMessage);
            }
        }

        private void mayOpenFileBufferInputStream() throws IOException {
            if (Objects.nonNull(fileBufferInputStream)) {
                return;
            }
            fileBufferInputStream = Files.newInputStream(
                    temporaryStorage.fileBuffer.toPath(), StandardOpenOption.CREATE, StandardOpenOption.READ
            );
        }

        private void mayCloseFileBufferInputStream() throws Exception {
            if (Objects.isNull(fileBufferInputStream)) {
                return;
            }
            fileBufferInputStream.close();
            fileBufferInputStream = null;
        }
    }

    private class TemporaryStorageOutputStream extends OutputStream {

        private final TemporaryStorage temporaryStorage;

        private boolean closed = false;

        private OutputStream fileBufferOutputStream;

        public TemporaryStorageOutputStream(TemporaryStorage temporaryStorage) {
            this.temporaryStorage = temporaryStorage;
        }

        @Override
        public void write(@Nonnull byte[] b, int off, int len) throws IOException {
            makeSureOpen("流已经关闭");
            internalWrite(b, off, len);
        }

        @Override
        public void write(@Nonnull byte[] b) throws IOException {
            makeSureOpen("流已经关闭");
            internalWrite(b, 0, b.length);
        }

        private void internalWrite(byte[] b, int off, int len) throws IOException {
            // 特殊值判断：如果 len 为 0，则直接返回。
            if (len == 0) {
                return;
            }
            // 如果 temporaryStorage.memoryBuffer 的剩余空间不足以容纳 len 个字节且
            // temporaryStorage.fileBufferUsed 为 false，
            // 则调用一次 temporaryStorage.allocateMemoryBuffer 方法。
            boolean needToAllocateMemoryBuffer =
                    temporaryStorage.memoryBuffer.length - temporaryStorage.memoryBufferActualLength < len &&
                            !temporaryStorage.fileBufferUsed;
            if (needToAllocateMemoryBuffer) {
                memoryAllocationLock.lock();
                try {
                    temporaryStorage.allocateMemoryBuffer(len);
                } finally {
                    memoryAllocationLock.unlock();
                }
            }
            // 如果 temporaryStorage.memoryBuffer 已经被写满了：
            if (temporaryStorage.memoryBufferActualLength >= temporaryStorage.memoryBuffer.length) {
                // 置位 temporaryStorage.fileBufferUsed 标志。
                temporaryStorage.fileBufferUsed = true;
                // 将数据写入 fileBuffer。
                mayOpenFileBufferOutputStream();
                fileBufferOutputStream.write(b, off, len);
                temporaryStorage.fileBufferActualLength += len;
                return;
            }
            // 如果 temporaryStorage.memoryBuffer 还有剩余空间：
            // 计算 temporaryStorage.memoryBuffer 剩余空间。
            int memoryBufferRemainingLength =
                    temporaryStorage.memoryBuffer.length - temporaryStorage.memoryBufferActualLength;
            // 如果剩余空间大于等于 len，则直接写入 temporaryStorage.memoryBuffer。
            if (memoryBufferRemainingLength >= len) {
                System.arraycopy(
                        b, off, temporaryStorage.memoryBuffer, temporaryStorage.memoryBufferActualLength, len
                );
                temporaryStorage.memoryBufferActualLength += len;
                return;
            }
            // 如果剩余空间小于 len：
            // 先将 temporaryStorage.memoryBuffer 写满。
            System.arraycopy(
                    b, off, temporaryStorage.memoryBuffer, temporaryStorage.memoryBufferActualLength,
                    memoryBufferRemainingLength
            );
            temporaryStorage.memoryBufferActualLength = temporaryStorage.memoryBuffer.length;
            // 置位 temporaryStorage.fileBufferUsed 标志。
            temporaryStorage.fileBufferUsed = true;
            // 将剩余数据写入 fileBuffer。
            mayOpenFileBufferOutputStream();
            fileBufferOutputStream.write(b, off + memoryBufferRemainingLength, len - memoryBufferRemainingLength);
            temporaryStorage.fileBufferActualLength += len - memoryBufferRemainingLength;
        }

        @Override
        public void write(int b) throws IOException {
            makeSureOpen("流已经关闭");
            // 如果 temporaryStorage.memoryBuffer 已经被写满了且 temporaryStorage.fileBufferUsed 为 false，
            // 则调用一次 temporaryStorage.allocateMemoryBuffer 方法。
            boolean needToAllocateMemoryBuffer =
                    temporaryStorage.memoryBufferActualLength >= temporaryStorage.memoryBuffer.length &&
                            !temporaryStorage.fileBufferUsed;
            if (needToAllocateMemoryBuffer) {
                memoryAllocationLock.lock();
                try {
                    temporaryStorage.allocateMemoryBuffer(1);
                } finally {
                    memoryAllocationLock.unlock();
                }
            }
            // 如果 temporaryStorage.memoryBuffer 已经被写满了：
            if (temporaryStorage.memoryBufferActualLength >= temporaryStorage.memoryBuffer.length) {
                // 置位 temporaryStorage.fileBufferUsed 标志。
                temporaryStorage.fileBufferUsed = true;
                // 将数据写入 fileBuffer。
                mayOpenFileBufferOutputStream();
                fileBufferOutputStream.write(b);
                temporaryStorage.fileBufferActualLength++;
                return;
            }
            // 如果 temporaryStorage.memoryBuffer 还有剩余空间：
            temporaryStorage.memoryBuffer[temporaryStorage.memoryBufferActualLength++] = (byte) b;
        }

        @Override
        public void flush() throws IOException {
            makeSureOpen("流已经关闭");
            // 如果没有使用 fileBuffer，则直接返回。
            if (!temporaryStorage.fileBufferUsed) {
                return;
            }
            // 如果使用了 fileBuffer，则 flush fileBuffer。
            mayOpenFileBufferOutputStream();
            fileBufferOutputStream.flush();
        }

        @Override
        public void close() throws IOException {
            makeSureOpen("不能多次关闭流");

            // 根据情况关闭文件缓冲区输出流。
            try {
                mayCloseFileBufferOutputStream();
            } catch (Exception e) {
                LOGGER.debug("关闭文件缓冲区输出流时发生异常, 将抛出异常...");
                closed = true;
                temporaryStorage.storageLock.writeLock().unlock();
                throw new IOException("关闭文件缓冲区输出流时发生异常", e);
            }

            // 释放内存缓冲区。
            memoryAllocationLock.lock();
            try {
                temporaryStorage.deallocateMemoryBuffer();
            } catch (Exception e) {
                LOGGER.debug("释放内存缓冲区时发生异常, 将抛出异常...");
                closed = true;
                temporaryStorage.storageLock.writeLock().unlock();
                throw new IOException("释放内存缓冲区时发生异常", e);
            } finally {
                memoryAllocationLock.unlock();
            }

            // 状态处理。
            closed = true;
            temporaryStorage.storageLock.writeLock().unlock();
        }

        private void makeSureOpen(String exceptionMessage) throws IllegalStateException {
            if (closed) {
                throw new IllegalStateException(exceptionMessage);
            }
        }

        private void mayOpenFileBufferOutputStream() throws IOException {
            if (Objects.nonNull(fileBufferOutputStream)) {
                return;
            }
            fileBufferOutputStream = Files.newOutputStream(
                    temporaryStorage.fileBuffer.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        }

        private void mayCloseFileBufferOutputStream() throws IOException {
            if (Objects.isNull(fileBufferOutputStream)) {
                return;
            }
            fileBufferOutputStream.close();
            fileBufferOutputStream = null;
        }
    }
}
