package com.dwarfeng.tmpstg.sdk.exception;

import com.dwarfeng.subgrade.basic.stack.exception.ServiceException;
import com.dwarfeng.tmpstg.core.internal.i18n.CoreMessageKey;
import com.dwarfeng.tmpstg.core.internal.i18n.CoreMessages;

import java.util.function.Supplier;

/**
 * TMPSTG 服务异常代码 Supplier。
 *
 * <p>
 * Supplier 在调用时根据当前偏移量和消息语言环境创建独立的 {@link ServiceException.Code} 快照。
 *
 * @author DwArFeng
 * @since 3.0.0
 */
public final class ServiceExceptionCodeSuppliers {

    private static volatile int EXCEPTION_CODE_OFFSET = 20000;

    /**
     * 临时存储根异常。
     */
    public static final Supplier<ServiceException.Code> TEMPORARY_STORAGE_FAILED =
            () -> new ServiceException.Code(
                    offset(0), CoreMessages.message(CoreMessageKey.SERVICE_EXCEPTION_TEMPORARY_STORAGE_FAILED)
            );

    /**
     * 临时存储处理器已停止异常。
     */
    public static final Supplier<ServiceException.Code> TEMPORARY_STORAGE_HANDLER_STOPPED =
            () -> new ServiceException.Code(
                    offset(1), CoreMessages.message(CoreMessageKey.SERVICE_EXCEPTION_TEMPORARY_STORAGE_HANDLER_STOPPED)
            );

    /**
     * 临时存储不存在异常。
     */
    public static final Supplier<ServiceException.Code> TEMPORARY_STORAGE_NOT_EXISTS =
            () -> new ServiceException.Code(
                    offset(2), CoreMessages.message(CoreMessageKey.SERVICE_EXCEPTION_TEMPORARY_STORAGE_NOT_EXISTS)
            );

    /**
     * 临时存储状态无效异常。
     */
    public static final Supplier<ServiceException.Code> TEMPORARY_STORAGE_INVALID_STATUS =
            () -> new ServiceException.Code(
                    offset(3), CoreMessages.message(CoreMessageKey.SERVICE_EXCEPTION_TEMPORARY_STORAGE_INVALID_STATUS)
            );

    /**
     * 临时存储流打开异常。
     */
    public static final Supplier<ServiceException.Code> TEMPORARY_STORAGE_STREAM_OPEN =
            () -> new ServiceException.Code(
                    offset(4), CoreMessages.message(CoreMessageKey.SERVICE_EXCEPTION_TEMPORARY_STORAGE_STREAM_OPEN)
            );

    /**
     * 临时存储 QoS 根异常。
     */
    public static final Supplier<ServiceException.Code> TEMPORARY_STORAGE_QOS_FAILED =
            () -> new ServiceException.Code(
                    offset(5), CoreMessages.message(CoreMessageKey.SERVICE_EXCEPTION_TEMPORARY_STORAGE_QOS_FAILED)
            );

    /**
     * 临时存储处理器歧义异常。
     */
    public static final Supplier<ServiceException.Code> AMBIGUOUS_TEMPORARY_STORAGE_HANDLER =
            () -> new ServiceException.Code(
                    offset(6),
                    CoreMessages.message(CoreMessageKey.SERVICE_EXCEPTION_AMBIGUOUS_TEMPORARY_STORAGE_HANDLER)
            );

    /**
     * 临时存储处理器不存在异常。
     */
    public static final Supplier<ServiceException.Code> NO_TEMPORARY_STORAGE_HANDLER_PRESENT =
            () -> new ServiceException.Code(
                    offset(7),
                    CoreMessages.message(CoreMessageKey.SERVICE_EXCEPTION_NO_TEMPORARY_STORAGE_HANDLER_PRESENT)
            );

    /**
     * 临时存储 QoS 处理器未找到异常。
     */
    public static final Supplier<ServiceException.Code> TEMPORARY_STORAGE_QOS_HANDLER_NOT_FOUND =
            () -> new ServiceException.Code(
                    offset(8), CoreMessages.message(
                    CoreMessageKey.SERVICE_EXCEPTION_TEMPORARY_STORAGE_QOS_HANDLER_NOT_FOUND
            )
            );

    private ServiceExceptionCodeSuppliers() {
        throw new IllegalStateException("禁止实例化");
    }

    private static int offset(int index) {
        return EXCEPTION_CODE_OFFSET + index;
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
        EXCEPTION_CODE_OFFSET = exceptionCodeOffset;
    }
}
