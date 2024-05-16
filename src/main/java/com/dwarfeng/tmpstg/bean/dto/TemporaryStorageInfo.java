package com.dwarfeng.tmpstg.bean.dto;

import com.dwarfeng.subgrade.stack.bean.dto.Dto;

/**
 * 临时存储信息。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class TemporaryStorageInfo implements Dto {

    private static final long serialVersionUID = 4405589290801960158L;

    private String key;
    private int memoryBufferAllocatedLength;
    private int memoryBufferActualLength;
    private boolean fileBufferUsed;
    private long fileBufferActualLength;
    private int status;

    /**
     * 临时存储存放的内容的长度。
     *
     * <p>
     * 该字段的值应该等于 <code>memoryBufferActualLength + fileBufferActualLength</code>。
     *
     * @since 1.0.1
     */
    private long contentLength;

    public TemporaryStorageInfo() {
    }

    public TemporaryStorageInfo(
            String key, int memoryBufferAllocatedLength, int memoryBufferActualLength, boolean fileBufferUsed,
            long fileBufferActualLength, int status, long contentLength
    ) {
        this.key = key;
        this.memoryBufferAllocatedLength = memoryBufferAllocatedLength;
        this.memoryBufferActualLength = memoryBufferActualLength;
        this.fileBufferUsed = fileBufferUsed;
        this.fileBufferActualLength = fileBufferActualLength;
        this.status = status;
        this.contentLength = contentLength;
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

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
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
                ", contentLength=" + contentLength +
                '}';
    }
}
