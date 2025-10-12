package com.dwarfeng.tmpstg.configuration;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandlerImpl;
import com.dwarfeng.tmpstg.struct.TemporaryStorageConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 单例模式配置。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
@Configuration
public class SingletonConfiguration {

    /**
     * SPEL: 临时存储的临时文件目录。
     *
     * @since 1.0.7
     */
    public static final String SPEL_TEMPORARY_FILE_DIRECTORY_PATH = "${tmpstg.temporary_file_directory_path:" +
            "#{T(com.dwarfeng.tmpstg.struct.TemporaryStorageConfig$Builder).DEFAULT_TEMPORARY_FILE_DIRECTORY_PATH}}";

    /**
     * SPEL: 临时存储临时文件的前缀。
     *
     * @since 1.0.7
     */
    public static final String SPEL_TEMPORARY_FILE_PREFIX = "${tmpstg.temporary_file_prefix:" +
            "#{T(com.dwarfeng.tmpstg.struct.TemporaryStorageConfig$Builder).DEFAULT_TEMPORARY_FILE_PREFIX}}";

    /**
     * SPEL: 临时存储临时文件的后缀。
     *
     * @since 1.0.7
     */
    public static final String SPEL_TEMPORARY_FILE_SUFFIX = "${tmpstg.temporary_file_suffix:" +
            "#{T(com.dwarfeng.tmpstg.struct.TemporaryStorageConfig$Builder).DEFAULT_TEMPORARY_FILE_SUFFIX}}";

    /**
     * SPEL: 临时存储每个存储的最大缓冲区大小。
     *
     * @since 1.0.7
     */
    public static final String SPEL_MAX_BUFFER_SIZE_PER_STORAGE = "${tmpstg.max_buffer_size_per_storage:" +
            "#{T(com.dwarfeng.tmpstg.struct.TemporaryStorageConfig$Builder).DEFAULT_MAX_BUFFER_SIZE_PER_STORAGE}}";

    /**
     * SPEL: 临时存储总的最大缓冲区大小。
     *
     * @since 1.0.7
     */
    public static final String SPEL_MAX_BUFFER_SIZE_TOTAL = "${tmpstg.max_buffer_size_total:" +
            "#{T(com.dwarfeng.tmpstg.struct.TemporaryStorageConfig$Builder).DEFAULT_MAX_BUFFER_SIZE_TOTAL}}";

    /**
     * SPEL: 临时存储清理已释放存储的间隔。
     *
     * @since 1.0.7
     */
    public static final String SPEL_CLEAR_DISPOSED_INTERVAL = "${tmpstg.clear_disposed_interval:" +
            "#{T(com.dwarfeng.tmpstg.struct.TemporaryStorageConfig$Builder).DEFAULT_CLEAR_DISPOSED_INTERVAL}}";

    /**
     * SPEL: 临时存储检查内存的间隔。
     *
     * @since 1.0.7
     */
    public static final String SPEL_CHECK_MEMORY_INTERVAL = "${tmpstg.check_memory_interval:" +
            "#{T(com.dwarfeng.tmpstg.struct.TemporaryStorageConfig$Builder).DEFAULT_CHECK_MEMORY_INTERVAL}}";

    /**
     * SPEL: 临时存储的临时文件目录。
     *
     * <p>
     * 该常量由于命名不规范，已被弃用。<br>
     * 请使用 {@link #SPEL_TEMPORARY_FILE_DIRECTORY_PATH} 替代。
     *
     * @see #SPEL_TEMPORARY_FILE_DIRECTORY_PATH
     * @deprecated 该常量由于命名不规范，已被弃用。
     */
    @Deprecated
    public static final String SPEL_DEFAULT_TEMPORARY_FILE_DIRECTORY_PATH = SPEL_TEMPORARY_FILE_DIRECTORY_PATH;

    /**
     * SPEL: 临时存储临时文件的前缀。
     *
     * <p>
     * 该常量由于命名不规范，已被弃用。<br>
     * 请使用 {@link #SPEL_TEMPORARY_FILE_PREFIX} 替代。
     *
     * @see #SPEL_TEMPORARY_FILE_PREFIX
     * @deprecated 该常量由于命名不规范，已被弃用。
     */
    @Deprecated
    public static final String SPEL_DEFAULT_TEMPORARY_FILE_PREFIX = SPEL_TEMPORARY_FILE_PREFIX;

    /**
     * SPEL: 临时存储临时文件的后缀。
     *
     * <p>
     * 该常量由于命名不规范，已被弃用。<br>
     * 请使用 {@link #SPEL_TEMPORARY_FILE_SUFFIX} 替代。
     *
     * @see #SPEL_TEMPORARY_FILE_SUFFIX
     * @deprecated 该常量由于命名不规范，已被弃用。
     */
    @Deprecated
    public static final String SPEL_DEFAULT_TEMPORARY_FILE_SUFFIX = SPEL_TEMPORARY_FILE_SUFFIX;

    /**
     * SPEL: 临时存储每个存储的最大缓冲区大小。
     *
     * <p>
     * 该常量由于命名不规范，已被弃用。<br>
     * 请使用 {@link #SPEL_MAX_BUFFER_SIZE_PER_STORAGE} 替代。
     *
     * @see #SPEL_MAX_BUFFER_SIZE_PER_STORAGE
     * @deprecated 该常量由于命名不规范，已被弃用。
     */
    @Deprecated
    public static final String SPEL_DEFAULT_MAX_BUFFER_SIZE_PER_STORAGE = SPEL_MAX_BUFFER_SIZE_PER_STORAGE;

    /**
     * SPEL: 临时存储总的最大缓冲区大小。
     *
     * <p>
     * 该常量由于命名不规范，已被弃用。<br>
     * 请使用 {@link #SPEL_MAX_BUFFER_SIZE_TOTAL} 替代。
     *
     * @see #SPEL_MAX_BUFFER_SIZE_TOTAL
     * @deprecated 该常量由于命名不规范，已被弃用。
     */
    @Deprecated
    public static final String SPEL_DEFAULT_MAX_BUFFER_SIZE_TOTAL = SPEL_MAX_BUFFER_SIZE_TOTAL;

    /**
     * SPEL: 临时存储清理已释放存储的间隔。
     *
     * <p>
     * 该常量由于命名不规范，已被弃用。<br>
     * 请使用 {@link #SPEL_CLEAR_DISPOSED_INTERVAL} 替代。
     *
     * @see #SPEL_CLEAR_DISPOSED_INTERVAL
     * @deprecated 该常量由于命名不规范，已被弃用。
     */
    @Deprecated
    public static final String SPEL_DEFAULT_CLEAR_DISPOSED_INTERVAL = SPEL_CLEAR_DISPOSED_INTERVAL;

    /**
     * SPEL: 临时存储检查内存的间隔。
     *
     * <p>
     * 该常量由于命名不规范，已被弃用。<br>
     * 请使用 {@link #SPEL_CHECK_MEMORY_INTERVAL} 替代。
     *
     * @see #SPEL_CHECK_MEMORY_INTERVAL
     * @deprecated 该常量由于命名不规范，已被弃用。
     */
    @Deprecated
    public static final String SPEL_DEFAULT_CHECK_MEMORY_INTERVAL = SPEL_CHECK_MEMORY_INTERVAL;

    private final ThreadPoolTaskScheduler scheduler;

    @Value(SPEL_TEMPORARY_FILE_DIRECTORY_PATH)
    private String temporaryFileDirectoryPath;

    // SPEL 太长，故使用常量缩短长度。
    @Value(SPEL_TEMPORARY_FILE_PREFIX)
    private String temporaryFilePrefix;
    // SPEL 太长，故使用常量缩短长度。
    @Value(SPEL_TEMPORARY_FILE_SUFFIX)
    private String temporaryFileSuffix;

    // SPEL 太长，故使用常量缩短长度。
    @Value(SPEL_MAX_BUFFER_SIZE_PER_STORAGE)
    private int maxBufferSizePerStorage;
    // SPEL 太长，故使用常量缩短长度。
    @Value(SPEL_MAX_BUFFER_SIZE_TOTAL)
    private long maxBufferSizeTotal;

    @Value(SPEL_CLEAR_DISPOSED_INTERVAL)
    private long clearDisposedInterval;
    @Value(SPEL_CHECK_MEMORY_INTERVAL)
    private long checkMemoryInterval;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public SingletonConfiguration(ThreadPoolTaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public TemporaryStorageHandler temporaryStorageHandler() {
        TemporaryStorageConfig temporaryStorageConfig = new TemporaryStorageConfig(
                temporaryFileDirectoryPath, temporaryFilePrefix, temporaryFileSuffix, maxBufferSizePerStorage,
                maxBufferSizeTotal, clearDisposedInterval, checkMemoryInterval
        );

        return new TemporaryStorageHandlerImpl(scheduler, temporaryStorageConfig);
    }
}
