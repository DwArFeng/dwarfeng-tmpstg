package com.dwarfeng.tmpstg.bean.dto;

import com.dwarfeng.subgrade.stack.bean.dto.Dto;

/**
 * 临时存储信息。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class TemporaryStorageInfo implements Dto {

    private static final long serialVersionUID = 5316163037017194153L;

    private String key;
    private int memoryBufferAllocatedLength;
    private int memoryBufferActualLength;
    private boolean fileBufferUsed;
    private long fileBufferActualLength;
    private int status;

    public TemporaryStorageInfo() {
    }

    public TemporaryStorageInfo(
            String key, int memoryBufferAllocatedLength, int memoryBufferActualLength, boolean fileBufferUsed,
            long fileBufferActualLength, int status
    ) {
        this.key = key;
        this.memoryBufferAllocatedLength = memoryBufferAllocatedLength;
        this.memoryBufferActualLength = memoryBufferActualLength;
        this.fileBufferUsed = fileBufferUsed;
        this.fileBufferActualLength = fileBufferActualLength;
        this.status = status;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getMemoryBufferAllocatedLength() {
        return memoryBufferAllocatedLength;
    }

    public void setMemoryBufferAllocatedLength(int memoryBufferAllocatedLength) {
        this.memoryBufferAllocatedLength = memoryBufferAllocatedLength;
    }

    public int getMemoryBufferActualLength() {
        return memoryBufferActualLength;
    }

    public void setMemoryBufferActualLength(int memoryBufferActualLength) {
        this.memoryBufferActualLength = memoryBufferActualLength;
    }

    public boolean isFileBufferUsed() {
        return fileBufferUsed;
    }

    public void setFileBufferUsed(boolean fileBufferUsed) {
        this.fileBufferUsed = fileBufferUsed;
    }

    public long getFileBufferActualLength() {
        return fileBufferActualLength;
    }

    public void setFileBufferActualLength(long fileBufferActualLength) {
        this.fileBufferActualLength = fileBufferActualLength;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TemporaryStorageInfo{" +
                "key='" + key + '\'' +
                ", memoryBufferAllocatedLength=" + memoryBufferAllocatedLength +
                ", memoryBufferActualLength=" + memoryBufferActualLength +
                ", fileBufferUsed=" + fileBufferUsed +
                ", fileBufferActualLength=" + fileBufferActualLength +
                ", status=" + status +
                '}';
    }
}
