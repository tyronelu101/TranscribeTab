package com.simplu.transcribetab.tablist

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.simplu.transcribetab.R
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureDatabase
import com.simplu.transcribetab.database.TablatureRepository
import com.simplu.transcribetab.databinding.FragmentTabListBinding


class TabListFragment : Fragment() {

    private lateinit var binding: FragmentTabListBinding
    private lateinit var tabListViewModel: TabListViewModel
    private var tablatureSelected: Tablature? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application = requireNotNull(this.activity).application
        val repository = TablatureRepository(TablatureDatabase.getInstance(application))
        val viewModelFactory =
            TabListViewModelFactory(repository)
        tabListViewModel =
            ViewModelProvider(this, viewModelFactory).get(TabListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_tab_list, container, false
        )
        binding.addTabBtn.setOnClickListener { view ->
            view.findNavController().navigate(R.id.action_tabListFragment_to_songListFragment)
        }

        val adapter = TablatureAdapter(TabClickListener { tab ->
            findNavController().navigate(
                TabListFragmentDirections.actionTabListFragmentToTabFragment(
                    tab
                )
            )
        },
            TabItemContextMenuListener { tab ->
                tablatureSelected = tab
                false
            }
        )

        binding.tabList.adapter = adapter
        binding.tabList.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        registerForContextMenu(binding.tabList)

        tabListViewModel.tabList.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })


        return binding.root
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        val inflater = MenuInflater(v.context)
        inflater.inflate(R.menu.tab_item_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            //todo tablist to edit tab
            R.id.context_tab_item_edit -> {
                true
            }

            R.id.context_tab_item_delete -> {
                //Remove the selected match from the database
                tablatureSelected?.let {
                    Toast.makeText(context, "Delete tab ${it.tabId}", Toast.LENGTH_SHORT).show()
                    tabListViewModel.deleteTab(it)
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }

    }
}
