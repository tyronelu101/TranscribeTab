package com.simplu.transcribetab

import android.media.MediaPlayer
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
import com.simplu.transcribetab.databinding.FragmentMediaPlayerBinding
import kotlinx.coroutines.*

class MediaPlayerFragment : Fragment() {

    private lateinit var mediaPlayerViewModel: MediaPlayerViewModel
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var binding: FragmentMediaPlayerBinding

    private val mediaPlayerJob = Job()
    private val mediaPlayerScope = CoroutineScope(mediaPlayerJob + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = arguments
        var songUri = ""
        if (bundle != null) {
            songUri = bundle.getString("songUri")
        }
        Log.v("Testing", "Created uri is ${songUri}")

        mediaPlayerViewModel = ViewModelProvider(this).get(MediaPlayerViewModel::class.java)

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

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_media_player, container, false)
        binding.mediaPlayerViewModel = mediaPlayerViewModel
        binding.lifecycleOwner = this

        mediaPlayerViewInit()

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun mediaPlayerViewInit() {

        binding.songSeekBar.max = mediaPlayer.duration / 1000
        binding.songSeekBar.setOnSeekBarChangeListener(seekBarOnChangeListener())

        mediaPlayerViewModel.setDuration(mediaPlayer.duration / 1000L)

        mediaPlayerViewModel.currentTimeString.observe(this, Observer {
            binding.songCurrentTime.text = it
        })

        mediaPlayerViewModel.durationString.observe(this, Observer {
            binding.songDuration.text = it
        })

        mediaPlayerViewModel.isPlaying.observe(this, Observer { play ->
            Log.v("MediaPlayerFragment", "Media play status updated")
            if (play) {
                mediaPlayer.start()
                observeMedia()
            }
            else {
                mediaPlayer.pause()
            }
        })

        mediaPlayerViewModel.skipTo.observe(this, Observer {
            mediaPlayer?.seekTo(it.toInt() * 1000)
        })

        mediaPlayer.start()
    }

    private fun observeMedia() {
        mediaPlayerScope.launch {
            while (mediaPlayer.isPlaying) {
                val currentPosition = (mediaPlayer.currentPosition) / 1000
                binding.songSeekBar.progress = (currentPosition)
                delay(500)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer != null && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
        mediaPlayerJob.cancel()
    }

    inner class seekBarOnChangeListener : SeekBar.OnSeekBarChangeListener {
        var currentProgress = 0

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            if (fromUser) {
                currentProgress = progress
            }
            mediaPlayerViewModel.updateTime(progress.toLong())

        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            mediaPlayer?.seekTo(currentProgress * 1000)
        }

    }

}
