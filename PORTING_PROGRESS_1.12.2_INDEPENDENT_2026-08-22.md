# Occultism 1.12.2 独立移植进度

审计开始：2026-08-22  
最后更新：2026-08-23  
目标环境：Minecraft 1.12.2 / Forge / CleanroomMC 兼容构建  
工作基线：`17116dc`

## 审计原则

本记录从当前 1.12.2 工作区的 Java 源码、资源、构建配置、测试、Forge 1.12.2 源码和 `_reference` 上游 Git 对象重新整理，没有读取或修改仓库原有的两个移植进度 Markdown。

进度只按以下证据确认：

- 存在实际注册或生命周期入口，而不只是类或资源文件。
- 代码能够由当前 1.12.2 工具链编译。
- 行为有合约测试、资源校验或运行时观察支撑。
- 上游行为需要对照时，以 `_reference/occultism-upstream` 中可读取的 1.15 提交 `190f3d4` 为依据。

## 当前结论

当前工作区已经具备可编译的 1.12.2 核心实现，注册、仪式、灵火、灵魂与熟悉环、存储控制器、矿井与战场、容器、实体、渲染及兼容入口均已有较大覆盖。本轮验证状态为：

- 主源码：329 个 Java 文件。
- 测试源码：54 个 Java 文件。
- 主资源：1432 个文件。
- JUnit：55 个测试套件、334 个测试，失败 0、错误 0、跳过 0。
- 资源：697 个模型、136 个方块状态、108 个配方、1 个仪式资源通过校验。

这说明项目当前处于可构建、静态资源一致、主要合约测试通过的阶段，但不能据此宣称完整功能等价或发布就绪。尚未完成的真实客户端操作和专用服务器世界生成烟测仍是主要风险。

## 本轮完成：地下异界树丛

审计确认：本地已有天然异界树、树叶、树苗、花和天然异界石，但此前没有地下异界树丛的自然生成入口。现已完成：

1. 新增 `WorldGenOtherworldGrove`，使用 1.12.2 `WorldGenerator` API。
2. 从 `ModWorldGenerator` 的主世界分支以 `1/400` 的保守概率触发。
3. 生成中心固定在请求区块内部，Y 限制为 `25..60`，拒绝天空可见位置。
4. 生成有限椭球洞室，外壳使用 `OTHERSTONE_NATURAL`，内部准备草地平台。
5. 复用 `WorldGenOtherworldTree(true, true)` 生成天然树，并加入低概率天然异界花和荧石。
6. 预检并保护液体、基岩、TileEntity 和非白名单方块，同时允许普通石质地层及矿脉参与替换。
7. 树生成失败时恢复洞室快照，避免留下失败洞穴。
8. 新增 `OtherworldGrovePortContractTest`，覆盖维度、地下限制、自然方块、保护规则、恢复快照和区块内触发点。

## 本轮完成：呼唤之书普通 GUI

对照上游 1.15 `BookOfCallingGui` 后，移除了此前偏离上游的自定义面板和确认按钮，恢复真实交互：

1. GUI 高度为 166，模式按钮为 `150 x 20`，位于 `guiTop + 60`。
2. 只有带尺寸的模式显示工作范围按钮，位置为 `guiTop + 85`。
3. 退出按钮为底部 `20 x 20` 的 `X`，位置随工作范围控件是否显示而变化。
4. 模式和工作范围点击后立即发包并重建控件，不再等待确认。
5. GUI 不暂停游戏，不显示上游不存在的 Done/Confirm 操作。
6. 使用空槽 `GuiContainer` 适配 1.12 Forge 远程窗口生命周期，使 Esc、X、死亡和传送门等关闭路径能正确关闭服务端容器。
7. `ContainerBookOfCalling` 绑定打开时的背包槽位；服务端只接受当前打开该容器、槽位匹配、物品仍为呼唤之书且职业支持该模式的更新。
8. 客户端和服务端 GUI 入口都增加槽位边界检查，服务端更新后同步玩家库存容器。

## 本轮完成：协议与旧数据兼容

上游模式稳定值为 `deposit=0`、`extract=1`、`base=2`、`storage_controller=3`、`managed_machine=4`；工作范围稳定值为 `16/32/64`。本轮完成：

1. 新增独立的即时模式消息和工作范围消息，并在 `ModNetworking` 注册。
2. 新消息使用上游稳定整数值，不再使用本地 enum `ordinal()`。
3. 非法稳定值在解码后返回空值并被服务端拒绝。
4. 保留旧的组合配置消息作为本地旧协议兼容入口，其 ordinal 语义不变。
5. 模式 NBT 继续保留本地旧声明顺序，避免将已有 1.12 数据中的 `0/1/2` 无版本标记地误解释为上游模式。
6. 工作范围 NBT 写入统一改为 `16/32/64`，读取时先识别稳定值，再兼容旧 `0/1/2` ordinal。
7. 呼唤之书和 `EntityFoliotJob` 的工作范围持久化均使用同一兼容规则。

## 本轮完成：管理机器专用 GUI

继续沿呼唤之书功能链审计时，确认本地已有 `MachineReference` 和 `MessageSetManagedMachine`，但缺少上游 `BookOfCallingManagedMachineGui` 及实际客户端打开入口。现已补齐：

1. 新增 1.12 专用屏幕，保持上游 166 高布局、150 宽按钮、5 像素间距和底部 `X`。
2. 输入面和输出面按钮按六个 `EnumFacing` 循环，点击后立即发送更新。
3. 自定义名称长度限制为 30；仅在关闭时、名称非空且确有变化时提交。
4. 仅在 `SET_MANAGED_MACHINE` 模式、目标任意侧具备物品能力且书内已有严格机器引用时打开屏幕。
5. 服务端消息仍只采纳输入面、输出面和自定义名；机器坐标、维度和注册名始终保留服务端值。
6. 移除会在刚切换机器时错误拒绝合法编辑的客户端端点一致性要求。
7. 重复设置同一台机器时保留已有朝向、自定义名和独立输出端点；真正更换机器时才初始化新引用。
8. 补齐英文和中文的六个朝向键及三个管理机器 GUI 字段键，两份语言文件均无重复键。

## 本轮完成：高阶灵体独立实体

对照上游 `DjinniEntity`、`AfritEntity` 和 `MaridEntity` 后，修正了本地所有高阶灵体都退化为通用 `EntityBoundSpirit` 的问题：

1. 新增 `EntityDjinni`、`EntityAfrit` 和 `EntityMarid`，分别固定自身 tier，避免读档或运行时更新时被改成其他 tier。
2. 按上游补齐生命值、移动速度、攻击力、护甲、护甲韧性和跟随范围属性。
3. `EntityBoundSpirit.createForTier` 在 Djinni/Afrit/Marid 时返回专用实体，Ancient 等未专门实现的 tier 继续回退到旧通用实体。
4. 仪式召唤和调试召唤统一使用该工厂；旧 `bound_spirit` 注册保留，三个新注册追加在现有实体注册末尾，避免改变旧实体 ID 顺序。
5. 三个专用实体复用 1.12 现有灵体 renderer，并加入专门的 renderer 注册入口。
6. 新增 `HighTierSpiritEntityPortContractTest`，覆盖实体工厂、属性、固定 tier、召唤入口和注册顺序。

## 本轮完成：高阶职业灵体独立实体

继续对照上游高阶灵体与 `FoliotEntity` 的职业差异后，本地高阶 Crusher、Smelter、Crystallizer 等职业灵体也不再全部实例化为同一个 `EntityFoliotJob`：

1. 新增 `EntityDjinniJob`、`EntityAfritJob` 和 `EntityMaridJob`，固定各自 tier，并沿用现有职业库存、过滤器、工作区、机器管理和灵魂碎片逻辑。
2. 高阶职业灵体使用上游属性；Djinni Crusher 在 1.12 中动态使用 `1.2 x 2.4` 的放大碰撞体型，切换到其他职业时恢复普通体型。
3. `EntityFoliotJob.createForTier` 统一选择专用职业实体；仪式召唤和调试召唤均已接入。
4. 新增 `djinni_job`、`afrit_job`、`marid_job` 注册和 renderer，均追加在已有 43 个实体注册之后，当前注册总数为 46，不改变旧实体 ID 顺序。
5. 新增 `HighTierJobSpiritPortContractTest`，覆盖工厂回退、属性、固定 tier、动态体型、召唤入口、注册和 renderer。

## 本轮完成：Storage Controller 的 JEI 配方转移

审计确认：本地已有 3×3 存储终端矩阵、`MessageSetRecipe`、服务端真实性校验和 NBT 槽位协议，但 JEI 1.12 注册层缺少 `IRecipeTransferHandler`，因此配方界面的“转移到存储控制器”入口不可用。现已补齐：

1. 新增 `StorageControllerRecipeTransferHandler`，适配 JEI `4.16.1.1013` 的旧式 `IRecipeTransferHandler<Container>` API。
2. 从 JEI 原版合成布局读取 1 到 9 号输入槽，映射为 `s0` 到 `s8`，每格最多携带 5 个候选物品，复用现有 `MessageSetRecipe` 发往服务端。
3. 在 `RitualJeiPlugin` 中注册 `ContainerStorageController` 对原版 `VanillaRecipeCategoryUid.CRAFTING` 的转移处理器；直连和远程存储 GUI 共用同一容器类，因此同时覆盖两种入口。
4. 补齐英文和中文的“找不到配方需要的物品”提示，并新增 `StorageControllerJeiTransferContractTest`。

## 本轮完成：Foliot 实体注册闭环

审计发现 `rituals.json` 中 10 条职业仪式引用 `occultism:foliot`，但 1.12 注册表只有 `foliot_job`。虽然仪式执行器会把职业仪式路由到现有职业实体，这仍会使资源实体 ID、命令解析和外部兼容入口不一致。现已补齐：

1. 新增 `EntityFoliot`，固定 Foliot 等阶，使用上游生命值、速度、攻击力、护甲、护甲韧性、追踪范围、碰撞体型和默认一天寿命。
2. 在 `ModEntities` 末尾追加 `foliot` 注册，旧的 1.12 实体 ID 顺序保持不变；当前注册数为 47。
3. 为 `EntityFoliot` 注册 `RenderFoliotJob`，保证客户端实体入口完整。
4. 新增 `FoliotEntityPortContractTest`，并回扫所有仪式实体引用，当前 35 个 Occultism 实体 ID 均有注册入口。

## 本轮完成：Wondering Trader 1.12.2 交易实体

此前 `summon_wondering_trader` 仍直接召唤 `minecraft:villager`，语言文本也明确标记特殊异界交易尚未实现。本轮补齐了 1.12.2 的可运行替代：

1. 新增 `EntityWonderingTrader extends EntityVillager`，复用 1.12 村民的寻路、交互、交易容器、同步和持久化生命周期。
2. 新增 `WonderingTrades`，提供普通村民兼容交易表和持久化的 Occultism 异界交易表；实体 NBT 分别保存 `CommonOffers` 与 `OtherworldOffers`。
3. 第三只眼、头部异界护目镜、主手或副手真视法杖会切换到异界交易；客户端覆写 `setRecipes` 接收服务端交易包，确保 GUI 显示所选列表。
4. `summon_wondering_trader` 仪式改为 `occultism:wondering_trader`，实体注册追加在已有 47 个注册之后，当前注册数为 48；客户端复用 `RenderVillager`。
5. 现代版本没有的 Spyglass、Demonic Meat 等输入没有被静默伪装：Spyglass 使用明确记录的 1.12 Ender Eye 替代，Demonic Meat 相关交易使用本地 Otherworld Essence、Cruelty Essence 或 Cursed Honey 承接。
6. 新增 `WonderingTraderPortContractTest`，覆盖实体继承、两套交易表、持久化字段、仪式 ID、注册顺序、renderer 和异界视野入口契约。

## 本轮完成：Devil Familiar 的 Blacksmith 升级

对照当前上游 Devil Familiar 行为后，补齐此前语言中明确标记为“尚未实现”的 1.12 缺口：

1. `EntityDevilFamiliar` 现在实现 `IBlacksmithUpgradable`，以同步数据参数保存一次性升级状态，并使用 `hasBlacksmithUpgrade` NBT 键持久化。
2. 黑smith升级后的主人右键普通金苹果，会将其转化为 1.12 元数据 `1` 的附魔金苹果；冷却为 `20 * 60 * 33` tick，并持久化 `sinLastTime`。
3. 已升级、未升级和冷却状态均使用服务端判断；未升级或冷却时通过双语 action bar 提示，避免客户端伪造消耗。
4. 新增 `DevilUpgradePortContractTest`，覆盖一次性升级、1.12 金苹果元数据、冷却常量和 NBT/提示键契约。
5. 现代上游的黄金梦果输入在 Minecraft 1.12 没有对应物品，本地明确保留为兼容差异，未把不相同的 `Datura` 或 `Demon's Dream Essence` 冒充等价物。

## 本轮完成：四种附身实体与恶魔肉

审计剩余附身仪式时，发现女巫、烈焰人、守卫者和僵尸猪灵仍然会召唤原版实体，且 `demonic_meat` 只有资源引用没有 1.12 物品注册。本轮已补齐：

1. 新增 `EntityPossessedWitch`、`EntityPossessedBlaze`、`EntityPossessedGuardian` 和 `EntityPossessedZombifiedPiglin`，分别继承 1.12 的女巫、烈焰人、守卫者和僵尸猪人实体。
2. 补齐专用生命值、速度、攻击力、防御属性、火焰免疫、雪球/水免疫、和平难度移除保护，以及僵尸增援禁用等 1.12 可表达行为。
3. 四种实体均追加到既有实体注册末尾，保持此前 48 个实体 ID 不变；当前实体注册总数为 52，并注册对应客户端 renderer。
4. 四条附身仪式改为召唤 Occultism 专属实体。僵尸猪灵沿用项目和上游字典已经使用的 canonical ID `possessed_zombie_piglin`；实体 Java 类名保留 `ZombifiedPiglin` 以表达 1.12 的原版类型。
5. 新增四张 1.12 loot table；女巫、烈焰人、守卫者和僵尸猪灵的固定掉落池均使用 1.12 schema。僵尸猪灵掉落 1-4 个 `demonic_meat` 和金粒。
6. 注册 `ModItems.DEMONIC_MEAT`，食用后获得 30 秒力量和 5 分钟抗火，并加入中英文名称和 tooltip。
7. 恶魔梦食物可治疗四种新增附身实体；字典实体引用和本地替代映射同步更新。
8. 扩展 `PossessedEntityPortContractTest`，覆盖实体属性、renderer、仪式 canonical ID、loot table、语言和恶魔肉效果；修正实体注册数量契约为当前 52 个。

## 本轮完成：附身蜂兼容行为

继续审计本地附身蜂时，确认实体已经注册并能攻击、施毒和掉落诅咒蜂蜜，但缺少“受伤召唤更多蜜蜂”和“进入蜂巢返回异世界”这两个上游行为。本轮已补齐 1.12 可表达的兼容实现：

1. `EntityPossessedBee` 继续继承 1.12 `EntityBat`，保留小型飞行实体、持久化和现有蝙蝠 renderer。
2. 攻击冷却缩短为 10 tick；毒刺攻击仍施加 4 秒中毒，保持本地诅咒蜂蜜掉落链。
3. 被伤害且不在冷却时召唤 2-3 只附身蜂增援；以 12 格范围内最多 8 只为硬上限，并使用 60 tick 增援冷却，避免受伤事件造成无限增长。
4. 没有玩家目标时，实体每 20 tick 搜索 8 格水平、4 格垂直范围内的 `occultism:bee_nest`，飞向最近蜂巢并在抵达后消失，模拟灵体返回异世界。
5. 更新附身蜂仪式 tooltip、双语字典说明和附身实体统计；明确保留 1.12 无法原样提供的蜂巢内部占用、现代蜜蜂 AI、蜜蜂模型和真实蜂蜜方块机制。
6. 新增 `PossessedBeePortContractTest`，覆盖 1.12 基类、属性、冷却/蜂群上限、蜂巢返回入口、仪式资源和双语说明。

## 本轮完成：仁慈之山羊冲撞行为

审计确认 `EntityMercyGoat` 已有实体注册、仪式和残暴灵髓掉落，但原实现完全没有山羊主动攻击行为。本轮补齐了 1.12 可表达的核心兼容行为：

1. `EntityMercyGoat` 继续继承 `EntitySheep`，保留现有白色羊外观、持久化和不接受玩家交互的仪式实体语义。
2. 服务端每 tick 搜索 12 格内最近的非创造玩家；目标较远时使用 1.12 navigator 追踪，进入 1.5 格距离后执行冲撞。
3. 冲撞造成 4 点伤害和 0.7 击退，命中或未命中后均进入 60 tick 冷却，并播放 1.12 羊受伤音效。
4. 更新中英文 ritual dummy tooltip 和字典说明，明确指出现代山羊角、跳跃、精确导航及专属模型仍属于 1.12 无法原样复现的差异。
5. 新增 `MercyGoatPortContractTest`，覆盖实体基类、冲撞常量、服务端目标筛选、追踪、伤害、击退、冷却、仪式引用和双语说明。

## 本轮完成：Purified Allay 所有权闭环

审计确认本地 `EntityPurifiedAllay` 已能拾取和交还物品，但只是显式列入 Familiar Ring 白名单的运输实体，未实现上游熟悉环的所有权语义。本轮已补齐：

1. `EntityPurifiedAllay` 现在实现共享 `IFamiliar` 接口，可使用统一的 owner、捕获和释放逻辑。
2. `resurrect_allay` 仪式完成时将实体绑定到施法玩家；owner UUID 使用 `OccultismFamiliarOwner` NBT 键持久化。
3. 有主人时实体只跟随主人；拾取物品后只尝试交还给主人，主人未加载时不会把物品转交给附近其他玩家。
4. 只有主人可以右键取回手持物品，也只有主人能够通过 Familiar Ring 捕获已绑定的 Purified Allay。
5. 更新中英文 tooltip 和字典说明，明确保留 1.12 无法原样实现的悦灵模型、复制、音符盒绑定、跳舞和完整高版本搜索行为。
6. 新增 `PurifiedAllayPortContractTest`，覆盖 Vex 基类、IFamiliar、owner NBT、跟随/交还入口、仪式绑定和双语资源。

## 本轮完成：野性群体仪式实体注册闭环

继续审计野性群体仪式时，确认五条仪式和野性灾厄村民群仍只引用原版实体 ID，字典也只能显示原版替代。本轮已补齐 1.12 的实体注册、渲染和仪式闭环：

1. 新增 `EntityWildHordeHusk`、`EntityWildHordeParched`、`EntityWildHordeDrowned`、`EntityWildHordeCreeper`、`EntityWildHordeSilverfish` 和 `EntityWildHordeEvoker` 六个兼容实体，分别复用 1.12 的尸壳、流浪者、僵尸、苦力怕、蠹虫和唤魔者基类。
2. `EntityWildHordeDrowned` 覆盖白天燃烧规则为 `false`；`EntityWildHordeCreeper` 在仪式生成的 `onInitialSpawn` 路径通过 1.12 闪电命中钩子设置充能状态。
3. 六个实体追加到已有 52 个实体注册末尾，当前注册总数为 58，不改变此前实体 ID 顺序；客户端分别复用 `RenderHusk`、`RenderSkeleton`、`RenderZombie`、`RenderCreeper`、`RenderSilverfish` 和 `RenderEvoker`。
4. `wild_creeper`、`wild_horde_illager`、`wild_husk`、`wild_parched`、`wild_drowned` 和 `wild_silverfish` 仪式改用对应的 `occultism:wild_horde_*` canonical ID，运行时会通过现有实体工厂和 `onInitialSpawn` 准备流程创建注册实体。
5. 字典不再为这些 ID 提供原版 substitute 映射；中英文 tooltip 和野性群体/实体替代说明同步改为已注册的 1.12 兼容实体。
6. 新增 `WildHordePortContractTest`，覆盖六个实体的继承关系、渲染器、注册数量、尸壳/僵尸白天规则、充能苦力怕初始化入口、六条仪式 canonical ID 和双语字典说明。

## 本轮完成：高阶处理职业倍率与 Marid 铸晶路径

继续审计高阶职业实体时，确认它们虽然已经使用专用实体和等阶属性，但共享的 1.12 处理循环仍固定使用基础时长和单倍产出。本轮已补齐：

1. 在 `ModConfig` 中加入 Crusher、Crystallizer、Smelter 四个等阶的处理时间、产出和单次操作配置，默认值对齐上游职业倍率。
2. `EntityFoliotJob` 按实体 tier 实际应用处理时间、输出倍率和批量操作数；Ancient 兼容回退到 Marid 的最高职业倍率。
3. `SpiritJobRecipe` 增加最低职业等阶门槛，并为 Marid 铸晶者加入本地 `Amethyst Block -> Budding Amethyst` 路径，避免 Foliot/Djinni/Afrit 提前使用。
4. 1.12 没有 Crying Obsidian 原生物品，未伪造同名替代物；Marid 调试提示和双语字典改为明确说明本地可表达行为与保留差异。
5. 修正机器管理调试物品的过时 tooltip，改为描述已实现的存储控制器与机器面之间的匹配物品转移，并补契约断言。

## 本轮完成：未受缚 Afrit/Marid 召唤闭环

审计发现 `summon_unbound_afrit`、其 prototype 和 `summon_unbound_marid` 虽然描述为未受缚召唤，但祭坛执行器会优先读取 `spirit_tier`，实际生成已束缚的高阶灵体。本轮已修正：

1. `summon_unbound_afrit` 和 prototype 显式召唤已注册的 `occultism:afrit_wild`，不再生成 `EntityAfrit`/`EntityBoundSpirit`。
2. 新增 `EntityMaridWild extends EntityMob`，实现玩家敌对 AI、上游 Marid 属性、火焰/溺水/爆炸/魔法伤害兼容免疫、持久化和 3 到 8 只 Guardian 随从生成。
3. 新增 `occultism:marid_wild` 注册、renderer、loot table 和中英文实体名称；renderer 复用本地 wild spirit 模型与现有 Afrit 纹理资源，保持 1.12 资源闭环。
4. `summon_unbound_marid` 显式召唤 `occultism:marid_wild`，掉落 `marid_essence`；三条未受缚仪式的 `spirit_tier` 仅保留为配方阶级元数据。
5. 祭坛执行顺序改为：世界效果/职业灵体、Wild Hunt、显式 `entity_to_summon`、最后才是 tier fallback；管理机器职业仪式仍由职业分支优先处理。
6. 新增 `MaridWildPortContractTest`，覆盖三条仪式 canonical entity ID、属性、Guardian 随从、敌对实体注册、renderer、loot table 和显式召唤优先级。
7. 新增实体追加在既有注册末尾，当前实体注册数为 59；旧实体数字 ID 顺序保持不变。

## 本轮完成：高阶灵体说明与实际代码同步

审计发现高阶实体和职业代码已经前进，但中英文 ritual tooltip 与辞典仍把部分功能描述为“完整高阶职业尚未实现”。本轮已同步：

1. `summon_afrit_prototype` 和 `summon_marid_prototype` 现在明确说明本地碎矿、铸晶和熔炼职业已经可用，同时保留上游 AI/模型差异。
2. 高阶灵体辞典改为列出已接入的 Djinni、Afrit、Marid 职业和机器操作员路线；Ancient 仍按当前 contact/craft 语义回退，不虚构独立实体。
3. Marid 职业辞典明确记录 `EntityMaridJob` 对应的本地实体、处理倍率、批量数量、呼唤之书匹配与 renderer 闭环，并保留上游 AI、模型、动画差异。
4. 未受缚 Afrit 辞典改为记录 `afrit_wild`、烈焰人随从、敌对目标、火焰免疫、renderer 和掉落表；不再错误描述为已束缚灵体。
5. `HighTierJobSpiritPortContractTest` 新增双语状态断言，禁止旧的“完整高阶职业仍在迁移”和“未受缚 Afrit 映射为已束缚灵体”文案回归。

## 本轮完成：Storage Remote ItemStack capability proxy

审计确认远程访问器此前只能打开远程 GUI，不能作为 Forge 1.12 物品处理器被物流或机器直接读取。本轮已补齐加载态 capability 代理：

1. 新增 `StorageRemoteCapabilityProvider`，实现 `IStorageControllerProxy`，并在目标控制器已加载时暴露真实 `ITEM_HANDLER`。
2. 新增 `StorageRemoteCapabilityHandler`，通过 `AttachCapabilitiesEvent<ItemStack>` 只给 `Storage Remote` 附加代理，不污染普通物品。
3. `ItemStorageRemote.getStorageController` 允许无当前世界上下文时通过 `DimensionManager` 查询已加载目标，但仍拒绝未加载维度或区块，不触发强制加载。
4. 远程链接 setter 同时维护旧四键和嵌套 `linkedStorageController` NBT；清理时只移除链接字段，保留其它物品 NBT。
5. 存储辞典同步为动态 128 起始槽位、稳定器扩容和加载态物流能力，并保留跨网络虚拟缓存、异步缓存及跨区块加载尚未实现的边界。
6. 新增 `StorageRemoteCapabilityPortContractTest`，覆盖 ItemStack 附件、未加载目标惰性解析、NBT 往返/清理和双语文案。

## 2026-08-23 继续：Storage Remote 容器协议闭环

本轮继续审计上游 `StorageRemoteContainer` 与本地复用的远程/稳定虫洞容器，补齐了此前最明确的状态语义缺口：

1. `ItemStorageRemote` 打开 GUI 时传递具体玩家背包槽位；服务端从该槽重新读取 Remote 并解析已加载目标控制器，不再通过客户端坐标决定远程来源。
2. Remote 容器锁定该槽位，期间禁止取出、替换、放入和快捷移动；Remote 被替换或移除后容器访问校验失败。
3. 3x3 crafting matrix 与 order slot 改为持久化在 Remote ItemStack 的 `craftingMatrix`/`orderStack` NBT 中，不再错误写入目标控制器；新增 `RealSize` 伴随字段，保留 1.12 超过 127/64 的堆叠数量。
4. 稳定虫洞改用独立 `STORAGE_WORMHOLE` GUI 入口，继续按虫洞实体、附近距离和目标控制器实例校验，避免把虫洞访问误判成 Remote ItemStack 访问。
5. `StorageCraftingContractTest` 与 `StorageRemoteCapabilityPortContractTest` 新增 Remote NBT 往返、锁槽和替换失效契约；资源入口契约同步更新。

## 2026-08-23 继续：Otherworld Bird Double Jump

上游 Otherworld Bird 不只提供 Jump Boost 和 Slow Falling，还通过玩家 capability、客户端跳跃输入和服务端包提供 Double Jump。本轮在 1.12 补齐：

1. 新增 `PotionDoubleJump`，并把 Otherworld Bird 的第三个效果设为 120 tick、amplifier 4，对应最多 5 次空中跳跃授权。
2. 新增 `DoubleJumpCapability`，记录落地前已使用的空中跳跃次数；服务端和客户端落地时清零，NBT 仅保存计数。
3. 新增 `MovementUtil`、`MessageDoubleJump`、`MessageSetJumps`、`DoubleJumpHandler` 和 `ClientDoubleJumpHandler`：客户端按跳跃键沿用本地预测，服务端重新检查药水效果、落地/骑乘/水域/飞行/鞘翅状态后执行跳跃并同步计数。
4. 英中效果名和异界鸟辞典说明已从“缺少双重跳跃”更新为实际的服务端校验能力。
5. `OtherworldBirdPortContractTest` 与 `DoubleJumpCapabilityPortContractTest` 覆盖效果注册、能力 NBT、落地清零、输入包和服务端校验接线。

## 2026-08-23 继续：通用高阶灵体主库存与交互入口

本轮继续对照上游 `SpiritEntity` 的 `ItemStackHandler`、`ITEM_HANDLER` capability、主库存 NBT 和 `SpiritContainer`，补齐了本地通用高阶灵体此前缺少的最小可玩闭环：

1. `EntityBoundSpirit` 新增 1 槽主库存，暴露 Forge `ITEM_HANDLER` capability；槽位变化会同步到客户端实体数据，避免客户端 GUI 只看到空槽。
2. `SpiritInventory` 写入实体 NBT，读档后恢复完整 `ItemStack`；灵体死亡时先掉出主库存，再按既有路径掉落灵魂碎片。
3. 潜行空手与通用高阶灵体交互时，服务端追加 `BOUND_SPIRIT` GUI 入口；`ContainerBoundSpirit` 复用上游 1.12 布局和快捷移动语义，`GuiBoundSpirit` 显示灵体名称、阶级和主库存。
4. 既有职业灵体继续使用 `ContainerSpirit`/`ContainerSpiritTransporter` 与自身过滤、工作区、机器订单状态，避免把职业专用槽位和通用高阶灵体库存强行合并。
5. 新增 `BoundSpiritInventoryPortContractTest`，覆盖 capability 单槽、物品插入、NBT round-trip、GUI 注册、服务端交互和双语入口。

兼容边界：本轮只关闭通用库存/容器入口，不宣称完成上游 `SpiritEntity` 的 owner、SmartBrain、filter 槽、工作区/存取点同步、Job 状态、完整菜单授权或高阶职业 AI。

## 新增测试覆盖

新增 `BookOfCallingGuiPortContractTest`，覆盖：

- 模式稳定值及非法值拒绝。
- 工作范围稳定值和旧 1.12 ordinal 回退。
- 两个即时消息的 ByteBuf 编解码往返。
- 普通 GUI 的上游尺寸、布局、即时消息、条件控件和无确认按钮行为。
- 容器、槽位、物品类型和职业模式服务端校验。
- 管理机器专用 GUI 的布局、文本限制、发包时机和语言键。
- 管理机器消息只应用客户端可信字段，端点保持服务端所有权。

同时扩展 `HighTierJobSpiritPortContractTest`，覆盖高阶灵体、Marid 职业和未受缚 Afrit 的双语状态文案边界。

## 已执行验证

### 构建与测试

命令：`./gradlew-local.bat test`  
结果：`BUILD SUCCESSFUL`。57 个测试套件、341 个测试全部通过，失败 0、错误 0、跳过 0。

命令：`./gradlew-local.bat build`  
结果：`BUILD SUCCESSFUL`。主源码编译、重映射 jar、源码 jar、测试和检查任务均通过。

### 资源校验

命令：`./tools/verify_assets.ps1`  
结果：`Asset verification passed`。JSON、模型父级、纹理、方块状态、语言键、配方语义和字典语义错误均为 0。

### 差异检查

命令：`git diff --check`  
结果：通过。仅有 Git 的 LF/CRLF 工作区提示，没有空白错误。

### 语言键检查

`en_us.lang` 与 `zh_cn.lang` 重复键数量均为 0。

## 尚未完成的运行时验证

1. 此前 `runServer` 只到达首次 EULA 提示，没有自动接受 EULA，也没有生成世界，因此地下树丛尚未在真实专用服务器新区块中观察。
2. 普通呼唤之书和管理机器专用 GUI 已通过编译和源码/协议合约测试，但尚未做真实客户端逐按钮点击、缩放和关闭路径烟测。
3. 尚未在真实存档中验证旧 `0/1/2` 工作范围 NBT 经读取后升级为 `16/32/64` 的完整保存循环。
4. JEI 配方转移已完成静态和编译验证，但尚未在真实客户端中点击原版合成配方并观察远程存储矩阵填充、缺料跳过和合成结果。
5. `foliot` 注册闭环已通过源码、实体属性和资源引用验证，但尚未在真实客户端执行 `/summon occultism:foliot` 或从职业仪式完成后观察实体同步。
6. 当前整体功能没有逐项运行时清单，因此不提供虚假的总完成百分比；测试通过不等同于所有上游功能均已运行验证。

7. Devil Familiar 的金苹果转化已通过源码和契约测试，但尚未在真实客户端观察升级、冷却提示、背包满时的掉落路径；黄金梦果输入仍等待明确的 1.12 本地物品设计。
8. Wondering Trader 已通过源码、契约测试和构建验证，但尚未在真实客户端观察普通交易与异界交易的交互切换、交易耗尽后的重置和读档后的交易持久化。
9. 未受缚 Afrit/Marid 已通过源码、资源、契约测试和构建验证，但尚未在真实客户端观察仪式完成后的实体 AI、随从生成、掉落和 renderer 表现。

## 下一步

1. 在明确接受 EULA 的隔离测试实例中创建新世界，验证地下树丛生成、失败回滚和区块边界。
2. 启动 1.12 客户端，手动验证普通呼唤之书模式循环、工作范围、Esc/X 关闭以及服务端库存同步。
3. 绑定管理机器魔灵，验证专用 GUI 的六向循环、名称关闭提交、同机器配置保留和换机器初始化。
4. 在真实客户端验证 JEI 原版合成配方转移到 Storage Controller 的完整交互。
5. 在真实客户端和专用服务器验证 `occultism:foliot` 的命令召唤、同步和职业仪式路径。
6. 在真实客户端验证 `occultism:wondering_trader` 的普通/异界视野交易切换、交易同步和读档持久化。
7. 在真实客户端验证未受缚 Afrit/Marid 仪式完成、敌对目标选择、随从生成和灵髓掉落。
8. 继续从上游注册和生命周期入口反查本地已有资源或数据类但没有实际入口的剩余模块。
