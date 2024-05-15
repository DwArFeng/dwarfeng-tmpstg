package com.dwarfeng.tmpstg.exception;

import com.dwarfeng.subgrade.stack.exception.HandlerException;

/**
 * 临时存储异常。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class TemporaryStorageException extends HandlerException {

    private static final long serialVersionUID = -3147878294258240739L;

    public TemporaryStorageException() {
    }

    public TemporaryStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemporaryStorageException(String message) {
        super(message);
    }

    public TemporaryStorageException(Throwable cause) {
        super(cause);
    }
}
