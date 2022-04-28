package com.tokko.brewlog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import org.joda.time.DateTime

fun Brew.hasAction() =
    DateTime(this.fermentationTime).withTimeAtStartOfDay().isBeforeNow && !this.isBottled || this.dryhops.any {
        !it.checked && DateTime(
            it.date
        ).withTimeAtStartOfDay().isBeforeNow
    }

class BrewListViewModel(private val fireStoreRepository: IFirestoreRepository) : ViewModel() {
    val brewListState = mutableStateListOf<Brew>()

    fun observeBrews() {
        fireStoreRepository.getBrews { brews ->
            brewListState.apply { clear() }
                .addAll(brews.sortedByDescending { if (it.hasAction()) Long.MAX_VALUE else it.brewDate })
        }
    }
}

@Composable
fun BrewListScreen(brewListViewModel: BrewListViewModel, onBrewClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(brewListViewModel.brewListState) { brew ->
            BrewCard(brew) {
                onBrewClick(it)
            }
        }
    }
}

@Composable
fun BrewCard(brew: Brew, onClick: (String) -> Unit) {
    Column(modifier = Modifier
        .clickable {
            onClick(brew.id)
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
                else -> MaterialTheme.colors.onBackground
            }
        )
        Divider()
    }

}