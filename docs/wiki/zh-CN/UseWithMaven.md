# Use with Maven - 通过 Maven 使用本项目

## 安装本项目

请参考 [Install by Source Code](./InstallBySourceCode.md) 安装本项目。

## 使用本项目

在 Maven 中使用本项目非常简单，只需要在 `pom.xml` 中添加如下依赖即可：

```xml
<?xml version="1.0" encoding="UTF-8"?>

<!--suppress MavenModelInspection, MavenModelVersionMissed -->
<project
        xmlns="http://maven.apache.org/POM/4.0.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
        http://maven.apache.org/xsd/maven-4.0.0.xsd"
>

    <!-- 省略其他配置... -->
    <dependencies>
        <!-- 省略其他配置... -->
        <dependency>
            <groupId>com.dwarfeng</groupId>
            <artifactId>dwarfeng-tmpstg</artifactId>
            <version>${dwarfeng-tmpstg.version}</version>
        </dependency>
        <!-- 省略其他配置... -->
    </dependencies>
    <!-- 省略其他配置... -->
</project>
```

## 参阅

- [Install by Source Code](./InstallBySourceCode.md) - 通过源码安装本项目。
