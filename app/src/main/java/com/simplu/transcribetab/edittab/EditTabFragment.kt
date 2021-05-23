package com.simplu.transcribetab.edittab

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.simplu.transcribetab.R
import com.simplu.transcribetab.ShowcaseHelper
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureDatabase
import com.simplu.transcribetab.database.TablatureRepository
import com.simplu.transcribetab.databinding.FragmentEditTabBinding
import com.simplu.transcribetab.mediaplayer.MediaPlayerFragment
import kotlinx.android.synthetic.main.fragment_edit_tab.*


class EditTabFragment : Fragment() {


    private var _binding: FragmentEditTabBinding? = null
    private val binding get() = _binding!!

    private lateinit var editTabViewModel: EditTabViewModel
    private var mediaPlayerFragment: MediaPlayerFragment = MediaPlayerFragment()

    private var tab: Tablature? = null

    private var saveDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        saveDialog = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setMessage("Are you sure you want to exit? Tablature has not been saved yet.")
                setPositiveButton(
                    "Yes"
                ) { dialog, id ->

                    findNavController().popBackStack()
                }
                setNegativeButton(
                    "Cancel"
                ) { dialog, id ->
                    //Do nothing
                }
            }

            // Create the AlertDialog
            builder.create()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_tab, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val application = requireNotNull(this.activity).application
        val repository = TablatureRepository(TablatureDatabase.getInstance(application))

        val bundle = arguments
        val tabFromBundle: Tablature? = bundle?.getParcelable("tab")

        val viewModelFactory =
            EditTabViewModelFactory(
                repository,
                tabFromBundle,
                binding.editTablature.numberOfColumns
            )
        editTabViewModel =
            ViewModelProvider(this, viewModelFactory).get(EditTabViewModel::class.java)
        binding.editTabViewModel = editTabViewModel

        var songUri = EditTabFragmentArgs.fromBundle(requireArguments()).songUri
        if (tabFromBundle != null) {
            songUri = tabFromBundle.songUri
            tab = tabFromBundle
        }

        initEditTabView(tab)

        val mediaArgs = Bundle()
        mediaArgs.putString("songUri", songUri)
        mediaPlayerFragment.arguments = mediaArgs
        //Child fragment manager handles child fragment lifecycle
        //Future not to self: Don't use fragmentManager(for activities) when adding fragment inside a fragment
        childFragmentManager.beginTransaction().apply {
            replace(R.id.edit_media_fragment_container, mediaPlayerFragment)
            commitNow()
        }
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

        binding.currentSectionNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s.toString().toIntOrNull()?.let {
                    editTabViewModel.skipToSection(it)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!ShowcaseHelper.isShowing) {
                        if (editTabViewModel.tabIsUpdated()) {
                            findNavController().popBackStack()
                        } else {
                            saveDialog?.show()
                        }
                    }
                }
            })

        initShowCase()
    }


    private fun initEditTabView(tab: Tablature?) {

        if (tab != null) {
            binding.title.setText(tab.title)
            binding.artist.setText(tab.artist)
            binding.arranger.setText(tab.arranger)
            binding.tuning.setText(tab.tuning)
        }

        editTabViewModel.skipToVal.observe(viewLifecycleOwner, Observer {
            mediaPlayerFragment.skipTo(it)
        })

        for (view in binding.stringInputContainer.children.iterator()) {
            //Get the current row as linearlayout
            val currentRow = view as LinearLayout
            //get the children which are buttons and set each button to a touch listner
            for (button in currentRow.children.iterator()) {
                button.setOnClickListener(tabInputOnTouchListener())
            }
        }

        editTabViewModel.currentSection.observe(viewLifecycleOwner, Observer {
            edit_tablature.updateTablature(it.sectionCol)
        })

        add_section_btn.setOnClickListener {
            editTabViewModel.addSection(mediaPlayerFragment.getTime())
        }

        btn_set_time.setOnClickListener {
            editTabViewModel.onSetTime(mediaPlayerFragment.getTime())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edittab_menu, menu)

        //If tabId is not null, show confirm
        if (tab != null) {
            val confirmItem = menu.findItem(R.id.confirm)
            confirmItem?.isVisible = true
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var tab = this.tab

        if (tab == null) {
            tab = Tablature(
                title = binding.title.text.toString(),
                artist = binding.artist.text.toString(),
                arranger = binding.arranger.text.toString(),
                tuning = binding.tuning.text.toString(),
                sections = editTabViewModel.sectionMap,
                songUri = requireArguments().getString("songUri")
            )
        } else {
            tab.title = binding.title.text.toString()
            tab.artist = binding.artist.text.toString()
            tab.arranger = binding.arranger.text.toString()
            tab.tuning = binding.tuning.text.toString()
            tab.sections = editTabViewModel.sectionMap
            tab.songUri = requireArguments().getString("songUri")

        }
        when (item.itemId) {

            R.id.save -> {
                if (this.tab != null) {
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
                        findNavController().navigate(EditTabFragmentDirections.actionEditTabFragmentToTabListFragment())
                    }
                }
                return true
            }

            R.id.confirm -> {
                if (editTabViewModel.tabIsUpdated()) {
                    findNavController().popBackStack()
                } else {
                    saveDialog?.show()
                }

                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private inner class tabInputOnTouchListener : View.OnClickListener {

        override fun onClick(v: View?) {

            val button = v as Button
            val parent = button.parent as LinearLayout
            val row = when (parent.id) {
                R.id.input_row_1 -> 0
                R.id.input_row_2 -> 1
                R.id.input_row_3 -> 2
                else -> -1
            }
            val note = button.text.toString()
            val columnToUpdate = binding.editTablature.getCurrentSelectedColumn()
            editTabViewModel.insertAt(columnToUpdate, row, note)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        ShowcaseHelper.cleanUp()
    }

    override fun onStart() {
        super.onStart()
        ShowcaseHelper.addView(
            mediaPlayerFragment.binding.setSkipTo,
            "Saves the current time of audio",
            getString(R.string.next)
        )
        ShowcaseHelper.addView(
            mediaPlayerFragment.binding.goTo,
            "Skips audio to the time saved",
            dismissText = "Finish",
            skipText = ""
        )
        ShowcaseHelper.startSequence()

    }

    private fun initShowCase() {

        ShowcaseHelper.init(requireContext(), requireActivity(), "sequence " + javaClass.name)
        ShowcaseHelper.addView(
            binding.btnSetTime,
            "Sets this section's time",
            getString(R.string.next)
        );
        ShowcaseHelper.addView(
            binding.addSectionBtn,
            "Adds a new section with the current time",
            getString(R.string.next)
        )
        ShowcaseHelper.addView(
            binding.prevColumnButton,
            "Go to previous column",
            getString(R.string.next)
        )
        ShowcaseHelper.addView(binding.nextColumnBtn, "Go to next column", getString(R.string.next))
        ShowcaseHelper.addView(
            binding.inputRow1,
            "Swipe up to to input string 1, down for string 2",
            getString(R.string.next),
            rectangle = true
        )
        ShowcaseHelper.addView(
            binding.inputRow2,
            "Swipe up to to input string 3, down for string 4",
            getString(R.string.next),
            rectangle = true
        )
        ShowcaseHelper.addView(
            binding.inputRow3,
            "Swipe up to to input string 5, down for string 6",
            getString(R.string.next),
            rectangle = true
        )
        ShowcaseHelper.addView(
            binding.btnPrevSection,
            "Go to previous section",
            getString(R.string.next)
        )
        ShowcaseHelper.addView(
            binding.btnNextSection,
            "Go to next section",
            getString(R.string.next)
        )
        ShowcaseHelper.addView(
            binding.currentSectionNumber,
            "Type in section number to skip to it",
            getString(R.string.next)
        )
        ShowcaseHelper.addView(
            binding.txtSectionTime,
            "The current section's time. Tapping this skips the audio to this time",
            getString(R.string.next)
        )
    }
}

