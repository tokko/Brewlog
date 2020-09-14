package com.tokko.brewlog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class MainActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein(this)
    val firestoreRepository: IFirestoreRepository by instance()
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

    fun showBrewFragment(id: String) {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, BrewFragment.newInstance(id, firestoreRepository))
            .commit()
    }

    fun showBrewListFragment() {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, BrewListFragment())
            .commit()

    }

    fun showAddBrewFragment() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, BrewFormFragment())
            .commit()
    }

    fun brewAdded() {
        supportFragmentManager.beginTransaction().replace(android.R.id.content, BrewListFragment())
            .commit()
    }

}