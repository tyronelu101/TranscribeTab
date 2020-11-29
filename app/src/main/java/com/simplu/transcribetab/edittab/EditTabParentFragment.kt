package com.simplu.transcribetab.edittab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.simplu.transcribetab.R
import com.simplu.transcribetab.mediaplayer.MediaPlayerFragment


class EditTabParentFragment : Fragment() {

    private val editTabFragment: EditTabFragment = EditTabFragment()
    private val mediaPlayerFragment: MediaPlayerFragment = MediaPlayerFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_tab_parent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addFragments()

        editTabFragment.addSectionListener = mediaPlayerFragment

    }

    private fun addFragments() {

        var songUri = ""
        val tab = EditTabParentFragmentArgs.fromBundle(arguments!!).tab

        if (tab != null) {
            songUri = tab.songUri
        } else {
            songUri = EditTabParentFragmentArgs.fromBundle(arguments!!).songUri
        }

        val fragmentManager = getFragmentManager()
        val fragmentTransaction = fragmentManager?.beginTransaction()

        val mediaArgs = Bundle()
        mediaArgs.putString("songUri", songUri)
        mediaPlayerFragment.arguments = mediaArgs

        val tabArgs = Bundle()
        tabArgs.putParcelable("tab", tab)
        tabArgs.putString("songUri", songUri)
        editTabFragment.arguments = tabArgs

        fragmentTransaction?.replace(R.id.edit_tab_fragment_container, editTabFragment)
        fragmentTransaction?.replace(R.id.edit_media_fragment_container, mediaPlayerFragment)
        fragmentTransaction?.commit()

    }

}
