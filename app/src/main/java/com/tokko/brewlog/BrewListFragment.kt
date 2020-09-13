package com.tokko.brewlog

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.brew_list_fragment.*
import kotlinx.android.synthetic.main.mock_item.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class BrewListFragment : Fragment(), KodeinAware {
    private val adapter = GroupAdapter<GroupieViewHolder>()
    val firestoreRepository: IFirestoreRepository by instance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.brew_list_fragment, container, false)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.brew_list_menu, menu)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        brewListRecycler.adapter = adapter
        brewListRecycler.layoutManager = LinearLayoutManager(activity)
        firestoreRepository.getBrews {
            adapter.clear()
            it.forEach { adapter.add(StringItem(it.name)) }
            adapter.notifyDataSetChanged()
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        (activity as MainActivity).showAddBrewFragment()
        return true
    }

    override val kodein by kodein()

}


class StringItem(val name: String) : Item() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.mockText.text = name
    }

    override fun getLayout() = R.layout.mock_item

}