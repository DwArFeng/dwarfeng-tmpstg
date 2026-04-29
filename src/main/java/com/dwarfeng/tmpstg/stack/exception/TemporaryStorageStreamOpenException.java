package com.dwarfeng.tmpstg.stack.exception;

/**
 * 临时存储流打开异常。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class TemporaryStorageStreamOpenException extends TemporaryStorageException {

    private static final long serialVersionUID = -4344745192003743419L;
    
    private final String key;

    public TemporaryStorageStreamOpenException(String key) {
        this.key = key;
    }

    public TemporaryStorageStreamOpenException(Throwable cause, String key) {
        super(cause);
        this.key = key;
    }

    @Override
    public String getMessage() {
        return "临时存储 " + key + " 无法打开流";
    }
}
