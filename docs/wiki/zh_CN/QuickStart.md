# Quick Start - 快速开始

## 确认系统需求

- CPU：2核以上。
- 内存：2G 以上。
- 硬盘：50G 以上。

## 源码下载

使用 git 进行源码下载。

```shell
git clone git@github.com:DwArFeng/dwarfeng-tmpstg.git
```

对于中国用户，可以使用 gitee 进行高速下载。

```shell
git clone git@gitee.com:dwarfeng/dwarfeng-tmpstg.git
```

## 最小化配置

在下载的源码目录下，新建 `conf/test.tmpstg` 目录。

随后，将 `src/test/resources/tmpstg/tmpstg-settings.properties` 文件复制到新建好的目录下，
并按照注释按需修改配置项。

## 效果体验

运行 `src/test` 下的示例以观察全部特性。

| 示例类名                                                   | 说明      |
|--------------------------------------------------------|---------|
| com.dwarfeng.tmpstg.example.ConcurrentOperationExample | 多线程操作示例 |
| com.dwarfeng.tmpstg.example.MultipleReadsExample       | 多次读取示例  |
| com.dwarfeng.tmpstg.example.MultipleWritesExample      | 多次写入示例  |
| com.dwarfeng.tmpstg.example.ProcessExample             | 流程示例    |
