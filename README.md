## OCCULTISM

Minecraft 1.12.2 mod development workspace based on CleanroomMC ForgeDevEnv.

Environment:

- Minecraft 1.12.2
- Forge 14.23.5.2847
- Gradle 9.2.1
- RetroFuturaGradle 2.0.2
- `gradlew-local.bat` selects the project-local JDK 25 when present; `gradlew.bat` remains portable.

Use the local wrapper script so Gradle always runs with the project JDK:

```bat
.\gradlew-local.bat build
.\gradlew-local.bat runClient
.\gradlew-local.bat runServer
```

`gradle.properties` intentionally does not set `org.gradle.java.home`; the
absolute path would make a fresh clone and CI host-specific.

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
- Copied/ported upstream assets and references are covered by `THIRD_PARTY_NOTICES.md`.

For IntelliJ IDEA, set Gradle JVM to `.jdks/jdk-25.0.3` before refreshing Gradle.
