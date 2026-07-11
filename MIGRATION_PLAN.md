# План миграции на Kotlin Multiplatform

## Принятые решения

- **Архитектура модулей:** вынести новый KMP-модуль `:shared` со всей бизнес-логикой, Room, DI (Koin), domain/data, ViewModel'ями и Compose-UI. `:app` остаётся тонкой Android-оболочкой. Добавить `:iosApp` — Xcode-проект-оболочку.
- **Навигация:** JetBrains `org.jetbrains.androidx.navigation:navigation-compose` (KMP-порт AndroidX).
- **iOS:** полная реализация (все `iosMain` actuals, рабочий `:iosApp`), без тестов.
- **DI:** Koin 4.x (Multiplatform-first, `koin-compose-viewmodel`).

### Зафиксированные решения по сложностям

- **DAO (Сложность №1):** правим **только `WorkoutDao`** — `ExerciseDao` уже полностью `suspend`. Конвертируем 9 методов + `@Transaction updateWorkout()` в `suspend` (`getWorkoutHistory(): Flow` не трогаем). `withContext(Dispatchers.IO)` в `WorkoutRepositoryImpl`/`ExerciseRepositoryImpl` **оставляем** (безвредно, `Dispatchers.IO` есть на Native). Схема БД не меняется → Room `version = 1`, миграция не нужна.
- **export/import на iOS (Сложность №2):** в этой итерации **не поддерживается**. Весь фича-код (`FileWriter`, `FileReader`, `ExportWorkoutUseCase`, `ImportWorkoutsUseCase`, `ExportViewModel`, `ImportViewModel`, `ExportScreen`, `ImportScreen`) → в **`androidMain`**. `WorkoutParser`/`WorkoutParserImpl` (чистый Kotlin) → в `commonMain`. Гейтирование навигации через **одну** expect-точку: `expect fun NavGraphBuilder.importExportScreens(onBack)` (androidMain регистрирует маршруты Import/Export, iosMain — пустое тело). Список опций в `SettingsScreen` — через `expect val availableSettingsOptions: List<SettingsOption>` (androidMain = `[Import, Export]`, iosMain = `emptyList()`). Иконку шестерёнки настроек на Profile-экране на iOS **оставляем** → пользователь попадает в пустой `SettingsHostScreen`. Регистрация export/import VM и юзкейсов в Koin — только в `androidModule`.
- **Prebuilt БД / prefill (Сложность №6):** **оставляем закомментированным.** `DatabaseSeeder` не строим, сид-файл в `composeResources` не переносим, `createFromAsset` остаётся выключенным. Обе платформы стартуют с пустой БД симметрично. Asset `app/src/main/assets/trainstatsDb.db` остаётся как рудимент. Сид для instrumented-тестов (`app/src/androidTest/assets/trainstatsDb.sql`) не двигаем.

---

## Текущее состояние (baseline)

**Уже KMP:** `common-core` (BaseViewModel/MVI/UseCase + expect `platform()`, `Float.format1/2`), `common-ds` (дизайн-система), `common-date-picker`. Инфраструктура KMP полностью рабочая (Kotlin 2.2.21, AGP KMP-library, JetBrains Compose 1.10, convention-плагины в `build-logic`).

**В `:app` (чистый Android):** Dagger 2.55 через KSP (1 `AppComponent` + 4 модуля, 31 `@Inject`-класс, 7 ViewModel), Room 2.7.2 через KSP (v1, `fallbackToDestructiveMigration`), AndroidX navigation-compose, AndroidX lifecycle. Фичи: `workout`, `exercises` (полные 3 слоя), `settings/export`, `settings/workoutimport` (3 слоя), UI-only `home/profile/navigation/confirmdialog`. **Сети нет**, приложение офлайн.

---

## Целевая архитектура

```
:app                  — тонкая Android-оболочка (App.kt, MainActivity, Manifest)
:iosApp               — Xcode-проект-оболочка
:shared (новый KMP)   — commonMain: весь бизнес-код, Room, Koin, VM, Compose-UI, навигация
                        androidMain: driver Room, Context-зависимые actuals
                        iosMain:     driver Room (NativeSqliteDriver), actuals (TextUtils, DateTimeFormatter, file IO)
:common-core/-ds/-date-picker — без изменений (уже KMP)
:benchmark            — без изменений (com.android.test)
```

---

## Декомпозиция по фазам

### Фаза 0 — Инфраструктура и каталог (без behavioural-изменений)

- `gradle/libs.versions.toml`:
  - добавить: `koin` (`io.insert:koin-core`/`koin-compose`/`koin-compose-viewmodel` 4.x), `org.jetbrains.androidx.navigation:navigation-compose`, `androidx.sqlite:sqlite-bundled` (нативный драйвер для iOS).
  - починки гигиены: `kotlin-stdlib`/`kotlin-test` 2.1.21 → 2.2.21; `org-jetbrains-kotlin-jvm` 1.8.10 → 2.2.21; удалить мёртвый `compose-compiler = 1.5.10`; удалить `ext.compileSdk=32` из корневого `build.gradle.kts`.
- `build-logic/compose-setup.gradle.kts`: **удалить мёртвый блок `jvmMain.dependencies`** (JVM-таргет не создаётся `multiplatform-library`).
- `gradle.properties`: добавить `kotlin.mpp.enableCInteropCommonization=true` (нужно, когда несколько KMP-модулей экспортируют iOS-фреймворки — `commoncoreKit`, `commondsKit`, `sharedKit`).
- `settings.gradle.kts`: `include(":shared")`, `include(":iosApp")` (для подхода «Xcode + фреймворк» `:iosApp` можно не регистрировать в Gradle — это Xcode-проект в папке `iosApp/`; выбирается при реализации Фазы 8).

### Фаза 1 — Каркас `:shared`

- `:shared/build.gradle.kts` применяет convention `compose-setup` + добавляет: `alias(libs.plugins.room)`, `alias(libs.plugins.ksp)`, доп. зависимости в `commonMain` (navigation-compose, koin-core/compose/viewmodel, room-runtime) и `androidMain`/`iosMain` по надобности.
- Зависит от `:common-core` (уже подключается `compose-setup`), `:common-ds`, `:common-date-picker`.
- `:app` объявляет `implementation(project(":shared"))`.

### Фаза 2 — Room → KMP

- Перенести в `:shared/commonMain`:
  - `entrypoint/db/TrainStatsDb.kt`, `DateTimeConverter.kt`, `StringListConverter.kt` (конвертеры уже на `kotlinx-datetime`/`List<String>` — KMP-совместимы).
  - `features/exercises/data/db/**` (ExerciseEntity/Dao/Views/RoomExerciseDatasource).
  - `features/workout/data/db/**` (WorkoutEntity/ExerciseSetEntity/RepetitionsDb/relations/WorkoutDao/RoomWorkoutDatasource).
- **Сложность №1 — `WorkoutDao` смешанный:** сейчас часть методов не `suspend` (синхронные Room-вызовы). На Native Room корректно работает только с `suspend`/`Flow`. Нужно **преобразовать все блокирующие методы DAO в `suspend`** и убрать `withContext(Dispatchers.IO)` в репозиториях (либо оставить — не страшно).
- **Driver — expect/actual:**
  - commonMain: `expect class RoomDriverFactory` или функция `expect fun createDatabase(): RoomDatabase.Builder<TrainStatsDb>`.
  - androidMain: `Room.databaseBuilder<TrainStatsDb>(context, name)` (AndroidX-драйвер).
  - iosMain: `Room.databaseBuilder<TrainStatsDb>(name).setDriver(NativeSqliteDriver(...))` через `androidx.sqlite:native`/`sqlite-bundled`.
- KSP KMP: блок `ksp { arg("room.generateKotlin","true") }` (уже стоит) + `room { schemaDirectory(...) }` — для KMP схему нужно задавать **на каждый target**. Плагин `androidx.room` это умеет.
- **Prebuilt asset (`createFromAsset`) и `fallbackToDestructiveMigration`:** `createFromAsset` **оставляем закомментированным** (решение по Сложности №6). `fallbackToDestructiveMigration` кроссплатформенный, оставляем. Обе платформы стартуют с пустой БД симметрично; asset `trainstatsDb.db` остаётся как рудимент, `DatabaseSeeder` не строим.
- **Дополнительно к Сложности №1:** в `WorkoutDao` конвертируем в `suspend` методы `getAll`, `getWorkoutById`, `saveWorkout`, `saveSets`, `deleteWorkoutExercises`, `updateWorkout` (`@Transaction`), `commitWorkoutSave`, `archiveWorkout`, `deleteWorkout`. `getWorkoutHistory(): Flow` и `getHistoryByExercise` (уже suspend) — без изменений. `ExerciseDao` уже готов. `withContext(Dispatchers.IO)` в репозиториях оставляем.

### Фаза 3 — Domain + Data в `commonMain`

- Перенести (чистый Kotlin, без Android-API):
  - `domain/model/**`, `domain/usecase/**` (уже наследуют KMP-базы `UseCase`/`FlowUseCase` из common-core).
  - интерфейсы репозиториев и парсера (`WorkoutRepository`, `ExerciseRepository`, `WorkoutParser`).
  - имплементации `WorkoutRepositoryImpl`, `ExerciseRepositoryImpl`, `WorkoutParserImpl` (regex, чистый Kotlin), `RoomExerciseDatasource`, `RoomWorkoutDatasource`.
- **Сложность №2 — export/import не делается на iOS в этой итерации** (решено). Размещение кода по source-set'ам:
  - `FileWriter`, `FileReader`, `ExportWorkoutUseCase`, `ImportWorkoutsUseCase`, `ExportViewModel`, `ImportViewModel`, `ExportScreen`, `ImportScreen`, `ImportState` → **`androidMain`** (Android-only: `ContentResolver`/`MediaStore`).
  - `WorkoutParser` (интерфейс) + `WorkoutParserImpl` (regex) → **`commonMain`** (чистый Kotlin).
  - Гейтирование навигации: `expect fun NavGraphBuilder.importExportScreens(onBack: () -> Unit)` — androidMain регистрирует `composable(Import){ImportScreenRoute}` + `composable(Export){ExportScreenPage}`, iosMain — пустое тело. `SettingsHostScreen` в commonMain вызывает `importExportScreens(onBack)` вместо двух прямых `composable(...)`.
  - Список опций: `expect val availableSettingsOptions: List<SettingsOption>` → androidMain `[Import, Export]`, iosMain `emptyList()`. Иконка настроек на iOS остаётся → пустой `SettingsHostScreen`.
  - Koin: export/import VM и юзкейсы регистрируются **только в `androidModule`**. `WorkoutParser`/`WorkoutParserImpl` — в общем модуле (нужен `ImportWorkoutsUseCase` из androidMain).
  - Побочный эффект: строки `R.string.to_import`/`to_export`/`settings` (и вообще все `@StringRes`/`stringResource`) надо перенести в Compose Multiplatform resources — это часть сквозной задачи Фазы 6.

### Фаза 4 — Dagger → Koin 4.x

- Добавить Koin-зависимости (koin-core, koin-compose, koin-compose-viewmodel) в `:shared/commonMain`.
- Удалить все Dagger-аннотации: `@Inject`, `@Singleton`, `@Module`, `@Binds`, `@Provides`, `@Component`, `@Qualifier`. Заменить `@Inject constructor(...)` на обычные `constructor(...)`.
- Koin-модули в `:shared/commonMain`:
  - `dataModule`: `singleOf(::TrainStatsDb)`, DAOs, `singleOf<WorkoutLocalDatasource>(::RoomWorkoutDatasource)`, etc.
  - `repositoryModule`: `singleOf<WorkoutRepository>(::WorkoutRepositoryImpl)`, `singleOf<ExerciseRepository>(::ExerciseRepositoryImpl)`, `singleOf<WorkoutParser>(::WorkoutParserImpl)`.
  - `useCaseModule`: `factoryOf(::SaveWorkoutUseCase)` и т.д. (31 класс).
  - `viewModelModule`: `viewModelOf(::WorkoutViewModel)`, все 7 VM.
  - `platformModule`: expect — androidMain/iOS предоставляют конкретные реализации Room driver (file IO для export/import — только androidModule, т.к. фича Android-only в этой итерации).
- Старт: `expect fun initKoin(...)` либо `KoinApplication{}` Compose-обёртка; Android передаёт `androidContext`, iOS — без контекста.

### Фаза 5 — ViewModels

- 7 VM переносятся в `:shared/commonMain`. Базы уже KMP (common-core).
- `WorkoutSaver` (`@Singleton @Inject`, владеет собственным `CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)`) → обычный класс, в Koin как `singleOf(::WorkoutSaver)`.
- **Сложность №3 — замена механизма резолва VM:** сейчас `ViewModelsProvider` interface + `LocalViewModelsProvider staticCompositionLocalOf` + два хелпера (`getViewModel`, `getCurrentViewModel` через каст `App`→`appComponent`). Всё это **упраздняется**. На смену — Koin 4.x `koinViewModel<T>()` из `koin-compose-viewmodel` (единый API в commonMain, на Android использует `ViewModelStoreOwner`, на iOS — Compose-local scope). Удалить `core/viewmodel/ViewModels.kt` (или оставить только утилиты вроде `LaunchCollectEffects`).
- `Dispatchers.Main.immediate` поддерживается на iOS (kotlinx-coroutines-native). ОК.
- **Сложность №4 — два ad-hoc `android.util.Log`** (`ExerciseEditorViewModel`, `ImportWorkoutsUseCase`) → заменить на Napier (уже KMP).

### Фаза 6 — Compose-UI и навигация

- Все экраны (`RootScreen`, `HomeScreen`, `WorkoutScreen`, `HistoryScreenPage`, `ExerciseList*`, `ExerciseEditor*`, `ExerciseHistory*`, `ProfileScreen`, `NavBar`, `AlertDialog`) переносятся в `:shared/commonMain` (они уже Compose-MP-совместимы, дизайн-система в common-ds). **Исключение:** `ExportScreen`/`ImportScreen` остаются в `androidMain` (см. Фазу 3), подключаются к графу через `expect fun NavGraphBuilder.importExportScreens`.
- **Навигация:** заменить `androidx.navigation:navigation-compose` (Android-only) на `org.jetbrains.androidx.navigation:navigation-compose` (KMP-порт, API почти идентичен — `NavHost`, `composable(route)`, `rememberNavController()`). Изменения в `RootScreen`/`Navigation` минимальные.
- **`LocalDateFormat`** (`core/utils/DateFormats.kt`) использует Android-`DateTimeFormatter` + `Context`. Перенести на интерфейс `DateTimeFormatter` из common-core (сейчас без реализаций) с expect/actual: androidMain — `java.time.format.DateTimeFormatter`, iosMain — `NSDateFormatter` или kotlinx-datetime formatting.
- В `:shared/commonMain` определить корневую `@Composable fun SharedApp()` (`NavHost` + KoinApplication + тема).

### Фаза 7 — Оболочка `:app` (Android)

- `:app/build.gradle.kts`: убрать `ksp(libs.dagger.compiler)` и зависимость `libs.dagger`. KSP-for-Room уходит в `:shared`. Оставить `com.android.application`, `kotlin-android`, `kotlin.plugin.compose`, `benchmark` build type.
- **Удалить:** `di/` целиком (`AppComponent`, `RepositoryModule`, `DatasourceModule`, `DbModule`, `AndroidModule`, `ViewModelsProvider`), `core/viewmodel/ViewModels.kt`, `core/utils/ContextUtils.kt` (нужно было только для `getCurrentViewModel`), `entrypoint/db/` (переехал в `:shared`).
- `App.kt`: `startKoin { androidContext(this@App); modules(sharedModules + androidModule) }`, Napier-инициализация.
- `MainActivity.kt`: `setContent { SharedApp() }` (корневой composable из `:shared`). `FragmentActivity` оставить (нужно для `LocalViewModelStoreOwner` на Android-стороне Koin).
- `AndroidManifest`, ресурсы, build types — без изменений. **`:benchmark`** остаётся `com.android.test` → `:app`, проверить что benchmark-вариант собирается после slimming.

### Фаза 8 — iOS-приложение (`:iosApp`)

- Подход «Xcode-проект + потребление `sharedKit` xcframework» (классика; convention-плагин уже настраивает `iosX64/iosArm64/iosSimulatorArm64` с `framework.baseName = "sharedKit"`).
- Папка `iosApp/` содержит Xcode-проект (Swift). Точка входа вызывает `MainViewControllerKt.mainViewController { ... }`.
- В `:shared/iosMain`: `fun MainViewController() = ComposeUIViewController { KoinApplication(...) { SharedApp() } }`.
- **Реализовать iosMain-actuals:**
  - `TextUtils.ios.kt` (`format1`/`format2` сейчас `TODO("ios")`) → тривиально через `String.format("%.1f", this)` (Kotlin/Native умеет).
  - `DateTimeFormatter` ios actual → `NSDateFormatter` или kotlinx-datetime formatting API.
  - Room driver → `NativeSqliteDriver` (зависимость `androidx.sqlite:sqlite-bundled`).
  - **export/import на iOS:** в этой итерации **не реализуется** (см. Сложность №2). Соответствующих actuals на iOS нет; маршруты и точки входа гейтятся как описано в Фазе 3.
- `startKoin` для iOS: `startKoin { modules(sharedModules + iosModule) }`.

---

## Сводка сложностей и рисков

| # | Сложность | Где | Митигация |
|---|-----------|-----|-----------|
| 1 | `WorkoutDao` смешанные suspend/не-suspend методы | Фаза 2 | ✅ РЕШЕНО: 9 методов + `updateWorkout` → `suspend` (`ExerciseDao` уже готов); `withContext(IO)` в репозиториях оставляем; схема не меняется, Room v1 без миграции |
| 2 | **export/import через ContentResolver/MediaStore** — не переносим на iOS | Фаза 3, 7 | ✅ РЕШЕНО: в этой итерации iOS не поддерживается. Фича-код → `androidMain`; гейтирование через `expect fun NavGraphBuilder.importExportScreens` + `expect val availableSettingsOptions`; шестерёнка настроек на iOS остаётся → пустой экран |
| 3 | Механизм резолва VM (`ViewModelsProvider` + `LocalViewModelsProvider` + каст `App`) | Фаза 5 | Полная замена на `koinViewModel<T>()`; упрощает код, но затрагивает все 7 экранов |
| 4 | Ad-hoc `android.util.Log` | Фаза 5 | Заменить на Napier |
| 5 | Room driver expect/actual + schema per-target + KSP-KMP config | Фаза 2 | Плагин `androidx.room` умеет; схемы в `:shared/schemas` |
| 6 | `createFromAsset` для prebuilt-БД закомментирован; asset-загрузка Android-only | Фаза 2 | ✅ РЕШЕНО: prefill **оставляем закомментированным**; `DatabaseSeeder` не строим; обе платформы стартуют с пустой БД симметрично; asset остаётся как рудимент |
| 7 | DB v1 + миграции при рефакторинге | Фаза 2 | Если **формы сущностей не меняются** — миграция не нужна (схема та же). Иначе — bump version + Migration |
| 8 | Мёртвый `jvmMain` блок в `compose-setup` (no JVM target) | Фаза 0 | Удалить |
| 9 | Catalog-расхождения версий (stdlib 2.1.21, kotlin-jvm 1.8.10) | Фаза 0 | Выровнять с Kotlin 2.2.21 |
| 10 | `WorkoutSaver` со своим `CoroutineScope` как `@Singleton` | Фаза 4, 5 | Koin `singleOf` — гарантирует один инстанс |
| 11 | `common-ds` использует Material2 (`compose.material`), не Material3 | Фаза 6 | Работает; только verify тему на iOS |
| 12 | Векторные drawable XML в common-ds | Фаза 6 | Compose MP-resources умеет material-vector XML в commonMain; verify на iOS |
| 13 | `Dispatchers.Main.immediate` в MVI/`WorkoutSaver` | Фаза 5 | Поддерживается на iOS; ОК |
| 14 | Instrumented-тесты (`androidTest`) завязаны на Dagger + Room Android-API | после Фазы 7 | Перенастроить на Koin-test; `createTestDb()` через Android-сторону Room KMP работает |
| 15 | Unit-тесты (Kotest) — KMP-friendly, но **mockk не работает на Native** | после | Моки оставить в `commonTest`/JVM-target (если добавите) или `androidTest`; pure-logic в `commonTest` |
| 16 | `:benchmark` (com.android.test) | Фаза 7 | Без изменений, но проверить benchmark-build-type после slimming `:app` |
| 17 | Koin 4.x `koin-compose-viewmodel` на iOS — зрелость API | Фаза 5 | Зафиксировать стабильную 4.x; smoke-test на iOS-симуляторе |

---

## Рекомендуемый порядок (минимизация риска)

1. **Фаза 0** (инфра) — низкий риск, оркестрация.
2. **Фаза 2 + 3 для `workout`/`exercises` domain+data в новый `:shared`** — физический переезд кода, без DI-смены (временно оставив Dagger-обвязку через `:app` re-export). Позволяет проверить компиляцию KMP инкрементально.
3. **Фаза 4 (Koin)** — только после того как код лежит в `:shared`; Dagger удаляется одним коммитом на Koin.
4. **Фаза 5 (VM) + 6 (UI/nav)** — резолв VM и навигация.
5. **Фаза 7 (slimming `:app`)** — финальная проверка Android.
6. **Фаза 1 завершается каркасом до Фазы 2; Фаза 8 (iOS)** — последней, после того как Android снова полностью зелёный.

---

## Решения по сложностям (зафиксировано)

1. **DB continuity:** `fallbackToDestructiveMigration` оставляем. Схема БД не меняется от переезда (формы сущностей те же) → Room `version = 1`, миграция не нужна.
2. **export/import на iOS:** в этой итерации **не поддерживается**. Фича изолирована в `androidMain`, гейтится через `expect fun NavGraphBuilder.importExportScreens` + `expect val availableSettingsOptions`. Шестерёнка настроек на iOS остаётся → пустой экран настроек.
3. **Prebuilt БД / prefill:** **оставляем закомментированным**. `DatabaseSeeder` не строится, обе платформы стартуют с пустой БД симметрично.
4. **JVM-таргет:** десктоп **не нужен** — мёртвый `jvmMain`-блок в `compose-setup` удаляется в Фазе 0.
5. **Скоуп тестов:** iOS — без тестов. Существующие unit-тесты (Kotest) переезжают в `:shared/commonTest` (JVM-run) и должны остаться зелёными; mockk работает на JVM. Instrumented-тесты (`androidTest`) остаются Android-only, после Фазы 7 перенастраиваются на Koin-test.

## Оставшиеся открытые вопросы (на момент старта реализации)

- При илзючении KSP-KMP на iOS-таргетах: точная конфигурация `room { schemaDirectory(...) }` per-target (`android`, `iosArm64`, `iosX64`, `iosSimulatorArm64`) — сверить с актуальной документацией `androidx.room` 2.7.x на момент Фазы 2.
- Версия Koin 4.x: зафиксировать конкретный patch-релиз в Фазе 0 после проверки совместимости с Compose Multiplatform 1.10.
- `androidx.sqlite:sqlite-bundled` vs `androidx.sqlite:native` для iOS-драйвера Room — финально выбрать в Фазе 2 (bundled тяжелее, но стабильнее на старых iOS).
