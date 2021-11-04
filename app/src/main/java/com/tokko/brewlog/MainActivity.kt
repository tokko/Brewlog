package com.tokko.brewlog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein

class MainActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein(this)
    var brewList = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val brewId = intent.getStringExtra("brewId")
        if (brewId != null)
            showBrewFragment(brewId)
        else
            showBrewListFragment()
    }

    override fun onSupportNavigateUp(): Boolean {
        showBrewListFragment()
        return true
    }

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
        supportFragmentManager.beginTransaction().replace(android.R.id.content, BrewListFragment())
            .commit()

    }

    fun showAddBrewFragment() {
        brewList = false
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, BrewFormFragment())
            .commit()
    }

}