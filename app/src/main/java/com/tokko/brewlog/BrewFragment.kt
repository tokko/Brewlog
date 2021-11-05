package com.tokko.brewlog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import org.joda.time.DateTime
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.text.SimpleDateFormat
import java.util.*

class BrewFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val fireStoreRepository: IFirestoreRepository by instance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("sv-SE"))
    private val brewState = mutableStateOf(Brew())
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {
                    Text(
                        text = brewState.value.name,
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
                        Text(text = dateFormat.format(brewState.value.brewDate))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Fermentation end date: ",
                            fontWeight = FontWeight.Bold,
                        )
                        Text(text = dateFormat.format(brewState.value.fermentationTime))
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
                        items(brewState.value.dryhops) { dryHop ->
                            DryHop(dryHop)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Bottled", modifier = Modifier.weight(1f))
                        val state = remember { mutableStateOf(brewState.value.isBottled) }
                        Checkbox(
                            checked = brewState.value.isBottled,
                            onCheckedChange = {
                                brewState.value.isBottled = it
                                brewState.value.bottledDate =
                                    DateTime.now().withTimeAtStartOfDay().millis
                                brewState.value.drinkable =
                                    DateTime.now().withTimeAtStartOfDay().plusDays(14).millis
                                fireStoreRepository.addBrew(brewState.value)
                            })
                    }
                    brewState.value.bottledDate?.let {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Bottled: ",
                                fontWeight = FontWeight.Bold,
                            )
                            Text(text = dateFormat.format(it))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Drinkable: ",
                            fontWeight = FontWeight.Bold,
                        )
                        Text(text = dateFormat.format(brewState.value.drinkable))
                    }
                }
            }
        }
    }

    @Composable
    private fun DryHop(dryHop: DryHopping) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = dateFormat.format(dryHop.date), modifier = Modifier.weight(1f))
            Text(text = "Hop: ${dryHop.type}", modifier = Modifier.weight(1f))
            Text(text = "Amount: ${dryHop.amount}g", modifier = Modifier.weight(1f))
            val state = remember { mutableStateOf(dryHop.checked) }
            Checkbox(checked = state.value, onCheckedChange = {
                dryHop.checked = it
                state.value = it
                fireStoreRepository.addBrew(brewState.value)
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val brewId = (savedInstanceState ?: arguments)?.getString("brewId") ?: ""
        fireStoreRepository.getBrew(brewId) {
            brewState.value = it
        }
    }

    companion object {
        fun newInstance(id: String) =
            BrewFragment().apply {
                arguments = Bundle().apply { putString("brewId", id) }
            }
    }
}