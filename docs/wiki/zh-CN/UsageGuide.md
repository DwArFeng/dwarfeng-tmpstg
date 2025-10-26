# Usage Guide - 使用指南

本文档将详细介绍如何使用 dwarfeng-tmpstg 临时存储服务，包括基本概念、配置方法、API 使用以及最佳实践。

## 目录

- [基本概念](#基本概念)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [API 使用](#api-使用)
- [使用示例](#使用示例)
- [最佳实践](#最佳实践)
- [常见问题](#常见问题)

## 基本概念

### 临时存储

临时存储是 dwarfeng-tmpstg 的核心概念，它是一个可以存储任意二进制数据的容器。每个临时存储都有一个唯一的键（key），通过这个键可以访问存储的数据。

### 混合存储策略

dwarfeng-tmpstg 采用内存和文件的混合存储策略：

- **小数据**：当数据量小于配置的内存缓冲区大小时，数据完全存储在内存中，提供极快的访问速度。
- **大数据**：当数据量超过内存缓冲区大小时，超出部分会被写入临时文件，避免内存过度占用。
- **动态切换**：系统会根据数据大小自动在内存和文件存储之间切换。

### 线程安全

所有操作都是线程安全的，支持多线程并发访问。系统使用多级锁机制，确保不同临时存储之间的操作不会相互阻塞。

## 快速开始

### 1. 添加依赖

在项目的 `pom.xml` 中添加依赖：

```xml

<dependency>
    <groupId>com.dwarfeng</groupId>
    <artifactId>dwarfeng-tmpstg</artifactId>
    <version>${dwarfeng-tmpstg.version}</version>
</dependency>
```

### 2. 配置 Spring 上下文

创建 Spring 配置文件 `application-context.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans
        xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:context="http://www.springframework.org/schema/context"
        xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd"
>

    <!-- 扫描配置类 -->
    <context:component-scan base-package="com.dwarfeng.tmpstg.configuration" use-default-filters="false">
        <context:include-filter
                type="assignable"
                expression="com.dwarfeng.tmpstg.configuration.SingletonConfiguration"
        />
    </context:component-scan>
</beans>
```

### 3. 基本使用

```java
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.io.OutputStream;

public class BasicUsageExample {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TemporaryStorageHandler temporaryStorageHandler;

    public void basicUsage() {
        // 创建临时存储。
        String key = temporaryStorageHandler.create();

        // 写入数据。
        try (OutputStream out = temporaryStorageHandler.openOutputStream(key)) {
            out.write("Hello, World!".getBytes());
        }

        // 读取数据。
        try (InputStream in = temporaryStorageHandler.openInputStream(key)) {
            byte[] buffer = new byte[1024];
            int length = in.read(buffer);
            String content = new String(buffer, 0, length);
            System.out.println(content); // 输出: Hello, World!。
        }

        // 清理资源。
        temporaryStorageHandler.disposeAndRemove(key);
    }
}
```

## 配置说明

### 配置参数

| 参数名                          | 默认值                                    | 说明               |
|------------------------------|----------------------------------------|------------------|
| `temporaryFileDirectoryPath` | `System.getProperty("java.io.tmpdir")` | 临时文件目录路径         |
| `temporaryFilePrefix`        | `"tmpstg-"`                            | 临时文件前缀           |
| `temporaryFileSuffix`        | `".tmp"`                               | 临时文件后缀           |
| `maxBufferSizePerStorage`    | `2048`                                 | 单个存储的最大缓冲区大小（字节） |
| `maxBufferSizeTotal`         | `1048576`                              | 总的缓冲区大小限制（字节）    |
| `clearDisposedInterval`      | `300000`                               | 清理已释放存储的间隔（毫秒）   |
| `checkMemoryInterval`        | `60000`                                | 检查内存的间隔（毫秒）      |

### 配置文件示例

创建 `tmpstg-settings.properties` 文件：

```properties
# 临时文件目录路径。
tmpstg.temporary_file_directory_path=temporary-file
# 临时文件前缀。
tmpstg.temporary_file_prefix=tmpstg-
# 临时文件后缀。
tmpstg.temporary_file_suffix=.tmp
# 单个存储的最大缓冲区大小。
tmpstg.max_buffer_size_per_storage=2048
# 总的缓冲区大小限制。
tmpstg.max_buffer_size_total=1048576
# 清理已释放存储的间隔。
tmpstg.clear_disposed_interval=300000
# 检查内存的间隔。
tmpstg.check_memory_interval=60000
```

## API 使用

### 核心接口

`TemporaryStorageHandler` 是临时存储服务的核心接口，提供以下主要方法：

#### 存储管理

```java
import com.dwarfeng.tmpstg.bean.dto.TemporaryStorageInfo;
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

public class StorageManagementExample {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TemporaryStorageHandler temporaryStorageHandler;

    public void storageManagementOperations() {
        // 创建新的临时存储。
        String key = temporaryStorageHandler.create();

        // 检查键是否存在。
        boolean exists = temporaryStorageHandler.exists(key);

        // 获取存储信息。
        TemporaryStorageInfo info = temporaryStorageHandler.inspect(key);

        // 获取所有键。
        Collection<String> keys = temporaryStorageHandler.keys();
    }
}
```

#### 数据读写

```java
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import com.dwarfeng.tmpstg.handler.HandlerException;

import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Autowired;

public class DataReadWriteExample {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TemporaryStorageHandler temporaryStorageHandler;

    @SuppressWarnings("resource")
    public void dataReadWriteOperations(String key, long expectedLength) {
        // 打开输出流（数据长度未知）。
        OutputStream out = temporaryStorageHandler.openOutputStream(key);

        // 打开输出流（数据长度已知）。
        OutputStream outWithLength = temporaryStorageHandler.openOutputStream(key, expectedLength);

        // 打开输入流。
        InputStream in = temporaryStorageHandler.openInputStream(key);
    }
}
```

#### 资源清理

```java
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import com.dwarfeng.tmpstg.handler.HandlerException;
import org.springframework.beans.factory.annotation.Autowired;

public class ResourceCleanupExample {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TemporaryStorageHandler temporaryStorageHandler;

    public void resourceCleanupOperations(String key) {
        // 释放存储（删除临时文件，释放内存）。
        temporaryStorageHandler.dispose(key);

        // 移除存储（从处理器中移除）。
        temporaryStorageHandler.remove(key);

        // 释放并移除存储。
        temporaryStorageHandler.disposeAndRemove(key);

        // 移除已释放的存储。
        boolean removed = temporaryStorageHandler.removeIfDisposed(key);

        // 清理所有已释放的存储。
        temporaryStorageHandler.clearDisposed();
    }
}
```

### 重要注意事项

1. **流管理**：使用完流后必须关闭，否则会导致资源泄漏。
2. **状态检查**：操作前确保处理器已启动。
3. **键存在性**：操作前确保键存在。
4. **状态一致性**：确保存储处于正确状态（工作中/已释放）。

## 使用示例

### 示例 1：基本读写操作

```java
public class BasicReadWriteExample {

    @Autowired
    private TemporaryStorageHandler handler;

    public void basicReadWrite() throws HandlerException {
        // 创建存储。
        String key = handler.create();

        try {
            // 写入数据。
            String data = "这是测试数据";
            try (OutputStream out = handler.openOutputStream(key, data.getBytes().length)) {
                out.write(data.getBytes());
            }

            // 读取数据。
            try (InputStream in = handler.openInputStream(key)) {
                byte[] buffer = new byte[1024];
                int length = in.read(buffer);
                String readData = new String(buffer, 0, length);
                System.out.println("读取的数据: " + readData);
            }

        } finally {
            // 清理资源。
            handler.disposeAndRemove(key);
        }
    }
}
```

### 示例 2：多次读写操作

```java
public class MultipleReadWriteExample {

    @Autowired
    private TemporaryStorageHandler handler;

    public void multipleOperations() throws HandlerException {
        String key = handler.create();

        try {
            // 多次写入不同数据。
            for (int i = 0; i < 5; i++) {
                String data = "第 " + i + " 次写入的数据";

                // 写入数据。
                try (OutputStream out = handler.openOutputStream(key, data.getBytes().length)) {
                    out.write(data.getBytes());
                }

                // 读取并验证。
                try (InputStream in = handler.openInputStream(key)) {
                    byte[] buffer = new byte[1024];
                    int length = in.read(buffer);
                    String readData = new String(buffer, 0, length);
                    System.out.println("第 " + i + " 次读取: " + readData);
                }
            }

        } finally {
            handler.disposeAndRemove(key);
        }
    }
}
```

### 示例 3：并发操作

```java
public class ConcurrentOperationExample {

    @Autowired
    private TemporaryStorageHandler handler;

    @Autowired
    private ThreadPoolTaskExecutor executor;

    @SuppressWarnings("CallToPrintStackTrace")
    public void concurrentOperations() throws Exception {
        List<Future<?>> futures = new ArrayList<>();

        // 创建多个并发任务。
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            Future<?> future = executor.submit(() -> {
                try {
                    String key = handler.create();

                    // 写入任务特定数据。
                    String data = "任务 " + taskId + " 的数据";
                    try (OutputStream out = handler.openOutputStream(key, data.getBytes().length)) {
                        out.write(data.getBytes());
                    }

                    // 读取并验证。
                    try (InputStream in = handler.openInputStream(key)) {
                        byte[] buffer = new byte[1024];
                        int length = in.read(buffer);
                        String readData = new String(buffer, 0, length);
                        System.out.println("任务 " + taskId + " 读取: " + readData);
                    }

                    // 清理。
                    handler.disposeAndRemove(key);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            futures.add(future);
        }

        // 等待所有任务完成。
        for (Future<?> future : futures) {
            future.get();
        }
    }
}
```

### 示例 4：大文件处理

```java
public class LargeFileExample {

    @Autowired
    private TemporaryStorageHandler handler;

    public void handleLargeFile() throws HandlerException {
        String key = handler.create();

        try {
            // 模拟大文件写入（超过内存缓冲区大小）。
            byte[] largeData = new byte[10240]; // 10KB 数据。
            Arrays.fill(largeData, (byte) 'A');

            // 写入大文件。
            try (OutputStream out = handler.openOutputStream(key, largeData.length)) {
                out.write(largeData);
            }

            // 检查存储信息。
            TemporaryStorageInfo info = handler.inspect(key);
            System.out.println("存储信息: " + info);

            // 读取大文件。
            try (InputStream in = handler.openInputStream(key)) {
                byte[] buffer = new byte[1024];
                int totalRead = 0;
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    totalRead += bytesRead;
                }
                System.out.println("总共读取: " + totalRead + " 字节");
            }

        } finally {
            handler.disposeAndRemove(key);
        }
    }
}
```

## 最佳实践

### 1. 资源管理

**推荐做法**：

```java
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import com.dwarfeng.tmpstg.handler.HandlerException;
import org.springframework.beans.factory.annotation.Autowired;

public class ResourceManagementGoodExample {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TemporaryStorageHandler handler;

    @SuppressWarnings("EmptyTryBlock")
    public void recommendedResourceManagement() {
        String key = handler.create();
        try {
            // 使用存储。
            // ...
        } finally {
            handler.disposeAndRemove(key);
        }
    }
}
```

**避免**：

```java
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import com.dwarfeng.tmpstg.handler.HandlerException;
import org.springframework.beans.factory.annotation.Autowired;

public class ResourceManagementBadExample {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TemporaryStorageHandler handler;

    public void badResourceManagement() {
        String key = handler.create();
        // 使用存储。
        // 忘记清理资源 - 这会导致内存泄漏。
    }
}
```

### 2. 流管理

**推荐做法**：

```java
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import com.dwarfeng.tmpstg.handler.HandlerException;

import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Autowired;

public class StreamManagementGoodExample {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TemporaryStorageHandler handler;

    public void recommendedStreamManagement(String key, byte[] data) {
        try (OutputStream out = handler.openOutputStream(key)) {
            out.write(data);
        }
        // 流会自动关闭。
    }
}
```

**避免**：

```java
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import com.dwarfeng.tmpstg.handler.HandlerException;

import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Autowired;

public class StreamManagementBadExample {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TemporaryStorageHandler handler;

    @SuppressWarnings("resource")
    public void badStreamManagement(String key, byte[] data) {
        OutputStream out = handler.openOutputStream(key);
        out.write(data);
        // 忘记关闭流 - 这会导致资源泄漏。
    }
}
```

### 3. 数据长度优化

**已知数据长度时**：

```java
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import com.dwarfeng.tmpstg.handler.HandlerException;

import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Autowired;

public class DataLengthOptimizationKnownExample {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TemporaryStorageHandler handler;

    public void dataLengthOptimizationKnown(String key, byte[] data) {
        // 推荐：指定预期长度，避免不必要的内存分配。
        try (OutputStream out = handler.openOutputStream(key, data.length)) {
            out.write(data);
        }
    }
}
```

**未知数据长度时**：

```java
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import com.dwarfeng.tmpstg.handler.HandlerException;

import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Autowired;

public class DataLengthOptimizationUnknownExample {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TemporaryStorageHandler handler;

    public void dataLengthOptimizationUnknown(String key, byte[] data) {
        // 推荐：使用默认方法，让系统自动管理。
        try (OutputStream out = handler.openOutputStream(key)) {
            out.write(data);
        }
    }
}
```

### 4. 错误处理

```java
import com.dwarfeng.subgrade.sdk.exception.HandlerExceptionHelper;
import com.dwarfeng.subgrade.stack.exception.HandlerException;
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ErrorHandlingExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandlingExample.class);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TemporaryStorageHandler handler;

    public void safeOperation() {
        String key = null;
        try {
            key = handler.create();
            // 执行操作。
            // ...
        } catch (Exception e) {
            // 使用 HandlerExceptionHelper 进行异常映射（subgrade 项目提供）。
            throw HandlerExceptionHelper.parse(e);
        } finally {
            if (key != null) {
                try {
                    handler.disposeAndRemove(key);
                } catch (Exception e) {
                    // 清理资源失败时记录日志，但不抛出异常。
                    LOGGER.warn("清理资源失败", e);
                }
            }
        }
    }

}
```

### 5. 异常处理最佳实践

**推荐做法**：

```java
import com.dwarfeng.subgrade.sdk.exception.HandlerExceptionHelper;

public class ExceptionHandlingBestPractice {

    @SuppressWarnings("EmptyTryBlock")
    public void recommendedExceptionHandling() {
        try {
            // 业务逻辑。。
            // ...
        } catch (Exception e) {
            // 使用 HandlerExceptionHelper 进行异常映射（subgrade 项目提供）。
            throw HandlerExceptionHelper.parse(e);
        }
    }
}
```

HandlerExceptionHelper 是 subgrade 项目提供的异常处理工具类，主要用于异常类型转换。
其 `parse(Exception e)` 方法能够将任意异常转换为 `HandlerException` 类型，
如果传入的异常已经是 `HandlerException` 则直接返回，否则会包装为新的 `HandlerException` 并保持原始异常的堆栈跟踪信息。
这种方式简化了异常处理逻辑，避免了手动判断异常类型的复杂性。

**避免**：

```java

@SuppressWarnings("EmptyTryBlock")
public void badExceptionHandling() {
    try {
        // 业务逻辑。
        // ...
    } catch (HandlerException e) {
        // 手动处理业务异常 - 不推荐。
        log.error("业务异常", e);
    } catch (Exception e) {
        // 手动处理系统异常 - 不推荐。
        log.error("系统异常", e);
    }
}
```

### 6. 性能优化

- **合理设置缓冲区大小**：根据应用场景调整 `maxBufferSizePerStorage` 和 `maxBufferSizeTotal`。
- **批量操作**：对于大量小文件，考虑批量处理。
- **及时清理**：使用完毕后及时释放资源。
- **监控内存使用**：定期检查内存使用情况。

## 常见问题

### Q1: 如何处理内存不足的情况？

A: dwarfeng-tmpstg 会自动将超出内存缓冲区的数据写入临时文件，避免内存溢出。如果仍然遇到内存问题，可以：

1. 减小 `maxBufferSizePerStorage` 参数。
2. 减小 `maxBufferSizeTotal` 参数。
3. 增加系统内存。
4. 优化应用程序的内存使用。

### Q2: 临时文件会占用磁盘空间吗？

A: 是的，当数据超过内存缓冲区大小时，会创建临时文件。建议：

1. 定期清理已释放的存储。
2. 设置合适的 `clearDisposedInterval` 参数。
3. 确保临时文件目录有足够的磁盘空间。

### Q3: 如何处理并发访问？

A: dwarfeng-tmpstg 是线程安全的，支持多线程并发访问。但需要注意：

1. 同一个存储的读写操作会串行化。
2. 不同存储之间的操作可以并行执行。
3. 避免长时间持有流对象。

### Q4: 如何监控存储状态？

A: 可以使用以下方法监控存储状态：

```java
import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import com.dwarfeng.tmpstg.handler.HandlerException;
import com.dwarfeng.tmpstg.bean.dto.TemporaryStorageInfo;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

public class StorageMonitoringExample {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TemporaryStorageHandler handler;

    public void monitorStorageStatus(String key) {
        // 检查存储是否存在。
        boolean exists = handler.exists(key);

        // 获取存储详细信息。
        TemporaryStorageInfo info = handler.inspect(key);

        // 获取所有存储的键。
        Collection<String> keys = handler.keys();
    }
}
```

### Q5: 如何处理异常情况？

A: 常见的异常类型：

- `TemporaryStorageNotExistsException`：存储不存在。
- `TemporaryStorageInvalidStatusException`：存储状态无效。
- `TemporaryStorageStreamOpenException`：流打开失败。
- `TemporaryStorageHandlerStoppedException`：处理器未启动。

建议在代码中适当处理这些异常。

---

通过本文档，您应该能够熟练使用 dwarfeng-tmpstg 临时存储服务。如有其他问题，请参考项目源码中的示例代码或提交 Issue。
