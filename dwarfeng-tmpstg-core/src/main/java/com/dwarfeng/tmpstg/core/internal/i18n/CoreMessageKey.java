package com.dwarfeng.tmpstg.core.internal.i18n;

import static com.dwarfeng.tmpstg.core.internal.i18n.CoreMessages.Catalog.SDK;

/**
 * Core 模块消息键。
 *
 * @author DwArFeng
 * @since 3.0.0
 */
public enum CoreMessageKey {

    SERVICE_EXCEPTION_TEMPORARY_STORAGE_FAILED(SDK, "service_exception.temporary_storage_failed"),
    SERVICE_EXCEPTION_TEMPORARY_STORAGE_HANDLER_STOPPED(
            SDK, "service_exception.temporary_storage_handler_stopped"
    ),
    SERVICE_EXCEPTION_TEMPORARY_STORAGE_NOT_EXISTS(SDK, "service_exception.temporary_storage_not_exists"),
    SERVICE_EXCEPTION_TEMPORARY_STORAGE_INVALID_STATUS(SDK, "service_exception.temporary_storage_invalid_status"),
    SERVICE_EXCEPTION_TEMPORARY_STORAGE_STREAM_OPEN(SDK, "service_exception.temporary_storage_stream_open"),
    SERVICE_EXCEPTION_TEMPORARY_STORAGE_QOS_FAILED(SDK, "service_exception.temporary_storage_qos_failed"),
    SERVICE_EXCEPTION_AMBIGUOUS_TEMPORARY_STORAGE_HANDLER(
            SDK, "service_exception.ambiguous_temporary_storage_handler"
    ),
    SERVICE_EXCEPTION_NO_TEMPORARY_STORAGE_HANDLER_PRESENT(
            SDK, "service_exception.no_temporary_storage_handler_present"
    ),
    SERVICE_EXCEPTION_TEMPORARY_STORAGE_QOS_HANDLER_NOT_FOUND(
            SDK, "service_exception.temporary_storage_qos_handler_not_found"
    );

    private final CoreMessages.Catalog catalog;
    private final String key;

    CoreMessageKey(CoreMessages.Catalog catalog, String key) {
        this.catalog = catalog;
        this.key = key;
    }

    CoreMessages.Catalog catalog() {
        return catalog;
    }

    /**
     * 获取资源消息键。
     *
     * @return 资源消息键。
     */
    public String key() {
        return key;
    }
}
