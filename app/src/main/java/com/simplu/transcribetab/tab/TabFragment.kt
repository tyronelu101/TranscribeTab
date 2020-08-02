package com.simplu.transcribetab.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simplu.transcribetab.R
import com.simplu.transcribetab.database.TablatureDatabase
import com.simplu.transcribetab.databinding.FragmentTabBinding

class TabFragment : Fragment() {

    private lateinit var viewModel: TabViewModel
    private lateinit var binding: FragmentTabBinding
    private lateinit var tabView: TablatureView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tabId = TabFragmentArgs.fromBundle(
            arguments!!
        ).tabId

        val dataSource =
            TablatureDatabase.getInstance(requireNotNull(this.activity).application).tablatureDatabaseDao

        val viewModelFactory = TabViewModelFactory(dataSource, tabId)

        viewModel =
            ViewModelProvider(this, viewModelFactory).get(TabViewModel::class.java)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab, container, false)

        viewModel.tablature.observe(this, Observer {
            binding.title.text = it.title
            binding.artist.text = it.artist
            binding.arranger.text = it.arranger
            binding.tuning.text = it.tuning
            tabView = TablatureView(it.columns, activity!!.applicationContext)
            binding.tablatureContainer.addView(tabView)

        })


        // Inflate the layout for this fragment
        return binding.root
    }

}
