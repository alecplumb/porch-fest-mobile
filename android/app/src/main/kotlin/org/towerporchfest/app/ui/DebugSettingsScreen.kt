package org.towerporchfest.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import org.koin.compose.koinInject
import org.towerporchfest.app.R
import org.towerporchfest.shared.AppConfig

/**
 * Developer settings, reachable from the More tab in debug builds only.
 *
 * For now this shows which environment the build is using. Environment selection goes in the
 * "Environment" section; Android-only developer settings get their own section below it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugSettingsScreen(onBack: () -> Unit, config: AppConfig = koinInject()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            SectionHeader("Environment")
            ListItem(headlineContent = { Text("Environment") }, supportingContent = { Text(config.environment) })
            ListItem(headlineContent = { Text("Backend") }, supportingContent = { Text(config.baseUrl) })
        }
    }
}
