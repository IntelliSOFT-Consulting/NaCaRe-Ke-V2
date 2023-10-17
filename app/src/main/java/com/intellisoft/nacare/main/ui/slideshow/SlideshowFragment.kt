package com.intellisoft.nacare.main.ui.slideshow

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.intellisoft.nacare.helper_class.FormatterClass
import com.intellisoft.nacare.room.MainViewModel
import com.nacare.capture.databinding.FragmentSlideshowBinding

class SlideshowFragment : Fragment() {
    private lateinit var binding: FragmentSlideshowBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var formatterClass: FormatterClass
    private lateinit var caseId: String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel =
            ViewModelProvider(this).get(MainViewModel::class.java)
        binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root
        formatterClass = FormatterClass()


        return root
    }


}