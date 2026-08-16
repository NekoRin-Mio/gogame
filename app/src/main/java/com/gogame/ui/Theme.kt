package com.gogame.ui

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SpecLightColorScheme = lightColorScheme(
    primary = SpecPrimary,
    onPrimary = SpecOnPrimary,
    primaryContainer = SpecPrimaryContainer,
    onPrimaryContainer = SpecOnPrimaryContainer,
    secondary = SpecSecondary,
    onSecondary = SpecOnSecondary,
    secondaryContainer = SpecSecondaryContainer,
    onSecondaryContainer = SpecOnSecondaryContainer,
    tertiary = SpecTertiary,
    onTertiary = SpecOnTertiary,
    tertiaryContainer = SpecTertiaryContainer,
    onTertiaryContainer = SpecOnTertiaryContainer,
    error = SpecError,
    onError = SpecOnError,
    errorContainer = SpecErrorContainer,
    onErrorContainer = SpecOnErrorContainer,
    background = SpecBackground,
    onBackground = SpecOnBackground,
    surface = SpecSurface,
    onSurface = SpecOnSurface,
    surfaceVariant = SpecSurfaceVariant,
    onSurfaceVariant = SpecOnSurfaceVariant,
    surfaceContainerLowest = SpecSurfaceContainerLowest,
    surfaceContainerLow = SpecSurfaceContainerLow,
    surfaceContainer = SpecSurfaceContainer,
    surfaceContainerHigh = SpecSurfaceContainerHigh,
    surfaceContainerHighest = SpecSurfaceContainerHighest,
    outline = SpecOutline,
    outlineVariant = SpecOutlineVariant,
)

private val SpecDarkColorScheme = darkColorScheme(
    primary = SpecPrimaryDark,
    onPrimary = SpecOnPrimaryDark,
    primaryContainer = SpecPrimaryContainerDark,
    onPrimaryContainer = SpecOnPrimaryContainerDark,
    secondary = SpecSecondaryDark,
    onSecondary = SpecOnSecondaryDark,
    secondaryContainer = SpecSecondaryContainerDark,
    onSecondaryContainer = SpecOnSecondaryContainerDark,
    tertiary = SpecTertiaryDark,
    onTertiary = SpecOnTertiaryDark,
    tertiaryContainer = SpecTertiaryContainerDark,
    onTertiaryContainer = SpecOnTertiaryContainerDark,
    error = SpecErrorDark,
    onError = SpecOnErrorDark,
    errorContainer = SpecErrorContainerDark,
    onErrorContainer = SpecOnErrorContainerDark,
    background = SpecBackgroundDark,
    onBackground = SpecOnBackgroundDark,
    surface = SpecSurfaceDark,
    onSurface = SpecOnSurfaceDark,
    surfaceVariant = SpecSurfaceVariantDark,
    onSurfaceVariant = SpecOnSurfaceVariantDark,
    surfaceContainerLowest = SpecSurfaceContainerLowestDark,
    surfaceContainerLow = SpecSurfaceContainerLowDark,
    surfaceContainer = SpecSurfaceContainerDark,
    surfaceContainerHigh = SpecSurfaceContainerHighDark,
    surfaceContainerHighest = SpecSurfaceContainerHighestDark,
    outline = SpecOutlineDark,
    outlineVariant = SpecOutlineVariantDark,
)

@Composable
fun GoGameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> SpecDarkColorScheme
        else -> SpecLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SpecTypography,
        shapes = SpecShapes,
        content = content
    )
}
