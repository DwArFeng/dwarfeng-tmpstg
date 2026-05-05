# ChangeLog

## Release_2.0.0_20260429_build_A

### 功能构建

- 更新 README.md。

- Wiki 更新。
  - docs/wiki/zh-CN/Introduction.md。

- 新增 spring-telqos 框架集成指令。
  - com.dwarfeng.tmpstg.api.integration.springtelqos.TmpstgCommand。

- 增加依赖。
  - 增加依赖 `spring-telqos` 以应用其新功能，版本为 `2.0.0.a`。

- 为项目增加 xsd 配置机制。
  - 增加 `META-INF/dwarfeng-tmpstg.xsd` 文件。
  - 增加 `com.dwarfeng.tmpstg.node.configuration.TemporaryStorageNamespaceHandlerSupport` 及对应的定义解析器。
  - 调整测试目录的相关配置文件，以使用新的 xsd 配置机制。

- 新增 QoS 服务。
  - com.dwarfeng.tmpstg.stack.service.TemporaryStorageQosService。

- 重构项目模块。
  - 新增 `dwarfeng-tmpstg-core` 子模块，并迁移原有代码至该模块。
  - 新增 `dwarfeng-tmpstg-api` 子模块。

- 重构项目结构。
  - 将项目构型更改为 subgrade 稳健式标准构型。

### Bug 修复

- (无)

### 功能移除

- (无)

---

## 更早的版本

[View all changelogs](./changelogs)
