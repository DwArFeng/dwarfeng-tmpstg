# dwarfeng-tmpstg

Dwarfeng（赵扶风）的临时存储服务，基于 `subgrade` 项目，使用内存和文件的混合策略，以提高小文件的访问速度，
目前用于多个个人项目。

---

## 特性

1. Subgrade 架构支持。
2. 创建的临时存储，并使用输入流和输出流进行读写，支持多次读写。
3. 写入少量数据时，数据会被写入到内存缓冲，以提高访问速度。
4. 写入大量数据时，超过内存缓冲大小的剩余数据会被写入到文件中，以避免内存过多占用。
5. 支持并发操作，线程安全。
6. 多个临时存储同时写入数据时，保证所有临时存储占用的内存总和不超过指定的大小，以避免多个临时存储同时使用时内存过多占用。
7. 多级别锁机制，一个临时存储使用时不会阻塞其他临时存储的读写操作。
8. 临时存储释放后支持手动清理以及自动清理（可通过选项选择是否启用），适用于不同的场景。

运行 `src/test` 下的示例以观察全部特性。

| 示例类名                                                   | 说明      |
|--------------------------------------------------------|---------|
| com.dwarfeng.tmpstg.example.ConcurrentOperationExample | 多线程操作示例 |
| com.dwarfeng.tmpstg.example.MultipleReadsExample       | 多次读取示例  |
| com.dwarfeng.tmpstg.example.MultipleWritesExample      | 多次写入示例  |
| com.dwarfeng.tmpstg.example.ProcessExample             | 流程示例    |

## 文档

该项目的文档位于 [docs](../../../docs) 目录下，包括：

### wiki

wiki 为项目的开发人员为本项目编写的详细文档，包含不同语言的版本，主要入口为：

1. [简介](./Introduction.md) - 即本文件。
2. [目录](./Contents.md) - 文档目录。

## 安装说明

1. 下载源码。

   - 使用 git 进行源码下载。
        ```
        git clone git@github.com:DwArFeng/dwarfeng-tmpstg.git
        ```

   - 对于中国用户，可以使用 gitee 进行高速下载。
      ```
      git clone git@gitee.com:dwarfeng/dwarfeng-tmpstg.git
      ```

2. 项目安装。

   进入项目根目录，执行 maven 命令
    ```
    mvn clean source:jar install
    ```

3. 项目引入。

   在项目的 pom.xml 中添加如下依赖：
   ```xml
   <dependency>
       <groupId>com.dwarfeng</groupId>
       <artifactId>dwarfeng-tmpstg</artifactId>
       <version>${dwarfeng-tmpstg.version}</version>
   </dependency>
   ```

4. enjoy it.

---

## 如何使用

1. 运行 `src/test` 下的示例代码以观察全部特性。
2. 观察项目结构，将其中的配置运用到其它的 subgrade 项目中。

### 单例模式

加载 `com.dwarfeng.tmpstg.configuration.SingletonConfiguration`，即可获得单例模式的 `TmpstgHandler`。  
在项目的 `application-context-scan.xml` 中追加 `com.dwarfeng.tmpstg.configuration` 包中
`SingletonConfiguration` 的扫描，示例如下:

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

    <!-- 扫描 configuration 包中的 SingletonConfiguration -->
    <context:component-scan base-package="com.dwarfeng.tmpstg.configuration" use-default-filters="false">
        <context:include-filter
                type="assignable"
                expression="com.dwarfeng.tmpstg.configuration.SingletonConfiguration"
        />
    </context:component-scan>
</beans>
```

### 多实例模式

不使用包扫描，使用 xml 或者配置类生成 `TemporaryStorageHandlerImpl` 实例。  
在项目的 `bean-definition.xml` 中追加配置，示例如下:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- 以下注释用于抑制 idea 中 .md 的警告，实际并无错误，在使用时可以连同本注释一起删除。 -->
<!--suppress SpringBeanConstructorArgInspection, SpringXmlModelInspection, SpringPlaceholdersInspection -->
<beans
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.springframework.org/schema/beans"
        xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd"
>
    <!-- 第一个实例 -->
    <bean name="configBuilder1" class="com.dwarfeng.tmpstg.struct.TemporaryStorageConfig.Builder">
        <property name="temporaryFileDirectoryPath" value="${tmpstg.temporary_file_directory_path.1}"/>
        <property name="temporaryFilePrefix" value="${tmpstg.temporary_file_prefix.1}"/>
        <property name="temporaryFileSuffix" value="${tmpstg.temporary_file_suffix.1}"/>
        <property name="maxBufferSizePerStorage" value="${tmpstg.max_buffer_size_per_storage.1}"/>
        <property name="maxBufferSizeTotal" value="${tmpstg.max_buffer_size_total.1}"/>
        <property name="clearDisposedInterval" value="${tmpstg.clear_disposed_interval.1}"/>
        <property name="checkMemoryInterval" value="${tmpstg.check_memory_interval.1}"/>
    </bean>
    <bean name="config1" factory-bean="configBuilder1" factory-method="build"/>
    <bean name="instance1" class="com.dwarfeng.tmpstg.handler.TemporaryStorageHandlerImpl">
        <constructor-arg name="scheduler" ref="scheduler"/>
        <constructor-arg name="config" ref="config1"/>
    </bean>

    <!-- 第二个实例 -->
    <bean name="configBuilder2" class="com.dwarfeng.tmpstg.struct.TemporaryStorageConfig.Builder">
        <property name="temporaryFileDirectoryPath" value="${tmpstg.temporary_file_directory_path.2}"/>
        <property name="temporaryFilePrefix" value="${tmpstg.temporary_file_prefix.2}"/>
        <property name="temporaryFileSuffix" value="${tmpstg.temporary_file_suffix.2}"/>
        <property name="maxBufferSizePerStorage" value="${tmpstg.max_buffer_size_per_storage.2}"/>
        <property name="maxBufferSizeTotal" value="${tmpstg.max_buffer_size_total.2}"/>
        <property name="clearDisposedInterval" value="${tmpstg.clear_disposed_interval.2}"/>
        <property name="checkMemoryInterval" value="${tmpstg.check_memory_interval.2}"/>
    </bean>
    <bean name="config2" factory-bean="configBuilder2" factory-method="build"/>
    <bean name="instance2" class="com.dwarfeng.tmpstg.handler.TemporaryStorageHandlerImpl">
        <constructor-arg name="scheduler" ref="scheduler"/>
        <constructor-arg name="config" ref="config2"/>
    </bean>
</beans>
```

### 任意数量的实例模式

自行设计 `TemporaryStorageHandler` 的工厂类，调用相关工厂方法生成 `TemporaryStorageHandlerImpl` 实例。
需要注意的是：生成的 `TemporaryStorageHandlerImpl` 在使用之前需要调用 `TemporaryStorageHandlerImpl#start()` 启动处理器；
同时在使用完毕之后， 需要调用 `TemporaryStorageHandlerImpl#stop()` 关闭处理器。
