package com.simplu.transcribetab.tab

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.simplu.transcribetab.R
import com.simplu.transcribetab.databinding.FragmentTabBinding
import com.simplu.transcribetab.mediaplayer.MediaPlayerFragment
import kotlinx.android.synthetic.main.fragment_tab.*

class TabFragment : Fragment(),
    SectionUpdater {

    private lateinit var tabViewModel: TabViewModel
    private lateinit var binding: FragmentTabBinding
    private lateinit var mediaPlayerFragment: MediaPlayerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tablature = TabFragmentArgs.fromBundle(
            arguments!!
        ).tablature
        val viewModelFactory = TabViewModelFactory(tablature)

        tabViewModel =
            ViewModelProvider(this, viewModelFactory).get(TabViewModel::class.java)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val tablature = TabFragmentArgs.fromBundle(
            arguments!!
        ).tablature

        tablature.let {
            initMediaPlayerFragment(it.songUri)
        }


        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab, container, false)
        binding.viewModel = tabViewModel
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)

        tabViewModel.topSection.observe(this, Observer {
            binding.tabSection1.updateTablature(it.sectionCol)
        })

        tabViewModel.bottomSection.observe(this, Observer {
            binding.tabSection2.updateTablature(it.sectionCol)
        })

        tabViewModel.sectionUpdateTime.observe(this, Observer {
            mediaPlayerFragment.setSectionUpdateTime(it)
        })

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.tab_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.edit_tab_option -> {
                val tablature = TabFragmentArgs.fromBundle(
                    arguments!!
                ).tablature
                Log.v("TabFragment", "Tablature id is ${tablature.tabId}")
                view?.findNavController()?.navigate(
                    TabFragmentDirections.actionTabFragmentToEditTabFragment(
                        tablature.songUri,
                        tablature
                    )
                )
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initMediaPlayerFragment(songUri: String) {
        val fragmentManager = getFragmentManager()
        val fragmentTransaction = fragmentManager?.beginTransaction()
        mediaPlayerFragment =
            MediaPlayerFragment(this)
        val args = Bundle()
        args.putString("songUri", songUri)
        mediaPlayerFragment.setArguments(args)
        fragmentTransaction?.add(R.id.media_player, mediaPlayerFragment)
        fragmentTransaction?.commit()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayerFragment.onPause()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayerFragment.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayerFragment.onDestroy()
    }

    override fun updateSection() {
        tabViewModel.updateSection()
    }

    //time is retrieved from mediaplayerfragment.
    //Check to see what section we have to jump to
    override fun updateSectionTo(time: Int) {
        tabViewModel.updateSectionTo(time)
    }

}
