# План: KMP + нативный SwiftUI на iOS (v3)

> Обновлён 11.10.2026 по текущему коду. Курс прежний: Android — Compose,
> iOS — SwiftUI, общее KMP-ядро. Ф1 (разделение ядра и UI) уже выполнена;
> оставшиеся фазы учитывают статистику, просмотр тренировки и новые UI-сценарии.
> Это план работ, а не подтверждение успешной iOS-сборки текущего checkout.

## Решения и границы

- `:shared` содержит domain/data, общие MVI VM/State/Event/SideEffect и Koin DSL.
  `:common-core` не зависит от Compose. SwiftUI использует это ядро через `sharedKit`.
- Compose UI, Navigation 3, `koin-compose`, ресурсы и bottom sheet компоненты
  остаются в `:ui-compose` и его зависимостях. Эти модули пока имеют iOS targets,
  но не должны попадать в дерево зависимостей iOS-приложения.
- iOS навигация — SwiftUI `NavigationStack`, модальные сценарии — нативные sheet.
  Вкладки как на Android: **История / Упражнения / Профиль**. Редактор тренировки
  и статистика открываются из истории, просмотр тренировки — из статистики.
- SKIE + Swift-обёртка MVI остаются выбранным направлением interop. Версию и
  совместимость с Kotlin 2.3.21 проверяем при реализации; SKIE ещё не подключён.
- Import/export на iOS вне текущего объёма. Android-файловые службы/VM остаются
  в `:shared/androidMain`, UI — в `:ui-compose/androidMain`. Общий парсер остаётся
  в commonMain. На iOS не показываем неработающие пункты backup.
- RU-локализация в String Catalog; значения цветов/типографики переносим из
  `:common-ds` в SwiftUI. Источник строк —
  `sources/ui-compose/src/commonMain/composeResources/values/strings.xml`
  и ресурсы общих компонентов. Compose Resources не подключаем к SwiftUI.
- Декомпозиция по `ARCH_REFACTOR_PLAN.md` — отдельный, ещё не реализованный трек.
  При его реализации VM/state должны остаться доступны iOS без Compose.
  Старое предложение `ui = Compose + VM` и `:shared` как агрегатор всех UI-модулей
  необходимо согласовать с этим ограничением до переноса. Учесть также новую
  фичу `stats` и `workout/ui/details`, отсутствующие в старом списке модулей.
- Server sync/backend, hints прошлого подхода, вариации, позиция упражнения в
  истории, календарь с тренировками и заметки не входят в текущий iOS-паритет:
  в приложении эти фичи ещё не реализованы. Их спецификации в `plan/` — backlog.

## Текущее состояние

Модули под `sources/` подключаются через `includeSourceModule()`.

| Модуль | Фактическое содержимое |
|---|---|
| `:app` | Android shell, `App.kt` с Koin/Napier, `MainActivity → SharedApp`, тесты |
| `:shared` | Чистый KMP без Compose: exercises/workout/stats, 7 общих VM, `WorkoutSaver`, Koin; androidMain — backup |
| `:ui-compose` | Все Compose-экраны, RootScreen/nav3, графики, `WorkoutStopwatch`, UI helpers/resources; androidMain — backup UI |
| `:common-core` | Use case bases, lifecycle MVI, дата/текст/UUID; Compose-хелперы уже перенесены в UI |
| `:common-db` / `:common-db-api` | Room KMP, DAO/конвертеры/платформенные builders / entities/views; версия 1, tracked schemas в `sources/common-db/schemas/` |
| `:common-ds`, `:common-date-picker`, `:common:alertdialog`, `:common:bottomsheet` | Compose-компоненты; общий host вложенных sheets уже работает в Android UI |
| `:navigation:api` / `:navigation:impl` | Compose Navigation 3, включая `StatisticsNav` и `WorkoutDetailsNav` |
| `iosApp/` | Нет tracked Xcode-проекта и Swift-исходников |
| `backend/trainstats` | Отдельный Spring Boot build, вне объёма адаптации |

Конвенция `multiplatform-library` создаёт static frameworks для `iosArm64` и
`iosSimulatorArm64`; имя `:shared` — `sharedKit`. JVM и `iosX64` targets,
XCFramework aggregation, Swift API export зависимостей и SKIE сейчас не настроены.

Версии: Kotlin 2.3.21, AGP 8.9.3, Gradle 8.11.1, KSP 2.3.5, Room 2.8.4,
Koin 4.0.0, coroutines 1.10.1, Compose MP 1.10.0, lifecycle 2.9.2,
nav3 runtime/UI 1.1.7/1.1.1, JB lifecycle nav3 2.10.0, datetime 0.7.1,
sqlite-bundled 2.5.0. Источник версий — `gradle/libs.versions.toml`.

### Контракты, которые переиспользует iOS

- `MviViewModel<State, Event, SideEffect>` / `IMviViewModel`:
  **`startState`, `state`, `sideEffects`, `processEvent(event)`**.
  State — преимущественно data class, события/эффекты — sealed interface;
  `StatisticsViewModel` и `WorkoutDetailsViewModel` используют `Nothing` для эффектов.
- Koin — явный DSL (`factoryOf`, `singleOf`), без annotations/Koin KSP.
  `viewModelModule` регистрирует 7 VM: Workout, History, ExerciseList,
  ExerciseEditor, ExerciseHistory, Statistics, WorkoutDetails.
- Инициализация через события: `WorkoutEvent.RequestInit(editingId?)`,
  `ExerciseEditorEvent.Init(editingId?)`, `ExerciseHistoryEvent.Init(exerciseId)`,
  `WorkoutDetailsEvent.Load(workoutId)`. Не придумывать конструкторные параметры,
  которых нет в VM. Statistics/History/List запускают загрузку через сбор state.
- Getter `state` создаёт `stateIn(WhileSubscribed(5_000))`; Swift-адаптер должен
  сохранить один экземпляр flow. Повторные обращения/подписки и возобновление
  загрузки проверяются отдельно, включая возврат со страницы просмотра.
- `WorkoutSaver` — singleton с debounce 5 секунд. `WorkoutViewModel.onCleared()`
  вызывает commit; Swift `onDisappear` не эквивалентен завершению VM.

### Оставшиеся задачи iOS-ядра

| Задача | Текущее состояние |
|---|---|
| `Dispatchers.IO` | 12 `withContext` в двух commonMain репозиториях; прежний прогон зафиксировал Native opt-in блокер, нового прогона здесь нет |
| Числовой формат | `TextUtils.ios.format1/format2` — `TODO("ios")`; используется и просмотром/статистикой |
| Формат даты | `createDateTimeFormatter(localeTag, use24HourFormat)` на iOS возвращает ISO строки |
| Lifecycle линковка | Исторический klib-конфликт; после разделения модулей статус требует свежей проверки, исчезновение не предполагаем |
| Room файл | iOS builder использует bare `DB_NAME`; нужен устойчивый путь в writable sandbox и проверка перезапуска |
| Swift API | `:shared` использует `implementation` зависимостей, framework export не настроен; публичные supertype/model типы нужно проверить из Swift |
| Bootstrap/ownership | Есть `platformModule.ios`, но нет `initKoin`, VM provider, Swift wrapper и владельца lifecycle |

## Целевая схема

```text
Android :app -> :ui-compose -> :shared -> :common-core + :common-db -> :common-db-api
                    |
                    +-> Compose DS/components + :navigation:api/impl

iOS iosApp (SwiftUI) -> sharedKit (:shared + необходимые экспортируемые типы ядра)
```

В iOS-пути остаётся KMP `androidx.lifecycle.ViewModel`; условие — отсутствие
Compose и UI navigation, а не отсутствие lifecycle вообще. Выравнивание lifecycle
проверяем на реальном dependency tree и линковке.

## Ф1 — Разделение ядра и Compose UI: выполнено

Повторный перенос не нужен: `:shared`/`:common-core` уже используют
`multiplatform-library`, экраны и ресурсы находятся в `:ui-compose`.
`Res` package сознательно сохранён как `training_stats.shared.generated.resources`.
`expect importExportEntries()` и пустой iOS actual **сохранились в UI**;
`availableSettingsOptions` — обычный список Import/Export. Это не iOS backup.

Исторические Android-гейты Ф1 записаны в [PROGRESS.md](PROGRESS.md), включая
таймауты полного HappyPathTest. Они не заменяют проверки после следующих изменений.

## Ф2 — Компиляция и платформа ядра

1. Убрать ненужные IO wrappers вокруг Room suspend-вызовов в
   `WorkoutRepositoryImpl` и `ExerciseRepositoryImpl`; подтвердить, что нет
   блокирующих платформенных операций, требующих собственного dispatcher.
   Android-only FileReader/FileWriter/export при этом не переносить.
2. Реализовать `format1/format2` на Native с паритетом Android, проверить дробные
   значения и форматирование без лишних нулей; исключить runtime TODO.
3. Реализовать actual `createDateTimeFormatter(localeTag, use24HourFormat)`:
   учитывать locale, текущий год, 12/24 часа и локальный timezone. Android-эталон:
   `EEEE, d MMMM[ yyyy]`, время `HH:mm` либо `h:mm a`, разделитель ` • `.
4. Для Room задать путь файла в каталоге приложения, проверить создание пустой
   БД и сохранность после перезапуска. `BundledSQLiteDriver` уже подключён;
   prebuilt Android asset не является источником iOS seed.
   Схему/версию менять только при реальном изменении entities/views с миграцией.
5. Проверить compile/link и dependency tree. Если lifecycle-конфликт остаётся,
   исправить точную несовместимость версий; не обновлять весь nav3/UI стек вслепую.

Гейты: `:common-core:compileKotlinIosSimulatorArm64`,
`:shared:compileKotlinIosSimulatorArm64`, `:shared:linkDebugFrameworkIosSimulatorArm64`,
`:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug`.
Сохранность БД дополнительно проверяется в приложении в Ф4.

## Ф3 — Swift API, DI и lifecycle

1. Подключить совместимую SKIE-версию через каталог к `:shared`; проверить Flow,
   sealed events и фактический Swift API. Не подключать несколько Flow-мостов.
2. Настроить `api`/framework `export` для необходимых публичных типов (MVI
   supertype из common-core, domain/state модели и их зависимости). `api()` само
   по себе не экспортирует классы в Swift. Не экспортировать Compose или весь
   граф автоматически; валидировать небольшим Swift compile-smoke.
3. В iosMain добавить `initKoin()` и Napier bootstrap с общими модулями и
   `platformModule`. `androidSharedModule` не подключать. Provider должен покрыть
   все 7 VM, включая **Statistics** и **WorkoutDetails**.
4. Ввести Kotlin владельца VM/`ViewModelStore` либо эквивалентный lifecycle handle
   с явным освобождением. VM создаётся на экран/модальную сессию, а не каждый
   SwiftUI render; временное перекрытие экраном не очищает её. После окончательного
   pop/dismiss отменяются подписки и очищается VM; редактор завершает commit.
5. Предусмотреть flush редактора при уходе приложения в background, не полагаясь
   только на `onCleared`. При необходимости добавить общий явный save/flush API,
   сохраняющий Android-поведение. Проверить быстрый уход до 5 секунд debounce.
6. Swift wrapper: `@MainActor`, начальное значение из `startState`, стабильный
   flow, сбор `state`/`sideEffects`, отправка `processEvent`. Для VM с `Nothing`
   не требовать несуществующий поток эффектов. Подписки отменяются и возобновляются
   с экраном; события selector/editor и открытие selector новой тренировки не теряются.

[KMP_VIEWMODEL_IOS_INTEROP.md](KMP_VIEWMODEL_IOS_INTEROP.md) — концептуальный
референс. Его примеры `accept`, `initialState`, `sideEffect`, порядок generic
параметров, `@Factory`, `import shared` **не совпадают с этим репозиторием**;
адаптировать к текущему контракту, DSL и `import sharedKit`, не копировать буквально.

Гейты: simulator framework link + Swift compile-smoke для каждой VM, включая
Statistics/Details; создание/закрытие VM без утечек, повторной загрузки на каждый
render и потери финальных правок; Android unit/build regression.

## Ф4 — Xcode-каркас

1. Создать tracked `iosApp` (SwiftUI lifecycle, baseline iOS 16+), хранить исходники
   и project configuration в git, исключить build/DerivedData/user-state.
2. Настроить Xcode/Gradle integration с выбором simulator/device и Debug/Release.
   Использовать подходящую integration task после проверки доступных Gradle задач;
   не привязывать build phase только к `linkDebugFrameworkIosSimulatorArm64`.
   Framework static: не настраивать embed как для dynamic framework.
3. До создания экранов вызвать bootstrap; добавить wrapper/VM ownership из Ф3,
   `TabView` История / Упражнения / Профиль и маршруты new/edit workout,
   statistics, read-only workout details. Тип маршрута и workoutId различать явно.
4. Добавить RU String Catalog, тему/цвета, типографику, safe-area/keyboard поведение,
   нативный DatePicker. Если графики требуют API новее iOS 16, отдельно согласовать
   baseline или использовать доступный renderer; не повышать deployment target молча.
5. Smoke: startup → Koin → Room → вкладки; запись/перезапуск/чтение БД;
   сборка симулятора и device framework.

## Ф5 — SwiftUI-экраны и паритет

Порядок учитывает зависимости: selector/editor → workout → history → exercise
history → details → statistics. Profile завершает оболочку. Каждый шаг проверяется
на симуляторе и Android build; UI не переносит бизнес-расчёты из Kotlin в Swift.

| Шаг | Содержимое и критерий |
|---|---|
| 5.1 Exercises + Selector + Editor | Список, поиск, создание/правка/удаление; hasWeight/separated. Вложенный selector → editor, результат/refresh после сохранения и dismiss. Состояние selector не теряется при открытии editor |
| 5.2 Workout editor | New/edit по id, автоматический selector новой тренировки, дата, группы/подходы, ввод дробных весов/повторов и двух сторон, reorder, collapse, delete/undo, save/autosave. Таймер от lastEdited только для текущего дня; lifecycle/background flush из Ф3 |
| 5.3 History | Список/поиск, создание новой тренировки и открытие существующей в редакторе, кнопка статистики. Не добавлять пустой календарь и неподдерживаемые действия только ради визуального совпадения |
| 5.4 ExerciseHistory | Нативная sheet из редактора, дата и подходы упражнения, корректное состояние при смене упражнения |
| 5.5 WorkoutDetails | `WorkoutDetailsViewModel`, `Load(id)`, loading/error/retry; весь workout, группы по orderPosition, вес/две стороны повторов. Без editor/save/delete и записи данных |
| 5.6 Statistics: показатели | Exercise search, Weight/Repetitions/Volume, default maximum series, отдельные подходы, рекорд, viewport/жесты/выбор точки и переход в details; loading/empty/error/retry |
| 5.7 Statistics: частота | Вкладка Frequency; Week/Month/Quarter/Year, Exercise/AllWorkouts, Workouts/Sets, нулевые интервалы, выбор периода, totals за период/всё время, прокрутка к актуальным столбцам |
| 5.8 Profile | Текущий профиль-заглушка; скрытые backup-настройки, без выдуманных account/sync возможностей |

### Правила статистики для обеих платформ

- Источник — `GetStatisticsUseCase`, `StatisticsState`, `ExerciseStatistics.kt`,
  `FrequencyStatistics.kt`, `StatisticsChartRange.kt`. Android Canvas/gesture code
  из `StatisticsChart*.kt` и `FrequencyStatisticsContent.kt` служит UI-эталоном,
  но не экспортируется в iOS; выбрать нативный renderer и обеспечить поведение.
- Неархивные тренировки; `saved=false` не исключает запись. Порядок подходов
  локален выбранному упражнению, включая повторные группы. Нули повторов пропускаем
  без сдвига номеров; отсутствие веса отличается от нулевого веса.
- Раздельные повторы: среднее для Repetitions, сумма сторон для Volume.
  Карточка точки/рекорда показывает полный реальный подход с обеими сторонами.
- Максимум по каждой тренировке — series 0, включён по умолчанию. Рекорд за всю
  историю независим от viewport/видимых линий, tie-break берём из общего ядра.
- Начальный viewport — последние 6 календарных месяцев истории упражнения;
  zoom/pan/full-history reset, выбор совпадающих точек и вертикальный scroll
  страницы. Скрытые серии не участвуют в выборе точек.
- Частота — календарные интервалы (неделя с понедельника), в текущем timezone;
  считаются подходы с конечной положительной суммой повторов. Пустые интервалы
  сохраняются. Выбор упражнения/метрики/точки сохраняется при смене вкладки
  статистики и после возврата из details.

## Ф6 — QA и завершение

- Android: assembleDebug, unit tests, lintDebug; device suites HappyPathTest,
  StatisticsScreenTest, FrequencyChartPositionTest, BottomSheetHostTest,
  Room/backup regression; `:benchmark:assembleBenchmark`.
- iOS clean simulator build и device/Release framework; smoke на устройстве при
  наличии. Happy path: exercise → workout с двумя сторонами/без веса → сохранение
  → история → exercise history → metrics → точка/рекорд → details → back → frequency.
- Проверить nested sheet dismissal/клавиатуру/отмену, быстрый back/background до
  debounce, перезапуск и сохранность БД, отсутствие записей из details,
  отсутствие повторных VM/subscription после возврата.
- Расчёты сравнить на одинаковых fixtures: несколько групп одного упражнения,
  одинаковые даты, архив/`saved=false`, пустые подходы, нулевой/отсутствующий вес,
  равные рекорды, нулевые интервалы, границы года/квартала/недели/timezone.
  Эталон — Kotest `StatisticsTest`/`FrequencyStatisticsTest` и
  [STATS_CASE.md](../QA/STATS_CASE.md); renderer проверять SwiftUI UI/snapshot
  тестами или воспроизводимым ручным сценарием, зафиксировать способ и результат.
- Обновлять `PROGRESS.md` фактическими результатами, а `AGENTS.md` — при изменении
  архитектуры. Не отмечать фазы завершёнными только по наличию файлов.

## Риски и контроль

| Риск | Проверка/решение |
|---|---|
| Lifecycle/Native dependency conflict | Свежий dependency tree и compile/link в Ф2, Compose не входит в shared tree |
| SKIE/Swift generics, Nothing, неполный экспорт | Проверка совместимой версии и Swift compile-smoke 7 VM в Ф3 |
| Потеря autosave или утечки VM | Явное ownership/clear и background flush; проверка до/после debounce |
| Отличающиеся расчёты/даты статистики | Общие Kotlin расчёты, одинаковые fixtures, timezone и calendar QA |
| Вложенные sheet теряют результат/selector state | Владение модальной сессией, обработка dismiss/cancel и refresh |
| ARCH_REFACTOR возвращает Compose в iOS | Согласовать границы shared presentation и UI до декомпозиции |
| Xcode integration работает только на debug simulator | Проверка platform/configuration-aware сборки и static linking |

## Связанные документы

- [PROGRESS.md](PROGRESS.md) — текущий статус фаз и проверок.
- [KMP_VIEWMODEL_IOS_INTEROP.md](KMP_VIEWMODEL_IOS_INTEROP.md) — общий interop референс с адаптацией контракта по Ф3.
- [ARCH_REFACTOR_PLAN.md](../features/ARCH_REFACTOR_PLAN.md) — отдельная будущая декомпозиция.
- [BOTTOM_SHEET_REFACTOR.md](../BOTTOM_SHEET_REFACTOR.md) — исходное предложение, не описание текущего host.
- [STATS_CASE.md](../QA/STATS_CASE.md) — Android поведение показателей и крайние случаи.
