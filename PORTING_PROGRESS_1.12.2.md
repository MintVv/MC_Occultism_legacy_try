# Occultism 1.12.2 移植进度报告

> 快照日期：2026-10-16（Asia/Shanghai）；下方旧日期验证记录保留为历史，不代表最新全量结果。
> 2026-10-14：Patchouli 1.12 `1.0-23.6` 已接入，原辞典物品优先打开帕秋莉；英中各 9 类、203 条目，当前共 2065 页（初次接入时为2071页），生成器为 `tools/generate_patchouli_book.py`。仪式/灵火页接入运行期配方，法阵使用本地检测图案及骷髅 tile 检查；补齐两张功能性 0 阶稳定器配方。详细实现、验证与剩余边界见 `进度.md`、`PORTING_STATUS_SUMMARY.md` 最新段落。
> Patchouli 接入阶段历史验证：Java 编译通过；最终仅定向执行 `PatchouliBookPortTest`，4 项通过。旧全量在新书缺失引用处失败，修复后没有重跑全量。客户端加载日志确认 10 个 Mod 和 170 条仪式，未做实际开书与世界投影回归；不代表后续源码已运行客户端验证。
> 当前工作约束：不使用 computer，不跑全量测试；优先写代码，累计修改后必要时定向检查，不移植装饰类方块。
> 2026-10-16 注册表追加：修复脚本删除最后一条/全部配方后查询恢复默认仪式及加载失败不断重试；普通初始化保留已加载表，新增 CraftTweaker `Ritual.removeAll()`。Java 编译与注册表/生命周期/献祭/数据四个定向类共39项通过，0失败/错误/跳过，未启动游戏或运行全量。明确重载和未验证边界见 `RITUAL_REGISTRY_1.12.md`。
> 材料分配追加：现有170条仪式的单件追加成本改用有界 Forge 匹配，启动/持续校验/实际消费共用分配计划；多件及大型脚本成本保留兼容后备。Java 编译与六个定向类56项通过，未启动游戏/执行全量；局限及与上游贪心实现的明确偏离见 `RITUAL_MATERIAL_MATCHING_1.12.md`。
> 仪式持续校验追加：等待/运行期间检查选定法阵、启动物和未消费材料；失效中断返还当前启动物，加载时跳过已消费材料，修复加速结束漏付成本。Java 编译和五个定向类48项通过，未启动游戏/执行全量；真实区块/多人/脚本回归仍未完成，详见 `RITUAL_LIFECYCLE_1.12.md`。
> 献祭事件追加：删除金碗自动击杀，改为服务端玩家归因死亡事件（含箭矢）、金碗8格三维方块范围、tick末取消/复活/仪式代次确认；`humans` 对齐村民和玩家。编译及五个定向类47项通过，未用 computer/启动游戏/跑全量。真实多人及保存卸载边界见 `RITUAL_SACRIFICE_1.12.md`。
> 天气/时间职业追加：五条仪式改为召唤本地 Djinni/Afrit 职业实体，迁入上游延迟施法、昼夜插值、五项配置开关与施法 NBT；正常完成后消散。Java 编译和四个定向类共 36 项通过，未启动客户端/执行全量。旧“直接修改世界状态”基线已被替代，剩余世界回归见 `WEATHER_TIME_PORT_1.12.md`。
> 2026-10-14 交易 ID/NBT 追加：已支持按 ID 配方选择/替换/移除、四个上游同名存档字段和 CraftTweaker 入口，帕秋莉交易页读取当前定义；默认物品延迟解析修复初始化顺序问题。最新三个定向类共 26 项通过，编译通过；没有客户端或全量运行。脚本实际执行、多输入交易与自动同步仍未验证/完成，见 `SPIRIT_TRADE_1.12.md`。
> 2026-10-14 交易职业批次：新增共享 `SpiritTradeRecipeRegistry`，实体与两套说明书共用交易定义；异界石/异界岩按上游 300 tick / 4 次，异界树苗按 400 tick / 1 次；新增 Patchouli 交易绘制页。定向 21 项测试（交易 7、高阶职业 10、说明书 4）通过，未运行全量。
> 2026-10-16 多原料追加：最多 9 项无序成本、Forge `RecipeMatcher` 分配、复制式 `isValid/consume`、实体余料补入、Patchouli/旧辞典多成本显示和 CraftTweaker `addShapelessRecipe` 已完成。四个定向类共 38 项通过，编译通过；未使用 computer、未跑全量、未做页面实测。原生数据包、自定义交易同步、混合物品收集 AI 和世界回归仍未完成。
> 2026-10-16 工程追加：移除 `gradle.properties` 的本机绝对 JDK 路径，改由 `gradlew-local.bat` 的 `JAVA_HOME` 选择本地 JDK；未编译、未运行测试。
> 2026-10-16 存储追加：修复 tier5/tier5_dark 稳定器容量映射缺失，新增默认 1024 槽配置与定向契约；`StorageStabilizerPortContractTest` 8 项通过，Java 编译通过，未启动客户端/运行全量。
> 2026-10-16 消息追加：170 条仪式均补齐中英文四类上游消息键；开始/完成/中断/条件失败使用 per-ritual 组件，CraftTweaker 动态定义保留通用回退。仪式本地化和资源定向测试通过，Java 编译通过，未启动客户端/运行全量。
> 历史构造器修复批次（非当前验证）：（1）当时全量基线为 **58 个测试类、345 项、0 失败/错误**；新增高阶灵体模型/renderer 契约。（2）修复 `EntityDjinniJob`、`EntityAfritJob`、`EntityMaridJob` 注册时缺少公开 `World` 构造器的问题。（3）修正 Otherworld Bird 野性仪式中的 1.12 物品 ID：`minecraft:cobweb` → `minecraft:web`。（4）当时 `runClient` 加载烟测确认仪式数据和9个 Mod 成功加载。最新结果只认上方定向批次及 `进度.md`，不将旧全量/客户端结果推广到当前改动。
> 2026-10-13 增补：仪式辞典页已接入按上游 `BookRitualRecipePageRenderer` 重建的专用布局；同时按维护者要求移除异界木板门、墙、按钮、台阶、楼梯、栅栏、压力板及镂空/裂纹装饰砖等装饰类方块与残留资源。`compileJava`、全量测试、资源审计和客户端加载烟测均通过。
> 2026-10-13 追加：仪式页法阵标题采用上游本地化键并可点击跳转至法阵条目；12 个祭祀碗、中心金碗、启动物、产物、献祭/召唤/职业文本保持上游 renderer 坐标。
> 2026-10-13 追加：仪式运行视觉按上游状态补齐灰色等待粒子、紫色运行粒子、等待献祭状态、祭祀碗内物品浮空旋转和完成时中心烟雾；未新增上游不存在的法阵光束。
> 2026-10-13 追加：进一步按上游粒子时序收敛：等待粒子改为独立 1.12 `Particle` 的上升/渐隐/灰度实现，运行粒子改为服务端 `1/16` 随机 `PORTAL`，删除客户端重复粒子与自创材料碗 marker；所有金碗变体和浮空物品放入时相位统一。
> 目标环境：Minecraft 1.12.2 / Forge 14.23.5.2847  
> 代码参考上游：`_reference/occultism-upstream`，分支 `version/1.20.1`，提交 `84b8ad4c14debb3c28247964edd55c076cd010c2`，`gradle.properties` 标记 Occultism `1.81.4`  
> 手册基础索引来源：本地索引头部标记为 `occultism-26.1.2 generated Modonomicon dictionary_of_spirits`，与代码参考快照不是同一基线；功能语义仍以 1.81.4 为准，26.1.2 仅用于资源结构和明显错误修正

## 总体结论

当前移植版应定义为：**可构建、核心玩法已有较完整本地实现，但上游等价性和发布验证尚未完成的功能性 Alpha**。

- 构建、注册、静态资源、双语文本和主要 GUI 已形成完整工程骨架。
- 仪式、法阵、Foliot 工人、矿井、战场、存储、虫洞，以及 16 类本地 Familiar 的正常死亡 -> 灵魂碎片 -> 复活主循环已有实际 1.12 代码，不只是资源占位。
- 本轮已补齐 Familiar 公共属性基线、构造持久化与禁止 despawn、墙体碰撞免疫；Bound Spirit/Foliot Job 也已补墙体免疫及非玩家/非箭矢伤害衰减，并由 12 项 Familiar shard 契约测试覆盖。
- 参考上游的 16 条 Familiar 直接仪式现已全部落地；Cthulhu、Blacksmith、Greedy、Dragon、Fairy、Beholder、Chimera，以及由 Chimera 分离 Goat、再转化 Shub Niggurath、周期生成 Spawn 的依赖链，均已形成实体、AI、模型渲染、熟魔戒或派生生成、辞典资源的静态闭环。Blacksmith 现可升级同主人的 Bat、Beaver、Beholder、Cthulhu、Deer、Dragon、Goat、Greedy、Guardian、Headless、Mummy 和 Shub Niggurath 共 12 类目标，但仍缺游戏世界内行为回归。
- Wild Hunt 已从单只原版凋灵骷髅替代升级为专属双实体、3 首领/每首领 3-8 随从、护盾/友军免疫、专用法阵/仪式/掉落和隐藏进度的静态闭环；本轮新增首领/随从 UUID NBT 持久化及仅对已加载实体的延迟重连，献祭模型仍不等价，真实世界群战和区块重载仍未回归。
- 固定 1.81.4 上游的 10 类专用附身实体现已全部接入本地注册：Endermite、Skeleton、Enderman、Ghast、Weak Shulker、Shulker、Elder Guardian、Phantom 为原版基类兼容实现，Hoglin 与 Warden 为专用 1.12 兼容实体，并分别接入仪式、loot table、模型和 renderer。Hoglin 已补经典红/棕蘑菇回避替代和下界外 300 tick 后转化原版僵尸猪人，但仍缺现代 Brain、真正 Zoglin 属性、下界生物群系规则和诡异菌身份；Warden 已补有界的玩家扫描、4-24 格魔法脉冲、60 tick 冷却、6 tick 同步状态、Weakness 和周期失明场，但仍缺现代 Brain、真实振动事件、钻地、Darkness 和真正音波等价行为；上述批次尚未进入真实世界击杀或执行仪式。
- 高版本专属实体、完整高阶灵体 AI/模型、部分 owner/link 行为、原版 Curios API 兼容、真实世界中的碎片交付/复活回归和现代集成仍以简化实现、原版实体替代、未验证或未移植状态存在；Familiar Ring 已改用原生 1.12 玩家单槽 capability。
- 当前快照已通过 **57 个测试类、342 项 JUnit**，0 失败/错误；并通过主工程重新编译。静态资源校验和本轮打包结果均已通过。本轮补齐 `FamiliarTrigger` 全部 26 类事件：稀有变体、Cthulhu sad、Devil fire、Blacksmith upgrade、Greedy pickup、Dragon ride、Chimera ride、Goat detach、Shub summon/friends/spawn、Beholder ray/eat、Beaver woodchop 和 PARTY，并新增 15 个隐藏 advancement（Dragon PET/FETCH 仍共用一个既有文件）。同时补入 Storage Controller 自代理语义（控制器按上游实现 `IStorageControllerProxy`，返回自身和全局位置），远程访问器上游兼容的嵌套 `linkedStorageController` 引用和 loaded-only 解析，以及 `GlobalBlockPos` 对上游 `pos` packed-long + `dimension` resource-id NBT 的双向兼容和未知维度拒绝；此前的合成矩阵/order slot 持久化、容器级瞬时 recipe lock、linked machine/order 最小路由、机器订单请求包、控制器级 `MessageUpdateStacks` 快照缓存及失效也已保留。Familiar settings 已覆盖 17 类（含 Otherworld Bird），客户端按键为 18 个并有双语状态文案。新增 Soul Shard `getNBTShareTag`，客户端只同步实体 `id`，服务端完整实体 NBT、legacy 字段和副本隔离均有契约覆盖。通用高阶灵体新增 1 槽主库存、ITEM_HANDLER capability、NBT 同步/往返和独立服务端容器/GUI，并由 Bound Spirit 契约测试覆盖。此前终端合成（3x3 矩阵、服务端配方匹配、结果槽安全返还、存储补料、剩余物处理、批量合成、配方消息边界校验）、Machine Manager 同维度导航/严格端点/有界订单重试、Familiar Ring 客户端 foil/类型本地化/运输边界、Djinni 机器操作员、虚拟存储聚合/提取映射、有界栈同步、有原生 Familiar Ring capability/网络、`familiarType` loot 转换、跨维度存储 GUI 与稳定虫洞 capability 代理、Storage Controller 动态容量/超大堆叠/黑名单/搜索/排序/设置同步/GUI、Hoglin/Warden、`summon_wild_parrot`、`summon_wild_afrit` 均保留在无过滤全量测试中。客户端主菜单、JEI/仪式加载烟测早于专用附身实体批次，因此不能把该旧烟测当成新实体/renderer 已运行加载的证据；完整玩法回归和专用服务器验证仍未完成。
- 不建议给出单一完成百分比。参考上游和 1.12 的技术边界差异很大，Java 文件数、资源数以及 ritual dummy 数量都不能直接换算为功能完成率。

如必须用于项目管理，可采用三个彼此独立的粗略区间：

- **1.12 核心玩法循环：约 65%-75%**。入门材料、第三眼、法阵、仪式、职业灵体和矿井主线已经存在。
- **相对当前参考上游的完整功能面：约 45%-55%**。主要缺口是存储网络、实体/使魔阵容、独立高阶 AI、Modonomicon 框架能力、配置和现代集成。
- **当前已注册内容的静态资源闭合度：约 85%-90%**。这只表示模型、贴图、语言和引用较齐全，不表示行为等价或发布就绪。

## 近期移植：Blacksmith 使魔与 Cthulhu 升级链

- 新增独立 `EntityBlacksmithFamiliar`：30 生命、15 护甲、10 护甲韧性、0.3 移速、6 攻击，保留耳环/胡须/发型 variant、铁储量、同步铁锭层数和 NBT 往返。
- 保留 1.81.4 喂铁、容量和升级契约：铁锭计 1、铁块计 9，默认每次升级消耗 18、最多显示 10 组；死亡按铁块和余数铁锭返还。1.81.4 先缩栈再判定铁类型的单物品误算已修正为先记录类型。
- 新增 `IBlacksmithUpgradable` 接口；Blacksmith 在 4 格内寻找同主人且具备完整升级效果的目标，默认冷却 400 tick，接近到距离平方小于 3 后扣铁并升级。
- 新增 1.12 Forge 配置 `spirit_job`，默认修理概率 `0.05`、升级花费 `18`、冷却 `400`；客户端启动已实际生成并读取 `run/config/occultism.cfg`。
- 新增石头拾取修理：10 格内有存活且同主人的 Blacksmith 时，拾取 `stone`/`cobblestone` 会按 `0.05 * 堆叠数` 判定，并只修复装备遍历中的第一件受损物品 2 点；无论是否触发修理，整堆石头都会被消耗。
- 新增 64x64 上游模型/纹理、货车和铁砧、唱片舞蹈/坐姿/行走动画、三种 variant、圣诞胡须，以及最多 10 个铁锭的独立渲染层。
- 新增 `familiar_blacksmith` 仪式、ritual dummy、僵尸献祭、熟魔戒 owner 校验、辞典四页和中英文文本；同时移除 Cthulhu 指南中“Blacksmith 尚未完成”的过期说明。
- 原版 1.12 玩家单槽已接入 Familiar Ring：装备戒中的实体每 tick 执行 `curioTick`，每 20 tick 刷新设置允许的效果，Guardian/Fairy/Headless/Blacksmith 查询也会合并已装备戒指；普通背包戒指仍不会生效。客户端 foil 会按装备 Familiar 的 `isEffectEnabled` 决定，类型 tooltip 会通过实体注册名本地化并对未知 ID 回退原文；非 `IFamiliar` 实体仅允许运输、不能装备或提供戒中效果。Beaver、Greedy、Dragon、Goat、Shub Niggurath 与 Beholder 已在后续批次接入，固定 1.81.4 快照中本地可升级阵容现为 12 类。Chimera 按该快照本身不可升级；真实世界升级流程仍未完成。

## 近期移植：Guardian 与 Mummy 升级

- `EntityGuardianFamiliar` 已接入同步升级标记和精确的 `hasBlacksmithUpgrade` NBT：仅在未升级且生命少于 5 条时接受升级，成功后只恢复 1 条生命并永久消耗一次升级机会；满生命时拒绝且不会误置升级标记。
- `EntityMummyFamiliar` 已接入同一升级契约：保留基础攻击 9，以固定 UUID `6aa62086-7009-402b-9c13-c2de74bf077d` 添加 operation 0、永久 `+3` 攻击 modifier，重复调用和存档属性恢复均按 UUID 幂等，最终攻击为 12。严格采用 1.81.4 行为，不移植后续版本的“双倍闪避”描述。
- 木乃伊模型加入上游左右拳套几何，仅在升级后显示；Guardian 的生命数本来就会通过肢体模型直接反映，无需新增装饰。两类 ritual dummy 的英文和简体中文提示已改为实际升级效果。
- 修复 1.12 `EntityTameable` 不自动注册 `ATTACK_DAMAGE` 导致的构造空指针：Mummy、Devil、Headless 和 Demonic Partner 现在会在缺失时显式注册攻击属性；Cthulhu 与 Blacksmith 已采用同一兼容路径。
- 新增 5 项 `FamiliarUpgradePortContractTest`，覆盖 Guardian 的一次性恢复/满生命拒绝、Mummy 的 `9 -> 12`、UUID 幂等和无世界状态恢复模拟、模型/双语文本/NBT 源码契约，以及 6 类现有战斗实体的真实构造。

## 本轮新增：Bat 生命窃取升级

- `EntityBatFamiliar` 已实现 `IBlacksmithUpgradable`，加入同步 Boolean 和精确的 `hasBlacksmithUpgrade` NBT；未升级时只接受一次 Blacksmith 升级，重复调用不会改变状态。
- 新增 beneficial Potion `occultism:bat_lifesteal`，颜色 `0x960201`、默认 amplifier 0、持续 300 tick；升级后的 Bat 每 10 tick 与夜视一起刷新给主人。夜视已由本地旧实现的 amplifier 0 修正为 1，并与上游一样隐藏效果粒子。
- `LivingDeathEvent` 通过 1.12 `DamageSource.getTrueSource()` 识别近战或投射物的实际攻击者；攻击者持有生命窃取时，每次死亡事件恢复 `1 + amplifier` 点生命。上游 1.81.4 与 26.1.2 都在 Guardian 取消死亡后继续执行吸血，本地明确保留这一调用顺序。
- Bat 坐下现在只停止移动、捕食和飞行，不再错误地停止给主人刷新效果；这与上游 `FamiliarEntity.aiStep()` 在坐姿外统一应用效果的行为一致。
- `ModelBatFamiliar` 已迁入 7 段酒杯几何，使用现有且与上游 SHA-256 完全一致的 64x32 纹理；酒杯仅在已升级且未坐下时显示。未迁移的 jukebox partying 状态不在本轮伪装实现。
- 英文和简体中文已新增 `effect.occultism.bat_lifesteal`，ritual dummy 提示会说明一次性升级和每次击杀恢复 1 点生命；新增 5 项 `BatUpgradePortContractTest` 覆盖升级、NBT/主人效果、Potion、回血公式、true source、酒杯几何和纹理。

## 本轮新增：Deer 战锤与 Headless 虚弱升级

- `EntityDeerFamiliar` 与 `EntityHeadlessFamiliar` 已实现 `IBlacksmithUpgradable`，加入同步 Boolean、精确的 `hasBlacksmithUpgrade` NBT 和一次性升级门控；随着 Beaver、Greedy、Dragon 以及后续 Goat、Shub Niggurath、Beholder 完成，Blacksmith 的能力实现者 allowlist 现固定为 Bat、Beaver、Beholder、Cthulhu、Deer、Dragon、Goat、Greedy、Guardian、Headless、Mummy、Shub Niggurath 共 12 类。
- `EntityDevilFamiliar.AttackGoal` 已抽成 Deer/Devil/Headless 共用目标：采用 1.81.4 的 `MAX_COOLDOWN=20` 与 `cooldown-- < 0` 节拍检查主人的受击和攻击目标，在距离平方小于 5 时造成 4 点、归因于主人的伤害并点燃 4 秒；保留重复目标、不额外过滤死亡目标及无坐姿门控语义。Deer 仅在升级后启用该目标，Headless 则按上游常驻启用并在“死亡重建”状态停用。
- Deer 模型加入完整战锤层级和八个锤刺，只在升级后显示；攻击时使用 1.12 `swingProgress` 驱动头部和鼻部挥击。现有 64x16 Deer 纹理哈希与 1.81.4 参考一致。
- Headless 保留 1.81.4 的七种精确头型：Player、Zombie、Skeleton、Wither Skeleton、Creeper、Spider、Cthulhu；玩家子类统一为 Player，其他实体不再因 Java 继承关系误匹配。主人造成的同头型最终伤害在 `LivingDamageEvent` 阶段乘 `1.3`。
- 升级后的 Headless 每 10 tick 对 5 格包围盒内、与当前头型精确相同且不是主人的实体施加 60 tick、amplifier 0 的 Weakness；客户端同期显示青色 `SPELL_MOB`，作为 1.12 缺少 `SOUL_FIRE_FLAME` 的近似。头盔几何、UV、显隐及实体纹理哈希与参考快照一致。
- 最终复核已删除 Headless 原有的第二套近战/owner-target 链和共享目标中的额外自身火焰爆发，避免 6 点近战与 4 点火焰攻击叠加，也避免 Deer/Headless 获得上游没有的自身 `FLAME` 粒子；Devil 则在自己的客户端 `onLivingUpdate` 中保留上游每个挥击 tick 生成 5 颗定向 `FLAME` 的视觉。中英文 ritual dummy 提示已同步更新。
- 新增 7 项 `DeerHeadlessUpgradePortContractTest`，覆盖一次性升级、同步/NBT、共享攻击契约、Devil 专属客户端挥击火焰、Deer 战锤、Headless 虚弱/粒子/头盔、七种精确头型及双语文本。

## 本轮新增：Beaver 零食升级

- `EntityBeaverFamiliar` 已实现 `IBlacksmithUpgradable`，加入同步 Boolean 和精确的 `hasBlacksmithUpgrade` NBT；成功升级后只接受一次升级，重复调用不再改变状态。
- 主人未潜行且当前交互手为空时，Beaver 会优先处理零食交互而不切换坐姿：未升级显示 actionbar 提示；升级后在 `worldTime > lastSnackTime + 20` 的严格边界给予 1 个零食，否则显示冷却提示。该入口保留上游的主/副手语义，潜行空手仍落回原有坐姿交互；发物和提示只在服务端执行。
- 新增 append-only 物品 `occultism:beaver_nugget`：普通 `ItemFood`，恢复 8 点饥饿值、饱和度系数 `0.8`、默认堆叠 64，无药水效果；16x16 上游纹理 SHA-256 为 `C126E7824E4C600665D6B7102E49C07A09A44E5182158C331140F81A5815F1DA`。
- 上游 1.81.4 的 `canBlacksmithUpgrade()` 无条件返回 `true`，会让同一只已升级 Beaver 反复消耗默认 18 铁且没有叠加效果，还可能持续抢占其他目标。本地有意修正为一次性升级，并由测试和本报告固定该偏离；`lastSnackTime` 则仍按上游不写 NBT，重载后会重置。
- 英文和简体中文已加入物品名及两条 dialog；ritual dummy 提示改为实际升级效果。辞典原先声称零食由 Beaver 死亡掉落、附带挖掘疲劳并提高采矿速度，与 1.81.4 代码冲突，现已改为“升级后的 Beaver 分发、恢复 8 点饥饿值、无额外效果”。
- 新增 6 项 `BeaverUpgradePortContractTest`，覆盖一次性升级、同步/NBT、严格冷却和副手入口、食物属性、append-only 注册、模型/纹理哈希、双语文本及错误辞典描述清理。

## 本轮新增：Greedy 方块定位升级

- `EntityGreedyFamiliar` 已实现一次性 `IBlacksmithUpgradable`，加入同步升级标记和精确的 `hasBlacksmithUpgrade` NBT。副手方块样本由原版 `HandItems` 保存，可随熟魔戒捕获/释放保留；同步的 `Optional<BlockPos>` 只表示当前运行期目标，不写入存档。
- 样本交互保持 1.81.4 的任意玩家语义：已有样本时优先返还；升级后持 `ItemBlock` 可放入一个 count 1、无 metadata/NBT 的样本；只有主人主手空手时才切换坐姿。物品和状态变更只在服务端执行，换样本或坐下会清理旧目标与路径。
- `FindItemGoal` 在 12 格内使用 `PlayerMainInvWrapper` 模拟主人主背包容量，尊重物品栈和实体上的 `PreventRemoteMovement`，靠近后调用原版 `EntityItem.onCollideWithPlayer(owner)`；已删除旧本地实现中暂存/返还主人主手物品的覆盖链。
- 独立 `FindBlockGoal` 搜索水平 10、垂直 5 的完整体积，按 `Block` 身份精确匹配并忽略 metadata/state，选择最近目标且不挖掘方块。样本变化会立即重搜；无目标或目标失效后退避 200-399 tick；未到达目标最多寻路 1200 tick 并每 40 tick 重算，到达后按上游状态机递减随机 1200-3598 tick 的停留预算且不再反复向实体方块内部寻路，目标区块卸载时安全退出。
- 基础属性对齐上游：尺寸 `0.5 x 0.9`、30 生命、15 护甲、10 护甲韧性、0.3 移速、6 攻击；Panic、Swimming、Sit、FindBlock、RideDragon、FindItem、Watch、FollowOwner、Wander 和 1.12 `EntityAIFollow` 优先级已接入。Dragon 批次已补齐同主人、未坐下且无乘客目标的骑乘逻辑。
- 客户端迁入 16 部件、32x32 模型和耳朵指向、箱盖、箱中怪物、行走、坐姿、party、目标右臂动画；专属 `RenderLiving` 提供箱体/圣诞透明叠加层、升级后右手铁镐与左手 0.75 倍方块样本，避免 `RenderBiped` 自动手持层重复。三张纹理 SHA-256 分别为基础 `F6AC783462D71B634CB8775133BEFE9B45C4A0F632405DDEDE74AEDF66A13AAB`、箱体 `1AEE0D70D33FCBA893A375AB7ADB85EDC0CBC6B2C66E8A44C51256EBB3AE459F`、圣诞 `364AAFA87E639672EB491B2C9BCAD2070D9ADD99CD7CE5D93633D5543316B46B`。
- Dictionary 新增 entity/ritual/description 三页、生成器白名单、GUI fallback 和书图标；图标 SHA-256 为 `1229DC5E950CDAFFC4B91D661C198AA4863576C8CEC5F7516BD50052D909E102`。`familiar_greedy` 仪式时长补为 600 tick，英中 dummy 提示已说明完整升级行为以及原生单槽装备时的 5 格磁吸。
- 新增 9 项 `GreedyUpgradePortContractTest`，覆盖一次性升级、属性、同步/NBT、副手样本与运行期目标、任意玩家交互顺序、方块搜索生命周期、拾物容量与反磁吸、旧缓存链清理、模型/渲染/纹理、三页辞典、图标、生成器、600 tick 仪式及双语边界。

## 本轮新增：Dragon Familiar 静态闭环

- 新增独立 `EntityDragonFamiliar`：尺寸 `1.0 x 0.8`、30 生命、25 护甲、10 护甲韧性、0.3 移速、6 攻击，免疫火焰和摔落并在下落时把垂直速度减半；生成时按 10% Fez、50% Ears、50% Arms 随机 variant，木棍状态、variant、Blacksmith 升级和贪婪计时均可保存，其中旧版四个 Boolean NBT 仍能读回。
- AI 优先级按 1.81.4 接入：P0 Panic/Swimming、P1 Sit、P2 Watch、P3 在同主人 Greedy 乘客存在时替其拾物、P4 Fetch stick、P5 FollowOwner、P6 未升级火焰攻击或升级后投剑、P7 Wander、P8 `EntityAIFollow`。Greedy 同时新增 P3 `RideDragonGoal`，只选择 5 格内同主人、未坐下且无乘客的 Dragon，骑乘后由 Dragon 承接容量感知和反磁吸拾物。Fetch 会在消费前重新验证缓存的木棍栈；消耗后空栈立即 `EntityItem.setDead()`，有剩余则调用 `setItem` 触发同步，阻断多个 Dragon 缓存同一物品实体造成的复制窗口。
- 任意玩家交互保持上游顺序：Dragon 嘴中有木棍时先返还；否则金粒使 `greedyTimer` 累加 6000 tick；否则潜行空手触发 40 tick 的 `purrr`/`呜呜` 抚摸文字；最后才进入主人空手坐姿或默认实体交互。熟魔戒捕获由 `ItemFamiliarRing.itemInteractionForEntity` 独立处理。Dragon 每 10 tick 给 8 格内主人刷新 300 tick `dragon_greed`：默认 amplifier 0，喂过金粒时为 1；击杀经验在原掉落非零时增加 `1 + amplifier`，即额外 1 或 2 XP。
- 未升级攻击复用 Devil 的主人受击/攻击目标与 4 点、4 秒点燃契约；一次 Blacksmith 升级后改为 `EntityThrownSword`。飞剑尺寸 `0.5 x 0.5`、无重力、100 tick 后清理，以主人归因造成 6 点投掷伤害，并过滤主人及同主人使魔；独立 renderer 使用铁剑物品并每 tick 绕 Z 轴旋转 20 度。飞剑实现 Forge 1.12 的 `IThrowableEntity`，使生成包同步主人和初速度；主人或同主人使魔碰撞在客户端、服务端均直接早退并保持飞剑存活，避免飞剑从 Dragon/Greedy 重叠碰撞箱内生成时立即自毁。
- 客户端迁入 42 部件模型、木棍层、尾部铁剑层、坐姿/行走/飞行/翼展/攻击/party/variant 动画和抚摸文字。实体纹理与书图标 SHA-256 分别为 `D9EBEBB1946AE26BE89E94F0EC5BA158024F7D2E5E1C64E48C897D7845A58D81`、`0027F8176F5EE4F96F4120AAC29D600A59945898A97CB2CF0F0E2F4702AF764D`；42 个部件的父子、UV、box、pivot、rotation 和 mirror 与固定上游机械比对无差异。上游 1.81.4 实际 renderer 未使用动态眼色 getter，本地按实际输出保留普通纹理眼色，不伪称动态彩眼。
- 新增 Djinni 阶级 `familiar_dragon` 仪式：`possess_djinni`、1200 tick、马类献祭、岩浆桶/打火石及煤块、石英块、金块、火药、两份黑曜石共八个槽位；标签经 1.12 OreDictionary 解析。ritual dummy、双语文本、Dictionary entity/ritual/description 三页、生成器白名单、GUI fallback、熟魔戒 owner 门控和捕获前解除乘客均已接入；Vemerion 2021 MIT 模型/纹理/行为署名已加入 `THIRD_PARTY_NOTICES.md`。
- 新增 12 项 `DragonPortContractTest`，覆盖属性、同步/NBT、交互顺序、贪婪经验、缓降/免火、AI、Greedy 骑乘、飞剑生命周期/伤害/友军过滤源码契约、`setThrower` 与主人友军早退直接调用、无世界 NBT、注册顺序、戒指、42 部件模型、renderer、纹理哈希、精确仪式、三页辞典和双语边界。
- 明确保留两项边界：原版 Curios API 未引入，Dragon 改用本地原生单槽戒指效果；固定上游的 `DRAGON_RIDE` advancement 自身没有可达触发路径，本地不宣称完成。Dragon 现已随 16 类 Familiar 接入统一死亡碎片路径，但该路径尚未在真实游戏世界中人工执行。

## 本轮新增：Fairy Familiar 静态闭环

- 新增独立 `EntityFairyFamiliar`：尺寸 `0.6 x 1.0`、18 生命、15 护甲、10 护甲韧性、0.3 移速、0.4 飞行速度和 6 攻击，免疫火焰及摔落并保持无重力。1.12 `EntityTameable` 缺失 `FLYING_SPEED` 与 `ATTACK_DAMAGE` 的真实构造问题均通过显式属性注册修复；牙齿、花朵与左利手状态可同步并随 NBT 保存。
- 引入通用 `IFamiliar`，并令 `IBlacksmithUpgradable` 继承它；Blacksmith、Devil 与当前 12 类升级目标因此能被统一识别，仪式鹦鹉保留标记兼容。Fairy 只追踪主人最近攻击的非玩家、非 Familiar 目标；每 10 tick 造成 3 点主人归因伤害、施加 40 tick 缓慢 II，并治疗主人及 7 格内同主人使魔 3 点。复核已删除 1.12 `EntityAIAttackMelee` 之外的重复计时递减，避免错误缩短为约 5 tick。
- Fairy 每 5 tick 扫描 10 格内同主人使魔，按燃烧、入水、下落、长距离寻路或受击状态分别给予 200 tick 抗火、水下呼吸、自定义 `fairy_slow_fall`、速度或抗性；每目标独立冷却 200 tick。1.12 缓降兼容会把下降速度钳制到不低于 `-0.125` 并清空摔落距离。
- `LivingDeathEvent` 可由主人 10 格范围内、未坐下且冷却结束的 Fairy 救下同主人使魔：排除 Guardian/Fairy 和绕过无敌的伤害，取消死亡后恢复到 2 生命并给予 100 tick 吸收 III，救援冷却 400 tick。死亡事件发生时目标生命已经归零，因此已删除错误的 `isEntityAlive()` 前置检查；Fairy 本身按固定 1.81.4 不可被 Blacksmith 升级。
- 客户端迁入 36 部件模型、悬浮/飞行/坐姿/party/魔法/支援动画和睡眠覆盖层。普通、睡眠实体纹理及书图标 SHA-256 分别为 `22E4812971D73EB4F383390EDDDEFE2B23FC19008A1671F9A0935929C0F44366`、`A116AE3458A09289E757BCAD3B0B7B5C1ED3B8539EAB134BB772A26640E526CF`、`5505CB996E791B04EF24252A551BD1D17093A2A1A244BE78D0136E2AB69C36EA`，与固定上游一致。终审修复了现代 `PoseStack` 到 1.12 renderer 时的矩阵顺序差异，先平移相机相对坐标再执行局部魔法旋转，避免把实体世界位置绕原点旋转。
- 新增 `familiar_fairy` 仪式：Djinni 附身法阵、600 tick、马类献祭、两个金苹果、恶魂之泪、三份火药与龙息；ritual dummy、owner-only 熟魔戒、双语文本、Dictionary entity/ritual/description 三页、GUI fallback 和 Vemerion 2021 MIT 根目录/打包内声明均已接入。`FairyPortContractTest` 的 9 项契约覆盖精确仪式、真实构造、属性/NBT、攻击/支援/救援、戒指/飞剑友军、模型/renderer/纹理、辞典与双语边界。
- 原生单槽已补上 Fairy 的戒中 tick：储存期间会递减救援冷却，且死亡事件会查询装备戒指；世界攻击、支援、魔法环绕和完整释放行为仍只完成静态/Bootstrap 验证，尚未在真实世界中人工执行五类支援、致死救援或戒指捕获/释放。

## 本轮新增：Chimera、Goat、Shub Niggurath、Spawn 与 Beholder 生存链

- 在旧实体 ID `0-19` 之后追加 Goat `20`、Shub Niggurath Familiar `21`、Shub Niggurath Spawn `22`、Beholder `23`、Chimera `24`，避免改写既有存档映射。新增 `familiar_beholder` 与 `familiar_chimera` 两条直接仪式及 ritual dummy 后，固定上游 16 条 Familiar 仪式已达到 `16/16`；Goat、Shub 与 Spawn 按 1.81.4 作为派生生存链，不伪造直接召唤仪式。
- `EntityChimeraFamiliar` 保留 20 生命、基础 4 攻击/0.25 移速、五类外观与 `0-100` 成长值；喂狼喜爱的肉类按营养成长并治疗 4 点，满成长时尺寸、攻击和移速分别达到 2 倍、7、0.33。成长超过 80 后可由主人骑乘并通过 1.12 `IJumpingMount` 跳跃；狮、羊、蛇三头近战分别点燃 4 秒、击退 2、施加 10 秒中毒，金苹果可把羊头分离成继承 ring、beard、size 与 owner 的 Goat。48 部件模型、骑乘/成长/三头攻击动画、64x32 纹理、熟魔戒和辞典四页均已接入；Chimera 按固定快照不实现 Blacksmith 升级。
- Goat 与 Shub 均为 30 生命、15 护甲、10 护甲韧性、6 攻击的独立可升级 Familiar。主人可在森林中按任意顺序给 Goat 使用黑色染料、燧石和末影之眼，使其转化为 Shub Niggurath；转化严格只传递 ring、beard、owner 与位置。Blacksmith 升级后，Goat/Shub 受实体伤害会发出 1.12 的音符盒竖琴替代警铃，并让 30 格内怪物把主人设为攻击目标。
- Shub 会骑乘同主人、未坐下且无乘客的 Cthulhu，并稳定面向坐骑；每实际 `202 tick` 生成一只非 Familiar 的 Spawn。Spawn 保存 creator 与寿命，实际 `402 tick` 后或每 10 tick 检测到近敌时以主人归因造成 6 点范围伤害，导航实际每 `22 tick` 刷新。为忠实于固定源码，距离平方仍直接与原始 range 比较，revenge/last-attacked 重复引用可造成两次伤害，普通受击死亡只发粒子而不范围爆发，Shub 的 `spawnTimer` 也不写入 NBT。
- Beholder 为 30 生命、15 护甲、10 护甲韧性、6 攻击的可升级 Familiar：随机给主人 10 格内非玩家实体施加 60 秒发光，追踪主人的 revenge/last-attacked 目标并在 23 tick 蓄力后用四眼同步表现基础 9 点的主人归因射线；吞食 3 格内 Spawn 后获得 10 分钟力量 I 与速度 I。固定源码的 `20 tick` 冷却短于 `23 tick` 蓄力会令射线永不可达，本地以 pending volley 锁阻止覆盖计时，同时保留每 tick 冷却递减；Blacksmith 升级后，10 格内主人免疫失明，但不扩展到后期 Darkness/Warden 语义。
- 客户端新增 Goat/Shub/Spawn 的 36/51/25 部件模型、Beholder 的 39 部件四眼模型和睡眠/眼镜/吞食缩放层，以及对应五个 renderer。Goat、Shub、Spawn、Spawn blink、Beholder、Beholder sleep、Chimera 纹理 SHA-256 依次为 `321F13B1DF0CAA34DB5E014413868C044629488A69A7E66B7C95D9490DB1CB57`、`0C446E96DD2FBB8ADABF5446292F4C1AD99DA88EC661D2FCE6632E481EC96C21`、`71A89685E0A17B977813C246F07F7560882BC3218F77E43F1962CB4A26756699`、`35E3B4908701F4919218480AA0ED0F404404D68DD524AD0DC2AA29937B830BC9`、`4633EA7BB0D6FAE0E73769350FA2FC2E1C28CB71E8F177A9F3A3E8A2400B13E5`、`BBC0EE90CFF092E29D397059A65FB2D38ECCE0C002BC9DC21677D9CE0156E7CC`、`A362B81171EA53C64C5F926ECAD9C7ACA132F4DC803510BC3DD6D39B3DA3318B`，均与固定参考快照一致。
- 熟魔戒支持 owner-only 收纳 Goat、Shub、Beholder 与 Chimera，明确排除临时 Spawn；Dictionary 新增 Shub/Beholder 各三页、Chimera 四页及 GUI fallback，中英文与两份 `THIRD_PARTY_NOTICES.md` 已同步。新增 `GoatShubSpawnPortContractTest` 7 项、`BeholderPortContractTest` 6 项、`ChimeraPortContractTest` 6 项；这些验证覆盖注册、仪式、属性/NBT、固定计时语义、模型/纹理、辞典和戒指边界，但不替代真实世界中的转化、生成、吞食、射线、骑乘和三头攻击回归。

## 本轮新增：Familiar 灵魂碎片与复活闭环

- 新增共享 `EntityFamiliarBase`，16 类本地 Familiar 统一从 1.12 `dropLoot(...)` 生成恰好一枚普通 `soul_shard`：同维度在线主人存在时使用 Forge `ItemHandlerHelper` 交付，否则在尸体上方掉落并设置 5 tick 拾取延迟、清零 X/Z 动量。该路径与固定上游直接生成物品实体的边界一致，不受 `LivingDropsEvent` 取消影响；vanilla Parrot/Otherworld Bird 路线和临时 Shub Spawn 不纳入，Demonic Husband/Wife 继续使用各自独立的同语义路径。
- 新碎片改写固定上游 canonical `entityData` schema；读取仍兼容旧 `OccultismEntity`、`EntityId`、`EntityName` 三键。序列化只临时把实体 Health 设为满值并在 `finally` 恢复，保留 UUID、OwnerUUID、Motion、Rotation、ActiveEffects、variant、升级和实体专属状态；使用无条件 `writeToNBT`，骑乘船/矿车时也不会因 1.12 `writeToNBTOptional` 的限制丢失碎片。canonical tooltip 会由注册 ID 解析本地化实体名。
- Guardian 的唯一基类碎片会把碎片内生命随机为 `1..5`，并在写入后恢复死亡实体的原 lives；最后一条守护生命耗尽后改为进入实际死亡/掉落流程。固定 1.81.4 的 Guardian override 字面上先由父类生成一枚、再自行生成一枚，属于明确的复制缺陷；本地有意修正为恰好一枚并由测试固定，不宣称逐行复刻该 bug。
- Headless 删除私有 `onDeath`/`droppedSoulShard` 和碎片前强制 `dead=false`、`rebuilt=0` 的旧路径，改由共享基类保存真实 head、headTimer、weapon、rebuilt、variants 和升级状态；实际扣血后的死亡状态转换已移到 `damageEntity`，保证致死一击也会在 shard 序列化前写入待重建状态。
- `resurrect_familiar` 已校正为固定快照的 `summon_foliot` 法阵、15 秒和四份 `otherworld_essence`。复活只从副本 NBT 移除 `Pos`，保留其他状态；成功生成后仅对本地 `IFamiliar` 把仍在线的施法者设为主人，Demonic Partner 保留碎片中的原主人。空/损坏碎片校验与生成失败退还仍作为本地健壮性修正保留。
- 裂魂附魔现在拒绝 `IFamiliar`，避免击杀 Familiar 同时生成普通 shard 与 `soul_shattered`，也避免旧右键模拟死亡路径再产一枚普通 shard；易碎灵魂右键已改为只执行一次 1.12 原生死亡掉落流程，关闭原先的双倍基础掉落。辞典解锁、两种法阵 uses、中英文指南和 ritual dummy 提示已同步。
- 新增 12 项 `FamiliarSoulShardPortContractTest`，覆盖 16 类继承矩阵/排除类型、公共属性、持久化/despawn、墙体碰撞免疫、Bound Spirit/Foliot 墙体免疫与非玩家/非箭矢伤害衰减、唯一掉落路径、canonical 与 legacy codec、满血完整 NBT、骑乘序列化契约、Guardian/Headless 缺陷修正、施法者复绑定边界以及精确仪式/辞典/双语数据。验证仍以 Bootstrap、真实原版实体 NBT 和源码/资源契约为主，不等同于世界内的满背包、离线/跨维度主人、实际死亡与仪式执行回归。

## 本轮新增：Wild Hunt 战斗代码与资源静态闭环

- 新增 append-only 实体 ID `25/26`：`EntityWildHuntSkeleton` 与 `EntityWildHuntWitherSkeleton`，分别为 20 生命/4 攻击和 60 生命/6 攻击，尺寸均为 `0.6 x 2.9`；客户端直接复用原版 `RenderSkeleton`/`RenderWitherSkeleton`，无需新增实体模型或纹理。Wither Skeleton 在 1.12 原生初始化后会明确恢复 6 攻击，并取消原版继承来的全火焰免疫。
- 每只首领生成 3-8 只命名随从，随从先执行原版装备逻辑，再以 70% 概率换成石剑；仪式固定生成 3 只首领，总规模为 12-27 个实体。首领在自己的随从列表清空前免疫普通伤害，`OUT_OF_WORLD` 与创造玩家伤害可绕过；任意 Wild Hunt 成员的直接来源或真实来源伤害均被同阵营免疫。两类实体只抑制日晒点火和和平难度自动清除，不获得额外普通火焰免疫；仪式生成时会显式启用 1.12 persistence，避免命名实体因距离被正常清除。
- `wild_hunt` 已迁移为 canonical `summon_wild_hunt`：专用 `occultism:summon_wild_hunt` 类型、600 tick、骷髅头启动/凋灵骷髅头展示结果、六种固定材料、`humans` 献祭及 11x11 `summon_wild_greater_spirit` 法阵。专用执行分支从 JSON 读取实体类型、经过 1.12 `SpecialSpawn` 事件、按高斯偏移生成三只首领，并通知 50 格内玩家的原版 `SUMMONED_ENTITY` trigger；配方加载器同时把该专用类型的 JEI/调试元数据固定为 3 首领，JSON 无需引入上游不存在的 `summon_number`；旧存档中的 `ActiveRitual=wild_hunt` 会迁移到新 ID。
- 修复了两个 1.12 生成取消边界：即使三个首领都被 `EntityJoinWorldEvent` 取消，专用分支也会把配方结果标记为已处理，不会把仅供展示的凋灵骷髅头免费掉出；随从只有在 `spawnEntity` 成功后才加入首领护盾列表，避免被取消的随从造成永久免疫。旧 `ritual_dummy/wild_hunt` 保留注册 ID 但从创造栏隐藏，新增 canonical `ritual_dummy/summon_wild_hunt` 追加到物品数组末尾。
- 新增两张结构化 1.12 loot table：随从掉箭和骨；首领掉煤、骨，并仅在玩家击杀时按上游 `0.67..1.0` 映射生成 metadata 1 的骷髅头，再受 Looting 影响。1.12 不存在 Rib Armor Trim，因此不伪造该掉落；Iesnium 屠刀的通用 15% 额外头颅逻辑仍可能在此表之外追加头颅。恶魔之梦食物现可治疗两类 Wild Hunt 实体。
- 新增通用 1.12 `RitualTrigger` 与隐藏 `occultism:occultism/summon_wild_hunt` advancement；成功完成专用仪式时会按 `occultism:ritual/summon_wild_hunt` 条件授予。Dictionary 新增 `summon_wild_greater_spirit` 三页和 `possess_warden` 三页并更新 Wither Skull 配方引用，现为 9 类、203 条目、717 页、20 个法阵条目；生成器支持本地 entry override/fallback，并用本地 resurrection override 保留此前 `summon_foliot` 解锁修正。
- 新增 11 项 `WildHuntPortContractTest`，通过 Bootstrap 实体构造及源码/资源契约覆盖注册/renderer、属性、随从数量和装备、友军免疫与首领护盾、生成取消、日照/和平实现、UUID NBT 主从往返与已加载实体重连、掉落表、canonical 仪式/法阵/dummy、JEI/调试数量元数据、双语/辞典和隐藏进度。当前仍没有真实世界中的完整仪式、12-27 实体战斗、实际掉落、区块卸载或存档重载回归。
- 本轮新增 `WildHuntMaster`/`WildHuntMinions` UUID NBT，并每 20 tick 在当前 `WorldServer` 的已加载实体中重连主从关系；不会为关系恢复强制加载区块，目标实体不存在时保留有限引用以等待后续加载。随从从世界移除时通知首领。该历史批次的自动献祭已由后续玩家击杀事件实现替代，`humans` 现匹配村民/玩家；不会主动伤害玩家。法阵检测器仍允许高阶粉笔替代低阶粉笔，声音/粒子等视觉差异继续单独记录。

## 本轮新增：十类专用附身实体、Hoglin/Warden 兼容实现与通用仪式收尾

- 在 Wild Hunt 的 append-only ID `25/26` 之后追加 `27-34`：`EntityPossessedEndermite`、`EntityPossessedSkeleton`、`EntityPossessedEnderman`、`EntityPossessedGhast`、`EntityPossessedWeakShulker`、`EntityPossessedShulker`、`EntityPossessedElderGuardian` 与 `EntityPossessedPhantom`。前七类实体沿用对应 1.12 原版 AI 和 renderer，但使用独立注册 ID、固定上游属性/火焰语义及专用 loot table；Skeleton 另有 30 生命、4 攻击和日晒免疫，Weak/普通 Shulker 为 10/40 生命，Elder Guardian 为 160 生命且尺寸 `2 x 2`。Ghast、两类 Shulker、Elder Guardian 和 Phantom 免火；`PeacefulRemovalGuard` 只用于 Endermite、Skeleton、Enderman、Ghast、Elder Guardian 与 Phantom，两类 Shulker 的 1.12 继承路径无需该兼容层。
- Phantom 在 1.12 中没有原版基类，新增 `EntityPossessedPhantom` 的无重力盘旋/俯冲攻击循环、免火、免日照和和平难度保护；`ModelPossessedPhantom`、`RenderPossessedPhantom`、专属 64x64 纹理与 `occultism:phantom_membrane` 物品/loot table 配套接入。上游 `clientTrackingRange(16)` 的单位是区块，1.12 Forge 注册参数按 16 blocks/chunk 换算为 `256` blocks；仪式时长 `600` tick，`occultism:flying_passive` 在 1.12 严格匹配 Bat/Parrot（Bee/Allay 无对应原版实体）。
- `possess_endermite`、`possess_skeleton`、`possess_enderman`、`possess_ghast`、`possess_weak_shulker`、`possess_shulker`、`possess_elder_guardian`、`possess_phantom` 已从原版替代 ID 改为上述专用实体；`possess_hoglin` 与 `possess_warden` 另接入专用 1.12 兼容实体，注册 ID 为 `35/36`，Warden 上游 48 chunks 换算为本地 768 blocks tracking range。通用配置实体召唤现在先发送 1.12 `SpecialSpawn`，仅在事件未处理时调用 `onInitialSpawn`；召唤/职业/高阶灵体/天气时间等非物品结果统一由运行时持有，展示用 skull、soul gem、chorus fruit、shulker shell、nautilus shell 等不会在最终 `spawnEntity` 失败时作为免费结果掉出。craft 仍掉正常结果，repair 与 Familiar resurrection 仍由各自优先分支处理。
- 通用 `occultism:ritual` trigger 现在从 `completeRitual()` 统一触发一次；旧 Wild Hunt 专用重复调用已移除。新增 `possess_skeleton`、`possess_endermite`、`possess_enderman`、`possess_ghast` 四条隐藏 advancement。高阶工人也未被此次分流破坏：tier2/3/4 crusher、crystallizer、smelter 是 `SpiritJobType` 的 alias，`isFoliotJob()` 会让它们在通用 tier 分支之前生成带对应 tier 的 `EntityFoliotJob`；Djinni Gambler 同样保留 max age 和绑定呼唤之书。
- 十张 1.12 Possessed loot table 已纳入 `LootTableManager.GSON_INSTANCE` 与 `ForgeHooks.loadLootTable` 反序列化契约。Skeleton 至少一枚 skull，Endermite 至少一块 end stone，Enderman/Ghast 至少一枚 pearl/tear，Weak/普通 Shulker 保留固定与概率池，Elder Guardian 保留原版池、玩家击杀 wet sponge 和本地 nautilus shell `2-4`；Phantom 保证 `1-4` 个 Phantom Membrane，并受 Looting 增加 `0-1`；Hoglin 使用猪排/皮革兼容池，Warden 使用 echo dust 与 1.12 唱片替代池。Elder Guardian 的 2.5% + Looting 稀有鱼池按 1.12 原版 fishing/fish 的 `60/25/2/13` 权重展开四种 metadata，并为每种保留燃烧熔炼函数。1.12 不存在的 armor trim、heart of the sea、wind charge 和 Echo Shard 未伪造。前八类直接复用原版模型/纹理，Phantom、Hoglin、Warden 使用新增专用模型/纹理或兼容模型；本轮全量契约测试已重新运行并通过。
- 通用配置实体、职业灵体、通用高阶灵体和 Wild Hunt 首领现在统一经过 1.12 `ForgeEventFactory.doSpecialSpawn`，事件未处理时回退到 `onInitialSpawn`，并按固定上游顺序在调用 `world.spawnEntity` 前通知 50 格内玩家的原版 `SUMMONED_ENTITY`；复活路径仍保持完整 NBT 恢复后直接加入世界。Demon's Dream 白名单已补齐固定上游 tag 在本地可表达的 Foliot/Bound Spirit、Wild Hunt、三类已移植 Possessed、全部本地 Familiar、Parrot、Shub Niggurath Spawn 与 Demonic Partner，治疗量校正为 Datura `2`、Demon's Dream Essence `10`、Otherworld Essence `20`。
- Dictionary GUI 已删除十个 canonical ID 的原版 fallback，十条 ritual dummy tooltip 已改为专用实体行为；Endermite/Enderman/Shulker/Phantom/Hoglin/Warden 掉落文字已与实际表一致，并明确 Minecraft 1.12 没有现代潜影贝受弹增殖机制。总览页现将八类原版基类兼容实体、Hoglin/Warden 专用兼容实体、专用 Parrot 子类 Otherworld Bird 和仍保留的 Witch/Blaze/Wild Horde 原版替代分开说明。

### Possessed Hoglin：已接线的 1.12 兼容实现

- 本地新增 `EntityPossessedHoglin`，追加注册到 `ModEntities`，并接入 `possess_hoglin` 仪式、`possessed_hoglin.json` 掉落表、专用 `ModelPossessedHoglin`/`RenderPossessedHoglin` 和实体辞典页面。它使用 1.12 的基础敌对近战目标，保留上游关键外形参数：`66` 最大生命、`12` 攻击、`1.88 x 1.88` 尺寸和火焰免疫；独立的 `possess_zombified_piglin` 仪式仍是 Zombie Pigman 的另一条兼容路线。
- 这不是现代 Hoglin 的行为等价移植。上游 `PossessedHoglinEntity` 只继承原版 Hoglin，因而实际依赖 Hoglin 的 Brain AI、击退/攻击节奏、Warped Fungus 回避以及离开下界后的 Zoglin 转化；本地 1.12 用经典红/棕蘑菇作为可放置回避标记，并在非维度 `-1` 累计 300 tick 后替换为原版 Zombie Pigman，同时保留位置、动量、名称和生命比例。1.12 端仍无法表达现代 Brain、真正 Zoglin 属性、下界生物群系规则或 Warped Fungus 身份。
- 掉落也明确降级：上游一池一 roll 的权重为 empty `3`、Netherite Upgrade Smithing Template `2`、Snout Armor Trim `1`、Netherite Scrap `3`；本地表改为猪排 `1-3` 和皮革 `0-2` 两个带 Looting 的 1.12 池，现代模板/装饰纹样没有可用等价物。上游另有手持 Occultism 刀击杀 Hoglin 时追加 `4` Tallow 的 loot modifier，本地 `PlayerEventHandler` 已为 `EntityPossessedHoglin` 接入该数量规则。
- 上游注册规格为 MONSTER、火免疫、尺寸 `1.88 x 1.88`、tracking range `24` chunks，renderer 直接使用 `HoglinRenderer`；本地 Forge 注册参数换算为 `384` blocks，renderer 使用紧凑的 1.12 自定义模型并复用猪纹理，导航采用 1.12 AI 目标，并新增蘑菇扫描/回避和僵尸猪人转换状态 NBT。该项目前有源码/资源契约覆盖，但尚无真实世界的仪式执行、AI、转化、掉落概率、刀具 Tallow 掉落或区块重载回归。
- `PossessedEntityPortContractTest` 现为 10 项，新增 Hoglin 蘑菇回避替代、维度边界、300 tick 转换计时、Zombie Pigman 转换契约和计时 NBT 往返/坏值钳制；同时覆盖十类 append-only 注册/renderer、真实 Bootstrap 构造/属性、和平与日晒兼容、八条上游基类仪式及 Hoglin/Warden 两条兼容仪式、Phantom tracking range 与飞行循环、Warden 脉冲/失明源码契约、统一初始化/结果所有权/单次 ritual trigger、普通召唤 `SUMMONED_ENTITY`、Demon's Dream 完整白名单和三档治疗量、四鱼种权重、十张 loot table 的 1.12 反序列化、Dictionary fallback/双语文字与四条隐藏进度；最终全量结果已重新验证。测试没有创建真实 `WorldServer`，也未覆盖实际仪式、和平切换、掉落概率统计、治疗交互、renderer 加载或 Dictionary 实体预览。

## 本轮补充：Familiar 公共基线、Familiar Ring 与高阶实体审计

- 本节汇总固定上游 `84b8ad4` 与当前 1.12 代码的公共行为补充及差异审计。当前 `ItemFamiliarRing` 已支持捕获、旧三键 NBT 储存、释放、释放者 owner 重绑定和释放时名称恢复；依赖硬编码实体清单。捕获前会强制解除骑乘并移除乘客，随后以 `writeToNBTOptional` 写入且删除 `Pos`、`Motion`、`Rotation`，因此不保留骑乘关系、位置、动量或朝向。新增 `familiarType` loot 标签的服务端惰性转换：合法类型写入 canonical 实体 NBT 和随机 BoundSpiritName，未知类型保留以便重试；tooltip 追加上游风格的类型行。运输实体仍是基本容器，但已支持填充戒指进入原生玩家单槽。
- `IFamiliar` 现已补齐 `getFamiliarEffects()`、`curioTick()`、`isEffectEnabled()` 与默认升级契约；Bat、Beaver、Cthulhu、Deer、Devil、Dragon、Mummy 已提供上游对应的 fresh potion effects。1.12 新增 `FamiliarSettings` 玩家 capability：17 类本地 Familiar（含 Otherworld Bird）默认启用，支持 NBT 序列化、死亡 Clone、登录/重生/换维度同步和固定顺序服务端切换包；客户端新增 18 个默认未绑定的 Controls 按键，通过 `ClientRegistry` 注册并在无 GUI 时发送固定顺序布尔数组，英中状态文案齐全。上游没有独立 Familiar Settings GUI。
- 固定上游的完整戒指语义依赖 Curios 装备态：每 tick 驱动戒中 Familiar，每 20 tick 按玩家设置刷新效果。当前本地以 `FamiliarRingCapability` 提供一个持久化原生单槽，戒中实体延迟从 NBT 加载并按 20 tick 写回；`FamiliarUtil` 已统一附近实体与装备戒指查询，Guardian/Fairy/Headless/Blacksmith、Greedy 磁吸、Beholder 发光和 Fairy 冷却等路径已接入部分戒中行为。潜行空手取下通过客户端请求包、服务端校验并同步；普通背包戒指不会生效。客户端 foil、类型本地化和非 `IFamiliar` 运输实体的装备边界已完成；仍缺部分事件和真实世界回归。
- 16 类本地 Familiar 的 owner、坐下和跟随主路径已由 `EntityTameable` 与各实体 AI 覆盖；`EntityFamiliarBase` 现统一提供 30 生命、15 护甲、10 护甲韧性、0.3 移速、6 攻击的上游公共属性基线（Chimera、Dragon、Mummy 保留各自覆盖），并在构造时持久化、禁止 despawn、免疫 `IN_WALL`/`FLY_INTO_WALL`。Bat、Beaver、Cthulhu、Deer、Devil、Dragon、Mummy 的世界内主人效果也已有各自实现。
- `EntityBoundSpirit` 与 `EntityFoliotJob` 已补齐同样的墙体碰撞免疫；来自非玩家且非箭矢的伤害按 `(amount + 1) / 2` 衰减，箭矢和玩家伤害保持原值。升级 Beholder 的失明免疫现在通过 `FamiliarUtil.hasFamiliar` 同时查询附近实体和原生装备戒中的实体。剩余缺口集中在 Familiar Ring 的完整事件覆盖和真实世界回归，而不是这些公共基线。
- 高阶/野性实体审计中最明确的 `wild_hunt` 降级点已在本轮关闭：专属双实体、三首领/随从群、护盾与友军免疫、和平/日照兼容、专用掉落、法阵、仪式、隐藏进度以及主从 UUID 持久化/已加载实体重连均已接入。剩余风险集中在真实世界战斗、区块重载回归、通用献祭模型和法阵粉笔替代规则，而不是原版单体替代。
- Djinni、Afrit、Marid 目前不是三套本地专属实体，而是单一 `EntityBoundSpirit`/`EntityFoliotJob` 按 tier 参数复用。完整上游 `SpiritEntity` 还耦合 owner、库存/filter capability、工作区、Job/Brain、容器、Calling Book 和存档协议；不能只拆三个注册名或模型就把高阶灵体标记为完成。

### 本轮新增：Storage Controller 动态容量第一阶段

- `TileEntityStorageController` 已从固定 54 槽改为新实例基础 128 槽；Tier 1/2/3/4 stabilizer（含 dark 变体）分别增加 256/512/1024/2048 槽，Tier 0/Tier 5 保持 0 增量。服务端每 20 tick 从控制器上方起点沿六向扫描最多 5 格，按方块身份识别首个 stabilizer；当前 1.12 blockstate 没有上游 `FACING` 属性，因此暂不伪造方向状态。控制器自身和 Storage Remote 会被库存黑名单拒绝，避免递归存储。
- NBT 新增 `StorageSlotCount`，`Items` 使用 int `Slot` 写入并兼容旧 byte Slot；缺少容量标签的旧 54 槽数据仍可加载，若物品位于更高槽位会自动扩展 active 容量。缩容只降低可访问槽位，backing 列表保留隐藏物品，重新扩容后恢复，便携控制器物品的写入、放置和 tooltip 统计也不会截断高槽位内容。
- `ContainerStorageController` 现在按运行时容量建立全部槽位，shift-click 边界随容量变化；客户端 GUI 保留六行固定视口，使用滚轮、上下键和 PageUp/PageDown 滚动动态槽位，玩家背包坐标不再被 128+ 槽位推到窗口外。稳定虫洞仍将 `ITEM_HANDLER` 转发到已加载目标控制器。

### 本轮新增：Storage Controller 虚拟快照、搜索与排序基础

- 控制器新增大小写不敏感的显示名/注册名搜索，按数量、名称或模组排序，并支持升/降序；快照会先聚合同类物品，不改变真实物理槽位内容。
- 新增 `StorageSortType`、`StorageSortDirection` 及搜索词/排序设置 NBT 往返；物理槽位排序仅作用于当前可见容量，隐藏扩容槽位保持原内容，避免缩容时丢失物品。
- 这仍只是上游虚拟终端的本地第一阶段：已实现本地物理槽聚合、映射、跨槽提取、客户端 `ClientStorageCache`、有界分片栈同步和第一阶段终端合成；服务端控制器现在懒构建并复用 `MessageUpdateStacks`，所有 `markDirty`/容量变化/NBT 读取路径都会失效，避免重复压缩或发送过期列表。本轮已补齐容器级瞬时 recipe lock 和同维度已加载的 linked machine/order 最小路由，但仍未实现完整跨网络虚拟缓存。

### 本轮新增：`summon_wild_parrot` 仪式

- 新增 canonical `occultism:summon_wild_parrot`：绑定魔精书、鸡献祭、羽毛/四色染料/鸡蛋材料，召唤未驯服的原版 1.12 `EntityParrot`；同步加入 ritual dummy、模型、英中文本和契约测试。
- 该仪式保留原版 1.12 `EntityParrot` 作为“Wild Parrot”独立路线；canonical Otherworld Bird 使用另一个专用 `summon_wild_otherworld_bird` 仪式。原版 Parrot 与 Otherworld Bird 的正常死亡都不接入本地 Familiar 灵魂碎片复活链。

### 本轮新增：Storage Controller 超大堆叠第二阶段

- `ModConfig.storage` 新增 `controllerStackSize=1024` 与 `overrideItemStackSizes=true`；自定义 `IItemHandlerModifiable` 取代 `InvWrapper` 的 64 槽上限，支持单槽最多 1024 个物品，并在关闭覆盖时按物品原始最大堆叠数限制（例如末影珍珠为 16）。
- Storage GUI 使用专用 `StorageSlot`，玩家 shift-click 进入控制器时通过 handler 合并，避免 vanilla `Container.mergeItemStack` 按 64/物品原始上限截断；现有远程访问和稳定虫洞 capability 继续使用同一 handler。
- 1.12 原生 `ItemStack` 的 `Count` 字段是 byte，控制器 NBT 额外写入 `RealSize` int；读取时先构造兼容的非空栈再恢复真实数量，1024 数量、旧 byte Slot 和旧无 `RealSize` NBT 均有契约测试覆盖。

### 本轮新增：Storage Controller 设置同步与 1.12 GUI 控件

- 新增 `MessageUpdateStorageSettings`：客户端发送搜索词、排序类型和方向，服务端只从当前打开的 `ContainerStorageController` 解析目标，不接受任意方块坐标；普通控制器要求 8 格可用距离，Remote 要求背包中存在有效绑定，稳定虫洞要求附近且指向同一已加载控制器。
- 服务端成功更新后执行物理槽位排序、查询过滤、`markDirty`、TileEntity 更新包和容器同步；搜索只把不匹配的非空物理槽移出视口，空槽始终保留，避免搜索状态阻断插入。过长查询在消息边界裁剪为 128 字符并统一 trim/lowercase。
- `GuiStorageController` 已加入搜索框、排序类型按钮、升/降序按钮、清空按钮和第一阶段合成面板，英文/中文键完整；当前容器按本地聚合条目映射物理槽并支持跨槽提取，且已通过 `ClientStorageCache` 与有界 `MessageRequestStacks`/`MessageUpdateStacks` 接收快照，但尚未达到上游完整网络虚拟终端结构。
- 新增 `StorageSettingsNetworkContractTest`（3 项）和 `StorageControllerGuiPortContractTest`（3 项），覆盖消息往返、权限边界、空槽可见性、GUI 控件和双语键。

### 本轮新增：Storage Controller 有界栈同步

- 新增 1.12 `MessageRequestStacks`/`MessageUpdateStacks`：客户端只请求当前打开容器，服务端重新解析并校验控制器后发送当前虚拟条目、usedSlots/maxSlots；不接受客户端方块坐标。栈列表使用 Deflater 最快级别压缩，包头和解压均有 1 MiB/8 MiB 上限、条目数/数量/尾部数据校验，避免恶意长度或解压炸弹。
- 新增本地 `ClientStorageCache`（按物品索引的延迟缓存）和 GUI 首次请求/快照应用；容器在客户端只显示快照，不把客户端聚合副本写回物理 handler。设置更新、服务器库存变化后可发送新的快照；控制器端缓存只覆盖单控制器服务端全量虚拟快照，`markDirty`、容量变化和 NBT 读取会失效，不覆盖跨网络持久缓存或客户端跨 GUI 持久缓存。`StorageStacksNetworkContractTest` 覆盖 3 项压缩超大堆叠、畸形长度拒绝、注册和缓存入口。
- 该同步只覆盖上游终端的栈列表传输；终端合成第一阶段已补入本地 1.12 容器，并由容器级瞬时 recipe lock 保护配方更新和 shift-click 批次；linked machine/order 也已有同维度已加载的最小路由，但仍不包含远程完整 capability 代理或跨维度网络缓存。

### 本轮新增：Storage Controller 终端合成第一阶段

- `ContainerStorageController` 新增 3x3 `InventoryCrafting` 和安全结果槽，使用 1.12 `CraftingManager` 匹配配方；服务端可从玩家背包、物理槽和虚拟聚合存储补充材料，并正确处理 `getRemainingItems`、容器物品返还和输出提取。
- 新增 `MessageSetRecipe` 及 `ModNetworking.sendStorageRecipe`：接受上游 `s0`-`s8` 配方模板，限制 64 KiB NBT、最多 9 个槽位和每槽 16 个候选，并在服务端校验玩家权限及当前容器后应用配方。
- GUI 扩展至 362px，加入合成面板和结果槽；支持 shift-click 批量合成和关闭容器时安全返还矩阵物品。`StorageCraftingContractTest` 覆盖矩阵布局、配方/存储补料代码路径、消息边界和 GUI 尺寸。
- 该阶段尚未接入完整 JEI/配方书界面、上游独立 order inventory 或客户端合成缓存；recipe lock 已按 1.12 容器级瞬时语义接入，shift-click 批量合成以玩家背包容量为上限。

### 本轮新增：Storage Controller 合成矩阵持久化

- `TileEntityStorageController` 新增上游兼容的 `matrix`/`orderStack` NBT/API；矩阵严格限制为 9 槽，读取兼容小写 `slot`、旧 `Slot` 以及 `RealSize` 超大堆叠数量，并同时写入普通方块实体和便携控制器 `BlockEntityTag`。
- `ContainerStorageController` 开启时恢复控制器矩阵，矩阵修改、配方转移和合成后同步回控制器；关闭服务器容器时先清除持久化副本，再按现有安全返还路径退回玩家，避免重复计算。物理 `ITEM_HANDLER` 不包含矩阵；`orderStack` 仍只有持久化/API，没有订单路由。
- `StorageControllerContractTest` 新增 4 项矩阵恢复、便携空物理库存、异常 NBT 和订单槽往返契约，并增加快照缓存复用/失效与返回列表防篡改契约，共 20 项。

### 本轮新增：Storage Controller recipe lock 与 linked machine/order 最小路由

- `ContainerStorageController` 的 `recipeLocked` 保持上游容器级瞬时语义：整个 shift-click 批次持锁，`finally` 保证异常/空结果也解锁；锁定时拒绝 `MessageSetRecipe`/配方矩阵更新，批次结束后一次性同步矩阵、结果和存储。该锁不写入 TileEntity NBT，避免重启后永久锁死；`matrix`/`orderStack` 持久化不等于持久锁。
- `TileEntityStorageController` 新增有界（最多 128 条）linked machine 注册表，提供去重、坏数据过滤、防御性副本和 NBT 往返；`EntityFoliotJob` 在配置变更、重载和死亡时注册/注销。`MessageSetManagedMachine` 仅允许当前手持、已绑定 `MANAGE_MACHINE` 书籍更新可信机器的面向/自定义名称，坐标、维度和方块注册名由服务端严格保留。
- 控制器以已加载同维度的 spirit UUID 解析 `DepositOrder`，验证控制器位置、维度和完整端点后下发到 `EntityFoliotJob`，失效路由自动清理；不执行跨维度或区块加载。linked machine NBT 持久化是 1.12 兼容策略，和上游主要依赖网络态自注册存在明确差异；`orderStack` 仍是终端持久化/API 槽，不冒充完整订单库存。
- 新增 `LinkedMachineRegistrationContractTest`（4 项）与 recipe lock 契约，覆盖 NBT/坏数据/去重/防御副本、网络包边界、UUID 路由和批次锁定。

### 本轮新增：Storage Controller Proxy API 与机器订单请求边界

- 新增 1.12 `GlobalBlockPos`（兼容上游 `pos` packed-long + `dimension` resource-id/string 与旧整数维度 ID/X/Y/Z NBT，未知维度拒绝解析，无世界/区块加载副作用）、`IStorageController` 和 `IStorageControllerProxy`，控制器暴露容量、聚合栈快照、真实 `ITEM_HANDLER`、模拟/真实插入提取、黑名单、常用物品和 linked machine API。
- `TileEntityStableWormhole` 现在实现 `IStorageControllerProxy`：目标维度或区块未加载时保留引用并返回 `null`，已加载但目标不是控制器时仅服务端清理；`ITEM_HANDLER` 仍只委托已加载的真实控制器。`ItemStorageRemote` 也已通过 ItemStack capability 附件提供同样的 loaded-only `ITEM_HANDLER` 代理，旧坐标 NBT 保留，同时写入嵌套 `linkedStorageController` 以兼容 API 形式。这是加载态委托，不是完整跨网络虚拟缓存或异步网络缓存。
- 新增 `MessageRequestOrder`：客户端只能从已打开且可交互的 Storage Controller 容器请求订单；服务端重新验证控制器身份、同维度、区块加载、机器引用和已注册 spirit，并在库存模拟匹配成功后才下发 `DepositOrder`。包体 NBT、堆叠数量、注册名和自定义名称均有界；不接受客户端坐标作为可信路由。
- `StorageProxyCapabilityContractTest`（7 项）和 `MachineManagementPortContractTest` 新增订单包边界，覆盖 GlobalBlockPos 两套 NBT schema 往返、未知维度拒绝、控制器自代理、模拟提取、未加载链接保留、包往返和空堆栈拒绝。

### 本轮新增：Familiar Trigger 与 Beholder 戒中免疫

- 新增 1.12 `FamiliarTrigger`，保留上游 26 类事件枚举、JSON `type` 解析和监听/触发语义；`ModAdvancements` 注册 `occultism:familiar`。当前 26 类均已接线：原有 `CAPTURE`、`MUMMY_DODGE`、`FAIRY_SAVE`、`GUARDIAN_ULTIMATE_SACRIFICE`、`HEADLESS_CTHULHU_HEAD`、`HEADLESS_REBUILT`、`DEER_POOP`、`BAT_EAT`、`DRAGON_NUGGET`、`DRAGON_PET`、`DRAGON_FETCH`，以及本轮新增 `RARE_VARIANT`、`CTHULHU_SAD`、`DEVIL_FIRE`、`GREEDY_ITEM`、`PARTY`、`DRAGON_RIDE`、`BLACKSMITH_UPGRADE`、`CHIMERA_RIDE`、`GOAT_DETACH`、`SHUB_NIGGURATH_SUMMON`、`SHUB_CTHULHU_FRIENDS`、`SHUB_NIGGURATH_SPAWN`、`BEHOLDER_RAY`、`BEHOLDER_EAT`、`BEAVER_WOODCHOP`。对应隐藏 advancement 已覆盖 25 个 Familiar 文件（Dragon PET/FETCH 共用一个）；FamiliarEventHandler 已移除错误的 `TERRAIN_GEN_BUS` 手动注册，改由 `@Mod.EventBusSubscriber` 走 Forge 主事件总线。
- `EntityBeholderFamiliar.onPotionApplicable` 不再只扫描世界中的 Beholder；升级 Beholder 现在通过 `FamiliarUtil.hasFamiliar` 纳入玩家原生装备戒指中的 NBT 实体，佩戴戒指时也能提供上游失明免疫。新增/更新契约测试覆盖该边界。

## 本轮新增：Wild Afrit 1.12 兼容实体与召唤仪式

- 新增末尾 append-only 注册的 `EntityAfritWild`，保留上游关键属性：100 生命、8 攻击、50 跟随距离、0.4 移速、8 护甲、50 韧性；使用 1.12 `EntityMob` 近战 AI、火焰免疫，并把 Blaze/同类 Afrit 伤害视为盟友免疫。
- `onInitialSpawn` 按上游生成 3-8 个 Blaze 随从，逐个执行 1.12 `SpecialSpawn`/`onInitialSpawn` 后再加入世界；专用掉落表掉落 `afrit_essence`，并复制上游 Afrit 普通/发光纹理。1.12 模型和 renderer 是 Afrit 几何的兼容近似，仍不是 GeckoLib/发光层逐帧等价。
- 新增 canonical `summon_wild_afrit`：1200 tick、Bound Afrit 激活、牛献祭、下界岩/石英/打火石/火药材料、`afrit_essence` 结果、11x11 Abras 法阵、ritual dummy、英中文本和契约测试；法阵检测器对该定义单独解释上游 `C` 蜡烛与 `P` 紫色粉笔，避免和旧 1.12 图案符号冲突。
- AfritWild 已加入固定上游 Demon’s Dream 治疗白名单；实体注册明确追加在既有 Warden 之后，保持旧 1.12 实体数字 ID 不漂移。当前仍缺真实世界仪式/随从群战、掉落概率、区块重载和客户端 renderer 烟测。

### 上游差异补充：存储、配置与通用能力

- 当前存储已完成动态槽位、旧 NBT 无损迁移、stabilizer 基础扫描、1024 超大堆叠/物品堆叠覆盖、`RealSize` NBT、动态容器/六行滚动 GUI、虚拟快照/搜索/排序、Deflater/Inflater 有界栈同步、客户端 `ClientStorageCache`、已加载目标的稳定虫洞和远程访问器 item-handler 转发、第一阶段 3x3 终端合成、容器级瞬时 recipe lock、同维度已加载 linked machine/order 最小路由，以及上游兼容的 `matrix`/`orderStack` 持久化和便携控制器往返。仍未实现完整跨网络虚拟缓存、异步网络缓存和跨维度/区块加载代理；linked machine NBT 持久化、同维度/已加载约束和简化订单路由是明确的 1.12 兼容差异，`orderStack` 仍不宣称完整订单库存。12 个 stabilizer 变体已使用方向状态、六向模型和放置/拆除通知；完整网络行为仍未等价。
- 本轮补入一个低风险跨维度闭环：`ItemStorageRemote` 与 `BlockStableWormhole` 使用 `DimensionManager.getWorld` 解析已加载目标维度，并将目标 `World` 传给现有 `STORAGE_REMOTE` 服务端 GUI；远程访问器同时读写上游兼容的嵌套 `linkedStorageController` 和旧四键格式，提供 loaded-only `getStorageController` 解析，并通过 ItemStack capability 暴露已加载控制器的 `ITEM_HANDLER`；目标世界/区块未加载时保留链接并提示，目标方块缺失时才按原有逻辑清理稳定虫洞链接。稳定虫洞同时把 `ITEM_HANDLER` capability 转发到目标控制器，供 1.12 机器/使魔访问；两者仍不是上游虚拟库存或完整跨网络 capability 代理。
- 2026-08-23 继续补齐 Remote 容器协议：Remote GUI 传递并锁定具体玩家槽位，服务端从该槽重新验证 Remote 与目标控制器；3x3 `craftingMatrix` 和 `orderStack` 现在写回 Remote ItemStack NBT，并以 `RealSize` 保留 1.12 超大堆叠。稳定虫洞改用独立 `STORAGE_WORMHOLE` GUI 入口，保留虫洞实体/附近距离/目标实例校验，避免复用 `remoteAccess` 布尔值造成状态归属混淆。完整跨网络虚拟缓存、异步缓存、强制加载代理和真实客户端/专用服务器回归仍未完成。
- 2026-08-23 继续补齐 Otherworld Bird Double Jump：新增 `PotionDoubleJump`、玩家 `DoubleJumpCapability`、落地清零、客户端跳跃键预测、服务端 `MessageDoubleJump` 校验/执行和 `MessageSetJumps` 同步；Otherworld Bird 现在向附近主人提供 Jump Boost、Slow Falling 和最多 5 次空中跳跃授权。效果注册、能力 NBT、输入包和服务器边界已通过契约测试，真实客户端按键与世界运动仍待烟测。
- 2026-08-23 继续补齐通用高阶灵体的主库存闭环：`EntityBoundSpirit` 新增 1 槽 `ItemStackHandler`、Forge `ITEM_HANDLER` capability、客户端数据同步、`SpiritInventory` NBT 往返和死亡掉落；潜行空手交互现在由服务端打开追加的 `BOUND_SPIRIT` 容器/GUI 入口，显示灵体名称与阶级并允许安全取放主库存物品。该闭环覆盖了上游 `SpiritEntity` 的库存/容器基础，不宣称已完成 owner、SmartBrain、filter、工作区、存取点、Job 或完整高阶职业行为。
- 1.12 `ModConfig` 当前已包含 Blacksmith 的修理概率/升级花费/冷却和 Storage Controller 的基础容量、Tier 1-4 增量、1024 堆叠及物品上限覆盖；上游的矿工时间/roll/耐久、天气与昼夜仪式开关、持续时间倍率、灵体命名和物品设置等配置尚未形成等价实现。
- 上游 `SpiritEntity` 的 SmartBrain、owner/menu、filter 槽、工作区、存取点、Job 状态和完整存档协议仍未统一移植；本地 `EntityBoundSpirit` 现在已有 1 槽主库存、ITEM_HANDLER capability、同步/NBT 往返和独立容器入口，`EntityFoliotJob` 继续保留自己的职业过滤/工作区/订单实现。两者仍是 1.12 分层兼容路径，不等于高阶灵体 AI、owner 或上游完整机器订单行为已完成。
- 上游的 `DoubleJumpCapability`/`DoubleJumpEffect`、Curios 装备访问、扫描管理器和对应网络/客户端入口未移植；本地第三只眼仍是 1.12 物品与 overlay 的兼容实现，不能据此宣称上游玩家能力和装备集成等价。
- 上游 `FamiliarTrigger` 提供 26 类 Familiar 行为 advancement trigger；本地已注册 1.12 `occultism:familiar` criterion，并已将全部 26 类接入对应成功路径。仍需注意这些触发已通过源码/资源契约和无世界测试验证，但真实世界中的唱片机、AI 拾取/骑乘、升级、分离、Shub 爆炸和存档重载尚未完成集成回归。

## 本轮新增：Otherworld Bird、Djinni 机器操作员与虚拟存储聚合

- 新增专用 `EntityOtherworldBird`，继承 1.12 `EntityParrot` 并实现 `IFamiliar`：去除落肩目标，保留驯服/主人 NBT，加入跟随主人和飞行漫游；距离主人 8 格内每 10 tick 刷新 Jump Boost 与本地 Slow Falling。1.12 没有上游 Double Jump effect，因此没有伪造该效果。`familiar_otherworld_bird`、`possess_unbound_otherworld_bird` 和新增 `summon_wild_otherworld_bird` 均改为该专用实体，追加实体 ID `39`、renderer、纹理、dummy、英中文本和指南说明；Otherworld Bird 契约测试为 3 项。
- 新增末尾枚举 `SpiritJobType.MANAGE_MACHINE`、`MachineReference` 及 Book of Calling 的 Storage Controller / Managed Machine / Extract 三种配置模式。`EntityFoliotJob` 现在以同维度、已加载的 Forge `ITEM_HANDLER` 面为基础执行机器->存储、存储->机器搬运，并持久化控制器位置、维度、面向、机器位置和可选抽取点；新增 `summon_djinni_manage_machine`（1200 tick、Djinni、`#c:storage_blocks/coal`）及书籍、模型、dummy、文案。`DepositOrder` 以 ItemStack/可选 NBT 匹配和剩余数量持久化，按机器输入槽容量生成最多 64 个简化订单，输出优先回存；新增 `MachineManagementPortContractTest` 9 项订单/容量/边界契约。
- `EntityFoliotJob` 已加入同维度 `PathNavigate` 导航、ManageMachine 状态机、已加载区块检查、机器输入/输出注册名与指定面 `ITEM_HANDLER` 校验，并将状态、当前订单和队列写入 NBT。`MachineReference` 对双端点、维度、位置、区块加载、方块注册名和面向执行严格校验；`DepositOrder` 提供 `PENDING/COMPLETE/EXHAUSTED` 状态及最多 8 次重试。
- 机器操作员仍是 1.12 兼容实现：没有独立上游 Djinni 实体、SmartBrain、跨维度或区块加载、完整机器 GUI 或 Storage Proxy；当前只处理同一已加载世界中的 handler，linked machine 注册和订单下发是本地可持久化简化版，导航和订单队列不宣称上游完全等价。
- 新增 `StorageVirtualEntry` 和物理槽位映射。Storage Controller 的 GUI 现在按同物品聚合条目分页，黑名单物品不会进入虚拟列表，聚合提取支持模拟/真实模式并跨物理槽消费，shift-click 插入仍经真实 handler，空槽保持可见；本轮另接入客户端 `ClientStorageCache`、有界 `MessageRequestStacks`/`MessageUpdateStacks` 和 `MessageSetRecipe`。终端合成已完成第一阶段，recipe lock 已接入，完整跨网络虚拟缓存仍未移植。

## 状态说明

| 状态 | 含义 |
| --- | --- |
| 基本完成 | 有实际 1.12 实现，静态检查或构建已验证，仍可能需要玩法回归 |
| 大部分完成 | 主循环可见，仍缺少边缘行为、上游细节或当前运行验证 |
| 部分完成 | 使用通用实现、原版替代或只完成最小闭环 |
| 未完成 | 未找到对应本地实现，或只存在说明/占位数据 |
| 未验证 | 代码或资源看似已处理，但缺少当前版本运行证据 |

## 进度矩阵

| 模块 | 当前状态 | 现有成果 | 主要缺口 |
| --- | --- | --- | --- |
| Forge 工程与打包 | 基本完成 | 1.12.2 生命周期、注册、GUI、网络、世界生成和打包链已接通；本机 JDK 路径已移除，local wrapper 仍可选项目 JDK | 无普通 push/PR 构建 CI |
| 方块、物品与模型 | 大部分完成 | `BLOCKS` 数组 127 项，`ITEMS` 数组 335 项；模型、贴图、blockstate 和语言键静态校验通过；slab、fence gate、门物品和动画元数据问题已修复；新增无 ItemBlock 的 `lighted_air`、Beaver Nugget 食物、Phantom Membrane、Otherworld Bird/机器操作员书籍与 dummy、Warden ritual dummy、`summon_wild_parrot`/`summon_wild_afrit` 资源及 Dragon/Fairy/Beholder/Chimera/Wild Hunt ritual dummy | 335 个物品中含 165 个 ritual dummy 和 9 个已从创造栏隐藏的调试/测试物品；legacy Wild Hunt dummy 也已隐藏但保留注册，16 个方块直接使用 `BlockModelOnly`；仍需游戏内抽查复杂模型和动态方块 |
| 法阵与仪式引擎 | 大部分完成，基础加载已回归 | 20 个本地法阵、170 条仪式定义、65 条实体献祭要求；支持合成、召唤、职业召唤、升级、修复、复活和野性召唤；新增 Otherworld Bird 与 Djinni 机器操作员仪式；`resurrect_familiar` 已校正为 `summon_foliot`，Wild Hunt 使用专用 600 tick 三首领分支，Warden 使用 1200 tick 兼容分支，新增 Wild Afrit 使用 1200 tick 兼容分支；170 条定义已通过当前资源/契约校验 | 献祭现用玩家击杀事件并在tick末确认取消/复活，仍缺真实多人及保存卸载回归；prototype/替代实体仍不等于上游行为等价 |
| Spirit Fire | 基本完成 | 有独立配方注册、方块逻辑、JEI 分类和 CraftTweaker 接口 | 需要运行期覆盖全部输入、掉落和自动化交互 |
| Foliot 职业 | 大部分完成 | 清洁、搬运、农夫、伐木、熔炼、粉碎、结晶及 3 类交易者，共 10 条本地职业仪式；呼唤之书支持工作区、提取点、存放点和实体收纳 | 尚无自动化行为测试；复杂模组库存兼容仅由通用 Forge inventory/capability 路径保证 |
| Djinni/Afrit/Marid 职业 | 部分完成 | 三个阶级均有粉碎、结晶、熔炼工人；Djinni 另有 gambler 和 `manage_machine` 机器操作员；复用本地职业循环、呼唤之书、神灯和灵魂碎片链路；机器操作员已有同维度导航、严格端点、状态持久化、有界订单重试和 linked machine/order 最小路由 | 主要是阶级化通用工人，不是上游专属模型、AI 和完整高阶职业；机器操作员仍缺 SmartBrain、跨维度/区块加载、完整 GUI、Storage Proxy 和真实机器回归 |
| 天气和时间灵体 | 实体职业已接入，世界回归未完成 | 五条仪式召唤本地 Djinni/Afrit 职业实体；晴天/降雨/雷暴延迟 300/600/1200 tick，昼夜 100 tick 插值，五项配置开关，进度与目标 NBT，完成消散 | 现代 Brain/GeckoLib 与信标音不等价；实际召唤、运输、中途死亡、跨维度和客户端回归未完成 |
| 高阶灵体实体 | 部分完成 | 末尾追加了专用 Djinni、Afrit、Marid 类；`EntityBoundSpirit` 提供可命名、收纳、治疗、灵魂碎片恢复，以及 1 槽主库存、ITEM_HANDLER capability、同步/NBT 往返和独立服务端容器/GUI；职业路线同样复用 `EntityFoliotJob`，机器操作员已有同维度 handler 搬运第一阶段 | 仍非完整上游 SpiritEntity：缺少完整专属模型/AI、owner、filter、工作区/存取点统一状态、Job/Brain 和 Ancient 职业体系；机器订单/导航也只是 1.12 简化路径 |
| 使魔 | 大部分完成，世界行为未回归 | 参考上游 16 条 Familiar 直接仪式已达到 `16/16`；Cthulhu、Blacksmith、Greedy、Dragon、Fairy、Beholder、Chimera 均有独立实体、AI、模型、仪式、戒指运输和手册闭环，Chimera -> Goat -> Shub Niggurath -> Spawn -> Beholder 的派生生存链也已接通；16 类本地 Familiar 已统一接入正常死亡 -> canonical shard -> 复活及施法者复绑定；`IFamiliar` effects/tick API、16 类 settings capability、固定顺序同步包、16 个客户端 settings 按键、原生 Familiar Ring capability 和 nearby/equipped `FamiliarUtil` 已接入；Blacksmith 可升级 12 类目标，升级 Beholder 的装备戒指失明免疫已修复，Fairy 支援/救援、Greedy/Dragon 骑乘代拾物等既有链路保留；`occultism:familiar` 26 类成功条件均已接入并有 25 个 Familiar advancement 文件 | 原版 Curios API 本身未引入；当前原生槽只允许一个 `IFamiliar` 戒中实体，非 `IFamiliar` 运输实体只能捕获/释放、不能装备或提供戒中效果；客户端 foil 与类型 tooltip 本地化已完成，但完整世界 AI、唱片机/升级/分离/爆炸交互、真实死亡/满背包/离线与跨维度主人/复活执行和专服回归仍未完成；vanilla Parrot/Otherworld Bird 按固定快照不走共享死亡 shard；Devil、Blacksmith、Fairy、Chimera、Otherworld Bird 不可升级 |
| 附身与野性实体 | 大部分完成（固定上游核心阵容），世界行为未回归 | 固定 1.81.4 的十类专用附身实体已全部接线：八类使用 1.12 原版基类兼容，Hoglin/Warden 使用专用兼容实现；十条仪式、十张 loot table、模型/renderer、Dictionary canonical 映射/文字、普通召唤 `SUMMONED_ENTITY` 和四类召唤路径的 `SpecialSpawn`/`onInitialSpawn` 已同步。Hoglin 新增经典蘑菇回避替代、非下界 300 tick 转 Zombie Pigman 和计时 NBT；Warden 新增玩家扫描、魔法脉冲、Weakness、失明场和脉冲状态 NBT。另有专用 Otherworld Bird、本地 Possessed Bee、Mercy Goat、Purified Allay；Wild Hunt 已有专属双实体、3 首领、每首领 3-8 随从、护盾/友军免疫、专用掉落/法阵/仪式和隐藏进度 | Hoglin 仍缺现代 Brain、真实 Zoglin 属性、下界生物群系规则、Warped Fungus 身份和现代 loot 规则；Warden 缺现代 Brain、振动感知、Darkness、钻地和真正音波行为，当前以近战、周期魔法脉冲和失明场替代。Otherworld Bird 已有本地服务端校验的 Double Jump capability/网络兼容层，但仍使用原生 Parrot 模型/AI，真实客户端跳跃和世界运动尚未回归。刀具 Tallow 规则已有本地接线但仍缺世界回归；Blaze、Witch、Guardian、Iesnium Golem 及随机动物等现代数据仍多为原版替代/简化。所有新实体与 Wild Hunt 均缺真实世界战斗、掉落及区块/存档重载验证；复活路径按上游继续不走通用 spawn hook |
| 维度矿井与战场 | 大部分完成 | 矿工耐久、加权产物、提取器输出、战场掉落捕获、GUI、比较器和 capability 交互均有本地实现 | 需要长时间运行、区块加载、满库存、死亡掉落和模组交互回归 |
| 存储与虫洞 | 部分完成 | 基础 128 槽、tier1-5 stabilizer 容量、六向 5 格扫描、旧 NBT/便携物品无损扩容、缩容隐藏栈保留、1024 超大堆叠/物品上限覆盖、控制器/Remote 黑名单、动态容器和六行滚动 GUI、虚拟聚合条目/物理槽映射/跨槽提取、搜索/数量名称模组排序、客户端搜索框、排序/方向/清空控件、服务端设置同步、`ClientStorageCache`、有界压缩栈同步、第一阶段 3x3 终端合成、容器级瞬时 recipe lock、同维度已加载 linked machine/order 最小路由、`GlobalBlockPos`/`IStorageController`/`IStorageControllerProxy` loaded-only API 委托、远程访问器 ItemStack `ITEM_HANDLER` capability 和 `matrix`/`orderStack` 持久化已实现；远程访问器、稳定虫洞和实体虫洞仍只访问已加载目标并保留链接 | 未移植完整跨网络虚拟缓存、异步网络缓存和跨维度/区块加载代理；linked machine NBT 持久化、同维度/已加载约束及简化订单路由是 1.12 差异；合成尚无完整配方书、独立 order inventory 和客户端合成缓存；tier5 容量映射已于 2026-10-16 修复，真实放置/拆除世界回归仍未完成 |
| 便携物品与工具 | 大部分完成 | 挎包、末影挎包、仪式挎包、灵魂宝石、神灯、生命指南针、知识石板、占卜工具、屠刀、自然糨糊等有本地行为 | 仍需覆盖 NBT 边界、死亡/复制、跨维度和模组容器兼容 |
| 异界方块与世界生成 | 大部分完成 | 铜、银、Iesnium、异界石/木/树、隐藏/揭示状态、第三只眼和本地矿物/树生成已接入 | 高版本生态、地形和装饰内容只保留 1.12 可实现部分 |
| 渲染、动画、音效与粒子 | 部分完成 | 有 1.12 原生实体模型/Renderer、第三只眼 overlay 和原版粒子/音效替代；Cthulhu 已迁入专属层级模型、状态动画和送花层，Blacksmith 已迁入双根模型、唱片舞蹈、坐姿、variant 和铁锭层；Mummy 显示双拳套，Bat 显示酒杯，Deer 显示八刺战锤与挥击，Headless 显示头盔及青色灵焰近似，Devil 保留专属挥击火焰；Greedy/Dragon/Fairy 分别迁入 16/42/36 部件模型及专属层；Goat/Shub/Spawn 分别迁入 36/51/25 部件模型，Beholder 迁入 39 部件四眼、睡眠/眼镜/吞食层，Chimera 迁入 48 部件成长、骑乘和三头攻击动画；Beaver 升级按上游没有新增外观 | 未迁入上游 GeckoLib geo/animation、专用 sounds/OGG、粒子定义和 shader；多种替代实体复用原版外观；已有 Familiar 视觉表现尚未游戏内人工检查，Bat jukebox partying 未迁移 |
| Dictionary of Spirits | 部分至大部分完成 | 使用自研 1.12 GUI 读取迁移后的 Modonomicon 索引：9 类、203 条目、717 页；支持文本、配方、实体、图片和法阵预览等页面；新增 Wild Greater Spirit 三页和 Possessed Warden 三页，并把 Wither Skull 指向 canonical Wild Hunt，同时保留 Familiar 复活解锁修正 | 不是完整 Modonomicon 运行时；缺少完整的条件解锁、进度、搜索和书签能力；当前客户端烟测未人工逐页打开验证 |
| JEI / CraftTweaker | 基本完成 | JEI 覆盖仪式、Spirit Fire、职业加工和矿井；CraftTweaker 提供仪式、Spirit Fire、职业及矿井增删接口 | 未做脚本级自动回归；上游 EMI、Almost Unified、Theurgy 等现代集成未移植 |
| 配置、进度与数据驱动扩展 | 部分完成 | 新增标准 1.12 Forge `ModConfig`；Blacksmith 三项、Storage Controller 基础/Tier 1-4 容量、1024 堆叠和物品上限覆盖参数可配置；新增通用 `occultism:ritual` advancement trigger，并接入 Wild Hunt、Wild Afrit 与 4 条 Possessed 隐藏进度和本地辞典 entry override/fallback；普通配置实体、职业灵体、通用高阶灵体和 Wild Hunt 均共享 Forge spawn hook，并按上游在 `world.spawnEntity` 前发送原版 `SUMMONED_ENTITY` | 尚未形成对应上游 Client/Common/Server 的完整配置体系，矿工/仪式/灵体配置及其他自定义 trigger 未系统移植，固定上游 `DRAGON_RIDE` 本身也没有可达触发路径；数据包/标签能力受 1.12 限制 |
| 自动化测试与发布 | 部分完成 | Gradle 已启用 JUnit；当前全量结果为 57 个测试类、342 项全部通过；新增通用高阶灵体主库存/容器/NBT 契约、Wild Hunt 主从 UUID 持久化、Soul Shard share-tag，以及此前 Storage Controller 自代理、矩阵持久化、recipe lock、linked machine/order、Storage Proxy API、FamiliarTrigger 全 26 类成功条件和 Familiar Ring 边界契约 | 测试仍以无世界实体、资源和源码契约为主，尚无 Wild Hunt/附身实体实际战斗、工人 AI、世界拾取/升级、真实死亡/复活、完整存档、满库存或跨维度集成测试；根目录无有效 Git，专服及发布元数据/CI 未完成 |

> 本轮矩阵中的 Familiar Ring 缺口已细化：客户端 foil、`familiarType` 服务端转换、类型 tooltip 本地化、非 `IFamiliar` 运输实体的装备边界和升级 Beholder 的装备戒指失明免疫已完成；剩余的是部分事件统一接入及真实世界回归。
>
> 存储矩阵中的 capability 缺口也已细化：稳定虫洞和远程访问器现在都可把 `ITEM_HANDLER` 转发到已加载目标控制器；本地物理槽虚拟聚合、分片同步、第一阶段终端合成、容器级瞬时 recipe lock 和同维度已加载 linked machine/order 最小路由已完成，但跨网络虚拟缓存、异步网络缓存、跨维度/区块加载和完整网络代理仍未完成。

## 可量化盘点

| 指标 | 当前 1.12 工程 | 参考上游 | 说明 |
| --- | ---: | ---: | --- |
| Java 文件 | 340 个生产文件 + 57 个测试文件 | 480 | 架构差异很大，不能直接作为完成率；新增通用高阶灵体主库存/容器/NBT 契约、Wild Hunt 主从持久化、Soul Shard share-tag、FamiliarTrigger 成功条件、Storage Proxy API/loaded-only 委托、机器订单包、Storage Controller recipe lock/linked machine/order、矩阵/order slot 持久化、控制器快照缓存失效、Beholder 戒中免疫契约，以及此前 StorageCrafting/MessageSetRecipe、Machine Manager 状态/导航/订单边界、Familiar Ring foil/type/运输边界契约和完整 1.12 兼容实体/资源 |
| Java 代码行 | 45,374 | 58,063 | 仅统计生产 Java 的非空行；上游包含 datagen、现代网络、集成和辅助框架；数字随本快照新增全量 Familiar advancement 接线、Storage Proxy API、自代理、远程访问器嵌套引用解析、订单包、recipe lock/linked route、矩阵持久化、控制器快照缓存和 Beholder 戒中查询实现 |
| 资源文件 | 1,420 | 674 | 1.12 端将部分数据展开为大量模型、物品和本地手册资源；本快照新增 Otherworld Bird、Djinni 机器操作员书籍/dummy、Hoglin/Warden/AfritWild 兼容资源、25 个 Familiar advancement、机器订单确认文本、`summon_wild_parrot` 和 `summon_wild_afrit` 资源 |
| 方块注册数组 | 127 | 不直接可比 | 包含 18 个 chalk glyph 方块、`lighted_air` 及自然/暗色/双台阶等变体 |
| 物品注册数组 | 334 | 不直接可比 | 包含 165 个 ritual dummy、9 个调试/测试物品、Beaver Nugget、Phantom Membrane 和大量材料/变体；Warden、canonical Wild Hunt、Otherworld Bird、Djinni 机器操作员、`summon_wild_parrot` 和 `summon_wild_afrit` dummy 均按 append-only 追加 |
| 自定义实体注册 | 39 | 不直接可比 | Wild Hunt Skeleton/Wither Skeleton 为 ID `25/26`；十类 Possessed 继续 append-only，其中八类原版基类为 `27-34`，Hoglin/Warden 为 `35/36`，AfritWild 与 Otherworld Bird 依次追加到末尾，避免改变既有 ID |
| Tile Entity | 6 | 6 个 block entity 类 | 数量相近不代表行为完全等价 |
| 法阵 | 20 | 数据/代码结构不同 | 新增 11x11 `summon_wild_greater_spirit` 和 `summon_wild_afrit`；本地由 `PentacleDetector` 固化 1.12 图案 |
| 仪式定义 | 170 | 参考目录中 66 个 ritual JSON | 本地数据包含额外移植、替代、repair/misc 和 prototype 条目；Familiar 直接仪式为 `16/16`，另有 Otherworld Bird、Djinni 机器操作员、Warden 1.12 兼容仪式、`summon_wild_parrot` 和 `summon_wild_afrit` |
| 普通合成 JSON | 108 | 参考目录主要为仪式 JSON | 另有烧炼和药水配方在 Java 中注册 |
| 手册 | 9 类 / 203 条目 / 717 页 | Modonomicon 数据 | 新增 Wild Greater Spirit 三页和 Possessed Warden 三页，Wither Skull 引用 canonical 仪式；本地 override 保留 Familiar 复活条件 |
| 本地化 | 英中各 2,939 个唯一键 | 不直接可比 | 两种语言键集合完全一致，无重复键；新增仪式名称、Familiar settings 切换提示、18 个客户端按键标签和 Storage Controller 搜索/排序控件键 |

补充：普通 108 条配方均能作为 JSON 解析；新增语义校验已覆盖装饰类方块退役契约、已知手册引用问题和存储/实体资源边界，但仍不能替代 Forge 对全部配方的运行期解析。上游参考资源有 10 种 locale，本地仅保留英文和简体中文。

## 已验证结果

### 构建

执行：

```powershell
.\gradlew-local.bat compileJava --rerun-tasks --no-daemon --no-parallel --console=plain
.\gradlew-local.bat test --rerun-tasks --no-daemon --no-parallel --console=plain
.\gradlew-local.bat build --no-daemon --no-parallel --console=plain
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\verify_assets.ps1
```

结果：`compileJava` 和无过滤全量 `test` 均成功，当前汇总为 57 个测试类共 342 项全部通过；本轮 `build` 与资源校验脚本也通过。本轮机器可用内存不足以启动默认 `-Xmx3G`，因此 Gradle 使用低内存配置 `-Dorg.gradle.jvmargs=-Xmx768m -XX:+UseSerialGC` 完成验证。最终生成：

- `build/libs/occultism-1.0.0.jar`：3,134,978 B
- `build/libs/occultism-1.0.0-dev.jar`：3,097,773 B
- `build/libs/occultism-1.0.0-sources.jar`：2,495,082 B

`compileTestJava` 与 `test` 均在最终全量命令中实际执行，并重新生成完整 JUnit XML。结果为：

- `RitualDataContractTest`：4 个测试，0 失败、0 错误；覆盖仪式 ID 唯一性、1.12 实体标签映射、职业 ID 和灵体阶级 ID。
- `PortedResourceContractTest`：7 个测试，0 失败、0 错误；覆盖装饰类方块/异界木板门退役、手册已知坏引用、动画元数据、跨维度 remote/wormhole GUI world 解析、remote container 距离门控和稳定虫洞 `ITEM_HANDLER` 代理。
- `StorageControllerContractTest`：20 个测试，0 失败、0 错误；覆盖基础 128 槽、tier0-5/dark 容量映射、旧 byte/int Slot、动态扩缩容、1024 超大堆叠、override 开关、`RealSize` byte 溢出恢复、黑名单、动态容器边界、虚拟条目聚合/物理槽映射/跨槽模拟与真实提取、数量/名称/模组排序、设置 NBT 往返、隐藏槽位保护、矩阵恢复、异常矩阵 NBT、便携空物理库存、order slot 往返、快照缓存复用/内容失效/容量失效和返回列表防篡改。
- `CthulhuPortContractTest`：7 个测试，0 失败、0 错误；覆盖精确仪式、任意生鱼 wildcard、注册顺序、模型/纹理哈希、辞典三页、熟魔戒 owner 校验，以及坐姿/水域/跟随/照明状态转换。
- `BlacksmithPortContractTest`：12 个测试，0 失败、0 错误；覆盖精确仪式、append-only 注册、模型/纹理、dummy 父级、辞典四页、戒指 owner 校验、配置默认值、真实实体构造/属性、喂铁/NBT/返还、升级目标筛选和石头拾取修理契约。
- `FamiliarUpgradePortContractTest`：5 个测试，0 失败、0 错误；覆盖 Guardian 恢复一条生命与满生命拒绝、Mummy 固定 UUID 永久 `+3` 攻击和无世界幂等恢复模拟、升级手套/双语提示/NBT 源码契约，以及 Mummy、Devil、Headless、Demonic Wife/Husband 的攻击属性构造路径。
- `BatUpgradePortContractTest`：5 个测试，0 失败、0 错误；覆盖一次性升级、NBT/坐姿主人效果、Potion 注册/颜色/双语、`1 + amplifier` 回血、投射物 true source、酒杯几何和纹理哈希。
- `DeerHeadlessUpgradePortContractTest`：7 个测试，0 失败、0 错误；覆盖 Deer/Headless 一次性升级、同步/NBT、共享火焰攻击、`1.3x` 同头型最终伤害、Deer 战锤、Headless 虚弱/粒子/头盔、七种精确头型和双语提示。
- `BeaverUpgradePortContractTest`：6 个测试，0 失败、0 错误；覆盖一次性升级、同步/NBT、零食冷却和副手入口、食物属性、append-only 注册、模型/纹理哈希、双语文本及错误辞典描述清理。
- `GreedyUpgradePortContractTest`：9 个测试，0 失败、0 错误；覆盖一次性升级、同步/NBT、交互顺序、拾物容量/反磁吸、10x5 方块搜索、目标生命周期、模型/渲染/纹理、辞典/图标、仪式和双语边界。
- `DragonPortContractTest`：12 个测试，0 失败、0 错误；覆盖精确仪式/标签、属性、升级/variant/NBT、交互顺序、主人贪婪效果与经验公式、AI/Greedy 骑乘、飞剑生命周期/伤害/友军过滤的源码契约、`setThrower` 与主人友军早退直接调用、无世界 NBT、注册/熟魔戒、42 部件模型/renderer/纹理、三页辞典和双语边界。
- `FairyPortContractTest`：9 个测试，0 失败、0 错误；覆盖精确仪式/注册顺序、真实 Bootstrap 构造/属性/variant/NBT、10 tick 魔法、五类支援与 200 tick 冷却、400 tick 致死救援、owner-only 戒指与 `IFamiliar` 飞剑友军、36 部件模型/半透明 renderer/纹理哈希、三页辞典和双语/Curios 边界。
- `GoatShubSpawnPortContractTest`：7 个测试，0 失败、0 错误；覆盖 append-only 注册、Goat/Shub 属性/variant/升级/NBT、任意顺序森林转化、Spawn creator/寿命、固定计时/距离/重复伤害/普通死亡语义、三套模型/renderer/纹理、Shub 辞典和戒指边界。
- `BeholderPortContractTest`：6 个测试，0 失败、0 错误；覆盖精确仪式、注册顺序、属性/外观 RGB/升级 NBT、可达射线计时与伤害、Spawn 吞食/发光/失明免疫、39 部件模型/renderer/双纹理、辞典/戒指和双语边界。
- `ChimeraPortContractTest`：6 个测试，0 失败、0 错误；覆盖九材料仪式、成长尺寸/属性/五类 variant/NBT、喂肉/Goat 分离/骑乘交互顺序、1.12 服务端跳跃接口、三头攻击效果、48 部件模型/renderer/纹理、四页辞典/戒指和双语边界。
- `FamiliarSoulShardPortContractTest`：12 个测试，0 失败、0 错误；覆盖 16 类共享 Familiar 基类与排除类型、公共属性、持久化/despawn、墙体碰撞免疫、Bound Spirit/Foliot 墙体免疫与非玩家/非箭矢伤害衰减、唯一死亡 shard 交付路径、canonical/legacy NBT、满血且保留 Motion/UUID/专属状态、骑乘序列化、裂魂排除、Guardian/Headless 缺陷修正、施法者复绑定和精确仪式/辞典/双语契约。
- `FamiliarEffectsPortContractTest`：6 个测试，0 失败、0 错误；覆盖七类 fresh potion effects、IFamiliar hook、17 类 settings 默认开启/NBT round-trip（含 Otherworld Bird）、capability attach/Clone、登录/重生/换维度同步、固定顺序网络切换、单一客户端按键 handler/一次注册、双语按键键集合唯一性和普通背包戒指/equipped 查询边界。
- `FamiliarRingCapabilityPortContractTest`：8 个测试，0 失败、0 错误；覆盖原生单槽边界、戒中状态 NBT/持久化接口、死亡/维度 Clone 入口、客户端 RightClickEmpty 请求、服务端潜行空手校验、装备/取下网络同步、`familiarType` 惰性转换/坏标签保留、类型 tooltip/foil、运输实体边界、统一 equipped 查询和升级 Beholder 装备戒指失明免疫。
- `WildHuntPortContractTest`：10 个测试，0 失败、0 错误；通过 Bootstrap 实体构造及源码/资源契约覆盖 append-only 实体/renderer、属性/火焰语义、3-8 随从与 70% 石剑、友军免疫/首领护盾、随从生成取消、日晒/和平实现、两张 loot table、专用三首领仪式及其 JEI/调试数量元数据、11x11 法阵、canonical dummy、双语/辞典和隐藏 advancement。
- `PossessedEntityPortContractTest`：10 个测试方法，覆盖十类 append-only 实体/renderer、Bootstrap 构造/属性/火焰语义、和平与日晒兼容、八条 canonical 仪式及 Hoglin/Warden 两条兼容仪式、Phantom tracking range 与飞行循环、Warden 脉冲/失明源码契约、统一初始化/结果所有权/单次 ritual trigger、普通召唤 `SUMMONED_ENTITY`、固定上游可表达的完整治疗白名单与 `2/10/20` 三档治疗量、四鱼种稀有池、十张 1.12 loot table 的 Forge 反序列化、Dictionary fallback/双语文字与四条隐藏 advancement；本轮全量结果为 10 个通过、0 失败、0 错误。
- `StorageSettingsNetworkContractTest`：3 个测试，0 失败、0 错误；覆盖 UTF-8 查询/长度边界、排序枚举协议、网络注册、普通/Remote/Wormhole 权限校验、服务器排序/过滤/更新包和空槽可见性。
- `StorageControllerGuiPortContractTest`：3 个测试，0 失败、0 错误；覆盖搜索框、排序/方向/清空按钮、客户端发送入口、空槽插入边界和英文/中文 GUI 键。
- `AfritWildPortContractTest`：5 个测试，0 失败、0 错误；覆盖 1200 tick canonical 仪式、激活/献祭/材料、11x11 法阵、100 生命/8 攻击/50 跟随距离实体属性、3-8 Blaze 随从、火焰/盟友免疫、末尾注册/renderer/dummy/loot、Demon's Dream 白名单和双语资源。
- `WildParrotPortContractTest`：2 个测试，0 失败、0 错误；覆盖 canonical 仪式材料/实体类型、ritual dummy 注册、模型、英中文本和 Parrot 替代语义。
- `OtherworldBirdPortContractTest`：3 个测试，0 失败、0 错误；覆盖专用 1.12 实体、无落肩/跟随主人/效果兼容、两条 Familiar/野性仪式、append-only 注册、renderer/纹理/dummy 和双语资源。
- `ManageMachinePortContractTest`：4 个测试，0 失败、0 错误；覆盖 MANAGE_MACHINE ID/三种书籍模式、控制器/机器 NBT、实体持久化、ITEM_HANDLER 模拟搬运和 Djinni 仪式材料。
- `MachineManagementPortContractTest`：10 个测试，0 失败、0 错误；覆盖 MachineReference 位置/维度/面向/名称往返、严格双端点/同维度边界、仪式 JSON、书籍/实体源码路由、状态/导航/已加载端点检查、dummy 模型和英中文本，以及 DepositOrder 匹配/数量往返、输入容量订单生成、有界重试和 `MessageRequestOrder` 包往返/空堆栈边界。
- `StorageStacksNetworkContractTest`：3 个测试，0 失败、0 错误；覆盖 Deflater 栈列表/1024 超大数量往返、畸形压缩长度拒绝、网络注册、容器权限入口、控制器缓存发送路径和客户端 `ClientStorageCache`/GUI 快照入口。
- `StorageCraftingContractTest`：5 个测试，0 失败、0 错误；覆盖 3x3 合成矩阵/结果槽布局、配方匹配与存储补料/安全返还代码路径、`MessageSetRecipe` 的 64 KiB/槽位/候选数边界和 GUI 合成面板尺寸。
- `LinkedMachineRegistrationContractTest`：4 个测试，0 失败、0 错误；覆盖 linked machine 去重、坏 NBT、防御性副本、机器引用网络包往返/partial reject，以及已加载同维度 spirit UUID 订单路由边界。
- `StorageProxyCapabilityContractTest`：7 个测试，0 失败、0 错误；覆盖 GlobalBlockPos 上游/旧版 NBT schema、未知维度拒绝、控制器自代理、控制器 API 的真实 handler/模拟提取约定，以及未加载稳定虫洞目标引用保留。
- `FamiliarTriggerPortContractTest`：8 个测试，0 失败、0 错误；覆盖 1.12 trigger 注册、26 类上游事件枚举、JSON type 解析/未知类型不匹配、Familiar Ring CAPTURE、原有成功条件、Deer/Bat/Dragon 交互 advancement、Chimera/Goat/Shub/Dragon ride/PARTY advancement、事件总线接线和隐藏 advancement 双语资源。
- `FamiliarRareVariantPortContractTest`：4 个测试，0 失败、0 错误；覆盖 RARE_VARIANT、CTHULHU_SAD、DEVIL_FIRE 的隐藏 advancement、双语键和 Cthulhu/Devil/Guardian/Mummy/Headless/Fairy 成功路径。
- `FamiliarUpgradeAdvancementPortContractTest`：2 个测试，0 失败、0 错误；覆盖 Blacksmith/Greedy/Beholder/Beaver 五类动作 advancement、Dragon ride 成功挂载以及动作成功顺序。
- `WildHuntPortContractTest`：11 个测试，0 失败、0 错误；新增首领/随从 UUID NBT 往返、仅对已加载实体延迟重连和关系清理契约。
- `SoulShardShareTagPortContractTest`：3 个测试，0 失败、0 错误；覆盖 canonical/legacy 实体快照仅同步 `id`、服务端 NBT 不变、副本隔离和空 shard。

总计 57 个测试类、342 个测试，0 失败、0 错误、0 跳过；57 份 JUnit XML 已由本次无过滤 `test` 任务生成。新增通用高阶灵体主库存/容器/NBT 契约、Wild Hunt 主从 UUID NBT round-trip/已加载实体重连、Soul Shard share-tag 过滤，以及 Storage Controller 自代理、矩阵持久化、recipe lock、linked machine/order、Storage Proxy API、机器订单包、控制器快照缓存失效、FamiliarTrigger 全 26 类成功条件、Beholder 戒中免疫，并保留 StorageCrafting、MessageSetRecipe、Machine Manager 导航/严格端点/订单重试和 Familiar Ring foil/type/运输边界契约。

### 静态资源

执行：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\verify_assets.ps1
```

结果：通过。校验覆盖 696 个 model JSON、136 个 blockstate JSON、108 个 recipe JSON、仪式 JSON、334 个注册物品、模型父级、贴图引用、物品模型、手册图片和英中文本键；`JsonErrors`、全部缺失项以及 `RecipeSemanticErrors`、`BlockstateSemanticErrors`、`DictionarySemanticErrors` 均为 0。

额外盘点 `src/main/resources` 下 1,420 个资源文件，其中 994 个 JSON 文件、语法错误 0；当前共有 13 张实体 loot table（2 张 Wild Hunt、10 张 Possessed、1 张 AfritWild）和 31 个 advancement JSON（root、Wild Hunt、4 条 Possessed、25 条 Familiar advancement，其中 Dragon PET/FETCH 共用一个文件），仪式总数为 170。Dictionary 为 9 类、203 条目、717 页；使用 `D:\anaconda\python.exe` 连续生成两次，最终两次 SHA-256 均为 `22F32D263BEE774FE2A1CB5478CEEACF3DFBF51C2B261E272FE16652D5AB5791`，静态引用校验为 0 错误。既有 Familiar/使魔纹理哈希仍由各自契约测试固定；Wild Hunt 与八类基础 Possessed 复用原版实体纹理，Phantom 使用新增专属实体 PNG，Hoglin 使用新增模型并复用猪纹理，Warden 使用 1.20.1 官方 Warden PNG（SHA-256 `6D88F7C1EEDCF37CF4D700B8E85A1143C142B3DF1CDCBCFCFD6E8EAD852400A5`）及新的 1.12 模型；AfritWild 使用复制的上游 Afrit PNG/发光遮罩与 1.12 兼容模型，Otherworld Bird 使用复制的上游专用 PNG。

### 客户端主菜单烟测与旧快照世界记录

2026-07-12 05:57:06-05:57:39（游戏日志时间；受控脚本约 05:56:34 启动）在 Wild Hunt 主体静态闭环构建后执行受控 `runClient`；`run/logs/latest.log` 证明：

- Forge 成功加载 9 个模组，客户端窗口正常到达 `Minecraft 1.12.2`。
- `Loaded 165 Occultism ritual recipes from JSON.` 只出现一次（该历史烟测早于 Warden、`summon_wild_parrot`、`summon_wild_afrit` 和本轮新增条目；当前 `rituals.json` 静态数量为 170）。
- JEI 完成分类、插件、配方注册和运行时构建；运行时构建耗时 `138.4 ms`，JEI 总启动耗时 `1.216 s`。
- `run/config/occultism.cfg` 已由 Forge 配置注入生成，`blacksmithFamiliarRepairChance=0.05`、`blacksmithFamiliarUpgradeCost=18`、`blacksmithFamiliarUpgradeCooldown=400` 均正确。
- 日志有 5 条 `WARN`、4 条 `ERROR`、0 条 `FATAL`；与此前数量和内容一致，均为 RFG Maven 路径、无签名、coremod `MCVersion` 和空 `lastServer` 的开发环境基线。没有新增 Occultism、Wild Hunt、模型、纹理、实体、renderer 或 trigger 注册告警，也没有 `Missing data for item`、`MissingVariantException`、缺失模型、未知实体或 advancement 解析错误；离线开发账号的 Realms 授权异常发生在成功加载标记之后，不影响本地主菜单烟测。
- 达到烟测标记并终止进程树后，工作区相关 Java/Gradle 进程数为 0。

边界：该烟测只验证到主菜单，没有进入世界、执行 `summon_wild_hunt`、人工生成相关实体、逐页打开 Dictionary of Spirits 或逐个点击 JEI 仪式分类；达到 Forge、历史烟测加载的 165 条仪式和 JEI runtime 三个标记后主动终止本次进程树，不能记作游戏内行为、advancement 授予或正常关服回归。当前静态仪式数量已是 170。客户端烟测早于 Hoglin/Warden 兼容实体、十张 loot table 和本轮最终测试，新的实体/renderer 客户端加载仍未获得运行证据。无过滤 JUnit 已覆盖两类 Wild Hunt 与十类 Possessed 构造、无世界行为契约和 Forge loot table 反序列化，但没有执行真实世界 12-27 实体群战、附身实体掉落概率、和平切换、区块卸载/重载、主从关系恢复、Familiar 实际死亡/复活或既有升级能力的世界行为，也没有客户端像素/GL 检查。2026-07-11 较早的快照曾成功进入现有 `New World`，但发生在 Cthulhu/Blacksmith/Dragon/Fairy、五实体生存链、Familiar shard、Wild Hunt 和专用 Possessed 批次之前，只能继续作为基础集成服务器链路证据。

专用服务器仍只验证到 EULA 检查，没有完成接受 EULA 后的 FML 启动、玩家进入和正常关服。

### 2026-08-31：`runClient` 启动回归与注册错误修复

- 历史崩溃报告 `run/crash-reports/crash-2026-08-30_22.21.10-client.txt` 的根因是 Forge 在注册 `occultism:djinni_job` 时找不到 `EntityDjinniJob(World)`。同样的构造器缺口也存在于已注册的 `EntityAfritJob` 和 `EntityMaridJob`。
- 三个专用职业实体现在都提供公开的 `World` 构造器，默认职业使用现有的 `CRUSHER`；带 `SpiritJobType` 的构造器和 `EntityFoliotJob.createForTier(...)` 路径保持不变。新增测试固定四个已注册职业实体的 Forge 构造器契约。
- Otherworld Bird 野性仪式原本引用高版本物品 ID `minecraft:cobweb`，1.12 资源注册名为 `minecraft:web`；仪式 JSON 和对应契约测试已同步修正。
- 2026-08-31 运行当前源码的 `runClient`：日志记录 `Loaded 170 Occultism ritual recipes from JSON.` 和 `Forge Mod Loader has successfully loaded 9 mods`，没有新的 Occultism 异常或崩溃报告。测试进程在完成加载烟测后由维护脚本主动停止，因此 Gradle 最终显示退出码 `-1`，这不是客户端加载失败。
- 日志中的 Maven 路径格式、开发环境签名、Forge 版本检查、空 `lastServer` 和离线 Realms 授权提示均为非致命开发环境警告，未作为本次 P0 问题处理。

## 本轮关闭的阻断项

- Dictionary 的调试铺阵现在只允许服务端创造模式、至少 2 级命令权限的玩家使用；普通玩家仍可只读识别法阵。
- `DEBUG_WAND`、7 个调试召唤物和 `MINER_DEBUG_UNSPECIALIZED` 保留注册 ID 以避免存档映射破坏，但已从创造栏隐藏。
- 7 个 slab 配方结果与 2 个 chiseled 配方输入已补 `data: 0`；fence gate 已补齐包含 `powered` 的 32 个 blockstate 组合。
- 1.12 的 `ResourceLocation` 不能可靠使用单字符 `c:` namespace；村民标签已由 `c:villagers` 改为 `occultism:villagers -> minecraft:villager`，并由契约测试固定。
- 已修复 6 个 Spotlight、4 个 book-binding 配方引用、悬空 parent/condition，以及无法解析的 `storage_stabilizer_demo` 页面。
- `repair_icon.png.mcmeta` 已补齐；异界木板门按装饰内容范围约束已从 1.12 注册表、OreDictionary、配方、模型、方块状态和语言资源中移除，异界木板活板门仍保留。
- Cthulhu 的实体/渲染/仪式/辞典/熟魔戒链路已闭合；二次代码复核发现的坐姿 AI、水方块传送判定、navigator 切换时机、共享光源误删和跟随节拍偏差也已修正并加入契约测试。
- Blacksmith 的实体/渲染/仪式/辞典/熟魔戒链路已闭合；喂铁、同步层数、死亡返还、同主人筛选、400 tick 冷却、Cthulhu/Guardian/Mummy 升级和石头修理都有对应实现与契约测试。
- Guardian 的一次性 `+1` 生命和 Mummy 的固定 UUID 永久 `+3` 攻击已按 1.81.4 接入；满生命拒绝、重复调用幂等、无世界恢复模拟、手套显示和双语提示均有回归测试。
- Bat 的一次性升级、生命窃取 Potion、true-source 击杀回血、坐下时继续刷新效果、夜视 amplifier 修正、酒杯显示和双语提示已按 1.81.4 接入，并有独立契约测试。
- Deer 的一次性战锤、共享 4 点/4 秒点燃攻击和八刺模型，以及 Headless 的一次性同头型虚弱、七种精确头型、`1.3x` 最终伤害、头盔与青色粒子已按 1.81.4 接入。复核中发现的 Headless 双攻击链、扩展头型误匹配、`2.0x` 倍率及共享额外 `FLAME` 爆发均已关闭；Devil 专属客户端挥击火焰已独立恢复，并由同一组 7 项契约测试固定。
- Beaver 的一次性升级、严格大于 20 tick 的主人零食入口、8 点饥饿值/`0.8` 饱和度的 Beaver Nugget、append-only 注册和双语文本已接入；同时修正上游重复耗铁缺陷及本地辞典中不存在的死亡掉落/挖掘效果描述，并由 6 项契约测试固定。
- Greedy 的一次性升级、任意玩家方块样本交互、容量感知/反磁吸拾物、10x5 方块搜索、运行期目标同步、专属模型/三纹理/双手物品层和三页辞典已接入；终审发现的到达后 1200 tick 预算误耗和目标区块卸载读取也已修正，并由 9 项契约测试固定。
- Dragon 的实体/飞剑/Greedy 同主人骑乘、贪婪经验、交互、仪式、熟魔戒、42 部件模型、双语与三页辞典链路已按固定 1.81.4 快照接入；飞剑客户端主人同步与友军自撞早退、Fetch 多 Dragon 防复制也已修复，实体/资源纹理哈希、八槽标签仪式、注册顺序和边界由 12 项契约测试固定。
- Fairy 的实体/五类支援/致死救援/仪式/熟魔戒/36 部件模型/睡眠层/双语与三页辞典链路已按固定 1.81.4 快照接入；终审发现的死亡目标存活门、重复攻击计时、无主 Familiar 过滤、1.12 渲染矩阵、半透明翅膀和视锥裁剪问题均已修正，并由 9 项契约测试固定。
- Chimera -> Goat -> Shub Niggurath -> Spawn -> Beholder 的完整生存链已闭合；两条直接仪式、五个 append-only 实体、五套模型/renderer、熟魔戒、辞典和双语资源均已接入。终审修复了 Goat/Chimera 实际碰撞箱缩放、Chimera 专服跳跃接口、Shub 朝向和 Beholder 不可达射线计时，并由新增 19 项契约测试固定。
- Wild Hunt 的原版单体替代已关闭：append-only 双实体、三首领/随从群、护盾和友军免疫、和平/日照兼容、专用掉落/法阵/仪式、canonical dummy、双语/辞典和隐藏进度均已接入；全部首领生成失败时不再误掉展示用头颅，被取消的随从也不会进入护盾列表。
- 固定 1.81.4 的十类专用附身实体已关闭原版单体替代：十个 append-only 实体、十条仪式、十张 1.12 loot table、原版/专用 renderer、可表达的完整 Demon’s Dream 治疗集合、Dictionary canonical 映射/双语文字和四条隐藏进度均已接入；Hoglin/Warden 为明确的 1.12 兼容实体，不宣称现代 Brain/Zoglin 或 Warden Brain/振动/钻地/Darkness/真正音波等价。Elder Guardian 稀有池已按 1.12 四鱼种权重展开，Phantom 另有专属飞行循环、膜物品和 tracking range 上游 16 chunks/本地 Forge 256 blocks。普通配置实体、Foliot job、Bound Spirit 和 Wild Hunt 按上游在 `world.spawnEntity` 前发送原版 `SUMMONED_ENTITY`，并共享 `SpecialSpawn`/`onInitialSpawn` 初始化顺序。通用完成分支现以“结果所有权”区分 runtime 与 item result，职业/天气/高阶/普通召唤不会因缺少 `entity_to_summon` 或生成失败而误掉展示物；craft、repair、resurrection 仍保持独立结果规则。
- Wild Afrit 已关闭“缺少专属实体”的这条具体缺口：末尾 append-only `EntityAfritWild`、3-8 Blaze 随从、1200 tick 仪式、11x11 法阵、掉落表、兼容模型/renderer、双语资源和 Demon’s Dream 白名单均已接入；现代 GeckoLib 发光层和真实世界战斗/掉落仍未回归。
- Storage Controller 设置同步已关闭“排序/搜索后端不可操作”的具体缺口：1.12 GUI 控件、UTF-8 有界查询、服务端容器权限、Remote/Wormhole 校验、物理排序和 TileEntity 更新包均已接入；有界分片栈同步、第一阶段终端合成、容器级瞬时 recipe lock 和同维度已加载 linked machine/order 最小路由也已接入，仍缺上游完整虚拟缓存和跨网络 capability 代理。
- 16 类本地 Familiar 已统一接入正常死亡 shard：canonical `entityData` 写入与 legacy 三键读取、骑乘状态序列化、owner 交付、Guardian 唯一随机 lives shard、Headless 完整死亡/重建状态、裂魂排除和施法者复绑定均有实现。`resurrect_familiar` 的法阵/时长/四份精华及辞典路线已校正；Guardian 上游双 shard、Headless 状态清零和易碎灵魂双倍基础掉落作为明确缺陷修复记录，并由 12 项契约测试固定。
- `EntityFamiliarBase` 现统一注册缺失的 `ATTACK_DAMAGE`，并提供公共生命/护甲/韧性/移速/攻击基线、持久化、禁止 despawn 和 `IN_WALL`/`FLY_INTO_WALL` 免疫；Chimera、Dragon、Mummy 仍按上游覆盖特例。`EntityBoundSpirit` 与 `EntityFoliotJob` 同样免疫墙体碰撞，并将非玩家、非箭矢伤害按 `(amount + 1) / 2` 衰减。
- Cthulhu、Blacksmith、Mummy、Devil、Headless、Demonic Partner 从 `EntityTameable` 继承时缺失 `ATTACK_DAMAGE` 注册会在真实构造阶段触发空指针，Fairy 还缺失 `FLYING_SPEED`；现已改为缺失时显式注册，并由 Bootstrap 实体构造测试覆盖。

截至本快照，没有仍开放的 P0 加载阻断项。

## 当前已知问题与风险

### P1：仍存在明确 prototype/桥接仪式

以下 6 条数据仍以材料结果承接未完成的召唤、附身或接触语义：

- `possess_djinni_prototype`
- `possess_unbound_afrit_prototype`
- `possess_afrit_prototype`
- `craft_afrit_prototype`
- `contact_wild_spirit_prototype`
- `contact_eldritch_spirit_prototype`

固定上游 ritual 目录中此前缺失的 `summon_djinni_manage_machine`、`summon_wild_otherworld_bird` 已补入；前者是现有 `EntityFoliotJob` 的同维度 handler 兼容路线，后者是专用 `EntityOtherworldBird` 的 Parrot 子类路线，二者均已由契约测试覆盖但仍不等于现代上游实体/网络行为。`summon_wild_afrit` 已按 1.12 兼容实体接入，`summon_wild_parrot` 仍保留为独立的原版 `EntityParrot` 仪式。

另外，很多高版本实体虽然仪式可执行，但结果是 1.12 原版实体或通用本地实体，不能标记为上游等价完成。

### P1：专用附身实体已静态闭环，世界行为仍未验证

- 固定 1.81.4 的十类专用附身实体当前为“8 类原版基类接线 + Hoglin 兼容实现 + Warden 兼容实现”。Hoglin 已不再映射 Zombie Pigman：`occultism:possessed_hoglin` 有独立实体、仪式、loot、模型和 renderer；1.12 AI 在经典红/棕蘑菇标记附近回避，并在非维度 `-1` 累计 300 tick 后转换为原版 Zombie Pigman（状态写入 NBT），但尚无现代 Brain、真正 Zoglin 属性、下界生物群系规则或 Warped Fungus 身份。Warden 的 `occultism:possessed_warden` 有独立实体、1200 tick 仪式、专属 loot、模型、renderer、官方纹理和辞典页；本地用近战、60 tick 魔法脉冲（6 点伤害/Weakness）及 40 tick 失明场近似，尚无现代 Brain、振动监听、愤怒/黑暗、真正 sonic boom、钻地和古城资源。现代 26.1.2 数据中的 Blaze、Witch、Guardian、Iesnium Golem、随机动物/村民等也仍是原版替代或简化召唤；Otherworld Bird 已有专用 Parrot 子类，但仍缺现代 Double Jump 和独立飞行/动画栈，不能与 canonical 实体合并标成全部完成。
- `ItemDemonDreamFood.tryHealSpirit()` 已按固定上游 tag 在 1.12 可表达的集合收敛：Foliot/Bound Spirit、Wild Hunt、三类已移植 Possessed、全部本地 Familiar、Parrot、Shub Niggurath Spawn 与 Demonic Partner 均可治疗；明确不扩大到固定 tag 未列出的 Possessed Skeleton、Shulker、Elder Guardian、Bee、Mercy Goat 或 Purified Allay。Datura、Demon's Dream Essence、Otherworld Essence 的治疗量已分别固定为 `2/10/20`，仍需真实世界右键交互回归。
- Dictionary GUI 已使用本批八个 canonical 注册实体和 Hoglin/Warden 兼容注册实体，十条 ritual dummy tooltip 和 Endermite/Enderman/Shulker/Phantom/Hoglin/Warden 掉落说明已校正；潜影贝页面明确记录 1.12 不具备现代受弹增殖机制。仍缺客户端逐页打开与实体预览回归。
- Elder Guardian 的 2.5% + Looting 稀有鱼池已按原版 1.12 fishing/fish 的 `60/25/2/13` 权重展开 cod、salmon、clownfish、pufferfish metadata，并保留每个 entry 的燃烧熔炼函数；十张表已纳入 Forge 反序列化契约，本轮全量测试已通过，但仍没有世界内击杀抽样统计；Hoglin 已接入刀具 Tallow 追加规则，Warden 已接入 echo_dust 保底与 1.12 唱片替代池。
- 通用 `spawnConfiguredEntities()`、Foliot job、Bound Spirit 和 Wild Hunt 均在加入世界前经过统一的 `SpecialSpawn`/`onInitialSpawn` 兼容 helper，并按上游在调用 `spawnEntity` 前通知实体周围 50 格玩家的原版 `SUMMONED_ENTITY`；自定义 `occultism:ritual` trigger 仍只对施法者在完成点触发一次。复活路径明确不调用该 helper，以免覆盖完整恢复的 NBT。
- `SpecialSpawn` 接线顺序与 Forge 1.12 `WorldEntitySpawner` 一致：事件返回 true 时跳过 `onInitialSpawn`，之后仍进入 `world.spawnEntity`；真正的加入世界取消由 `spawnEntity` 返回 false 表示。报告和测试不应把 `SpecialSpawn` 返回值本身描述成“阻止生成”，当前“失败不掉展示结果”和“失败随从不进入护盾列表”依赖的是最终 `world.spawnEntity` 结果。

### P1：Wild Hunt 已静态闭环，仍缺世界回归

- 原版单只 Wither Skeleton 替代已删除；canonical 600 tick 仪式、专用 11x11 法阵、3 首领/每首领 3-8 随从、属性、装备、护盾、友军免疫、日晒/和平兼容、掉落、双语/辞典和隐藏进度均已实现并由 10 项契约测试固定。
- 当前尚未在真实世界执行完整仪式或战斗，未验证 12-27 个实体同时寻路/攻击的 tick 成本、玩家击杀与 Looting 实际掉落、创造/虚空绕过、恶魔之梦治疗、附近玩家 `SUMMONED_ENTITY` 触发和隐藏进度授予。
- 主从引用现在以 `WildHuntMaster`/`WildHuntMinions` UUID 写入 NBT，并在已加载实体可见时延迟重连；该行为是明确记录的本地上游缺陷修正，不会强制加载区块。真实区块卸载、服务端重启和存档重载仍需集成回归。
- 本地 `humans` 已改为玩家击杀村民/玩家的死亡通知；不再自动击杀，也不再接受女巫替代。事件确认推迟至世界tick末避免救援取消误计，尚缺完整 Wild Hunt 世界内献祭回归。法阵仍允许彩虹/虚空等高阶粉笔替代，声音等视觉差异继续记录。

### P1：测试缺口

- 现有 **342 个测试**主要覆盖仪式、资源、源码契约、无世界实体行为、Forge loot table 反序列化和原版实体完整 NBT；测试已在 Minecraft Bootstrap 后构造两类 Wild Hunt、十类 Possessed、AfritWild 与 Otherworld Bird 的静态/构造契约，并验证 Bound Spirit 主库存 capability/NBT/GUI、Wild Hunt 主从 UUID NBT round-trip/已加载实体重连、Soul Shard share-tag 只同步实体 ID、FamiliarTrigger 全 26 类成功条件、Beholder 装备戒指免疫、settings/effects/Ring capability、客户端按键入口、Storage Controller 自代理/动态容量/矩阵持久化/recipe lock/linked machine/order、控制器快照缓存复用与失效、Storage Proxy API/loaded-only capability、远程访问器嵌套/旧引用解析、GlobalBlockPos 上游/旧版 NBT schema 和未知维度拒绝、虚拟聚合/跨槽提取/NBT/黑名单/快照搜索排序、设置网络/GUI、有界压缩栈同步、跨维度存储 GUI 解析、Djinni 机器操作员 NBT/handler/DepositOrder/订单包路由和 AfritWild Demon’s Dream 白名单，但仍不创建真实游戏世界或执行 Forge 游戏循环。
- 没有带真实 `World`、模组注册恢复、死亡/复活及戒指捕获/释放流程的完整集成 NBT 往返，也没有实际 ItemStack 交互、机器库存、矿井、战场或工人 AI 自动化测试；机器操作员虽已有同维度 PathNavigate、状态机、严格端点和订单重试契约，但仍未做真实世界导航/机器吞吐回归。
- 没有覆盖真实 stabilizer 放置/拆除、库存满载、区块卸载、跨维度和存档重载的集成回归。

### P1：Familiar Ring 已有原生单槽，仍缺完整等价能力

- `ItemFamiliarRing` 仍通过硬编码 allowlist 接受本地 Familiar/替代实体，不是固定上游“任意 `IFamiliar`”的完整接口契约；原生 capability 装备时只接受能实现 `IFamiliar` 的戒中实体，Purified Allay、Possessed Bee、Mercy Goat 等运输实体仍只能捕获/释放。
- `FamiliarRingCapability` 提供一个玩家单槽：戒中实体延迟加载、每 tick 调用 `curioTick`、每 20 tick 按设置刷新效果并写回实体 NBT；死亡 Clone 与维度 Clone 均保留槽内容。普通背包戒指不会生效；潜行空手取下由 `RightClickEmpty` 客户端发包，服务端校验空手/潜行后回库存或掉落，并同步槽状态。`canEquipFamiliar` 明确限制装备态为 `IFamiliar`，运输-only 实体仍可捕获/释放但不能装备。
- `FamiliarUtil` 已合并附近世界实体与已装备戒指查询，Guardian/Fairy/Headless/Blacksmith、Greedy 磁吸、Beholder 发光/失明免疫和 Fairy 冷却已接入部分戒中行为。`familiarType` loot 已有服务端惰性转换和类型 tooltip；客户端 foil 与类型本地化已按实体 `isEffectEnabled`/注册翻译名接入，非 `IFamiliar` 运输实体明确不提供戒中能力；Familiar advancement 26 类成功条件均已接线，仍缺真实世界/NBT 集成回归。

### P1：Blacksmith/使魔升级链与动态光源尚缺游戏内回归

- 当前只证明实体/renderer/Potion/ItemFood/投射物注册可加载、资源引用闭合、源码契约和无世界实体构造/升级正确，尚未在世界中观察水陆跟随、主人效果、主人/其他实体受击、五分钟送花、稀有外观、戒指捕获/释放、Guardian 肢体恢复、Mummy 拳套、Bat 酒杯/吸血、Beaver 零食/冷却、Deer 战锤点燃、Greedy 样本定位/拾物/骑乘、Dragon 拾棍/喂金/抚摸/经验/攻击、Fairy 魔法/五类支援/死亡救援/半透明渲染、Headless 虚弱/头型伤害，以及 Chimera 成长/骑乘/三头攻击/Goat 分离、Goat 森林转化/警铃、Shub 骑乘/生成、Spawn 寻敌/爆发、Beholder 发光/射线/吞食/失明免疫和各自动画。
- `lighted_air` 尚未覆盖两只 Cthulhu 同格、区块卸载、死亡、戒指收纳、换维度和存档重载场景。
- Blacksmith 尚未在世界中回归 1 铁锭/1 铁块喂入、179 铁时喂铁块到 188 的上游容量边界、死亡返还、同主/异主升级、坐下暂停、400 tick 冷却、石头/圆石整堆消耗和第一件装备修理。
- 当前 Bat、Beaver、Beholder、Cthulhu、Deer、Dragon、Goat、Greedy、Guardian、Headless、Mummy、Shub Niggurath 共 12 类实现 `IBlacksmithUpgradable`。Beaver 已明确修正上游“重复接受升级、反复耗铁但效果不叠加”的缺陷为一次性；Goat/Shub 的警铃和 Beholder 的主人失明免疫也已形成静态闭环。Blacksmith 自身、Devil、Fairy、Chimera、Otherworld Bird 按 1.81.4 不可升级。
- 本地没有 Curios API 本身，但原生槽已覆盖 Blacksmith 石头修理、Greedy 5 格磁吸、Dragon/Beaver/Cthulhu/Devil/Mummy/Bat 等效果刷新，以及 Fairy 冷却、Guardian 保命、Headless 增伤/虚弱和 Beholder 发光的部分事件路径。Goat/Shub/Chimera/Spawn 等尚未把所有世界 AI 等价迁入 `curioTick`；这些行为和完整存档仍需世界回归。
- 固定上游的 `DRAGON_RIDE` advancement 没有可达触发路径；本地虽然已为 Wild Hunt 移植通用 ritual trigger，但没有为这条不可达进度伪造触发点，仍不能把 Greedy 骑乘实现写成该进度已完成。
- Familiar shard/复活目前只有静态、Bootstrap 和主菜单证据；尚未在真实世界验证 `doMobLoot`、环境/玩家致死、在线主人直送、满背包回落、无主/离线/跨维度主人尸体掉落、Guardian 最后一命、Headless 待重建状态、UUID 冲突退还和完整仪式消费。
- 单只 Greedy 的完整方块搜索最坏检查 `21 * 21 * 11 = 4,851` 个位置，并以 200-399 tick 退避；当前没有大量 Greedy 同时活动时的 tick 性能回归。

这些缺口不阻止当前构建和客户端主菜单加载，但在完成游戏内场景回归前，Blacksmith/现有 12 类目标的升级链只能标记为“大部分完成、世界行为未验证”。

### P1：旧测试世界兼容性

Nautilus armor 部分已于 2026-07-26 关闭：8 个 ID 经存档注册表反查确定为 `copper/diamond/golden/iron_nautilus_armor` 及对应 4 个 `ritual_dummy/misc_*_nautilus_armor`（注册名含斜杠），已由新增的 `registry/ModMissingMappings.java` 统一 `ignore()`（依据 `en_us.lang:1498` 的有意不移植决定，无等价替代物故不 remap），并有 7 项契约测试固定。Headless 头型 byte 的语义变更仍未处理，见下段。另一个仅影响开发快照的风险是 Headless 头型 byte 已从旧本地 18 类映射切换为上游七类 ID：旧实体保存的 `OccultismHeadlessHead=3/5/6/7` 会改变含义，其余扩展值会变为未知；当前没有数据修复器。

### P1：工程和发布信息未整理

- 根目录 `.git` 是空目录，不是有效 Git 仓库；当前移植代码没有可用提交历史、分支或远端信息。
- 2026-10-16 已移除 `gradle.properties:3` 的本机 `org.gradle.java.home`；fresh clone/CI 不再读取本机路径，local wrapper 通过 `JAVA_HOME` 选择项目 JDK。
- `.github/workflows/deploy.yml` 只在版本 tag 上触发，且没有常规 build/test job。
- `CHANGELOG.md` 仍是模板内容。
- JAR 元数据仍为版本 `1.0.0`、作者 `Developer`、通用描述，URL、credits、update JSON 和 logo 为空。
- 9 个调试物品仍保留注册 ID，但已从创造栏隐藏；发布前仍应决定是否保留兼容注册。

### P2：仪式运行状态的硬编码显示名（本轮已修复）

原状况：`rituals.json` 的 `display_name` 是直接显示字符串，`BlockGoldenSacrificialBowl` 把它作为 String 参数传入通用开始/中断/完成消息，因此中文客户端执行 `familiar_fairy` 时显示"已启动仪式：Fairy Familiar"。

2026-07-26 已修复：`RitualDefinition` 新增可空 `displayNameKey` 与 `createNameComponent()`，`RitualRecipeManager` 按 `ritual.occultism.<id>.name` 生成键，消息发送点改传 `ITextComponent` 嵌套参数，无键时回退 `display_name`；两个 lang 文件各新增 170 个 `.name` 键（总键数 2,939，键集合一致）。`display_name` 字段本身按 JEI 启发式依赖保持英文原文。详见 `PORTING_STATUS_SUMMARY.md`。

### P2：上游同步边界

代码比较基线是 `version/1.20.1` / `84b8ad4` / Occultism `1.81.4`，但 `modonomicon_index.json:2` 又声明手册基础资源来自 `occultism-26.1.2`。本地 170 条仪式中也有大量不属于参考目录 66 个 ritual JSON 的条目。当前移植已经形成多来源分叉，后续必须为每个功能记录语义基线；Blacksmith、Fairy、现有 12 类升级目标、五实体生存链及本批 Familiar shard/复活语义明确采用 1.81.4，只借用 26.1.2 的资源结构和明显 bug 修正。Otherworld Bird 的 Double Jump 省略、Djinni 机器操作员的 FoliotJob/同维度 handler 兼容、虚拟存储的本地物理槽聚合，以及 Beaver 一次性升级、Beholder 可达射线计时、Guardian 单 shard、Headless 状态保留、裂魂排除和易碎灵魂单次掉落均是单独记录的缺陷/兼容策略，不应误写成逐行兼容。

## 建议的后续顺序

1. 在隔离测试世界串联执行 `familiar_blacksmith` 和 12 类升级目标：验证召唤/owner、喂铁与容量边界、400 tick 同主升级及异主/坐下拒绝，并覆盖 Bat 吸血、Beaver 零食、Cthulhu 动态光、Deer 战锤、Dragon 飞剑、Goat/Shub 警铃、Greedy 定位拾物、Guardian 恢复、Headless 虚弱、Mummy 永久攻击和 Beholder 失明免疫；同步检查戒指 NBT、外观动画、Dictionary/JEI 与 `lighted_air` 清理。
2. 在同一世界完整执行 `familiar_chimera` -> 喂肉成长/骑乘/三头攻击 -> 金苹果分离 Goat -> 森林任意顺序使用黑染料、燧石、末影之眼 -> Shub 骑乘 Cthulhu 并生成 Spawn -> `familiar_beholder` 发光/射线/吞食强化；同时逐类验证死亡 shard 的在线主人/满背包/无主交付、canonical NBT、Guardian/Headless 特例、`summon_foliot` 复活和施法者 owner 重绑定。
3. 为十条 possession ritual（八条 canonical + Hoglin/Warden 兼容条目）做一次客户端加载烟测和隔离世界回归：验证十个实体 ID/renderer（含 Phantom 上游 16 chunks、本地 Forge 256 blocks tracking range、Warden 上游 48 chunks/本地 768 blocks）、和平难度存活、Skeleton 日晒、Phantom 盘旋/俯冲、Hoglin 近战与模型、Warden 脉冲/失明与模型、SpecialSpawn 与实际加入世界、Demon’s Dream 全部可表达治疗类型、十张表的击杀掉落、四鱼种稀有池、附近玩家原版 trigger、四条隐藏进度及 Dictionary 实体预览。Hoglin 的现代 Brain、Warped Fungus、Zoglin 转化和现代 loot，以及 Warden 的 Brain、振动、Darkness、钻地和真正 sonic boom 仍另列差异；刀具 Tallow 已接线但需世界回归。
4. 在隔离运行目录接受 EULA，完成专用服务器启动、玩家进入和正常关服烟测。
5. 为 ItemStack/NBT 往返、实体死亡/复活、捕获/释放、库存满载、离线/跨维度主人、区块卸载和存档重载补自动化或可复现集成场景。
6. 在隔离世界完整执行 `summon_wild_hunt`：验证献祭/材料消费、3 首领和 3-8 随从范围、护盾与友军免疫、创造/虚空绕过、日照/和平、Looting/Iesnium 屠刀叠加、恶魔之梦治疗、附近玩家原版 trigger 和隐藏进度；随后覆盖区块卸载/存档重载，重点确认已加载实体的 UUID 主从重连。
7. 继续完善 Familiar Ring 原生单槽：foil、类型本地化和非 `IFamiliar` 运输实体的装备边界已完成，下一步集中于 Goat/Shub/Chimera/Spawn 等剩余 `curioTick`/事件及真实世界中的装备、取下、`familiarType` loot/give、死亡 Clone、维度切换和完整 NBT 回归；settings 已按上游由 Controls 中的 16 个按键提供，不需要另建独立 GUI，不能用普通背包 tick 代替装备语义。
8. 明确旧开发存档兼容策略，并处理 Nautilus armor 的 missing mappings。
9. 若继续存储/自动化线，把动态容量、超大堆叠、虚拟搜索/排序、客户端缓存、有界栈同步、第一阶段终端合成、容器级瞬时 recipe lock 和同维度已加载 linked machine/order 最小路由作为已完成基线：下一步实现完整 JEI/配方书协同、remote/wormhole capability proxy、跨网络虚拟缓存和更完整的跨网络侧面策略；`manage_machine` 当前已有同维度 PathNavigate/状态机/严格 MachineReference/简化 DepositOrder/handler 路径，仍需专用 GUI、跨维度/区块加载和真实机器回归。不能只增加槽数或复用 Foliot 搬运逻辑标记完整上游存储；tier0/5/dark 当前仅是已明确的本地容量策略。
10. 明确移植目标：若目标是“1.12 可玩版”，把剩余替代实体和 Hoglin/Warden 的兼容行为列为设计决定；若目标是“1.81.4 等价版”，下一阶段重点应转向 Warden 的现代 Brain/振动/钻地等不可等价机制、高阶灵体、完整存储网络、其他 advancement trigger 与 Curios/Baubles 等能力的明确兼容方案。
11. 最后处理可移植构建、Git 历史、CI、版本号、作者/credits、CHANGELOG 和发布元数据。

## 关键证据位置

- 工程环境：`README.md`、`gradle.properties`、`build.gradle`
- 主生命周期：`src/main/java/com/occultism/occultism/Occultism.java`
- 注册：`src/main/java/com/occultism/occultism/registry/ModBlocks.java`、`ModItems.java`、`ModEntities.java`
- 法阵：`src/main/java/com/occultism/occultism/ritual/PentacleDetector.java`
- 仪式数据：`src/main/resources/assets/occultism/rituals/rituals.json`
- 仪式执行：`src/main/java/com/occultism/occultism/tileentity/TileEntitySacrificialBowl.java`
- 职业灵体：`src/main/java/com/occultism/occultism/entity/EntityFoliotJob.java`
- 高阶通用灵体：`src/main/java/com/occultism/occultism/entity/EntityBoundSpirit.java`
- Wild Hunt：`entity/EntityWildHuntSkeleton.java`、`EntityWildHuntWitherSkeleton.java`、`entity/IWildHuntEntity.java`、`TileEntitySacrificialBowl.spawnWildHunt()`、`assets/occultism/loot_tables/entities/`、`advancement/RitualTrigger.java` 与 `assets/occultism/advancements/occultism/summon_wild_hunt.json`；固定上游对照为 `common/entity/spirit/WildHuntSkeletonEntity.java`、`WildHuntWitherSkeletonEntity.java` 与 `common/ritual/SummonWildHuntRitual.java`
- 专用 Possessed：`entity/EntityPossessedEndermite.java`、`EntityPossessedSkeleton.java`、`EntityPossessedEnderman.java`、`EntityPossessedGhast.java`、`EntityPossessedWeakShulker.java`、`EntityPossessedShulker.java`、`EntityPossessedElderGuardian.java`、`entity/EntityPossessedPhantom.java`、`entity/EntityPossessedHoglin.java`、`entity/EntityPossessedWarden.java`、`client/model/ModelPossessedPhantom.java`、`client/model/ModelPossessedHoglin.java`、`client/model/ModelPossessedWarden.java`、`client/render/RenderPossessedPhantom.java`、`client/render/RenderPossessedHoglin.java`、`client/render/RenderPossessedWarden.java`、`entity/PeacefulRemovalGuard.java`、`registry/ModEntities.java`、`client/render/ModEntityRenderers.java`、`assets/occultism/loot_tables/entities/possessed_*.json`、`assets/occultism/textures/entity/possessed_phantom.png`、`assets/occultism/textures/entity/possessed_warden.png`、`assets/occultism/advancements/occultism/possess_*.json`；固定上游对照为 `common/entity/possessed/` 与 `common/ritual/SummonRitual.java`
- Possessed 联动与剩余边界：`item/ItemDemonDreamFood.java`、`client/gui/GuiDictionaryOfSpirits.java#getLocalEntitySubstitute()`、`tileentity/TileEntitySacrificialBowl.java`、`assets/occultism/lang/en_us.lang`、`zh_cn.lang`、`assets/occultism/dictionary/modonomicon_index.json`
- Cthulhu 核心：`src/main/java/com/occultism/occultism/entity/EntityCthulhuFamiliar.java`、`block/BlockLightedAir.java`
- Cthulhu 客户端：`src/main/java/com/occultism/occultism/client/model/ModelCthulhuFamiliar.java`、`client/render/RenderCthulhuFamiliar.java`
- Blacksmith 核心：`src/main/java/com/occultism/occultism/entity/EntityBlacksmithFamiliar.java`、`entity/IBlacksmithUpgradable.java`、`handler/LootEventHandler.java`、`config/ModConfig.java`
- Blacksmith 客户端：`src/main/java/com/occultism/occultism/client/model/ModelBlacksmithFamiliar.java`、`client/render/RenderBlacksmithFamiliar.java`
- Guardian/Mummy 升级：`src/main/java/com/occultism/occultism/entity/EntityGuardianFamiliar.java`、`entity/EntityMummyFamiliar.java`、`client/model/ModelMummyFamiliar.java`
- Bat 升级：`src/main/java/com/occultism/occultism/entity/EntityBatFamiliar.java`、`potion/PotionBatLifesteal.java`、`registry/ModPotions.java`、`handler/FamiliarEventHandler.java`、`client/model/ModelBatFamiliar.java`
- Deer/Headless 升级：`src/main/java/com/occultism/occultism/entity/EntityDeerFamiliar.java`、`entity/EntityHeadlessFamiliar.java`、`entity/EntityDevilFamiliar.java`、`handler/FamiliarEventHandler.java`、`client/model/ModelDeerFamiliar.java`、`client/model/ModelHeadlessFamiliar.java`
- Beaver 升级：`src/main/java/com/occultism/occultism/entity/EntityBeaverFamiliar.java`、`registry/ModItems.java`、`src/main/resources/assets/occultism/models/item/beaver_nugget.json`、`textures/item/beaver_nugget.png`
- Greedy 升级：`src/main/java/com/occultism/occultism/entity/EntityGreedyFamiliar.java`、`client/model/ModelGreedyFamiliar.java`、`client/render/RenderGreedyFamiliar.java`、`src/main/resources/assets/occultism/textures/entity/greedy_familiar.png`、`greedy_familiar_chest.png`、`greedy_familiar_christmas.png`、`textures/gui/book/familiar_greedy.png`
- Dragon 静态闭环：`src/main/java/com/occultism/occultism/entity/EntityDragonFamiliar.java`、`EntityThrownSword.java`、`EntityGreedyFamiliar.java`、`item/ItemFamiliarRing.java`、`registry/ModEntities.java`、`registry/ModItems.java`、`registry/ModPotions.java`、`potion/PotionDragonGreed.java`、`handler/LootEventHandler.java`、`client/model/ModelDragonFamiliar.java`、`client/render/RenderDragonFamiliar.java`、`RenderThrownSword.java`
- Fairy 静态闭环：`src/main/java/com/occultism/occultism/entity/EntityFairyFamiliar.java`、`entity/IFamiliar.java`、`entity/IBlacksmithUpgradable.java`、`handler/FamiliarEventHandler.java`、`potion/PotionFairySlowFall.java`、`item/ItemFamiliarRing.java`、`EntityThrownSword.java`、`registry/ModEntities.java`、`registry/ModItems.java`、`registry/ModPotions.java`、`client/model/ModelFairyFamiliar.java`、`client/render/RenderFairyFamiliar.java`、`ModEntityRenderers.java`
- Goat/Shub/Spawn 派生链：`src/main/java/com/occultism/occultism/entity/EntityGoatFamiliar.java`、`EntityShubNiggurathFamiliar.java`、`EntityShubNiggurathSpawn.java`、`client/model/ModelGoatFamiliar.java`、`ModelShubNiggurathFamiliar.java`、`ModelShubNiggurathSpawn.java`、`client/render/RenderGoatFamiliar.java`、`RenderShubNiggurathFamiliar.java`、`RenderShubNiggurathSpawn.java`
- Beholder 静态闭环与戒中免疫：`src/main/java/com/occultism/occultism/entity/EntityBeholderFamiliar.java`、`client/model/ModelBeholderFamiliar.java`、`client/render/RenderBeholderFamiliar.java`、`util/FamiliarUtil.java`
- Chimera 静态闭环：`src/main/java/com/occultism/occultism/entity/EntityChimeraFamiliar.java`、`client/model/ModelChimeraFamiliar.java`、`client/render/RenderChimeraFamiliar.java`
- Familiar 灵魂碎片与复活：`src/main/java/com/occultism/occultism/entity/EntityFamiliarBase.java`、`entity/IFamiliar.java`、`entity/EntityGuardianFamiliar.java`、`entity/EntityHeadlessFamiliar.java`、`entity/EntityDemonicPartner.java`、`item/ItemSoulShard.java`、`handler/PlayerEventHandler.java`、`tileentity/TileEntitySacrificialBowl.java`
- Familiar effects/settings 基础：`src/main/java/com/occultism/occultism/entity/IFamiliar.java`、`entity/EntityBatFamiliar.java`、`EntityBeaverFamiliar.java`、`EntityCthulhuFamiliar.java`、`EntityDeerFamiliar.java`、`EntityDevilFamiliar.java`、`EntityDragonFamiliar.java`、`EntityMummyFamiliar.java`、`util/FamiliarUtil.java`、`capability/FamiliarSettings.java`、`registry/ModCapabilities.java`、`handler/FamiliarSettingsHandler.java`
- Familiar Ring 审计：`src/main/java/com/occultism/occultism/item/ItemFamiliarRing.java`、`capability/FamiliarRingCapability.java`、`handler/FamiliarRingHandler.java`、`entity/IFamiliar.java`、`handler/FamiliarEventHandler.java`、`handler/LootEventHandler.java`；当前 `build.gradle`/`gradle/scripts/dependencies.gradle` 无 Baubles/Curios 依赖，使用原生单槽 capability
- Familiar advancement trigger：`src/main/java/com/occultism/occultism/advancement/FamiliarTrigger.java`、`registry/ModAdvancements.java`、`handler/FamiliarEventHandler.java`、`handler/PlayerEventHandler.java`、各 `entity/Entity*Familiar.java` 成功路径、`assets/occultism/advancements/occultism/familiar_*.json`（25 个 Familiar advancement）、`assets/occultism/lang/en_us.lang`、`zh_cn.lang`、`src/test/java/com/occultism/occultism/advancement/FamiliarTriggerPortContractTest.java`、`FamiliarRareVariantPortContractTest.java`、`FamiliarUpgradeAdvancementPortContractTest.java`
- Familiar settings 客户端入口：`src/main/java/com/occultism/occultism/client/ClientFamiliarSettingsHandler.java`、`src/main/java/com/occultism/occultism/Occultism.java`
- Familiar settings 网络：`src/main/java/com/occultism/occultism/network/MessageToggleFamiliarSettings.java`、`MessageSyncFamiliarSettings.java`、`ModNetworking.java`
- 手册索引与 GUI：`src/main/resources/assets/occultism/dictionary/modonomicon_index.json`、`src/main/java/com/occultism/occultism/client/gui/GuiDictionaryOfSpirits.java`
- 辞典调试权限门：`src/main/java/com/occultism/occultism/item/ItemDictionaryOfSpirits.java:102`
- 存储动态容量、终端合成、Proxy API 与矩阵持久化证据：`src/main/java/com/occultism/occultism/tileentity/TileEntityStorageController.java`、`tileentity/TileEntityStableWormhole.java`、`api/common/blockentity/IStorageController.java`、`api/common/blockentity/IStorageControllerProxy.java`、`api/common/data/GlobalBlockPos.java`、`container/ContainerStorageController.java`、`client/gui/GuiStorageController.java`、`network/MessageSetRecipe.java`、`src/test/java/com/occultism/occultism/tileentity/StorageControllerContractTest.java`、`StorageProxyCapabilityContractTest.java`、`src/test/java/com/occultism/occultism/container/StorageCraftingContractTest.java`；控制器自身也实现 loaded self-proxy，返回自身和 `GlobalBlockPos`
- 存储网络快照：`src/main/java/com/occultism/occultism/network/MessageRequestStacks.java`、`MessageUpdateStacks.java`、`ModNetworking.java`、`tileentity/TileEntityStorageController.java`、`item/ItemStorageRemote.java`、`client/gui/ClientStorageCache.java`、`src/test/java/com/occultism/occultism/network/StorageStacksNetworkContractTest.java`
- 机器订单/导航兼容：`src/main/java/com/occultism/occultism/spirit/MachineReference.java`、`spirit/DepositOrder.java`、`entity/EntityFoliotJob.java`、`network/MessageRequestOrder.java`、`src/test/java/com/occultism/occultism/resource/MachineManagementPortContractTest.java`
- 跨维度存储访问：`src/main/java/com/occultism/occultism/item/ItemStorageRemote.java`、`block/BlockStableWormhole.java`、`tileentity/TileEntityStableWormhole.java`、`gui/ModGuiHandler.java`
- 静态资源检查：`tools/verify_assets.ps1`
- 契约测试：上述测试集另新增 `src/test/java/com/occultism/occultism/resource/FamiliarRingCapabilityPortContractTest.java`，覆盖原生单槽边界、生命周期、网络取下、`familiarType` 转换、Beholder 装备免疫和 NBT 状态写回；`PortedResourceContractTest.java` 覆盖跨维度 remote/wormhole GUI 解析契约
- 第三方署名：`THIRD_PARTY_NOTICES.md`、`src/main/resources/THIRD_PARTY_NOTICES.md`
- 当前客户端日志：`run/logs/latest.log`

## 更新本报告时的判定原则

- “基本完成”至少需要：代码存在、资源齐全、构建通过，并有对应静态或运行验证。
- 使用原版实体、通用 AI 或简化工作站承接上游内容时，统一标记为“部分完成”，即使仪式已经可执行。
- ritual dummy、调试物品、模型文件数量和上游文本复制量不计为独立功能完成项。
- 每次重要修改后，应更新快照日期、参考上游提交、构建结果、客户端/专服验证和已知问题列表。
