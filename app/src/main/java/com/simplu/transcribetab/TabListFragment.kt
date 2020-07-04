package com.simplu.transcribetab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.simplu.transcribetab.databinding.FragmentTabListBinding

class TabListFragment : Fragment() {

    private lateinit var binding: FragmentTabListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_list, container, false)
        binding.addTabBtn.setOnClickListener {view ->
            view.findNavController().navigate(R.id.action_tabListFragment_to_songListFragment)
        }


        return binding.root
    }


}
