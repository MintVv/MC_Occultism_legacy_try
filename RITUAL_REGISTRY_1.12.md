# 仪式注册表与脚本清空

更新：2026-10-16（Asia/Shanghai）。

## 修复范围

`RitualRecipeManager` 以前以 `definitions.isEmpty()` 判断是否需要加载默认 JSON。
删除最后一条仪式后，金碗、辞典或 JEI 的下一次查询会重新加载内置配方；
缺失或损坏的资源也可能在每次查询时重复解析和输出错误。

现在只用 `null` 表示尚未初始化。成功加载的空数组、脚本删除后的空表和加载失败后的空表，
都是已初始化状态。查询不会隐式恢复默认配方。

- `getDefinitions()` 只在尚未初始化时加载内置资源。
- `Occultism.init` 使用同一个惰性入口，不再无条件覆盖此前已经加载/修改的表。
- `load()` 保留为明确的 Java 重载入口；调用会替换脚本修改，不能当作普通查询使用。
- 加载仍为整表发布：全部解析成功才发布新表；出错时发布空表，不发布已解析的部分配方。
- 加载失败不自动重试，修复后须明确重载或重启；本批没有新增游戏内重载命令。
- 读写通过注册表锁协调，查询返回不可修改的列表快照；后续增删不会改变已返回的旧列表。

这是一项 1.12 本地注册生命周期修复，不是将现代数据包重载系统完整移植到 1.12。
本批没有变更170条内置仪式、六条 prototype 桥接或装饰方块范围。

## CraftTweaker

既有 `mods.occultism.Ritual.removeById` 和 `removeByPentacle` 不变。
新增 `removeAll()`，通过与现有接口相同的 `IAction` 应用。

示例先清空默认表，再添加唯一的自定义仪式：

```zenscript
import mods.occultism.Ritual;

Ritual.removeAll();
Ritual.addCrafting(
    "pack_only", "summon_foliot", <minecraft:book>, <minecraft:diamond>, 200,
    [<minecraft:iron_ingot>, <minecraft:gold_ingot>]
);
```

`removeAll()` 可以重复调用。删除之后新增只加入显式指定的配方，不恢复默认配方。
Java `RitualRecipeManager.removeAll()` 返回移除数量；ZenScript 入口不返回数量，与现有删除入口一致。

示例尚未通过真实 CraftTweaker/ZenScript 启动验证。现有仪式原料转换对复杂条件、NBT和动态矿物词典的
限制没有在本批改变，不能据此宣称所有 `IIngredient` 条件均已支持。

## 活动仪式

金碗仍按每 tick 的真实配方查询结果检查持续有效性。当唯一配方被删除或整表清空：

- 等待献祭和运行中的仪式都不能通过隐式重载恢复。
- 从 NBT 恢复的活动仪式同样会在下一个服务端 tick 中断。
- 只返还金碗当前启动物，不发放缓存结果，不再次返还已取走物品，不退回已消费材料。

这些行为复用既有生命周期中断机制；本批修复了空注册表绕过该机制的入口。
同 ID 替换运行中仪式、配方客户端同步和 GUI 缓存刷新不属于本批已验证范围。

## 验证

新增 `RitualRecipeManagerTest`，覆盖空数组/包装根、最后一条按 ID 或法阵删除、
重复清空后新增、缺失/损坏/读取失败资源、整表失败不发布部分结果、显式重载和不可修改快照。
空表查询检查同时捕获注册表日志，避免默认资源本身加载失败掩盖重复重载。

`RitualLifecycleTest` 改为通过真实删除入口检查最后一条配方消失，并追加清空后恢复等待仪式的中断测试。
测试使用内存 World 桩，不代表真实服务器、脚本加载顺序、GUI 或网络流程验证。

本批合并运行一次：

```bat
gradlew-local.bat test --tests com.occultism.occultism.ritual.RitualRecipeManagerTest --tests com.occultism.occultism.ritual.RitualLifecycleTest --tests com.occultism.occultism.ritual.RitualSacrificeEventTest --tests com.occultism.occultism.ritual.RitualDataContractTest
```

Java 编译通过；注册表9、生命周期13、献祭13、仪式数据4，共39项，0失败/错误/跳过。
只执行上述四个类，不使用 computer、不启动客户端/专服、不运行全量测试。
