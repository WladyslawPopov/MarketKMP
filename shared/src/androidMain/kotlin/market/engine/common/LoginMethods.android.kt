package market.engine.common

actual object LoginMethods {
    actual var login: (externalID: String) -> Unit = {}
    actual var logout: () -> Unit = {}
}
