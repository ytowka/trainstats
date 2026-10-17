# AGENTS.md

Repo-specific guidance for AI agents working on Training-stats, a workout tracker.
Android ships from `:app`; domain, data and presentation state are shared through
Kotlin Multiplatform. The planned iOS UI is native SwiftUI, not Compose.

> Architecture checked against the repository on 11 October 2026. Implementation
> takes precedence over older feature proposals in `plan/`.

## Toolchain

- JDK 17, Kotlin 2.3.21, AGP 8.9.3, Gradle 8.11.1; Android compile/targetSdk 36,
  minSdk 26. Dependency/plugin versions live in `gradle/libs.versions.toml`.
  Do not hardcode library versions in module build files.
- KSP is pinned to 2.3.5: the catalog records an AGP-KMP apply failure on 2.3.6+.
  The JetBrains Navigation 3 lifecycle integration is pinned to 2.10.0 because
  2.11.0 pulls a newer Android toolchain. Treat upgrades as separate work.
- `local.properties` (gitignored) must contain `sdk.dir=...`. Never commit it.
- Keep existing `@Suppress("DSL_SCOPE_VIOLATION")` above plugin blocks
  (KTIJ-19369).

## Modules and dependency boundaries

`settings.gradle.kts` maps `includeSourceModule(":foo:bar")` to
`sources/foo/bar`. Only `:app` and `:benchmark` live at the root.

| Module | Responsibility |
|---|---|
| `:app` | Android application, manifest/resources/assets, Koin bootstrap, `FragmentActivity`, unit/instrumented tests |
| `:shared` | KMP domain, repositories/datasources, use cases, MVI ViewModels/State/Event/SideEffect, Koin modules; Android import/export services and VM |
| `:ui-compose` | Compose screens, `SharedApp`, root navigation, UI resources, Compose VM helpers and date formatting local; Android import/export UI |
| `:common-core` | Compose-free KMP: use case bases, MVI contracts, date/text utilities and IDs |
| `:common-db` | Room database, DAO, converters, platform database builders, KSP and tracked schemas |
| `:common-db-api` | Room entities/views and relation/query models |
| `:common-ds` | Compose theme, reusable controls, icons and drag-and-drop |
| `:common-date-picker` | Compose date picker |
| `:common:bottomsheet` | Compose `BottomSheetHost`, sheet state, animation and modal input handling |
| `:common:alertdialog` | Compose confirmation dialog |
| `:navigation:api` / `:navigation:impl` | Compose navigation contract/destinations and Navigation 3 host/back stack |
| `:benchmark` | Android macrobenchmark targeting `:app`; only the benchmark variant is enabled |

Current dependency graph:

```text
:app -> :shared + :ui-compose
:ui-compose -> :shared (api) + Compose components + :navigation:api/impl
:shared -> :common-core + :common-db + koin-core
:common-db -> :common-db-api (api)
```

- `:app` uses `com.android.application`; never add `iosMain`/`jvmMain` there.
- Keep Compose, navigation and `koin-compose` out of `:shared` and `:common-core`.
  Their `ui/` packages contain presentation state/VM, not Composable screens.
- Compose modules still have iOS targets under the current convention. This does
  not make them part of the planned SwiftUI application.
- There is no JVM KMP target configured today. The Android host tests run on JVM.
- Feature-module folders under `sources/features/` are proposals/scaffolding,
  not registered feature modules. `plan/features/ARCH_REFACTOR_PLAN.md` is a
  separate, unimplemented decomposition plan; do not assume its graph exists.
  Any future decomposition must preserve the Compose-free iOS dependency tree,
  including shared VM/state for statistics and workout details.
- `backend/trainstats` is a separate Spring Boot Gradle build, outside the root
  Android/KMP build. Server sync is a proposal, not an active app capability.
- No tracked Xcode project/Swift sources, SKIE plugin or iOS VM provider exist yet.
  See `plan/KMP/KMP_MIGRATION_PLAN.md` and its `PROGRESS.md` before iOS work.

## Convention plugins (`build-logic/`)

- `multiplatform-library`: Kotlin Multiplatform + Android KMP library;
  Android namespace `com.danilkha.<name>`, iOS device `iosArm64` and Apple Silicon
  simulator `iosSimulatorArm64`, static framework `<name>Kit`. Default name is
  `project.name.replace("-", "")`, overridable via `moduleSetup.name`.
  It defines framework binaries, not an XCFramework assembly task.
- `compose-setup`: adds Compose/compiler to the above, common Compose and
  JetBrains lifecycle dependencies, Android tooling, and `:common-core` in
  `commonMain`. No desktop/JVM target is configured.

Use these conventions for KMP modules instead of raw KMP/Compose plugin aliases.
`build-logic` is an included build and shares the root version catalog through
`the<VersionCatalogsExtension>().named("libs")`. Nested modules with the same
leaf name need distinct namespace/framework names; the proposed path-based
naming and `kotlin-library` convention are not implemented yet.

## DI, presentation and navigation

- **Koin DSL, no Dagger/Hilt or Koin annotations.** Android
  `app/src/main/java/com/danilkha/trainstats/entrypoint/App.kt` initializes Napier
  and `startKoin`, with `platformModule`, `dataModule`, `repositoryModule`,
  `useCaseModule`, `androidSharedModule`, `viewModelModule`.
- Modules are in `sources/shared/src/{commonMain,androidMain,iosMain}/kotlin/`
  under `com/danilkha/trainstats/di`. Register new dependencies there.
  Common VM are `factoryOf`; `WorkoutSaver` is `singleOf`.
- MVI lives in `common-core/viewmodel`: `MviViewModel<State, Event, SideEffect>`
  extends `androidx.lifecycle.ViewModel`, implements `IMviViewModel`, exposes
  `startState`, `state: StateFlow`, `sideEffects: SharedFlow`, and
  `processEvent(event)`. Do not copy the generic interop guide's
  `initialState`/`accept`/`sideEffect` names into repository code.
- `state` currently creates a `stateIn(WhileSubscribed(5_000))` flow in its
  getter and starts loading on collection. Keep a stable flow reference in
  UI/adapters (as statistics/details already do), and consider subscriptions
  and VM ownership when changing screen lifecycle.
- Android resolves VM with `koinViewModel`; Compose effect helpers are in
  `:ui-compose`, not `:common-core`. Navigation 3 owns screen VM stores.
- `RootScreen` has History / Exercises / Profile tabs. Workout editor,
  Statistics, WorkoutDetails, Settings and import/export are pushed routes.
  History cards open `WorkoutNav` (editor); statistics cards open
  `WorkoutDetailsNav` (read-only). Keep these behaviors distinct.
- `SharedApp` installs `BottomSheetHost` above screen insets. Sheets register at
  the caller's lifetime and render at the host with caller composition locals.
  Only the top sheet receives input/back; closing animation completes before
  dismissal. Preserve nested selector → editor, focus/IME handling, dismissal
  and result callbacks. A key-based `BottomSheetNavigator` and independent
  per-sheet VM stores are still proposals, not the current implementation.

## Feature map and invariants

Feature packages are `com.danilkha.trainstats.features.<feature>`. For each
feature, logic/VM live in `sources/shared/src/commonMain/kotlin/`, screens in
`sources/ui-compose/src/commonMain/kotlin/`, preserving package names.

| Feature | Packages/files and behavior |
|---|---|
| Exercises | `exercises/{data,domain,ui}`; list/search/create/edit/delete, with-weight and separate left/right repetitions; selector reuses `ExerciseListViewModel` |
| Workout editor | `workout/ui/editor`; groups/sets, date, reordering, collapse, delete/undo, autosave through `WorkoutSaver`, new-workout selector and elapsed time since last edit |
| History | `workout/ui/history`; list/search and entry into workout editor or statistics |
| Exercise history | `workout/ui/exercisehistory`; per-exercise history shown from editor |
| Workout details | `workout/ui/details`; `WorkoutDetailsViewModel`/`WorkoutDetailsScreen`, read-only complete workout from selected point or record |
| Statistics | `stats/domain`, `stats/ui`; `StatisticsViewModel`, metrics chart and frequency histogram |
| Profile/settings/backup | Profile/settings screens in UI; file reader/writer, import/export use cases and VM in `:shared/androidMain`, screens in `:ui-compose/androidMain`; parser remains common |

- `WorkoutSaver` debounces updates by 5 seconds. Editor `onCleared()` commits
  remaining state (or deletes an empty workout). Do not bypass this lifecycle
  when changing navigation or building iOS ownership/background handling.
  `WorkoutStopwatch` is UI behavior based on `lastEdited`, not a stored duration.
- Statistics use non-archived history; `saved=false` does **not** exclude a
  workout. Set order is global `orderPosition`; metric series numbers are local
  to the selected exercise across all its groups in a workout.
- Metric calculations live in `ExerciseStatistics.kt`: weight, repetitions,
  volume; separated repetitions use `(left + right) / 2`, volume uses
  `weight * (left + right)`. No weight excludes weight/volume points; explicit
  zero weight is valid. Zero repetitions are excluded without renumbering sets.
  Maximum series (`MAXIMUM_SERIES = 0`) is enabled by default; all-time record
  is independent of visible series/time range. Reuse shared tie-breaking logic.
- Exercise variations belong to an exercise; `null` means ordinary. Names are
  unique within an exercise; use cases store `trim + lowercase + capitalize`
  (including Russian), with a unique index on `(exerciseId, name)`.
  At most one named variation has `isDefault = true`; none means ordinary.
  Card selection uses the default; variation chips select explicitly, including null.
  Groups merge only consecutive `(exerciseId, variationId)` pairs. Exercise and
  variation drafts save atomically; deleting a variation uses `ON DELETE SET NULL`.
  Selectors offer only the current exercise’s variations. Workout writes clear
  deleted IDs from pending autosaves; groups retain the selected ID and name
  without subscribing to dictionary changes.
  Both statistics tabs share variation selection; filtering happens before series
  numbering and affects all-time records/totals. All-workouts frequency ignores it.
  Export headers use `$` as delimiter and `$$` for literal dollars. See
  `plan/features/EXERCISE_VARIATION.md`.
- Frequency calculations live in `FrequencyStatistics.kt`: calendar week
  (Monday start), month, quarter, year; exercise/all-workouts scope;
  workout/set counts, empty intervals and all-time totals. Count approaches with
  finite positive total repetitions; use shared timezone-aware bucketing.
- Chart drawing, viewport gestures and histogram scroll position are Compose
  UI concerns. `StatisticsChartRange.kt` contains shared range calculations.
  Behavior and edge cases are documented in `plan/QA/STATS_CASE.md`.
- Previous-set hints, exercise position in history,
  calendar drill-down and workout notes are **not implemented**. Calendar
  button currently has an empty callback. Do not treat `plan/BOARD.md` or
  feature requests as evidence of shipped behavior.

## Database and platform code

- **Room KMP via KSP**, no kapt. Database:
  `sources/common-db/src/commonMain/kotlin/com/danilkha/trainstats/db/TrainStatsDb.kt`.
  Version 4, `exportSchema = true`, `@ConstructedBy(TrainStatsDbConstructor::class)`.
  KSP runs for Android and both iOS targets. Schemas are tracked in
  `sources/common-db/schemas/com.danilkha.trainstats.db.TrainStatsDb/`.
- Entities/views are in `:common-db-api`; DAO/converters/builders in `:common-db`.
  `PendingUpdateEntity` is a sync scaffold and is not in `TrainStatsDb.entities`.
- The variation review explicitly chose development database resets instead of
  migrations: both builders use `fallbackToDestructiveMigration(true)`. Updating
  from older schemas recreates the database; use export/import to transfer data.
  Preserve historical tracked schemas. Reconsider migrations before release.
- `app/src/main/assets/trainstatsDb.db` exists, but the current Android builder
  does not call `createFromAsset`. Do not assume it seeds production data.
- iOS builder uses `BundledSQLiteDriver`; its current bare database name needs
  a verified writable/persistent application path before iOS release.
- iOS `TextUtils.format1/format2` still throw `TODO("ios")`; date formatter
  returns ISO strings. Repositories still contain 12 `Dispatchers.IO` wrappers.
  These are remaining iOS tasks, not completed compatibility work.

## Testing and common commands

- `app/src/test`: Kotest (`BehaviorSpec` etc.), JUnit Platform, mockk;
  existing specs use `IsolationMode.InstancePerLeaf`. Statistics coverage:
  `StatisticsTest`, `FrequencyStatisticsTest`.
- `app/src/androidTest`: JUnit4/AndroidJUnit4, AndroidJUnitRunner,
  mockk-android and Compose UI tests. Relevant suites: `HappyPathTest`,
  `StatisticsScreenTest`, `FrequencyChartPositionTest`, `BottomSheetHostTest`,
  `WorkoutDaoTest`, `RoomWorkoutDatasourceTest`, `ExportImportTest`.
- Room fixtures: `RoomTestUtils.createTestDb()`/`populateDb()` seed an in-memory
  DB from `app/src/androidTest/assets/trainstatsDb.sql` line-by-line. Keep SQL and
  `workout_export*.txt` fixtures consistent with changed import/database behavior.
- `:common:bottomsheet` additionally has Kotest Android host tests under
  `src/androidHostTest`, enabled by `withHostTest` and JUnit Platform.
- Run focused checks for the change; record actual results. Historical green
  checks in plans are not evidence for the current checkout.

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:testDebugUnitTest --tests 'com.danilkha.trainstats.StatisticsTest' --tests 'com.danilkha.trainstats.FrequencyStatisticsTest'
./gradlew :common:bottomsheet:testAndroidHostTest
./gradlew :app:lintDebug
# Requires a device/emulator; filter by instrumentation runner argument, not --tests
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.danilkha.trainstats.StatisticsScreenTest
./gradlew :benchmark:connectedBenchmarkAndroidTest
# iOS checks on a Mac with Xcode; remaining blockers are described above
./gradlew :shared:compileKotlinIosSimulatorArm64
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

There is no repository CI/formatter/commit-hook configuration.
`opencode.jsonc` configures tools/providers, not project architecture guidance.
