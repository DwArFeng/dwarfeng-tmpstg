package com.dwarfeng.tmpstg.util;

import java.io.File;
import java.util.Objects;

/**
 * 临时存储配置工具类。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public final class TemporaryStorageConfigUtil {

    /**
     * 检查指定的临时文件目录路径是否合法。
     *
     * @param temporaryFileDirectoryPath 指定的临时文件目录路径。
     */
    public static void checkTemporaryFileDirectoryPath(String temporaryFileDirectoryPath) {
        if (Objects.isNull(temporaryFileDirectoryPath)) {
            throw new NullPointerException("临时文件目录路径不能为 null");
        }
        File temporaryFileDirectory = new File(temporaryFileDirectoryPath);
        // 确认目录是否存在，不存在则尝试创建，创建失败则抛出异常。
        if (!temporaryFileDirectory.exists()) {
            if (!temporaryFileDirectory.mkdirs()) {
                throw new IllegalArgumentException("无法创建临时文件目录");
            }
        }
        // 目录不能是文件。
        if (temporaryFileDirectory.isFile()) {
            throw new IllegalArgumentException("临时文件目录不能是文件");
        }
        // 需要有读写权限。
        if (!temporaryFileDirectory.canRead() || !temporaryFileDirectory.canWrite()) {
            throw new IllegalArgumentException("临时文件目录需要有读写权限");
        }
    }

    /**
     * 检查指定的临时文件前缀是否合法。
     *
     * @param temporaryFilePrefix 指定的临时文件前缀。
     */
    public static void checkTemporaryFilePrefix(String temporaryFilePrefix) {
        if (Objects.isNull(temporaryFilePrefix)) {
            throw new NullPointerException("临时文件前缀不能为 null");
        }
    }

    /**
     * 检查指定的临时文件后缀是否合法。
     *
     * @param temporaryFileSuffix 指定的临时文件后缀。
     */
    public static void checkTemporaryFileSuffix(String temporaryFileSuffix) {
        if (Objects.isNull(temporaryFileSuffix)) {
            throw new NullPointerException("临时文件后缀不能为 null");
        }
    }

    /**
     * 检查指定的缓冲区大小是否合法。
     *
     * @param maxBufferSizePerStorage 指定的缓冲区大小。
     * @param maxBufferSizeTotal      指定的缓冲区大小。
     */
    public static void checkBufferSize(int maxBufferSizePerStorage, long maxBufferSizeTotal) {
        if (maxBufferSizePerStorage <= 0) {
            throw new IllegalArgumentException("单个存储的最大缓冲区大小必须大于 0");
        }
        if (maxBufferSizeTotal <= 0) {
            throw new IllegalArgumentException("总的最大缓冲区大小必须大于 0");
        }
        if (maxBufferSizePerStorage > maxBufferSizeTotal) {
            throw new IllegalArgumentException("单个存储的最大缓冲区大小不能大于总的最大缓冲区大小");
        }
    }

    /**
     * 检查指定的清理已释放资源的时间间隔是否合法。
     *
     * @param clearDisposedInterval 指定的清理已释放资源的时间间隔。
     */
    @SuppressWarnings("EmptyMethod")
    public static void checkClearDisposedInterval(long clearDisposedInterval) {
        // 允许为负数，表示不自动清理。
    }

    /**
     * 检查指定的检查内存的时间间隔是否合法。
     *
     * @param checkMemoryInterval 指定的检查内存的时间间隔。
     */
    @SuppressWarnings("EmptyMethod")
    public static void checkCheckMemoryInterval(long checkMemoryInterval) {
        // 允许为负数，表示不自动检查。
    }

    private TemporaryStorageConfigUtil() {
        throw new IllegalStateException("禁止实例化");
    }
}
