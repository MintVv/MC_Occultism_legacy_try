# 天气与时间职业移植

## 对照依据

固定源码：`_reference/occultism-upstream`，Occultism 1.81.4，提交 `84b8ad4`。

- `common/entity/job/ChangeWeatherJob.java` 与 Clear/Rain/Thunder 三个子类。
- `common/entity/job/ChangeTimeJob.java`、`DayTimeJob.java`、`NightTimeJob.java`。
- `registry/OccultismSpiritJobs.java` 的默认施法时长。
- `src/main/resources/data/occultism/recipes/ritual/summon_*` 的阶级和寿命。

## 已实现

五条仪式不再在金碗完成时直接修改世界；它们创建已注册的 `EntityDjinniJob` 或 `EntityAfritJob`，在服务端独立施法。仪式展示使用本地职业实体 ID，不将展示 dummy 掉为奖励。

| 职业 | 灵体 | 召唤仪式时长 | 召出后的施法时长 | 最大灵体寿命 |
| --- | --- | --- | --- | --- |
| clear_weather | Djinni | 1200 tick | 300 tick | 60 秒 |
| rain_weather | Afrit | 1200 tick | 600 tick | 120 秒 |
| thunder_weather | Afrit | 1200 tick | 1200 tick | 240 秒 |
| day_time | Djinni | 1200 tick | 100 tick | 60 秒 |
| night_time | Djinni | 1200 tick | 100 tick | 60 秒 |

晴天设置 clear-weather 为 6000 tick，雨/雷计时清零并关闭状态；雨和雷设置 clear-weather 为 0、雨/雷计时为 6000 tick，分别关闭或开启雷暴。天气状态只在施法结束时改变。

昼夜目标按上游计算：

```text
黎明 = floor((当前时间 + 24000) / 24000) * 24000
黄昏 = floor((当前时间 + 11000) / 24000) * 24000 + 13000
```

目标在施法开始时固定；每个 tick 按「目标差值 / 剩余 tick」逐步更新，最后钳制到目标。保留上游分母为 `max(requiredChangeTicks - currentChangeTicks, 1)` 的行为：倒数第二 tick 可达目标，最后一次可将自然昼夜多走的 1 tick 拉回目标。

`WorldEffectJob` 提供可测试的状态与 WorldInfo 变更逻辑；`EntityFoliotJob` 负责服务端调用、挥手、天气烟雾/时间 PORTAL 粒子、结束清理和移除。正常施法结束不生成可重复使用的魂片；中途正常死亡仍沿用本地工人的死亡/魂片路径，魂片中保存施法进度。

## 配置

Forge 配置 `rituals` 下五个独立开关，默认全为 `true`：

```text
enableClearWeatherRitual
enableRainWeatherRitual
enableThunderWeatherRitual
enableDayTimeRitual
enableNightTimeRitual
```

天气开关在施法完成时检查，禁用时不修改天气；时间职业在首次开始时若已禁用则立即结束。施法中禁用时间只跳过后续插值，并在原计时结束时移除灵体。在线施法者收到 `ritual.occultism.disabled` 消息，无主/离线情况不会解引用空玩家。

## 存档

实体 `spiritJob` compound 保存上游字段 `factoryId`、`currentChangeTicks`、`requiredChangeTicks`，时间职业初始化后还保存 `newTime`。本地增加 `completed` 防重复触发；施法者 UUID 用 `WorldEffectCaster` 保存。

保存与神灯收纳保留进度和固定目标，运输暂停施法。改变职业清除旧状态，重用 NBT 容器也会移除旧的 `spiritJob`。缺失字段用默认时长；非法计数钳制到有效范围，不通过负值提前完成。

## 兼容边界

- 复用 1.12 已有职业实体、模型和 Spawn hook，不引入现代 SmartBrain/GeckoLib 运行时。
- 1.12 没有固定上游使用的信标启动音，使用已有附魔台完成音。
- 上游创建了 visual-only lightning 对象但没有加入世界；本地不额外生成会造成伤害或天气变化的雷电事件。
- 本批对齐五条仪式时长、阶级、寿命和职业行为；现有部分 1.12 材料替代保持原样，不声称全 JSON 逐字段一致。
- 不新增装饰方块。不使用 computer，不启动客户端，不运行全量测试。
- 真实世界中的召唤、施法打断、神灯运输、跨维度时钟、配置提示和渲染仍缺实测证据。WorldInfo/无世界实体测试不等于完整游戏内回归。

## 定向验证

Java 编译通过；仅执行以下四个测试类，共 36 项，0 失败/错误：

- `WorldEffectJobPortTest`：12 项，验证 WorldInfo 的天气计时、延迟生效、黎明/黄昏目标、每 tick 上游插值公式、配置禁用、进度续算、完成不重放、非法计数及实体 NBT。
- `RitualDataContractTest`：4 项，验证仪式 ID/实体标签/职业/阶级。
- `HighTierJobSpiritPortContractTest`：10 项，确认共用实体工厂和高阶职业未回归。
- `PossessedEntityPortContractTest`：10 项，确认共享召唤路径、Hook 顺序及其他既有附身行为契约。

首次编译的 1.12 粒子重载歧义已修正；插值测试明确保留上游最后 1 tick 钳制。没有运行全量或游戏客户端。中英文书籍正文在测试后同步更新，生成器检查可单独执行，不需要编译。
