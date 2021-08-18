package com.example.nanotypos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.media.Image
import android.os.Bundle
import android.util.AttributeSet
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


class CameraFragment: Fragment(R.layout.fragment_camera) {


    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private val sharedViewModel: ViewModel by activityViewModels()

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!


    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }


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
            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

            val options: ObjectDetector.ObjectDetectorOptions =
                ObjectDetector.ObjectDetectorOptions.builder().setMaxResults(1).setScoreThreshold(0.1f).build()

            val objectDetector: ObjectDetector =
                ObjectDetector.createFromFileAndOptions(context, "model.tflite", options)

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
                            val detectedObject = results.first()
                            val obj = detectedObject.categories.first()

                            val boundingBox = detectedObject.boundingBox
                            val score = obj.score
                            activity?.runOnUiThread {
                                binding.scoreText.setText("Score is $score")
                                binding.rectOverlay.post { binding.rectOverlay.drawRectangle(boundingBox) }
                            }

                            Log.d(
                                "LOGO",
                                "Score is $score (${boundingBox.left}, ${boundingBox.top}) - (${boundingBox.right},${boundingBox.bottom}) "
                            )


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

class RectOverlay constructor(context: Context?, attributeSet: AttributeSet?) :
    View(context, attributeSet) {

    private var rectangle =  RectF()
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context!!, android.R.color.black)
        strokeWidth = 10f
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Pass it a list of RectF (rectBounds)
        canvas.drawRect(rectangle, paint)
    }

    fun drawRectangle(rect: RectF) {
        this.rectangle = rect
        invalidate()
    }
}

