package com.dwarfeng.tmpstg.exception;

/**
 * 临时存储不存在异常。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class TemporaryStorageNotExistsException extends TemporaryStorageException {

    private static final long serialVersionUID = 3290375744039156236L;

    private final String key;

    public TemporaryStorageNotExistsException(String key) {
        this.key = key;
    }

    public TemporaryStorageNotExistsException(Throwable cause, String key) {
        super(cause);
        this.key = key;
    }

    @Override
    public String getMessage() {
        return "临时存储 " + key + " 不存在";
    }
}
