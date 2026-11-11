package org.towerporchfest.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light follows the website: white pages, beige panels, indigo bands, slate-blue controls, gold accents.
private val LightColors = lightColorScheme(
    primary = BrandIndigo,
    onPrimary = Color.White,
    primaryContainer = BrandBeige,
    onPrimaryContainer = BrandIndigo,
    secondary = BrandBlue,
    onSecondary = Color.White,
    secondaryContainer = BlueContainer,
    onSecondaryContainer = BrandIndigo,
    tertiary = BrandGold,
    onTertiary = BrandIndigo,
    tertiaryContainer = GoldContainer,
    onTertiaryContainer = BrandIndigo,
    background = Color.White,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = BrandBeige,
    onSurfaceVariant = InkMuted,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = BeigeLightest,
    surfaceContainer = BeigeLight,
    surfaceContainerHigh = BeigeMid,
    surfaceContainerHighest = BrandBeige,
    outline = Color(0xFF7A7A86),
    outlineVariant = Color(0xFFD6CEC4),
)

// The website has no dark mode. This one is built on its indigo so the app still looks like
// Porch Fest when the phone is in dark mode.
private val DarkColors = darkColorScheme(
    primary = BlueLight,
    onPrimary = BrandIndigo,
    primaryContainer = IndigoHigh,
    onPrimaryContainer = BrandBeige,
    secondary = BlueLight,
    onSecondary = BrandIndigo,
    secondaryContainer = BlueDark,
    onSecondaryContainer = Color.White,
    tertiary = BrandGold,
    onTertiary = BrandIndigo,
    tertiaryContainer = Color(0xFF5C4600),
    onTertiaryContainer = GoldContainer,
    background = IndigoDarkest,
    onBackground = Color(0xFFEDE7E0),
    surface = IndigoDarkest,
    onSurface = Color(0xFFEDE7E0),
    surfaceVariant = IndigoRaised,
    onSurfaceVariant = Color(0xFFC4C0CC),
    surfaceContainerLowest = Color(0xFF101018),
    surfaceContainerLow = Color(0xFF1B1B29),
    surfaceContainer = BrandIndigo,
    surfaceContainerHigh = IndigoRaised,
    surfaceContainerHighest = IndigoHigh,
    outline = Color(0xFF8E8E9C),
    outlineVariant = Color(0xFF44445A),
)

/** The app's theme. Colours are fixed to the brand; Android's wallpaper-based colours are not used. */
@Composable
fun PorchFestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
