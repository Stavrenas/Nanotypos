package com.example.nanotypos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.nanotypos.data.ViewModel
import com.example.nanotypos.databinding.FragmentImageBinding

class ImageFragment: Fragment() {

    private val sharedViewModel: ViewModel by activityViewModels()
    private var binding: FragmentImageBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = FragmentImageBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            imageFragment = this@ImageFragment
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    fun searchForLogo(){
        Toast.makeText(activity, "Search for Logo pressed!", Toast.LENGTH_LONG).show()
        //sharedViewModel.uri

    }

    fun searchForQR(){
        Toast.makeText(activity, "Search for QR pressed!", Toast.LENGTH_LONG).show()
    }

}