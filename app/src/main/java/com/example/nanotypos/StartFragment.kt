package com.example.nanotypos

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.nanotypos.data.ViewModel
import com.example.nanotypos.databinding.FragmentStartBinding
import java.io.File
import java.io.IOException
import java.util.*


class StartFragment : Fragment() {

    // Binding object instance corresponding to the fragment_start.xml layout
    // This property is non-null between the onCreateView() and onDestroyView() lifecycle callbacks,
    // when the view hierarchy is attached to the fragment.
    private var binding: FragmentStartBinding? = null
    private val sharedViewModel: ViewModel by activityViewModels()

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1000
        const val REQUEST_IMAGE_OPEN = 1
        const val CAPTURE_IMAGE_REQUEST = 1
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = FragmentStartBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.startFragment = this
    }



    //function to launch intent to open gallery
    fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        // Only the system receives the ACTION_OPEN_DOCUMENT, so no need to test.
        startActivityForResult(intent, REQUEST_IMAGE_OPEN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_OPEN && resultCode == Activity.RESULT_OK) {
            val fullPhotoUri: Uri? = data?.data
            if (fullPhotoUri != null) {
                sharedViewModel.setModelUri(fullPhotoUri)
            }
            findNavController().navigate(R.id.action_startFragment_to_imageFragment)
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            findNavController().navigate(R.id.action_startFragment_to_imageFragment)
        }
        else
            Toast.makeText(activity, "Oopsie!",Toast.LENGTH_LONG).show()

    }



    //function to launch intent to open camera and capture an image
    fun captureImage() {
        Toast.makeText(activity, "Open Camera pressed!",Toast.LENGTH_SHORT).show()
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (context?.let { takePictureIntent.resolveActivity(it.packageManager) } != null) {
                // Create the File where the photo should go
                    try {
                    val photoFile = createImageFile()
                    // Continue only if the File was successfully created
                        val photoURI = FileProvider.getUriForFile(
                            requireActivity(),
                            "com.example.nanotypos.fileprovider",
                            photoFile!!
                        )
                        sharedViewModel.setModelUri(photoURI)
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST)
                    } catch (ex: Exception) {
                        // Error occurred while creating the File
                    }
            }
    }

    lateinit var currentPhotoPath: String
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    //    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // pick image after request permission success
//                    pickImage()
//                }
//            }
//        }
//    }



}



