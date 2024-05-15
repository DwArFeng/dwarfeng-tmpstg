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

    private static final String SPEL_DEFAULT_TEMPORARY_FILE_DIRECTORY_PATH = "${tmpstg.temporary_file_directory_path:" +
            "#{T(com.dwarfeng.tmpstg.struct.TemporaryStorageConfig$Builder).DEFAULT_TEMPORARY_FILE_DIRECTORY_PATH}}";
    private static final String SPEL_DEFAULT_TEMPORARY_FILE_PREFIX = "${tmpstg.temporary_file_prefix:" +
            "#{T(com.dwarfeng.tmpstg.struct.TemporaryStorageConfig$Builder).DEFAULT_TEMPORARY_FILE_PREFIX}}";
    private static final String SPEL_DEFAULT_TEMPORARY_FILE_SUFFIX = "${tmpstg.temporary_file_suffix:" +
            "#{T(com.dwarfeng.tmpstg.struct.TemporaryStorageConfig$Builder).DEFAULT_TEMPORARY_FILE_SUFFIX}}";
    private static final String SPEL_DEFAULT_MAX_BUFFER_SIZE_PER_STORAGE = "${tmpstg.max_buffer_size_per_storage:" +
            "#{T(com.dwarfeng.tmpstg.struct.TemporaryStorageConfig$Builder).DEFAULT_MAX_BUFFER_SIZE_PER_STORAGE}}";
    private static final String SPEL_DEFAULT_MAX_BUFFER_SIZE_TOTAL = "${tmpstg.max_buffer_size_total:" +
            "#{T(com.dwarfeng.tmpstg.struct.TemporaryStorageConfig$Builder).DEFAULT_MAX_BUFFER_SIZE_TOTAL}}";
    private static final String SPEL_DEFAULT_CLEAR_DISPOSED_INTERVAL = "${tmpstg.clear_disposed_interval:" +
            "#{T(com.dwarfeng.tmpstg.struct.TemporaryStorageConfig$Builder).DEFAULT_CLEAR_DISPOSED_INTERVAL}}";
    private static final String SPEL_DEFAULT_CHECK_MEMORY_INTERVAL = "${tmpstg.check_memory_interval:" +
            "#{T(com.dwarfeng.tmpstg.struct.TemporaryStorageConfig$Builder).DEFAULT_CHECK_MEMORY_INTERVAL}}";

    private final ThreadPoolTaskScheduler scheduler;

    @Value(SPEL_DEFAULT_TEMPORARY_FILE_DIRECTORY_PATH)
    private String temporaryFileDirectoryPath;

    // SPEL 太长，故使用常量缩短长度。
    @Value(SPEL_DEFAULT_TEMPORARY_FILE_PREFIX)
    private String temporaryFilePrefix;
    // SPEL 太长，故使用常量缩短长度。
    @Value(SPEL_DEFAULT_TEMPORARY_FILE_SUFFIX)
    private String temporaryFileSuffix;

    // SPEL 太长，故使用常量缩短长度。
    @Value(SPEL_DEFAULT_MAX_BUFFER_SIZE_PER_STORAGE)
    private int maxBufferSizePerStorage;
    // SPEL 太长，故使用常量缩短长度。
    @Value(SPEL_DEFAULT_MAX_BUFFER_SIZE_TOTAL)
    private long maxBufferSizeTotal;

    @Value(SPEL_DEFAULT_CLEAR_DISPOSED_INTERVAL)
    private long clearDisposedInterval;
    @Value(SPEL_DEFAULT_CHECK_MEMORY_INTERVAL)
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
