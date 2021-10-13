package com.tokko.brewlog

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BrewFormFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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