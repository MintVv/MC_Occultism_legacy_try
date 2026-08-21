# Occultism 1.12.2 移植进度总结

> 快照日期：2026-08-01（Asia/Shanghai）
> 详细报告：见 `PORTING_PROGRESS_1.12.2.md`（本轮新增 Wild Hunt 主从持久化、Soul Shard 客户端 share-tag，并更新全量测试基线）
> 上游基线：`_reference/occultism-upstream`，分支 `version/1.20.1`，提交 `84b8ad4`，Occultism `1.81.4`

## 一句话结论

当前为**可构建、核心玩法已有较完整本地实现、但上游等价性与真实世界回归尚未完成的功能性 Alpha**。

## 进度区间（三个独立口径，不可合并为单一百分比）

| 口径 | 进度 | 说明 |
| --- | --- | --- |
| 1.12 核心玩法循环 | **约 65%-75%** | 入门材料、第三眼、法阵、仪式、职业灵体、矿井主线均已存在 |
| 相对上游 1.81.4 的完整功能面 | **约 45%-55%** | 主要缺口：完整存储网络、专属高阶灵体、Modonomicon 运行时、配置体系、现代集成 |
| 已注册内容的静态资源闭合度 | **约 85%-90%** | 模型/贴图/语言/引用齐全，不代表行为等价或发布就绪 |

## 已完成（要点）

- **工程与构建**：Forge 1.12.2 全生命周期、注册、GUI、网络、世界生成、打包链路接通；`build` 成功产出 3 个 jar。
- **测试**：**37 个测试类、256 项 JUnit 全部通过**（0 失败、0 错误）。
- **仪式引擎**：20 个法阵、170 条仪式定义；召唤/附身/职业/升级/修复/复活/野性召唤分支齐全。
- **使魔（Familiar）**：上游 16 条直接 Familiar 仪式 `16/16` 落地；16 类本地 Familiar 有独立实体/AI/模型/仪式/辞典闭环；死亡→灵魂碎片→复活主循环、Blacksmith 12 类升级链、26 类 `FamiliarTrigger` 事件与 25 个隐藏进度、原生单槽 Familiar Ring capability 均已接入。
- **附身与野性实体**：固定上游 10 类专用附身实体全部接线（8 类原版基类兼容 + Hoglin/Warden 专用 1.12 兼容实现）；Wild Hunt 专属双实体、3 首领/3-8 随从、护盾/友军免疫和主从 UUID 持久化/已加载实体重连均已接入；Wild Afrit、Wild Parrot、Otherworld Bird 仪式落地。
- **存储网络（第一阶段）**：动态容量（128 + stabilizer 增量）、1024 超大堆叠、虚拟聚合/搜索/排序、有界压缩栈同步、3x3 终端合成、recipe lock、linked machine/order 最小路由、`GlobalBlockPos`/Proxy API、跨维度已加载访问。
- **手册**：自研 1.12 GUI 承载 9 类 / 203 条目 / 717 页；英中双语各 2,687 个键完全一致。
- **集成**：JEI 覆盖仪式/灵火/职业/矿井；CraftTweaker 增删接口可用。

## 实测基线修正（2026-08-01）

此前报告声明的测试数量已过时。早期实跑为 **235 项、228 通过、7 失败**，随后修复并继续增加契约覆盖；当前基线为：

- `compileJava`：**通过**。
- 全量 `test`：**37 个测试类、256 项，0 失败 / 0 错误 / 0 跳过**。已独立聚合 `build/test-results/test/*.xml` 复核，37 份 XML 汇总一致。
- `tools/verify_assets.ps1`：本机 PowerShell 执行策略为默认 Restricted，五个作用域均为 `Undefined`，**不加 `-ExecutionPolicy Bypass` 无法运行**。项目文档记录的标准命令本身带该 flag；是否授权使用需由维护者决定，否则每轮都会缺模型/贴图/lang/blockstate 覆盖。

### 已修复：7 项 FamiliarRing 契约断言过时

**根因**：`item/ItemFamiliarRing.java:554-566` 的 `canCaptureFamiliar` 已重构为通用 `IFamiliar` 归属检查，返回 `owner == null || owner == player`；而 7 个契约测试仍断言重构前的逐物种分支文本 `isTamed() && isOwner(player)`。

**判定依据**（已逐字核实上游）：`_reference/occultism-upstream/.../common/item/tool/FamiliarRingItem.java:134` 的 `interactLivingEntity` 判定为
`(familiar.getFamiliarOwner() == playerIn || familiar.getFamiliarOwner() == null)`
——即"未绑定可认领、已绑定仅主人可捕获"。本地实现与上游等价，**过时的是测试**。旧断言要求"未驯服不可捕获"，与上游语义相反。

**修复方式**：只改测试，不动生产代码。7 处逐物种断言改为固定完整通用契约的结构化正则（类型门控 → 取 owner → 精确返回式），并加 `assertFalse(capture.contains("isTamed()"))` 负向守卫防止回退；各处注释注明契约来源为上游 `FamiliarRingItem.interactLivingEntity`。四个方法已改名以名副其实（如 `familiarRingOnlyCapturesAnOwnedCthulhu` → `familiarRingCapturesUnboundOrOwnedCthulhu`）。各测试中仍然有效的 `isSupportedStoredFamiliar` 断言、Dragon 的 dismount→removePassengers→serialize→setDead 顺序检查等均原样保留。

**后续回归对照基线**：235 项测试全绿。此后出现任何失败即为回归。

## prototype 仪式复核：一次被推翻又被纠正的判断（重要教训）

**结论：原报告的 P1 判断是对的。这 6 条 prototype 仪式是可启动的真实内容，必须保留。**

本轮曾一度基于一份错误的研究结论把它们当作"死数据"删除，随后发现前提错误并已回滚。记录如下以防重犯：

**错误结论**（不成立）：6 条 prototype 没有 `pentacle` 字段 → 引擎无法启动 → 属死数据 → 应删除。

**证伪依据**：
- 字段名是 **`pentacle_id`** 而非 `pentacle`。6 条**全部**带 `pentacle_id`。
- 对应的 6 个法阵在 `ritual/PentacleDetector.java` 中**真实存在**且有正式名称：

  | 仪式 | pentacle_id | PentacleDetector 行 | 法阵名 |
  | --- | --- | ---: | --- |
  | possess_djinni_prototype | `occultism:possess_djinni` | 70 | Ihagan's Enthrallment |
  | possess_unbound_afrit_prototype | `occultism:possess_unbound_afrit` | 134 | Odus' Open Convocation |
  | possess_afrit_prototype | `occultism:possess_afrit` | 152 | Posuc's Convocation |
  | craft_afrit_prototype | `occultism:craft_afrit` | 170 | Sevira's Permanent Confinement |
  | contact_wild_spirit_prototype | `occultism:contact_wild_spirit` | 308 | Osorin's Unbound Calling |
  | contact_eldritch_spirit_prototype | `occultism:contact_eldritch_spirit` | 322 | Ronaza's Contact |

- 6 条都有完整的 `activation_item`、`ingredients`、`duration_ticks` 和**真实产出**：`lenses`、`burnt_otherstone`、`iesnium_nugget` x4、`iesnium_ingot`、`awakened_feather`、`taboo_book`。删除会让这些物品失去仪式产出路径——其中 `iesnium_ingot` 在 rituals.json 中仍被当作**原料消耗**，属进度链断裂。
- 所谓"canonical 替代已存在（21 条 / 4 条 / 13 条…）"的计数**不是仪式 id 计数**：本地根本不存在 id 为 `possess_djinni`、`possess_afrit`、`contact_wild_spirit` 的仪式。那些数字是 `pentacle_id` 引用次数等字符串出现次数。

**教训**：核对字段名要以实际 schema 为准（`pentacle_id`）；"某某已存在 N 条"这类计数必须落到具体 id 列表上验证，不能采信裸计数。原报告对这 6 条的描述——"仍以材料结果承接未完成的召唤、附身或接触语义"——是准确的：它们是**可玩的桥接实现**，真正的移植工作是把结果从材料替代升级为真实的召唤/附身/接触语义，而不是删除。

## 主要缺口

1. **6 条 prototype 桥接仪式**：法阵、材料、时长齐全且可启动，但结果仍是材料替代，未承接真实的召唤/附身/接触语义（`possess_djinni`、`possess_unbound_afrit`、`possess_afrit`、`craft_afrit`、`contact_wild_spirit`、`contact_eldritch_spirit` 六个法阵各对应一条）。这是真实待办，**不可删除**，详见上节。
   另一项仪式侧缺口是**消息协议未本地化**：上游每仪式 4 个翻译键 `ritual.occultism.<path>.{started|interrupted|finished|conditions}`，本地仅 7 条仪式有键，其余走硬编码英文 `display_name`；164/170 条可复用既有 `ritual_dummy` 名称键。
2. **高阶灵体**：Djinni/Afrit/Marid 仍为单一 `EntityBoundSpirit`/`EntityFoliotJob` 按 tier 复用，非三套专属实体/AI/库存/工作区。
3. **存储网络**：缺完整跨网络虚拟缓存、远程完整 capability 代理、JEI/配方书协同、stabilizer 方向属性。
4. **真实世界回归**：几乎所有新批次（Familiar 升级链、Wild Hunt 群战、10 类附身实体、碎片/复活、机器操作员）只有静态/Bootstrap/契约验证，无游戏内执行证据；专服仅验证到 EULA。
5. **视觉与音效**：未迁 GeckoLib geo/animation、专用 OGG、粒子定义；部分实体复用原版外观。
6. **P2 已知问题**：旧存档 Nautilus armor 8 个 ID 已由 missing-mapping 处理器明确 `ignore()`；仪式名称本地化已完成，仍需真实客户端逐条回归。
7. **工程发布**：无有效 Git 历史、JDK 路径硬编码、CI/CHANGELOG/JAR 元数据未整理。

## 可量化指标

| 指标 | 数值 |
| --- | ---: |
| 生产 Java 文件 / 行数 | 293 / 45,374 |
| 测试类 / 测试数 | 37 / 256（全部通过） |
| 资源文件 | 1,420 |
| 方块 / 物品注册 | 127 / 334 |
| 自定义实体 | 39 |
| 法阵 / 仪式定义 | 20 / 170 |
| 手册 | 9 类 / 203 条目 / 717 页 |
| 本地化键（英/中） | 各 2,687，完全一致 |

## 本轮（2026-07-26）安排

### 已发生并已回滚的误操作

首轮工作流基于错误研究结论删除了 6 条 prototype 仪式（170 → 164）并写了两个断言其缺席的契约测试。发现前提错误后已停止工作流并回滚：还原 6 条仪式、删除 `PrototypeRitualRetirementContractTest.java` 与 `ContactLineRetirementContractTest.java`。除 `rituals.json` 与这两个测试文件外未触及其他文件。

### 修正后的待办顺序

1. ~~**修复 7 项存量失败**（前置项）~~ —— **已完成**，235 项全绿，详见上节。
2. ~~**仪式消息本地化**~~ —— **已完成**，见下节。
3. ~~**旧存档 missing-mapping**~~ —— **已完成**，见下节。
4. **6 条 prototype 仪式升级为真实语义**（真正的移植工作，工作量最大）：把材料替代结果换成上游的召唤/附身/接触行为。已完成范围界定，见下节。

## 本轮已完成：仪式名称本地化

修掉了中文客户端"已启动仪式：Fairy Familiar"的中英混排。

- `RitualDefinition` append-only 新增可空 `displayNameKey` 字段、带该参数的构造器重载、`getDisplayNameKey()`/`hasDisplayNameKey()` 和 `createNameComponent()`；有键返回 `TextComponentTranslation`，无键回退 `TextComponentString(displayName)`，覆盖 CraftTweaker 和运行时注入的定义。
- `RitualRecipeManager.readRitual()` 按 `ritual.occultism.<id>.name` 生成键。消息发送点（`BlockGoldenSacrificialBowl` 7 处、`PlayerEventHandler` 1 处）改为传 `ITextComponent` 嵌套参数，由客户端按自身语言解析。
- 两个 lang 文件各新增 **170** 个 `.name` 键，总键数 2,687 → **2,939**；已复核：**键集合完全一致、无重复键、170 条仪式全覆盖**、en 值与 JSON `display_name` 逐条一致、zh 值无残留"仪式："前缀。既有的 7×4 上游式 `.started/.interrupted/.finished/.conditions` 键和 24 个 `ritual.occultism.sacrifice.*` 键均未被触碰。
- 中文值取自既有 164 个 ritual_dummy 名称（剥离"仪式："前缀）；6 条无 dummy 的桥接仪式为手工译名，**建议人工校对**：附身巨灵（原型）／附身未束缚火灵（原型）／附身火灵（原型）／火灵灌注（原型）／接触荒野魔灵（原型）／接触诡奇魔灵（原型）。
- **JEI 启发式已保护**：`RitualRecipeWrapper.java:264` 的 `getDisplayName().toLowerCase(Locale.ROOT)` + `contains("crusher")` 分类逻辑原样保留（已复核），标题改为有键用键、无键回退；契约测试固定"`display_name` 必须保持英文"，防止后人改成中文而悄悄弄坏分类。
- 新增 `RitualNameLocalizationContractTest`（10 项）。生成脚本存于 `tools/generate_ritual_name_keys.py`。
- **对上游的有意偏离**（已记入测试注释）：上游是每仪式 4 条整句键（`Ritual.java:135-158`），本地采用"通用句式 + 可翻译名字参数"，因为本地消息集更丰富（running / waiting_item_use / missing_ingredients / missing_sacrifice / item_used 上游没有）。

## 本轮已完成：旧存档 Nautilus armor missing-mapping

- 新增 `registry/ModMissingMappings.java`：监听 `RegistryEvent.MissingMappings<Item>`，对 8 个已退役注册名调用 `mapping.ignore()`，清单外条目保持 `Action.DEFAULT` 以便真正的意外缺失仍然可见。
- **8 个 ID 由存档注册表反查确定**（原报告只说"8 个"未给清单）：解压 `run/saves/New World-20260711-182812.zip` 的 `level.dat` 得 482 个 `occultism:` 注册名，与源码字面量精确比对得 8 个孤儿；更早的 `New World-20260622-024350.zip` 得完全相同结果。清单为 `copper/diamond/golden/iron_nautilus_armor` 及对应 4 个 `ritual_dummy/misc_*_nautilus_armor`（**注册名含斜杠**）。
- **处置为 `ignore` 而非 `remap`**，依据 `en_us.lang:1498` 记载的 Nautilus armor 有意不移植决定：无语义等价替代物，remap 会凭空替换玩家存档中的物品。已核实 `ignore()` 使条目在 `ForgeRegistry.processMissingEvent` 被静默丢弃，不进入触发确认弹窗和强制存档备份的 `defaulted` 集合。
- 新增 `MissingMappingsContractTest`（7 项），固定 8 名清单、斜杠形式、清单外与外部 namespace 不受影响（含仍在使用的 `nautilus_shell` 和 `ritual_dummy/misc_copper_horse_armor`）、以及"必须 ignore、不得 remap/warn/fail"。
- 我移除了该测试中对 `en_us.lang` 一句无关说明文字的断言：本地化批次正在并发改写同一文件，让注册表契约测试依赖手册散文会造成虚假失败。理由改由处理器 javadoc 承载。

## 6 条 prototype 仪式：范围界定（2026-07-26 实测）

### 对照上游：这 6 个法阵在上游没有"同名仪式"，只作为其他仪式的法阵被复用

上游 `src/main/resources/data/occultism/recipes/ritual/` 共 **67 个** ritual JSON。按 `pentacle_id` 引用统计：

| 法阵 | 上游引用它的仪式数 | 本地引用它的仪式数（含 prototype） |
| --- | ---: | ---: |
| `possess_djinni` | 15 | 22 |
| `possess_afrit` | 4 | 5 |
| `possess_unbound_afrit` | **0** | 3 |
| `craft_afrit` | 2 | 14 |
| `contact_wild_spirit` | **0** | 22 |
| `contact_eldritch_spirit` | **0** | 11 |

**结论**：上游并不存在 id 为 `possess_djinni`、`craft_afrit`、`contact_wild_spirit` 的仪式——这些只是**法阵名**，被 `familiar_bat`、`possess_warden`、`craft_stabilizer_tier3` 等具体仪式引用。本地这 6 条 `*_prototype` 是移植时自造的桥接条目，**上游没有对应物可逐条对齐**。三个法阵（`possess_unbound_afrit`、`contact_wild_spirit`、`contact_eldritch_spirit`）在上游 67 条 ritual 中零引用，属上游自身未使用的法阵定义。

### 产出物可达性：删除不会硬性断链

先前担心的"删除会切断 `iesnium_ingot` 产出链"经核实**不成立**，6 种产出都有其他获取途径：

| 产出 | prototype 外的获取途径 |
| --- | --- |
| `lenses` | 普通配方 `lenses.json`、`lenses_stained.json` |
| `iesnium_ingot` | 熔炼（Iesnium 矿石 / 生 Iesnium / Iesnium 粉 / 下界合金粉）+ 配方 `iesnium_ingot_from_block/nuggets` |
| `iesnium_nugget` | 配方 `iesnium_nugget_from_ingot` |
| `burnt_otherstone` | 熔炼抛光异界石（`ModRecipes.java:31`） |
| `awakened_feather` | Spirit Fire（`SpiritFireRecipeRegistry.java:77`，羽毛→觉醒羽毛） |
| `taboo_book` | Spirit Fire（`SpiritFireRecipeRegistry.java:79`，书→禁忌之书） |

**但这不改变"不该删"的结论**：它们是可启动、能正常完成、有独立产出配方的功能性内容，删除等于无故移除 6 条玩家可用的仪式路径。之前的删除仍是错误操作。

### 建议处置（供维护者决策，非本轮执行）

上游无对应物 ⇒ 没有"移植到等价语义"这个选项。三条路：

1. **保留为已记录的本地设计决定**（推荐、成本最低）：补上 ritual dummy、辞典页和双语文本，明确标注为本地新增的桥接内容，并在报告中固定该偏离。当前它们缺 dummy / lang / 辞典页，是"半成品"而非"错误"。
2. 按上游精神重新设计为真实的召唤/附身/接触行为——但这是**新增设计**而非移植，需先明确移植目标（"1.12 可玩版"还是"1.81.4 等价版"）。
3. 删除——只有在明确选择"1.81.4 等价版"、且接受移除玩家可用内容时才成立。

在维护者未选定移植目标前，**不应对这 6 条做任何破坏性改动**。

回归判据：235 项测试中，除已知 7 项外不得出现新失败；第 1 项完成后目标为全绿。**任何"删除内容"类改动都必须先给出该内容不可达的直接证据（字段名以实际 schema 为准），再动手。**
