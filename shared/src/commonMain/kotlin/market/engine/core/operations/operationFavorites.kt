package market.engine.core.operations

import market.engine.core.network.functions.OfferOperations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import market.engine.core.network.networkObjects.Offer
import org.koin.mp.KoinPlatform

suspend fun operationFavorites(currentOffer : Offer, scope: CoroutineScope) : Boolean {
    val offerOperations : OfferOperations = KoinPlatform.getKoin().get()
    
    val res = scope.async {
        val buf = if (!currentOffer.isWatchedByMe) offerOperations.postOfferOperationWatch(
            currentOffer.id
        ) else
            offerOperations.postOfferOperationUnwatch(currentOffer.id)
        
        val res = buf.success
        withContext(Dispatchers.Main) {
            if (res != null && res.success) {
                currentOffer.isWatchedByMe = !currentOffer.isWatchedByMe
            }
            currentOffer.isWatchedByMe
        }
      
        return@async currentOffer.isWatchedByMe
    }
    res.await()
    return res.await()
}
