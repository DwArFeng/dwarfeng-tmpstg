package com.dwarfeng.tmpstg.stack.exception;

import com.dwarfeng.subgrade.stack.exception.HandlerException;

/**
 * 临时存储 QoS 异常。
 *
 * @author DwArFeng
 * @since 2.0.0
 */
public class TemporaryStorageQosException extends HandlerException {

    private static final long serialVersionUID = 2391061022170965724L;

    public TemporaryStorageQosException() {
    }

    public TemporaryStorageQosException(String message) {
        super(message);
    }

    public TemporaryStorageQosException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemporaryStorageQosException(Throwable cause) {
        super(cause);
    }
}
