package market.engine.fragments.base.listing

import androidx.compose.runtime.Composable
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.fragments.base.screens.NoItemsFoundLayout
import org.jetbrains.compose.resources.stringResource


@Composable
fun ListingNotFoundContent(
    isLoading: Boolean,
    itemCount: Int,
    activeType: ActiveWindowListingType,
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit,
    onRefresh: () -> Unit
): @Composable (() -> Unit)? {
    return when {
        activeType == ActiveWindowListingType.LISTING -> {
            if (isLoading && itemCount < 1) {
                @Composable {
                    if (hasActiveFilters) {
                        NoItemsFoundLayout(
                            textButton = stringResource(strings.resetLabel)
                        ) {
                            onClearFilters()
                        }
                    } else {
                        NoItemsFoundLayout {
                            onRefresh()
                        }
                    }
                }
            } else {
                null
            }
        }

        else -> {
            null
        }
    }
}
