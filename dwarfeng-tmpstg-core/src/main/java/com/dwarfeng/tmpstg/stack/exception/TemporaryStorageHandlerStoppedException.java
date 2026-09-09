package com.dwarfeng.tmpstg.stack.exception;

import java.io.Serial;

/**
 * 临时存储处理器停止异常。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class TemporaryStorageHandlerStoppedException extends TemporaryStorageException {

    @Serial
    private static final long serialVersionUID = 1577967064408199816L;

    public TemporaryStorageHandlerStoppedException() {
    }

    public TemporaryStorageHandlerStoppedException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return "临时存储处理器处于停止状态";
    }
}
