package com.example.nanotypos

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
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

    /*
    fun searchForLogo(){
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
    */


    fun searchForLogo (){
        val uri: Uri? = sharedViewModel.getModelUri()
        //val image: InputImage

        // Initialization
        val options: ObjectDetector.ObjectDetectorOptions =
            ObjectDetector.ObjectDetectorOptions.builder().setMaxResults(1).build()
        val objectDetector: ObjectDetector =
            ObjectDetector.createFromFileAndOptions(context, "model.tflite", options)
        try {
            //image = InputImage.fromFilePath(context, uri)
            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
            val tensorImage = TensorImage.fromBitmap(bitmap)
            // Run inference
            val results: List<Detection> = objectDetector.detect(tensorImage)
            for (detectedObject in results) {
                val boundingBox = detectedObject.boundingBox
                Log.d("LOGO", " boundingBox: (${boundingBox.left}, ${boundingBox.top}) - (${boundingBox.right},${boundingBox.bottom})")
                Toast.makeText(activity," boundingBox: (${boundingBox.left}, ${boundingBox.top}) - (${boundingBox.right},${boundingBox.bottom})", Toast.LENGTH_SHORT).show()
                for (category in detectedObject.categories){
                    val label = category.label
                    val score = category.score
                    Log.d("LOGO", "Label is $label")
                    Log.d("LOGO", "Score is $score")
                    Toast.makeText(activity, "Label is $label", Toast.LENGTH_SHORT).show()
                    Toast.makeText(activity,"Score is $score", Toast.LENGTH_SHORT).show()
                }

            }
            //val result = objectDetector.detect(tensorImage)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    /*
    fun searchForLogo(){
        val model = Model.newInstance(context)
        val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
        // Creates inputs for reference.
        val image = TensorImage.fromBitmap(bitmap)

        // Runs model inference and gets result.
        val outputs = model.process(image)
        val detectionResult = outputs.detectionResultList.get(0)

        // Gets result from DetectionResult.
        val location = detectionResult.locationAsRectF;
        val category = detectionResult.categoryAsString;
        val score = detectionResult.scoreAsFloat;

        // Releases model resources if no longer used.
        model.close()
    }

     */

    fun searchForQR(){
        val uri: Uri? = sharedViewModel.getModelUri()
        val image: InputImage
        try {
            image = InputImage.fromFilePath(context, uri)
            //get instance of barcode scanner
            val scanner = BarcodeScanning.getClient()
            var foundCode = false
            //Process the image
            //val result = scanner...
            scanner.process(image).addOnSuccessListener { barcodes ->
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
                                sharedViewModel.setTextValue(getString(R.string.QR_success))
                                foundCode = true
                                findNavController().navigate(R.id.action_imageFragment_to_successFragment)
                            }
                            Barcode.TYPE_TEXT ->{
                                sharedViewModel.setBarcode(barcode)
                                sharedViewModel.setTextValue(getString(R.string.QR_failure))
                                findNavController().navigate(R.id.action_imageFragment_to_successFragment)
                            }
                        }
                    }
                    // Task completed successfully
                    // ...

                    //Display toast if QR is not found
                    if(!foundCode)
                        Toast.makeText(activity, "No QR code found", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    // ...
                    Toast.makeText(activity, "Error :(", Toast.LENGTH_SHORT).show()
                }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}