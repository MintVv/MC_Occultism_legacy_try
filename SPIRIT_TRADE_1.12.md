# 灵体交易配方与存档

## 固定上游依据

- `occultism-upstream` 1.81.4 的 `TraderJob` 按配方 ID 选择交易；每 20 个世界 tick 推进一步，并保存 `spiritTradeId`、`timeToConvert`、`conversionTimer`、`maxTradesPerRound`。
- `OccultismSpiritJobs` 设置异界石交易为 15 步、每轮 4 次；树苗交易为 20 步、每轮 1 次。
- 本地异界岩交易沿用后续版本路线，默认使用与异界石相同的节拍。

## 默认配方

| ID | 职业 | 输入 | 每次输出 | 秒数 / 每轮次数 |
| --- | --- | --- | --- | --- |
| `occultism:spirit_trade/stone_to_otherstone` | `trader_otherstone` | 原版石头及 `stone` 矿物词典 | 2 异界石 | 15 / 4 |
| `occultism:spirit_trade/stone_to_otherrock` | `trader_otherrock` | 原版石头及 `stone` 矿物词典 | 2 异界岩 | 15 / 4 |
| `occultism:spirit_trade/otherworld_sapling` | `trader_otherworld_saplings` | 自然异界树苗 | 1 稳定异界树苗 | 20 / 1 |

短 ID `stone_to_otherstone` 与 `spirit_trade/stone_to_otherstone` 会解析到同一 Occultism 配方；外部命名空间不被覆盖。

## CraftTweaker

脚本类型：`mods.occultism.SpiritTrade`。依赖项目已有的 CraftTweaker 1.12 集成，建议启动前配置脚本，不将服务器热重载或自动同步给客户端视为已实现。

```zenscript
// 替换默认异界石交易：每 7 秒最多兑换 2 个铁锭，每个得到 3 个金锭。
mods.occultism.SpiritTrade.addRecipe(
    "occultism:spirit_trade/stone_to_otherstone",
    "trader_otherstone",
    [<minecraft:iron_ingot>],
    <minecraft:gold_ingot> * 3,
    7,
    2
);

// 使用动态矿物词典输入；不要给矿物词典名添加 ore: 前缀。
mods.occultism.SpiritTrade.addOreRecipe(
    "occultism:spirit_trade/stone_to_otherrock",
    "trader_otherrock",
    "stone",
    <occultism:otherrock> * 2,
    15,
    4
);

// 删除按 ID 选择的交易；不会转而挑选同职业的另一条配方。
mods.occultism.SpiritTrade.removeRecipe("otherworld_sapling");
```

`addRecipe` 的数组是**单个消耗物的候选项**，不是多个同时消耗的成本。每次交易消耗 1 个物品；候选项支持 metadata wildcard，带 NBT 的候选项要求 NBT 相等，无 NBT 的候选项接受任意 NBT。输出及候选项在注册时复制。

### 多项无序成本

`addShapelessRecipe` 的数组表示每次交易必须同时满足的成本，合计 1..9 项；普通物品的数量会展开成重复成本。支持普通物品堆和矿物词典条目，不支持带 transformer/任意条件的复合 CraftTweaker ingredient；这些情况会被拒绝，不会默默丢掉条件。

```zenscript
// 每次消耗 3 个铁锭，产出 2 个金锭；5 秒一轮，最多交易 4 次。
mods.occultism.SpiritTrade.addShapelessRecipe(
    "occultism:spirit_trade/stone_to_otherstone",
    "trader_otherstone",
    [<minecraft:iron_ingot> * 3],
    <minecraft:gold_ingot> * 2,
    5,
    4
);
```

Java 侧可用 `SpiritTradeIngredient.items(...)` 或 `.ore(...)` 组成成本列表，通过 `registerIngredients(...)` 注册。每个成本代表一件物品；各成本可以拥有不同候选项，矿物词典成员在查询时更新。

- `Recipe.isValid(List<ItemStack>)` 不修改输入；`consume(List<ItemStack>)` 深拷贝并返回余料，每项成本只扣一件，不足时返回原数量的副本。
- 多堆输入复用 Forge `RecipeMatcher` 做无序分配，候选单元最多 81 个，不会按库存中的巨大数量展开。`[铁或金, 铁]` 等重叠条件会选择可行分配。
- 固定上游 `SpiritTradeRecipe` 的旧实现会让单项成本从每个匹配堆都扣一件，并且容易被重叠候选顺序影响。本地明确修正这两个问题，不复制误扣材料的缺陷。
- `TraderJob` 上游只提交主手一堆物品；本地保留这个执行边界。重复同类成本可执行，必须同时用到不同物品类型的配方仅能由多堆输入 API 消费，不能宣称工人已经拥有混合原料收集 AI。
- 本地处理器按「成本项数 × 每轮次数」拾取，且不超过物品堆叠上限；已有不足一次交易的余料可从同类、同 metadata/NBT 的来源继续补齐。不同物品不会覆盖手中材料。
- 默认单成本 Patchouli 页继续使用上游布局；多成本页显示最多 3×3 个成本槽，输出与每轮次数不因槽位增加而改变。旧辞典同样列出各项成本。

秒数必须为 `1..107374182`，每轮次数必须为 `1..64`，单次输出必须是非空合法大小的堆叠。注册前校验失败不会删除旧配方。`addOreRecipe` 可使用启动后才有成员的矿物词典名；空表时不进行交易。

新增 ID 不会自动替代默认职业绑定。替换已有 ID 可直接影响该 ID 的使用者；其他 ID 由 Java `EntityFoliotJob.setTradeRecipeId` 或实体 NBT 的 `spiritTradeId` 明确选择。

## 保存与恢复

- 固定交易者写入上述四个上游同名字段，并保留历史 `WorkTimer`。
- 有 `conversionTimer` 时以秒为准；旧存档只含 `WorkTimer` 时取完整秒。负数和越界值会被限制，计时乘 20 不会整数溢出。
- `timeToConvert` 与 `maxTradesPerRound` 为每实体配置。已有存档显式配置不会被注册表替换覆盖；没有显式配置的实体使用当前配方默认值。
- 缺失或格式错误的显式 ID 保留为未解析选择，停止处理并保留手中物品，不退回默认交易；同 ID 恢复后重新可用。
- 切换职业会清除上一个职业的交易选择及覆盖设置。运行时替换当前配方会清除旧配方累计进度。
- Patchouli 交易页绘制时按 ID 查询当前定义；物品查阅索引在书籍重载时更新。

## 尚未覆盖

多成本配方 API、同类多件消耗和书页已实现；原生数据包加载、客户端自动同步自定义交易、专用交易设置 GUI 尚未移植。不同类型材料的收集 AI 不在固定上游主手交易行为中，本地也未新增。固定交易的寻路、容器交付和页面实际渲染仍缺游戏内回归证据。没有使用 computer 或运行全量测试。

2026-10-16 定向验证：多原料 12 项、交易 12 项、说明书 4 项、高阶职业 10 项全部通过；Java 编译通过。覆盖重叠候选穷举、重复成本、OreDictionary 动态成员、NBT、9 项成本上限、大堆栈、余料补入、存档字段和配方移除。上述 ZenScript 示例尚未在游戏中执行，不能将 Java 测试计为脚本运行验证。
