package market.engine.common

expect object LoginMethods {
    var login : (externalID: String) -> Unit
    var logout : () -> Unit
}
