package com.simplu.transcribetab

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SimpleCursorAdapter
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.simplu.transcribetab.databinding.FragmentSongListBinding


class SongListFragment : Fragment() {

    data class Song(
        val uri: Uri,
        val name: String,
        val duration: Int
    )

    private val PERMISSION_REQUEST_READ_EXTERNAL = 1

    private lateinit var binding: FragmentSongListBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_song_list, container, false)
        permissions()

        binding.songList.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = parent?.getItemAtPosition(position) as Cursor
                val indexID = item.getColumnIndex(MediaStore.Audio.Media._ID)
                val ID = item.getLong(indexID)

                val uri =
                    ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ID)
                        .toString()

                view?.findNavController()?.navigate(SongListFragmentDirections.actionSongListFragmentToEditTabFragment(uri))
            }
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun permissions() {
        when {
            ContextCompat.checkSelfPermission(
                this.context!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                retrieveSongs()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
            -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                Log.v("SongListFragment", "Show explanation")
            }
            else -> {
                // You can directly ask for the permission.
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_READ_EXTERNAL
                )
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_READ_EXTERNAL -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    retrieveSongs()
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Log.v("SongListFragment", "Must load songs to continue")
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun retrieveSongs() {

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION
        )


        var selection = null
        var selectionArgs = null
        var sortOrder = null

        val cursor = context?.contentResolver?.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection, selection, selectionArgs, sortOrder
        )

        val listItems = intArrayOf(R.id.song_title, R.id.song_duration)
        val listColumn: Array<String> =
            arrayOf(
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION
            )

        val cursorAdapter =
            SimpleCursorAdapter(context, R.layout.item_song, cursor, listColumn, listItems, 0)

        binding.songList.adapter = cursorAdapter
    }
}
