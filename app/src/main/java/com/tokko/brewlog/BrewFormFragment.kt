package com.tokko.brewlog

import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.brew_form_fragment.*
import kotlinx.android.synthetic.main.dry_hop_item.view.*
import org.joda.time.DateTime
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.text.SimpleDateFormat
import java.util.*

class BrewFormFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    val firestoreRepository: IFirestoreRepository by instance()
    val brewService: IBrewService by instance()
    lateinit var brew: Brew
    val dryHopAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.brew_form_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dryHopRecycler.adapter = dryHopAdapter
        dryHopRecycler.layoutManager = LinearLayoutManager(activity)
        brew = (savedInstanceState ?: arguments)?.getParcelable<Brew>("brew") ?: Brew()
        initViews()


    }

    fun initViews() {
        brewName.setText(brew.name)
        brewDate.text = SimpleDateFormat("yyyy-MM-dd").format(Date(brew.brewDate))
        fermentationEndDate.text =
            SimpleDateFormat("yyyy-MM-dd").format(Date(brew.fermentationTime))
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
        dryHopAddAmount.addTextChangedListener(textWatcher)
        dryHopAddDate.addTextChangedListener(textWatcher)
        dryHopAddHopType.addTextChangedListener(textWatcher)
        brewName.addTextChangedListener(textWatcher)
        validateDryHopButton()

        dryHopAddButton.setOnClickListener {
            val dryHop = DryHopping(
                DateTime(brew.brewDate).plusDays(
                    dryHopAddDate.text.toString().toInt()
                ).millis,
                dryHopAddHopType.text.toString(),
                dryHopAddAmount.text.toString().toInt()
            )
            brew.dryhops.add(dryHop)
            dryHopAdapter.add(
                DryHopItem(
                    dryHop
                )
            )
            dryHopAddAmount.text.clear()
            dryHopAddDate.text.clear()
            dryHopAddHopType.text.clear()
            dryHopAdapter.notifyDataSetChanged()
        }
        addBrewButton.setOnClickListener {
            brew.apply {
                name = brewName.text.toString()
            }
            brewService.createBrew(brew)
            (activity as MainActivity).showBrewListFragment()

        }
    }

    fun validateBrewButton() {
        addBrewButton.isEnabled = brewName.text.isNotBlank()
    }

    fun validateDryHopButton() {
        dryHopAddButton.isEnabled =
            dryHopAddDate.text.isNotBlank() && dryHopAddAmount.text.isNotEmpty() && dryHopAddHopType.text.isNotEmpty()
    }

}

class DryHopItem(val dryHopping: DryHopping) : Item() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.dryhopAmount.text = "Amount: ${dryHopping.amount}g"
        viewHolder.itemView.dryHopDate.text =
            SimpleDateFormat("yyyy-MM-dd").format(Date(dryHopping.date))
        viewHolder.itemView.dryHopType.text = "Hop: ${dryHopping.type}"
    }

    override fun getLayout() = R.layout.dry_hop_item

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