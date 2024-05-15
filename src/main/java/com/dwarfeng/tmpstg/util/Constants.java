package com.dwarfeng.tmpstg.util;

/**
 * 常量类。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public final class Constants {

    /**
     * 临时存储状态：工作中。
     */
    public static final int TEMPORARY_STORAGE_STATUS_WORKING = 0;

    /**
     * 临时存储状态：已经销毁。
     */
    public static final int TEMPORARY_STORAGE_STATUS_DISPOSED = 1;

    private Constants() {
        throw new IllegalStateException("禁止实例化");
    }
}
