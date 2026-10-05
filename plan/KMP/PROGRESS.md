# Прогресс: KMP + нативный SwiftUI на iOS

> Трекер перезапущен 15.09.2026 при смене курса на SwiftUI (план v2).
> Прогресс предыдущего плана (Compose-MP, фазы 0–7) удалён; история — в git.

## Сводка

| Фаза | Описание | Статус |
|------|----------|--------|
| **Ф1** | Расщепление `:shared` → `:shared` (ядро) + `:ui-compose` | ✅ Завершено |
| **Ф2** | iOS-компиляция ядра (Dispatchers.IO, actual'ы, lifecycle) | ⬜ Не начато |
| **Ф3** | Interop-инфраструктура (SKIE, ViewModelProvider, initKoin) | ⬜ Не начато |
| **Ф4** | `iosApp`: Xcode-проект, каркас TabView | ⬜ Не начато |
| **Ф5** | Экраны SwiftUI (5.1 Workout → 5.5 Profile) | ⬜ Не начато |
| **Ф6** | QA и финализация | ⬜ Не начато |

## Контрольные точки

### Ф1

- [x] `:ui-compose` создан (compose-setup), `:app` переключён
- [x] Compose-экраны/ресурсы перенесены в `:ui-compose` (пакеты сохранены)
- [x] `expect availableSettingsOptions` упразднён; `ImportExportNavigation.ios` удалён
- [x] `:shared` → `multiplatform-library`, Compose-deps убраны
- [x] `commonds.components.move` отсоединён от `WorkoutViewModel`
- [x] `common-core` без Compose; чистая `createDateTimeFormatter()` expect/actual
- [x] `:app:assembleDebug`, `:app:test`, `:app:lint` — зелёные
- [x] `connectedAndroidTest` HappyPathTest — 3/3
- [x] `:shared:compileKotlinMetadata`, `:common-core:compileKotlinMetadata` — зелёные

На эмуляторе Pixel_9_proxyman (API 35) все три сценария проверены: сценарии
создания упражнения и тренировки прошли в составе класса,
`scenario3_exerciseHistory` — отдельным запуском. Полный запуск класса дважды
таймаутился в `createWorkoutViaUi()` при ожидании экрана «История»; по принятому
решению это не блокирует фазу 1. iOS-компиляция ядра дошла до ожидаемого блокера
Ф2: 12 вызовов `withContext(Dispatchers.IO)` в репозиториях (6 в
`WorkoutRepositoryImpl`, 6 в `ExerciseRepositoryImpl`).

### Ф2

- [ ] `withContext(Dispatchers.IO)` убран из репозиториев
- [ ] `TextUtils.ios` реализован
- [ ] iOS `DateTimeFormatter` actual (NSDateFormatter)
- [ ] `:shared:compileKotlinIosSimulatorArm64` — зелёный
- [ ] `:shared:linkDebugFrameworkIosSimulatorArm64` — зелёный (lifecycle-конфликт закрыт)

### Ф3

- [ ] SKIE подключён к `:shared` (версия зафиксирована)
- [ ] `ViewModelProvider` + `initKoin()` в iosMain
- [ ] Smoke-check Swift-API фреймворка (Flow → AsyncSequence, sealed → плоские имена)

### Ф4

- [ ] Xcode-проект создан, исходники в git
- [ ] `sharedKit` подключён (build phase)
- [ ] `MviViewModelWrapper` добавлен
- [ ] Запуск на симуляторе: Koin стартует, БД создаётся, табы работают

### Ф5

- [ ] 5.1 Workout
- [ ] 5.2 History
- [ ] 5.3 ExerciseList + Selector + Editor
- [ ] 5.4 ExerciseHistory
- [ ] 5.5 Profile

### Ф6

- [ ] Полный Android-гейт (assembleDebug, test, lint, connectedAndroidTest, benchmark)
- [ ] iOS: clean build + ручной happy path
- [ ] `AGENTS.md`, `BOARD.md` обновлены
