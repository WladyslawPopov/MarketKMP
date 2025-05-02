package market.engine.common

actual object LoginMethods {
    actual var login: (externalID: String) -> Unit
        get() = {}
        set(value) {}
    actual var logout: () -> Unit
        get() = {}
        set(value) {}
}
