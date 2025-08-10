package market.engine.fragments.root.main.createOffer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.PhotoSave
import market.engine.core.utils.convertDateWithMinutes
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.ilustrations.LoadImage
import market.engine.widgets.texts.SeparatorLabel
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.stringResource

@Composable
fun SuccessContent(
    images: List<PhotoSave>,
    title : String,
    isActive : Boolean = true,
    futureTime : Long?,
    modifier: Modifier = Modifier,
    goToOffer : () -> Unit,
    createNewOffer : () -> Unit,
    addSimilarOffer : () -> Unit,
){
    Column(
        modifier = modifier
            .background(colors.white, MaterialTheme.shapes.small)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val header = buildAnnotatedString {
            if (isActive) {
                append(stringResource(strings.congratulationsCreateOfferInFutureLabel))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.yellowSun)) {
                    append(" ${futureTime?.convertDateWithMinutes()}")
                }
            } else{
                append(
                    stringResource(strings.congratulationsLabel)
                )
            }
        }

        SeparatorLabel("", annotatedString = header )

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(dimens.mediumPadding).clickable {
                    goToOffer()
                }.clip(MaterialTheme.shapes.medium)
            ,
            verticalAlignment = Alignment.Top,
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.white),
                shape = MaterialTheme.shapes.medium
            ) {
                LoadImage(
                    images.firstOrNull()?.uri ?: images.firstOrNull()?.url ?: "",
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.FillBounds
                )
            }

            Spacer(Modifier.width(dimens.mediumSpacer))

            TitleText(
                title,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(dimens.smallPadding),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // add similar
            AcceptedPageButton(
                stringResource(strings.createSimilarOfferLabel),
                Modifier.weight(1f)
                    .padding(dimens.smallPadding),
                containerColor = colors.brightGreen,
            ) {
                addSimilarOffer()
            }
            // create New
            AcceptedPageButton(
                stringResource(strings.createNewOfferTitle),
                Modifier.weight(1f)
                    .padding(dimens.smallPadding),
            ) {
                createNewOffer()
            }
        }
    }
}
