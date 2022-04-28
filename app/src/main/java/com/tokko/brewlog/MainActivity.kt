package com.tokko.brewlog

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

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
        /*
        if (brewId != null)
            showBrewFragment(brewId)
        else
            showBrewListFragment()
         */
        setContent {
            val navController = rememberNavController()
            BrewLogTheme {
                NavHost(navController = navController, startDestination = "brewList") {
                    composable(route = "brew") {
                        Scaffold(
                            topBar = {
                                TopAppBar(title = { Text(text = "Brew") },
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
                                BrewScreen(brewViewModel = brewViewModel)
                            }
                        )
                    }
                    composable(route = "brewForm") {
                        Scaffold(
                            topBar = {
                                TopAppBar(title = { Text(text = "Add brew") },
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
                                BrewFormScreen(brewFormViewModel = brewFormViewModel)
                            }
                        )
                    }
                    composable(route = "brewList") {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                TopAppBar(
                                    title = { Text(text = "Brewlog") },
                                    actions = {
                                        IconButton(onClick = {
                                            brewFormViewModel.brewState.value =
                                                Brew(); navController.navigate("brewForm")
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