package com.tokko.brewlog

import android.os.Parcelable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*

class BrewFormViewModel(val brewService: IBrewService) : ViewModel() {
    val yearDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("sv-SE"))
    val brewState = mutableStateOf(Brew())
}

@ExperimentalComposeUiApi
@Composable
fun BrewFormScreen(
    brewFormViewModel: BrewFormViewModel,
    onWortBoilDesign: () -> Unit,
    onAddBrew: () -> Unit
) {
    val (first, second, third) = FocusRequester.createRefs()
    val nameState = remember { mutableStateOf(brewFormViewModel.brewState.value.name) }
    val instructionsState =
        remember { mutableStateOf(brewFormViewModel.brewState.value.instructions) }
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester = first),
            keyboardActions = KeyboardActions(onAny = { second.requestFocus() }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            value = nameState.value,
            onValueChange = {
                nameState.value = it
            },
            label = { Text("Brew name") },
        )
        DateWithLabel(
            brewFormViewModel = brewFormViewModel,
            label = "Brew date: ",
            date = brewFormViewModel.brewState.value.brewDate
        )
        DateWithLabel(
            brewFormViewModel = brewFormViewModel,
            label = "Fermentation end date: ",
            date = brewFormViewModel.brewState.value.fermentationTime
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester = second),
            label = { Text(text = "Instructions") },
            value = instructionsState.value,
            onValueChange = { instructionsState.value = it }
        )
        val dryhopState = remember { mutableStateListOf<DryHopping>() }
        if (FEATURE_WORT_BOIL)
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                brewFormViewModel.brewState.value.name = nameState.value
                brewFormViewModel.brewState.value.dryhops = dryhopState
                brewFormViewModel.brewState.value.instructions = instructionsState.value
                onWortBoilDesign()
            }) {
                Text(text = "Design wort boil")
            }
        Text(text = "Dry hops:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        DryhopList(list = dryhopState)
        if (dryhopState.isNotEmpty())
            Spacer(modifier = Modifier.height(2.dp))
        DryhopInput(first = third, state = dryhopState)
        Button(
            onClick = {
                brewFormViewModel.brewState.value.name = nameState.value
                brewFormViewModel.brewState.value.dryhops = dryhopState
                brewFormViewModel.brewState.value.instructions = instructionsState.value
                brewFormViewModel.brewService.createBrew(brewFormViewModel.brewState.value)
                onAddBrew()
                brewFormViewModel.brewState.value = Brew()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = brewFormViewModel.brewState.value.name.isNotBlank()
        ) {
            Text(text = "Add brew")
        }
    }
}


@ExperimentalComposeUiApi
@Composable
fun WortBoilDesigner(brewFormViewModel: BrewFormViewModel) {
    val (first, second) = FocusRequester.createRefs()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.dp)
    ) {
        brewFormViewModel.brewState.value.wortBoil.forEach {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = it.minutes.toString())
                Text(text = it.action)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val minutesState = remember { mutableStateOf("") }
            val actionState = remember { mutableStateOf("") }
            val onAdd = {
                brewFormViewModel.brewState.value.wortBoil.add(
                    WortAction(
                        isOffset = minutesState.value.contains("+"),
                        minutes = minutesState.value.replace("+", "").toInt(),
                        action = actionState.value
                    )
                )
                minutesState.value = ""
                actionState.value = ""
            }
            OutlinedTextField(modifier = Modifier
                .weight(0.2f)
                .focusRequester(focusRequester = first),
                value = minutesState.value,
                label = { Text(text = "Time") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onAny = { second.requestFocus() }),
                onValueChange = { minutesState.value = it })
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester = second),
                label = { Text(text = "Action") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onAny = { onAdd(); first.requestFocus() }),
                value = actionState.value, onValueChange = { actionState.value = it })
        }
    }
}

@Composable
private fun DryhopList(list: SnapshotStateList<DryHopping>) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(list) { dryHop ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = dryHop.type, fontWeight = FontWeight.Bold)
                Text(text = "${dryHop.amount}g")
                Text(text = SimpleDateFormat("yyyy-MM-dd", Locale("sv")).format(dryHop.date))
            }

        }
    }
}

@ExperimentalComposeUiApi
@Composable
private fun DryhopInput(
    first: FocusRequester = FocusRequester(),
    state: SnapshotStateList<DryHopping>
) {
    val dateState = remember { mutableStateOf("") }
    val typeState = remember { mutableStateOf("") }
    val amountState = remember { mutableStateOf("") }
    val (second, third) = FocusRequester.createRefs()
    val onAdd = {
        state.add(
            DryHopping(
                DateTime.now().withTimeAtStartOfDay()
                    .plusDays(dateState.value.toInt()).millis,
                typeState.value,
                amountState.value.toInt()
            )
        )
        dateState.value = ""
        typeState.value = ""
        amountState.value = ""
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                modifier = Modifier
                    .weight(2f)
                    .focusRequester(focusRequester = first),
                value = dateState.value,
                keyboardActions = KeyboardActions(onAny = { second.requestFocus() }),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                onValueChange = { dateState.value = it },
                label = { Text("Day of fermentation") },
            )
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester = second),
                value = typeState.value,
                keyboardActions = KeyboardActions(onAny = { third.requestFocus() }),
                keyboardOptions = KeyboardOptions(autoCorrect = false, imeAction = ImeAction.Next),
                onValueChange = { typeState.value = it },
                label = { Text("Hop") },
            )

            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester = third),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onAny = {
                    onAdd()
                    first.requestFocus()
                }),
                value = amountState.value,
                onValueChange = { amountState.value = it },
                label = { Text("Grams") },
            )
        }
        /*
        Spacer(Modifier.height(2.dp))
        Button(
            onClick = {
                onAdd()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = dateState.value.isNotBlank() && typeState.value.isNotBlank() && amountState.value.isNotBlank()
        ) {
            Text(text = "Add dryhop")
        }
        Spacer(Modifier.height(2.dp))
         */
    }

}

@Composable
private fun DateWithLabel(brewFormViewModel: BrewFormViewModel, label: String, date: Long) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                text = brewFormViewModel.yearDateFormat.format(date),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Divider(modifier = Modifier.height(2.dp))
    }
}

@Parcelize
data class DryHopping(
    var date: Long = 0,
    var type: String = "",
    var amount: Int = 0,
    var checked: Boolean = false,
    var alarmId: String = ""
) : Parcelable

@Parcelize
class WortAction(var minutes: Int = 0, var action: String = "", var isOffset: Boolean = true) :
    Parcelable

@Parcelize
class Brew(
    var name: String = "",
    var brewDate: Long = DateTime.now().millis,
    var dryhops: MutableList<DryHopping> = mutableListOf(),
    var fermentationTime: Long = DateTime.now().plusDays(14).millis,
    var drinkable: Long = Long.MAX_VALUE,
    var drinkableAlarmId: String = "",
    var isBottled: Boolean = false,
    var bottledAlarmId: String = "",
    var bottledDate: Long? = null,
    var instructions: String = "",
    var id: String = UUID.randomUUID().toString(),
    var wortBoil: MutableList<WortAction> = mutableListOf()
) : Parcelable