# AGENTS.md

Repo-specific guidance for AI agents working in this codebase.
"Training-stats" is an Android workout tracker. The `:app` module ships on
Android; the `common-*` modules are Kotlin Multiplatform (Android + iOS + JVM).

## Toolchain

- JDK 17, Kotlin 2.2.21, AGP 8.9.3, Gradle 8.11.1
- compileSdk 36, minSdk 26, targetSdk 36
- All versions centralized in `gradle/libs.versions.toml` — do not hardcode
  versions in module `build.gradle.kts` files.
- `local.properties` (gitignored) must contain `sdk.dir=...` for the build to
  configure. Don't commit it.
- `@Suppress("DSL_SCOPE_VIOLATION")` above a `plugins {}` block is required
  (KTIJ-19369). Keep it when editing those files.

## Module layout

- `:app` — single-platform Android app (uses `com.android.application`, not
  KMP). Do **not** add `iosMain`/`jvmMain` sources here. Namespace
  `com.danilkha.trainstats`. Entry points: `entrypoint/App.kt` (builds the
  Dagger `AppComponent`), `entrypoint/MainActivity.kt` (a `FragmentActivity`).
- `:common-core`, `:common-ds`, `:common-date-picker` — KMP libraries. They
  must apply a convention plugin from `build-logic` (see below), **not** the
  raw KMP/Compose plugin aliases from the version catalog.
- `:benchmark` — `com.android.test` macrobenchmark module targeting `:app`.
  Only the `benchmark` build type is enabled; other variants are disabled in
  `androidComponents.beforeVariants`.
- Dependency graph: `app` → `common-core`, `common-ds`, `common-date-picker`;
  `common-date-picker` → `common-core` + `common-ds`. The `compose-setup`
  convention plugin also pulls `common-core` into `commonMain` automatically.

## Convention plugins (`build-logic/`)

Two precompiled script plugins live in `build-logic/src/main/kotlin/`:

- **`multiplatform-library`** — applies `kotlin("multiplatform")` +
  `com.android.kotlin.multiplatform.library`, configures Android namespace as
  `com.danilkha.<moduleName-without-dashes>`, and wires iOS simulator/device
  framework binaries (`.xcframework` baseName `<name>Kit`). Use this for any
  new non-UI KMP module.
- **`compose-setup`** — applies `multiplatform-library` + JetBrains Compose
  Multiplatform + compose compiler, and wires common Compose deps into
  `commonMain`, tooling into `androidMain`, and desktop into `jvmMain`. Use
  this for any new Compose UI module.

`build-logic` is an included build (`includeBuild` in `settings.gradle.kts`)
and reuses the root `libs` version catalog via
`../gradle/libs.versions.toml`. Edit catalog accessors through
`the<VersionCatalogsExtension>().named("libs")` (see `compose-setup.gradle.kts`).

## App internals

- **DI is plain Dagger (not Hilt).** `dagger-compiler` is processed by **KSP**,
  not kapt. The single component is `di/AppComponent` with modules
  `RepositoryModule`, `DatasourceModule`, `DbModule`, `AndroidModule`.
  ViewModels are exposed via `di/ViewModelsProvider` and resolved at runtime
  through a `LocalViewModelsProvider` `CompositionLocal` in `MainActivity`.
- **Room via KSP** (not KAPT). `exportSchema = true`; schemas are exported to
  `shared/schemas/` and tracked in git. The DB is
  `entrypoint/db/TrainStatsDb.kt` (version 1). When entities/views change you
  must either bump `version` and provide a migration, or reset schemas
  deliberately — do not silently change entities without handling the version.
- A prebuilt DB is shipped at `app/src/main/assets/trainstatsDb.db`.
- Features follow `features/<feature>/{data,domain,ui}` layering. Keep new
  features consistent with this.
- Logging uses Napier (initialized in `App.onCreate`).

## Testing

- **Unit tests (`app/src/test`) use Kotest, not JUnit.** `app/build.gradle.kts`
  configures `testOptions.unitTests.all { it.useJUnitPlatform() }` globally —
  write specs as `BehaviorSpec` / related Kotest styles (see `SampleTest.kt`).
  Default `IsolationMode.InstancePerLeaf`. Mocking via **mockk**.
- **Instrumented tests (`app/src/androidTest`) use JUnit4** with
  `AndroidJUnitRunner` and `AndroidJUnit4` runner — different framework from
  unit tests. Mocking via `mockk-android`.
- Room instrumented tests use `features/workout/data/db/RoomTestUtils.kt`:
  `createTestDb()` builds an in-memory DB and `populateDb()` seeds it from
  `app/src/androidTest/assets/trainstatsDb.sql` (execSQL line-by-line). New
  seed data must be added to that SQL file.
- Additional fixtures live in `app/src/androidTest/assets/`
  (`workout_export*.txt` etc.) and are referenced by `ExportImportTest`.

## Common commands

```bash
# Build / install the app
./gradlew :app:assembleDebug
./gradlew :app:installDebug

# Unit tests (Kotest via JUnit Platform)
./gradlew test
./gradlew :app:test --tests "com.danilkha.trainstats.SampleTest"

# Instrumented tests (needs a device/emulator)
./gradlew connectedAndroidTest
./gradlew :app:connectedAndroidTest --tests "*.WorkoutDaoTest"

# Static checks
./gradlew lint

# Macrobenchmark (needs device/emulator; only `benchmark` variant is built)
./gradlew :benchmark:connectedBenchmarkAndroidTest
```

There is no CI workflow, formatter, or commit-hook configuration in the repo.
`opencode.jsonc` only configures MCP and a local provider — it carries no
project instructions.
