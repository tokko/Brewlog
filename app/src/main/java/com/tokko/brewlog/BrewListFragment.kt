package com.tokko.brewlog

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import org.joda.time.DateTime
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class BrewListFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val fireStoreRepository: IFirestoreRepository by instance()
    private val brewListState = mutableStateListOf<Brew>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                BrewLogTheme {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(brewListState) { brew ->
                            BrewCard(brew)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BrewCard(brew: Brew) {
        Column(modifier = Modifier
            .clickable {
                (activity as MainActivity).showBrewFragment(brew.id)
            }
            .fillMaxWidth()) {
            Text(
                brew.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp),
                fontSize = 24.sp,
                color = when {
                    brew.hasAction() -> Color(-65536)
                    DateTime(brew.drinkable).withTimeAtStartOfDay().isBeforeNow -> Color(-16711936)
                    else -> Color(-16777216)
                }
            )
            Divider()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.brew_list_menu, menu)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        fireStoreRepository.getBrews { brews ->
            if (activity != null) {
                brewListState.addAll(brews.sortedByDescending { if (it.hasAction()) Long.MAX_VALUE else it.brewDate })
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        (activity as MainActivity).showAddBrewFragment()
        return true
    }
}

fun Brew.hasAction() =
    DateTime(this.fermentationTime).withTimeAtStartOfDay().isBeforeNow && !this.isBottled || this.dryhops.any {
        !it.checked && DateTime(
            it.date
        ).withTimeAtStartOfDay().isBeforeNow
    }
