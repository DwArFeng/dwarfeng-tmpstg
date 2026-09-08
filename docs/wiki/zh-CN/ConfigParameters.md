# Config Parameters - 配置参数详解

本文档详细说明了 dwarfeng-tmpstg 的配置参数，包括各配置项的含义、默认值、校验规则以及配置示例。
配置参数通过 properties 文件或 Spring 占位符注入，最终构建为 `TemporaryStorageConfig` 对象供 `TemporaryStorageHandlerImpl` 使用。

## 配置加载方式

### 单例模式

使用 `SingletonConfiguration` 时，通过 Spring 的 `@Value` 注解从 properties 中读取配置。
需要确保 Spring 的 `property-placeholder` 已加载包含 `tmpstg.*` 前缀的配置文件。

配置文件通常位于 `classpath:tmpstg/*.properties` 或通过 `file:conf/test.tmpstg/*.properties` 指定。
本地文件可覆盖 classpath 中的配置。

### 多实例模式

使用 XML 或配置类手动创建多个 `TemporaryStorageHandlerImpl` 实例时，通过占位符区分不同实例的配置。
例如：`${tmpstg.temporary_file_directory_path.1}`、`${tmpstg.temporary_file_prefix.1}` 对应第一个实例，
`${tmpstg.temporary_file_directory_path.2}`、`${tmpstg.temporary_file_prefix.2}` 对应第二个实例。

## 存储文件参数

### tmpstg.temporary_file_directory_path

临时文件目录路径。用于承接内存缓冲区写满或受配置上限限制后的数据落盘。目录需存在或可创建，且具备读写权限。类型：String，默认值：System.getProperty
("java.io.tmpdir")。

### tmpstg.temporary_file_prefix

临时文件的前缀，用于区分本项目产生的临时文件。类型：String，默认值：tmpstg-。

### tmpstg.temporary_file_suffix

临时文件的后缀，用于规范文件的扩展名。类型：String，默认值：.tmp。

## 内存缓冲区参数

### tmpstg.max_buffer_size_per_storage

单个存储的最大缓冲区大小，单位字节。内存缓冲区写满后，剩余部分写入临时文件；全局缓冲区上限也可能使实际分配大小更小。必须大于
0，且不能大于 tmpstg.max_buffer_size_total。类型：int，默认值：2048。

### tmpstg.max_buffer_size_total

总的最大缓冲区大小，单位字节。用于限制所有临时存储在内存中的累计占用。必须大于 0。类型：long，默认值：1048576。

## 后台任务参数

### tmpstg.clear_disposed_interval

清理已释放的临时存储的间隔，单位毫秒。小于等于 0 表示不自动清理。类型：long，默认值：300000。

### tmpstg.check_memory_interval

检查内存的间隔，单位毫秒。小于等于 0 表示不自动检查。类型：long，默认值：60000。

## 配置示例

### 单例模式

在 `tmpstg-settings.properties` 中配置：

```properties
# 临时文件目录路径。
tmpstg.temporary_file_directory_path=temporary-file
# 临时文件前缀。
tmpstg.temporary_file_prefix=tmpstg-
# 临时文件后缀。
tmpstg.temporary_file_suffix=.tmp
# 单个存储的最大缓冲区大小。
tmpstg.max_buffer_size_per_storage=2048
# 总的最大缓冲区大小。
tmpstg.max_buffer_size_total=1048576
# 清理已经释放的缓冲区的间隔。
tmpstg.clear_disposed_interval=300000
# 检查内存的间隔。
tmpstg.check_memory_interval=60000
```

### 多实例模式

在 Spring XML 中为多个实例配置不同的占位符：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- 以下注释用于抑制 idea 中 .md 的警告，实际并无错误，在使用时可以连同本注释一起删除。 -->
<!--suppress SpringBeanConstructorArgInspection, SpringXmlModelInspection, SpringPlaceholdersInspection -->
<beans
        xmlns:tmpstg="http://dwarfeng.com/schema/dwarfeng-tmpstg"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.springframework.org/schema/beans"
        xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://dwarfeng.com/schema/dwarfeng-tmpstg
        http://dwarfeng.com/schema/dwarfeng-tmpstg/dwarfeng-tmpstg.xsd"
>
    <!-- 第 1 个实例。 -->
    <tmpstg:config
            config-name="temporaryStorageConfig1"
            temporary-file-directory-path="${tmpstg.temporary_file_directory_path.1}"
            temporary-file-prefix="${tmpstg.temporary_file_prefix.1}"
            temporary-file-suffix="${tmpstg.temporary_file_suffix.1}"
            max-buffer-size-per-storage="${tmpstg.max_buffer_size_per_storage.1}"
            max-buffer-size-total="${tmpstg.max_buffer_size_total.1}"
            clear-disposed-interval="${tmpstg.clear_disposed_interval.1}"
            check-memory-interval="${tmpstg.check_memory_interval.1}"
    />
    <tmpstg:handler
            handler-name="temporaryStorageHandler1"
            config-ref="temporaryStorageConfig1"
    />

    <!-- 第 2 个实例。 -->
    <tmpstg:config
            config-name="temporaryStorageConfig2"
            temporary-file-directory-path="${tmpstg.temporary_file_directory_path.2}"
            temporary-file-prefix="${tmpstg.temporary_file_prefix.2}"
            temporary-file-suffix="${tmpstg.temporary_file_suffix.2}"
            max-buffer-size-per-storage="${tmpstg.max_buffer_size_per_storage.2}"
            max-buffer-size-total="${tmpstg.max_buffer_size_total.2}"
            clear-disposed-interval="${tmpstg.clear_disposed_interval.2}"
            check-memory-interval="${tmpstg.check_memory_interval.2}"
    />
    <tmpstg:handler
            handler-name="temporaryStorageHandler2"
            config-ref="temporaryStorageConfig2"
    />
</beans>
```

在 properties 中对应配置 `tmpstg.temporary_file_directory_path.1`、`tmpstg.temporary_file_directory_path.2` 等。

## 参数校验规则

配置在构建 `TemporaryStorageConfig` 时会进行校验，校验逻辑由 `TemporaryStorageConfigUtil` 实现。常见约束如下：

- 临时文件目录路径、临时文件前缀、临时文件后缀不能为 null。
- 临时文件目录必须存在或可创建，且为目录且有读写权限。
- 单个存储的最大缓冲区大小必须大于 0。
- 总的最大缓冲区大小必须大于 0。
- 单个存储的最大缓冲区大小不能大于总的最大缓冲区大小。
- 清理已释放的临时存储的间隔允许为负数，表示不自动清理。
- 检查内存的间隔允许为负数，表示不自动检查。

违反上述规则时，将抛出 `NullPointerException` 或 `IllegalArgumentException`。

## 参阅

- [Usage Guide](./UsageGuide.md) - 使用指南，系统说明本项目的接入方式、配置方式与 API 用法。
- [Quick Start](./QuickStart.md) - 快速开始，用最快的方式体验本项目。
