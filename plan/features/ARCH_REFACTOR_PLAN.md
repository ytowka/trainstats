# ARCH_REFACTOR_PLAN — рефакторинг `:shared` в многомодульную архитектуру

> Детальный план по заданию из `ARCH_REFACTOR.md`. К реализации не приступал.

## 0. Исходный концепт (из ARCH_REFACTOR.md)

- Многомодульная архитектура задается папками: `sources/features` → основные фичи,
  `sources/app/navigation` → навигация, которая «знает» про все ui-модули и как их открыть.
- Цикл workout ↔ exercises: завязка на workout в модуле exercise есть только в истории.
  Историю упражнений переносим в workout, чтобы в exercise не было зависимостей на workout.
- Граф зависимостей workout: `workouts:domain-api -> exercises:domain-api (через api())`.
- Общий концепт зависимостей модулей:
  - `ui -> domain-api`
  - `domain -> domain-api`
  - `data -> domain-api`
  - `data -> data-api`
- Слои:
  - `ui` — слой с ui и viewmodels
  - `domain-api` — модельки, интерфейсы юзкейсов, интерфейсы репозиториев
  - `domain` — реализации интерфейсов из domain-api
  - `data-api` — интерфейсы слоя данных
  - `data` — слой с реализацией репозиториев и слоя данных
- Модуль `common-db-api` удаляем, сущности раскидываем по соответствующим data-api модулям,
  эти data-api потом подключаем в `common-db`. DAO так же переносим в data-api.
  В `common-db` остается только `TrainStatsDb.kt` и платформо-специфический код.
- settings/import/export — это фича `backup`.
- `profile` — пока состоит только из ui-модуля.
- `:shared` модуль станет агрегатором всех фич.

### Дополнительные решения (зафиксированы при обсуждении плана)

1. **Открытие фич друг другом — только через навигацию.** WorkoutScreen не подключает
   exercises:ui напрямую: selector/editor становятся экранами с роутами, результат
   возвращается через `savedStateHandle` (glue-код в `:app:navigation`).
2. **Пакеты при переносе не меняем** (кроме `features.settings` → `features.backup` и
   истории упражнений) — перенос между модулями с сохранением package/import.
3. **domain-api и domain — чисто Kotlin-модули**: без Compose, без Android-зависимостей.
   Только Kotlin/KMP-код: модели, интерфейсы, реализации. Зависимости ограничены
   `common-core`, `coroutines`, `kotlinx-datetime` (+ `koin-core` в domain для объявления
   Koin-модулей; koin-core — чистый Kotlin). Для них в `build-logic` добавляется легковесный
   convention plugin `kotlin-library`: `kotlin("multiplatform")` (+ android target для
   потребления из `:app`) **без** Compose и **без** сборки `.xcframework`-бинарников.
   `data-api` тоже не содержит UI-кода (только room-runtime KMP-аннотации) — используется
   тот же `kotlin-library`.

## 1. Целевая архитектура

```
:app ──► :shared (агрегатор всех фич + SharedApp + Koin-склейка)
              │
              ├─► :app:navigation ──► home:ui, workouts:ui, exercises:ui, profile:ui, backup:ui
              ├─► :features:workouts:{ui, domain, domain-api, data, data-api}
              ├─► :features:exercises:{ui, domain, domain-api, data, data-api}
              ├─► :features:backup:{ui, domain, domain-api, data, data-api}
              ├─► :features:home:ui,  :features:profile:ui
              └─► :common-db ──api──► workouts:data-api + exercises:data-api

workouts:domain-api ──api()──► exercises:domain-api   (единственная межфичевая связь слоев)
```

Правила зависимостей: `ui -> domain-api`, `domain -> domain-api`, `data -> domain-api + data-api`.
Открытие чужих экранов — только через `:app:navigation`.

## 2. Новые Gradle-модули (18 шт., скелеты папок уже существуют)

| Модуль | Плагин | namespace (выводится из пути) | package Res-класса |
|---|---|---|---|
| `:features:exercises:domain-api` | `kotlin-library` (новый) | `exercisesdomainapi` | — |
| `:features:exercises:domain` | `kotlin-library` | `exercisesdomain` | — |
| `:features:exercises:data-api` | `kotlin-library` | `exercisesdataapi` | — |
| `:features:exercises:data` | `multiplatform-library` | `exercisesdata` | — |
| `:features:exercises:ui` | `compose-setup` | `exercisesui` | `training_stats.features.exercises.ui` |
| `:features:workouts:domain-api` | `kotlin-library` | `workoutsdomainapi` | — |
| `:features:workouts:domain` | `kotlin-library` | `workoutsdomain` | — |
| `:features:workouts:data-api` | `kotlin-library` | `workoutsdataapi` | — |
| `:features:workouts:data` | `multiplatform-library` | `workoutsdata` | — |
| `:features:workouts:ui` | `compose-setup` | `workoutsui` | `training_stats.features.workouts.ui` |
| `:features:backup:domain-api` | `kotlin-library` | `backupdomainapi` | — |
| `:features:backup:domain` | `kotlin-library` | `backupdomain` | — |
| `:features:backup:data-api` | `kotlin-library` | `backupdataapi` | — |
| `:features:backup:data` | `multiplatform-library` | `backupdata` | — |
| `:features:backup:ui` | `compose-setup` | `backupui` | `training_stats.features.backup.ui` |
| `:features:home:ui` | `compose-setup` | `homeui` | `training_stats.features.home.ui` |
| `:features:profile:ui` | `compose-setup` | `profileui` | `training_stats.features.profile.ui` |
| `:app:navigation` | `compose-setup` | `navigation` | — |

**Важно:**
- **Имя модуля выводится из пути проекта** (правило в convention plugin, см. ниже) —
  явная per-module конфигурация `moduleSetup { name = ... }` не нужна. Правило обратно
  совместимо со всеми существующими модулями (в т.ч. `:common:alertdialog` → `alertdialog`).
- Package Res-класса тоже выводится из пути: `:common:alertdialog` уже генерирует
  `training_stats.common.alertdialog` → ui-модули получат уникальные
  `training_stats.features.<feature>.ui` автоматически. `packageOfResClass` задаем явно
  только если генерация окажется не path-based (проверить на первом ui-модуле).
- `:app:navigation` включается через `includeSourceModule(":app:navigation")` —
  projectDir `sources/app/navigation`; Gradle допустимо делает его подпроектом `:app`.
- Пустые скелетные папки home (data, data-api, domain, domain-api) в settings.gradle
  не включаются — зарезервированы на будущее.

### Вывод имени из пути (правило для convention plugins)

`project.name` в Gradle — только последний сегмент пути (`:features:workouts:ui` → `"ui"`),
существующая логика `multiplatform-library` (`project.name.replace("-", "")`) дала бы
одинаковые namespace (`com.danilkha.ui`) и baseName фреймворков (`uiKit`) у всех
ui-модулей. Новое правило — брать все сегменты пути, кроме первого (контейнер), а для
одноуровневых модулей — единственный сегмент:

```kotlin
val segments = project.path.split(':').filter { it.isNotBlank() }
val moduleName = (if (segments.size > 1) segments.drop(1) else segments)
    .joinToString("")
    .replace("-", "")
```

Проверка обратной совместимости (все существующие имена не меняются):
`:shared` → `shared`, `:common-db-api` → `commondbapi`, `:common:alertdialog` →
`alertdialog` (сейчас `project.name` == `alertdialog` — совпадает).

Примеры новых: `:features:workouts:ui` → `workoutsui` (namespace
`com.danilkha.workoutsui`, фреймворк `workoutsuiKit`), `:features:exercises:domain-api`
→ `exercisesdomainapi`, `:app:navigation` → `navigation`.

### Новый convention plugin `kotlin-library` (build-logic)

```kotlin
// build-logic/src/main/kotlin/kotlin-library.gradle.kts
plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library") // только для androidTarget, без resources
}
// androidLibrary { namespace = "com.danilkha.<name>"; compileSdk 36; minSdk 26 }
// targets: androidTarget + iosX64/iosArm64/iosSimulatorArm64 БЕЗ binaries.framework
```
Никаких Compose-зависимостей, никаких `.xcframework` (код попадает в iOS-приложение
транзитивно через фреймворк `:shared`). Вывод `<name>` — общее правило из пути
(см. выше), общая логика выносится в общий код/функцию build-logic и применяется
в обоих plugins (`multiplatform-library` обновляется, `kotlin-library` использует её же).

## 3. Перенос кода (пакеты НЕ меняем, кроме двух случаев)

### 3.1. common-db-api → data-api (пакеты `…db.entity` / `…db` сохраняем — ноль правок импортов)

- `exercises:data-api` ← `ExerciseEntity`, `ExerciseCountView`, `ExerciseLastUsedView`,
  `ExerciseWithLastUsed`, `ExerciseDao` (из common-db)
- `workouts:data-api` ← `WorkoutEntity`, `ExerciseSetEntity`, `ExerciseWorkoutRelation`,
  `WorkoutWithExercises`, `ExerciseSetWithData`, `RepetitionsDb` (+TypeConverter),
  `WorkoutDao`, `DateTimeConverter`, `StringListConverter` (по использованию)
- `common-db` остается: `TrainStatsDb.kt`, `DatabaseDriverFactory` (+ платформенные
  actual'ы, `schemas/`). Подключает оба data-api через `api()`.
- Схема БД не меняется (те же классы, те же пакеты → v1, `1.json` валиден).
- `:common-db-api` удаляется из settings.gradle и с диска.

### 3.2. exercises

- `domain/model/ExerciseData` → domain-api
- `ExerciseRepository` → domain-api
- 5 юзкейсов (`GetAll/Get/Create/Update/Delete Exercises`) → интерфейсы в domain-api
  (расширяют `UseCase`/`FlowUseCase` из common-core) + `*Impl` в domain
- `ExerciseLocalDatasource`, `RoomExerciseDatasource`, `ExerciseRepositoryImpl`,
  `EntityMappers`, `FakeExerciseRepository` → data
- `ExerciseListScreenPage` + список/selector/editor → ui (selector/editor — см. п. 4)

### 3.3. workouts

- Модели (`Workout`, `ExerciseSet`, `ExerciseWorkout`, `Kg`, `SetParams`, `WorkoutParams`,
  `WorkoutPreview`) + `WorkoutRepository` → domain-api
- Юзкейсы (iface/impl), включая `GetExerciseHistoryUseCase` → domain-api/domain
- `WorkoutLocalDatasource`, `RoomWorkoutDatasource`, `WorkoutRepositoryImpl`,
  `EntityMappers`, `FakeWorkoutRepository` → data. Маппер `toDomain` из
  exercises `EntityMappers` дублируется локально (разрывает data→data).
- Весь ui (editor, history, components) + история упражнений (см. п. 4.1) → ui

### 3.4. backup (было settings; пакет rename `features.settings` → `features.backup`)

- `WorkoutParser` (+ модели результата) → domain-api
- `WorkoutParserImpl` → domain
- `FileReader`/`FileWriter` интерфейсы → data-api; android-реализации → data
- `ImportWorkoutsUseCase`, `ExportWorkoutsUseCase` → domain (androidMain — используют
  `android.net.Uri`, как сейчас)
- `SettingsScreen`, Import/Export экраны и VM → ui (androidMain как сейчас);
  expect/actual `availableSettingsOptions` → backup:ui
- `ImportExportNavigation` expect/actual расформировывается в `:app:navigation`
  (роуты регистрируются там, список опций остается expect/actual в backup:ui)

### 3.5. home / profile / navigation

- `home:ui` ← `HomeScreen`, `NavBar` (+ строки navigation_item_*)
- `profile:ui` ← `ProfileScreen`
- `:app:navigation` ← `Navigation.kt` (все route-константы, включая новые для
  selector/editor/history), `RootScreen.kt`

## 4. Разрыв цикла workout ↔ exercises + «открытие через навигацию»

1. **История упражнений → workouts:** `exercises/ui/history/*` (BottomSheet, State,
   ViewModel, ExerciseSetHistoryModel) переезжают в `workouts:ui`, пакет
   `features.workout.ui.exercisehistory`. `GetExerciseHistoryUseCase` уже в
   workout.domain — цикл закрыт. Внутри workouts:ui остается bottom sheet (модуль свой).
2. **`ExerciseModel` → `ExerciseData`:** модели идентичны; `WorkoutState`/`WorkoutViewModel`
   переключаются на `ExerciseData` из exercises:domain-api (виден транзитивно через
   `api()` из workouts:domain-api).
3. **Selector/Editor становятся экранами:** `ExerciseSelectorBottomSheet` /
   `ExerciseEditorBottomSheet` → полноценные экраны в exercises:ui. Selector владеет
   своим `ExerciseListViewModel` (включая «создать упражнение» → editor, результат
   обновляет список). RootScreen регистрирует роуты и прокидывает колбэки; результат
   выбора возвращается в WorkoutScreen через `savedStateHandle` предыдущего
   backStackEntry (передаются примитивы/saveable-значения, `ExerciseData` восстанавливается
   в glue-слое). После этого workouts:ui **не зависит** от exercises:ui.

## 5. DI (Koin)

- Каждый модуль несет свой Koin-модуль: `exercisesDomainModule`, `exercisesDataModule`,
  `exercisesUiModule`, `workouts*`, `backup*` (android-части backup — в androidMain).
- `:shared` остается:
  - `platformModule` (expect/actual: `DatabaseDriverFactory`, `contentResolver`)
  - `appDbModule` (создание `TrainStatsDb`, предоставление `ExerciseDao`/`WorkoutDao`)
  - агрегатор `val trainStatsModules = listOf(…)`
- `App.kt` → `startKoin { modules(trainStatsModules) }`.

## 6. Ресурсы

`strings.xml` (`:shared`) расщепляется по ui-модулям:

- `home:ui`: navigation_item_*
- `workouts:ui`: new_workout, edit_workout, delete_workout_title, delete_exercise_title,
  delete_exercise_subtitle, kg, reps, add_exercise, history, exercise_history, weight,
  reps_label, reps_left, reps_right, split, separated, expand, collapse, undo, drag_handle,
  clear, scroll_to_top, chart, calendar, has_weight, with_body_weight
- `exercises:ui`: new_exercise, edit_exercise, name, save, update, delete, cancel,
  search, empty_search, …
- `backup:ui`: import_workout, export_workout, to_import, from_file, to_export, settings,
  total_entries, success_export, last_edited
- Общие строки (`save`, `cancel`, `delete`, …) дублируются в нуждающиеся модули.
- Импорты `Res.*` правятся в каждом экране (у каждого модуля свой generated Res).

## 7. Порядок работ (сборка зеленая после каждой фазы)

1. **build-logic:** обновить вывод имени из `project.path` в `multiplatform-library`
   (проверить: все существующие namespace'ы/фреймворки не меняются) и добавить
   `kotlin-library` convention plugin.
2. **DB-слой:** создать оба data-api, перенести сущности+DAO (пакеты те же),
   common-db → `api()` на них, удалить `:common-db-api`. Проверка: `:app:assembleDebug`.
3. **История в shared:** перенос exercises/ui/history → workout/ui/exercisehistory
   внутри :shared, правка импортов WorkoutScreen.
4. **exercises (5 модулей):** перенос, iface/impl юзкейсов, ресурсы, Koin. `:shared`
   подключает.
5. **workouts (5 модулей):** аналогично.
6. **backup (5 модулей):** перенос + rename пакета + обновить `ExportImportTest`.
7. **home:ui, profile:ui.**
8. **:app:navigation:** перенос Navigation/RootScreen + рефакторинг selector/editor на
   роуты и savedStateHandle-результаты; удаление ui→ui зависимости workouts→exercises.
   Самая содержательная фаза.
9. **:shared → агрегатор:** `api()` на все модули, упрощение DI, App.kt.
10. **Финал:** `Fake*Repository` удалить или перенести в data; финальный settings.gradle.kts;
    переписать AGENTS.md (в файле отмечено, что он не актуален); проверки:
    `./gradlew :app:assembleDebug test lint`, `:shared:linkDebugFrameworkIosSimulatorArm64`,
    при устройстве `connectedAndroidTest`.

## 8. Риски и заметки

- Каждый модуль на `multiplatform-library`/`compose-setup` собирает свой `.xcframework` —
  iOS собирается дольше; чисто-Kotlin модули (`kotlin-library`) фреймворков не создают.
  iosApp в git не входит, Xcode продолжает линковать только `sharedKit` (static,
  транзитивно).
- `savedStateHandle`-результаты: `ExerciseData` не saveable — передавать набор примитивов,
  объект восстанавливать в glue-слое `:app:navigation`.
- `androidTest` в `:app`: тесты БД ссылаются на пакеты db (не меняются), `ExportImportTest`
  завязан на settings (правится в фазе backup).
- Вывод имени из `project.path` меняет behavior convention plugin: после правки
  `multiplatform-library` убедиться, что у существующих модулей namespace и baseName
  фреймворков не изменились (обратная совместимость проверена в п. 2, но прогнать
  `:app:assembleDebug` и линковку iOS до переноса кода). Package Res-класса — проверить
  на первом переносимом ui-модуле, что генерация path-based (как у `common:alertdialog`);
  если нет — задать `packageOfResClass` явно.
