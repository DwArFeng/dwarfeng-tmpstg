package com.dwarfeng.tmpstg.util;

import com.dwarfeng.subgrade.stack.exception.ServiceException;

/**
 * 服务异常代码。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public final class ServiceExceptionCodes {

    private static int EXCEPTION_CODE_OFFSET = 20000;

    public static final ServiceException.Code TEMPORARY_STORAGE_FAILED =
            new ServiceException.Code(offset(0), "temporary storage failed");
    public static final ServiceException.Code TEMPORARY_STORAGE_HANDLER_STOPPED =
            new ServiceException.Code(offset(1), "temporary storage handler stopped");
    public static final ServiceException.Code TEMPORARY_STORAGE_NOT_EXISTS =
            new ServiceException.Code(offset(2), "temporary storage not exists");
    public static final ServiceException.Code TEMPORARY_STORAGE_INVALID_STATUS =
            new ServiceException.Code(offset(3), "temporary storage invalid status");
    public static final ServiceException.Code TEMPORARY_STORAGE_STREAM_OPEN =
            new ServiceException.Code(offset(4), "temporary storage stream open");

    private static int offset(int i) {
        return EXCEPTION_CODE_OFFSET + i;
    }

    /**
     * 获取异常代号的偏移量。
     *
     * @return 异常代号的偏移量。
     */
    public static int getExceptionCodeOffset() {
        return EXCEPTION_CODE_OFFSET;
    }

    /**
     * 设置异常代号的偏移量。
     *
     * @param exceptionCodeOffset 指定的异常代号的偏移量。
     */
    public static void setExceptionCodeOffset(int exceptionCodeOffset) {
        // 设置 EXCEPTION_CODE_OFFSET 的值。
        EXCEPTION_CODE_OFFSET = exceptionCodeOffset;

        // 以新的 EXCEPTION_CODE_OFFSET 为基准，更新异常代码的值。
        TEMPORARY_STORAGE_FAILED.setCode(offset(0));
        TEMPORARY_STORAGE_HANDLER_STOPPED.setCode(offset(1));
        TEMPORARY_STORAGE_NOT_EXISTS.setCode(offset(2));
        TEMPORARY_STORAGE_INVALID_STATUS.setCode(offset(3));
        TEMPORARY_STORAGE_STREAM_OPEN.setCode(offset(4));
    }

    private ServiceExceptionCodes() {
        throw new IllegalStateException("禁止实例化");
    }
}
