package market.engine.core.data.items

data class SpinnerCustomItem(var code: Int, var name: String, var weight:Int) {

    override fun toString(): String {
        return name
    }
}
