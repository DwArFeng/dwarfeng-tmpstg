# ChangeLog

## Release_1.0.8_20260101_build_A

### 功能构建

- Wiki 编写。
  - docs/wiki/zh-CN/UsageGuide.md。

- Wiki 更新。
  - docs/wiki/zh-CN/QuickStart.md。
  - docs/wiki/en-US/README.md。
  - docs/wiki/zh-CN/README.md。

- 优化部分类的代码，不再使用部分弃用的 API。
  - com.dwarfeng.tmpstg.example.ConcurrentOperationExample。
  - com.dwarfeng.tmpstg.example.MultipleWritesExample。

- 类优化注释、文档注释格式、代码换行格式。
  - com.dwarfeng.tmpstg.util.ServiceExceptionHelper。
  - com.dwarfeng.tmpstg.example.ConcurrentOperationExample。

- 优化文件格式。
  - 优化 `application-context-*.xml` 文件的格式。

- 依赖升级。
  - 升级 `log4j2` 依赖版本为 `2.25.3` 以规避漏洞。
  - 升级 `dutil` 依赖版本为 `0.4.0.a-beta` 以规避漏洞。
  - 升级 `subgrade` 依赖版本为 `1.6.2.a` 以规避漏洞。

- 优化开发环境支持。
  - 在 .gitignore 中添加 VSCode 相关文件的忽略规则。

### Bug 修复

- (无)

### 功能移除

- (无)

---

## Release_1.0.7_20251012_build_A

### 功能构建

- 优化 `docs/wiki` 目录结构。
  - 将 `docs/wiki/en_US` 目录重命名为 `en-US`，以符合 rfc5646 规范。
  - 将 `docs/wiki/zh_CN` 目录重命名为 `zh-CN`，以符合 rfc5646 规范。
  - 更新 `docs/wiki/README.md` 中的链接指向。
  - 更新 `README.md` 中的链接指向。

- 优化开发环境支持。
  - 在 .gitignore 中添加 Cursor IDE 相关文件的忽略规则。

- 优化部分 Configuration 类中的常量命名。
  - com.dwarfeng.tmpstg.configuration.SingletonConfiguration。

- 依赖升级。
  - 升级 `subgrade` 依赖版本为 `1.6.0.a` 以规避漏洞。

### Bug 修复

- (无)

### 功能移除

- (无)

---

## Release_1.0.6_20250601_build_A

### 功能构建

- 优化 `src/test` 下的部分示例的控制台输出文案。
  - com.dwarfeng.tmpstg.example.MultipleReadsExample。
  - com.dwarfeng.tmpstg.example.MultipleWritesExample。
  - com.dwarfeng.tmpstg.example.ProcessExample。

- Wiki 编写。
  - docs/wiki/zh_CN/QuickStart.md。

- 更新 README.md。

- Wiki 更新。
  - docs/wiki/zh_CN/Introduction.md。

- 依赖升级。
  - 升级 `subgrade` 依赖版本为 `1.5.10.a` 以规避漏洞。

### Bug 修复

- (无)

### 功能移除

- (无)

---

## Release_1.0.5_20250504_build_A

### 功能构建

- Wiki 编写。
  - docs/wiki/zh_CN/VersionBlacklist.md。

- 依赖升级。
  - 升级 `subgrade` 依赖版本为 `1.5.9.a` 以规避漏洞。

### Bug 修复

- (无)

### 功能移除

- (无)

---

## Release_1.0.4_20250324_build_A

### 功能构建

- 依赖升级。
  - 升级 `subgrade` 依赖版本为 `1.5.8.a` 以规避漏洞。

- 更新 README.md。

- Wiki 编写。
  - 构建 wiki 目录结构。
  - docs/wiki/en_US/Contents.md。
  - docs/wiki/en_US/Introduction.md。
  - docs/wiki/zh_CN/Contents.md。
  - docs/wiki/zh_CN/Introduction.md。

### Bug 修复

- (无)

### 功能移除

- (无)

---

## Release_1.0.3_20241117_build_A

### 功能构建

- 优化部分类中部分方法的行为分析行为。
  - com.dwarfeng.tmpstg.handler.TemporaryStorageHandlerImpl。

- 依赖升级。
  - 升级 `spring` 依赖版本为 `5.3.39` 以规避漏洞。
  - 升级 `subgrade` 依赖版本为 `1.5.7.a` 以规避漏洞。

### Bug 修复

- (无)

### 功能移除

- (无)

---

## Release_1.0.2_20240626_build_A

### 功能构建

- (无)

### Bug 修复

- 修复部分功能中的 bug。
  - 修复 TemporaryStorageHandlerImpl 获取的输出流，调用其 `write(int)` 方法时行为不正确的 bug。

### 功能移除

- (无)

---

## Release_1.0.1_20240516_build_A

### 功能构建

- 优化部分类的文档注释。
  - com.dwarfeng.tmpstg.handler.TemporaryStorageHandlerImpl。

- 增加实体字段。
  - com.dwarfeng.tmpstg.bean.dto.TemporaryStorageInfo.contentLength。

### Bug 修复

- (无)

### 功能移除

- (无)

---

## Release_1.0.0_20240515_build_A

### 功能构建

- 实现核心机制。
  - 定义并实现 com.dwarfeng.tmpstg.handler.TemporaryStorageHandler。
  - 编写示例代码。
  - 编写单元测试并通过。

- 项目结构建立，程序清理测试通过。

### Bug 修复

- (无)

### 功能移除

- (无)
