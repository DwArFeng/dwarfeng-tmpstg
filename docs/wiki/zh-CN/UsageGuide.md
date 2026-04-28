# Usage Guide - 使用指南

## 综述

本文档用于系统说明 `dwarfeng-tmpstg` 的落地使用方式。

`dwarfeng-tmpstg` 是一个基于 `subgrade` 架构的临时存储组件，
核心目标是让业务侧以统一 API 完成临时内容的创建、读写、检查、释放与回收。

相比直接在业务代码中管理内存缓冲与临时文件，组件额外提供了以下能力：

- 统一的 `TemporaryStorageHandler` 抽象，屏蔽内存/文件混合存储细节。
- 输出流写入时自动在内存缓冲与文件缓冲之间切换。
- 全局内存上限约束，避免多存储并发写入时内存失控。
- 存储级锁与处理器级锁组合的线程安全模型。
- 已释放存储的手动清理与定时清理。
- 统一异常语义（处理器未启动、存储不存在、状态不合法、流打开失败等）。

本文将按“快速接入 -> 参数配置 -> API 说明 -> 运行约束 -> 最佳实践”的顺序展开，目标是让您可以直接复制配置并落地到自己的工程中。

## 快速开始

### 添加依赖

在工程 `pom.xml` 中添加依赖：

```xml
<?xml version="1.0" encoding="UTF-8"?>

<!--suppress MavenModelInspection, MavenModelVersionMissed -->
<project
        xmlns="http://maven.apache.org/POM/4.0.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
        http://maven.apache.org/xsd/maven-4.0.0.xsd"
>

    <!-- 省略其他配置 -->
    <dependencies>
        <!-- 省略其他配置 -->
        <dependency>
            <groupId>com.dwarfeng</groupId>
            <artifactId>dwarfeng-tmpstg</artifactId>
            <version>${dwarfeng-tmpstg.version}</version>
        </dependency>
        <!-- 省略其他配置 -->
    </dependencies>
    <!-- 省略其他配置 -->
</project>
```

### 基础 Spring 配置

`dwarfeng-tmpstg` 的单例接入通常需要三部分配置：

1. 扫描 `SingletonConfiguration`。
2. 提供 `ThreadPoolTaskScheduler` 类型的 `scheduler` Bean（用于后台清理与巡检任务）。
3. 加载 `tmpstg.*` 配置参数。

`application-context-scan.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans
        xmlns:context="http://www.springframework.org/schema/context"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.springframework.org/schema/beans"
        xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd"
>

    <!-- 扫描 dwarfeng-tmpstg 的配置类。 -->
    <context:component-scan base-package="com.dwarfeng.tmpstg.configuration" use-default-filters="false">
        <context:include-filter
                type="assignable" expression="com.dwarfeng.tmpstg.configuration.SingletonConfiguration"
        />
    </context:component-scan>
</beans>
```

`application-context-task.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans
        xmlns:task="http://www.springframework.org/schema/task"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.springframework.org/schema/beans"
        xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/task
        http://www.springframework.org/schema/task/spring-task.xsd"
>

    <!-- 装配 scheduler。 -->
    <task:scheduler id="scheduler" pool-size="1"/>
</beans>
```

`application-context-placeholder.xml`（类路径默认值 + 本地覆盖）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- 以下注释用于抑制 idea 中 .md 的警告，实际并无错误，在使用时可以连同本注释一起删除。 -->
<!--suppress SpringXmlModelInspection -->
<beans
        xmlns:context="http://www.springframework.org/schema/context"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.springframework.org/schema/beans"
        xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd"
>

    <!-- 先加载 classpath 默认配置。 -->
    <context:property-placeholder
            location="classpath:tmpstg/*.properties"
            local-override="true"
            properties-ref="refProperties"
    />

    <!-- 再加载本地文件覆盖配置。 -->
    <bean id="refProperties" class="org.springframework.beans.factory.config.PropertiesFactoryBean">
        <property name="locations">
            <list>
                <!--suppress SpringXmlModelInspection -->
                <value>file:conf/test.tmpstg/*.properties</value>
            </list>
        </property>
    </bean>
</beans>
```

### 准备最小化参数

`src/main/resources/tmpstg/tmpstg-settings.properties`：

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
# 清理已经释放的临时存储的间隔。
tmpstg.clear_disposed_interval=300000
# 检查内存的间隔。
tmpstg.check_memory_interval=60000
```

### 创建第一个使用类

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.bean.dto.TemporaryStorageInfo;
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@SuppressWarnings("UnnecessaryModifier")
public class FoobarQuickStart {

    public static void main(String[] args) throws Exception {
        try (ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "classpath:spring/application-context*.xml"
        )) {
            ctx.registerShutdownHook();
            ctx.start();

            TemporaryStorageHandler handler = ctx.getBean(TemporaryStorageHandler.class);

            String key = handler.create();
            byte[] source = "hello dwarfeng-tmpstg".getBytes(StandardCharsets.UTF_8);

            try (OutputStream out = handler.openOutputStream(key, source.length)) {
                out.write(source);
            }

            byte[] readBack;
            try (
                    InputStream in = handler.openInputStream(key);
                    ByteArrayOutputStream out = new ByteArrayOutputStream()
            ) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, length);
                }
                readBack = out.toByteArray();
            }

            TemporaryStorageInfo info = handler.inspect(key);
            System.out.println("内容一致: " + Arrays.equals(source, readBack));
            System.out.println("存储信息: " + info);

            handler.disposeAndRemove(key);
        }
    }
}
```

### 启动与验证

可以通过以下方式验证接入结果：

1. 启动应用后无初始化异常。
2. `create` 后 `exists(key)` 返回 `true`。
3. `openOutputStream` 写入后，`inspect(key).getContentLength()` 与写入长度一致。
4. `openInputStream` 读取内容与原始字节数组一致。
5. `disposeAndRemove` 后，`exists(key)` 返回 `false`。

如果第 2-5 步出现异常，可优先检查配置项是否加载成功，
并确认 Spring 中确实存在 `ThreadPoolTaskScheduler` 类型的 `scheduler` Bean。

## 配置详解

### 配置加载方式

#### 单例模式配置加载

使用 `SingletonConfiguration` 时，`tmpstg.*` 参数通过 `@Value` 注入。典型加载路径是：

- `classpath:tmpstg/*.properties`。
- 可选本地覆盖路径（例如 `file:conf/tmpstg/*.properties`）。

#### 多实例模式配置加载

使用 XML 或配置类手动创建多个处理器实例时，通常通过后缀区分参数，例如：

- `tmpstg.temporary_file_directory_path.1`、`tmpstg.max_buffer_size_total.1`。
- `tmpstg.temporary_file_directory_path.2`、`tmpstg.max_buffer_size_total.2`。

### 临时文件参数

#### `tmpstg.temporary_file_directory_path`

- 类型：`String`。
- 默认值：`System.getProperty("java.io.tmpdir")`。
- 说明：临时文件目录路径，内存不足时超出部分数据会落到该目录下的文件缓冲。
- 约束：
  不能为空。
  路径不存在时会尝试创建。
  不能是文件，必须是目录。
  目录必须具备读写权限。

#### `tmpstg.temporary_file_prefix`

- 类型：`String`。
- 默认值：`tmpstg-`。
- 说明：临时文件名前缀。
- 约束：不能为空。

#### `tmpstg.temporary_file_suffix`

- 类型：`String`。
- 默认值：`.tmp`。
- 说明：临时文件名后缀。
- 约束：不能为空。

### 缓冲区参数

#### `tmpstg.max_buffer_size_per_storage`

- 类型：`int`。
- 默认值：`2048`（2 KiB）。
- 说明：单个临时存储允许占用的最大内存缓冲区大小。
- 约束：必须大于 `0`，且不能大于 `tmpstg.max_buffer_size_total`。

#### `tmpstg.max_buffer_size_total`

- 类型：`long`。
- 默认值：`1048576`（1 MiB）。
- 说明：处理器内全部临时存储可占用的内存缓冲区总上限。
- 约束：必须大于 `0`，且不能小于 `tmpstg.max_buffer_size_per_storage`。

### 后台任务参数

#### `tmpstg.clear_disposed_interval`

- 类型：`long`。
- 默认值：`300000`（毫秒）。
- 说明：清理已释放存储的定时任务间隔。
- 约束：允许任意值；当该值 `<= 0` 时，不启动自动清理任务。

#### `tmpstg.check_memory_interval`

- 类型：`long`。
- 默认值：`60000`（毫秒）。
- 说明：内存缓冲区一致性检查任务间隔。
- 约束：允许任意值；当该值 `<= 0` 时，不启动内存巡检任务。

### 完整参数模板

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
# 清理已经释放的临时存储的间隔（毫秒）。
tmpstg.clear_disposed_interval=300000
# 检查内存的间隔（毫秒）。
tmpstg.check_memory_interval=60000
```

### 参数校验规则总结

配置构建为 `TemporaryStorageConfig` 时会触发校验，常见约束如下：

- 临时目录路径、前缀、后缀不能为 `null`。
- 临时目录必须是可读写目录，不存在时尝试创建。
- `max_buffer_size_per_storage` 必须大于 `0`。
- `max_buffer_size_total` 必须大于 `0`。
- `max_buffer_size_per_storage` 不能大于 `max_buffer_size_total`。
- `clear_disposed_interval` 与 `check_memory_interval` 可为负值，用于关闭自动任务。

违反约束时通常会抛出 `NullPointerException` 或 `IllegalArgumentException`。

### 场景化配置模板

以下模板用于快速落地不同运行侧重点，您可以在此基础上微调。

#### 模板 A：开发环境默认模板

```properties
tmpstg.temporary_file_directory_path=temporary-file
tmpstg.temporary_file_prefix=tmpstg-
tmpstg.temporary_file_suffix=.tmp
tmpstg.max_buffer_size_per_storage=2048
tmpstg.max_buffer_size_total=1048576
tmpstg.clear_disposed_interval=300000
tmpstg.check_memory_interval=60000
```

#### 模板 B：高吞吐读写优先（内存更大）

```properties
tmpstg.temporary_file_directory_path=D:/tmpstg-buf
tmpstg.temporary_file_prefix=tmpstg-fast-
tmpstg.temporary_file_suffix=.tmp
tmpstg.max_buffer_size_per_storage=65536
tmpstg.max_buffer_size_total=16777216
tmpstg.clear_disposed_interval=120000
tmpstg.check_memory_interval=30000
```

#### 模板 C：磁盘优先（控制内存占用）

```properties
tmpstg.temporary_file_directory_path=D:/tmpstg-disk
tmpstg.temporary_file_prefix=tmpstg-disk-
tmpstg.temporary_file_suffix=.tmp
tmpstg.max_buffer_size_per_storage=1024
tmpstg.max_buffer_size_total=65536
tmpstg.clear_disposed_interval=60000
tmpstg.check_memory_interval=60000
```

#### 模板 D：关闭自动任务（由业务方统一调度）

```properties
tmpstg.temporary_file_directory_path=temporary-file
tmpstg.temporary_file_prefix=tmpstg-
tmpstg.temporary_file_suffix=.tmp
tmpstg.max_buffer_size_per_storage=4096
tmpstg.max_buffer_size_total=2097152
tmpstg.clear_disposed_interval=-1
tmpstg.check_memory_interval=-1
```

#### 模板选型建议

1. 需要开箱即用、调试方便，优先模板 A。
2. 临时内容中小文件居多、吞吐优先，优先模板 B。
3. 机器内存受限、允许更多磁盘 I/O，优先模板 C。
4. 统一调度中心负责清理和巡检时，优先模板 D。

## 接入模式

### 单例模式（推荐）

单例模式由 `SingletonConfiguration` 提供，适用于绝大多数业务工程。

`application-context-scan.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans
        xmlns:context="http://www.springframework.org/schema/context"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.springframework.org/schema/beans"
        xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd"
>

    <context:component-scan base-package="com.dwarfeng.tmpstg.configuration" use-default-filters="false">
        <context:include-filter
                type="assignable" expression="com.dwarfeng.tmpstg.configuration.SingletonConfiguration"
        />
    </context:component-scan>
</beans>
```

该模式下，`TemporaryStorageHandler` 会按 Spring Bean 生命周期自动 `start/stop`。

### 多实例模式

多实例模式不依赖扫描配置类，直接通过 XML 手动创建多个 `TemporaryStorageHandlerImpl` 实例。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- 以下注释用于抑制 idea 中 .md 的警告，实际并无错误，在使用时可以连同本注释一起删除。 -->
<!--suppress SpringBeanConstructorArgInspection, SpringXmlModelInspection, SpringPlaceholdersInspection -->
<beans
        xmlns:task="http://www.springframework.org/schema/task"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.springframework.org/schema/beans"
        xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/task
        http://www.springframework.org/schema/task/spring-task.xsd"
>
    <task:scheduler id="scheduler" pool-size="2"/>

    <!-- 第 1 个实例。 -->
    <bean name="fooConfigBuilder" class="com.dwarfeng.tmpstg.struct.TemporaryStorageConfig.Builder">
        <property name="temporaryFileDirectoryPath" value="${tmpstg.temporary_file_directory_path.1}"/>
        <property name="temporaryFilePrefix" value="${tmpstg.temporary_file_prefix.1}"/>
        <property name="temporaryFileSuffix" value="${tmpstg.temporary_file_suffix.1}"/>
        <property name="maxBufferSizePerStorage" value="${tmpstg.max_buffer_size_per_storage.1}"/>
        <property name="maxBufferSizeTotal" value="${tmpstg.max_buffer_size_total.1}"/>
        <property name="clearDisposedInterval" value="${tmpstg.clear_disposed_interval.1}"/>
        <property name="checkMemoryInterval" value="${tmpstg.check_memory_interval.1}"/>
    </bean>
    <bean name="fooConfig" factory-bean="fooConfigBuilder" factory-method="build"/>
    <bean
            name="fooTemporaryStorageHandler"
            class="com.dwarfeng.tmpstg.handler.TemporaryStorageHandlerImpl"
            init-method="start"
            destroy-method="stop"
    >
        <constructor-arg name="scheduler" ref="scheduler"/>
        <constructor-arg name="config" ref="fooConfig"/>
    </bean>

    <!-- 第 2 个实例。 -->
    <bean name="barConfigBuilder" class="com.dwarfeng.tmpstg.struct.TemporaryStorageConfig.Builder">
        <property name="temporaryFileDirectoryPath" value="${tmpstg.temporary_file_directory_path.2}"/>
        <property name="temporaryFilePrefix" value="${tmpstg.temporary_file_prefix.2}"/>
        <property name="temporaryFileSuffix" value="${tmpstg.temporary_file_suffix.2}"/>
        <property name="maxBufferSizePerStorage" value="${tmpstg.max_buffer_size_per_storage.2}"/>
        <property name="maxBufferSizeTotal" value="${tmpstg.max_buffer_size_total.2}"/>
        <property name="clearDisposedInterval" value="${tmpstg.clear_disposed_interval.2}"/>
        <property name="checkMemoryInterval" value="${tmpstg.check_memory_interval.2}"/>
    </bean>
    <bean name="barConfig" factory-bean="barConfigBuilder" factory-method="build"/>
    <bean
            name="barTemporaryStorageHandler"
            class="com.dwarfeng.tmpstg.handler.TemporaryStorageHandlerImpl"
            init-method="start"
            destroy-method="stop"
    >
        <constructor-arg name="scheduler" ref="scheduler"/>
        <constructor-arg name="config" ref="barConfig"/>
    </bean>
</beans>
```

对应的参数文件示例：

```properties
# 第 1 个实例。
tmpstg.temporary_file_directory_path.1=D:/tmpstg/foo
tmpstg.temporary_file_prefix.1=foo-
tmpstg.temporary_file_suffix.1=.tmp
tmpstg.max_buffer_size_per_storage.1=4096
tmpstg.max_buffer_size_total.1=2097152
tmpstg.clear_disposed_interval.1=300000
tmpstg.check_memory_interval.1=60000
# 第 2 个实例。
tmpstg.temporary_file_directory_path.2=D:/tmpstg/bar
tmpstg.temporary_file_prefix.2=bar-
tmpstg.temporary_file_suffix.2=.tmp
tmpstg.max_buffer_size_per_storage.2=8192
tmpstg.max_buffer_size_total.2=4194304
tmpstg.clear_disposed_interval.2=120000
tmpstg.check_memory_interval.2=30000
```

### 任意数量实例模式

任意数量实例模式通常通过工厂类动态创建 `TemporaryStorageHandlerImpl` 实例。

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandlerImpl;
import com.dwarfeng.tmpstg.struct.TemporaryStorageConfig;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class FoobarTemporaryStorageFactory {

    private final ThreadPoolTaskScheduler scheduler;

    public FoobarTemporaryStorageFactory(ThreadPoolTaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public TemporaryStorageHandler newHandler(String tempDir, String prefix) throws Exception {
        TemporaryStorageConfig config = new TemporaryStorageConfig.Builder()
                .setTemporaryFileDirectoryPath(tempDir)
                .setTemporaryFilePrefix(prefix)
                .setTemporaryFileSuffix(".tmp")
                .setMaxBufferSizePerStorage(4096)
                .setMaxBufferSizeTotal(2097152)
                .setClearDisposedInterval(-1)
                .setCheckMemoryInterval(-1)
                .build();

        TemporaryStorageHandlerImpl handler = new TemporaryStorageHandlerImpl(scheduler, config);
        handler.start();
        return handler;
    }

    public void closeHandler(TemporaryStorageHandler handler) throws Exception {
        handler.stop();
    }
}
```

需要注意：工厂创建的实例应由业务方明确调用 `start()` 与 `stop()`，否则会出现“未启动处理器调用 API”或“资源未关闭”问题。

### 接入模式选择建议

1. 单系统单配置场景优先单例模式。
2. 多租户或多资源池隔离场景优先多实例模式。
3. 动态按任务创建/回收处理器实例时使用任意数量实例模式。
4. 若团队运维成熟度一般，建议先从单例模式开始。

## API 使用详解

### 接口总览

`TemporaryStorageHandler` 的核心能力可分为四类：

- 元数据查询：`keys`、`exists`、`inspect`。
- 存储创建：`create`。
- 数据读写：`openInputStream`、`openOutputStream`。
- 生命周期清理：`dispose`、`remove`、`removeIfDisposed`、`disposeAndRemove`、`clearDisposed`。

### 方法签名参考

```java
package com.example.foobar;

import com.dwarfeng.subgrade.stack.exception.HandlerException;
import com.dwarfeng.tmpstg.bean.dto.TemporaryStorageInfo;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

@SuppressWarnings("unused")
public interface TemporaryStorageHandlerSignatureReference {

    Collection<String> keys() throws HandlerException;

    boolean exists(String key) throws HandlerException;

    TemporaryStorageInfo inspect(String key) throws HandlerException;

    String create() throws HandlerException;

    InputStream openInputStream(String key) throws HandlerException;

    OutputStream openOutputStream(String key) throws HandlerException;

    OutputStream openOutputStream(String key, long expectedLength) throws HandlerException;

    void dispose(String key) throws HandlerException;

    void remove(String key) throws HandlerException;

    boolean removeIfDisposed(String key) throws HandlerException;

    void disposeAndRemove(String key) throws HandlerException;

    void clearDisposed() throws HandlerException;
}
```

### 常见方法组合

最常见的调用组合如下：

1. `create -> openOutputStream -> openInputStream -> disposeAndRemove`。
2. `create -> openOutputStream -> inspect -> dispose -> remove`。
3. `keys -> inspect -> removeIfDisposed`（管理端批量清理）。
4. `create -> openOutputStream(key, expectedLength)`（已知长度写入优化）。

### 基础存储操作

#### 判断键是否存在

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

public class ExistsSnippet {

    public boolean exists(TemporaryStorageHandler handler, String key) throws Exception {
        return handler.exists(key);
    }
}
```

#### 创建存储并列举键

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

import java.util.Collection;

public class CreateAndKeysSnippet {

    public String createAndPrint(TemporaryStorageHandler handler) throws Exception {
        String key = handler.create();
        Collection<String> keys = handler.keys();
        for (String each : keys) {
            System.out.println(each);
        }
        return key;
    }
}
```

#### 查看存储信息

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.bean.dto.TemporaryStorageInfo;
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

public class InspectSnippet {

    public TemporaryStorageInfo inspect(TemporaryStorageHandler handler, String key) throws Exception {
        return handler.inspect(key);
    }
}
```

#### 示例：`FoobarStorageLifecycleService`

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.bean.dto.TemporaryStorageInfo;
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class FoobarStorageLifecycleService {

    private final TemporaryStorageHandler temporaryStorageHandler;

    public FoobarStorageLifecycleService(TemporaryStorageHandler temporaryStorageHandler) {
        this.temporaryStorageHandler = temporaryStorageHandler;
    }

    public TemporaryStorageInfo writeAndInspect(String text) throws Exception {
        String key = temporaryStorageHandler.create();
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        try (OutputStream out = temporaryStorageHandler.openOutputStream(key, data.length)) {
            out.write(data);
        }
        return temporaryStorageHandler.inspect(key);
    }
}
```

### 流式读写操作

#### 打开输出流（长度未知）

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

import java.io.OutputStream;

public class OpenOutputStreamUnknownLengthSnippet {

    public void write(TemporaryStorageHandler handler, String key, byte[] data) throws Exception {
        try (OutputStream out = handler.openOutputStream(key)) {
            out.write(data);
            out.flush();
        }
    }
}
```

#### 打开输出流（长度已知）

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

import java.io.OutputStream;

public class OpenOutputStreamKnownLengthSnippet {

    public void write(TemporaryStorageHandler handler, String key, byte[] data) throws Exception {
        try (OutputStream out = handler.openOutputStream(key, data.length)) {
            out.write(data);
            out.flush();
        }
    }
}
```

#### 打开输入流

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

import java.io.InputStream;

public class OpenInputStreamSnippet {

    public int readFirstByte(TemporaryStorageHandler handler, String key) throws Exception {
        try (InputStream in = handler.openInputStream(key)) {
            return in.read();
        }
    }
}
```

#### 多次读取同一存储

```java
package com.example.foobar;

import com.dwarfeng.dutil.basic.io.IOUtil;
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MultipleReadSnippet {

    public byte[] read(TemporaryStorageHandler handler, String key) throws Exception {
        try (InputStream in = handler.openInputStream(key); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            IOUtil.trans(in, out, 4096);
            return out.toByteArray();
        }
    }

    public void readThreeTimes(TemporaryStorageHandler handler, String key) throws Exception {
        read(handler, key);
        read(handler, key);
        read(handler, key);
    }
}
```

#### 多次写入同一存储

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

import java.io.OutputStream;

public class MultipleWriteSnippet {

    public void rewrite(TemporaryStorageHandler handler, String key, byte[] data) throws Exception {
        try (OutputStream out = handler.openOutputStream(key, data.length)) {
            out.write(data);
        }
    }

    public void rewriteThreeTimes(TemporaryStorageHandler handler, String key) throws Exception {
        rewrite(handler, key, "v1".getBytes());
        rewrite(handler, key, "v2".getBytes());
        rewrite(handler, key, "v3".getBytes());
    }
}
```

`openOutputStream` 会重置当前存储内容，新写入内容会覆盖旧内容。
如果需要保留历史版本，请由业务层生成新 key。

#### 示例：`FoobarBinaryStorageService`

```java
package com.example.foobar;

import com.dwarfeng.dutil.basic.io.IOUtil;
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

import java.io.*;

public class FoobarBinaryStorageService {

    private final TemporaryStorageHandler temporaryStorageHandler;

    public FoobarBinaryStorageService(TemporaryStorageHandler temporaryStorageHandler) {
        this.temporaryStorageHandler = temporaryStorageHandler;
    }

    public String upload(File source) throws Exception {
        String key = temporaryStorageHandler.create();
        try (
                InputStream in = new FileInputStream(source);
                OutputStream out = temporaryStorageHandler.openOutputStream(key, source.length())
        ) {
            IOUtil.trans(in, out, 4096);
        }
        return key;
    }

    public void download(String key, File target) throws Exception {
        try (
                InputStream in = temporaryStorageHandler.openInputStream(key);
                OutputStream out = new FileOutputStream(target)
        ) {
            IOUtil.trans(in, out, 4096);
        }
    }
}
```

### 清理与回收操作

#### 释放存储

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

public class DisposeSnippet {

    public void dispose(TemporaryStorageHandler handler, String key) throws Exception {
        handler.dispose(key);
    }
}
```

#### 移除已释放存储

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

public class RemoveSnippet {

    public void remove(TemporaryStorageHandler handler, String key) throws Exception {
        handler.remove(key);
    }
}
```

#### 若已释放则移除

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

public class RemoveIfDisposedSnippet {

    public boolean removeIfDisposed(TemporaryStorageHandler handler, String key) throws Exception {
        return handler.removeIfDisposed(key);
    }
}
```

#### 释放并移除

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

public class DisposeAndRemoveSnippet {

    public void disposeAndRemove(TemporaryStorageHandler handler, String key) throws Exception {
        handler.disposeAndRemove(key);
    }
}
```

#### 清理全部已释放存储

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

public class ClearDisposedSnippet {

    public void clear(TemporaryStorageHandler handler) throws Exception {
        handler.clearDisposed();
    }
}
```

#### 示例：`FoobarCleanupService`

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

public class FoobarCleanupService {

    private final TemporaryStorageHandler temporaryStorageHandler;

    public FoobarCleanupService(TemporaryStorageHandler temporaryStorageHandler) {
        this.temporaryStorageHandler = temporaryStorageHandler;
    }

    public void safeDelete(String key) throws Exception {
        if (!temporaryStorageHandler.exists(key)) {
            return;
        }
        if (!temporaryStorageHandler.removeIfDisposed(key)) {
            temporaryStorageHandler.disposeAndRemove(key);
        }
    }

    public void cleanAllDisposed() throws Exception {
        temporaryStorageHandler.clearDisposed();
    }
}
```

### DTO 与常量

`inspect` 返回 `TemporaryStorageInfo`，包含字段：

- `key`：临时存储键。
- `memoryBufferAllocatedLength`：当前分配的内存缓冲长度。
- `memoryBufferActualLength`：当前内存缓冲中实际写入长度。
- `fileBufferUsed`：是否使用了文件缓冲。
- `fileBufferActualLength`：文件缓冲中的实际写入长度。
- `status`：存储状态。
- `contentLength`：总内容长度，等于内存长度 + 文件长度。

状态常量定义在 `Constants`：

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.util.Constants;

public class TemporaryStorageStatusSnippet {

    public static String statusToString(int status) {
        if (status == Constants.TEMPORARY_STORAGE_STATUS_WORKING) {
            return "working";
        }
        if (status == Constants.TEMPORARY_STORAGE_STATUS_DISPOSED) {
            return "disposed";
        }
        return "unknown";
    }
}
```

## 键模型与临时文件映射封装

### 直接使用键

最直接的用法是业务方持有 `String key`，并将其作为临时内容标识。

```java
package com.example.foobar;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

import java.io.OutputStream;

public class DirectKeySnippet {

    public String save(TemporaryStorageHandler handler, byte[] data) throws Exception {
        String key = handler.create();
        try (OutputStream out = handler.openOutputStream(key, data.length)) {
            out.write(data);
        }
        return key;
    }
}
```

该方式简单直接，但在多维业务键（例如类型 + 任务 + 文件名）场景下，通常需要额外映射层来管理 `key`。

### 业务侧映射封装

- 业务层维护“业务键 -> tmpstg 键”映射。
- 创建时先创建 tmpstg 键，再写入映射。
- 读写时先查映射，再调用 tmpstg 流式 API。
- 删除时调用 `disposeAndRemove` 并清理映射。

该模式的核心价值是把 `tmpstg` 的短生命周期键封装在基础设施层，业务层只面对稳定的业务键。

### 示例：`FoobarTemporaryFileHandler`

```java
package com.example.foobar;

import com.dwarfeng.subgrade.sdk.exception.HandlerExceptionHelper;
import com.dwarfeng.subgrade.stack.exception.HandlerException;
import com.dwarfeng.tmpstg.bean.dto.TemporaryStorageInfo;
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FoobarTemporaryFileHandler {

    private final TemporaryStorageHandler temporaryStorageHandler;

    private final Map<FoobarFileKey, String> identifierMap = new HashMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FoobarTemporaryFileHandler(TemporaryStorageHandler temporaryStorageHandler) {
        this.temporaryStorageHandler = temporaryStorageHandler;
    }

    public long length(String category, String taskId, String name) throws HandlerException {
        try {
            FoobarFileKey key = new FoobarFileKey(category, taskId, name);
            String identifier;
            lock.readLock().lock();
            try {
                identifier = identifierMap.get(key);
            } finally {
                lock.readLock().unlock();
            }
            if (Objects.isNull(identifier)) {
                throw new IllegalStateException("临时文件不存在");
            }
            TemporaryStorageInfo info = temporaryStorageHandler.inspect(identifier);
            return info.getContentLength();
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    public void create(String category, String taskId, String name) throws HandlerException {
        try {
            FoobarFileKey key = new FoobarFileKey(category, taskId, name);
            String oldIdentifier;
            lock.readLock().lock();
            try {
                oldIdentifier = identifierMap.get(key);
            } finally {
                lock.readLock().unlock();
            }
            if (Objects.nonNull(oldIdentifier)) {
                temporaryStorageHandler.disposeAndRemove(oldIdentifier);
            }

            String identifier = temporaryStorageHandler.create();
            lock.writeLock().lock();
            try {
                identifierMap.put(key, identifier);
            } finally {
                lock.writeLock().unlock();
            }
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    public InputStream openInputStream(String category, String taskId, String name) throws HandlerException {
        try {
            FoobarFileKey key = new FoobarFileKey(category, taskId, name);
            String identifier;
            lock.readLock().lock();
            try {
                identifier = identifierMap.get(key);
            } finally {
                lock.readLock().unlock();
            }
            if (Objects.isNull(identifier)) {
                throw new IllegalStateException("临时文件不存在");
            }
            return temporaryStorageHandler.openInputStream(identifier);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    public OutputStream openOutputStream(String category, String taskId, String name) throws HandlerException {
        try {
            FoobarFileKey key = new FoobarFileKey(category, taskId, name);
            String identifier;
            lock.readLock().lock();
            try {
                identifier = identifierMap.get(key);
            } finally {
                lock.readLock().unlock();
            }
            if (Objects.isNull(identifier)) {
                throw new IllegalStateException("临时文件不存在");
            }
            return temporaryStorageHandler.openOutputStream(identifier);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    public void delete(String category, String taskId, String name) throws HandlerException {
        try {
            FoobarFileKey key = new FoobarFileKey(category, taskId, name);
            String identifier;
            lock.readLock().lock();
            try {
                identifier = identifierMap.get(key);
            } finally {
                lock.readLock().unlock();
            }
            if (Objects.isNull(identifier)) {
                return;
            }
            temporaryStorageHandler.disposeAndRemove(identifier);
            lock.writeLock().lock();
            try {
                identifierMap.remove(key);
            } finally {
                lock.writeLock().unlock();
            }
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static final class FoobarFileKey {

        private final String category;
        private final String taskId;
        private final String name;

        private FoobarFileKey(String category, String taskId, String name) {
            this.category = category;
            this.taskId = taskId;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FoobarFileKey that = (FoobarFileKey) o;
            return Objects.equals(category, that.category) && Objects.equals(taskId, that.taskId)
                    && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(category);
            result = 31 * result + Objects.hashCode(taskId);
            result = 31 * result + Objects.hashCode(name);
            return result;
        }
    }
}
```

### 使用建议

- 不要将 tmpstg key 直接暴露给外部调用方。
- 映射层建议提供 `create/open/delete/length` 四个核心方法，减少调用面。
- 在映射层统一补充权限、日志、审计与超时控制。
- 删除时优先 `disposeAndRemove`，避免“已释放未移除”长时间堆积。

## 线程安全与生命周期

### 生命周期

`TemporaryStorageHandler` 实现了 `StartableHandler`，核心生命周期如下：

1. `start()`：启动处理器，并按配置启动定时清理/巡检任务。
2. `stop()`：停止任务，释放并清空处理器管理的临时存储。
3. `isStarted()`：返回当前启动状态。

在 Spring 中推荐通过 `init-method="start"` 与 `destroy-method="stop"` 交由容器托管。

### 自动清理与内存巡检任务

运行过程中可选启动两类后台任务：

- 已释放存储清理任务：按 `clear_disposed_interval` 周期移除 `DISPOSED` 状态存储。
- 内存巡检任务：按 `check_memory_interval` 周期校验已分配内存统计值。

若任一间隔配置为 `<= 0`，对应任务不会启动。

### 线程安全模型

处理器内部采用分层锁模型：

- `handlerLock`：保护处理器级状态与存储映射结构。
- `memoryAllocationLock`：保护全局内存分配计数与分配/释放动作。
- `storageLock`：保护单个存储读写与状态变更。

该模型确保：

1. 不同 key 的读写尽可能并行。
2. 同一 key 的流式写入与读写冲突被正确串行化。
3. 全局内存上限在并发场景下仍然有效。

### 流式操作约束

对于 `openInputStream` / `openOutputStream`，请遵循以下约束：

1. 获取流后立即消费，不要长时间持有。
2. 必须在 `finally` 或 `try-with-resources` 中关闭。
3. 流未关闭前，不要在同一存储上发起冲突操作。
4. 输出流关闭前，内存缓冲不会执行最终回收。

不遵守上述约束，可能导致线程等待、状态异常或资源滞留。

### 异常处理建议

业务侧建议统一捕获 `HandlerException` 并映射为业务异常。

```java
package com.example.foobar;

import com.dwarfeng.subgrade.stack.exception.HandlerException;
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

import java.io.OutputStream;

public class FoobarFacade {

    private final TemporaryStorageHandler temporaryStorageHandler;

    public FoobarFacade(TemporaryStorageHandler temporaryStorageHandler) {
        this.temporaryStorageHandler = temporaryStorageHandler;
    }

    public void save(byte[] data) {
        String key = null;
        try {
            key = temporaryStorageHandler.create();
            try (OutputStream out = temporaryStorageHandler.openOutputStream(key, data.length)) {
                out.write(data);
            }
        } catch (HandlerException e) {
            throw new IllegalStateException("临时存储写入失败", e);
        } catch (Exception e) {
            throw new IllegalStateException("系统异常", e);
        } finally {
            if (key != null) {
                try {
                    temporaryStorageHandler.disposeAndRemove(key);
                } catch (Exception ignored) {
                    // 清理失败可在外层统一记录日志。
                }
            }
        }
    }
}
```

## 最佳实践

### 键规划与命名规范

- 不要把 key 作为持久主键长期保存，key 更适合短生命周期会话。
- 若业务确需关联 key，建议增加过期策略与兜底清理。
- 管理端可按“业务键 -> tmpstg 键”做二级映射，不要让上层直接操作 tmpstg key。

### 小内容与大内容策略

- 小内容优先使用 `openOutputStream(key, expectedLength)`，减少缓冲扩容。
- 大内容或长度未知内容可用 `openOutputStream(key)`，让组件动态分配。
- 超大内容建议配合磁盘性能更好的临时目录，减少写放大。

### 缓冲区与磁盘策略

- `max_buffer_size_per_storage` 控制单 key 内存峰值。
- `max_buffer_size_total` 控制实例总内存峰值。
- 当总内存上限紧张时，更多内容会落盘；需提前评估磁盘 I/O。
- 临时目录建议部署在可监控容量且可读写的独立路径。

### 清理策略

- 正常链路建议优先 `disposeAndRemove` 一步到位。
- 若有“先释放后人工复核”的场景，可 `dispose` 后再 `remove`。
- 若启用了自动清理任务，仍建议业务链路主动清理，避免积压窗口过长。

### 流式传输策略

- 强制使用 `try-with-resources`。
- 不要将同一个流对象跨线程传递。
- 不要把流持有到 RPC/消息边界之外。
- 写入后如需立即读回校验，请先关闭输出流再打开输入流。

### 幂等与补偿

- 写入重试时建议“新建 key 再替换映射”，避免污染原存储状态。
- 删除重试可优先调用 `removeIfDisposed`，失败再走 `disposeAndRemove`。
- 对批处理任务记录“已清理 key 集合”，便于失败后续跑。

### 安全建议

- 不在日志中打印完整 key、临时目录绝对路径与敏感上下文。
- 临时目录应避免与业务永久文件目录混用。
- 生产环境最小权限原则：仅授予进程必要目录读写权限。

### 代码组织建议

推荐将 tmpstg 访问逻辑收敛到独立网关/处理器组件，避免在业务代码中到处出现 `create/open/dispose`。

```java
package com.example.foobar;

import com.dwarfeng.dutil.basic.io.IOUtil;
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;

import java.io.*;

public class FoobarTemporaryContentGateway {

    private final TemporaryStorageHandler temporaryStorageHandler;

    public FoobarTemporaryContentGateway(TemporaryStorageHandler temporaryStorageHandler) {
        this.temporaryStorageHandler = temporaryStorageHandler;
    }

    public String save(InputStream source, long expectedLength) throws Exception {
        String key = temporaryStorageHandler.create();
        try (OutputStream out = temporaryStorageHandler.openOutputStream(key, expectedLength)) {
            IOUtil.trans(source, out, 4096);
        }
        return key;
    }

    public void load(String key, OutputStream target) throws Exception {
        try (InputStream in = temporaryStorageHandler.openInputStream(key)) {
            IOUtil.trans(in, target, 4096);
        }
    }

    public void delete(String key) throws Exception {
        temporaryStorageHandler.disposeAndRemove(key);
    }
}
```

这种结构有三个好处：

1. API 访问集中化，减少误用。
2. 异常与重试策略集中化，便于治理。
3. 便于统一加入审计、限流、观测等横切逻辑。

### 监控与运维检查清单

建议在生产环境维护如下检查项：

1. 临时目录可用性探针（存在性、读写权限、剩余空间）。
2. 周期写入/读取探针（覆盖内存态与落盘态两种长度）。
3. 存储数量与已释放未移除数量监控。
4. 后台清理任务执行次数与失败次数监控。
5. 异常类型分布监控（未启动/不存在/状态无效/流打开失败）。

### 发布前检查清单

上线前建议逐项确认：

1. `tmpstg.*` 参数与目标环境一致。
2. `scheduler` 已注册且线程池参数满足负载。
3. 所有流式代码使用 `try-with-resources`。
4. 清理逻辑覆盖成功、失败、超时三条路径。
5. 已对临时目录磁盘容量与读写权限做预检查。

## 常见问题

### 处理器未启动异常

现象：调用任一 API 抛出“临时存储处理器处于停止状态”。

排查顺序建议：

1. 是否在 Spring 之外手动创建了处理器但未调用 `start()`。
2. 是否提前调用了 `stop()`。
3. 是否容器启动顺序异常导致业务 Bean 早于处理器可用。

### 存储不存在异常

现象：`inspect/open/remove` 抛出“临时存储 xxx 不存在”。

常见原因：

1. key 拼写错误或已经被 `remove`。
2. 已执行 `disposeAndRemove` 后仍使用旧 key。
3. 映射层更新不一致导致读取了过期 key。

### 状态无效异常

现象：`remove` 或 `dispose` 抛出状态不合法。

典型触发：

1. 对 `WORKING` 状态直接调用 `remove`。
2. 对 `DISPOSED` 状态重复 `dispose`。

建议按状态机顺序操作：

- `WORKING -> dispose -> DISPOSED -> remove`。
- 或直接 `disposeAndRemove`。

### 流打开失败异常

现象：`openInputStream/openOutputStream` 抛出无法打开流。

常见原因：

1. 存储状态不是 `WORKING`。
2. 临时目录异常（不可写、磁盘故障、路径权限变化）。
3. 并发冲突下上游未正确关闭流，导致后续操作失败。

### 为什么写入后再次写入会覆盖旧内容

`openOutputStream` 的语义是“打开一个用于写入当前存储内容的新流”，实现中会重置原有内容并重新分配缓冲区。

如果您需要版本化，请使用新 key 保存新版本，并在映射层切换引用。

### 为什么不 remove 会“看起来有泄漏”

`dispose` 只释放内容和资源，不会从处理器映射中移除 key。

如果业务只调用 `dispose` 不调用 `remove`，key 仍会出现在 `keys()` 中，直到手动 `remove` 或自动清理任务执行。

### 为什么 clearDisposed 不生效

`clearDisposed` 只会清理状态为 `DISPOSED` 的存储。

如果 key 仍为 `WORKING`，该方法不会移除。建议先执行 `dispose(key)` 再 `clearDisposed()`。

### 如何判断内容是否落盘

可以通过 `inspect(key)` 的以下字段判断：

- `fileBufferUsed=false`：仅使用内存缓冲。
- `fileBufferUsed=true`：已使用文件缓冲。
- `fileBufferActualLength>0`：说明有内容实际写入了磁盘缓冲。

### 本地配置与 classpath 配置冲突

当使用 `property-placeholder` 且开启本地覆盖时，请确认加载顺序与 `local-override` 配置。

建议统一规定配置覆盖目录，并在启动日志中打印关键参数摘要，降低多源配置歧义。

## 参阅

- [Quick Start](./QuickStart.md) - 快速开始，用最快的方式体验本项目。
- [Install by Source Code](./InstallBySourceCode.md) - 通过源码安装本项目。
- [Introduction](./Introduction.md) - 项目简介。
- [Version Blacklist](./VersionBlacklist.md) - 版本黑名单。
- [README](./README.md) - 文档入口与项目总体说明。
