# 仪式持续校验与中断

## 上游依据

固定参考为 Occultism 1.81.4，提交 `84b8ad4`：

- `GoldenSacrificialBowlBlockEntity.tick` 在等待献祭/物品使用之前先调用 `Ritual.isValid`。
- `Ritual.isValid` 检查启动物、未消耗材料、法阵是否仍有效。
- `stopRitual(false)` 中断时弹出剩余启动物，不生成结果、不退回已经消耗的材料。

## 已接入

`TileEntitySacrificialBowl.update` 每个服务端 tick 都检查当前选中的法阵、启动物及复活碎片的有效性，以及剩余材料。等待献祭/使用物品同样进行校验，不会因为尚未推进时间而跳过检查。

`PentacleDetector.matches` 直接验证选中的图案，不重复搜索全部法阵；读取每个必要格前检查 `isBlockLoaded`，不主动载入缺失区块。材料碗扫描也跳过不可用位置。

`RitualDefinition.matchesRemainingIngredients` 按定义顺序跳过已经消费的前缀，用不改输入的分配计划判断剩余需求。存档中的 `ConsumedAdditionalIngredients` 与既有顺序消费机制兼容；负数或超出总成本的值不会通过校验。后续材料匹配批次已将标准单件成本接入 Forge 分配，细节及计数脚本后备局限见 `RITUAL_MATERIAL_MATCHING_1.12.md`。

无效时中断并返还金碗里当前剩下的物品：

- 法阵、启动物、剩余材料被取走或配方被移除，会停止推进。
- 启动物已被外部取走时不再掉出第二份。
- 启动物被换成错误物品时只返还金碗当前物品，不重新创建原启动物。
- 缓存的 `RitualResult` 不会作为中断兜底产物掉出。
- 已消费的材料不退回，也不在加载后重新要求它们。
- 选中法阵的必要格不可用时中断，不强制加载、不假装检查通过；这是一项明确的 loaded-only 兼容策略。

## 加速成本

加速后的仪式可能比材料项数还短。旧实现使用至少1 tick的消费间隔，可能在结果生成时留下部分材料未消费。

现在结束 tick 的目标消费数固定为全部成本：即使被加速到1 tick，也必须先完成所有剩余消费，否则中断，不生成结果。

## 消息与手册

补齐动态/CraftTweaker 仪式回退的 `occultism.ritual.finished`、`.interrupted`、`.conditions` 中英文通用键。原有四类 per-ritual 消息键不变；无翻译名称键的运行时定义不会因缺失通用句式而只显示原始键名。

Patchouli 双语仪式概览已说明等待时仍需维持法阵与材料、中断退回启动物但不退已用材料。测试后仅重生成书籍资源，没有再次编译。

## 持续校验批次验证

Java 编译通过。只运行以下五个测试类，共48项，0失败/错误：

- `RitualLifecycleTest` 11项：法阵破坏、等待献祭/物品使用时中断、剩余材料取走、已消费材料加载、加速到1 tick全部付费、普通完成不重放、缺失配方/未知法阵、启动物取走/替换、未加载格、高阶粉笔替代、非法计数。
- `RitualSacrificeEventTest` 13项：使用相同内存 World 桩和真实法阵图案，确认前批献祭接线不回归。
- `RitualNameLocalizationContractTest` 11项：增加动态仪式四种回退消息键检查。
- `RitualVisualPortContractTest` 3项，`PossessedEntityPortContractTest` 10项。

共享 `RitualTestWorld` 只在内存中存放方块/TileEntity并捕获物品掉落，访问未加载格会直接报错，以防测试掩盖强制读取。它不启动实际游戏世界、网络或专服。没有使用 computer 或运行全量测试；实际区块边界、多人改材料、运行期脚本变更和断服恢复仍需集成回归。

后续材料匹配批次扩展为六个定向类共56项通过，生命周期类增加到12项；此前48项为历史阶段结果，不是全量结果。

后续注册表批次修复“删完配方后查询又加载默认表”的漏洞：最后一条配方移除及整表清空现在真正保持为空。
生命周期测试改用真实删除接口，并覆盖清空后从 NBT 恢复的献祭等待仪式；详见 `RITUAL_REGISTRY_1.12.md`。
