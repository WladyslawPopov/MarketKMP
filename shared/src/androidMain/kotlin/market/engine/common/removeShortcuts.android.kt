package market.engine.common

var deleteShortcuts: () -> Unit = {}

actual fun removeShortcuts() {
    deleteShortcuts()
}
