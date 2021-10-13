package com.tokko.brewlog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.tokko.brewlog.databinding.BrewFragmentBinding
import com.tokko.brewlog.databinding.DryHoppedItemBinding
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.viewbinding.BindableItem
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*

class BrewFragment(private val firestoreRepository: IFirestoreRepository) : Fragment() {
    lateinit var brew: Brew
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private var _binding: BrewFragmentBinding? = null
    private val binding get() = _binding!!
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("sv-SE"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BrewFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val brewId = (savedInstanceState ?: arguments)?.getString("brewId") ?: ""
        firestoreRepository.getBrew(brewId) {
            brew = it
            initViews()
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
                firestoreRepository.addBrew(brew)
            }
        })
        adapter.notifyItemRangeChanged(0, adapter.groupCount)
        binding.bottledCheckbox.isChecked = brew.isBottled
        binding.bottledCheckbox.isEnabled = !brew.isBottled
        binding.bottledCheckbox.setOnCheckedChangeListener { _, isChecked ->
            brew.isBottled = isChecked
            brew.bottledDate = DateTime.now().withTimeAtStartOfDay().millis
            firestoreRepository.addBrew(brew)
            updateDrinkableDate()
            binding.bottledCheckbox.isEnabled = !isChecked
            brew.drinkable = DateTime.now().plusDays(14).millis
            firestoreRepository.addAlarm(
                Alarm(
                    brew.id,
                    DateTime.now().plusDays(14).millis,
                    "Drinkable brew!",
                    "${brew.name} is now drinkable!"
                ).also { brew.drinkableAlarmId = it.id })
            firestoreRepository.addBrew(brew)
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
        fun newInstance(id: String, firestoreRepository: IFirestoreRepository) =
            BrewFragment(firestoreRepository).apply {
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