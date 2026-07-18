# Прогресс миграции на Kotlin Multiplatform

> Последнее обновление: 18 июля 2026

## Сводка

| Фаза | Описание | Статус |
|------|----------|--------|
| **0** | Инфраструктура и каталог | ✅ Завершено |
| **1** | Каркас `:shared` | ✅ Завершено |
| **2** | Room → KMP | ✅ Завершено |
| **3** | Domain + Data в `commonMain` | ✅ Завершено |
| **4** | Dagger → Koin 4.x | ✅ Завершено |
| **5** | ViewModels | ✅ Завершено |
| 6 | Compose-UI и навигация | ⬜ Не начато |
| 7 | Оболочка `:app` (slimming) | ⬜ Не начато |
| 8 | iOS-приложение (`:iosApp`) | ⬜ Не начато |

### Результаты сборки (на момент завершения Фаз 0-5)

- ✅ `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL** (113 задач)
- ✅ `./gradlew test` — **BUILD SUCCESSFUL** (139 задач, все unit-тесты зелёные)
- ✅ `./gradlew :app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.danilkha.trainstats.HappyPathTest` — **3 сценария прошли** (scenario1_exerciseCreation, scenario2_workoutCreation, scenario3_exerciseHistory)

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
| 8 | 5 | 5 smart-cast правок в 3 `@Composable`-экранах (`ExerciseListScreenPage`, `ExerciseHistoryBottomSheet`, `WorkoutScreen`) | После переноса state-классов в другой модуль Kotlin'у требуется локальная `val` для smart-cast'а cross-module public API properties. Изменения минимальные (extract-to-local), без логических правок |
| 9 | 5 | Создан `TestActivity` + `app/src/debug/AndroidManifest.xml` | Предсуществующая проблема test-infra (см. раздел выше): `androidx.test:core 1.7.0` не регистрирует `ComponentActivity` автоматически. Не относится к Phase 5, но решена в рамках итерации, т.к. `HappyPathTest` — контрольный gate |

---

## Нерешённые вопросы / технический долг (для последующих фаз)

| # | Описание | Фаза устранения |
|---|----------|----------------|
| ~~1~~ | ~~`:app` сохраняет мёртвый Room/KSP-конфиг~~ | Фаза 7 |
| ~~2~~ | ~~Неиспользуемые каталог-записи `dagger`/`dagger-compiler`/`javax-inject` в `libs.versions.toml`~~ | Фаза 7 |
| ~~3~~ | ~~`android.util.Log` в `ExerciseEditorViewModel` и `ImportWorkoutsUseCase`~~ | **✅ Устранено в Фазе 5** (замена на Napier; фактически был только в `ImportWorkoutsUseCase`) |
| 4 | Пустые директории-оболочки в `:app` (git не трекает) | Косметика, можно очистить |
| 5 | `FakeWorkoutRepository`/`FakeExerciseRepository` — мёртвый код, не зарегистрированы в Koin | Можно удалить в любой фазе |
| ~~6~~ | ~~VMs и `appModule` всё ещё в `:app` (перенос в `:shared/commonMain`)~~ | **✅ Устранено в Фазе 5** (5 VM + Saver в commonMain, 2 VM в androidMain, `viewModelModule` создан) |
| 7 | Компиляция iOS-таргетов `:shared` не проверялась | Фаза 8 |
| 8 | `:app` всё ещё содержит `:common-core`, `:common-ds`, `:common-date-picker` как прямые зависимости (транзитивно доступны через `:shared`) | Фаза 7 (slimming) |
| 9 | `core/viewmodel/ViewModels.kt` (`collectSingleEvents` + `LaunchCollectEffects`) остаётся в `:app` | Фаза 6 (UI migration) — это Compose-хелперы, переедут вместе с UI |
| 10 | Неосвещённые instrumented-тесты: `ExportImportTest`, `WorkoutDaoTest`, `RoomWorkoutDatasourceTest` (были red/непроходящие до Фазы 5) | Отдельный ticket — не блок Phase 5 |

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

---

## Следующие шаги

Согласно рекомендуемому порядку (`MIGRATION_PLAN.md`, строки 159-166):

1. ~~**Фаза 4 (Dagger → Koin)**~~ ✅ Завершено
2. ~~**Фаза 5 (ViewModels)**~~ ✅ Завершено
3. **Фаза 6 (Compose-UI + навигация)** — перенести экраны, заменить AndroidX navigation на JetBrains KMP, создать `SharedApp()`, перенести `core/viewmodel/ViewModels.kt` в `:shared/commonMain`
4. **Фаза 7 (slimming `:app`)** — удалить мёртвый Room/KSP-конфиг, неиспользуемые каталог-записи (`dagger`/`javax-inject`), убрать прямые deps на `:common-*` (транзитивно через `:shared`), обновить `App.kt`/`MainActivity.kt`
5. **Фаза 8 (iOS)** — реализовать `:iosApp`, actuals для iOS, проверить компиляцию Native
