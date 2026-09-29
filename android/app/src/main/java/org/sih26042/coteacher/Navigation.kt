package org.sih26042.coteacher

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import org.sih26042.coteacher.ui.screens.*

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val app = context.applicationContext as SIHApplication
    val container = app.container

    val backStack = rememberNavBackStack(HomeDest)

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            } else {
                (context as? Activity)?.finish()
            }
        },
        entryProvider = entryProvider {
            entry<HomeDest> {
                HomeScreen(
                    container = container,
                    onNavigate = { dest -> backStack.add(dest) }
                )
            }
            entry<LiveClassDest> {
                LiveClassScreen(
                    container = container,
                    onNavigate = { dest -> backStack.add(dest) }
                )
            }
            entry<TranslationDetailDest> { dest ->
                TranslationDetailScreen(
                    args = dest,
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<WorksheetsDest> {
                WorksheetsScreen(
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<FlashcardsDest> {
                FlashcardsScreen(
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<ActivitiesDest> {
                ActivitiesScreen(
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<LanguagePacksDest> {
                LanguagePacksScreen(
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<PerformanceDest> {
                PerformanceScreen(
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<SettingsDest> {
                SettingsScreen(
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
