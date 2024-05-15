package com.dwarfeng.tmpstg.struct;

import com.dwarfeng.dutil.basic.prog.Buildable;
import com.dwarfeng.tmpstg.util.TemporaryStorageConfigUtil;

/**
 * 临时存储配置。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class TemporaryStorageConfig {

    private final String temporaryFileDirectoryPath;

    private final String temporaryFilePrefix;
    private final String temporaryFileSuffix;

    private final int maxBufferSizePerStorage;
    private final long maxBufferSizeTotal;

    private final long clearDisposedInterval;
    private final long checkMemoryInterval;

    public TemporaryStorageConfig(
            String temporaryFileDirectoryPath, String temporaryFilePrefix, String temporaryFileSuffix,
            int maxBufferSizePerStorage, long maxBufferSizeTotal, long clearDisposedInterval, long checkMemoryInterval
    ) {
        this(
                temporaryFileDirectoryPath, temporaryFilePrefix, temporaryFileSuffix, maxBufferSizePerStorage,
                maxBufferSizeTotal, clearDisposedInterval, checkMemoryInterval, false
        );
    }

    private TemporaryStorageConfig(
            String temporaryFileDirectoryPath, String temporaryFilePrefix, String temporaryFileSuffix,
            int maxBufferSizePerStorage, long maxBufferSizeTotal, long clearDisposedInterval, long checkMemoryInterval,
            boolean paramReliable
    ) {
        // 如果参数不可靠，则检查参数。
        if (!paramReliable) {
            TemporaryStorageConfigUtil.checkTemporaryFileDirectoryPath(temporaryFileDirectoryPath);
            TemporaryStorageConfigUtil.checkTemporaryFilePrefix(temporaryFilePrefix);
            TemporaryStorageConfigUtil.checkTemporaryFileSuffix(temporaryFileSuffix);
            TemporaryStorageConfigUtil.checkBufferSize(maxBufferSizePerStorage, maxBufferSizeTotal);
            TemporaryStorageConfigUtil.checkClearDisposedInterval(clearDisposedInterval);
            TemporaryStorageConfigUtil.checkCheckMemoryInterval(checkMemoryInterval);
        }
        // 设置值。
        this.temporaryFileDirectoryPath = temporaryFileDirectoryPath;
        this.temporaryFilePrefix = temporaryFilePrefix;
        this.temporaryFileSuffix = temporaryFileSuffix;
        this.maxBufferSizePerStorage = maxBufferSizePerStorage;
        this.maxBufferSizeTotal = maxBufferSizeTotal;
        this.clearDisposedInterval = clearDisposedInterval;
        this.checkMemoryInterval = checkMemoryInterval;
    }

    public String getTemporaryFileDirectoryPath() {
        return temporaryFileDirectoryPath;
    }

    public String getTemporaryFilePrefix() {
        return temporaryFilePrefix;
    }

    public String getTemporaryFileSuffix() {
        return temporaryFileSuffix;
    }

    public int getMaxBufferSizePerStorage() {
        return maxBufferSizePerStorage;
    }

    public long getMaxBufferSizeTotal() {
        return maxBufferSizeTotal;
    }

    public long getClearDisposedInterval() {
        return clearDisposedInterval;
    }

    public long getCheckMemoryInterval() {
        return checkMemoryInterval;
    }

    @Override
    public String toString() {
        return "TemporaryStorageConfig{" +
                "temporaryFileDirectoryPath='" + temporaryFileDirectoryPath + '\'' +
                ", temporaryFilePrefix='" + temporaryFilePrefix + '\'' +
                ", temporaryFileSuffix='" + temporaryFileSuffix + '\'' +
                ", maxBufferSizePerStorage=" + maxBufferSizePerStorage +
                ", maxBufferSizeTotal=" + maxBufferSizeTotal +
                ", clearDisposedInterval=" + clearDisposedInterval +
                ", checkMemoryInterval=" + checkMemoryInterval +
                '}';
    }

    public static final class Builder implements Buildable<TemporaryStorageConfig> {

        public static final String DEFAULT_TEMPORARY_FILE_DIRECTORY_PATH = System.getProperty("java.io.tmpdir");

        public static final String DEFAULT_TEMPORARY_FILE_PREFIX = "tmpstg-";
        public static final String DEFAULT_TEMPORARY_FILE_SUFFIX = ".tmp";

        // 2048 bytes = 2 KiB
        public static final int DEFAULT_MAX_BUFFER_SIZE_PER_STORAGE = 2048;
        // 1048576 bytes = 1 MiB
        public static final long DEFAULT_MAX_BUFFER_SIZE_TOTAL = 1048576;

        public static final long DEFAULT_CLEAR_DISPOSED_INTERVAL = 300000;
        public static final long DEFAULT_CHECK_MEMORY_INTERVAL = 60000;

        private String temporaryFileDirectoryPath = DEFAULT_TEMPORARY_FILE_DIRECTORY_PATH;

        private String temporaryFilePrefix = DEFAULT_TEMPORARY_FILE_PREFIX;
        private String temporaryFileSuffix = DEFAULT_TEMPORARY_FILE_SUFFIX;

        private int maxBufferSizePerStorage = DEFAULT_MAX_BUFFER_SIZE_PER_STORAGE;
        private long maxBufferSizeTotal = DEFAULT_MAX_BUFFER_SIZE_TOTAL;

        private long clearDisposedInterval = DEFAULT_CLEAR_DISPOSED_INTERVAL;
        private long checkMemoryInterval = DEFAULT_CHECK_MEMORY_INTERVAL;

        public Builder() {
        }

        public Builder setTemporaryFileDirectoryPath(String temporaryFileDirectoryPath) {
            this.temporaryFileDirectoryPath = temporaryFileDirectoryPath;
            return this;
        }

        public Builder setTemporaryFilePrefix(String temporaryFilePrefix) {
            this.temporaryFilePrefix = temporaryFilePrefix;
            return this;
        }

        public Builder setTemporaryFileSuffix(String temporaryFileSuffix) {
            this.temporaryFileSuffix = temporaryFileSuffix;
            return this;
        }

        public Builder setMaxBufferSizePerStorage(int maxBufferSizePerStorage) {
            this.maxBufferSizePerStorage = maxBufferSizePerStorage;
            return this;
        }

        public Builder setMaxBufferSizeTotal(long maxBufferSizeTotal) {
            this.maxBufferSizeTotal = maxBufferSizeTotal;
            return this;
        }

        public Builder setClearDisposedInterval(long clearDisposedInterval) {
            this.clearDisposedInterval = clearDisposedInterval;
            return this;
        }

        public Builder setCheckMemoryInterval(long checkMemoryInterval) {
            this.checkMemoryInterval = checkMemoryInterval;
            return this;
        }

        @Override
        public TemporaryStorageConfig build() {
            // 检查参数。
            TemporaryStorageConfigUtil.checkTemporaryFileDirectoryPath(temporaryFileDirectoryPath);
            TemporaryStorageConfigUtil.checkTemporaryFilePrefix(temporaryFilePrefix);
            TemporaryStorageConfigUtil.checkTemporaryFileSuffix(temporaryFileSuffix);
            TemporaryStorageConfigUtil.checkBufferSize(maxBufferSizePerStorage, maxBufferSizeTotal);
            TemporaryStorageConfigUtil.checkClearDisposedInterval(clearDisposedInterval);
            TemporaryStorageConfigUtil.checkCheckMemoryInterval(checkMemoryInterval);
            // 构造并返回配置。
            return new TemporaryStorageConfig(
                    temporaryFileDirectoryPath, temporaryFilePrefix, temporaryFileSuffix, maxBufferSizePerStorage,
                    maxBufferSizeTotal, clearDisposedInterval, checkMemoryInterval, true
            );
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "temporaryFileDirectoryPath='" + temporaryFileDirectoryPath + '\'' +
                    ", temporaryFilePrefix='" + temporaryFilePrefix + '\'' +
                    ", temporaryFileSuffix='" + temporaryFileSuffix + '\'' +
                    ", maxBufferSizePerStorage=" + maxBufferSizePerStorage +
                    ", maxBufferSizeTotal=" + maxBufferSizeTotal +
                    ", clearDisposedInterval=" + clearDisposedInterval +
                    ", checkMemoryInterval=" + checkMemoryInterval +
                    '}';
        }
    }
}
