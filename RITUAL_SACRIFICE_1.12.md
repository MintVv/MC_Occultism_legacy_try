# 仪式献祭事件移植

## 上游依据

固定参考：Occultism 1.81.4，`_reference/occultism-upstream` 提交 `84b8ad4`。

- `common/blockentity/GoldenSacrificialBowlBlockEntity.onLivingDeath` 要求伤害实际来源是玩家，死亡方块到金碗的距离平方不超过 64，类型符合配方献祭要求。
- `common/ritual/Ritual.isValidSacrifice` 只检查类型，不要求 `isAlive`；死亡事件发生时生命值已为零。
- `generated/resources/data/occultism/tags/entity_types/humans.json` 包含村民标签和玩家。

## 行为

先启动仪式，然后在金碗 8 格内由玩家击杀要求的生物。金碗不再搜索并自动攻击/强制删除附近实体。仅放置活的祭品不会推进仪式、也不会消耗祭品生命。

`RitualSacrificeHandler` 监听服务端 `LivingDeathEvent`：

1. 按配方类型、玩家归因和死亡方块范围，记录事件发生时已加载且正在等待献祭的金碗。
2. 在该世界 `WorldTickEvent.END` 再确认原事件未取消、受害者仍为零生命，且金碗和仪式运行代次没有变化。
3. 满足条件才设置 `SacrificeProvided` 并同步；不会再次伤害受害者，也不改动其原版掉落流程。

玩家箭矢通过 `DamageSource.getTrueSource()` 归因，可以计入；环境伤害或非玩家生物击杀不计入。无需是启动仪式的玩家，单次死亡可供范围内多个符合要求的活动仪式使用，与固定上游监听模型一致。

死亡点取 `floor(posX, posY, posZ)`；1.12 的 `Entity.getPosition()` 会额外给 Y 加 0.5，因此这里不直接调用它。范围是方块坐标的三维距离，不是旧的水平 8 / 垂直 4 的自动搜索包围盒。

## 死亡取消与生命周期

- 捕获阶段采用 LOWEST，先让已有 Guardian/Fairy 等处理救援；最终确认再次读取同一个事件的取消状态，覆盖更晚的同优先级监听器。
- 获救恢复生命、已经取消的死亡不产生献祭进度。
- 金碗被替换、区块不可用、世界卸载、仪式在事件后才开始或 NBT 重载，均不能使用旧的待确认通知。
- 待确认队列只存活至世界 tick 末尾，不载入区块；世界卸载显式清理。确认可比上游即时监听晚一个 tick。
- `SacrificeProvided` 继续使用原存档字段，已完成的献祭加载后不要求再杀一次。运行代次只用于临时队列，不是新的存档进度。
- 极端的同 tick 存档/卸载若发生在死亡确认前，不持久化该待确认事件；区块恢复不会补记旧死亡。真实世界场景仍需回归。

## 人类献祭

`occultism:humans` 现在接受村民或玩家，不再接受女巫。1.12 本地异想商人为村民子类，可通过村民分支表达；不存在现代流浪商人实体的逐类等价。

加入玩家类型不会自动攻击附近玩家：生物类型匹配只由已经发生的玩家击杀事件使用。玩家死亡若被保护效果取消，同样不计入。

## 说明书与兼容边界

- Patchouli 中英文献祭条目与旧辞典说明已改为“先启动，再玩家击杀”；等待提示使用可翻译献祭名称。
- 帕秋莉仪式页与 JEI 也解析原配方中的献祭翻译键，不直接显示标签字符串。
- 现有现代生物标签的明确 1.12 替代继续保留，本批没有扩充现代实体兼容范围。
- 移除旧 `consumeSacrifice`、`hasMatchingSacrifice` 和内部自动搜索/击杀代码；本地其他模块没有调用这两个旧开发接口。外部开发快照调用方需要转用事件接线，不应继续主动伤害祭品。
- 尚未在实际世界完成 Wild Hunt/使魔/天气等仪式的玩家击杀、保护效果、多人、区块卸载和专服集成回归。
- 未使用 computer、未启动客户端或专服、未运行全量测试。内存 World 桩与直接事件调用不等于实际 Forge 加载流程。

## 定向验证

Java 编译通过；仅执行五个测试类，共 47 项，0 失败/错误：

- `RitualSacrificeEventTest` 13 项：存活祭品安全等待、玩家/箭矢归因、环境/生物击杀拒绝、死亡取消和复活、三维8格、多个金碗、humans、旧通知失效、卸载/替换/客户端隔离、已确认 NBT、名称组件。
- `RitualVisualPortContractTest` 3 项，`WildHuntPortContractTest` 11 项，`PossessedEntityPortContractTest` 10 项，`RitualNameLocalizationContractTest` 10 项。

献祭测试注册最小方块实体映射，并用事件桩补充普通 JUnit 缺失的 Forge Cancelable 运行时注入；没有修改生产事件的取消机制。首次验证中的这两类环境缺失已修复，随后同范围通过。帕秋莉生成器 `--check` 通过，仍为英中各9类、203条目，共2065页。
