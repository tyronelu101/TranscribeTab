package com.tlinq.transcribetab.tab

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.tlinq.transcribetab.R
import com.tlinq.transcribetab.database.Tablature
import com.tlinq.transcribetab.database.TablatureDatabase
import com.tlinq.transcribetab.databinding.FragmentTabBinding
import com.tlinq.transcribetab.mediaplayer.MediaPlayerCallback
import com.tlinq.transcribetab.mediaplayer.MediaPlayerFragment

class TabFragment : Fragment(),
    MediaPlayerCallback {

    private var _binding: FragmentTabBinding? = null
    private val binding get() = _binding!!

    private lateinit var tabViewModel: TabViewModel
    private var mediaPlayerFragment: MediaPlayerFragment = MediaPlayerFragment(this)

    private lateinit var tablature: Tablature
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = arguments

        tablature = requireNotNull(bundle?.getParcelable("tab"))

        val application = requireNotNull(this.activity).application
        tablature.let {
            val viewModelFactory = TabViewModelFactory(it, TablatureDatabase.getInstance(application))
            tabViewModel =
                ViewModelProvider(this, viewModelFactory).get(TabViewModel::class.java)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab, container, false)
        binding.viewModel = tabViewModel
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tab = arguments?.getParcelable<Tablature>("tab")

        var songUri = ""
        if (tab != null) {
            songUri = tab.songUri
        } else {
            Toast.makeText(context, "This tab does not have a tab object", Toast.LENGTH_SHORT)
                .show()
        }

        val mediaArgs = Bundle()
        mediaArgs.putString("songUri", songUri)
        mediaPlayerFragment.arguments = mediaArgs

        //Child fragment manager handles child fragment lifecycle
        //Future not to self: Don't use fragmentManager(for activities) when adding fragment inside a fragment
        childFragmentManager.beginTransaction().apply {
            replace(R.id.tab_media_player, mediaPlayerFragment)
            commit()
        }

        tabViewModel.tab.observe(viewLifecycleOwner, Observer {
            tabViewModel.initializeTablature()
        })

        tabViewModel.topSection.observe(viewLifecycleOwner, Observer {
            binding.tabSection1.updateTablature(it.sectionCol)
            binding.tabSection1Num.text = it.sectionNum.toString()
        })

        tabViewModel.bottomSection.observe(viewLifecycleOwner, Observer {
            binding.tabSection2.updateTablature(it.sectionCol)
            binding.tabSection2Num.text = it.sectionNum.toString()

        })

        tabViewModel.sectionUpdateTime.observe(viewLifecycleOwner, Observer {
            mediaPlayerFragment.setTriggerTime(it)
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.tab_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_tab_option -> {
                findNavController().navigate(
                    TabFragmentDirections.actionTabFragmentToEditTabFragment(
                        tablature.songUri,
                        tablature
                    )
                )
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun trigger() {
        tabViewModel.updateSection()
    }

    //time is retrieved from mediaplayerfragment.
    //Check to see what section we have to jump to
    override fun triggerAt(time: Int) {
        tabViewModel.updateSectionTo(time)
    }

}
