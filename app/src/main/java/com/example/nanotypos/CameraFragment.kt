package com.example.nanotypos

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.nanotypos.data.ViewModel
import com.example.nanotypos.databinding.FragmentCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.min


class CameraFragment: Fragment(R.layout.fragment_camera) {


    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService

    private val sharedViewModel: ViewModel by activityViewModels()

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }
    @androidx.camera.core.ExperimentalGetImage
    private fun startCamera() {
        // Create an instance of the ProcessCameraProvider,
        // which will be used to bind the use cases to a lifecycle owner.
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        // Add a listener to the cameraProviderFuture.
        // The first argument is a Runnable, which will be where the magic actually happens.
        // The second argument (way down below) is an Executor that runs on the main thread.
        cameraProviderFuture.addListener({
            // Add a ProcessCameraProvider, which binds the lifecycle of your camera to
            // the LifecycleOwner within the application's life.
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Initialize the Preview object, get a surface provider from your PreviewView,
            // and set it on the preview instance.

            val preview = Preview.Builder().apply {
                setTargetResolution(Size(binding.viewFinder.width, binding.viewFinder.height))
            }.build()

            val options: ObjectDetector.ObjectDetectorOptions =
                ObjectDetector.ObjectDetectorOptions.builder().setMaxResults(1).setScoreThreshold(0.1f).build()

            val objectDetector: ObjectDetector =
                ObjectDetector.createFromFileAndOptions(context, "model.tflite", options)
            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

            var textValue = ""

            // Setup the ImageAnalyzer for the ImageAnalysis use case
            val imageAnalysis = ImageAnalysis.Builder().setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, { input ->
                        try {
                            //image = InputImage.fromFilePath(context, uri)

                            val bitmap = input.image?.toBitmap()
                            val tensorImage = TensorImage.fromBitmap(bitmap)

                            // Run inference
                            val results: List<Detection> = objectDetector.detect(tensorImage)
                            for (detectedObject in results) {

                                val boundingBox = detectedObject.boundingBox

                                //Log.d("LOGO"," boundingBox: (${boundingBox.left}, ${boundingBox.top}) - (${boundingBox.right},${boundingBox.bottom})")
                                //Toast.makeText(activity," boundingBox: (${boundingBox.left}, ${boundingBox.top}) - (${boundingBox.right},${boundingBox.bottom})",Toast.LENGTH_SHORT).show()

                                for (category in detectedObject.categories) {
                                    //val label = category.label
                                    val score = category.score
                                    textValue = "Score is $score"

                                    activity?.runOnUiThread {
                                        binding.scoreText.setText(textValue)
                                    }

                                    Log.d(
                                        "LOGO",
                                        "Score is $score (${boundingBox.left}, ${boundingBox.top}) - (${boundingBox.right},${boundingBox.bottom}) "
                                    )
                                    val niceBox = mapOutputCoordinates(boundingBox)
                                    drawRect(niceBox)
                                    //Toast.makeText(activity,"Label is $label and score is $score",Toast.LENGTH_SHORT).show()
                                    /*
                                    val imgWithResult = drawDetectionResult(bitmap, detectedObject)
                                    binding?.targetImage?.setImageBitmap(imgWithResult)
                                    */

                                    //sharedViewModel.setModelUri(imgWithResult)
                                }


                            }

                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        input.close()

                    })
                }
            try {
                // Unbind any bound use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to lifecycleOwner
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (e: Exception) {
                Log.e("PreviewUseCase", "Binding failed! :(", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val vuBuffer = planes[2].buffer // VU

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    override fun onDestroy() {
        cameraExecutor.shutdown()
        super.onDestroy()
    }

    private fun drawRect(rectangle: RectF) = binding.viewFinder.post {

        val rect = RectF(rectangle)
        val location = mapOutputCoordinates(rect)

        (binding.boxPrediction.layoutParams as ViewGroup.MarginLayoutParams).apply {
            topMargin = location.top.toInt()
            leftMargin = location.left.toInt()
            width = min(binding.viewFinder.width, location.right.toInt() - location.left.toInt())
            height = min(binding.viewFinder.height, location.bottom.toInt() - location.top.toInt())
            //Log.d("QR", "topMargin is $topMargin")
            //Log.d("QR", "leftMargin is $leftMargin")
            //Log.d("QR", "width is $width")
            //Log.d("QR", "height is $height")
            //Log.d("QR", " boundingBox: (${rect.left}, ${rect.top}) - (${rect.right},${rect.bottom})")
        }

        binding.boxPrediction.visibility = View.VISIBLE

    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }


    /**
     * Helper function used to map the coordinates for objects coming out of
     * the model into the coordinates that the user sees on the screen.
     */
    private fun mapOutputCoordinates(location: RectF): RectF {

        // Step 1: map location to the preview coordinates
        val previewLocation = RectF(
            location.left * binding.viewFinder.width,
            location.top * binding.viewFinder.height,
            location.right * binding.viewFinder.width,
            location.bottom * binding.viewFinder.height
        )

        // Step 2: compensate for camera sensor orientation and mirroring
        val isFrontFacing = lensFacing == CameraSelector.LENS_FACING_FRONT
        val correctedLocation = if (isFrontFacing) {
            RectF(
                binding.viewFinder.width - previewLocation.right,
                previewLocation.top,
                binding.viewFinder.width - previewLocation.left,
                previewLocation.bottom)
        } else {
            previewLocation
        }

        // Step 3: compensate for 1:1 to 4:3 aspect ratio conversion + small margin
        val margin = 0.1f
        val requestedRatio = 4f / 3f
        val midX = (correctedLocation.left + correctedLocation.right) / 2f
        val midY = (correctedLocation.top + correctedLocation.bottom) / 2f
        return if (binding.viewFinder.width < binding.viewFinder.height) {
            RectF(
                midX - (1f + margin) * requestedRatio * correctedLocation.width() / 2f,
                midY - (1f - margin) * correctedLocation.height() / 2f,
                midX + (1f + margin) * requestedRatio * correctedLocation.width() / 2f,
                midY + (1f - margin) * correctedLocation.height() / 2f
            )
        } else {
            RectF(
                midX - (1f - margin) * correctedLocation.width() / 2f,
                midY - (1f + margin) * requestedRatio * correctedLocation.height() / 2f,
                midX + (1f - margin) * correctedLocation.width() / 2f,
                midY + (1f + margin) * requestedRatio * correctedLocation.height() / 2f
            )
        }
    }
}


