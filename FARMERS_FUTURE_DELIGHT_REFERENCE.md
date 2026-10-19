# 未世之乐源码参考

记录日期：2026-10-19（Asia/Shanghai）。

## 来源与位置

- 项目：Farmer's Future Delight（未世之乐）。
- 用户提供的介绍页：https://www.mcmod.cn/class/29546.html
- 源码仓库：https://github.com/xy177/Farmers-Future-Delight
- 本地目录：`_reference/farmers-future-delight/`。
- 参考分支：`main`。
- 固定提交：`f15fd3f167a0b6be27eb7d449e962388eb6e1d6e`。
- 该提交日期：2026-10-19。
- 根目录许可证：MIT；原始 `LICENSE` 保留在参考目录中。

## 下载范围

此目录采用 Git partial clone 和 sparse checkout，下载全部 Java 源码、
构建声明、许可证及非 OGG 资源。仓库含 966 个 OGG 文件，本次不下载这些音频，
也不下载完整 Git 历史。音频路径和版本仍保留在固定提交的 Git 索引中。

需要完整音频工作副本时，可在网络条件允许后执行：

```powershell
git -C _reference/farmers-future-delight sparse-checkout disable
```

这会继续下载缺失资源，不要将当前无音频工作副本当成已经验证可独立构建的完整发行工程。

## 与 Occultism 的边界

- 这是本地独立上游参考仓库，不是已完成的运行期兼容。
- 不把第三方源码直接复制到 Occultism 的 `src/main/java`。
- 不添加 Gradle 子项目，不添加强制或可选运行依赖，不改实体、配方或存档 ID。
- `_reference/` 沿用主项目的忽略规则，源码不进入 Occultism jar，也不会作为主仓库普通文件提交。
- 目录中上游自带的装饰内容仅作为参考保留，不因此注册到 Occultism。
- 后续复用具体代码或资源时，须另外核对该文件的来源、版权声明、依赖和行为；
  保留适用许可声明，并记录实际移入的文件。本次没有改变主模组的第三方分发范围。

## 验证范围

按固定提交的 Git tree 对照工作副本：

- Java 源码：383 个，应有383个，实际383个。
- 非 OGG 文件：2672 个，缺失0个。
- OGG 音频：上游966个，本地0个，按 sparse checkout 显式省略。
- 已跟踪文件修改：0；上游工作副本干净。
- 主仓库的 `git check-ignore` 确认该参考目录沿用 `_reference/` 忽略规则。

常用入口：

- 实体定义：`src/main/java/xy177/farmersfuturedelight/common/registry/FFDEntities.java`。
- 幻翼实现：`src/main/java/xy177/farmersfuturedelight/common/entity/EntityPhantom.java`。
- 兼容入口：`src/main/java/xy177/farmersfuturedelight/common/FFDCompat.java`。
- 模组主类：`src/main/java/xy177/farmersfuturedelight/FarmerFutureDelight.java`。

以上路径均相对于参考目录。本次仅核对本地文件、固定提交和 Git 工作副本，不执行上游构建脚本。
不编译 Occultism，不运行全量测试，不启动客户端或专服，不使用 computer。

## 实际移入的资源（2026-10-19）

按上文「复用具体代码或资源时须核对来源、保留许可声明、记录实际移入的文件」的要求补充记录。

许可核对：该仓库根目录 `LICENSE` 为 MIT；本次只搬移**美术资源与模型几何**，未搬移任何 Java 代码。

移入 Occultism 的文件（来源均为 `_reference/farmers-future-delight/src/main/resources/assets/farmers_future_delight/`）：

| 目标文件 | 说明 |
| --- | --- |
| `assets/occultism/textures/block/bee_nest_bottom.png` | 蜂巢模型底面 |
| `assets/occultism/textures/block/bee_nest_top.png` | 蜂巢模型顶面 |
| `assets/occultism/textures/block/bee_nest_front.png` | 蜂巢模型正面 |
| `assets/occultism/textures/block/bee_nest_side.png` | 蜂巢模型侧面 |
| `assets/occultism/models/block/bee_nest.json` | 由 `models/block/bee_nest_empty.json` 改名空间而来 |
| `assets/occultism/textures/block/amethyst_block.png` | 紫水晶块 |
| `assets/occultism/textures/block/budding_amethyst.png` | 紫水晶芽床 |
| `assets/occultism/textures/item/nautilus_shell.png` | 鹦鹉螺壳 |
| `assets/occultism/textures/item/phantom_membrane.png` | 幻翼膜 |

许可声明记录在 `THIRD_PARTY_NOTICES.md`。该仓库不提供钟、强化深板岩、野性纹饰，这三项仍为本移植自建或派生。
