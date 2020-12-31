package com.simplu.transcribetab.tab

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simplu.transcribetab.R
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.databinding.FragmentTabBinding
import com.simplu.transcribetab.mediaplayer.MediaPlayerFragment

class TabFragment : Fragment(),
    SectionUpdater {

    private lateinit var tabViewModel: TabViewModel
    private lateinit var binding: FragmentTabBinding
    private var mediaPlayerFragment: MediaPlayerFragment = MediaPlayerFragment(this)

    private lateinit var tablature: Tablature
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = arguments

        tablature = requireNotNull(bundle?.getParcelable<Tablature>("tab"))

        tablature.let {
            val viewModelFactory = TabViewModelFactory(tablature)
            tabViewModel =
                ViewModelProvider(this, viewModelFactory).get(TabViewModel::class.java)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

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
            mediaPlayerFragment.setTriggerTime(it)
        })

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
            Toast.makeText(context, "This tab does not have a tab object", Toast.LENGTH_SHORT).show()
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
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.tab_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.edit_tab_option -> {
                Log.v("TabFragment", "Tablature id is ${tablature.tabId}")
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        Log.v("Lifecycle", "TabFragment onresume")
    }

    override fun onPause() {
        super.onPause()
        Log.v("Lifecycle", "TagFragment On pause")
    }

    override fun onStop() {
        super.onStop()
        Log.v("Lifecycle", "TagFragment On stop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("Lifecycle", "TabFragment destroyed")
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
