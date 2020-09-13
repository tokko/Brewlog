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
import org.greenrobot.eventbus.EventBus

class BrewListFragment : Fragment() {
    private val adapter = GroupAdapter<GroupieViewHolder>()
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
        (0..10).forEach { adapter.add(MockItem(it)) }
        adapter.notifyDataSetChanged()


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        EventBus.getDefault().post(MainActivity.EventShowAddBrewFragment())
        return true
    }
}


class MockItem(val n: Int) : Item() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.mockText.text = "Text $n"
    }

    override fun getLayout() = R.layout.mock_item

}