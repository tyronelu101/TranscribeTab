package com.tlinq.transcribetab.tablist

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.tlinq.transcribetab.R
import com.tlinq.transcribetab.database.Tablature
import com.tlinq.transcribetab.database.TablatureDatabase
import com.tlinq.transcribetab.database.TablatureRepository
import com.tlinq.transcribetab.databinding.FragmentTabListBinding


class TabListFragment : Fragment() {

    private var _binding: FragmentTabListBinding? = null
    private val binding get() = _binding!!

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

        _binding = DataBindingUtil.inflate(
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
        setHasOptionsMenu(true)

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

            R.id.context_tab_item_edit -> {
                tablatureSelected?.let {
                    findNavController().navigate(
                        TabListFragmentDirections.actionTabListFragmentToEditTabFragment(
                            it.songUri, it
                        )
                    )
                }
                true
            }

            R.id.context_tab_item_delete -> {
                //Remove the selected match from the database
                tablatureSelected?.let {
                    tabListViewModel.deleteTab(it)
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.tab_list_menu, menu)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.settings -> {
                findNavController().navigate(TabListFragmentDirections.actionTabListFragmentToSettingsFragment())
            }

        }
        return super.onOptionsItemSelected(item)
    }
}
