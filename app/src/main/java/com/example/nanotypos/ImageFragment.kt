package com.example.nanotypos

import android.graphics.*
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
import java.util.*


class ImageFragment: Fragment() {

    private val sharedViewModel: ViewModel by activityViewModels()
    private var binding: FragmentImageBinding? = null

    companion object {
        private const val MAX_FONT_SIZE = 96F
    }

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
        val uri: Uri? = sharedViewModel.getModelUri()
        //val image: InputImage

        // Initialization
        val options: ObjectDetector.ObjectDetectorOptions =
            ObjectDetector.ObjectDetectorOptions.builder().setMaxResults(1).setScoreThreshold(0.1f).build()

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

                Log.d("LOGO"," boundingBox: (${boundingBox.left}, ${boundingBox.top}) - (${boundingBox.right},${boundingBox.bottom})")
                Toast.makeText(activity," boundingBox: (${boundingBox.left}, ${boundingBox.top}) - (${boundingBox.right},${boundingBox.bottom})",Toast.LENGTH_SHORT).show()

                for (category in detectedObject.categories) {
                    val label = category.label
                    val score = category.score

                    Log.d("LOGO", "Label is $label and score is $score ")
                    Toast.makeText(activity,"Label is $label and score is $score",Toast.LENGTH_SHORT).show()

                    val imgWithResult = drawDetectionResult(bitmap, detectedObject)
                    binding?.targetImage?.setImageBitmap(imgWithResult)
                    //sharedViewModel.setModelUri(imgWithResult)
                }

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun drawDetectionResult(
        bitmap: Bitmap,
        detectionResult: Detection
    ): Bitmap {
        val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(outputBitmap)
        val pen = Paint()
        pen.textAlign = Paint.Align.LEFT

            // draw bounding box
            pen.color = Color.RED
            pen.strokeWidth = 8F
            pen.style = Paint.Style.STROKE
            val box = detectionResult.boundingBox
            canvas.drawRect(box, pen)

            val tagSize = Rect(0, 0, 0, 0)

            // calculate the right font size
            pen.style = Paint.Style.FILL_AND_STROKE
            pen.color = Color.YELLOW
            pen.strokeWidth = 2F

            pen.textSize = MAX_FONT_SIZE
                for (category in detectionResult.categories) {
                val label = category.label
                val score = category.score
                    pen.getTextBounds(label, 0, label.length, tagSize)

                    val fontSize: Float = pen.textSize * box.width() / tagSize.width()

                    // adjust the font size so texts are inside the bounding box
                    if (fontSize < pen.textSize) pen.textSize = fontSize

                    var margin = (box.width() - tagSize.width()) / 2.0F
                    if (margin < 0F) margin = 0F
                    canvas.drawText(
                        label, box.left + margin,
                        box.top + tagSize.height().times(1F), pen
                    )
            }

        return outputBitmap
    }



    fun searchForQR() {
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
                        Barcode.TYPE_TEXT -> {
                            sharedViewModel.setBarcode(barcode)
                            sharedViewModel.setTextValue(getString(R.string.QR_failure))
                            findNavController().navigate(R.id.action_imageFragment_to_successFragment)
                        }
                    }
                }
                // Task completed successfully
                // ...

                //Display toast if QR is not found
                if (!foundCode)
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