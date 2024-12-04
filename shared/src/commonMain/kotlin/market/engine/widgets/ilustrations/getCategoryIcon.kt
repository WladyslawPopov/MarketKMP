package market.engine.widgets.ilustrations

import androidx.compose.runtime.Composable
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun getCategoryIcon(categoryName: String?): DrawableResource? {
    return when (categoryName) {
        stringResource(strings.categoryCollection) -> drawables.collectionPng
        stringResource(strings.categoryVintage) -> drawables.vintagePng
        stringResource(strings.categoryRetro) -> drawables.vintagePng
        stringResource(strings.categoryElectronic) -> drawables.electronicPng
        stringResource(strings.categoryElectronic2) -> drawables.electronicPng
        stringResource(strings.categoryBeauty) -> drawables.beautyPng
        stringResource(strings.categoryFashion) -> drawables.beautyPng
        stringResource(strings.categoryMusic) -> drawables.musicPng
        stringResource(strings.categoryMusicBooksFilms) -> drawables.musicPng
        stringResource(strings.categoryPhone) -> drawables.phonePng
        stringResource(strings.categorySport) -> drawables.sportPng
        stringResource(strings.categoryBook) -> drawables.bookPng
        stringResource(strings.categoryOther) -> drawables.otherPng
        stringResource(strings.categoryArt) -> drawables.artPng
        else -> null
    }
}
