package com.dwarfeng.tmpstg.stack.exception;

/**
 * 临时存储处理器未找到异常。
 *
 * @author DwArFeng
 * @since 2.0.0
 */
public class TemporaryStorageHandlerNotFoundException extends TemporaryStorageQosException {

    private static final long serialVersionUID = 4688133310078473683L;

    private final String handlerName;

    public TemporaryStorageHandlerNotFoundException(String handlerName) {
        this.handlerName = handlerName;
    }

    public TemporaryStorageHandlerNotFoundException(Throwable cause, String handlerName) {
        super(cause);
        this.handlerName = handlerName;
    }

    @Override
    public String getMessage() {
        return "应用上下文中没有找到名称为 " + handlerName + " 的临时存储处理器";
    }
}
