package com.simplu.transcribetab.mediaplayer

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simplu.transcribetab.R
import com.simplu.transcribetab.databinding.FragmentMediaPlayerBinding
import com.simplu.transcribetab.tab.SectionUpdater

class MediaPlayerFragment(private val sectionUpdater: SectionUpdater? = null) :
    Fragment() {

    private lateinit var mediaPlayerViewModel: MediaPlayerViewModel
    private lateinit var binding: FragmentMediaPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = arguments
        var songUri = ""
        if (bundle != null) {
            songUri = bundle.getString("songUri")
        }
        val uri = Uri.parse(songUri)

        val mediaPlayerViewModelFactory = MediaPlayerViewModelFactory(requireContext(), uri, sectionUpdater)
        mediaPlayerViewModel = ViewModelProvider(
            this,
            mediaPlayerViewModelFactory
        ).get(MediaPlayerViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_media_player, container, false
        )
        binding.mediaPlayerViewModel = mediaPlayerViewModel
        binding.lifecycleOwner = this

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.songSeekBar.setOnSeekBarChangeListener(seekBarOnChangeListener())

        mediaPlayerViewModel.duration.observe(this, Observer { duration ->
            binding.songSeekBar.max = duration
        })

        mediaPlayerViewModel.currentTime.observe(this, Observer { currentTime ->
            binding.songSeekBar.progress = currentTime.toInt()
        })

        mediaPlayerViewModel.durationString.observe(this, Observer {
            binding.songDuration.text = it
        })

        mediaPlayerViewModel.isPlaying.observe(this, Observer { play ->
            if (play) {
                binding.playPauseBtn.text = "Pl"

            } else {
                binding.playPauseBtn.text = "Pa"
            }
        })

    }

    fun getTime(): Int = mediaPlayerViewModel.currentTime.value ?: 0

    fun skipTo(time: Int) {
        mediaPlayerViewModel.skipTo(time)
    }

    override fun onResume() {
        super.onResume()
        Log.v("Lifecycle", "MediaPlayer onresume")
    }

    override fun onPause() {
        super.onPause()
        Log.v("Lifecycle", "MediaPlayer On pause")
        mediaPlayerViewModel.pause()
    }

    override fun onStop() {
        super.onStop()
        Log.v("Lifecycle", "MediaPlayer On stop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v("Lifecycle", "Media player on destroy")

    }

    fun setTriggerTime(time: Int) {
        mediaPlayerViewModel.setTriggerTime(time)
    }
    inner class seekBarOnChangeListener : SeekBar.OnSeekBarChangeListener {

        var currentProgress = 0
        var wasPlaying = false

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            if (fromUser) {

                if (mediaPlayerViewModel.isPlaying()) {
                    wasPlaying = true
                    mediaPlayerViewModel.pause()
                }

                currentProgress = progress
                mediaPlayerViewModel.skipTo(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if (wasPlaying) {
                mediaPlayerViewModel.play()
                wasPlaying = false
            }
            mediaPlayerViewModel.skipTo(currentProgress)
        }
    }
}
