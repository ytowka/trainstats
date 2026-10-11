# Прогресс: KMP + нативный SwiftUI на iOS

> Обновлён 11.10.2026 под [план v3](KMP_MIGRATION_PLAN.md).
> Статусы сверены с кодом; новых build/test прогонов при обновлении документов нет.
> Исторические результаты Ф1 сохранены ниже и не подтверждают текущий checkout.

## Сводка

| Фаза | Описание | Статус |
|---|---|---|
| **Ф1** | Разделение `:shared` (ядро) / `:ui-compose` (UI), common-core без Compose | ✅ Реализовано; исторические проверки ниже |
| **Ф2** | Native compile/link, числовой/датный формат, persistent Room path | ⬜ Не завершено; IO wrappers и actual-заглушки остаются |
| **Ф3** | SKIE, Swift exports, 7 VM provider, bootstrap, ownership/flush | ⬜ Не начато |
| **Ф4** | Xcode-проект, integration, вкладки История / Упражнения / Профиль | ⬜ Не начато |
| **Ф5** | SwiftUI-экраны, включая Details и оба таба Statistics | ⬜ Не начато |
| **Ф6** | QA Android/iOS, сохранность данных и статистический паритет | ⬜ Не начато |

## Контрольные точки

### Ф1 — фактическое состояние

- [x] `:ui-compose` создан на `compose-setup`, `:app` подключает его и `:shared`.
- [x] Compose-экраны/ресурсы перенесены; package Res сохранён как
  `training_stats.shared.generated.resources`.
- [x] `:shared` и `:common-core` без Compose, на `multiplatform-library`.
- [x] Compose VM helpers и LocalDateFormat находятся в `:ui-compose`;
  в common-core есть `createDateTimeFormatter(localeTag, use24HourFormat)`.
- [x] `availableSettingsOptions` — обычный список Import/Export.
- [x] Platform `expect importExportEntries()` и пустой iOS actual остаются в
  `:ui-compose`; прежняя запись об удалении actual была неточной.

Историческая запись от 15.09.2026: `:app:assembleDebug`, `:app:test`, `:app:lint`,
`:shared:compileKotlinMetadata`, `:common-core:compileKotlinMetadata` проходили.
На Pixel_9_proxyman (API 35) три happy-path сценария проходили: первые два в
составе класса, `scenario3_exerciseHistory` — отдельно. Полный класс дважды
таймаутился в `createWorkoutViaUi()` при ожидании «Истории»; тогда принято решение
не блокировать Ф1. Native compile дошёл до 12 `Dispatchers.IO` wrappers в
репозиториях (6 Workout + 6 Exercise). Эти wrappers всё ещё есть в коде.

### Изменения после исходного плана

Уже реализованы в общем ядре и Android UI, **не в SwiftUI**:

- [x] Statistics: метрики Weight/Repetitions/Volume, максимум по тренировке,
  отдельные подходы, all-time рекорд, выбор точки, viewport/жесты.
- [x] Frequency: календарные Week/Month/Quarter/Year, Exercise/AllWorkouts,
  Workouts/Sets, выбранный период и all-time totals.
- [x] `WorkoutDetailsViewModel` и read-only экран, переход из точки/рекорда.
- [x] Корневой `BottomSheetHost` с вложенными sheets и модальным input/back.
- [x] `WorkoutStopwatch` от `lastEdited`, открытие selector новой тренировки,
  refresh списка/selector через события результата и dismiss.
- [x] Unit/UI coverage статистики/частоты и host tests bottom sheet добавлены.

Будущие hints, variations, позиция в истории, calendar drill-down и notes не
считаются выполненными. Feature-module декомпозиция также не реализована.

### Ф2

- [ ] Убрать IO wrappers из commonMain репозиториев с проверкой поведения Room.
- [ ] Реализовать `TextUtils.ios.format1/format2`.
- [ ] Реализовать iOS formatter с locale, текущим годом и 12/24 часами.
- [ ] Настроить устойчивый writable Room path; проверить сохранность в Ф4.
- [ ] Native compile ядра и simulator framework link; перепроверить lifecycle tree.
- [ ] Android build/unit/lint regression после изменений.

### Ф3

- [ ] Зафиксировать совместимую SKIE-версию в каталоге и подключить к `:shared`.
- [ ] Настроить необходимые `api`/framework exports без Compose.
- [ ] Добавить `initKoin()`/Napier и provider всех 7 commonMain VM.
- [ ] Создать явный VM lifecycle owner; проверить clear/отмену подписок.
- [ ] Добавить/проверить flush редактора при back/background до debounce.
- [ ] Swift wrapper по `startState` / `state` / `sideEffects` / `processEvent`,
  стабильный flow и поддержка VM без эффектов (`Nothing`).
- [ ] Swift compile-smoke всех VM, включая Statistics/WorkoutDetails.

### Ф4

- [ ] Xcode-проект/Swift исходники в git, platform/configuration-aware integration.
- [ ] Static `sharedKit` линкуется для simulator/device, Debug/Release.
- [ ] Bootstrap, wrapper/ownership, правильные вкладки и отдельные editor/details routes.
- [ ] RU String Catalog и SwiftUI дизайн-система.
- [ ] Simulator startup и DB persistence после перезапуска.

### Ф5

- [ ] 5.1 ExerciseList + Selector + Editor, nested sheet/result/refresh.
- [ ] 5.2 Workout editor: reorder/undo, autosave/flush, stopwatch.
- [ ] 5.3 History: поиск, new/edit workout, статистика.
- [ ] 5.4 ExerciseHistory.
- [ ] 5.5 WorkoutDetails: read-only, loading/error/retry.
- [ ] 5.6 Statistics: показатели, viewport/selection, рекорд → details → back.
- [ ] 5.7 Statistics: частота, calendar intervals/totals/scope и scroll position.
- [ ] 5.8 Profile без неподдерживаемых backup/account функций.

### Ф6

- [ ] Android build/unit/lint, device regression и benchmark build.
- [ ] iOS clean build, device/Release framework и полный happy path.
- [ ] Сохранность данных при быстром back/background/перезапуске; VM ownership QA.
- [ ] Одинаковые статистические fixtures/расчёты и UI паритет обоих табов.
- [x] `AGENTS.md`, план и трекер обновлены по коду 11.10.2026.
- [ ] Документы/`BOARD.md` отражают окончательный результат после реализации iOS.
