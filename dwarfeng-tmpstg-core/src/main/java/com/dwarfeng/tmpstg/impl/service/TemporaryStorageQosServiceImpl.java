package com.dwarfeng.tmpstg.impl.service;

import com.dwarfeng.subgrade.sdk.exception.ServiceExceptionHelper;
import com.dwarfeng.subgrade.stack.exception.ServiceException;
import com.dwarfeng.subgrade.stack.exception.ServiceExceptionMapper;
import com.dwarfeng.subgrade.stack.log.LogLevel;
import com.dwarfeng.tmpstg.stack.bean.dto.TemporaryStorageInfo;
import com.dwarfeng.tmpstg.stack.handler.TemporaryStorageQosHandler;
import com.dwarfeng.tmpstg.stack.service.TemporaryStorageQosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

/**
 * 临时存储 QoS 服务实现。
 *
 * @author DwArFeng
 * @since 2.0.0
 */
public class TemporaryStorageQosServiceImpl implements TemporaryStorageQosService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemporaryStorageQosServiceImpl.class);

    private final TemporaryStorageQosHandler temporaryStorageQosHandler;
    private final ServiceExceptionMapper sem;

    public TemporaryStorageQosServiceImpl(
            TemporaryStorageQosHandler temporaryStorageQosHandler,
            ServiceExceptionMapper sem
    ) {
        this.temporaryStorageQosHandler = temporaryStorageQosHandler;
        this.sem = sem;
    }

    @PreDestroy
    public void preDestroy() {
        try {
            temporaryStorageQosHandler.stopAllManagedHandlers();
        } catch (Exception e) {
            LOGGER.warn("容器销毁时停止临时存储托管处理器失败，将忽略该异常", e);
        }
    }

    @Override
    public List<String> listHandlerNames() throws ServiceException {
        try {
            return temporaryStorageQosHandler.listHandlerNames();
        } catch (Exception e) {
            throw ServiceExceptionHelper.logParse("列出所有临时存储处理器名称时发生异常", LogLevel.WARN, e, sem);
        }
    }

    @Override
    public boolean isStarted(@Nullable String handlerName) throws ServiceException {
        try {
            return temporaryStorageQosHandler.isStarted(handlerName);
        } catch (Exception e) {
            throw ServiceExceptionHelper.logParse("查询临时存储处理器是否已启动时发生异常", LogLevel.WARN, e, sem);
        }
    }

    @Override
    public void start(@Nullable String handlerName) throws ServiceException {
        try {
            temporaryStorageQosHandler.start(handlerName);
        } catch (Exception e) {
            throw ServiceExceptionHelper.logParse("启动临时存储处理器时发生异常", LogLevel.WARN, e, sem);
        }
    }

    @Override
    public void stop(@Nullable String handlerName) throws ServiceException {
        try {
            temporaryStorageQosHandler.stop(handlerName);
        } catch (Exception e) {
            throw ServiceExceptionHelper.logParse("停止临时存储处理器时发生异常", LogLevel.WARN, e, sem);
        }
    }

    @Override
    public Collection<String> keys(@Nullable String handlerName) throws ServiceException {
        try {
            return temporaryStorageQosHandler.keys(handlerName);
        } catch (Exception e) {
            throw ServiceExceptionHelper.logParse("获取临时存储键集合时发生异常", LogLevel.WARN, e, sem);
        }
    }

    @Override
    public boolean exists(@Nullable String handlerName, @Nonnull String key) throws ServiceException {
        try {
            return temporaryStorageQosHandler.exists(handlerName, key);
        } catch (Exception e) {
            throw ServiceExceptionHelper.logParse("判断临时存储键是否存在时发生异常", LogLevel.WARN, e, sem);
        }
    }

    @Override
    public TemporaryStorageInfo inspect(@Nullable String handlerName, @Nonnull String key) throws ServiceException {
        try {
            return temporaryStorageQosHandler.inspect(handlerName, key);
        } catch (Exception e) {
            throw ServiceExceptionHelper.logParse("获取临时存储信息时发生异常", LogLevel.WARN, e, sem);
        }
    }

    @Override
    public String create(@Nullable String handlerName) throws ServiceException {
        try {
            return temporaryStorageQosHandler.create(handlerName);
        } catch (Exception e) {
            throw ServiceExceptionHelper.logParse("创建临时存储时发生异常", LogLevel.WARN, e, sem);
        }
    }

    @Override
    public InputStream openInputStream(@Nullable String handlerName, @Nonnull String key) throws ServiceException {
        try {
            return temporaryStorageQosHandler.openInputStream(handlerName, key);
        } catch (Exception e) {
            throw ServiceExceptionHelper.logParse("打开临时存储输入流时发生异常", LogLevel.WARN, e, sem);
        }
    }

    @Override
    public OutputStream openOutputStream(@Nullable String handlerName, @Nonnull String key) throws ServiceException {
        try {
            return temporaryStorageQosHandler.openOutputStream(handlerName, key);
        } catch (Exception e) {
            throw ServiceExceptionHelper.logParse("打开临时存储输出流时发生异常", LogLevel.WARN, e, sem);
        }
    }

    @Override
    public OutputStream openOutputStream(
            @Nullable String handlerName, @Nonnull String key, long expectedLength
    ) throws ServiceException {
        try {
            return temporaryStorageQosHandler.openOutputStream(handlerName, key, expectedLength);
        } catch (Exception e) {
            throw ServiceExceptionHelper.logParse("打开临时存储输出流时发生异常", LogLevel.WARN, e, sem);
        }
    }

    @Override
    public void dispose(@Nullable String handlerName, @Nonnull String key) throws ServiceException {
        try {
            temporaryStorageQosHandler.dispose(handlerName, key);
        } catch (Exception e) {
            throw ServiceExceptionHelper.logParse("释放临时存储时发生异常", LogLevel.WARN, e, sem);
        }
    }

    @Override
    public void remove(@Nullable String handlerName, @Nonnull String key) throws ServiceException {
        try {
            temporaryStorageQosHandler.remove(handlerName, key);
        } catch (Exception e) {
            throw ServiceExceptionHelper.logParse("移除临时存储时发生异常", LogLevel.WARN, e, sem);
        }
    }

    @Override
    public boolean removeIfDisposed(@Nullable String handlerName, @Nonnull String key) throws ServiceException {
        try {
            return temporaryStorageQosHandler.removeIfDisposed(handlerName, key);
        } catch (Exception e) {
            throw ServiceExceptionHelper.logParse("按状态移除临时存储时发生异常", LogLevel.WARN, e, sem);
        }
    }

    @Override
    public void disposeAndRemove(@Nullable String handlerName, @Nonnull String key) throws ServiceException {
        try {
            temporaryStorageQosHandler.disposeAndRemove(handlerName, key);
        } catch (Exception e) {
            throw ServiceExceptionHelper.logParse("释放并移除临时存储时发生异常", LogLevel.WARN, e, sem);
        }
    }

    @Override
    public void clearDisposed(@Nullable String handlerName) throws ServiceException {
        try {
            temporaryStorageQosHandler.clearDisposed(handlerName);
        } catch (Exception e) {
            throw ServiceExceptionHelper.logParse("清除已释放临时存储时发生异常", LogLevel.WARN, e, sem);
        }
    }

}
