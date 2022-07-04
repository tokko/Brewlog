package com.tokko.brewlog

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@ExperimentalComposeUiApi
class MainActivity : AppCompatActivity(), KoinComponent {
    private val brewListViewModel: BrewListViewModel by inject()
    private val brewViewModel: BrewViewModel by inject()
    private val brewFormViewModel: BrewFormViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        supportActionBar?.hide()
        brewListViewModel.observeBrews()
        val brewId = intent.getStringExtra("brewId")
        if (brewId != null)
            brewViewModel.getBrew(brewId = brewId)
        setContent {
            val navController = rememberNavController()
            BrewLogTheme {
                NavHost(
                    navController = navController,
                    startDestination = if (brewId != null) "brew" else "brewList"
                ) {
                    composable(route = "brew") {
                        NavigateUpComposable(title = "Brew", navController = navController) {
                            BrewScreen(brewViewModel = brewViewModel, onWortBoil = {
                                navController.navigate("wortBoilViewer")
                            })
                        }
                    }
                    composable(route = "wortBoilDesigner") {
                        NavigateUpComposable(
                            title = "Wort boil designer",
                            navController = navController
                        ) {
                            WortBoilDesigner(brewFormViewModel = brewFormViewModel)
                        }
                    }
                    composable(route = "wortBoilViewer") {
                        NavigateUpComposable(title = "Wort boil", navController = navController) {
                            WortBoilViewer(brewViewModel = brewViewModel)
                        }
                    }
                    composable(route = "brewForm") {
                        NavigateUpComposable(title = "Add brew", navController = navController) {
                            BrewFormScreen(
                                brewFormViewModel = brewFormViewModel,
                                onWortBoilDesign = { navController.navigate("wortBoilDesigner") }) {
                                navController.navigateUp()
                            }
                        }
                    }
                    composable(route = "brewList") {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                TopAppBar(
                                    title = { Text(text = "Brewlog") },
                                    actions = {
                                        IconButton(onClick = {
                                            navController.navigate("brewForm")
                                        }) {
                                            Icon(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = "Add brew"
                                            )
                                        }
                                    }
                                )
                            }
                        ) {
                            BrewListScreen(brewListViewModel = brewListViewModel, onBrewClick = {
                                brewViewModel.getBrew(it)
                                navController.navigate("brew")
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NavigateUpComposable(
    title: String,
    navController: NavController,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "navigate up"
                        )
                    }
                })
        },
        content = {
            content()
        }
    )
}