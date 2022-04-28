package com.tokko.brewlog

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.text.SimpleDateFormat
import java.util.*

class BrewFormFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val brewService: IBrewService by instance()
    private val yearDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("sv-SE"))

    private val brewState = mutableStateOf(Brew())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                BrewLogTheme {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        val s = remember { mutableStateOf("") }
                        TextField(
                            value = s.value,
                            onValueChange = {
                                s.value = it
                            },
                            label = { Text("Brew name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DateWithLabel(label = "Brew date: ", date = brewState.value.brewDate)
                        DateWithLabel(
                            label = "Fermentation end date: ",
                            date = brewState.value.fermentationTime
                        )
                        Text(text = "Dry hops:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        val dryhopState = remember { mutableStateListOf<DryHopping>() }
                        DryhopList(list = dryhopState)
                        Spacer(modifier = Modifier.height(2.dp))
                        DryhopInput(state = dryhopState)
                        Button(
                            onClick = {
                                brewState.value.name = s.value
                                brewState.value.dryhops = dryhopState
                                brewService.createBrew(brewState.value)
                                (activity as MainActivity).showBrewListFragment()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = s.value.isNotBlank()
                        ) {
                            Text(text = "Add brew")
                        }
                    }

                }
            }
        }
    }


    @Composable
    private fun DryhopList(list: SnapshotStateList<DryHopping>) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(list) { dryHop ->
                Text(text = dryHop.type)

            }
        }
    }

    @Composable
    private fun DryhopInput(state: SnapshotStateList<DryHopping>) {
        val dateState = remember { mutableStateOf("") }
        val typeState = remember { mutableStateOf("") }
        val amountState = remember { mutableStateOf("") }
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = dateState.value,
                    onValueChange = { dateState.value = it },
                    label = { Text("Day of fermentation") },
                    modifier = Modifier.weight(2f)
                )
                TextField(
                    value = typeState.value,
                    onValueChange = { typeState.value = it },
                    label = { Text("Hop") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = amountState.value,
                    onValueChange = { amountState.value = it },
                    label = { Text("Gram") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(2.dp))
            Button(
                onClick = {
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
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = dateState.value.isNotBlank() && typeState.value.isNotBlank() && amountState.value.isNotBlank()
            ) {
                Text("Add dryhop")
            }
            Spacer(Modifier.height(2.dp))
        }

    }

    @Composable
    private fun DateWithLabel(label: String, date: Long) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = yearDateFormat.format(date),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Divider(modifier = Modifier.height(2.dp))
        }
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
    var notes: String = "",
    var id: String = UUID.randomUUID().toString()
) : Parcelable