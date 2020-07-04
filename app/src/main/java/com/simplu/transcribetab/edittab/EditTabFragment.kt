package com.simplu.transcribetab.edittab

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simplu.transcribetab.R
import com.simplu.transcribetab.database.TablatureDatabase
import com.simplu.transcribetab.databinding.FragmentEditTabBinding


class EditTabFragment : Fragment() {

    private lateinit var binding: FragmentEditTabBinding
    private lateinit var editTabViewModel: EditTabViewModel
    private lateinit var editTabView: EdittableTabView
    private lateinit var mediaPlayer: MediaPlayer

    private var mHandler: Handler = Handler()

    //Runnable to update progress bar value with mediaplayer time
    private val runnable: Runnable = object : Runnable {
        override fun run() {

            // Current position is in milliseconds so convert to seconds
            val currentTime = (mediaPlayer.currentPosition) / 1000L
            binding.mediaPlayer.songSeekBar.progress = currentTime.toInt()
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

        //Initialize our edit tab view
        editTabView = EdittableTabView(activity!!.applicationContext)

        //Initialize the media player with uri from args
        val args = EditTabFragmentArgs.fromBundle(arguments!!)
        mediaPlayer = MediaPlayer.create(context, Uri.parse(args.songUri))

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_tab, container, false)

        setHasOptionsMenu(true)

        binding.tablatureContainer.addView(editTabView)

        binding.addColBtn.setOnClickListener {
            editTabView.addColumnToEnd(true)
        }

        binding.mediaPlayer.songSeekBar.max = mediaPlayer.duration / 1000

        binding.insertBar.setOnClickListener {
            editTabView.insertBar()
        }

        binding.clrColumn.setOnClickListener {
            editTabView.clearColumn()
        }
        binding.insert.setOnClickListener {
            editTabView.insertColumn()
        }
        binding.mediaPlayer.playPauseBtn.setOnClickListener {

            //Current state of media player is paused so play it
            if (!mediaPlayer.isPlaying) {
                editTabViewModel.onPlay()
            } else {
                editTabViewModel.onPause()
            }
        }

        binding.mediaPlayer.songSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val current = seekBar?.progress ?: 0
                editTabViewModel.updateTime(current.toLong())

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val seekTo = seekBar?.progress ?: 0
                editTabViewModel.updateTime(seekTo.toLong())
                mediaPlayer.seekTo(seekTo * 1000)
            }

        })

        binding.mediaPlayer.setSkipTo.setOnClickListener {
            editTabViewModel.setSkipTo()
        }

        binding.mediaPlayer.goTo.setOnClickListener {
            editTabViewModel.onGoTo()
        }
        binding.deleteBtn.setOnClickListener {
            editTabView.deleteCol()
        }

        for (view in binding.stringInputContainer.children.iterator()) {
            //Get the current row as linearlayout
            val currentRow = view as LinearLayout
            //get the children which are buttons and set each button to a touch listner
            for (button in currentRow.children.iterator()) {
                button.setOnTouchListener(tabInputOnTouchListener())
            }
        }

        editTabViewModel.setDuration(mediaPlayer.duration / 1000L)

        editTabViewModel.isPlaying.observe(this, Observer { play ->
            if (play) {
                mediaPlayer.start()
                mHandler.post(runnable)
            }
        })
        editTabViewModel.isPaused.observe(this, Observer { paused ->
            if (paused) {
                Log.v("EditTabView", "Paused")
                mediaPlayer.pause()
                mHandler.removeCallbacks(runnable)
            }
        })

        editTabViewModel.currentTimeString.observe(this, Observer {
            binding.mediaPlayer.songCurrentTime.text = it
        })

        editTabViewModel.durationString.observe(this, Observer {
            binding.mediaPlayer.songDuration.text = it
        })

        editTabViewModel.skipTo.observe(this, Observer {
            binding.mediaPlayer.songSeekBar.progress = it.toInt()
            mediaPlayer.seekTo(it.toInt() * 1000)
        })


        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.edittab_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {

            R.id.save -> {

            }
        }
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
                    Log.v("Touch", "Note is ${note}")
                    if (note.equals("c", ignoreCase = true)) {
                        editTabView.clearString(stringToUpdate)
                    } else
                        editTabView.insertNote(stringToUpdate, note)

                    false
                }
            }


            return false
        }
    }
}

