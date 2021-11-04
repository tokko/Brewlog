package com.tokko.brewlog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.tokko.brewlog.databinding.BrewFragmentBinding
import com.tokko.brewlog.databinding.DryHoppedItemBinding
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.viewbinding.BindableItem
import org.joda.time.DateTime
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.text.SimpleDateFormat
import java.util.*

class BrewFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val fireStoreRepository: IFirestoreRepository by instance()
    lateinit var brew: Brew
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private var _binding: BrewFragmentBinding? = null
    private val binding get() = _binding!!
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
                            checked = state.value,
                            onCheckedChange = {
                                state.value = it
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

    private fun initViews() {
        binding.brewName.text = brew.name
        binding.brewDate.text = dateFormat.format(Date(brew.brewDate))
        binding.fermentationEndDate.text = dateFormat.format(Date(brew.fermentationTime))
        binding.dryHopRecycler.adapter = adapter
        binding.dryHopRecycler.layoutManager = LinearLayoutManager(activity)
        adapter.addAll(brew.dryhops.map {
            DryHoppedItem(it) {
                fireStoreRepository.addBrew(brew)
            }
        })
        adapter.notifyItemRangeChanged(0, adapter.groupCount)
        binding.bottledCheckbox.isChecked = brew.isBottled
        binding.bottledCheckbox.isEnabled = !brew.isBottled
        binding.bottledCheckbox.setOnCheckedChangeListener { _, isChecked ->
            brew.isBottled = isChecked
            brew.bottledDate = DateTime.now().withTimeAtStartOfDay().millis
            fireStoreRepository.addBrew(brew)
            updateDrinkableDate()
            binding.bottledCheckbox.isEnabled = !isChecked
            brew.drinkable = DateTime.now().plusDays(14).millis
            fireStoreRepository.addAlarm(
                Alarm(
                    brew.id,
                    DateTime.now().plusDays(14).millis,
                    "Drinkable brew!",
                    "${brew.name} is now drinkable!"
                ).also { brew.drinkableAlarmId = it.id })
            fireStoreRepository.addBrew(brew)
        }
        binding.dryHopLabel.visibility = if (brew.dryhops.isNullOrEmpty()) GONE else VISIBLE
        updateDrinkableDate()
    }

    fun updateDrinkableDate() {
        if (brew.isBottled) {
            binding.drinkableLabel.visibility = VISIBLE
            binding.drinkableDate.visibility = VISIBLE
            binding.bottledDateLabel.visibility = VISIBLE
            binding.bottledDate.visibility = VISIBLE
            binding.bottledDate.text = dateFormat.format(DateTime(brew.bottledDate).millis)
            binding.drinkableDate.text =
                dateFormat.format(DateTime(brew.bottledDate).plusDays(14).millis)
        } else {
            binding.drinkableLabel.visibility = GONE
            binding.drinkableDate.visibility = GONE
            binding.bottledDateLabel.visibility = GONE
            binding.bottledDate.visibility = GONE
        }
    }

    companion object {
        fun newInstance(id: String) =
            BrewFragment().apply {
                arguments = Bundle().apply { putString("brewId", id) }
            }
    }
}

class DryHoppedItem(val dryHopping: DryHopping, val saveCallback: () -> Unit) :
    BindableItem<DryHoppedItemBinding>() {
    val datePattern = SimpleDateFormat("yyyy-MM-dd", Locale("sv-SE"))
    override fun initializeViewBinding(view: View): DryHoppedItemBinding {
        return DryHoppedItemBinding.bind(view)
    }

    @SuppressLint("SetTextI18n")
    override fun bind(viewBinding: DryHoppedItemBinding, position: Int) {
        viewBinding.dryhopAmount.text = "Amount: ${dryHopping.amount}g"
        viewBinding.dryHopDate.text = datePattern.format(Date(dryHopping.date))
        viewBinding.dryHopType.text = "Hop: ${dryHopping.type}"
        viewBinding.isDryhoppedCheckBox.isChecked = dryHopping.checked
        viewBinding.isDryhoppedCheckBox.isEnabled = !dryHopping.checked

        viewBinding.isDryhoppedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            dryHopping.checked = isChecked
            saveCallback()
        }
    }

    override fun getLayout() = R.layout.dry_hopped_item

}