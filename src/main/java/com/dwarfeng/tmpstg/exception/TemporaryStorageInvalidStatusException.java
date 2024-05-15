package com.dwarfeng.tmpstg.exception;

/**
 * 临时存储状态无效异常。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class TemporaryStorageInvalidStatusException extends TemporaryStorageException {

    private static final long serialVersionUID = -9150514385883879446L;

    private final String key;
    private final int expectedStatus;
    private final int actualStatus;

    public TemporaryStorageInvalidStatusException(String key, int expectedStatus, int actualStatus) {
        this.key = key;
        this.expectedStatus = expectedStatus;
        this.actualStatus = actualStatus;
    }

    public TemporaryStorageInvalidStatusException(Throwable cause, String key, int expectedStatus, int actualStatus) {
        super(cause);
        this.key = key;
        this.expectedStatus = expectedStatus;
        this.actualStatus = actualStatus;
    }

    @Override
    public String getMessage() {
        return "临时存储 " + key + " 的状态无效: 期望状态为 " + expectedStatus + ", 实际状态为 " + actualStatus;
    }
}
