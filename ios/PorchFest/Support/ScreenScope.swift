import Shared

/// Owns the shared (Kotlin) view models for one SwiftUI screen. Keep it in `@State`; when
/// SwiftUI discards the screen this is deallocated and the view models' coroutines are cancelled.
final class ScreenScope {
    let viewModels = ViewModelOwner()

    deinit {
        viewModels.clear()
    }
}
