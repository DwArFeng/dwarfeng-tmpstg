package com.dwarfeng.tmpstg.sdk.exception;

import com.dwarfeng.subgrade.basic.stack.exception.ServiceException;
import com.dwarfeng.tmpstg.stack.exception.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 异常的帮助工具类。
 *
 * @author DwArFeng
 * @since 3.0.0
 */
public final class ServiceExceptionHelper {

    /**
     * 向指定的映射中添加 dwarfeng-tmpstg 默认的目标映射。
     *
     * <p>
     * 该方法可以在配置类中快速的搭建目标映射。
     *
     * @param map 指定的映射，允许为 null。
     * @return 添加了默认目标的映射。
     */
    public static Map<Class<? extends Exception>, Supplier<ServiceException.Code>> putDefaultDestination(
            Map<Class<? extends Exception>, Supplier<ServiceException.Code>> map
    ) {
        if (Objects.isNull(map)) {
            map = new HashMap<>();
        }

        map.put(TemporaryStorageException.class, ServiceExceptionCodeSuppliers.TEMPORARY_STORAGE_FAILED);
        map.put(
                TemporaryStorageHandlerStoppedException.class,
                ServiceExceptionCodeSuppliers.TEMPORARY_STORAGE_HANDLER_STOPPED
        );
        map.put(
                TemporaryStorageInvalidStatusException.class,
                ServiceExceptionCodeSuppliers.TEMPORARY_STORAGE_INVALID_STATUS
        );
        map.put(TemporaryStorageNotExistsException.class, ServiceExceptionCodeSuppliers.TEMPORARY_STORAGE_NOT_EXISTS);
        map.put(TemporaryStorageStreamOpenException.class, ServiceExceptionCodeSuppliers.TEMPORARY_STORAGE_STREAM_OPEN);
        map.put(TemporaryStorageQosException.class, ServiceExceptionCodeSuppliers.TEMPORARY_STORAGE_QOS_FAILED);
        map.put(
                AmbiguousTemporaryStorageHandlerException.class,
                ServiceExceptionCodeSuppliers.AMBIGUOUS_TEMPORARY_STORAGE_HANDLER
        );
        map.put(
                NoTemporaryStorageHandlerPresentException.class,
                ServiceExceptionCodeSuppliers.NO_TEMPORARY_STORAGE_HANDLER_PRESENT
        );
        map.put(
                TemporaryStorageHandlerNotFoundException.class,
                ServiceExceptionCodeSuppliers.TEMPORARY_STORAGE_QOS_HANDLER_NOT_FOUND
        );

        return map;
    }

    private ServiceExceptionHelper() {
        throw new IllegalStateException("禁止外部实例化");
    }
}
