package com.tokko.brewlog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*


class BrewViewModel(private val fireStoreRepository: IFirestoreRepository) : ViewModel() {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("sv-SE"))
    val brewState = mutableStateOf(Brew())
    fun getBrew(brewId: String) {
        fireStoreRepository.getBrew(brewId) {
            brewState.value = it
        }
    }

    fun addBrew(value: Brew) {
        fireStoreRepository.addBrew(brew = value)
    }
}


@Composable
private fun DryHop(brewViewModel: BrewViewModel, dryHop: DryHopping) {

    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = brewViewModel.dateFormat.format(dryHop.date), modifier = Modifier.weight(1f))
        Text(text = "Hop: ${dryHop.type}", modifier = Modifier.weight(1f))
        Text(text = "Amount: ${dryHop.amount}g", modifier = Modifier.weight(1f))
        val state = remember { mutableStateOf(dryHop.checked) }
        Checkbox(checked = state.value, onCheckedChange = {
            dryHop.checked = it
            state.value = it
            brewViewModel.addBrew(brewViewModel.brewState.value)
        })
    }
}

@Composable
fun BrewScreen(brewViewModel: BrewViewModel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background)
            .padding(16.dp)
    ) {
        Text(
            text = brewViewModel.brewState.value.name,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Brew date: ",
                fontWeight = FontWeight.Bold,
            )
            Text(text = brewViewModel.dateFormat.format(brewViewModel.brewState.value.brewDate))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Divider()
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Fermentation end date: ",
                fontWeight = FontWeight.Bold,
            )
            Text(text = brewViewModel.dateFormat.format(brewViewModel.brewState.value.fermentationTime))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Divider()
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Dry hops:",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        LazyColumn {
            items(brewViewModel.brewState.value.dryhops) { dryHop ->
                DryHop(brewViewModel, dryHop)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Divider()
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Bottled", modifier = Modifier.weight(1f))
            Checkbox(
                checked = brewViewModel.brewState.value.isBottled,
                onCheckedChange = {
                    brewViewModel.brewState.value.isBottled = it
                    brewViewModel.brewState.value.bottledDate =
                        DateTime.now().withTimeAtStartOfDay().millis
                    brewViewModel.brewState.value.drinkable =
                        DateTime.now().withTimeAtStartOfDay().plusDays(14).millis
                    brewViewModel.addBrew(brewViewModel.brewState.value)
                })
        }
        brewViewModel.brewState.value.bottledDate?.let {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Bottled: ",
                    fontWeight = FontWeight.Bold,
                )
                Text(text = brewViewModel.dateFormat.format(it))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Drinkable: ",
                fontWeight = FontWeight.Bold,
            )
            Text(text = brewViewModel.dateFormat.format(brewViewModel.brewState.value.drinkable))
        }
    }
}