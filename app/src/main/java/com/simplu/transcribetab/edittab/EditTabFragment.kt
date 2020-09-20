package com.simplu.transcribetab.edittab

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simplu.transcribetab.R
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureDatabase
import com.simplu.transcribetab.databinding.FragmentEditTabBinding

class EditTabFragment : Fragment() {

    private lateinit var binding: FragmentEditTabBinding
    private lateinit var editTabViewModel: EditTabViewModel
    private lateinit var mediaPlayerViewModel: MediaPlayerViewModel
    private lateinit var mediaPlayer: MediaPlayer

    val mHandler = Handler()
    private val runnable: Runnable = object : Runnable {

        override fun run() {

            // Current position is in milliseconds so convert to seconds
            val currentPosition = (mediaPlayer.currentPosition) / 1000
            binding.mediaPlayer.songSeekBar.progress = (currentPosition)
            mHandler.postDelayed(this, 1000)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = requireNotNull(this.activity).application
        val dataSource =
            TablatureDatabase.getInstance(application).tablatureDatabaseDao

        val viewModelFactory = EditTabViewModelFactory(dataSource)
        editTabViewModel =
            ViewModelProvider(this, viewModelFactory).get(EditTabViewModel::class.java)
        mediaPlayerViewModel = ViewModelProvider(this).get(MediaPlayerViewModel::class.java)

        val songUri = EditTabFragmentArgs.fromBundle(arguments!!).songUri
        val uri = Uri.parse(songUri)
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            prepare()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_tab, container, false)

        setHasOptionsMenu(true)
        editTabViewInit()
        mediaPlayerViewInit()

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun editTabViewInit() {
        binding.addSectionBtn.setOnClickListener {
            editTabViewModel.addSection()
        }

        binding.btnLeft.setOnClickListener {
            editTabViewModel.previousSection()
        }

        binding.btnRight.setOnClickListener {
            editTabViewModel.nextSection()
        }

        binding.btnSetTime.setOnClickListener {
            Log.v("Test", "SetTimeBtnClicked")
            editTabViewModel.onSetTime(mediaPlayer.currentPosition/1000)
        }

        editTabViewModel.totalSectionsObs.observe(this, Observer {
            binding.totalSectionNumber.text = "/" + Integer.toString(it)
        })

        editTabViewModel.sectionTimeRangeString.observe(this, Observer {
            Log.v("EditTab", "Range time string is ${it}")
            binding.sectionTimeText.text = it
        })

        for (view in binding.stringInputContainer.children.iterator()) {
            //Get the current row as linearlayout
            val currentRow = view as LinearLayout
            //get the children which are buttons and set each button to a touch listner
            for (button in currentRow.children.iterator()) {
                button.setOnTouchListener(tabInputOnTouchListener())
            }
        }

        editTabViewModel.currentSectionObs.observe(this, Observer {
            binding.editTablature.updateTablature(it)
        })

        editTabViewModel.currentSectionNumObs.observe(this, Observer {
            binding.currentSectionNumber.setText(Integer.toString(it))
        })
    }

    private fun mediaPlayerViewInit() {

        binding.mediaPlayer.songSeekBar.max = mediaPlayer.duration / 1000
        binding.mediaPlayer.songSeekBar.setOnSeekBarChangeListener(seekBarOnChangeListner())

        binding.mediaPlayer.setSkipTo.setOnClickListener {
            mediaPlayerViewModel.setSkipTo()
        }

        binding.mediaPlayer.goTo.setOnClickListener {
            mediaPlayerViewModel.onGoTo()
        }

        binding.mediaPlayer.playPauseBtn.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayerViewModel.onPause()
            } else {
                mediaPlayerViewModel.onPlay()
            }
        }

        mediaPlayerViewModel.setDuration(mediaPlayer.duration / 1000L)

        mediaPlayerViewModel.currentTimeString.observe(this, Observer {
            binding.mediaPlayer.songCurrentTime.text = it
        })

        mediaPlayerViewModel.durationString.observe(this, Observer {
            binding.mediaPlayer.songDuration.text = it
        })

        mediaPlayerViewModel.isPlaying.observe(this, Observer {
            if (it) {
                mediaPlayer.start()
                startSeekbarUpdate()
            }
        })

        mediaPlayerViewModel.isPaused.observe(this, Observer {
            if (it) {
                mediaPlayer.pause()
                stopSeekbarUpdate()
            }
        })

        mediaPlayerViewModel.skipTo.observe(this, Observer {
            mediaPlayer?.seekTo(it.toInt() * 1000)
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
                    columns = editTabViewModel.sectionValuesMap,
                    songUri = songUri
                )

                editTabViewModel.onSave(tab)
                Toast.makeText(context, "Tablature saved", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startSeekbarUpdate() {
        mHandler.postDelayed(runnable, 0)
    }

    private fun stopSeekbarUpdate() {
        mHandler.removeCallbacks(runnable)
    }

    inner class seekBarOnChangeListner : SeekBar.OnSeekBarChangeListener {
        var currentProgress = 0

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            mediaPlayerViewModel.updateTime(progress.toLong())
            currentProgress = progress


        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            mediaPlayer?.seekTo(currentProgress * 1000)
        }

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
                    Log.v("Touch", "Note is ${note}")
                    if (note.equals("c", ignoreCase = true)) {
                        val columnToUpdate = binding.editTablature.getSelectedColumnNumber()
                        editTabViewModel.insertAt(columnToUpdate, stringToUpdate, "")
                    } else {
                        val columnToUpdate = binding.editTablature.getSelectedColumnNumber()
                        editTabViewModel.insertAt(columnToUpdate, stringToUpdate, note)
                        false
                    }
                    false
                }
            }

            return false
        }
    }
}

