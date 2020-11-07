package com.simplu.transcribetab.edittab

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simplu.transcribetab.R
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureDatabase
import com.simplu.transcribetab.databinding.FragmentEditTabBinding
import com.simplu.transcribetab.mediaplayer.MediaPlayerFragment
import kotlinx.android.synthetic.main.fragment_edit_tab.*

class EditTabFragment : Fragment() {

    private lateinit var binding: FragmentEditTabBinding
    private lateinit var editTabViewModel: EditTabViewModel
    private lateinit var mediaPlayerFragment: MediaPlayerFragment

    private var tabId = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = requireNotNull(this.activity).application
        val dataSource =
            TablatureDatabase.getInstance(application).tablatureDatabaseDao

        val viewModelFactory =
            EditTabViewModelFactory(dataSource, EditTabFragmentArgs.fromBundle(arguments!!).tab)
        editTabViewModel =
            ViewModelProvider(this, viewModelFactory).get(EditTabViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_tab, container, false)
        binding.editTabViewModel = editTabViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var songUri = ""
        val tab = EditTabFragmentArgs.fromBundle(arguments!!).tab
        if (tab != null) {
            songUri = tab.songUri
            tabId = tab.tabId
        } else {
            songUri = EditTabFragmentArgs.fromBundle(arguments!!).songUri
        }

        initMediaPlayerFragment(songUri)

        initEditTabView(tab)

    }
    private fun initMediaPlayerFragment(songUri: String) {

        val fragmentManager = getFragmentManager()
        val fragmentTransaction = fragmentManager?.beginTransaction()
        mediaPlayerFragment =
            MediaPlayerFragment()
        val args = Bundle()
        args.putString("songUri", songUri)
        mediaPlayerFragment.setArguments(args)
        fragmentTransaction?.add(R.id.media_player, mediaPlayerFragment)
        fragmentTransaction?.commit()
    }

    private fun initEditTabView(tab: Tablature?) {

        if (tab != null) {
            binding.title.setText(tab.title)
            binding.artist.setText(tab.artist)
            binding.arranger.setText(tab.arranger)
            binding.tuning.setText(tab.tuning)
        }

        editTabViewModel.skipToVal.observe(this, Observer {
            mediaPlayerFragment.skipTo(it * 1000)
        })

        for (view in binding.stringInputContainer.children.iterator()) {
            //Get the current row as linearlayout
            val currentRow = view as LinearLayout
            //get the children which are buttons and set each button to a touch listner
            for (button in currentRow.children.iterator()) {
                button.setOnTouchListener(tabInputOnTouchListener())
            }
        }

        editTabViewModel.currentSection.observe(this, Observer {
            edit_tablature.updateTablature(it.sectionCol)
        })

        add_section_btn.setOnClickListener {
            editTabViewModel.addSection(mediaPlayerFragment.getTime())
        }

        btn_set_time.setOnClickListener {
            editTabViewModel.onSetTime(mediaPlayerFragment.getTime())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.edittab_menu, menu)

        Log.v("EditTabFragment", "Creating menu ${tabId}")
        //If tabId is -1, means creating tab
        if (tabId != -1L) {
            Log.v("EditTabFragment", "Making confirm item visible ${tabId}")
            val confirmItem = menu?.findItem(R.id.confirm)
            confirmItem?.isVisible = true
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        val tabTitle = binding.title.text.toString()
//        val tabArtist = binding.artist.text.toString()
//        val tabArranger = binding.arranger.text.toString()
//        val tuning = binding.tuning.text.toString()
//        val songUri = EditTabFragmentArgs.fromBundle(arguments!!).songUri
//        val tab = Tablature(
//            title = tabTitle,
//            artist = tabArtist,
//            arranger = tabArranger,
//            tuning = tuning,
//            sections = editTabViewModel.sectionMap,
//            sectionToTimeMap = editTabViewModel.sectionTimeMap,
//            songUri = songUri
//        )
//
//        when (item?.itemId) {
//
//            R.id.save -> {
//                Log.v("Saving", "Title is ${tabTitle}")
//                if (editTabViewModel.sectionMap.size < 2) {
//                    Toast.makeText(
//                        context,
//                        "Please create at least two sections before saving.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    false
//                }
//
//
//                if (this.tabId != -1L) {
//                    tab.tabId = tabId
//                    editTabViewModel.onUpdate(tab)
//                    Toast.makeText(context, "Tablature updated", Toast.LENGTH_SHORT).show()
//
//                } else {
//                    editTabViewModel.onSave(tab)
//                    Toast.makeText(context, "Tablature saved", Toast.LENGTH_SHORT).show()
//                    view?.findNavController()
//                        ?.navigate(EditTabFragmentDirections.actionEditTabFragmentToTabListFragment())
//                }
//                false
//            }
//
//            R.id.confirm -> {
//                if (tab != null) {
//                    Log.v("OnConfirm", "Going back.")
//                    view?.findNavController()
//                        ?.navigate(EditTabFragmentDirections.actionEditTabFragmentToTabFragment(tab))
//
//
//                }
//            }
//        }
        return super.onOptionsItemSelected(item)
    }

    inner class tabInputOnTouchListener : View.OnTouchListener {

        var initialY = 0f
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            val button = v as Button

            val parent = button.parent as LinearLayout

            val note = button.text.toString()
            var stringToUpdate = -1;

            var isSwipeUp = false

            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = event.y
                    Log.v("touch", "Initial ${initialY}")
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val currentY = event.y

                    //Swipe up
                    if (initialY > currentY + 50) {
                        isSwipeUp = true
                    }
                    //Swipe down
                    else if (initialY < currentY) {
                        isSwipeUp = false
                    }
                    //Do nothing
                    else {
                        false
                    }
                    stringToUpdate = when (parent.id) {
                        R.id.input_row_1 -> if (isSwipeUp) 0 else 1
                        R.id.input_row_2 -> if (isSwipeUp) 2 else 3
                        R.id.input_row_3 -> if (isSwipeUp) 4 else 5
                        else -> -1
                    }
                    val columnToUpdate = binding.editTablature.getSelectedColumnNumber()
                    if (note.equals("c", ignoreCase = true)) {
                        editTabViewModel.insertAt(columnToUpdate, stringToUpdate, "")
                    } else {
                        editTabViewModel.insertAt(columnToUpdate, stringToUpdate, note)
                    }
                    false
                }
            }

            return false
        }
    }

}

