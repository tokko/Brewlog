package com.tokko.brewlog

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.tokko.brewlog.databinding.BrewItemBinding
import com.tokko.brewlog.databinding.BrewListFragmentBinding
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.viewbinding.BindableItem
import org.joda.time.DateTime
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class BrewListFragment : Fragment(), KodeinAware {
    private val adapter = GroupAdapter<GroupieViewHolder>()
    val firestoreRepository: IFirestoreRepository by instance()

    private var _binding: BrewListFragmentBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BrewListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.brew_list_menu, menu)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding.brewListRecycler.adapter = adapter
        binding.brewListRecycler.layoutManager = LinearLayoutManager(activity)
        firestoreRepository.getBrews {
            if (activity != null) {
                adapter.clear()
                it.sortedByDescending { if (it.hasAction()) Long.MAX_VALUE else it.brewDate }
                    .forEach { adapter.add(Brewitem(it, activity as MainActivity)) }
                adapter.notifyDataSetChanged()
            }
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        (activity as MainActivity).showAddBrewFragment()
        return true
    }

    override val kodein by kodein()

}

fun Brew.hasAction() =
    DateTime(this.fermentationTime).withTimeAtStartOfDay().isBeforeNow && !this.isBottled || this.dryhops.any {
        !it.checked && DateTime(
            it.date
        ).withTimeAtStartOfDay().isBeforeNow
    }

class Brewitem(val brew: Brew, val activity: MainActivity) : BindableItem<BrewItemBinding>() {
    override fun getLayout() = R.layout.brew_item
    override fun bind(viewBinding: BrewItemBinding, position: Int) {
        viewBinding.mockText.text = brew.name
        viewBinding.mockText.setTextColor(
            when {
                brew.hasAction() -> Color.RED
                DateTime(brew.drinkable).withTimeAtStartOfDay().isBeforeNow -> Color.GREEN
                else -> Color.BLACK
            }
        )
        viewBinding.root.setOnClickListener {
            activity.showBrewFragment(brew.id)
        }

    }

    override fun initializeViewBinding(view: View) = BrewItemBinding.bind(view)

}