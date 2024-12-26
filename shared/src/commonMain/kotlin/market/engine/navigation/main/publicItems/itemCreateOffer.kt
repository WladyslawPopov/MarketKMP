package market.engine.navigation.main.publicItems

import com.arkivanov.decompose.ComponentContext
import market.engine.core.data.types.CreateOfferType
import market.engine.fragments.createOffer.CreateOfferComponent
import market.engine.fragments.createOffer.DefaultCreateOfferComponent

fun itemCreateOffer(
    componentContext: ComponentContext,
    catPath: List<Long>?,
    offerId: Long?,
    type : CreateOfferType,
    externalImages : List<String>?,
    navigateOffer: (Long) -> Unit,
    navigateCreateOffer: (Long?, List<Long>?, CreateOfferType) -> Unit,
    navigateBack: () -> Unit
    ): CreateOfferComponent {
        return DefaultCreateOfferComponent(
            catPath = catPath,
            offerId = offerId,
            type = type,
            externalImages = externalImages,
            componentContext,
            navigateToOffer = { id->
                navigateOffer(id)
            },
            navigateToCreateOffer = { id, path, t ->
                navigateCreateOffer(id, path, t)
            },
            navigateBack = {
                navigateBack()
            }
        )
    }
