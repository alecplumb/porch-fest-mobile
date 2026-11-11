package org.towerporchfest.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.towerporchfest.app.BuildConfig
import org.towerporchfest.app.R

private const val CREATOR_COMPANY = "Etherwalker Labs"
private const val CREATOR_NAME = "Alec Plumb"
private const val CREATOR_EMAIL = "alec@etherwalker.com"

/** The More tab: about the app and, in debug builds, the way into Debug Settings. */
@Composable
fun MoreScreen(onOpenDebugSettings: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SectionHeader("About")
        ListItem(
            headlineContent = { Text(stringResource(R.string.app_name)) },
            supportingContent = { Text("Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})") },
        )
        HorizontalDivider()

        SectionHeader("Created by")
        ListItem(headlineContent = { Text(CREATOR_COMPANY) }, supportingContent = { Text(CREATOR_NAME) })
        ListItem(
            headlineContent = { Text(CREATOR_EMAIL, color = MaterialTheme.colorScheme.secondary) },
            supportingContent = { Text("Send an email") },
            modifier = Modifier.clickable { uriHandler.openUri("mailto:$CREATOR_EMAIL") },
        )

        if (BuildConfig.DEBUG) {
            HorizontalDivider()
            SectionHeader("Developer")
            ListItem(
                headlineContent = { Text("Debug Settings") },
                supportingContent = { Text("Environment and other developer options") },
                modifier = Modifier.clickable(onClick = onOpenDebugSettings),
            )
        }
    }
}

@Composable
internal fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
    )
}
