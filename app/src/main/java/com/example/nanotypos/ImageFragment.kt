package com.example.nanotypos

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.nanotypos.data.ViewModel
import com.example.nanotypos.databinding.FragmentImageBinding
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import java.io.IOException

class ImageFragment: Fragment() {

    private val sharedViewModel: ViewModel by activityViewModels()

    private var _binding: FragmentImageBinding? = null

    private val binding get() = _binding!!

    val localModel = LocalModel.Builder()
        .setAssetFilePath("ml/model.tflite")
        .build()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val _binding = FragmentImageBinding.inflate(inflater, container, false)
        return binding.root
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
        _binding = null
    }

    fun searchForLogo(){
        val uri: Uri? = sharedViewModel.getModelUri()
        val image: InputImage
        // Live detection and tracking
        val customObjectDetectorOptions =
            CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.5f)
                .setMaxPerObjectLabelCount(3)
                .build()

        val objectDetector =ObjectDetection.getClient(customObjectDetectorOptions)
        try {
            image = InputImage.fromFilePath(context, uri)
            objectDetector.process(image).
            addOnFailureListener { e ->

                    Log.d("LOGO", "error is $e")

            }
            .addOnSuccessListener{results ->
                for (detectedObject in results) {

                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
        Toast.makeText(activity, "Search for Logo pressed!", Toast.LENGTH_SHORT).show()
    }

    fun searchForQR(){
        Toast.makeText(activity, "Searching for QR!", Toast.LENGTH_SHORT).show()
        val uri: Uri? = sharedViewModel.getModelUri()
        val image: InputImage
        try {
            image = InputImage.fromFilePath(context, uri)
            //get instance of barcode scanner
            val scanner = BarcodeScanning.getClient()
            //Process the image
            val result = scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        //Get information from barcodes

                        val bounds = barcode.boundingBox
                        val corners = barcode.cornerPoints
                        val rawValue = barcode.rawValue

                        // See API reference for complete list of supported types
                        when (barcode.valueType) {
                            Barcode.TYPE_WIFI -> {
                                val ssid = barcode.wifi!!.ssid
                                val password = barcode.wifi!!.password
                                val type = barcode.wifi!!.encryptionType
                            }
                            Barcode.TYPE_URL -> {
                                val title = barcode.url!!.title
                                val url = barcode.url!!.url
                                Toast.makeText(activity, "Url is $url", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    // Task completed successfully
                    // ...
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    // ...
                }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}