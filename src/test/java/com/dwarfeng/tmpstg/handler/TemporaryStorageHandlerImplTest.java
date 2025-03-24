package com.dwarfeng.tmpstg.handler;

import com.dwarfeng.dutil.basic.io.IOUtil;
import com.dwarfeng.tmpstg.bean.dto.TemporaryStorageInfo;
import com.dwarfeng.tmpstg.exception.TemporaryStorageInvalidStatusException;
import com.dwarfeng.tmpstg.exception.TemporaryStorageNotExistsException;
import com.dwarfeng.tmpstg.util.Constants;
import com.dwarfeng.tmpstg.util.ContentUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/application-context*.xml")
public class TemporaryStorageHandlerImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemporaryStorageHandlerImplTest.class);

    private static final String SPEL_DEFAULT_MAX_BUFFER_SIZE_PER_STORAGE = "${tmpstg.max_buffer_size_per_storage:" +
            "#{T(com.dwarfeng.tmpstg.struct.TemporaryStorageConfig$Builder).DEFAULT_MAX_BUFFER_SIZE_PER_STORAGE}}";

    @Autowired
    private TemporaryStorageHandler temporaryStorageHandler;

    @Autowired
    private ThreadPoolTaskExecutor executor;

    // SPEL 太长，故使用常量缩短长度。
    @Value(SPEL_DEFAULT_MAX_BUFFER_SIZE_PER_STORAGE)
    private int maxBufferSizePerStorage;

    // -----------------------------------------------------------读写测试-----------------------------------------------------------
    @Test
    public void testWriteAndReadOnceWithSmallContent() throws Exception {
        byte[] originalContent = ContentUtil.randomContent(maxBufferSizePerStorage - 100);
        String key = temporaryStorageHandler.create();
        try (OutputStream out = temporaryStorageHandler.openOutputStream(key)) {
            // 一次写 128 个字节，一直写完所有内容。
            for (int i = 0; i < originalContent.length; i += 128) {
                out.write(originalContent, i, Math.min(128, originalContent.length - i));
            }
            out.flush();
        }
        byte[] testContent;
        try (
                InputStream in = temporaryStorageHandler.openInputStream(key);
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            IOUtil.trans(in, out, 4096);
            testContent = out.toByteArray();
        }
        assertEquals(originalContent.length, testContent.length);
        assertArrayEquals(originalContent, testContent);
        TemporaryStorageInfo info = temporaryStorageHandler.inspect(key);
        assertEquals(key, info.getKey());
        assertEquals(maxBufferSizePerStorage - 100, info.getMemoryBufferAllocatedLength());
        assertEquals(maxBufferSizePerStorage - 100, info.getMemoryBufferActualLength());
        assertFalse(info.isFileBufferUsed());
        assertEquals(0, info.getFileBufferActualLength());
        assertEquals(Constants.TEMPORARY_STORAGE_STATUS_WORKING, info.getStatus());
        assertEquals(originalContent.length, info.getContentLength());
        temporaryStorageHandler.dispose(key);
        info = temporaryStorageHandler.inspect(key);
        assertEquals(key, info.getKey());
        assertEquals(0, info.getMemoryBufferAllocatedLength());
        assertEquals(0, info.getMemoryBufferActualLength());
        assertFalse(info.isFileBufferUsed());
        assertEquals(0, info.getFileBufferActualLength());
        assertEquals(Constants.TEMPORARY_STORAGE_STATUS_DISPOSED, info.getStatus());
        assertEquals(0, info.getContentLength());
    }

    @Test
    public void testWriteAndReadOnceWithCriticalContent() throws Exception {
        byte[] originalContent = ContentUtil.randomContent(maxBufferSizePerStorage);
        String key = temporaryStorageHandler.create();
        try (OutputStream out = temporaryStorageHandler.openOutputStream(key, 1)) {
            // 一次写 128 个字节，一直写完所有内容。
            for (int i = 0; i < originalContent.length; i += 128) {
                out.write(originalContent, i, Math.min(128, originalContent.length - i));
            }
            out.flush();
        }
        byte[] testContent;
        try (
                InputStream in = temporaryStorageHandler.openInputStream(key);
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            IOUtil.trans(in, out, 4096);
            testContent = out.toByteArray();
        }
        assertEquals(originalContent.length, testContent.length);
        assertArrayEquals(originalContent, testContent);
        TemporaryStorageInfo info = temporaryStorageHandler.inspect(key);
        assertEquals(key, info.getKey());
        assertEquals(maxBufferSizePerStorage, info.getMemoryBufferAllocatedLength());
        assertEquals(maxBufferSizePerStorage, info.getMemoryBufferActualLength());
        assertFalse(info.isFileBufferUsed());
        assertEquals(0, info.getFileBufferActualLength());
        assertEquals(Constants.TEMPORARY_STORAGE_STATUS_WORKING, info.getStatus());
        assertEquals(originalContent.length, info.getContentLength());
        temporaryStorageHandler.dispose(key);
        info = temporaryStorageHandler.inspect(key);
        assertEquals(key, info.getKey());
        assertEquals(0, info.getMemoryBufferAllocatedLength());
        assertEquals(0, info.getMemoryBufferActualLength());
        assertFalse(info.isFileBufferUsed());
        assertEquals(0, info.getFileBufferActualLength());
        assertEquals(Constants.TEMPORARY_STORAGE_STATUS_DISPOSED, info.getStatus());
        assertEquals(0, info.getContentLength());
    }

    @Test
    public void testWriteAndReadOnceWithLargeContent() throws Exception {
        byte[] originalContent = ContentUtil.randomContent(maxBufferSizePerStorage + 100);
        String key = temporaryStorageHandler.create();
        try (OutputStream out = temporaryStorageHandler.openOutputStream(key, 1)) {
            // 一次写 128 个字节，一直写完所有内容。
            for (int i = 0; i < originalContent.length; i += 128) {
                out.write(originalContent, i, Math.min(128, originalContent.length - i));
            }
            out.flush();
        }
        byte[] testContent;
        try (
                InputStream in = temporaryStorageHandler.openInputStream(key);
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            IOUtil.trans(in, out, 4096);
            testContent = out.toByteArray();
        }
        assertEquals(originalContent.length, testContent.length);
        assertArrayEquals(originalContent, testContent);
        TemporaryStorageInfo info = temporaryStorageHandler.inspect(key);
        assertEquals(key, info.getKey());
        assertEquals(maxBufferSizePerStorage, info.getMemoryBufferAllocatedLength());
        assertEquals(maxBufferSizePerStorage, info.getMemoryBufferActualLength());
        assertTrue(info.isFileBufferUsed());
        assertEquals(100, info.getFileBufferActualLength());
        assertEquals(Constants.TEMPORARY_STORAGE_STATUS_WORKING, info.getStatus());
        assertEquals(originalContent.length, info.getContentLength());
        temporaryStorageHandler.dispose(key);
        info = temporaryStorageHandler.inspect(key);
        assertEquals(key, info.getKey());
        assertEquals(0, info.getMemoryBufferAllocatedLength());
        assertEquals(0, info.getMemoryBufferActualLength());
        assertFalse(info.isFileBufferUsed());
        assertEquals(0, info.getFileBufferActualLength());
        assertEquals(Constants.TEMPORARY_STORAGE_STATUS_DISPOSED, info.getStatus());
        assertEquals(0, info.getContentLength());
    }

    @Test
    public void writeAndReadConcurrently() throws Exception {
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            futures.add(executor.submit(this::writeAndReadConcurrentlySingleThread));
        }
        for (Future<?> future : futures) {
            future.get();
        }
    }

    private void writeAndReadConcurrentlySingleThread() {
        try {
            byte[] originalContent = ContentUtil.randomContent(maxBufferSizePerStorage + 100);
            String key = temporaryStorageHandler.create();
            try (OutputStream out = temporaryStorageHandler.openOutputStream(key, 1)) {
                out.write(originalContent);
                out.flush();
            }
            // 多次读取，每次读取后检查内容及状态。
            byte[] testContent;
            for (int j = 0; j < 5; j++) {
                try (
                        InputStream in = temporaryStorageHandler.openInputStream(key);
                        ByteArrayOutputStream out = new ByteArrayOutputStream()
                ) {
                    IOUtil.trans(in, out, 4096);
                    testContent = out.toByteArray();
                }
                assertEquals(originalContent.length, testContent.length);
                assertArrayEquals(originalContent, testContent);
                TemporaryStorageInfo info = temporaryStorageHandler.inspect(key);
                assertEquals(key, info.getKey());
                assertTrue(info.isFileBufferUsed());
                assertEquals(Constants.TEMPORARY_STORAGE_STATUS_WORKING, info.getStatus());
            }
            temporaryStorageHandler.dispose(key);
            TemporaryStorageInfo info = temporaryStorageHandler.inspect(key);
            assertEquals(key, info.getKey());
            assertEquals(0, info.getMemoryBufferAllocatedLength());
            assertEquals(0, info.getMemoryBufferActualLength());
            assertFalse(info.isFileBufferUsed());
            assertEquals(0, info.getFileBufferActualLength());
            assertEquals(Constants.TEMPORARY_STORAGE_STATUS_DISPOSED, info.getStatus());
            assertEquals(0, info.getContentLength());
        } catch (Exception e) {
            // 如果发生异常，记录异常并标记测试失败。
            LOGGER.warn("测试过程中发生异常", e);
            fail();
        }
    }

    // -----------------------------------------------------------其它方法测试-----------------------------------------------------------
    @Test
    public void testExists() throws Exception {
        String key = temporaryStorageHandler.create();
        assertTrue(temporaryStorageHandler.exists(key));
        temporaryStorageHandler.dispose(key);
        assertTrue(temporaryStorageHandler.exists(key));
        temporaryStorageHandler.remove(key);
        assertFalse(temporaryStorageHandler.exists(key));
        assertFalse(temporaryStorageHandler.exists(UUID.randomUUID().toString()));
    }

    @Test
    public void testRemove() throws Exception {
        String key = temporaryStorageHandler.create();
        assertThrows(TemporaryStorageInvalidStatusException.class, () -> temporaryStorageHandler.remove(key));
        temporaryStorageHandler.dispose(key);
        temporaryStorageHandler.remove(key);
        assertThrows(TemporaryStorageNotExistsException.class, () -> temporaryStorageHandler.remove(key));
        assertThrows(
                TemporaryStorageNotExistsException.class,
                () -> temporaryStorageHandler.remove(UUID.randomUUID().toString())
        );
    }

    @Test
    public void testRemoveIfDisposed() throws Exception {
        String key = temporaryStorageHandler.create();
        assertFalse(temporaryStorageHandler.removeIfDisposed(key));
        temporaryStorageHandler.dispose(key);
        assertTrue(temporaryStorageHandler.removeIfDisposed(key));
        assertThrows(
                TemporaryStorageNotExistsException.class,
                () -> temporaryStorageHandler.removeIfDisposed(key)
        );
        assertThrows(
                TemporaryStorageNotExistsException.class,
                () -> temporaryStorageHandler.removeIfDisposed(UUID.randomUUID().toString())
        );
    }

    @Test
    public void testDisposeAndRemove() throws Exception {
        String key = temporaryStorageHandler.create();
        assertTrue(temporaryStorageHandler.exists(key));
        temporaryStorageHandler.disposeAndRemove(key);
        assertFalse(temporaryStorageHandler.exists(key));
        assertThrows(
                TemporaryStorageNotExistsException.class,
                () -> temporaryStorageHandler.disposeAndRemove(key)
        );
        assertThrows(
                TemporaryStorageNotExistsException.class,
                () -> temporaryStorageHandler.disposeAndRemove(UUID.randomUUID().toString())
        );
    }

    @Test
    public void testClearDisposed() throws Exception {
        String key1 = temporaryStorageHandler.create();
        String key2 = temporaryStorageHandler.create();
        assertTrue(temporaryStorageHandler.exists(key1));
        assertTrue(temporaryStorageHandler.exists(key2));
        temporaryStorageHandler.dispose(key1);
        temporaryStorageHandler.clearDisposed();
        assertFalse(temporaryStorageHandler.exists(key1));
        assertTrue(temporaryStorageHandler.exists(key2));
        temporaryStorageHandler.dispose(key2);
        temporaryStorageHandler.clearDisposed();
        assertFalse(temporaryStorageHandler.exists(key1));
        assertFalse(temporaryStorageHandler.exists(key2));
    }
}
