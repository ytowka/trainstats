# План: KMP + нативный SwiftUI на iOS (v2)

> Переработан 15.09.2026. Предыдущий план предполагал Compose Multiplatform на обеих
> платформах; его фазы 0–7 (Dagger→Koin, Room KMP, VM, UI в `:shared`, slim `:app`)
> завершены и удалены из плана. Новый курс: **iOS — нативный SwiftUI**, Compose
> остаётся только для Android.

## Принятые решения

- **Концепция:** Android — Compose (как сейчас), iOS — нативный SwiftUI. Общее
  KMP-ядро без единой Compose-зависимости в iOS-пути сборки.
- **Расщепление `:shared`:** ядро (domain + data + ViewModel'ы + DI, без Compose)
  остаётся в `:shared`; весь Compose-UI выносится в новый модуль `:ui-compose`.
- **`common-core` очищается от Compose** (`LocalDateFormat`, `ViewModels.kt`-хелперы →
  `:ui-compose`), иначе Compose попадал бы в iOS-фреймворк транзитивно.
- **ARCH_REFACTOR** (`plan/features/ARCH_REFACTOR_PLAN.md`, декомпозиция на feature-модули)
  — **параллельный трек, не смешивается** с этим планом. Новый план строится от текущей
  структуры; ui-модули будущей декомпозиции станут android-only.
- **`Dispatchers.IO`:** убрать `withContext(Dispatchers.IO)` из репозиториев — Room
  suspend-методы dispatcher-агностичны (на Native `Dispatchers.IO` —
  `@ExperimentalCoroutinesApi`, из-за этого iOS-компиляция падает).
- **iOS interop:** SKIE + `MviViewModelWrapper` по `KMP_VIEWMODEL_IOS_INTEROP.md`
  (документ — рабочий референс, не меняется).
- **Навигация:** Navigation 3 (`:navigation:api`/`:navigation:impl`) и JB-порт
  lifecycle — **только в `:ui-compose`** (Android UI). На iOS — SwiftUI NavigationStack.
- **backup (import/export):** на iOS не поддерживается, код остаётся в androidMain.
- **Строки на iOS:** дублируются в String Catalog (осознанно; Compose Resources на
  SwiftUI не работают). Локализация — RU, паритет с `strings.xml`.
- **`iosApp/`:** Xcode-проект создаётся с нуля, исходники коммитятся (сейчас в git
  только build-артефакты от пробных сборок).

---

## Текущее состояние (baseline, сентябрь 2026)

### Структура

Модули живут в `sources/` и подключаются через `includeSourceModule()` в
`settings.gradle.kts`:

| Модуль | Тип | Содержимое |
|---|---|---|
| `:app` | android-app | `App.kt` (Koin `startKoin`), `MainActivity` (`SharedApp()`), androidTest |
| `:shared` | KMP + Compose | domain/data обеих фич, все VM + State/Event, `WorkoutSaver`, все Compose-экраны, Koin-модули; androidMain: export/import; iosMain: `PlatformModule.ios`, `ImportExportNavigation.ios` |
| `:common-core` | KMP + Compose-runtime | usecase-базы, `MviViewModel`/`IMviViewModel` (androidx.lifecycle), `LocalDateFormat` (Compose), TextUtils/DateFormats/Ids(UUID), Napier |
| `:common-db` | KMP | Room: `TrainStatsDb` (**`@ConstructedBy` уже настроен**), DAO, конвертеры, `DatabaseDriverFactory` expect/actual (android + ios), `schemas/1.json`, KSP на Android и iOS-таргетах, `sqlite-bundled` в iosMain |
| `:common-db-api` | KMP | entities/views (+`PendingUpdateEntity` — задел под server-sync) |
| `:common-ds`, `:common-date-picker`, `:common:alertdialog`, `:common:bottomsheet` | KMP + Compose | дизайн-система и компоненты |
| `:navigation:api` / `:navigation:impl` | KMP + Compose | Navigation 3: `Navigator`, destinations, `NavHost` |
| `:features:exercises` | — | пустая заготовка под ARCH_REFACTOR (не регистрирован) |
| `:benchmark` | com.android.test | без изменений |
| `iosApp/` | — | только build-артефакты, исходников нет |
| `backend/trainstats` | Spring Boot | отдельный проект, вне скоупа |

### Версии (актуальные)

Kotlin 2.3.21, AGP 8.9.3, KSP 2.3.5, Room 2.8.4, Koin 4.0.0, coroutines 1.10.1,
Compose MP 1.10.0, nav3 1.1.7 / nav3-ui 1.1.1 / lifecycle-viewmodel-nav3 2.10.0,
kotlinx-datetime 0.7.1, sqlite-bundled 2.5.0, Napier 2.7.1.

### Что уже работает

- Android собирается и проходит гейты (`assembleDebug`, `test`, `lint`,
  `connectedAndroidTest` HappyPathTest 3/3, `:benchmark:assembleBenchmark`).
- MVI-VM отвязана от Compose: `IMviViewModel` (интерфейс) + `MviViewModel`
  (androidx.lifecycle.ViewModel, KMP). Все State/Event/SideEffect — `sealed interface`
  → SKIE даст плоский Swift-API из коробки.
- Room полностью KMP: `@ConstructedBy` + KSP на iOS-таргетах + Native-драйвер.
- UUID-генерация (`Ids.kt`), конвертеры на kotlinx-datetime.

### Известные блокеры/заглушки iOS-пути

| # | Проблема | Где |
|---|---|---|
| 1 | `withContext(Dispatchers.IO)` — на Native `@ExperimentalCoroutinesApi`, компиляция iOS падает | `WorkoutRepositoryImpl`, `ExerciseRepositoryImpl` (12 вызовов) |
| 2 | Lifecycle klib-конфликт: дубликаты `lifecycle-viewmodel-savedstate` 2.8.4/2.9.0, downgrade `org.jetbrains.androidx.lifecycle 2.9.6 → 2.8.4` (следы дебага в `tmp/`) | линковка iOS-фреймворка; ожидание — уйдёт после Ф1 (JB-порт уйдёт из дерева `:shared`) |
| 3 | `TextUtils.ios` = `TODO("ios")` — упадёт в рантайме | `common-core/iosMain` |
| 4 | `LocalDateFormat.ios` — ISO-заглушка вместо форматтера | `common-core/iosMain` |
| 5 | Xcode-проекта нет | `iosApp/` |

---

## Целевая архитектура

```
:app          — тонкая Android-оболочка (App.kt startKoin, MainActivity → SharedApp())
:ui-compose   — НОВЫЙ: весь Compose-UI (экраны, SharedApp, RootScreen/nav3, SettingsScreen,
                bottomsheet'ы, LocalDateFormat-обёртка, Compose-ресурсы, koin-compose)
:shared       — KMP-ядро БЕЗ Compose (multiplatform-library): domain, data, VM+State+Event,
                WorkoutSaver, Koin-модули, ViewModelProvider, SKIE, initKoin для iOS
:common-core  — чистый KMP (без Compose): usecase-базы, MVI, DateTimeFormatter-фабрика,
                TextUtils, Ids, DateUtils
:common-db / :common-db-api / :common-ds / :common-date-picker /
:common:alertdialog / :common:bottomsheet / :navigation:api|impl — как сейчас
                (Compose-модули потребляются только :ui-compose и :app)
:benchmark    — без изменений
iosApp/       — SwiftUI Xcode-проект, линкует ТОЛЬКО sharedKit (транзитивно :shared-дерево)
```

**iOS-путь сборки = дерево `:shared`**: без Compose, без JB-lifecycle → лёгкий фреймворк,
конфликт №2 выпадает из пути.

---

## Фазы

### Ф1 — Расщепление `:shared` → `:shared` (ядро) + `:ui-compose`

Android-гейты после каждого шага. Пакеты при переносе не меняем (кроме явно
указанного).

1. **Скелет `:ui-compose`** (`sources/ui-compose`, convention `compose-setup`,
   регистрация через `includeSourceModule`):
   deps: `api(project(":shared"))`, `:common-ds`, `:common-date-picker`,
   `:common:alertdialog`, `:common:bottomsheet`, `:navigation:api`, `:navigation:impl`,
   nav3-runtime/ui, koin-compose, koin-compose-viewmodel.
   `:app`: `implementation(project(":ui-compose"))` (плюс существующий
   `implementation(project(":shared"))` для `App.kt`).
2. **Перенос Compose-кода `:shared` → `:ui-compose`** (пакеты те же):
   - commonMain: `SharedApp.kt`, `features/navigation/RootScreen.kt`, `HomeScreen`,
     `NavBar`, `ProfileScreen`, `WorkoutScreen`, `ExerciseGroupCard`,
     `HistoryScreenPage`, `ExerciseListScreenPage`(+Preview), selector/editor
     bottomsheet'ы, `ExerciseHistoryBottomSheet`, `SettingsScreen`,
     `ImportExportNavigation.kt` (expect).
   - androidMain: `ExportScreen`, `ImportScreen`, `ImportExportNavigation.android`.
   - `ImportExportNavigation.ios` — удалить; `expect val availableSettingsOptions`
     упразднить → обычный `val` в androidMain `:ui-compose`.
   - `core/viewmodel/ViewModels.kt` (Compose-хелперы) и `LocalDateFormat`
     (Compose-обёртка) → `:ui-compose` (см. шаг 4).
   - `composeResources/` (strings.xml и пр.) → `:ui-compose`; при переезде проверить
     generated-Res импорты (sed-проход, как в старой Фазе 6).
3. **Очистка `:shared`**: convention `compose-setup` → `multiplatform-library`; убрать
   deps `:common-ds`, `:common-date-picker`, `:common:alertdialog`, `:common:bottomsheet`,
   `:navigation:*`, nav3, koin-compose/viewmodel. Остаются: `:common-db`,
   `:common-core` (транзитивно), coroutines, datetime, koin-core, room-runtime.
4. **`common-core` без Compose**: перенести `LocalDateFormat.kt` (composition local +
   `rememberDateTimeFormatter` expect) и Compose-actual'ы в `:ui-compose`; в commonMain
   добавить **чистую** `expect fun createDateTimeFormatter(): DateTimeFormatter`
   (android actual — `JvmDateTimeFormatter`, ios actual — NSDateFormatter, см. Ф2);
   убрать плагины `composeMultiplatform`/`compose.compiler` и deps `compose.runtime`/
   `compose.ui`. Проверить, что `common-ds`/`common-date-picker` (зависят от common-core)
   собираются.
5. **`:app`**: Compose-библиотеки экранов (foundation/material/ui/icons/resources)
   переносятся в `:ui-compose`; в `:app` остаются activity-compose, tooling,
   test-депы, koin-android. `HappyPathTest` — обновить импорт `SharedApp`.
6. **`:ui-compose` iOS-таргеты**: convention создаёт iosArm64/… задачи. В iosMain
   положить тривиальные stub-actual'ы (`rememberDateTimeFormatter` и др.), чтобы
   `compileKotlinIos*` оставались зелёными; iOS-приложение этот модуль не линкует.

**Гейты Ф1:** `:app:assembleDebug`, `:app:test`, `:app:lint`, `connectedAndroidTest`
(HappyPathTest), `:shared:compileKotlinMetadata`, `:common-core:compileKotlinMetadata`.

### Ф2 — iOS-компиляция ядра

1. Убрать `withContext(Dispatchers.IO)` в `WorkoutRepositoryImpl` /
   `ExerciseRepositoryImpl` (12 вызовов; Room сам переключает контекст).
   `ExportWorkoutUseCase` (androidMain) не трогаем — JVM-`Dispatchers.IO` стабилен.
2. `TextUtils.ios`: реальная реализация (`String.format("%.1f"/"%.2f")`).
3. `createDateTimeFormatter()` ios actual: `NSDateFormatter` с паттернами,
   идентичными `JvmDateTimeFormatter`.
4. Lifecycle klib-конфликт: проверить линковкой. Ожидание — после Ф1 из дерева
   `:shared` ушли JB-lifecycle/compose. Если остался — выровнять версии
   (resolutionStrategy / обновление lifecycleViewmodelNav3) до зелёной линковки.

**Гейты Ф2:** `:shared:compileKotlinIosSimulatorArm64`,
`:common-core:compileKotlinIosSimulatorArm64`,
`:shared:linkDebugFrameworkIosSimulatorArm64` — все зелёные.

### Ф3 — Interop-инфраструктура (SKIE)

1. `gradle/libs.versions.toml`: плагин `co.touchlab.skie` — зафиксировать версию,
   совместимую с Kotlin 2.3.21 (проверить на момент реализации).
2. `:shared`: применить SKIE-плагин. Никаких иных Flow-мостов.
3. `:shared/iosMain`:
   - `object ViewModelProvider : KoinComponent` — по одной функции на commonMain-VM
     (Workout, ExerciseList, History, ExerciseEditor, ExerciseHistory).
   - `fun initKoin()` — `startKoin { modules(platformModule, dataModule,
     repositoryModule, useCaseModule, viewModelModule) }` + Napier-инициализация.
4. Проверить сгенерированный API фреймворка: `StateFlow` → `AsyncSequence`,
   sealed → плоские имена, `suspend` → `async`.

**Гейты Ф3:** линковка framework зелёная; smoke-check заголовков
(DerivedData/headers или `swift-api-export`).

### Ф4 — `iosApp`: Xcode-каркас

1. Создать Xcode-проект (SwiftUI App lifecycle, min iOS 16+), таргет `iosApp`,
   исходники в git (`.gitignore`: `iosApp/build/` уже покрыт глобальным `build/`).
2. Подключение `sharedKit`: build phase → `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`
   (конвенция уже даёт `baseName = "sharedKit"`), embed framework.
3. `MviViewModelWrapper<Intent, State, SideEffect>` (код из
   `KMP_VIEWMODEL_IOS_INTEROP.md` §5) — единственный interop-файл.
4. `@main` + AppDelegate: `ViewModelProvider`-независимый `initKoin()` до первого
   экрана.
5. Каркас UI: `TabView` — История (Home) / Тренировка / Профиль; пустые placeholder-View.
6. Минимальная SwiftUI-дизайн-система: Assets (цвета) + модификаторы типографики,
   значения из `common-ds` (дублирование осознанное). Strings — String Catalog (RU).

**Гейты Ф4:** запуск на симуляторе: Koin стартует, Room создаёт пустую БД
(NativeSqliteDriver), табы переключаются, без крэшей.

### Ф5 — Экраны SwiftUI (по одному)

Каждый подэтап — отдельная итерация. Гейт: экран работает на симуляторе
(+ ручной смоук пути) и Android-регрессия не тронута (`:app:assembleDebug`).

| # | Экран | Содержимое |
|---|---|---|
| 5.1 | Workout (главный) | группы упражнений/подходов, редактирование веса/повторов, DatePicker (нативный), удаление сетов/групп, добавление упражнения → selector, undo; логика `WorkoutSaver` уже в VM |
| 5.2 | History (Home tab) | список тренировок, архивирование, удаление, confirm-диалоги |
| 5.3 | ExerciseList + Selector + Editor | список/поиск/создание/редактирование упражнений |
| 5.4 | ExerciseHistory | `.sheet` по упражнению из Workout/History |
| 5.5 | Profile | профиль; настройки — скрыть (backup на iOS вне скоупа) |

### Ф6 — QA и финализация

1. Android-гейты: `:app:assembleDebug`, `test`, `lint`, `connectedAndroidTest`
   (HappyPathTest), `:benchmark:assembleBenchmark`.
2. iOS: clean build + ручной happy path (создать упражнение → собрать тренировку →
   история → история упражнения).
3. Документация: обновить `AGENTS.md` (модульная структура устарела), `BOARD.md`
   (статус эпика KMP), при желании почистить `tmp/`.

---

## Рекомендуемый порядок

Ф1 → Ф2 → Ф3 → Ф4 → Ф5.1…Ф5.5 → Ф6. Ф1 — самая механическая и защищена
Android-гейтами; Ф2/Ф3 небольшие; Ф5 — основной объём, наращивается по экранам.

---

## Риски и сложности

| # | Риск | Митигация |
|---|---|---|
| 1 | Lifecycle klib-конфликт на линковке iOS | Уходит из `:shared` после Ф1 (JB-порт/compose только в `:ui-compose`); fallback — выравнивание версий lifecycle в Ф2 |
| 2 | SKIE ↔ Kotlin 2.3.21 / Koin 4.0.0 | Зафиксировать совместимую версию SKIE в Ф3; fallback — KMP-NativeCoroutines (менять только одно решение) |
| 3 | `common-core` без Compose ломает `common-ds`/`common-date-picker` | Чистый KMP-dep безопасен; проверить на шаге 4 гейтами |
| 4 | Переезд `strings.xml` ломает `Res`-импорты | Sed-проход по образцу старой Фазы 6; компиляция ловит все случаи |
| 5 | `:ui-compose` iOS-таргеты падают (JB-lifecycle) | Stub-actual'ы в iosMain; модуль не входит в iOS-путь линковки; при желании — отключить iOS-таргеты отдельным convention'ом позже |
| 6 | `HappyPathTest`/`ExportImportTest` завязаны на перемещаемые классы | Обновить импорты (`SharedApp` из `:ui-compose`, `ImportExportNavigation`); androidTest-депы `:common-core`/`:common-db` в `:app` уже скомпенсированы |
| 7 | Room Native-драйвер на симуляторе | Уже настроен в `:common-db` (BundledSQLiteDriver); в Ф4 только verify |

---

## Связанные документы

- `KMP_VIEWMODEL_IOS_INTEROP.md` — референс interop-слоя (SKIE, MviViewModelWrapper, ViewModelProvider).
- `../features/ARCH_REFACTOR_PLAN.md` — параллельный трек декомпозиции на feature-модули (не смешивать).
- `PROGRESS.md` — трекер этого плана.
