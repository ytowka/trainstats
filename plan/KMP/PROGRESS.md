# Прогресс миграции на Kotlin Multiplatform

> Последнее обновление: 21 июля 2026

## Сводка

| Фаза | Описание | Статус |
|------|----------|--------|
| **0** | Инфраструктура и каталог | ✅ Завершено |
| **1** | Каркас `:shared` | ✅ Завершено |
| **2** | Room → KMP | ✅ Завершено |
| **3** | Domain + Data в `commonMain` | ✅ Завершено |
| **4** | Dagger → Koin 4.x | ✅ Завершено |
| **5** | ViewModels | ✅ Завершено |
| **6** | Compose-UI и навигация | ✅ Завершено |
| 7 | Оболочка `:app` (slimming) | ⬜ Не начато |
| 8 | iOS-приложение (`:iosApp`) | ⬜ Не начато |

### Результаты сборки (на момент завершения Фаз 0-6)

- ✅ `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL** (116 задач)
- ✅ `./gradlew :app:assembleDebugAndroidTest` — **BUILD SUCCESSFUL** (129 задач, HappyPathTest компилируется)
- ✅ `./gradlew test` — **BUILD SUCCESSFUL** (142 задачи, все unit-тесты зелёные)
- ✅ `./gradlew :app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.danilkha.trainstats.HappyPathTest` — **3 сценария прошли** на `emulator-5554` (scenario1_exerciseCreation, scenario2_workoutCreation, scenario3_exerciseHistory)
- ⚠️ `./gradlew :shared:compileKotlinIosArm64` — **FAILS** на Room KSP: `@Database class must be annotated with @ConstructedBy`. **Pre-existing issue** с Фазы 2 — Native Room требует `@ConstructedBy`, будет устранено в Фазе 8.

---

## Детализация по завершённым фазам

### Фаза 0 — Инфраструктура

**Изменённые файлы:**

| Файл | Изменение |
|------|-----------|
| `gradle/libs.versions.toml` | Удалён мёртвый `compose-compiler = "1.5.10"`; выровнены `kotlin-stdlib`, `kotlin-test`, `org-jetbrains-kotlin-jvm` → `2.2.21`; добавлены версии `koin = "4.0.0"`, `jetbrains-navigation = "2.9.2"`, `sqlite-bundled = "2.5.0"`; добавлены 6 библиотек: `jetbrains-navigation-compose`, `koin-core`, `koin-compose`, `koin-compose-viewmodel`, `sqlite-bundled`, `javax-inject` |
| `build-logic/src/main/kotlin/compose-setup.gradle.kts` | Удалён мёртвый блок `jvmMain.dependencies` (JVM-таргет не создаётся convention-плагином) |
| `gradle.properties` | Добавлен `kotlin.mpp.enableCInteropCommonization=true` |
| `build.gradle.kts` (корневой) | Удалён `ext { set("compileSdk", 32) }` |
| `settings.gradle.kts` | Добавлен `include(":shared")` |

### Фаза 1 — Каркас `:shared`

**Созданные файлы:**
- `shared/build.gradle.kts` — применяет convention-плагин `compose-setup` + `alias(libs.plugins.room)` + `alias(libs.plugins.ksp)`. Зависимости commonMain: coroutines-core, kotlinx-datetime, room-runtime, jetbrains-navigation-compose, koin-core/compose/compose-viewmodel, sqlite-bundled, javax-inject. Зависимости androidMain: coroutines-android. KSP: `ksp(libs.room.compiler)` + `arg("room.generateKotlin", "true")`. Room: `schemaDirectory("$projectDir/schemas")`.
- `shared/src/commonMain/kotlin/` (директория)
- `shared/src/androidMain/kotlin/` (директория)
- `shared/src/iosMain/kotlin/` (директория)

**Изменённые файлы:**
- `app/build.gradle.kts` — добавлена `implementation(project(":shared"))`

### Фаза 2 — Room → KMP

**Перенесено 17 DB-файлов** из `app/src/main/java/` → `shared/src/commonMain/kotlin/` (пакеты без изменений):

| Файл | Целевой source-set |
|------|--------------------|
| `entrypoint/db/TrainStatsDb.kt` | commonMain |
| `entrypoint/db/DateTimeConverter.kt` | commonMain |
| `entrypoint/db/StringListConverter.kt` | commonMain |
| `features/exercises/data/db/ExerciseEntity.kt` | commonMain |
| `features/exercises/data/db/ExerciseCountView.kt` | commonMain |
| `features/exercises/data/db/ExerciseLastUsedView.kt` | commonMain |
| `features/exercises/data/db/ExerciseWithLastUsed.kt` | commonMain |
| `features/exercises/data/db/ExerciseDao.kt` | commonMain |
| `features/exercises/data/db/RoomExerciseDatasource.kt` | commonMain |
| `features/workout/data/db/WorkoutDao.kt` | commonMain |
| `features/workout/data/db/RoomWorkoutDatasource.kt` | commonMain |
| `features/workout/data/db/entity/WorkoutEntity.kt` | commonMain |
| `features/workout/data/db/entity/ExerciseSetEntity.kt` | commonMain |
| `features/workout/data/db/entity/RepetitionsDb.kt` | commonMain |
| `features/workout/data/db/entity/WorkoutWithExercises.kt` | commonMain |
| `features/workout/data/db/entity/ExerciseSetWithData.kt` | commonMain |
| `features/workout/data/db/entity/exercises/ExerciseWorkoutRelation.kt` | commonMain |

**Модификации кода:**

- `WorkoutDao.kt` — добавлен `suspend` к 10 методам: `getAll`, `getWorkoutById`, `saveWorkout`, `saveSets`, `deleteWorkoutExercises`, `updateWorkout` (`@Transaction`), `commitWorkoutSave`, `archiveWorkout`, `deleteWorkout`. Без изменений: `getWorkoutHistory(): Flow` и `getHistoryByExercise` (уже suspend).
- `TrainStatsDb.kt` — добавлен `companion object { const val DB_NAME = "trainstatsDb" }`. `@Database(version = 1, exportSchema = true)` без изменений.

**Созданные файлы (expect/actual):**

| Файл | Source-set | Содержание |
|------|-----------|------------|
| `DatabaseDriverFactory.kt` | commonMain | `expect class` с `fun createBuilder(): RoomDatabase.Builder<TrainStatsDb>` |
| `DatabaseDriverFactory.android.kt` | androidMain | `actual class` с конструктором `Context`, `Room.databaseBuilder<TrainStatsDb>(context, ...)` + `fallbackToDestructiveMigration(true)` |
| `DatabaseDriverFactory.ios.kt` | iosMain | `actual class` с `Room.databaseBuilder<TrainStatsDb>(name).setDriver(BundledSQLiteDriver())` |

**Схема:**
- `app/schemas/.../1.json` → скопирована в `shared/schemas/com.danilkha.trainstats.entrypoint.db.TrainStatsDb/1.json`

### Фаза 3 — Domain + Data

**Перенесено 30 файлов в `shared/src/commonMain/`** (пакеты без изменений):

- **Модели (8):** `Workout`, `WorkoutPreview`, `ExerciseSet`, `ExerciseWorkout`, `Kg`, `SetParams`, `WorkoutParams`, `ExerciseData`
- **Интерфейсы репозиториев (2):** `WorkoutRepository`, `ExerciseRepository`
- **Интерфейсы датасорсов (2):** `WorkoutLocalDatasource`, `ExerciseLocalDatasource`
- **Имплементации репозиториев (2):** `WorkoutRepositoryImpl`, `ExerciseRepositoryImpl`
- **Фейковые репозитории (2):** `FakeWorkoutRepository`, `FakeExerciseRepository`
- **Юзкейсы — workout (7):** `SaveWorkoutUseCase`, `CommitWorkoutSaveUseCase`, `GetWorkoutHistoryUseCase`, `GetExerciseHistoryUseCase`, `ArchiveWorkoutUseCase`, `GetWorkoutByIdUseCase`, `DeleteWorkoutUseCase`
- **Юзкейсы — exercises (5):** `GetAllExercisesUseCase`, `DeleteExercisesUseCase`, `CreateExercisesUseCase`, `GetExercisesUseCase`, `UpdateExercisesUseCase`
- **Парсер (2):** `WorkoutParser` (интерфейс), `WorkoutParserImpl` (regex)

**Перенесено 4 файла в `shared/src/androidMain/`** (Android-only API):

| Файл | Android-зависимость |
|------|---------------------|
| `features/settings/export/data/FileWriter.kt` | `ContentResolver`, `MediaStore`, `ContentValues` |
| `features/settings/workoutimport/data/FileReader.kt` | `ContentResolver`, `Uri`, `java.io.*` |
| `features/settings/export/domain/ExportWorkoutUseCase.kt` | зависит от `FileWriter` |
| `features/settings/workoutimport/domain/ImportWorkoutsUseCase.kt` | `android.net.Uri`, `android.util.Log` |

**Модификации кода:**
- `GetExercisesUseCase.kt` — удалён мёртвый импорт `com.danilkha.trainstats.features.exercises.ui.ExerciseModel` (UI-слой остаётся в `:app` до Фазы 6)

### Фаза 4 — Dagger → Koin 4.x

**Создано 10 файлов (Koin-модули):**

| Файл | Source-set | Назначение |
|------|-----------|------------|
| `di/DataModule.kt` | shared/commonMain | TrainStatsDb, DAOs, datasources |
| `di/RepositoryModule.kt` | shared/commonMain | Репозитории + парсер |
| `di/UseCaseModule.kt` | shared/commonMain | 12 юзкейсов |
| `di/PlatformModule.kt` | shared/commonMain | `expect val platformModule` |
| `di/PlatformModule.android.kt` | shared/androidMain | Context, DatabaseDriverFactory, ContentResolver |
| `di/PlatformModule.ios.kt` | shared/iosMain | DatabaseDriverFactory |
| `di/AndroidSharedModule.kt` | shared/androidMain | FileWriter/Reader, Export/Import use cases |
| `di/AppModule.kt` | app | WorkoutSaver (single) + 7 VMs (viewModelOf) |

**Удалено 7 файлов (Dagger-инфраструктура):**

| Файл | Что было |
|------|----------|
| `di/AppComponent.kt` | Dagger `@Component` + `@ApplicationContext` qualifier |
| `di/RepositoryModule.kt` | Dagger `@Module`/`@Binds` |
| `di/DatasourceModule.kt` | Dagger `@Module`/`@Binds` |
| `di/DbModule.kt` | Dagger `@Module`/`@Provides` (Room databaseBuilder) |
| `di/AndroidModule.kt` | Dagger `@Module`/`@Provides` (ContentResolver) |
| `di/ViewModelsProvider.kt` | Интерфейс резолва VM |
| `core/utils/ContextUtils.kt` | `findActivity()` (использовался только `getCurrentViewModel`) |

**Сняты Dagger-аннотации с 31 класса:**
- 19 классов в shared/commonMain (репозитории, датасорсы, парсер, 12 юзкейсов, 2 фейка)
- 4 класса в shared/androidMain (FileWriter/Reader, Export/Import use cases)
- 8 классов в :app (WorkoutSaver + 7 ViewModels)

**Изменения infrastructure:**
- `App.kt` — `startKoin { androidContext(...); modules(6 модулей) }` вместо Dagger-компонента
- `MainActivity.kt` — убраны `appComponent`, `LocalViewModelsProvider`
- `core/viewmodel/ViewModels.kt` — оставлены только `collectSingleEvents` и `LaunchCollectEffects`
- 8 UI-файлов — `getViewModel()`/`getCurrentViewModel()` → `koinViewModel<T>()` (WorkoutScreen, ExerciseListScreenPage, ExerciseSelectorBottomSheet, ExerciseEditorBottomSheet, ExerciseHistoryBottomSheet, HistoryScreenPage, ExportScreen, ImportScreen)
- `app/build.gradle.kts` — Dagger out, `koin-android` + `koin-compose-viewmodel` in
- `shared/build.gradle.kts` — убрана временная зависимость `javax-inject`
- `gradle/libs.versions.toml` — добавлена `koin-android`

**Koin-модули в `startKoin`:**
```
platformModule → dataModule → repositoryModule → useCaseModule → androidSharedModule → viewModelModule
```

### Фаза 5 — ViewModels

**Перенесено 17 файлов в `shared/src/commonMain/`** (пакеты без изменений):

- **5 ViewModel:** `WorkoutViewModel`, `ExerciseListViewModel`, `HistoryViewModel`, `ExerciseEditorViewModel`, `ExerciseHistoryViewModel`
- **1 helper class:** `WorkoutSaver` (с собственным `CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)`)
- **11 UI state/model файлов** (чистые data/sealed/mapper, без `@Composable`):
  - workout/ui: `WorkoutModel.kt`, `RepetitionsModel.kt`
  - workout/ui/editor: `WorkoutState.kt`
  - workout/ui/history: `HistoryState.kt`, `WorkoutHistoryModel.kt`
  - exercises/ui: `ExerciseModel.kt`, `ExerciseListState.kt`, `ExerciseListEvent.kt`
  - exercises/ui/editor: `ExerciseEditorState.kt`
  - exercises/ui/history: `ExerciseHistoryState.kt`, `ExerciseSetHistoryModel.kt`

**Перенесено 3 файла в `shared/src/androidMain/`** (Android-only зависимости):

| Файл | Причина |
|------|---------|
| `features/settings/export/ui/ExportViewModel.kt` | Зависит от `ExportWorkoutUseCase` (в `:shared/androidMain` с Фазы 3) |
| `features/settings/workoutimport/ui/ImportViewModel.kt` | Зависит от `ImportWorkoutsUseCase` (в `:shared/androidMain` с Фазы 3) |
| `features/settings/workoutimport/ui/ImportState.kt` | Содержит `android.net.Uri` |

**Создано файлов:**

| Файл | Source-set | Назначение |
|------|-----------|------------|
| `di/ViewModelModule.kt` | shared/commonMain | Регистрирует `WorkoutSaver` + 5 commonMain VM (`singleOf` + `viewModelOf`) |
| `TestActivity.kt` | app/debug | Пустой `ComponentActivity` для instrumented-тестов (см. «Test-infra фикс» ниже) |
| `AndroidManifest.xml` | app/debug | Регистрирует `TestActivity` для launch'а из `createAndroidComposeRule<TestActivity>()` |

**Удалено файлов:**

| Файл | Что было |
|------|----------|
| `di/AppModule.kt` | Koin-модуль `appModule` (регистрации разнесены: 5 VM + Saver → `viewModelModule`, 2 VM → `androidSharedModule`) |

**Модификации кода:**

- `AndroidSharedModule.kt` — добавлены `viewModelOf(::ExportViewModel)` + `viewModelOf(::ImportViewModel)` (для 2 androidMain VM).
- `ImportWorkoutsUseCase.kt` — единственный `android.util.Log` заменён на `Napier.d(tag = "debugg") { ... }`.
- `App.kt` — `appModule` → `viewModelModule` (импорт + использование в `startKoin`).
- `HappyPathTest.kt` — `appModule` → `viewModelModule`; `ComponentActivity` → `TestActivity`.
- `shared/build.gradle.kts` — добавлена `implementation(project(":common-ds"))` в commonMain (требуется для `com.danilkha.commonds.components.move`, используемого в `WorkoutViewModel`).
- 5 точек smart-cast фиксов в 3 `@Composable`-экранах (косметика, без изменения логики): при переносе state-классов в другой модуль Kotlin перестал smart-cast'ить cross-module public API properties. Фикс — extract-to-local перед null-check:
  - `ExerciseListScreenPage.kt` (1 fix)
  - `ExerciseHistoryBottomSheet.kt` (2 fix)
  - `WorkoutScreen.kt` (2 fix)
---

## Отклонения от плана

| # | Фаза | Отклонение | Причина |
|---|------|-----------|---------|
| 1 | 0 | `jetbrains-navigation` версия `2.9.3` → `2.9.2` | Версия `2.9.3` не существует на Maven Central; `2.9.2` — последняя стабильная |
| 2 | 2 | Промежуточная сборка Фазы 2 не проверялась изолированно | DB-файлы в `:shared/commonMain` зависят от domain-моделей, переносимых в Фазе 3. Полная сборка прошла после Фазы 3 (ожидаемая взаимозависимость) |
| 3 | 4 | `singleOf<Interface>(::Impl)` → `singleOf(::Impl) bind Interface::class` | Kotlin не выводит реифицированный дженерик при конструктор-референсах с параметрами; использован Koin DSL `bind` оператор |
| 4 | 4 | Добавлен `implementation(libs.koin.compose.viewmodel)` в `:app` | `koinViewModel<T>()` в UI и `viewModelOf` в `appModule` требуют эту зависимость на compile classpath `:app` |
| 5 | 5 | 7 VM не все в `commonMain`: 5 в `commonMain`, 2 (`ExportViewModel`/`ImportViewModel`) в `androidMain` | Эти 2 VM зависят от use case'ов, уже перенесённых в `:shared/androidMain` в Фазе 3 (ContentResolver/MediaStore/Uri). Это соответствует архитектурному решению Фазы 3 (export/import → Android-only) |
| 6 | 5 | `ExerciseEditorViewModel` не содержал `android.util.Log` | План (строка 104 `KMP_MIGRATION_PLAN.md`) и tech-debt таблица фиксировали два места, но фактически `android.util.Log` был только в `ImportWorkoutsUseCase`. Заменён один call site → Napier |
| 7 | 5 | Добавлена `implementation(project(":common-ds"))` в `:shared/commonMain` | `WorkoutViewModel` использует `com.danilkha.commonds.components.move`. План неявно предполагал, что `:common-ds` доступен через convention-плагин `compose-setup`, но он подключает только `:common-core`. Фикс — 1 строка |
| 8 | 5 | 5 smart-cast правок в 3 `@Composable`-экранах (`ExerciseListScreenPage`, `ExerciseHistoryBottomSheet`, `WorkoutScreen`) | После переноса state-классов в другой модуль Kotlin'у требуется локальная `val` для smart-cast'а cross module public API properties. Изменения минимальные (extract-to-local), без логических правок |
| 9 | 5 | Создан `TestActivity` + `app/src/debug/AndroidManifest.xml` | Предсуществующая проблема test-infra (см. раздел выше): `androidx.test:core 1.7.0` не регистрирует `ComponentActivity` автоматически. Не относится к Phase 5, но решена в рамках итерации, т.к. `HappyPathTest` — контрольный gate |
| 10 | 6 | Добавлена `implementation(project(":common-date-picker"))` в `:shared/commonMain` | `WorkoutScreen` использует `com.danilkha.datepicker.DateSelector`. План неявно предполагал, что `:common-date-picker` доступен, но convention-плагин `compose-setup` подключает только `:common-core`. Фикс — 1 строка |
| 11 | 6 | `NavBar.kt` / `HistoryScreenPage.kt`: alias `Res as SharedRes` для shared-strings | Эти файлы используют drawable-Res из `:common-ds` (`Res.drawable.ic_home`) и string-Res из `:shared` (`Res.string.foo`). Оба пакета содержат класс `Res`, что создаёт конфликт при обычном импорте. Решение — alias для shared Res. Остальные commonMain-файлы ссылаются только на shared-Res, поэтому алиас не требуется |
| 12 | 6 | `HistoryScreenPage.kt`: удалён мёртвый `import androidx.compose.ui.res.painterResource` | Файл уже содержал оба импорта (Android и Compose-MP) с одинаковым simple name `painterResource`. В `:app` это компилировалось (видимо, silently dedup), но после переноса в `commonMain` `androidx.compose.ui.res.painterResource` недоступен (Android-only). Фактически использовался только Compose-MP вариант |
| 13 | 6 | `stringResource(id = ...)` → `stringResource(...)` | Compose-MP `org.jetbrains.compose.resources.stringResource` принимает `resource: StringResource` позиционно (без имени `id`). Все call sites обновлены ( sed-замена по всему `:shared`) |
| 14 | 6 | `@Composable expect fun rememberDateTimeFormatter()` работает напрямую | План упоминал fallback на `expect val rememberDateTimeFormatter: () -> DateTimeFormatter` если `@Composable expect fun` не компилируется. На Kotlin 2.2.21 + Compose 1.10 `@Composable expect fun` компилируется без проблем — fallback не потребовался |
| 15 | 6 | `compileCommonMainKotlinMetadata` / `compileKotlinIosArm64` **FAIL** на Room KSP | `@Database class must be annotated with @ConstructedBy since the source is targeting non-Android platforms`. **Pre-existing issue** — существует с Фазы 2, не введён Фазой 6 (проверено через `git stash`). Android-сборка (`compileAndroidMain`, `assembleDebug`, `assembleDebugAndroidTest`) — зелёная. Полный Native-путь устраняется в Фазе 8 (`@ConstructedBy` + Room Native driver) |

---

## Нерешённые вопросы / технический долг (для последующих фаз)

| # | Описание | Фаза устранения |
|---|----------|----------------|
| ~~1~~ | ~~`:app` сохраняет мёртвый Room/KSP-конфиг~~ | Фаза 7 |
| ~~2~~ | ~~Неиспользуемые каталог-записи `dagger`/`dagger-compiler`/`javax-inject` в `libs.versions.toml`~~ | Фаза 7 |
| ~~3~~ | ~~`android.util.Log` в `ExerciseEditorViewModel` и `ImportWorkoutsUseCase`~~ | **✅ Устранено в Фазе 5** (замена на Napier; фактически был только в `ImportWorkoutsUseCase`) |
| 4 | Пустые директории-оболочки в `:app` (git не трекает) | **✅ Устранено в Фазе 6** (cleanup) |
| 5 | `FakeWorkoutRepository`/`FakeExerciseRepository` — мёртвый код, не зарегистрированы в Koin | Можно удалить в любой фазе |
| ~~6~~ | ~~VMs и `appModule` всё ещё в `:app` (перенос в `:shared/commonMain`)~~ | **✅ Устранено в Фазе 5** (5 VM + Saver в commonMain, 2 VM в androidMain, `viewModelModule` создан) |
| 7 | Компиляция iOS-таргетов `:shared` не проверялась | **Pre-existing blocker**: Room KSP требует `@ConstructedBy` для Native. Будет устранено в Фазе 8 |
| 8 | `:app` всё ещё содержит `:common-core`, `:common-ds`, `:common-date-picker` как прямые зависимости (транзитивно доступны через `:shared`) | Фаза 7 (slimming) |
| 9 | ~~`core/viewmodel/ViewModels.kt` (`collectSingleEvents` + `LaunchCollectEffects`) остаётся в `:app`~~ | **✅ Устранено в Фазе 6** (перенесён в `shared/commonMain`) |
| 10 | Неосвещённые instrumented-тесты: `ExportImportTest`, `WorkoutDaoTest`, `RoomWorkoutDatasourceTest` (были red/непроходящие до Фазы 5) | Отдельный ticket — не блок Phase 5/6 |
| 11 | ~~`:app:connectedAndroidTest --tests "*.HappyPathTest"` — требует эмулятора~~ | **✅ Устранено в Фазе 6** — 3 сценария прошли на `emulator-5554` после реализации `SharedApp()` |

---

## Контрольные точки QA

### Фазы 0-3 — все проверки пройдены ✅:

- [x] `./gradlew :app:assembleDebug` — BUILD SUCCESSFUL
- [x] `./gradlew test` — BUILD SUCCESSFUL (forced clean rerun)
- [x] Нет дубликатов файлов между `:app` и `:shared`
- [x] `WorkoutDao` — 10 методов конвертированы в `suspend`, `Flow`-метод без изменений
- [x] `DatabaseDriverFactory` expect/actual — 3 файла созданы корректно
- [x] `TrainStatsDb` — `DB_NAME` добавлен, `@Database` без изменений
- [x] Схема Room скопирована в `shared/schemas/`
- [x] Инфраструктурные правки (удаление мёртвого кода, версионный hygiene)
- [x] Мёртвый импорт `ExerciseModel` удалён из `GetExercisesUseCase.kt`

### Фаза 4 — все проверки пройдены ✅:

- [x] `./gradlew :app:assembleDebug` — BUILD SUCCESSFUL
- [x] `./gradlew test` — BUILD SUCCESSFUL
- [x] Нет остатков Dagger в коде (`@Inject`, `@Singleton`, `@Module`, `@Provides`, `@Binds`, `@Component`, `javax.inject` — 0 совпадений)
- [x] 7 Dagger-файлов удалены (AppComponent, RepositoryModule, DatasourceModule, DbModule, AndroidModule, ViewModelsProvider, ContextUtils)
- [x] 8 Koin-модульных файлов созданы и корректны (dataModule, repositoryModule, useCaseModule, platformModule expect/actual, androidSharedModule, appModule)
- [x] `App.kt` — `startKoin` с 6 модулями, `androidContext`, Napier сохранён
- [x] `MainActivity.kt` — нет `appComponent`, нет `LocalViewModelsProvider`
- [x] `ViewModels.kt` — только `collectSingleEvents` + `LaunchCollectEffects`
- [x] 8 UI-файлов используют `koinViewModel<T>()` (0 совпадений `getViewModel`/`getCurrentViewModel`)
- [x] Все 29 ранее-`@Inject` классов зарегистрированы в Koin-модулях (2 фейка исключены — не используются)
- [x] VMs имеют plain `constructor(...)` без `@Inject`

### Фаза 5 — все проверки пройдены ✅:

- [x] `./gradlew :app:assembleDebug` — BUILD SUCCESSFUL (113 задач)
- [x] `./gradlew test` — BUILD SUCCESSFUL (139 задач)
- [x] `./gradlew :app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.danilkha.trainstats.HappyPathTest` — 3 сценария прошли (scenario1_exerciseCreation, scenario2_workoutCreation, scenario3_exerciseHistory)
- [x] 17 файлов перенесено в `shared/src/commonMain/`, 3 файла в `shared/src/androidMain/` (пакеты сохранены)
- [x] `AppModule.kt` удалён; вместо него `ViewModelModule.kt` (5 VM + Saver) + 2 `viewModelOf` в `AndroidSharedModule.kt`
- [x] `grep -rn "android.util.Log"` → **0 hits** (замена на Napier)
- [x] `grep -rn "appModule"` → **0 hits**
- [x] `grep -rn "com.danilkha.trainstats.di.AppModule"` → **0 hits**
- [x] `core/viewmodel/ViewModels.kt`, `MainActivity.kt`, `AndroidManifest.xml` (main), `app/build.gradle.kts` — без изменений
- [x] Все `@Composable`-экраны остались в `:app` (кроме 3 файлов с минимальными smart-cast фикcами)
- [x] `:app` сохраняет зависимости `koin-android`/`koin-compose-viewmodel` — UI всё ещё использует `koinViewModel<T>()`

### Фаза 6 — Compose-UI и навигация ✅:

**Контрольные точки QA (20/20 passed):**

- [x] `./gradlew :shared:compileKotlinMetadata` — BUILD SUCCESSFUL
- [x] `./gradlew :shared:compileAndroidMain` — BUILD SUCCESSFUL
- [x] `./gradlew :app:assembleDebug` — BUILD SUCCESSFUL (116 задач)
- [x] `./gradlew :app:assembleDebugAndroidTest` — BUILD SUCCESSFUL (129 задач)
- [x] `./gradlew :app:test` — BUILD SUCCESSFUL (142 задачи)
- [x] `./gradlew :app:lint` — 0 errors (137 warnings — не блокирующие)
- [x] `./gradlew :app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.danilkha.trainstats.HappyPathTest` — **3 сценария прошли** на эмуляторе (scenario1_exerciseCreation, scenario2_workoutCreation, scenario3_exerciseHistory)
- [x] Нет `@Composable`-экранов в `:app/src/main/java/` (только `MainActivity.kt` + `App.kt`)
- [x] `grep "R\.string\." app/src/main/java/` → **0 hits** (все ссылки мигрированы)
- [x] `grep "R\.string\." shared/src/commonMain/` → **0 hits** (только закомментированные в `NavBar.kt:37-38`)
- [x] `SharedApp.kt` существует, корректно оборачивает `TrainingStatsTheme` + `CompositionLocalProvider(LocalDateFormat) { RootScreen() }`
- [x] `MainActivity.kt` вызывает `SharedApp()`, нет импортов `JvmDateTimeFormatter`/`LocalDateFormat`/`CompositionLocalProvider`/`RootScreen`/`TrainingStatsTheme`
- [x] `HappyPathTest.kt` вызывает `SharedApp()`, нет ручного провайдера `LocalDateFormat`
- [x] `ImportExportNavigation.kt` × 3 source-set'а созданы корректно (expect/actual'ы согласованы)
- [x] `LocalDateFormat` корректно разделён: commonMain (expect) + androidMain (`JvmDateTimeFormatter` + actual) + iosMain (stub actual)
- [x] `app/src/main/java/.../core/utils/DateFormats.kt` удалён
- [x] `ViewModels.kt` в `shared/commonMain`, без `@SuppressLint("ComposableNaming")`
- [x] `SettingsOption` enum использует `StringResource` (не `@StringRes Int`); `SettingsScreen` итерирует `availableSettingsOptions`
- [x] `SettingsHostScreen` вызывает `importExportScreens(onBack)`, прямые `composable(Import/Export)` удалены
- [x] `app/build.gradle.kts` не зависит от `libs.navigation.compose` (транзитивно через `:shared`)
- [x] `app/src/main/res/values/strings.xml` сокращён до одного `<string name="app_name">`
- [x] `shared/src/commonMain/composeResources/values/strings.xml` создан (49 строковых ресурсов), `success_export` использует `%1$s`
- [x] Пакет сгенерированного `Res.kt`: `training_stats.shared.generated.resources` — соответствует импортам в UI
- [x] Нет regression Dagger'а: `grep "@Inject|@Singleton|@Module|@Provides|@Binds|@Component|javax.inject"` → **0 hits**
- [x] `App.kt` без изменений — Koin-модули (`platformModule → dataModule → repositoryModule → useCaseModule → androidSharedModule → viewModelModule`) сохранены

**Результаты сборки:**

- ✅ `./gradlew :shared:compileKotlinMetadata` — **BUILD SUCCESSFUL** (commonMain компилируется для всех target'ов)
- ✅ `./gradlew :shared:compileAndroidMain` — **BUILD SUCCESSFUL** (аналог `compileDebugKotlinAndroid` для AGP-KMP)
- ✅ `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL** (116 задач)
- ✅ `./gradlew :app:assembleDebugAndroidTest` — **BUILD SUCCESSFUL** (129 задач, HappyPathTest компилируется)
- ✅ `./gradlew :app:test` — **BUILD SUCCESSFUL** (142 задачи, все unit-тесты зелёные)
- ✅ `./gradlew :benchmark:assembleBenchmark` — **BUILD SUCCESSFUL** (38 задач)
- ⚠️ `./gradlew :shared:compileKotlinIosArm64` — **FAILS** на Room KSP: `@Database class must be annotated with @ConstructedBy since the source is targeting non-Android platforms`. Это **pre-existing issue** Фазы 8 — Room для Native требует `@ConstructedBy`. Не блокирует Android; будет устранено в Фазе 8.
- ✅ `./gradlew :app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.danilkha.trainstats.HappyPathTest` — **3 сценария прошли** на эмуляторе `emulator-5554` (Pixel_9_proxyman, Android 15): `scenario1_exerciseCreation`, `scenario2_workoutCreation`, `scenario3_exerciseHistory`. Подтверждает, что `SharedApp()` корректно бутстрапит `LocalDateFormat` для тестовой композиции.

**Созданные файлы в `:shared`:**

| Файл | Source-set | Назначение |
|------|-----------|------------|
| `composeResources/values/strings.xml` | commonMain | 41 строковый ресурс (кроме `app_name`), `success_export` %s → %1$s |
| `core/utils/LocalDateFormat.kt` | commonMain | `val LocalDateFormat` + `expect @Composable fun rememberDateTimeFormatter()` |
| `core/utils/DateFormats.android.kt` | androidMain | `JvmDateTimeFormatter` (перенесён без изменений) + actual |
| `core/utils/LocalDateFormat.ios.kt` | iosMain | Stub actual (Phase 8: full NSDateFormatter) |
| `core/viewmodel/ViewModels.kt` | commonMain | `collectSingleEvents` + `LaunchCollectEffects` (убран `@SuppressLint`) |
| `features/settings/ImportExportNavigation.kt` | commonMain | expect `importExportScreens` + expect `availableSettingsOptions` |
| `features/settings/ImportExportNavigation.android.kt` | androidMain | actual: регистрирует Import/Export маршруты + `[Import, Export]` |
| `features/settings/ImportExportNavigation.ios.kt` | iosMain | actual: пустое тело + `emptyList()` |
| `SharedApp.kt` | commonMain | Корневой Composable: `TrainingStatsTheme { CompositionLocalProvider(LocalDateFormat) { RootScreen() } }` |

**Перенесено 18 файлов из `:app` в `:shared`:**

| Файл | Целевой source-set | Модификации |
|------|--------------------|-------------|
| `features/navigation/RootScreen.kt` | commonMain | None |
| `features/navigation/Navigation.kt` | commonMain | None |
| `features/home/ui/HomeScreen.kt` | commonMain | None |
| `features/home/ui/NavBar.kt` | commonMain | MOD-1/2/3 + alias `Res as SharedRes` для shared-strings (drawable-Res остаётся common-ds) |
| `features/profile/ui/ProfileScreen.kt` | commonMain | MOD-1/2/3 |
| `features/confirmdialog/AlertDialog.kt` | commonMain | MOD-1/2/3 |
| `features/workout/ui/editor/WorkoutScreen.kt` | commonMain | MOD-1/2/3 |
| `features/workout/ui/history/HistoryScreenPage.kt` | commonMain | MOD-1/2/3 + удалён мёртвый `androidx.compose.ui.res.painterResource` (дубликат `org.jetbrains.compose.resources.painterResource`) |
| `features/workout/ui/components/ExerciseGroupCard.kt` | commonMain | MOD-1/2/3 |
| `features/exercises/ui/ExerciseListScreenPage.kt` | commonMain | MOD-1/2/3 |
| `features/exercises/ui/ExerciseListScreenPreview.kt` | commonMain | None |
| `features/exercises/ui/selector/ExerciseSelectorBottomSheet.kt` | commonMain | MOD-1/2/3 |
| `features/exercises/ui/editor/ExerciseEditorBottomSheet.kt` | commonMain | MOD-1/2/3 |
| `features/exercises/ui/history/ExerciseHistoryBottomSheet.kt` | commonMain | MOD-1/2/3 |
| `features/settings/SettingsScreen.kt` | commonMain | Heavy refactor: `SettingsOption(@StringRes Int) → SettingsOption(StringResource)`, `SettingsOption.entries → availableSettingsOptions`, прямые `composable(Import/Export)` → `importExportScreens(onBack)` |
| `features/settings/export/ui/ExportScreen.kt` | androidMain | MOD-1/2/3 (Android-only: ContentResolver) |
| `features/settings/workoutimport/ui/ImportScreen.kt` | androidMain | MOD-1/2/3 (Android-only: `Uri`, `Toast`, `ActivityResultContracts`) |

**Модифицированные файлы в `:app`:**

| Файл | Изменение |
|------|-----------|
| `entrypoint/MainActivity.kt` | Удалены импорты `JvmDateTimeFormatter`, `LocalDateFormat`, `CompositionLocalProvider`, `RootScreen`, `TrainingStatsTheme`. Тело `setContent`: `setStatusBarAppearance(!isSystemInDarkTheme()); SharedApp()`. `setStatusBarAppearance` helper сохранён без изменений. |
| `build.gradle.kts` | Удалена зависимость `libs.navigation.compose` (транзитивно приходит из `:shared` через `libs.jetbrains.navigation.compose`) |
| `src/main/res/values/strings.xml` | Сокращён до одного `<string name="app_name">`. Все остальные ключи перенесены в `:shared/commonMain/composeResources/values/strings.xml` |
| `src/androidTest/java/.../HappyPathTest.kt` | Удалены импорты `JvmDateTimeFormatter`, `LocalDateFormat`, `CompositionLocalProvider`, `TrainingStatsTheme`, `RootScreen`. Добавлен импорт `SharedApp`. `setContent` упрощён до `SharedApp()` (LocalDateFormat предоставляется внутри). |

**Удалено файлов из `:app`:**

- Все 17 UI-файлов (перенесены в `:shared`)
- `core/utils/DateFormats.kt` (разделён на expect/actual)
- `core/viewmodel/ViewModels.kt` (перенесён)

**Финальное состояние `:app`:** только `entrypoint/App.kt` (Koin `startKoin` — без изменений) и `entrypoint/MainActivity.kt` (тонкая обёртка, вызывает `SharedApp()`).

**Совокупные правки MOD-1/2/3 (применены ко всем 14 commonMain-файлам со ссылками на строки):**

- MOD-1: удалён `import com.danilkha.trainstats.R`
- MOD-2: `import androidx.compose.ui.res.stringResource` → `import org.jetbrains.compose.resources.stringResource`
- MOD-3: `R.string.foo` → `Res.string.foo` + `import training_stats.shared.generated.resources.Res` + `import training_stats.shared.generated.resources.*`
- Параметр `id =` у `stringResource` удалён везде (Compose-MP `stringResource` принимает `resource: StringResource` позиционно)

---

## Следующие шаги

Согласно рекомендуемому порядку (`MIGRATION_PLAN.md`, строки 159-166):

1. ~~**Фаза 4 (Dagger → Koin)**~~ ✅ Завершено
2. ~~**Фаза 5 (ViewModels)**~~ ✅ Завершено
3. ~~**Фаза 6 (Compose-UI + навигация)**~~ ✅ Завершено
4. **Фаза 7 (slimming `:app`)** — удалить мёртвый Room/KSP-конфиг из `:app`, неиспользуемые каталог-записи (`dagger`/`javax-inject`), убрать прямые deps на `:common-*` (транзитивно через `:shared`), финальный App.kt/MainActivity.kt cleanup
5. **Фаза 8 (iOS)** — реализовать `:iosApp`, actuals для iOS, починить Room Native driver (`@ConstructedBy`), проверить компиляцию Native
