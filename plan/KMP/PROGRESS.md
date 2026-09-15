# Прогресс: KMP + нативный SwiftUI на iOS

> Трекер перезапущен 15.09.2026 при смене курса на SwiftUI (план v2).
> Прогресс предыдущего плана (Compose-MP, фазы 0–7) удалён; история — в git.

## Сводка

| Фаза | Описание | Статус |
|------|----------|--------|
| **Ф1** | Расщепление `:shared` → `:shared` (ядро) + `:ui-compose` | ⬜ Не начато |
| **Ф2** | iOS-компиляция ядра (Dispatchers.IO, actual'ы, lifecycle) | ⬜ Не начато |
| **Ф3** | Interop-инфраструктура (SKIE, ViewModelProvider, initKoin) | ⬜ Не начато |
| **Ф4** | `iosApp`: Xcode-проект, каркас TabView | ⬜ Не начато |
| **Ф5** | Экраны SwiftUI (5.1 Workout → 5.5 Profile) | ⬜ Не начато |
| **Ф6** | QA и финализация | ⬜ Не начато |

## Контрольные точки

### Ф1

- [ ] `:ui-compose` создан (compose-setup), `:app` переключён
- [ ] Compose-экраны/ресурсы перенесены в `:ui-compose` (пакеты сохранены)
- [ ] `expect availableSettingsOptions` упразднён; `ImportExportNavigation.ios` удалён
- [ ] `:shared` → `multiplatform-library`, Compose-deps убраны
- [ ] `commonds.components.move` отсоединён от `WorkoutViewModel`
- [ ] `common-core` без Compose; чистая `createDateTimeFormatter()` expect/actual
- [ ] `:app:assembleDebug`, `:app:test`, `:app:lint` — зелёные
- [ ] `connectedAndroidTest` HappyPathTest — 3/3
- [ ] `:shared:compileKotlinMetadata`, `:common-core:compileKotlinMetadata` — зелёные

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
