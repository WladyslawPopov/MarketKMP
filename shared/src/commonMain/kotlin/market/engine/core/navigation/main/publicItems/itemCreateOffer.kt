package market.engine.core.navigation.main.publicItems

import com.arkivanov.decompose.ComponentContext
import market.engine.core.types.CreateOfferTypes
import market.engine.presentation.createOffer.CreateOfferComponent
import market.engine.presentation.createOffer.DefaultCreateOfferComponent

fun itemCreateOffer(
    componentContext: ComponentContext,
    categoryId: Long,
    offerId: Long?,
    type : CreateOfferTypes,
    externalImages : List<String>?,
    navigateBack: () -> Unit
    ): CreateOfferComponent {
        return DefaultCreateOfferComponent(
            categoryId = categoryId,
            offerId = offerId,
            type = type,
            externalImages = externalImages,
            componentContext,
            navigateBack = {
                navigateBack()
            }
        )
    }
