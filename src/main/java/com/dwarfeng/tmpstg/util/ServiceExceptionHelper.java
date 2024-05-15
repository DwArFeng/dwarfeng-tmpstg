package com.dwarfeng.tmpstg.util;

import com.dwarfeng.subgrade.stack.exception.ServiceException;
import com.dwarfeng.tmpstg.exception.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 异常的帮助工具类。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public final class ServiceExceptionHelper {

    /**
     * 向指定的映射中添加 dwarfeng-ftp 默认的目标映射。
     * <p>该方法可以在配置类中快速的搭建目标映射。</p>
     *
     * @param map 指定的映射，允许为null。
     * @return 添加了默认目标的映射。
     */
    public static Map<Class<? extends Exception>, ServiceException.Code> putDefaultDestination(
            Map<Class<? extends Exception>, ServiceException.Code> map) {
        if (Objects.isNull(map)) {
            map = new HashMap<>();
        }

        map.put(TemporaryStorageException.class, ServiceExceptionCodes.TEMPORARY_STORAGE_FAILED);
        map.put(TemporaryStorageHandlerStoppedException.class, ServiceExceptionCodes.TEMPORARY_STORAGE_HANDLER_STOPPED);
        map.put(TemporaryStorageInvalidStatusException.class, ServiceExceptionCodes.TEMPORARY_STORAGE_INVALID_STATUS);
        map.put(TemporaryStorageNotExistsException.class, ServiceExceptionCodes.TEMPORARY_STORAGE_NOT_EXISTS);
        map.put(TemporaryStorageStreamOpenException.class, ServiceExceptionCodes.TEMPORARY_STORAGE_STREAM_OPEN);

        return map;
    }

    private ServiceExceptionHelper() {
        throw new IllegalStateException("禁止外部实例化");
    }
}
