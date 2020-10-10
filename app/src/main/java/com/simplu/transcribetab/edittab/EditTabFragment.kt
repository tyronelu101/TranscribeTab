package com.simplu.transcribetab.edittab

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simplu.transcribetab.MediaPlayerFragment
import com.simplu.transcribetab.R
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureDatabase
import com.simplu.transcribetab.databinding.FragmentEditTabBinding

class EditTabFragment : Fragment() {

    private lateinit var binding: FragmentEditTabBinding
    private lateinit var editTabViewModel: EditTabViewModel
    private lateinit var mediaPlayerFragment: MediaPlayerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = requireNotNull(this.activity).application
        val dataSource =
            TablatureDatabase.getInstance(application).tablatureDatabaseDao

        val viewModelFactory = EditTabViewModelFactory(dataSource)
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
        initMediaPlayerFragment()
        initEditTabView()

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun initMediaPlayerFragment() {
        val songUri = EditTabFragmentArgs.fromBundle(arguments!!).songUri

        val fragmentManager = getFragmentManager()
        val fragmentTransaction = fragmentManager?.beginTransaction()
        mediaPlayerFragment = MediaPlayerFragment()
        val args = Bundle()
        args.putString("songUri", songUri)
        mediaPlayerFragment.setArguments(args)
        fragmentTransaction?.add(R.id.media_player, mediaPlayerFragment)
        fragmentTransaction?.commit()
    }

    private fun initEditTabView() {

//        binding.btnSetTime.setOnClickListener {
//            editTabViewModel.onSetTime(mediaPlayer.currentPosition / 1000)
//        }

//        editTabViewModel.skipToVal.observe(this, Observer {
//            binding.mediaPlayer.songSeekBar.progress = it
//            mediaPlayer?.seekTo(it * 1000)
//        })

        for (view in binding.stringInputContainer.children.iterator()) {
            //Get the current row as linearlayout
            val currentRow = view as LinearLayout
            //get the children which are buttons and set each button to a touch listner
            for (button in currentRow.children.iterator()) {
                button.setOnTouchListener(tabInputOnTouchListener())
            }
        }

        editTabViewModel.currentSectionColumns.observe(this, Observer {
            binding.editTablature.updateTablature(it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.edittab_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {

            R.id.save -> {
                val tabTitle = binding.title.text.toString()
                val tabArtist = binding.artist.text.toString()
                val tabArranger = binding.arranger.text.toString()
                val tuning = binding.tuning.text.toString()
                val songUri = EditTabFragmentArgs.fromBundle(arguments!!).songUri
                val tab = Tablature(
                    title = tabTitle,
                    artist = tabArtist,
                    arranger = tabArranger,
                    tuning = tuning,
                    sections = editTabViewModel.sectionMap,
                    sectionToTimeMap = editTabViewModel.sectionTimeMap,
                    songUri = songUri
                )

                editTabViewModel.onSave(tab)
                Toast.makeText(context, "Tablature saved", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun test() {
        Toast.makeText(context, "Testing", Toast.LENGTH_SHORT).show()
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

