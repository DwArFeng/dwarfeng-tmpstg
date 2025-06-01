# ChangeLog

### Release_1.0.6_20250601_build_A

#### 功能构建

- 依赖升级。
  - 升级 `subgrade` 依赖版本为 `1.5.10.a` 以规避漏洞。

#### Bug修复

- (无)

#### 功能移除

- (无)

---

### Release_1.0.5_20250504_build_A

#### 功能构建

- Wiki 编写。
  - docs/wiki/zh_CN/VersionBlacklist.md。

- 依赖升级。
  - 升级 `subgrade` 依赖版本为 `1.5.9.a` 以规避漏洞。

#### Bug修复

- (无)

#### 功能移除

- (无)

---

### Release_1.0.4_20250324_build_A

#### 功能构建

- 依赖升级。
  - 升级 `subgrade` 依赖版本为 `1.5.8.a` 以规避漏洞。

- 更新 README.md。

- Wiki 编写。
  - 构建 wiki 目录结构。
  - docs/wiki/en_US/Contents.md。
  - docs/wiki/en_US/Introduction.md。
  - docs/wiki/zh_CN/Contents.md。
  - docs/wiki/zh_CN/Introduction.md。

#### Bug修复

- (无)

#### 功能移除

- (无)

---

### Release_1.0.3_20241117_build_A

#### 功能构建

- 优化部分类中部分方法的行为分析行为。
  - com.dwarfeng.tmpstg.handler.TemporaryStorageHandlerImpl。

- 依赖升级。
  - 升级 `spring` 依赖版本为 `5.3.39` 以规避漏洞。
  - 升级 `subgrade` 依赖版本为 `1.5.7.a` 以规避漏洞。

#### Bug修复

- (无)

#### 功能移除

- (无)

---

### Release_1.0.2_20240626_build_A

#### 功能构建

- (无)

#### Bug修复

- 修复部分功能中的 bug。
  - 修复 TemporaryStorageHandlerImpl 获取的输出流，调用其 `write(int)` 方法时行为不正确的 bug。

#### 功能移除

- (无)

---

### Release_1.0.1_20240516_build_A

#### 功能构建

- 优化部分类的文档注释。
  - com.dwarfeng.tmpstg.handler.TemporaryStorageHandlerImpl。

- 增加实体字段。
  - com.dwarfeng.tmpstg.bean.dto.TemporaryStorageInfo.contentLength。

#### Bug修复

- (无)

#### 功能移除

- (无)

---

### Release_1.0.0_20240515_build_A

#### 功能构建

- 实现核心机制。
  - 定义并实现 com.dwarfeng.tmpstg.handler.TemporaryStorageHandler。
  - 编写示例代码。
  - 编写单元测试并通过。

- 项目结构建立，程序清理测试通过。

#### Bug修复

- (无)

#### 功能移除

- (无)
