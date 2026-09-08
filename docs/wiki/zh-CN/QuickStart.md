# Quick Start - 快速开始

## 源码下载

使用 git 进行源码下载。

```shell
git clone git@github.com:DwArFeng/dwarfeng-tmpstg.git
```

对于中国用户，可以使用 gitee 进行高速下载。

```shell
git clone git@gitee.com:dwarfeng/dwarfeng-tmpstg.git
```

## 引入依赖

在项目的 `pom.xml` 中按需添加核心模块或 API 模块依赖：

```xml
<dependency>
    <groupId>com.dwarfeng</groupId>
    <artifactId>dwarfeng-tmpstg-core</artifactId>
    <version>${dwarfeng-tmpstg.version}</version>
</dependency>
<dependency>
    <groupId>com.dwarfeng</groupId>
    <artifactId>dwarfeng-tmpstg-api</artifactId>
    <version>${dwarfeng-tmpstg.version}</version>
</dependency>
```

## 最小化配置

在下载的源码目录下，新建 `conf/test.tmpstg` 目录。

运行 core 示例时，将 `dwarfeng-tmpstg-core/src/test/resources/tmpstg/tmpstg-settings.properties` 文件复制到新建好的目录下；
运行 API 示例时，将 `dwarfeng-tmpstg-api/src/test/resources/tmpstg/tmpstg-settings.properties` 文件复制到新建好的目录下，
并按照注释按需修改配置项。

## 效果体验

运行 `dwarfeng-tmpstg-core/src/test` 下的示例以观察核心特性。

| 示例类名                                                    | 说明           |
|-------------------------------------------------------------|----------------|
| com.dwarfeng.tmpstg.node.example.ConcurrentOperationExample | 多线程操作示例 |
| com.dwarfeng.tmpstg.node.example.MultipleReadsExample       | 多次读取示例   |
| com.dwarfeng.tmpstg.node.example.MultipleWritesExample      | 多次写入示例   |
| com.dwarfeng.tmpstg.node.example.ProcessExample             | 流程示例       |

运行 `dwarfeng-tmpstg-api/src/test` 下的示例以观察 API 扩展特性。

| 示例类名                                                  | 说明                                           |
|-----------------------------------------------------------|------------------------------------------------|
| com.dwarfeng.tmpstg.api.integration.example.TelqosExample | Telqos 示例：通过运维指令操作临时存储 QoS 服务 |
