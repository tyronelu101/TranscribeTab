package com.simplu.transcribetab.mediaplayer

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
import com.simplu.transcribetab.R
import com.simplu.transcribetab.databinding.FragmentMediaPlayerBinding
import com.simplu.transcribetab.edittab.EditTabFragment
import com.simplu.transcribetab.tab.SectionUpdater
import kotlinx.coroutines.*

class MediaPlayerFragment(var sectionUpdater: SectionUpdater? = null) : Fragment(), EditTabFragment.OnAddSectionListener {

    private lateinit var mediaPlayerViewModel: MediaPlayerViewModel
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var binding: FragmentMediaPlayerBinding

    private val mediaPlayerJob = Job()
    private val mediaPlayerScope = CoroutineScope(mediaPlayerJob + Dispatchers.Main)

    private var isObserveMedia: Boolean = false

    //Time to watch where next section update occurs
    //Only use in tab fragment
    private var sectionUpdateTime: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        var songUri = ""
        if (bundle != null) {
            songUri = bundle.getString("songUri")
        }
        mediaPlayerViewModel = ViewModelProvider(this).get(MediaPlayerViewModel::class.java)
        Log.v("MediaPlayerFragment", "MediaPlayerFragment created")
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

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_media_player, container, false
        )
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
            if (play) {
                mediaPlayer.start()
                isObserveMedia = true
                observeMedia()

            } else {
                mediaPlayer.pause()
                isObserveMedia = false
            }
        })

        mediaPlayerViewModel.skipTo.observe(this, Observer {
            mediaPlayer?.seekTo(it.toInt() * 1000)
        })

        mediaPlayer.start()
    }

    private fun observeMedia() {
        mediaPlayerScope.launch {
            while (isObserveMedia) {
                Log.v("MediaPlayerFragment", "Coroutine is running")
                val currentPosition = (mediaPlayer.currentPosition) / 1000
                binding.songSeekBar.progress = (currentPosition)
                if (sectionUpdater != null) {
                    updateSection();
                }
                delay(500)
            }
        }
    }

    private fun updateSection() {
        if (sectionUpdateTime == mediaPlayer.currentPosition / 1000) {
            sectionUpdater?.updateSection()
        }
    }

    fun getTime(): Int {
        return mediaPlayer.currentPosition / 1000
    }

    fun skipTo(time: Int) {
        mediaPlayer.seekTo(time)
        binding.songSeekBar.progress = time / 1000
    }

    fun setSectionUpdateTime(time: Int) {
        this.sectionUpdateTime = time
    }

    override fun onResume() {
        super.onResume()
        Log.v("Lifecycle", "MediaPlayer onresume")
    }

    override fun onPause() {
        super.onPause()
        Log.v("Lifecycle", "MediaPlayer On pause")
        if (mediaPlayer.isPlaying) {
            mediaPlayerViewModel.setIsPlaying(false)
        }
    }

    override fun onStop() {
        super.onStop()
        Log.v("Lifecycle", "MediaPlayer On stop")
    }

    override fun onDestroy() {
        Log.v("Lifecycle", "Media player onDestoroy")
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayerJob.cancel()
        mediaPlayer.release()

        super.onDestroy()
    }


    inner class seekBarOnChangeListener : SeekBar.OnSeekBarChangeListener {
        var currentProgress = 0
        var wasPlaying = false
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            if (fromUser) {
                Log.v("MediaPlayerFragment", "Was playing ${wasPlaying}")

                if (mediaPlayer.isPlaying) {
                    wasPlaying = true
                    mediaPlayer.pause()
                }

                currentProgress = progress
                sectionUpdater?.updateSectionTo(currentProgress)

            }
            mediaPlayerViewModel.updateTime(progress.toLong())
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if (wasPlaying) {
                mediaPlayer.start()
                wasPlaying = false
            }
            mediaPlayer?.seekTo(currentProgress * 1000)
        }

    }

    override fun getMediaTime(): Int = mediaPlayer.currentPosition / 1000

}
