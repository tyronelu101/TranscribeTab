package com.simplu.transcribetab.edittab

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.simplu.transcribetab.R
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureDatabase
import com.simplu.transcribetab.database.TablatureRepository
import com.simplu.transcribetab.databinding.FragmentEditTabBinding
import com.simplu.transcribetab.mediaplayer.MediaPlayerFragment
import kotlinx.android.synthetic.main.fragment_edit_tab.*

class EditTabFragment : Fragment() {

    private lateinit var binding: FragmentEditTabBinding
    private lateinit var editTabViewModel: EditTabViewModel
    private var mediaPlayerFragment: MediaPlayerFragment = MediaPlayerFragment()

    private var tabId = -1L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = requireNotNull(this.activity).application
        val repository = TablatureRepository(TablatureDatabase.getInstance(application))

        val bundle = arguments
        var tab = bundle?.getParcelable<Tablature>("tab")

        val viewModelFactory =
            EditTabViewModelFactory(repository, tab)
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

        val tab = arguments?.getParcelable<Tablature>("tab")
        var songUri = EditTabFragmentArgs.fromBundle(arguments!!).songUri
        if (tab != null) {
            tabId = tab.tabId
            songUri = tab.songUri
        }

        initEditTabView(tab)

        val mediaArgs = Bundle()
        mediaArgs.putString("songUri", songUri)
        mediaPlayerFragment.arguments = mediaArgs

        binding.prevColumnButton.setOnClickListener {
            binding.editTablature.prevColumn()
        }

        binding.nextColumnBtn.setOnClickListener {
            binding.editTablature.nextColumn()
        }
        binding.tuning.setOnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.tuning.clearFocus()
            }
            false
        }

        binding.btnClrColumn.setOnClickListener {
            editTabViewModel.clearColumn(binding.editTablature.getCurrentSelectedColumn())
        }

        //Child fragment manager handles child fragment lifecycle
        //Future not to self: Don't use fragmentManager(for activities) when adding fragment inside a fragment
        childFragmentManager.beginTransaction().apply {
            replace(R.id.edit_media_fragment_container, mediaPlayerFragment)
            commit()
        }

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

        //If tabId is -1, means creating tab
        if (tabId != -1L) {
            val confirmItem = menu?.findItem(R.id.confirm)
            confirmItem?.isVisible = true
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val tab = Tablature(
            title = binding.title.text.toString(),
            artist = binding.artist.text.toString(),
            arranger = binding.arranger.text.toString(),
            tuning = binding.tuning.text.toString(),
            sections = editTabViewModel.sectionMap,
            songUri = arguments!!.getString("songUri")
        )

        when (item?.itemId) {

            R.id.save -> {
                if (this.tabId != -1L) {
                    tab.tabId = tabId
                    editTabViewModel.onUpdate(tab)
                    Toast.makeText(context, "Tablature updated", Toast.LENGTH_SHORT).show()

                } else {
                    if (editTabViewModel.sectionMap.size < 2) {
                        Toast.makeText(
                            context,
                            "Requires at least two sections to save.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        editTabViewModel.onSave(tab)
                        Toast.makeText(context, "Tablature saved", Toast.LENGTH_SHORT).show()
                    }
                }
                return true
            }

            R.id.confirm -> {
                if (tab != null) {
                    editTabViewModel.loadTab()?.observe(this, Observer {
                        findNavController().navigate(
                            EditTabFragmentDirections.actionEditTabFragmentToTabFragment(
                                it
                            )
                        )
                    })
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class tabInputOnTouchListener : View.OnTouchListener {

        var initialY = 0f
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {

            var isSwipeUp: Boolean
            val offset = 25f

            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = event.y
                    Log.i("EditTabFragment", "Button View down")
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val currentY = event.y
                    Log.i("EditTabFragment", "Button View Up")
                    //Swipe up
                    if (initialY > currentY + offset) {
                        Log.i("EditTabFragment", "swipe up $initialY, $currentY")
                        isSwipeUp = true
                    }
                    //Swipe down
                    else if (initialY < currentY - offset) {
                        Log.i("EditTabFragment", "swipe down $initialY, $currentY")
                        isSwipeUp = false
                    }
                    //Do nothing
                    else {
                        return false
                    }
                    val button = v as Button
                    val parent = button.parent as LinearLayout
                    val stringToUpdate = when (parent.id) {
                        R.id.input_row_1 -> if (isSwipeUp) 0 else 1
                        R.id.input_row_2 -> if (isSwipeUp) 2 else 3
                        R.id.input_row_3 -> if (isSwipeUp) 4 else 5
                        else -> -1
                    }
                    val note = button.text.toString()
                    val columnToUpdate = binding.editTablature.getCurrentSelectedColumn()
                    if (note.equals("X")) {
                        editTabViewModel.insertAt(columnToUpdate, stringToUpdate, "X")
                    } else {
                        editTabViewModel.insertAt(columnToUpdate, stringToUpdate, note)
                    }
                    true
                }
            }

            return false
        }
    }

}

