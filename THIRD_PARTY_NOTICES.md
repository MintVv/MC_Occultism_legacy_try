## Occultism upstream assets and references

This project contains assets and implementation references derived from the
upstream Occultism project:

- Source: https://github.com/klikli-dev/occultism
- Reference checkout: `_reference/occultism-upstream`
- Copyright: Copyright 2020 klikli-dev
- License: MIT License

The copied/ported material is used under the following license notice:

```text
MIT License

Copyright 2020 klikli-dev
Copyright 2020 MrRiegel, Sam Bassett

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

The storage-terminal recipe-ID transfer and 3x3 ingredient layout are adapted
from the pinned upstream `MessageSetRecipeByID` and storage recipe-transfer
implementation, which also credit MrRiegel and Sam Bassett.

## Patchouli runtime dependency

The 1.12 guide integration uses the external Patchouli `1.0-23.6` artifact
(`vazkii.patchouli:Patchouli:1.0-23.6`) from BlameJared Maven. Patchouli classes
and textures are not bundled in the Occultism jar. The generated guide content
derives from the Occultism material credited above; the integration pages are
part of this port.

## Modonomicon textures retained for the legacy guide

This project includes a small subset of UI textures from Modonomicon 26.1.2 to
render the 1.12 Dictionary of Spirits with the same upstream book/category/node
resource paths used by Occultism's Modonomicon data.

- Source: https://github.com/klikli-dev/modonomicon
- Reference checkout: `_reference/modonomicon-26.1.2`
- Revision: `7a94a5b` on `version/26.1.2`
- Included paths: `src/main/resources/assets/modonomicon/textures/gui/**`
- License: CC-BY-4.0 for Modonomicon resources unless otherwise noted by
  Modonomicon's `REUSE.toml`; the included category buttons are derived from
  Arcana assets and are also documented by upstream Modonomicon as CC-BY-4.0.

## Vemerion Familiar models, textures, and behavior

The Dragon, Fairy, Goat, Shub Niggurath, Shub Niggurath Spawn, Beholder, and
Chimera Familiar model geometry, textures, renderer behavior, animation logic,
and familiar behavior are adapted from Occultism material authored by
vemerion:

- Source: https://github.com/klikli-dev/occultism
- Reference checkout: `_reference/occultism-upstream`
- Copyright: Copyright 2021 vemerion
- License: MIT License

The adapted material is used under the following license notice:

```text
MIT License

Copyright 2021 vemerion

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Farmer's Future Delight (未世之乐)

- 来源：https://github.com/xy177/Farmers-Future-Delight
- 许可：MIT License
- 使用范围：以下贴图与模型按 MIT 条款引入，用于本移植的 1.13+ 原版内容替身：
  - `textures/block/bee_nest_{bottom,front,side,top}.png` 与 `models/block/bee_nest.json`
  - `textures/block/amethyst_block.png`
  - `textures/block/budding_amethyst.png`
  - `textures/item/nautilus_shell.png`
  - `textures/item/phantom_membrane.png`
- 说明：仅搬移美术资源与模型几何，未引入其代码。
