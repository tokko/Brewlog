package com.tokko.brewlog

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : AppCompatActivity(), KoinComponent {
    var brewList = true
    val brewListViewModel: BrewListViewModel by inject()
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
                    composable(route = "brew/{brewId}",
                        arguments = listOf(navArgument("brewId") { type = NavType.StringType })
                    ) {
                        val brewId = it.arguments?.getString("brewId") ?: ""
                        Text(text = "Brew id: $brewId")
                    }
                    composable(route = "brewList") {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                TopAppBar(
                                    title = { Text(text = "Brewlog") },
                                    actions = {
                                        IconButton(onClick = { /*TODO*/ }) {
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
                                navController.navigate("brew/$it")
                            })
                        }
                    }
                }
            }
        }
    }

    /*
        override fun onSupportNavigateUp(): Boolean {
            showBrewListFragment()
            return true
        }
    */
    override fun onBackPressed() {
        if (!brewList)
            showBrewListFragment()
        else
            super.onBackPressed()
    }

    fun showBrewFragment(id: String) {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        brewList = false
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, BrewFragment.newInstance(id))
            .commit()
    }

    fun showBrewListFragment() {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        brewList = true
        //  supportFragmentManager.beginTransaction().replace(android.R.id.content, BrewListFragment())
        //    .commit()

    }

    fun showAddBrewFragment() {
        brewList = false
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, BrewFormFragment())
            .commit()
    }

}