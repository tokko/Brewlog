package com.tokko.brewlog

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.tokko.brewlog.databinding.BrewFormFragmentBinding
import com.tokko.brewlog.databinding.DryHopItemBinding
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.text.SimpleDateFormat
import java.util.*

class BrewFormFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    val brewService: IBrewService by instance()
    lateinit var brew: Brew
    val dryHopAdapter = GroupAdapter<GroupieViewHolder>()
    private var _binding: BrewFormFragmentBinding? = null
    val binding get() = _binding!!
    val yearDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("sv-SE"))

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
                    TextField(
                        placeholder = { Text("Amarillo IPA") },
                        value = brewState.value.name,
                        onValueChange = {
                            brewState.value.name = it
                        },
                        label = { Text("Brew name") }
                    )
                    DateWithLabel(label = "Brew date:", date = brewState.value.brewDate)
                    DateWithLabel(
                        label = "Fermentation end date:",
                        date = brewState.value.fermentationTime
                    )
                    Text(text = "Dry hops:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    val dryhopState = remember { mutableStateListOf<DryHopping>() }
                    DryhopList(list = dryhopState)
                    Spacer(modifier = Modifier.height(2.dp))
                    DryhopInput(state = dryhopState)
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
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = typeState.value,
                    onValueChange = { typeState.value = it },
                    label = { Text("Hop type") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = amountState.value,
                    onValueChange = { amountState.value = it },
                    label = { Text("Amoung (g)") },
                    modifier = Modifier.weight(1f)
                )
            }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        return
        binding.dryHopRecycler.adapter = dryHopAdapter
        binding.dryHopRecycler.layoutManager = LinearLayoutManager(activity)
        brew = (savedInstanceState ?: arguments)?.getParcelable<Brew>("brew") ?: Brew()
        initViews()


    }

    fun initViews() {
        binding.brewName.setText(brew.name)
        binding.brewDate.text = yearDateFormat.format(Date(brew.brewDate))
        binding.fermentationEndDate.text = yearDateFormat.format(Date(brew.fermentationTime))
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateDryHopButton()
                validateBrewButton()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        }
        binding.dryHopAddAmount.addTextChangedListener(textWatcher)
        binding.dryHopAddDate.addTextChangedListener(textWatcher)
        binding.dryHopAddHopType.addTextChangedListener(textWatcher)
        binding.brewName.addTextChangedListener(textWatcher)
        validateDryHopButton()

        binding.dryHopAddButton.setOnClickListener {
            val dryHop = DryHopping(
                DateTime(brew.brewDate).plusDays(
                    binding.dryHopAddDate.text.toString().toInt()
                ).millis,
                binding.dryHopAddHopType.text.toString(),
                binding.dryHopAddAmount.text.toString().toInt()
            )
            brew.dryhops.add(dryHop)
            dryHopAdapter.add(
                DryHopItem(
                    dryHop
                )
            )
            binding.dryHopAddAmount.text.clear()
            binding.dryHopAddDate.text.clear()
            binding.dryHopAddHopType.text.clear()
            dryHopAdapter.notifyItemInserted(dryHopAdapter.groupCount - 1)
        }
        binding.addBrewButton.setOnClickListener {
            brew.apply {
                name = binding.brewName.text.toString()
            }
            brewService.createBrew(brew)
            (activity as MainActivity).showBrewListFragment()

        }
    }

    fun validateBrewButton() {
        binding.addBrewButton.isEnabled = binding.brewName.text.isNotBlank()
    }

    fun validateDryHopButton() {
        binding.dryHopAddButton.isEnabled =
            binding.dryHopAddDate.text.isNotBlank() && binding.dryHopAddAmount.text.isNotEmpty() && binding.dryHopAddHopType.text.isNotEmpty()
    }

}

class DryHopItem(val dryHopping: DryHopping) : BindableItem<DryHopItemBinding>() {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("sv-SE"))
    override fun getLayout() = R.layout.dry_hop_item

    @SuppressLint("SetTextI18n")
    override fun bind(viewBinding: DryHopItemBinding, position: Int) {
        viewBinding.dryhopAmount.text = "Amount: ${dryHopping.amount}g"
        viewBinding.dryHopDate.text =
            dateFormat.format(Date(dryHopping.date))
        viewBinding.dryHopType.text = "Hop: ${dryHopping.type}"

    }

    override fun initializeViewBinding(view: View) = DryHopItemBinding.bind(view)
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