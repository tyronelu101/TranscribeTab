package com.simplu.transcribetab.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.simplu.transcribetab.R
import com.simplu.transcribetab.edittab.EditTabFragment
import com.simplu.transcribetab.edittab.EditTabParentFragmentArgs
import com.simplu.transcribetab.mediaplayer.MediaPlayerFragment


class TabParentFragment : Fragment() {
    private val tabFragment: TabFragment = TabFragment()
    private val mediaPlayerFragment: MediaPlayerFragment = MediaPlayerFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab_parent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addFragments()
        mediaPlayerFragment.sectionUpdater = tabFragment

    }

    private fun addFragments() {

        val tab = TabParentFragmentArgs.fromBundle(arguments!!).tab

        val fragmentManager = getFragmentManager()
        val fragmentTransaction = fragmentManager?.beginTransaction()

        val mediaArgs = Bundle()
        mediaArgs.putString("songUri", tab.songUri)
        mediaPlayerFragment.arguments = mediaArgs

        val tabArgs = Bundle()
        tabArgs.putParcelable("tab", tab)
        tabFragment.arguments = tabArgs

        fragmentTransaction?.replace(R.id.tab_fragment_container, tabFragment)
        fragmentTransaction?.replace(R.id.edit_media_fragment_container, mediaPlayerFragment)
        fragmentTransaction?.commit()

    }


}
