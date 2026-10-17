# 验证面清单

这份文档回答一个问题：**这个仓库里，什么东西是被证明过的，什么东西不是。**

它不是进度报告（那是 `进度.md`），也不是上手指引（那是 `交接.md`）。它只描述验证本身。

> 最后实测：2026-10-18，全部通过。

---

## 一条命令

```powershell
powershell -ExecutionPolicy Bypass -File tools\verify_all.ps1
```

```
compile + full test suite          PASS
patchouli book validation          PASS
book regeneration is clean         PASS
asset validation                   PASS
build the jars                     PASS
validate the built jars            PASS
All verifications passed.
```

`-SkipTests` / `-SkipBuild` 用于快速迭代。

---

## 六道关卡各自防什么

### 1. 编译 + 全量测试 — `gradlew test`

**规模**：78 个测试文件 / 476 个 `@Test`。

| 覆盖 | 典型失败 |
| --- | --- |
| 仪式引擎：法阵校验、材料分配、四层分发、献祭、时长 | 制作仪式不掉产物 |
| 数据契约：仪式 schema、`#c:` 标签到矿物词典的映射、献祭标签 | 两条仪式因错误的矿物词典名**永远无法完成** |
| 生产加载器解析真实 `rituals.json`（170/170） | 数据笔误只会在服务端启动时暴露 |
| 可达性：每个仪式/配方材料都必须有产出源 | **整个存储终端做不出来**（缺 `storage_controller` 配方） |
| 仪式 dummy 可达性 | 注册了却永远拿不到的隐形物品 |
| 成就树：父节点/图标/触发器/奖励/翻译键 | 两个成就的图标指向不存在的物品 |
| 手册页面：类型、键、覆盖、必填字段 | 缺 `item` 的 spotlight 页在游戏里是错误页 |
| locale 独立性扫描 | 依赖默认 locale 的标识符构造 |
| 存储/交易/JEI/网络/GUI 契约 | 各种 |

**环境矩阵**：

| 维度 | 取值 | 结果 |
| --- | --- | --- |
| locale | 默认 `zh`、`tr`、`de` | 全部通过（`-PtestLocale=tr`） |
| JDK | Java 8（toolchain 强制） | — |
| 字符集 | 主代码全部显式 UTF-8 | 无默认字符集依赖 |

### 2. 手册校验 — `verify_patchouli_book.py`

7 项：页面类型白名单、未解析语言键、仪式覆盖（170/170）与悬空引用、职业/等级/献祭标签、法阵 id、实体与物品 id、中英键对齐、**每种页面类型的必填字段**。

### 3. 手册再生成一致性 — `generate_patchouli_book.py --check`

索引改了但手册没重生成 → 失败。483 个文件 / 232 条目。

### 4. 资源校验 — `verify_assets.ps1`

20 项：JSON 合法性、模型父级、贴图、方块状态、item model、手册图片、语言键、配方语义、方块状态语义、**代码直接引用的贴图**。

规模：600 模型 / 95 方块状态 / 90 配方 / 398 贴图 / 3694 语言键 / 341 物品 / 77 方块。

### 5. 打包 — `gradlew build`

产出 `build/libs/occultism-1.0.0{,-dev,-sources}.jar`。

### 6. 产物校验 — `verify_build_jar.py`

| 检查 | 为什么构建不报 |
| --- | --- |
| 622 个 class 全为 major 52（Java 8） | 构建跑在 JDK 25 上，目标版本错会产出 1.12 加载不了的字节码 |
| release 与 dev 字节不同且含 srg 名 | 发布 dev jar 是静默失败：能启动，调用原版方法才崩 |
| 0 个测试类混入 | 构建不报 |
| 关键资源齐全、仪式表完整、每条仪式有显式时长 | 构建不报 |

产物：2684 条目 / 622 class / 509 个手册文件。

---

## 没被验证的部分

**这一节比上面所有内容都重要。**

| 未验证 | 为什么 | 后果 |
| --- | --- | --- |
| **jar 在真实游戏里跑过没有** | 需要接受 Minecraft EULA 启动专用服务端，或人工开客户端 | 静态全绿 ≠ 能玩。启动期崩溃、注册冲突、渲染问题都不会在这份清单里暴露 |
| **GUI 在屏幕上长什么样** | 无图形界面 | 排版溢出、文字截断、按钮错位无法发现 |
| **手册在 Patchouli 里翻得动吗** | 同上 | 结构正确 ≠ 渲染正确 |
| **法阵投影的视觉效果** | 同上 | — |
| **多人环境** | 未测试 | 服务端/客户端数据同步问题无法发现 |

冒烟脚本已就绪：`tools/run_server_smoke.ps1`。它**不会**替你写 `run/eula.txt`——接受 EULA 是你的决定。文件存在且为 `eula=true` 时，它会无头启动服务端、等待 `Done (...)!`、并断言 Forge/模组/仪式表加载成功。

---

## 内容层面仍待裁决

| 项 | 状态 |
| --- | --- |
| 4 个纯装饰方块（`bee_nest`/`bell`/`budding_amethyst`/`reinforced_deepslate`） | 已存在，是否移除以符合「不移植装饰类方块」待定 |
| `dark_golden_sacrificial_bowl` | 被仪式消耗但无任何来源 |
| `netherite_scrap_dust` | 被 `possess_hoglin` 消耗但无来源（1.16+ 材料残留） |

这三项都已写入 `RitualInputReachabilityTest` 的登记例外，**不是被忽略，是被记录下来等你决定**。

---

## 一条经验

本清单里每一项检查，都是因为某个真实缺陷曾经**从所有已有检查底下溜过去**才被加上的。加检查时一律做受控验证：故意破坏 → 确认它会失败并说出准确位置 → 还原 → 确认复绿。

不这么做的检查，很可能只是"看起来在工作"。这个项目里已经出现过两次：一次是扫描范围写错导致几乎没收集到输入，一次是掉落证据按整文件匹配导致把功能方块误判为有产出。**两次都是受控验证抓出来的，两次都表现为绿灯。**
