package com.simplu.transcribetab.mediaplayer

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.simplu.transcribetab.R
import com.simplu.transcribetab.databinding.FragmentMediaPlayerBinding
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig

class MediaPlayerFragment(private val sectionUpdater: MediaPlayerCallback? = null) :
    Fragment() {

    private var _binding: FragmentMediaPlayerBinding? = null
    private val binding get() = _binding!!

    private lateinit var mediaPlayerViewModel: MediaPlayerViewModel
    var flag = false

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

        _binding = DataBindingUtil.inflate(
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

        mediaPlayerViewModel.durationString.observe(viewLifecycleOwner, Observer {
            binding.songDuration.text = it
        })

        mediaPlayerViewModel.isPlaying.observe(viewLifecycleOwner, Observer { play ->
            if (play) {
                binding.playPauseBtn.setImageResource(R.drawable.ic_pause_white_24dp)

            } else {
                binding.playPauseBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp)
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getTime(): Int = mediaPlayerViewModel.currentTime.value ?: 0

    fun skipTo(time: Int) {
        mediaPlayerViewModel.skipTo(time)
    }

    override fun onPause() {
        super.onPause()
        mediaPlayerViewModel.pause()
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

    public fun startShowCase() {
        val config = ShowcaseConfig()
        config.delay = 0 // half second between each showcase view
        val sequence = MaterialShowcaseSequence(
            activity,
            javaClass.simpleName + " sequence"
        )

        sequence.setConfig(config)

        sequence.addSequenceItem(
            binding.setSkipTo,
            "Saves the current time of audio ",
            "NEXT"
        )
        sequence.addSequenceItem(
            binding.goTo,
            "Skips audio to the time saved previously",
            "NEXT"
        )
        sequence.addSequenceItem(
            binding.songRate,
            "Set playback rate of audio",
            "FINISH"
        )
        sequence.setOnItemDismissedListener { itemView, position ->
            if(position == 2) {
                flag = true
                val prefs: SharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(context)
                prefs.edit().putBoolean("pref_cb_showcase", false).commit()
            }
        }
        sequence.start()
    }
}
