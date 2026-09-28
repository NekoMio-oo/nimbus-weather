package com.example.nimbus.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nimbus.di.AppContainer
import com.example.nimbus.ui.home.HomeRoute
import com.example.nimbus.ui.home.HomeViewModel
import com.example.nimbus.ui.search.SearchRoute
import com.example.nimbus.ui.search.SearchViewModel

/** Route names. Two screens: the forecast pager and the place search. */
object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
}

/** The navigation graph. View models are built from the [container] so screens stay free of wiring. */
@Composable
fun NimbusApp(container: AppContainer) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { slideInHorizontally(tween(320)) { it / 4 } + fadeIn(tween(320)) },
        exitTransition = { slideOutHorizontally(tween(320)) { -it / 4 } + fadeOut(tween(240)) },
        popEnterTransition = { slideInHorizontally(tween(320)) { -it / 4 } + fadeIn(tween(320)) },
        popExitTransition = { slideOutHorizontally(tween(320)) { it / 4 } + fadeOut(tween(240)) },
    ) {
        composable(Routes.HOME) {
            val viewModel: HomeViewModel = viewModel {
                HomeViewModel(
                    observeSavedPlaces = container.observeSavedPlaces,
                    getForecast = container.getForecast,
                    observeUnitSystem = container.observeUnitSystem,
                    setUnitSystem = container.setUnitSystem,
                )
            }
            HomeRoute(viewModel, onOpenSearch = { navController.navigate(Routes.SEARCH) })
        }
        composable(Routes.SEARCH) {
            val viewModel: SearchViewModel = viewModel {
                SearchViewModel(
                    searchPlaces = container.searchPlaces,
                    observeSavedPlaces = container.observeSavedPlaces,
                    savePlace = container.savePlace,
                    removePlace = container.removePlace,
                )
            }
            SearchRoute(viewModel, onBack = { navController.popBackStack() })
        }
    }
}
