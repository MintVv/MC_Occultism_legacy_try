# Occultism 1.12.2 移植进度总结

> 快照日期：2026-10-16（Asia/Shanghai）
> 详细报告：见 `PORTING_PROGRESS_1.12.2.md`；最近批次为仪式注册表与脚本清空修复，仅做定向源码测试，旧客户端烟测不代表最新改动已完成游戏回归。
> 上游基线：`_reference/occultism-upstream`，分支 `version/1.20.1`，提交 `84b8ad4`，Occultism `1.81.4`

## 一句话结论

当前为**可构建、核心玩法已有较完整本地实现、但上游等价性与真实世界回归尚未完成的功能性 Alpha**。

## 仪式注册表追加

- 注册表以 `null` 区分未加载与合法空表。最后一条配方删除、整表清空及加载失败后，查询不再恢复默认仪式或不断重试/输出错误。
- 普通初始化改为惰性查询，保留已加载/脚本修改的表；明确的 Java `load()` 仍替换整表。新增 `mods.occultism.Ritual.removeAll()`，复用现有 CraftTweaker action 机制。
- 保留不可修改快照和整表发布/失败清空策略。生命周期测试现在通过真实删除接口检查最后一条仪式消失，并覆盖清空后从 NBT 恢复的献祭等待仪式中断、只返还一次启动物。
- Java 编译与四个定向类共 **39项通过**（注册表9、生命周期13、献祭13、仪式数据4），0失败/错误/跳过。测试捕获日志，防止默认资源加载失败掩盖重复重载。
- 未使用 computer、未启动游戏/专服、未运行全量。ZenScript 启动、同 ID 运行中替换、客户端同步和 GUI 刷新未验证；详见 `RITUAL_REGISTRY_1.12.md`。

## 仪式材料分配追加

- 固定上游也有通用候选抢占精确材料的贪心局限，本批明确修正而非宣称逐行等价。现有170条单件追加成本采用有界 Forge `RecipeMatcher`；保留本地一堆多件材料的兼容语义。
- 启动检查、剩余检查和逐项扣料统一使用分配计划；实际消费按计划定位物理碗，保存后按已消费前缀重新分配。输入堆巨大时不全量展开。
- 多件或超过32项的脚本成本保留旧首次匹配语义，但模拟与实际消费使用相同计划；其全局重叠分配仍有局限。非法零/负追加成本不能通过计划。
- Java 编译与六个定向类共 **56项通过**（匹配7、生命周期12、献祭13、本地化11、视觉3、附身10），包括432个小型重叠场景及材料碗消费/存档后完成。
- 未使用 computer、未启动游戏/专服、未运行全量。真实并发与性能回归仍未完成；详见 `RITUAL_MATERIAL_MATCHING_1.12.md`。

## 仪式持续有效性追加

- 对照 1.81.4 `GoldenSacrificialBowlBlockEntity.tick` / `Ritual.isValid`，本地在等待献祭/物品使用之前持续校验选定法阵、启动物及尚未消费的材料；不再只在启动时检查。
- 条件失效或配方定义缺失会中断并返还当前启动物，不掉结果、不重新创造已取走的启动物、不退已消费材料；未加载的法阵必要格不强制读取，而是判为无效。
- 新增按消费顺序跳过前缀的剩余材料校验；修复加速到少于材料项数的时长时，结果生成前未消费全部成本的问题。
- 补齐动态仪式通用 `finished/interrupted/conditions` 双语键。Patchouli 概览解释等待期间仍需保持法阵和材料完整。
- Java 编译与五个定向类共 **48 项通过**（生命周期11、献祭13、本地化11、视觉3、附身/召唤10）。共享内存 World 桩放置真实本地法阵图案、捕获掉落，并拒绝访问未加载格。
- 未用 computer、未启动游戏/专服、未跑全量。后续材料分配批次已修正标准单件成本的贪心局限，计数脚本后备与真实区块/多人/脚本恢复仍需验证；见本页最新段落。

## 玩家击杀献祭追加

- 对照 1.81.4 `GoldenSacrificialBowlBlockEntity.onLivingDeath` 和 `Ritual.isValidSacrifice`，删除本地金碗自动寻找、攻击和强制删除祭品的路径。
- `RitualSacrificeHandler` 监听服务端玩家归因击杀，包括玩家箭矢；用死亡脚下方块与金碗的三维距离平方 `<=64` 检查。任何玩家的击杀都可满足范围内多个活动仪式。
- 在 WorldTick.END 确认同一事件未取消、受害者未获救、金碗与运行代次未变化，才写入既有 `SacrificeProvided`；卸载世界清理队列，不强制加载区块。确认可比上游即时通知晚一个 tick，队列不跨存档保留。
- `humans` 对齐村民/玩家，不再接受女巫。仅通过真实死亡通知匹配，不会自动伤害法阵附近玩家。帕秋莉献祭正文、双语等待提示和帕秋莉/JEI 名称翻译同步更新。
- Java 编译通过，五个定向类共 **47 项通过**（献祭事件 13、视觉 3、Wild Hunt 11、附身/召唤 10、本地化 10）。书籍生成检查通过；未用 computer、未启动游戏/专服、未跑全量。
- 剩余验证和同 tick 保存/卸载边界见 `RITUAL_SACRIFICE_1.12.md`；下方旧报告的“自动击杀献祭”描述已由本批替代。

## 天气与时间职业追加

- 对照固定 1.81.4 的 `ChangeWeatherJob` / `ChangeTimeJob` 及其五个子类，新增本地 `WorldEffectJob`。金碗完成改为召唤本地 Djinni/Afrit 职业实体，不再直接修改世界状态。
- 晴天/降雨/雷暴分别施法 300/600/1200 tick 后生效，天气计时使用上游 6000 tick；昼夜在 100 tick 中按上游公式插值到下一个黎明/黄昏。施法目标随 NBT 保留，完成后不重复执行。
- 五条仪式补齐阶级、有限寿命、展示实体，召唤时长统一为上游 1200 tick；新增 Forge `rituals` 五项启用开关及双语禁用消息。正常职业完成消散，不掉可重复触发的魂片。
- 世界效果由已有职业实体承载，复用 Spawn hook、模型和神灯 NBT。完成时 1.12 以附魔台音替代高版本信标启动音；不添加上游未加入世界的雷电实体，不新增装饰方块。
- 本批 Java 编译通过，四个定向类 **36 项通过**（天气/时间 12、仪式数据 4、高阶职业 10、附身/召唤契约 10）。Patchouli 天气/时间正文和 tooltip 已同步；书籍仍为每语言 9 类、203 条目。
- 这些证据覆盖 WorldInfo 和无世界实体状态、数据及源码契约，不覆盖实际仪式全流程、客户端渲染、跨维度或神灯操作。未用 computer、未启动客户端、未执行全量测试。
- 细节与剩余边界：`WEATHER_TIME_PORT_1.12.md`。下方历史段落中“天气/时间直接效果”的描述已被本批替代。

## 2026-10-14 帕秋莉说明书接入

- 使用官方现有 1.12 版 Patchouli `1.0-23.6`，依赖坐标 `vazkii.patchouli:Patchouli:1.0-23.6`；开发运行时自动加载，发布时独立安装，不把其类打入 Occultism。不是重新移植高版本 Patchouli。
- 原灵魂辞典物品通过服务端 API 打开 Patchouli 书 `occultism:dictionary_of_spirits`；缺少 Patchouli 时保留旧入口。潜行与法阵调试权限保持原逻辑。
- 生成英中双语各 9 类、203 条目，共 2065 页，425 个 JSON（含 book.json）；转换文字标记、条目/锚点、物品 metadata、实体、图片、合成和熔炼引用。分类链接转为普通强调文字，因为固定版本的文本链接只支持条目。
- 新增仪式、灵火页面：仪式图沿用上游祭祀碗布局并适配 Patchouli 页宽，材料/产物来自运行期注册表；法阵标题可跳转。多方块直接取本地检测器图案，转置 Patchouli 的 X/Z 索引，支持粉笔替代、野性火灵特殊符号和骷髅 tile 类型预览/检查。
- 补齐功能配方 `storage_stabilizer_tier0` / `_dark`：按 26.1.2 参考配方从现有 otherstone/otherrock pedestal 转换；修正镜框替代配方引用。未新增装饰类方块。
- **当前验证**：Java 编译通过；最新定向 `PatchouliBookPortTest` 为 4/4 通过，覆盖双语清单、物品/图片/配方/法阵引用、链接锚点和两张稳定器配方；资源审计通过。用户新增限制前的一次全量为 352 项、351 通过、1 项新书引用失败；随后仅定向修复并验证，不能将最终状态记为全量全绿。
- 客户端日志确认 Patchouli 与其他 9 个 Mod、170 条仪式、JEI 加载成功。没有实际开书、页面截图或世界内投影验证；开发客户端已关闭，`runClient` 的 `-1` 来自主动终止。
- **仍未完成**：长文分页只采用保守宽度估算，英中内容仍含历史上游差异及部分未译文本，须继续核对；交易页已接入动态配方查询和多成本绘制，但尚无真实页面视觉回归，也不宣称整模组移植完成。
- 工作方式更新：不使用 computer，不运行全量测试；以代码为主，累计修改后只执行必要的定向检查。最新入口见 `进度.md`；下方旧日期段落与指标保留为历史记录。

## 2026-10-14 交易职业与说明书配方页对齐

- 新增 `spirit/SpiritTradeRecipeRegistry`，集中保存 `stone_to_otherstone`、`stone_to_otherrock`、`otherworld_sapling` 的输入、输出、上游处理时间和每轮交易次数。
- `EntityFoliotJob` 改用共享定义：异界石/异界岩每 20 tick 推进一步、共 15 步（300 tick），每轮最多 4 次；异界树苗共 20 步（400 tick），每轮 1 次。交易只在完整输入上消耗，并支持已注册的 `stone` OreDictionary 输入。
- 旧版自研辞典交易引用也改为共享注册表；Patchouli 新增 `occultism:spirit_trade` 自定义页，按上游 renderer 绘制输入框、抛物箭头、输出框和计时信息。生成器已从静态 spotlight 改为该页。
- 定向验证：`SpiritTradePortContractTest` 7 项、`HighTierJobSpiritPortContractTest` 10 项、`PatchouliBookPortTest` 4 项全部通过；`compileJava` 通过，生成器 `--check` 通过，未运行全量。
- 存档兼容：旧 `WorkTimer` 按交易定义折算为完整秒并限制在未完成步骤，避免旧逐 tick 计数直接跳过新计时。
- 当前书籍页数更新为英中合计 2065 页（交易页合并减少 6 页），仍为每语言 9 类 / 203 条目。
- 边界：共享表为三种固定单输入交易；尚未迁移上游任意多输入配方、交易 ID/NBT 配置或脚本增删。异界岩为本地后续版本路线，七种原版石头变种仍沿用旧本地输入；新增 OreDictionary 对应上游 stone tag。没有世界/GUI 实测证据，不标记完整 TraderJob 等价。

## 2026-10-14 交易 ID / NBT / 脚本接口追加

- 交易者新增 `setTradeRecipeId`、`setTimeToConvert`、`setMaxTradesPerRound`，恢复上游同名存档字段；旧 `WorkTimer` 仍兼容，秒数转 tick 受溢出边界保护。
- 注册表支持短名与完整资源 ID、按 ID 替换/移除及防御性副本；错误定义在修改前拒绝，指定缺失/无效 ID 时停止处理并保留输入，不隐式选另一条配方。
- 新增 `mods.occultism.SpiritTrade.addRecipe/addOreRecipe/removeRecipe`。数组表示单个输入的候选项，不是多成本列表；字段与示例见 `SPIRIT_TRADE_1.12.md`。
- Patchouli 交易页查询当前配方；默认方块物品延迟解析，修复测试发现的早期类初始化空物品问题。
- 本批只运行三个定向测试类，**26 项通过**（交易 12、说明书 4、高阶职业 10），编译通过。没有使用 computer、启动客户端或执行全量。
- 未完成范围仍包括任意多输入交易、原生数据包与客户端配方同步；脚本入口尚未实跑 ZenScript，世界/GUI 回归仍未完成。下方旧批次“不支持 ID/NBT/脚本”的描述为历史状态。

## 2026-10-16 多原料 SpiritTrade 对齐

- `SpiritTradeIngredient` 和 `SpiritTradeRecipeRegistry.registerIngredients` 已支持最多 9 项无序成本；每项可包含普通物品候选或动态 OreDictionary 条目，metadata wildcard 和带 NBT 候选按 1.12 语义匹配。
- `Recipe.isValid(List<ItemStack>)` 使用有界 Forge `RecipeMatcher` 做成本分配；`consume` 深拷贝并只扣除完整成本，输入不足或重叠候选无解时原数量保持不变。大堆栈不会按 count 展开。
- `EntityFoliotJob` 会按成本数量限制拾取，能补入不足一轮的同类余料；不同物品不会覆盖手中材料。单主手堆仍保留上游 TraderJob 执行边界，多物品类型配方只在多堆 API 中完整消费。
- Patchouli 自定义交易页和旧辞典交易引用会显示所有成本；单成本仍使用上游交易框布局，多成本使用最多 3x3 成本槽。新增 CraftTweaker `addShapelessRecipe`。
- 定向验证：`SpiritTradeIngredientsTest` 12、`SpiritTradePortContractTest` 12、`PatchouliBookPortTest` 4、`HighTierJobSpiritPortContractTest` 10，共 38 项通过；未使用 computer、未运行全量、未做客户端页面实测。
- 尚未完成：原生数据包交易配方、客户端自动同步、自定义交易设置 GUI、混合物品收集 AI 和世界内交易回归。旧段落中关于“多输入尚未实现”的内容属于历史状态，当前边界以本段为准。

## 2026-10-16 工程可移植性

- 移除 `gradle.properties` 中指向 `F:/MC_MOC_PJ/OCCULTISM/.jdks/jdk-25.0.3` 的绝对 `org.gradle.java.home`。`gradlew-local.bat` 仍通过 `JAVA_HOME` 选择项目 JDK，普通 wrapper 和 CI 不再被本机路径劫持。
- 该构建元数据修复未单独启动客户端；后续集中验证已重新编译 Java，并运行存储稳定器定向契约。

## 2026-10-16 5 阶存储稳定器修复

- 发现 tier5/tier5_dark 已有注册、配方、仪式和说明书，但 `TileEntityStorageController.getSlotsForStabilizerBlock` 未处理它们，实际容量贡献为 0。
- 新增 `stabilizerTier5Slots` Forge 配置，默认 1024，并将明暗两种 tier5 稳定器接入容量计算；保持 1.12 的六向定向扫描和动态配置边界。
- 新增定向契约，固定两种 tier5 方块必须返回配置容量；`StorageStabilizerPortContractTest` 8 项通过，`compileJava` 通过。未启动客户端、未运行全量。

## 2026-10-16 仪式消息协议对齐

- 依据上游 `Ritual.java` 的四类消息协议，为全部 170 条本地仪式补齐中英文 `started/interrupted/finished/conditions` 键，共 680 条/语言。
- `RitualDefinition` 新增四个协议组件；金碗开始/条件失败、祭祀碗完成/中断均使用 per-ritual 组件，额外的 `running`、献祭等待、物品使用和调试消息保持 1.12 本地扩展。
- 无本地 `display_name_key` 的 CraftTweaker/运行时定义回退通用键，避免显示原始翻译键。
- `RitualNameLocalizationContractTest` 与 `PortedResourceContractTest` 定向通过；Java 编译通过，未启动客户端、未运行全量。

## 进度区间（三个独立口径，不可合并为单一百分比）

| 口径 | 进度 | 说明 |
| --- | --- | --- |
| 1.12 核心玩法循环 | **约 65%-75%** | 入门材料、第三眼、法阵、仪式、职业灵体、矿井主线均已存在 |
| 相对上游 1.81.4 的完整功能面 | **约 45%-55%** | 主要缺口：完整存储网络、专属高阶灵体、Modonomicon 运行时、配置体系、现代集成 |
| 已注册内容的静态资源闭合度 | **约 85%-90%** | 模型/贴图/语言/引用齐全，不代表行为等价或发布就绪 |

## 2026-08-31 启动回归修复

- 修复 `EntityDjinniJob`、`EntityAfritJob`、`EntityMaridJob` 缺少公开 `World` 构造器导致的 Forge 预初始化崩溃。
- 将 Otherworld Bird 野性仪式中的 `minecraft:cobweb` 改为 1.12 的 `minecraft:web`，仪式加载从异常回退恢复为完整读取 170 条定义。
- 全量测试当前为 58 个测试类、345 项，0 失败、0 错误、0 跳过；新增高阶灵体渲染器契约。
- `runClient` 已完成当前源码的 FML 加载和 JEI 初始化，日志确认 9 个 Mod 成功加载；进程是在烟测完成后主动停止，Gradle 的退出码 `-1` 仅表示测试进程被停止。

## 已完成（要点）

- **工程与构建**：Forge 1.12.2 全生命周期、注册、GUI、网络、世界生成、打包链路接通；`build` 成功产出 3 个 jar。
- **测试**：**58 个测试类、345 项 JUnit 全部通过**（0 失败、0 错误）。
- **仪式引擎**：20 个法阵、170 条仪式定义；召唤/附身/职业/升级/修复/复活/野性召唤分支齐全。
- **使魔（Familiar）**：上游 16 条直接 Familiar 仪式 `16/16` 落地；16 类本地 Familiar 有独立实体/AI/模型/仪式/辞典闭环；死亡→灵魂碎片→复活主循环、Blacksmith 12 类升级链、26 类 `FamiliarTrigger` 事件与 25 个隐藏进度、原生单槽 Familiar Ring capability 均已接入。
- **附身与野性实体**：固定上游 10 类专用附身实体全部接线（8 类原版基类兼容 + Hoglin/Warden 专用 1.12 兼容实现）；Wild Hunt 专属双实体、3 首领/3-8 随从、护盾/友军免疫和主从 UUID 持久化/已加载实体重连均已接入；Wild Afrit、Wild Parrot、Otherworld Bird 仪式落地。
- **存储网络（第一阶段）**：动态容量（128 + stabilizer 增量）、1024 超大堆叠、虚拟聚合/搜索/排序、有界压缩栈同步、3x3 终端合成、recipe lock、linked machine/order 最小路由、`GlobalBlockPos`/Proxy API、跨维度已加载访问。
- **手册**：已接入 Patchouli 1.12，中英各 9 类 / 203 条目，共 2065 页；自研 GUI 保留为未安装依赖时的后备。文本和布局的完整回归尚未完成。
- **集成**：JEI 覆盖仪式/灵火/职业/矿井；CraftTweaker 增删接口可用。

## 2026-10-13 仪式页面与装饰方块范围修正

- 仪式辞典引用页不再使用旧的文字列表布局；有效 `occultism:ritual_recipe` 会进入专用仪式绘制分支，按上游 `BookRitualRecipePageRenderer` 的结构显示法阵名称、仪式 dummy、献祭条件、周围祭祀碗和材料、中央金祭祀碗与启动物、产物以及使用物品/召唤结果。
- 这是对上游 renderer 的 1.12 绘制逻辑移植，不是直接运行高版本 Modonomicon 类：1.12 工程仍由本地 `GuiDictionaryOfSpirits` 承载页面，因为项目没有高版本 Modonomicon 的运行时/API。
- 修复了有效仪式引用创建时未携带 `RitualDefinition` 的分支错误；此前即使专用绘制代码存在，运行时仍会回退到普通参考页。
- 法阵标题改为优先使用上游 `multiblock.occultism.*` 本地化键，并支持点击标题跳转到法阵条目；仪式页的 12 个祭祀碗、中心金碗、启动物、产物、祭品和文本坐标继续与上游 renderer 对齐。
- 明确不移植装饰类方块：异界木板门、墙、按钮、台阶、楼梯、栅栏、压力板、活板门，以及 chiseled/cracked otherstone/otherrock bricks；同步删除注册、模型、方块状态、配方、语言和无引用纹理。
- 本轮验证：`compileJava` 通过；全量测试通过；`tools/verify_assets.ps1` 通过；`runClient` 成功加载 1.12.2 Forge、9 个 Mod、170 条仪式和 JEI。尚未以自动截图完成辞典页面逐像素回归，需在游戏中实际打开仪式条目检查坐标与字体缩放。

## 2026-10-13 仪式运行视觉效果补齐

- 对照上游 `GoldenSacrificialBowlBlockEntity`、`RitualWaitingParticle` 和 `SacrificialBowlRenderer`，补齐 1.12 仪式的主要视觉状态：等待献祭/物品使用时的灰色粒子、条件满足后的紫色 `PORTAL` 粒子、祭祀碗中物品的浮空旋转，以及完成时金碗中心的一次 `SMOKE_LARGE` 烟雾。
- 仪式现在可以先启动再等待献祭条件；等待状态会同步到客户端，金碗右击提示也会区分“等待献祭”和“等待物品使用”，不再直接把等待状态显示成运行中。
- 新增 1.12 客户端 `ParticleRitualWaiting`，按上游独立实现 40-49 tick 生命周期、上升曲线、渐变尺寸、增强光照和随机灰度；等待粒子按上游 `1/16` 随机节拍生成，删除重复的服务端 `SPELL_MOB` 流。
- 正常运行粒子改为服务端按上游 `1/16` 随机节拍在金碗周围生成 5 个 `PORTAL` 粒子；删除此前自创的材料碗 marker 粒子和客户端重复的运行粒子。所有金碗变体均显示相同仪式效果。
- 浮空物品补齐上游水晶特殊缩放、放入时相位和系统时间旋转；没有新增法阵光束或装饰方块渲染，因为当前参考上游本身没有独立的法阵光束 renderer。
- 新增 `RitualVisualPortContractTest`，固定上述粒子来源、随机节拍、浮空物品动画和等待粒子轨迹契约。
- 本轮验证：`compileJava` 通过；全量测试通过；`tools/verify_assets.ps1` 通过；`git diff --check` 通过。

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
3. **存储网络**：缺完整跨网络虚拟缓存、异步网络缓存、跨维度/区块加载代理、JEI/配方书协同；1.12 的定向 stabilizer、loaded-only capability 代理和第一阶段终端合成已有本地实现。
4. **真实世界回归**：几乎所有新批次（Familiar 升级链、Wild Hunt 群战、10 类附身实体、碎片/复活、机器操作员）只有静态/Bootstrap/契约验证，无游戏内执行证据；专服仅验证到 EULA。
5. **视觉与音效**：未迁 GeckoLib geo/animation、专用 OGG、粒子定义；部分实体复用原版外观。
6. **P2 已知问题**：旧存档 Nautilus armor 8 个 ID 已由 missing-mapping 处理器明确 `ignore()`；仪式名称本地化已完成，仍需真实客户端逐条回归。
7. **工程发布**：无有效 Git 历史、CI/CHANGELOG/JAR 元数据未整理；JDK 绝对路径已在 2026-10-16 移除。

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
| 本地化键（英/中） | 各 2,939，完全一致 |

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
