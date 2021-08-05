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

    fun searchForLogo() {
        Toast.makeText(activity, "Search for Logo pressed!", Toast.LENGTH_SHORT).show()
        val uri: Uri? = sharedViewModel.getModelUri()


        // GOOGLE ML KIT IMPLEMENTATION


        val localModel = LocalModel.Builder()
            .setAssetFilePath("model.tflite")
            .build()
        val image: InputImage
        // Detection and tracking
        val customObjectDetectorOptions =
            CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.5f)
                .setMaxPerObjectLabelCount(1)
                .build()

        val objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)
        try {
            image = InputImage.fromFilePath(context, uri)
            objectDetector.process(image)
                .addOnFailureListener { e ->
                    Log.d("LOGO", "error is $e")
                }

                .addOnSuccessListener { results ->
                    for (detectedObject in results) {
                        val boundingBox = detectedObject.boundingBox
                        //val trackingId = detectedObject.trackingId
                        for (label in detectedObject.labels) {
                            //val text = label.text
                            //val index = label.index
                            val confidence = label.confidence
                            //Log.d("LOGO", "text is $text")
                            //Log.d("LOGO", "index is $index")
                            Log.d("LOGO", "confidence is $confidence")
                            Toast.makeText(
                                activity,
                                "confidence is $confidence",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        Log.d(
                            "LOGO",
                            " boundingBox: (${boundingBox.left}, ${boundingBox.top}) - (${boundingBox.right},${boundingBox.bottom})"
                        )
                    }
                }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


        /*

        //  TF LITE IMPLEMENTATION //


        val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
        val image = TensorImage.fromBitmap(bitmap)

        // Step 2: Initialize the detector object
        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(5)
            .setScoreThreshold(0.5f)
            .build()
        val detector = ObjectDetector.createFromFileAndOptions(
            context, // the application context
            "model.tflite", // must be same as the filename in assets folder
            options
        )
        val results = detector.detect(image)
        debugPrint(results)

        val resultToDisplay = results.map {
            // Get the top-1 category and craft the display text
            val category = it.categories.first()
            val text = "${category.label}, ${category.score.times(100).toInt()}%"

            // Create a data object to display the detection result
            DetectionResult(it.boundingBox, text)
        }
        // Draw the detection result on the bitmap and show it.
        val imgWithResult = drawDetectionResult(bitmap, resultToDisplay)
        runOnUiThread {
            inputImageView.setImageBitmap(imgWithResult)
        }

    }

    private fun debugPrint(results : List<Detection>) {
        for ((i, obj) in results.withIndex()) {
            val box = obj.boundingBox
            val TAG = "LOGO"

            Log.d(TAG, "Detected object: ${i} ")
            Log.d(TAG, "  boundingBox: (${box.left}, ${box.top}) - (${box.right},${box.bottom})")

            for ((j, category) in obj.categories.withIndex()) {
                Log.d(TAG, "    Label $j: ${category.label}")
                val confidence: Int = category.score.times(100).toInt()
                Log.d(TAG, "    Confidence: ${confidence}%")
            }
        }
    }


     */

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
                    Toast.makeText(activity, "No QR code found", Toast.LENGTH_SHORT).show()
                }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}