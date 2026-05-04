package com.dwarfeng.tmpstg.sdk.util;

/**
 * 常量类。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public final class Constants {

    // region 临时存储状态

    /**
     * 临时存储状态：工作中。
     */
    public static final int TEMPORARY_STORAGE_STATUS_WORKING = 0;

    /**
     * 临时存储状态：已经销毁。
     */
    public static final int TEMPORARY_STORAGE_STATUS_DISPOSED = 1;

    // endregion

    // region XSD 默认值

    /**
     * @since 2.0.0
     */
    public static final String XSD_DEFAULT_TEMPORARY_STORAGE_CONFIG_NAME = "temporaryStorageConfig";

    /**
     * @since 2.0.0
     */
    public static final String XSD_DEFAULT_TEMPORARY_STORAGE_HANDLER_NAME = "temporaryStorageHandler";

    /**
     * @since 2.0.0
     */
    public static final String XSD_DEFAULT_THREAD_POOL_TASK_SCHEDULER_NAME = "scheduler";

    /**
     * @since 2.0.0
     */
    public static final String XSD_DEFAULT_TEMPORARY_STORAGE_QOS_HANDLER_NAME = "temporaryStorageQosHandler";

    /**
     * @since 2.0.0
     */
    public static final String XSD_DEFAULT_TEMPORARY_STORAGE_QOS_SERVICE_NAME = "temporaryStorageQosService";

    /**
     * @since 2.0.0
     */
    public static final String XSD_DEFAULT_SERVICE_EXCEPTION_MAPPER_NAME = "mapServiceExceptionMapper";

    /**
     * @since 2.0.0
     */
    public static final String XSD_DEFAULT_TEMPORARY_STORAGE_HANDLER_AUTO_START_VALUE = "true";

    // endregion

    private Constants() {
        throw new IllegalStateException("禁止实例化");
    }
}
