package com.dwarfeng.tmpstg.stack.exception;

/**
 * 没有临时存储处理器异常。
 *
 * @author DwArFeng
 * @since 2.0.0
 */
public class NoTemporaryStorageHandlerPresentException extends TemporaryStorageQosException {

    private static final long serialVersionUID = 2254229881778100431L;

    public NoTemporaryStorageHandlerPresentException() {
    }

    public NoTemporaryStorageHandlerPresentException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return "应用上下文中没有临时存储处理器";
    }
}
