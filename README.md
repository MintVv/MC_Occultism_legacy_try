## OCCULTISM

Minecraft 1.12.2 mod workspace targeting Cleanroom exclusively. Ordinary Forge and Java 8 are no longer supported.

Environment:

- Minecraft 1.12.2
- Cleanroom 0.7.14-alpha
- Java 25 for compilation, tests, and game runtime (class-file major 69)
- Gradle 9.8.1
- CleanroomGradle 0.22.0
- `gradlew-local.bat` respects `JAVA_HOME`, otherwise selects `.jdks/jdk-25.0.5+8` when present. It does not install Java or change system settings.

Non-game checks and packaging:

```bat
.\gradlew-local.bat compileJava
.\gradlew-local.bat test --tests com.occultism.occultism.ritual.RitualLifecycleTest
.\gradlew-local.bat assemble
```

These commands do not start a Minecraft client or server. Do not execute `runClient`, `runServer`, or `tools/run_server_smoke.ps1` on the office computer.

`gradle.properties` intentionally does not set `org.gradle.java.home`; use a Java 25 `JAVA_HOME` on other machines. Gameplay code still uses the Forge-compatible interfaces provided by Cleanroom; switching Java versions does not supply newer Minecraft APIs.

Migration build verification: compilation, 40 targeted tests (five classes), `assemble`, Java 25 artifact/reobfuscation validation, and book checks pass through `gradlew-local.bat`. Minecraft client/server startup and gameplay remain untested for Cleanroom; no full test suite was run.

The wrapper optionally loads `.gradle-local-env.bat` (Git-ignored, scoped by `setlocal`) for machine-specific settings. On this Windows 10 LTSC machine, Java UNIX-domain sockets bind but fail to connect while TCP loopback works. The local file sets `jdk.net.unixdomain.tmpdir` to an intentionally nonexistent project path so the JDK selector falls back to TCP, and selects `Windows-ROOT` as the TLS trust store. Do not create that socket-directory path. These settings propagate to Gradle and its child Java processes without changing system Java, firewall rules, or certificate-validation requirements. Other machines do not need this file unless they have the same issue. Historical Forge verification results in other documents remain historical.

Progression compatibility choices (not upstream-equivalent recipes):

- The Foliot crusher uses iron, gold, copper and silver ingots, matching the pinned 1.21.1 ritual instead of requiring unobtainable raw silver.
- Red impure chalk retains Afrit essence/redstone and uses a poppy instead of torchflower. Orange retains cursed honey/blaze powder and uses glowstone instead of glow berries. Blue retains Marid essence/lapis dust and uses prismarine shard instead of tube coral. Cyan retains iesnium/echo dust and uses ink sac plus glowstone instead of glow ink sac. Every recipe also consumes white impure chalk and still requires spirit fire purification.
- Two obsidian dust, quartz and blaze powder craft one netherite scrap dust. Four scrap dust plus four gold dust retain the existing netherite dust recipe. This is a local Nether-material substitute, not ancient-debris backporting, and requires neither a miner nor Netherite to bootstrap.
- Four amethyst dust in a 2x2 grid make one amethyst block; there is no reverse unpacking recipe. This supplies the existing crystallizer and budding-amethyst rituals without introducing a new shard item.
- Possessed Bee retains the bound Djinni book and uses the upstream chicken sacrifice. Its local sugar/bottle/dandelion costs now use wheat in place of bee nest, avoiding the nest -> pink chalk -> demonic meat -> orange chalk -> honey bootstrap cycle. Bee nests remain obtainable through their separate ritual.
- Patchouli chalk, Hoglin and budding-amethyst entries display the relevant compatibility recipes. The bee entry describes the chicken/wheat route. Survival balance and full gameplay still require in-game validation.

Mineshaft behavior: a successful operation costs one durability; no recipe or insufficient combined output space costs none. Redstone resets progress. Replacing the miner restarts timing. Extractors only access loaded blocks and otherwise use internal output. This port uses deterministic enchantment formulas (30 fewer ticks per Efficiency level, extra rolls equal to Fortune level, count multiplier of Silk Touch level + 1) and always saves a miner when an operation leaves one durability; the corresponding modern upstream configuration toggles are not implemented. These are local compatibility semantics, not full upstream behavior parity.

Storage behavior: active capacity is separate from the backing inventory. Slots hidden by stabilizer removal remain serialized but stay inaccessible across reloads; legacy saves without an explicit capacity still expand to retain their highest slot. Portable controllers recalculate capacity after placement. Lowering the stack limit or disabling stack-size overrides must not truncate an existing stack when extracting items. Stabilizer scans and notifications access loaded positions only. The storage flow is covered by targeted in-memory tests; real world break/place events, packets and multiplayer still need runtime validation.

The development client loads JEI 4.16.1.1013 from Modrinth Maven automatically.
It is a dev dependency only and is not bundled into the generated mod jar.

Patchouli `1.0-23.6` (Minecraft 1.12) is resolved from BlameJared Maven for
compilation and the development runtime. Install Patchouli alongside Occultism
to use the new Dictionary of Spirits; it is not bundled into the Occultism jar.
The existing dictionary item opens the Patchouli book when present and retains
the legacy GUI fallback when absent.

The bilingual book lives in
`src/main/resources/assets/occultism/patchouli_books/dictionary_of_spirits`.
Regenerate it with Python 3.9+ using `python tools/generate_patchouli_book.py`;
add `--check` to verify generated files without writing them.
It uses the local dictionary index, translations, and crafting recipes.
Ritual and spirit-fire pages read the runtime registries, and pentacle previews
use the same patterns as the world detector.
Trade pages and trader entities also share the local trade registry. The fixed
stone trades convert at most four inputs per 15 world-second steps, and the
sapling trade converts one input per 20 steps, matching the pinned upstream
job timings. Shapeless trade recipes support up to nine costs through the Java
registry and `SpiritTrade.addShapelessRecipe`. Trader entities retain upstream's
single-held-stack execution: repeated costs for the same stack can execute,
while mixed item types require a caller of the multi-stack recipe API.
Trader recipe IDs and per-entity timing/batch settings persist using upstream
NBT field names. Named single-input trades can be replaced or removed through
`mods.occultism.SpiritTrade`; API usage and limits are in `SPIRIT_TRADE_1.12.md`.

Current development constraints: prioritize code, do not use Computer Use, and
do not run the full test suite. Batch relevant changes before targeted checks:

```bat
.\gradlew-local.bat test --tests com.occultism.occultism.resource.PatchouliBookPortTest
```

Current status and remaining gaps: `进度.md` and `PORTING_PROGRESS_1.12.2.md`.

Weather/time rituals now summon local tiered worker entities that cast once
and disappear, rather than mutating the world from the bowl. Timing, save state,
server configuration and remaining compatibility differences are documented in
`WEATHER_TIME_PORT_1.12.md`.

Sacrifice rituals wait for player-attributed kills near the bowl instead of
automatically killing nearby entities. Death cancellation, pending notifications,
the human tag and remaining integration limits are described in
`RITUAL_SACRIFICE_1.12.md`.

Active rituals now revalidate their pentacle, activation and remaining costs
while waiting and casting. Interrupted activations and accelerated ingredient
consumption are covered in `RITUAL_LIFECYCLE_1.12.md`.
Standard ritual costs now use a shared Forge matching plan for validation and
actual consumption; counted-script fallback limits and the deliberate upstream
bug fix are described in `RITUAL_MATERIAL_MATCHING_1.12.md`.
An empty ritual registry now stays empty after script removals or failed loads.
`Ritual.removeAll()` supports replacing the bundled table with a pack's own
recipes; initialization, explicit reloads and limits are documented in
`RITUAL_REGISTRY_1.12.md`.
Storage-terminal JEI transfer resolves unambiguous recipe IDs on the server,
with a bounded candidate fallback and upstream-shaped 3x3 ingredient layout.
The behavior and current Patchouli storage text are documented in
`STORAGE_RECIPE_TRANSFER_1.12.md`.

Build output is written to `build/libs/`. The normal mod jar is `occultism-<version>.jar`.

Main files:

- Mod metadata: `gradle.properties`
- Main mod class: `src/main/java/com/occultism/occultism/Occultism.java`
- Mod info resource: `src/main/resources/mcmod.info`
- Dependency declarations: `gradle/scripts/dependencies.gradle`

Porting notes:

- Upstream Occultism reference source is stored in `_reference/occultism-upstream`.
- Farmer's Future Delight reference code is stored separately in
  `_reference/farmers-future-delight`. Source revision, omitted audio and
  integration boundaries are recorded in `FARMERS_FUTURE_DELIGHT_REFERENCE.md`.
- Copied/ported upstream assets and references are covered by `THIRD_PARTY_NOTICES.md`.

For IntelliJ IDEA, select a Java 25 JDK as Gradle JVM before refreshing Gradle.
