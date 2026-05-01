package com.dwarfeng.tmpstg.stack.exception;

/**
 * 临时存储处理器歧义异常。
 *
 * @author DwArFeng
 * @since 2.0.0
 */
public class AmbiguousTemporaryStorageHandlerException extends TemporaryStorageQosException {

    private static final long serialVersionUID = -4571857396258017840L;

    public AmbiguousTemporaryStorageHandlerException() {
    }

    public AmbiguousTemporaryStorageHandlerException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return "应用上下文中有多个临时存储处理器, 但是没有指定 handlerName";
    }
}
