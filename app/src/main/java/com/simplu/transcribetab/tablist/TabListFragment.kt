package com.simplu.transcribetab.tablist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.simplu.transcribetab.R
import com.simplu.transcribetab.database.TablatureDatabase
import com.simplu.transcribetab.databinding.FragmentTabListBinding


class TabListFragment : Fragment() {

    private lateinit var binding: FragmentTabListBinding
    private lateinit var tabListViewModel: TabListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application = requireNotNull(this.activity).application
        val dataSource = TablatureDatabase.getInstance(application).tablatureDatabaseDao
        val viewModelFactory =
            TabListViewModelFactory(dataSource)
        tabListViewModel =
            ViewModelProvider(this, viewModelFactory).get(TabListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_tab_list, container, false)
        binding.addTabBtn.setOnClickListener { view ->
            view.findNavController().navigate(R.id.action_tabListFragment_to_songListFragment)
        }

        val adapter = TablatureAdapter(TablatureListener {tab ->
            findNavController().navigate(TabListFragmentDirections.actionTabListFragmentToTabFragment(tab))
        })

        binding.tabList.adapter = adapter
        binding.tabList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        tabListViewModel.tabs.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        return binding.root
    }


}
