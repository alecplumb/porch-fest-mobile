package org.towerporchfest.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import org.koin.androidx.compose.koinViewModel
import org.towerporchfest.app.R
import org.towerporchfest.app.theme.BrandBeige
import org.towerporchfest.app.theme.BrandGold
import org.towerporchfest.app.theme.BrandIndigo
import org.towerporchfest.shared.model.FestivalInfo
import org.towerporchfest.shared.model.Sponsor
import org.towerporchfest.shared.model.SponsorGroup
import org.towerporchfest.shared.presentation.HomeUiState
import org.towerporchfest.shared.presentation.HomeViewModel

/** The home tab: festival art, the date, and the sponsors, laid out like the website's home page. */
@Composable
fun PorchHomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.refresh() }
    PorchHomeContent(state = state, onRefresh = viewModel::refresh)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PorchHomeContent(state: HomeUiState, onRefresh: () -> Unit) {
    PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
            item(key = "art") { FestivalArt() }
            item(key = "date") { DateBand() }
            state.sponsorGroups.forEach { group ->
                item(key = "level-${group.level}") { SponsorGroupHeader(group) }
                // The top tier gets full-width logos; the others sit two to a row.
                val perRow = if (group.level == 0) 1 else 2
                items(group.sponsors.chunked(perRow), key = { row -> row.first().id }) { row ->
                    SponsorRow(row, perRow)
                }
            }
        }
    }
}

// The logo is navy line art, so it always sits on the website's beige, in light and dark mode.
@Composable
private fun FestivalArt() {
    Box(Modifier.fillMaxWidth().background(BrandBeige).padding(24.dp), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.brand_logo),
            contentDescription = "${FestivalInfo.NAME}: building community through music and art",
            contentScale = ContentScale.Fit,
            modifier = Modifier.height(300.dp),
        )
    }
}

@Composable
private fun DateBand() {
    Column(
        Modifier.fillMaxWidth().background(BrandIndigo).padding(vertical = 20.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            FestivalInfo.LOCATION.uppercase(),
            color = BrandGold,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
        )
        Text(FestivalInfo.DATE, color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Text(
            FestivalInfo.TAGLINE.uppercase(),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SponsorGroupHeader(group: SponsorGroup) {
    Text(
        group.title.uppercase(),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun SponsorRow(sponsors: List<Sponsor>, perRow: Int) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        sponsors.forEach { sponsor -> SponsorCard(sponsor, Modifier.weight(1f)) }
        // Keep a lone logo in a two-up row at half width.
        repeat(perRow - sponsors.size) { Spacer(Modifier.weight(1f)) }
    }
}

/** Sponsor logos are 2:1 artwork on white, so the card is always white, whatever the theme. */
@Composable
private fun SponsorCard(sponsor: Sponsor, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val shape = RoundedCornerShape(16.dp)
    Surface(
        color = Color.White,
        shape = shape,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .aspectRatio(2f)
            .clip(shape)
            .then(
                if (sponsor.website.isNotBlank()) {
                    Modifier.clickable(role = Role.Button, onClickLabel = "Open ${sponsor.name}'s website") {
                        uriHandler.openUri(sponsor.website)
                    }
                } else {
                    Modifier
                }
            ),
    ) {
        SubcomposeAsyncImage(
            model = sponsor.logoUrl,
            contentDescription = sponsor.name,
            contentScale = ContentScale.Fit,
            // No logo, or it couldn't be loaded (offline before it was ever cached): show the name.
            error = { SponsorName(sponsor.name) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun SponsorName(name: String) {
    Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
        Text(name, color = BrandIndigo, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
    }
}
