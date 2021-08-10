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
import androidx.navigation.fragment.findNavController
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
        Toast.makeText(activity, "Search for Logo pressed!", Toast.LENGTH_SHORT).show()
        val localModel = LocalModel.Builder()
            .setAssetFilePath("model.tflite")
            .build()
        val uri: Uri? = sharedViewModel.getModelUri()
        val image: InputImage
        // Detection and tracking
        val customObjectDetectorOptions =
            CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.5f)
                .setMaxPerObjectLabelCount(1)
                .build()

        val objectDetector =ObjectDetection.getClient(customObjectDetectorOptions)
        try {
            image = InputImage.fromFilePath(context, uri)
            objectDetector.process(image)
                .addOnFailureListener { e ->
                    Log.d("LOGO", "error is $e")
                }

                .addOnSuccessListener{results ->
                    for (detectedObject in results) {
                        val boundingBox = detectedObject.boundingBox
                        val trackingId = detectedObject.trackingId
                        for (label in detectedObject.labels) {
                            val text = label.text
                            val index = label.index
                            val confidence = label.confidence
                            Log.d("LOGO", "text is $text")
                            Log.d("LOGO", "index is $index")
                            Log.d("LOGO", "confidence is $confidence")
                            Toast.makeText(activity, "confidence is $confidence", Toast.LENGTH_SHORT).show()
                        }
                        Log.d("LOGO", "trackingId is $trackingId")
                        Log.d("LOGO", " boundingBox: (${boundingBox.left}, ${boundingBox.top}) - (${boundingBox.right},${boundingBox.bottom})")
                    }
                }

        } catch (e: IOException) {
            e.printStackTrace()
        }
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

                        //val bounds = barcode.boundingBox
                        //val corners = barcode.cornerPoints
                        //val rawValue = barcode.rawValue

                        // See API reference for complete list of supported types
                        when (barcode.valueType) {
                            Barcode.TYPE_WIFI -> {
                                //val ssid = barcode.wifi!!.ssid
                                //val password = barcode.wifi!!.password
                                //val type = barcode.wifi!!.encryptionType
                            }
                            Barcode.TYPE_URL -> {
                                //val title = barcode.url!!.title
                                //val url = barcode.url!!.url
                                sharedViewModel.setBarcode(barcode)
                                findNavController().navigate(R.id.action_imageFragment_to_startFragment)
                            }
                            Barcode.TYPE_TEXT ->{
                                sharedViewModel.setBarcode(barcode)
                                findNavController().navigate(R.id.action_imageFragment_to_startFragment)
                            }
                        }
                    }
                    // Task completed successfully
                    // ...
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    // ...
                    Toast.makeText(activity, "No QR code found", Toast.LENGTH_SHORT).show()
                }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}