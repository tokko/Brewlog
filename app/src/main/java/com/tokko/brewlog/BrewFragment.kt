package com.tokko.brewlog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.brew_fragment.*
import kotlinx.android.synthetic.main.dry_hop_item.view.dryHopDate
import kotlinx.android.synthetic.main.dry_hop_item.view.dryHopType
import kotlinx.android.synthetic.main.dry_hop_item.view.dryhopAmount
import kotlinx.android.synthetic.main.dry_hopped_item.view.*
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*

class BrewFragment(val firestoreRepository: IFirestoreRepository) : Fragment() {
    lateinit var brew: Brew
    val adapter = GroupAdapter<GroupieViewHolder>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.brew_fragment, null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val brewId = (savedInstanceState ?: arguments)?.getString("brewId") ?: ""
        firestoreRepository.getBrew(brewId) {
            brew = it
            initViews()
        }
    }

    private fun initViews() {
        brewName.text = brew.name
        brewDate.text = SimpleDateFormat("yyyy-MM-dd").format(Date(brew.brewDate))
        fermentationEndDate.text =
            SimpleDateFormat("yyyy-MM-dd").format(Date(brew.fermentationTime))
        dryHopRecycler.adapter = adapter
        dryHopRecycler.layoutManager = LinearLayoutManager(activity)
        adapter.addAll(brew.dryhops.map {
            DryHoppedItem(it) {
                firestoreRepository.addBrew(brew)
            }
        })
        adapter.notifyDataSetChanged()
        bottledCheckbox.setOnCheckedChangeListener { _, isChecked ->
            brew.isBottled = isChecked
            brew.bottledDate = DateTime.now().withTimeAtStartOfDay().millis
            firestoreRepository.addBrew(brew)
            updateDrinkableDate()
            bottledCheckbox.isEnabled = !isChecked
            firestoreRepository.addAlarm(
                Alarm(
                    brew.id,
                    DateTime.now().plusDays(14).millis,
                    "Drinkable brew!",
                    "${brew.name} is now drinkable!"
                ).also { brew.drinkableAlarmId = it.id })
            firestoreRepository.addBrew(brew)
        }
        bottledCheckbox.isChecked = brew.isBottled
        dryHopLabel.visibility = if (brew.dryhops.isNullOrEmpty()) GONE else VISIBLE
        updateDrinkableDate()
    }

    fun updateDrinkableDate() {
        if (brew.isBottled) {
            drinkableLabel.visibility = VISIBLE
            drinkableDate.visibility = VISIBLE
            bottledDateLabel.visibility = VISIBLE
            bottledDate.visibility = VISIBLE
            bottledDate.text =
                SimpleDateFormat("yyyy-MM-dd").format(DateTime(brew.bottledDate).millis)
            drinkableDate.text =
                SimpleDateFormat("yyyy-MM-dd").format(DateTime(brew.bottledDate).plusDays(14).millis)
        } else {
            drinkableLabel.visibility = GONE
            drinkableDate.visibility = GONE
            bottledDateLabel.visibility = GONE
            bottledDate.visibility = GONE
        }
    }

    companion object {
        fun newInstance(id: String, firestoreRepository: IFirestoreRepository) =
            BrewFragment(firestoreRepository).apply {
                arguments = Bundle().apply { putString("brewId", id) }
            }
    }
}

class DryHoppedItem(val dryHopping: DryHopping, val saveCallback: () -> Unit) : Item() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.dryhopAmount.text = "Amount: ${dryHopping.amount}g"
        viewHolder.itemView.dryHopDate.text =
            SimpleDateFormat("yyyy-MM-dd").format(Date(dryHopping.date))
        viewHolder.itemView.dryHopType.text = "Hop: ${dryHopping.type}"
        viewHolder.itemView.isDryhoppedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            dryHopping.checked = isChecked
            saveCallback()
        }
        viewHolder.itemView.isDryhoppedCheckBox.isChecked = dryHopping.checked
    }

    override fun getLayout() = R.layout.dry_hopped_item

}