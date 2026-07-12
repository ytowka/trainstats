# QA Happy-Path Test Case — Training-stats (post Koin 4.x migration)

Self-contained functional smoke test. Run it to confirm the app works end-to-end
(Koin DI → repositories → use cases → Room) after the Dagger → Koin migration.

The app UI is **localized in Russian**. This document is in English, with Russian UI
strings quoted verbatim so you can match them against `list_elements_on_screen` output.

> **Primary interaction method:** drive every tap from element **text/labels** returned
> by `list_elements_on_screen`. All interactive icon controls carry a Russian
> `contentDescription` (listed in §3), so they surface as `label=…` in the element tree
> and can be tapped by matching that label — no coordinates needed.

---

## 1. Prerequisites

| Item | Value |
|------|-------|
| Device | `emulator-5554` (Pixel 9, Android 15), online |
| Screen | 1080 × 2424 px, **density = 3.0** → `px = dp × 3` |
| Package | `com.danilkha.trainstats` |
| Launcher activity | `com.danilkha.trainstats.entrypoint.MainActivity` |
| Keyboard mode | `windowSoftInputMode = adjustResize` — content resizes when a field is focused, so **Y positions shift** while a field is focused. Always call `list_elements_on_screen` before tapping (labels are stable; only coordinates shift). |

### Russian → English UI glossary

| Russian (on screen) | English |
|---------------------|---------|
| Тренировки | Workouts (history lives here) |
| Упражнения | Exercises |
| Профиль | Profile |
| История | History |
| Поиск | Search |
| Новая тренировка | New workout |
| Редактирование | Editing |
| Новое упражнение | New exercise |
| название | name (field hint) |
| Раздельно | Separated |
| С доп. весом | With extra weight |
| с собственным весом | With body weight |
| Сохранить | Save |
| Добавить упражнение | Add exercise |
| кг | kg |
| повт | reps |
| Всего записей | Total records |
| Отредактировано | Edited |

### Environment caveats

- **`mobile-mcp list_crashes` is broken in this environment** (returns `device not found: emulator-5554` even though the device is online). **Verify crashes via `adb logcat`** instead — see §9.

---

## 2. Pre-step — Build / Clear / Launch

```bash
# 1. Build & install the debug APK
./gradlew :app:installDebug

# 2. Clear app data  ->  resets the DB to EMPTY
adb -s emulator-5554 shell pm clear com.danilkha.trainstats
```

> **Important:** the production Android Room builder
> (`shared/.../DatabaseDriverFactory.android.kt`) does **not** use
> `createFromAsset` — it is `Room.databaseBuilder(...).fallbackToDestructiveMigration(true)`.
> The file `app/src/main/assets/trainstatsDb.db` is **not loaded in production**
> (it is only used by instrumented tests via `RoomTestUtils`).
> Therefore after `pm clear` the database is **empty**: no exercises, no workouts.
> You must **create an exercise first** (Scenario 1) before Scenario 2.

Launch (either form works):

```
mobile-mcp:  launch_app(device="emulator-5554", packageName="com.danilkha.trainstats")
— or —
adb -s emulator-5554 shell am start -n com.danilkha.trainstats/.entrypoint.MainActivity
```

**Expected (PASS gate):**
- `list_elements_on_screen` returns the History header **«История»** and the 3-item
  bottom navigation (Тренировки / Упражнения / Профиль).
- No `FATAL` / `AndroidRuntime` lines for `com.danilkha.trainstats` in logcat.
- Exercise list is initially empty (no cards under Поиск) — expected after clear.

---

## 3. Navigation map & tap-target labels

Every interactive control is now addressable by its **text/label** in
`list_elements_on_screen`. Tap by matching the label value.

### Bottom navigation
Three nav items across the bottom. Tap by label text
(«Тренировки» / «Упражнения» / «Профиль»).

### Icon control → accessibility label (contentDescription)

| Screen | Control | Label to match (`label=…`) |
|--------|---------|----------------------------|
| Exercises tab | Add-exercise «+» button | `Новое упражнение` |
| Exercises tab | Clear-search ✕ (only when query non-empty) | `Очистить` |
| Workouts/History tab | Add-workout «+» button | `Новая тренировка` |
| Workouts/History tab | Chart button (no-op) | `График` |
| Workouts/History tab | Calendar button | `Календарь` |
| Workouts/History tab | Clear-search ✕ | `Очистить` |
| Workouts/History tab | Scroll-to-top FAB (only when scrolled down) | `Наверх` |
| Workout editor | Exercise group header — history icon | `История упражнения` |
| Workout editor | Exercise group header — expand/collapse arrow | `Развернуть` (collapsed) / `Свернуть` (expanded) |
| Workout editor | Set row — drag handle | `Перетащить` |
| Workout editor | Set row — weight field | `Вес` |
| Workout editor | Set row — reps field (single) | `Повторения` |
| Workout editor | Set row — reps field (separated, left) | `Повторения (лево)` |
| Workout editor | Set row — reps field (separated, right) | `Повторения (право)` |
| Workout editor | Set row — delete-set ✕ | `Удалить` |
| Workout editor | Set row — undo-delete ↩ (only while a set is pending-delete) | `Вернуть` |

> Tip: decorative icons (e.g. the search magnifier next to a labeled field) are
> intentionally unlabeled and won't appear — tap the field/card text instead.

---

## 4. Scenario 1 — Exercise creation

**Goal:** verify Koin → `CreateExercisesUseCase` → Room insert.

| # | Action                                                                                                 | Expected                                                                                                                             |
|---|--------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| 1 | Tap the nav item labelled **«Упражнения»**                                                             | Exercises tab opens; header shows **«Поиск»** (search hint). List may be empty after clear.                                          |
| 2 | Tap the Add-exercise «+» button — label **`Новое упражнение`**                                         | Bottom sheet opens titled **«Новое упражнение»** with fields: «название», «Раздельно», «С доп. весом», and a **«Сохранить»** button. |
| 3 | Tap the element with hint **«название»**, type `Test Exercise QA`                                      | EditText shows `Test Exercise QA`.                                                                                                   |
| 4 | *(Optional, to allow weighted sets later)* Tap the row/card containing **«С доп. весом»** to enable it | Toggle switches to on.                                                                                                               |
| 5 | Tap **«Сохранить»**                                                                                    | Bottom sheet closes (the `Saved` event fired → `createExercisesUseCase` ran).                                                        |
| 6 | Tap **«Поиск»**, type `Test`                                                                           | Search results show **«test exercise qa»** (and any matching items).                                                                 |
| 7 | PASS gate                                                                                              | The created exercise is retrievable via search.                                                                                      |

---

## 5. Scenario 2 — Workout creation

**Goal:** verify `WorkoutViewModel` → `WorkoutSaver` → `SaveWorkoutUseCase` → suspend DAOs.

| # | Action | Expected |
|---|--------|----------|
| 1 | Tap the nav item labelled **«Тренировки»** | History screen opens; header **«История»**. |
| 2 | Tap the Add-workout «+» button — label **`Новая тренировка`** (it's the first of 3 buttons under the search field; the others are `График`/`Календарь`) | Workout editor opens, title **«Новая тренировка»**. The **exercise selector auto-opens** (a new workout emits `OpenExerciseSelector`) and lists existing exercises, including the one from Scenario 1. |
| 3 | Tap the exercise element (e.g. **«test exercise qa»**) | An exercise group card appears (title = exercise name) with one empty set row. |
| 4 | Tap the **weight field** — label **`Вес`**; type `60` | EditText shows `60` next to «кг». A new empty stub set may auto-appear below. |
| 5 | Tap the **reps field** — label **`Повторения`** (or `Повторения (лево)` in separated mode); type `10` | EditText shows `10` next to «повт». |
| 6 | Dismiss the keyboard: press **BACK once** (the IME consumes it to hide; focus remains, no navigation happens). | Keyboard hides; layout stable. |
| 7 | Tap **«Сохранить»** | Navigates back to History (`onSaved` → `navigateUp`). |
| 8 | PASS gate | A new workout card appears in History (date chip + exercise chip). |
| 9 | Re-open it: tap the topmost **«Saturday, 11 July»** (or today's date) card in History | Opens in **«Редактирование»** mode showing the saved set **`60` кг × `10` повт**. |

> **Draft-write caveat:** `WorkoutViewModel.init` (new-workout branch) writes a draft
> workout row to the DB immediately via `saveWorkoutUseCase`, and every edit is
> auto-persisted via `workoutSaver.update`. Backing out of a brand-new workout
> **without** adding sets can leave an empty workout card in History. This is current
> app behavior, not a migration bug.

---

## 6. Scenario 3 — Exercise history

**Goal:** verify `GetExerciseHistoryUseCase` (Flow queries) + `ExerciseHistoryViewModel`.

| # | Action | Expected |
|---|--------|----------|
| 1 | From History, open any workout (tap its date card) | Opens in **«Редактирование»** mode with the exercise group card(s). |
| 2 | In an exercise group card header, tap the **history icon** — label **`История упражнения`** (next to the expand arrow `Развернуть`/`Свернуть`) | Bottom sheet opens titled **«История: <exercise name>»**. |
| 3 | Read the sheet | Shows **«Всего записей: N»** and per-date set rows like `<weight>кг` / `<reps>повт`, including the set entered in Scenario 2. |
| 4 | PASS gate | The sheet opens and displays the correct set data. |
| 5 | Press **BACK** once | Closes the bottom sheet (and/or returns to History). App remains stable. |

---

## 7. Reporting

Produce a PASS/FAIL matrix like:

| Scenario | Result |
|----------|--------|
| Pre-step (build/clear/launch) | PASS / FAIL |
| 1 — Exercise creation | PASS / FAIL |
| 2 — Workout creation | PASS / FAIL |
| 3 — Exercise history | PASS / FAIL |

Overall verdict: **functional after the Koin migration** only if all scenarios PASS and
no `com.danilkha.trainstats` crashes appear in logcat.

---

## 8. Known issues / false positives — DO NOT report these

- **System crashes in logcat are unrelated:** `droid.bluetooth` `SIGABRT` (emulator
  Bluetooth service) and `.mobile.android` `SIGSEGV` (a different app). Only
  `com.danilkha.trainstats` crashes count.
- **`mobile-mcp list_crashes`** returns `device not found` in this environment — use
  `adb logcat` (§9).

---

## 9. How to verify crashes (adb logcat)

`list_crashes` is unavailable; use logcat instead.

```bash
# All app-level fatal/crash + Koin resolution errors (filters out tooling noise)
adb -s emulator-5554 logcat -d \
  | grep -iE "FATAL|AndroidRuntime.*trainstats|org.koin.*error|KoinPlatform|NotResolv|StackOverflow" \
  | grep -v uiautomator | tail -40
```

Clean result = no lines referencing `com.danilkha.trainstats`. Lines from
`droid.bluetooth` / `.mobile.android` are unrelated system/other-app crashes (see §8).
