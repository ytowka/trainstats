# Interop ViewModel'ей KMP с iOS: универсальное руководство

Руководство описывает инфраструктурный слой подключения мультиплатформенных ViewModel (паттерн MVI) к SwiftUI. Применимо к любому KMP-проекту. Подразумевается контракт ViewModel: exposes `state: StateFlow<State>`, опционально `sideEffect: SharedFlow<SideEffect>`, приём команд через `fun accept(intent: Intent)`.

## 1. Стек

| Роль | Библиотека |
|---|---|
| Базовый класс VM (commonMain) | `androidx.lifecycle:lifecycle-viewmodel` (мультиплатформенный, ≥ 2.8.x) |
| Корутины/Flow | `org.jetbrains.kotlinx:kotlinx-coroutines-core` |
| DI | `io.insert-koin:koin-core` + `koin-annotations` + `koin-ksp-compiler` |
| **Мост Flow → Swift** | **SKIE** (`co.touchlab.skie`) |

**Главный принцип:** весь interop берёт на себя SKIE. Никаких `@NativeCoroutines`, `@objc`, ручных `expect/actual` для Flow или `ObservableObject` на стороне Kotlin.

## 2. Базовый класс ViewModel (только commonMain)

Базовый класс наследуется от мультиплатформенного `androidx.lifecycle.ViewModel` и живёт **только в `commonMain`** — без `expect/actual` и без платформенной специализации. Это даёт единый `viewModelScope` на Android и iOS.

```kotlin
abstract class MviViewModel<Intent, State, SideEffect> : ViewModel() {
    abstract val initialState: State

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state

    private val _sideEffect = MutableSharedFlow<SideEffect>(extraBufferCapacity = 5)
    val sideEffect: SharedFlow<SideEffect> = _sideEffect

    abstract fun accept(intent: Intent)   // точка входа событий из UI
    /* reduce / loadData / обработка intents — внутренняя MVI-логика */
}
```

**Контракт для interop:** каждая ViewModel выставляет наружу только `StateFlow` (состояние), опционально `SharedFlow` (side-эффекты) и функцию приёма `intent`. Сами типы `State`/`Intent`/`SideEffect` делаются `sealed interface` / `data class` — это нужно для красивого Swift-API (см. §3).

## 3. SKIE — мост Kotlin → Swift

SKIE (плагин `co.touchlab.skie`) подключается в модуле, который собирает iOS-фреймворк (обычно — зонтичный модуль `shared`), и трансформирует API на этапе компиляции:

- `StateFlow<T>` и `SharedFlow<T>` → Swift `AsyncSequence` → доступен `for await x in vm.state { ... }`;
- `suspend fun` → Swift `async`;
- sealed-классы → нативные Swift-типы с плоскими именами: `AuthIntent.OnLogin` → `AuthIntentOnLogin()`, `RootState.Loading` → `RootStateLoading()`;
- `data class` → Swift-классы со свойствами; `object` → синглтон (`.shared`); generics транслируются корректно.

> Альтернативы (KMP-NativeCoroutines, KMP-ObservableViewModel) требуют аннотаций или привязки `viewModelScope` к Swift Runloop. SKIE даёт то же без аннотаций — выберите **одно** решение, не комбинируйте.

## 4. Koin: доступ к ViewModel с iOS

На iOS нет `koin-androidx-compose` / `koin-compose-viewmodel`, поэтому VM получается из общего котлиновского `object : KoinComponent` — по одной функции на ViewModel. SKIE превращает `object` в Swift-синглтон.

```kotlin
object ViewModelProvider : KoinComponent {
    fun getAuthViewModel(): AuthViewModel = getKoin().get()
    fun getContentViewModel(topicId: Long): ContentViewModel =
        getKoin().get { parametersOf(topicId) }   // параметры → в конструктор VM
}
```

Вызов со Swift: `ViewModelProvider.shared.getAuthViewModel()`.

Регистрация: каждая ViewModel помечается `@Factory` (Koin annotations + KSP), зависимости резолвятся автоматически. Инициализация Koin — единая точка `startKoin` в `commonMain`, вызывается с обеих платформ.

## 5. iOS-обёртка `ObservableObject`

Котлиновский класс VM не является `ObservableObject`. Мостом служит тонкий дженерик-класс на Swift — единый файл, переиспользуемый всеми экранами. Это **вся инфраструктура interop на стороне Swift**:

```swift
import Foundation
import shared

final class MviViewModelWrapper<Intent: AnyObject, State: AnyObject, SideEffect: AnyObject>
    : ObservableObject {

    private let vm: MviViewModel<Intent, State, SideEffect>
    @Published var state: State

    init(vm: MviViewModel<Intent, State, SideEffect>) {
        self.vm = vm
        state = vm.initialState!                      // стартовое значение из Kotlin
    }

    @MainActor func activate() async {
        for await s in vm.state { self.state = s! }   // StateFlow → AsyncSequence (SKIE)
    }

    @MainActor func activateSideEffects(_ handler: @escaping (SideEffect) -> Void) async {
        for await e in vm.sideEffect { if let e = e { handler(e) } }
    }

    func accept(intent: Intent) { vm.accept(intent: intent) }
}
```

## 6. Подключение экрана

```swift
struct AuthView: View {
    @StateObject private var wrapper = MviViewModelWrapper(
        vm: ViewModelProvider.shared.getAuthViewModel()    // ← из Koin
    )

    var body: some View {
        // рендер по wrapper.state (sealed-кейсы): if let s = state as? AuthStateSuccess { ... }
        PrimaryButton("Войти") { wrapper.accept(intent: AuthIntentOnLogin()) }
    }
    .task { await wrapper.activate() }                      // сбор StateFlow
    .task { await wrapper.activateSideEffects { e in /* навигация/алерты */ } }
}
```

## 7. Запуск приложения

```swift
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_: UIApplication, didFinishLaunchingWithOptions _: ...) -> Bool {
        CommonDi.shared.doInitDi(appDeclaration: { _ in })  // startKoin
        return true
    }
}
```

Порядок: `startKoin` → все `@Factory` VM доступны → экраны зовут `ViewModelProvider.shared.getXxxViewModel()`.

## 8. Поток данных (сводно)

```
SwiftUI .task { await wrapper.activate() }
   │   ViewModelProvider.shared.getXxxViewModel() → Koin get
   ▼   ──────── граница SKIE (iOS-фреймворк) ────────
Kotlin MviViewModel (extends androidx.lifecycle.ViewModel, commonMain)
   ├─ accept(intent) → корутина внутри viewModelScope
   └─ StateFlow / SharedFlow ─→ SKIE ─→ Swift AsyncSequence
   ▼
Swift: for await s in vm.state { @Published state = s } → SwiftUI re-render
```

## 9. Чек-лист

- [ ] Базовый VM в `commonMain` наследует мультиплатформенный `androidx.lifecycle.ViewModel` (без `expect/actual`).
- [ ] Состояние — `StateFlow` (+ опц. `SharedFlow`), типы `State`/`Intent` — sealed/data.
- [ ] SKIE подключён в модуле, собирающем iOS-фреймворк; никаких других Flow-мостов.
- [ ] Koin: `@Factory` на ViewModel, единый `startKoin` в `commonMain`.
- [ ] `object ViewModelProvider : KoinComponent` — единственная точка получения VM с iOS.
- [ ] Swift-обёртка `MviViewModelWrapper: ObservableObject` — собирает flow через `for await`, форвардит intents.
- [ ] `CommonDi.shared.doInitDi { _ in }` в `AppDelegate`.
