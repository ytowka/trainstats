# Прогресс миграции на Kotlin Multiplatform

> Последнее обновление: 11 июля 2026

## Сводка

| Фаза | Описание | Статус |
|------|----------|--------|
| **0** | Инфраструктура и каталог | ✅ Завершено |
| **1** | Каркас `:shared` | ✅ Завершено |
| **2** | Room → KMP | ✅ Завершено |
| **3** | Domain + Data в `commonMain` | ✅ Завершено |
| 4 | Dagger → Koin 4.x | ⬜ Не начато |
| 5 | ViewModels | ⬜ Не начато |
| 6 | Compose-UI и навигация | ⬜ Не начато |
| 7 | Оболочка `:app` (slimming) | ⬜ Не начато |
| 8 | iOS-приложение (`:iosApp`) | ⬜ Не начато |

### Результаты сборки (на момент завершения Фаз 0-3)

- ✅ `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL** (114 задач)
- ✅ `./gradlew test` — **BUILD SUCCESSFUL** (142 задачи, все unit-тесты зелёные)

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

---

## Отклонения от плана

| # | Отклонение | Причина |
|---|-----------|---------|
| 1 | `jetbrains-navigation` версия `2.9.3` → `2.9.2` | Версия `2.9.3` не существует на Maven Central; `2.9.2` — последняя стабильная |
| 2 | Промежуточная сборка Фазы 2 не проверялась изолированно | DB-файлы в `:shared/commonMain` зависят от domain-моделей, переносимых в Фазе 3. Полная сборка прошла после Фазы 3 (ожидаемая взаимозависимость) |

---

## Нерешённые вопросы / технический долг (для последующих фаз)

| # | Описание | Фаза устранения |
|---|----------|----------------|
| 1 | `:app` сохраняет мёртвый Room/KSP-конфиг (`room {}`, `ksp(libs.room.compiler)`, `alias(libs.plugins.room/ksp)`) — now no-op, т.к. все entity перенесены в `:shared` | Фаза 7 |
| 2 | `app/di/DbModule.kt` дублирует создание драйвера (`Room.databaseBuilder`) вместо использования `DatabaseDriverFactory` | Фаза 4 (при удалении Dagger) |
| 3 | Dagger-аннотации (`@Inject`/`@Singleton`/`@Module`/`@Provides`/`@Binds`) сохранены на перенесённом коде; `javax-inject` добавлен как временная зависимость `:shared/commonMain` | Фаза 4 |
| 4 | `android.util.Log` в `ImportWorkoutsUseCase.kt` (androidMain) — работает на Android, замена на Napier | Фаза 5 |
| 5 | Пустые директории-оболочки в `:app` (git не трекает) | Косметика, можно очистить |
| 6 | Компиляция iOS-таргетов `:shared` не проверялась — `javax.inject:1` (JVM-only) не резолвится на Native до удаления Dagger-аннотаций | Фаза 4+8 |
| 7 | `io.insert-koin` vs `io.insert` — план указывает `io.insert`, фактически Maven group `io.insert-koin` (исправлено в libs.versions.toml) | — |

---

## Контрольные точки QA (Фазы 0-3)

Все проверки пройдены ✅:

- [x] `./gradlew :app:assembleDebug` — BUILD SUCCESSFUL
- [x] `./gradlew test` — BUILD SUCCESSFUL (forced clean rerun)
- [x] Нет дубликатов файлов между `:app` и `:shared`
- [x] `WorkoutDao` — 10 методов конвертированы в `suspend`, `Flow`-метод без изменений
- [x] `DatabaseDriverFactory` expect/actual — 3 файла созданы корректно
- [x] `TrainStatsDb` — `DB_NAME` добавлен, `@Database` без изменений
- [x] Схема Room скопирована в `shared/schemas/`
- [x] Инфраструктурные правки (удаление мёртвого кода, версионный hygiene)
- [x] Мёртвый импорт `ExerciseModel` удалён из `GetExercisesUseCase.kt`
- [x] Dagger-аннотации сохранены (не удалены преждевременно)

---

## Следующие шаги

Согласно рекомендуемому порядку (`MIGRATION_PLAN.md`, строки 159-166):

1. **Фаза 4 (Dagger → Koin)** — удалить все Dagger-аннотации, создать Koin-модули, убрать временную зависимость `javax-inject`, обновить `DbModule` для использования `DatabaseDriverFactory`
2. **Фаза 5 (ViewModels)** — перенести 7 VM в `:shared/commonMain`, заменить `ViewModelsProvider` на `koinViewModel<T>()`, заменить `android.util.Log` на Napier
3. **Фаза 6 (Compose-UI + навигация)** — перенести экраны, заменить AndroidX navigation на JetBrains KMP, создать `SharedApp()`
4. **Фаза 7 (slimming `:app`)** — удалить Dagger-инфраструктуру, Room/KSP-конфиг из `:app`, обновить `App.kt`/`MainActivity.kt`
5. **Фаза 8 (iOS)** — реализовать `:iosApp`, actuals для iOS, проверить компиляцию Native
