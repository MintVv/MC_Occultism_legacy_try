## OCCULTISM

Minecraft 1.12.2 mod development workspace based on CleanroomMC ForgeDevEnv.

Environment:

- Minecraft 1.12.2
- Forge 14.23.5.2847
- Gradle 9.2.1
- RetroFuturaGradle 2.0.2
- Project-local JDK 25 in `.jdks/jdk-25.0.3`

Use the local wrapper script so Gradle always runs with the project JDK:

```bat
.\gradlew-local.bat build
.\gradlew-local.bat runClient
.\gradlew-local.bat runServer
```

The development client loads JEI 4.16.1.1013 from Modrinth Maven automatically.
It is a dev dependency only and is not bundled into the generated mod jar.

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
