package com.simplu.transcribetab.tab

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simplu.transcribetab.MediaPlayerViewModel
import com.simplu.transcribetab.R
import com.simplu.transcribetab.database.TablatureDatabase
import com.simplu.transcribetab.databinding.FragmentTabBinding

class TabFragment : Fragment() {

    private lateinit var tabViewModel: TabViewModel
    private lateinit var mediaPlayerViewModel: MediaPlayerViewModel
    private lateinit var binding: FragmentTabBinding
    private lateinit var mediaPlayer: MediaPlayer
    private var timeToWatch: Int? = null
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

        val tabId = TabFragmentArgs.fromBundle(
            arguments!!
        ).tabId

        val dataSource =
            TablatureDatabase.getInstance(requireNotNull(this.activity).application).tablatureDatabaseDao

        val viewModelFactory = TabViewModelFactory(dataSource, tabId)

        tabViewModel =
            ViewModelProvider(this, viewModelFactory).get(TabViewModel::class.java)
        mediaPlayerViewModel = ViewModelProvider(this).get(MediaPlayerViewModel::class.java)

        mediaPlayer = MediaPlayer()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab, container, false)

        tabViewModel.tablature.observe(this, Observer {
            binding.title.text = it.title
            binding.artist.text = it.artist
            binding.arranger.text = it.arranger
            binding.tuning.text = it.tuning

            mediaPlayerViewModel.setUri(it.songUri)
        })

        tabViewModel.topTabValues.observe(this, Observer {
            binding.tab1.updateTablature(it)
        })

        tabViewModel.bottomTabValues.observe(this, Observer {
            binding.tab2.updateTablature(it)
        })

        tabViewModel.timeToWatch.observe(this, Observer {
            this.timeToWatch = it
        })
        mediaPlayerViewModel.songUri.observe(this, Observer {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(it))
                prepare()
            }

            mediaPlayerViewInit()
        })

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun mediaPlayerViewInit() {

        Log.v("TabFragment", "MediaPlayerViewInit")

        Log.v("TabFragment", "Initializing ${mediaPlayer.duration}")
        binding.mediaPlayer.songSeekBar.max = mediaPlayer.duration / 1000
        binding.mediaPlayer.songSeekBar.setOnSeekBarChangeListener(seekBarOnChangeListener())

        binding.mediaPlayer.setSkipTo.setOnClickListener {
            mediaPlayerViewModel.setSkipTo()
        }

        binding.mediaPlayer.goTo.setOnClickListener {
            mediaPlayerViewModel.onGoTo()
        }

        binding.mediaPlayer.playPauseBtn.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                Log.v("TabFragment", "Duration is ${mediaPlayer.duration}")
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
            Log.v("TagFragment", "Duration is ${it}")
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

    private fun startSeekbarUpdate() {
        mHandler.postDelayed(runnable, 0)
    }

    private fun stopSeekbarUpdate() {
        mHandler.removeCallbacks(runnable)
    }

    inner class seekBarOnChangeListener : SeekBar.OnSeekBarChangeListener {
        var currentProgress = 0

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            mediaPlayerViewModel.updateTime(progress.toLong())
            currentProgress = progress
            if (currentProgress == timeToWatch) {
                tabViewModel.update()
            }

        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            mediaPlayer?.seekTo(currentProgress * 1000)
        }

    }
}
