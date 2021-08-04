package com.example.nanotypos

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.RectF
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
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
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

/*
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
/*
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            //bindPreview(cameraProvider)
            })
            }
 */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startCamera()
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().apply {
            setTargetResolution(Size(binding.viewFinder.width, binding.viewFinder.height))
        }.build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(cameraExecutor, { input ->
            @androidx.camera.core.ExperimentalGetImage
            val mediaImage = input.image

            @androidx.camera.core.ExperimentalGetImage
            val inImage =
                InputImage.fromMediaImage(mediaImage!!, input.imageInfo.rotationDegrees)
            try {
                val scanner = BarcodeScanning.getClient()
                //Process the image
                @androidx.camera.core.ExperimentalGetImage
                val result = scanner.process(inImage).addOnSuccessListener { barcodes ->
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
                        Toast.makeText(activity, "No QR found", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
        val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)

    }

    private fun startCamera() {

        // Initialize the Preview object, get a surface provider from your PreviewView,
        // and set it on the preview instance..
        val preview = Preview.Builder().apply {
            setTargetResolution(Size(binding.viewFinder.width, binding.viewFinder.height))
        }.build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        // Create an instance of the ProcessCameraProvider,
        // which will be used to bind the use cases to a lifecycle owner
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        // Add a listener to the cameraProviderFuture.
        // The first argument is a Runnable, which will be where the magic actually happens.
        // The second argument (way down below) is an Executor that runs on the main thread.
        cameraProviderFuture.addListener({
            // Add a ProcessCameraProvider, which binds the lifecycle of your camera to
            // the LifecycleOwner within the application's life.
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()


            // Setup the ImageAnalyzer for the ImageAnalysis use case
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis.setAnalyzer(cameraExecutor, { input ->
                @androidx.camera.core.ExperimentalGetImage
                val mediaImage = input.image

                @androidx.camera.core.ExperimentalGetImage
                val inImage =
                    InputImage.fromMediaImage(mediaImage!!, input.imageInfo.rotationDegrees)
                try {
                    val scanner = BarcodeScanning.getClient()
                    //Process the image
                    @androidx.camera.core.ExperimentalGetImage
                    val result = scanner.process(inImage).addOnSuccessListener { barcodes ->
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
                    }
                        .addOnFailureListener {
                            // Task failed with an exception
                            Toast.makeText(activity, "No QR found", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            })

            try {
                // Unbind any bound use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to lifecycleOwner
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (e: Exception) {
                Log.e("PreviewUseCase", "Binding failed!", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    fun searchLogoPreview() {
        Toast.makeText(context, "Search for Logo selected!", Toast.LENGTH_SHORT).show()
        view?.let { Snackbar.make(it, "Search for Logo selected!", Snackbar.LENGTH_SHORT).show() }
        sharedViewModel.toggleLogoButton()
    }

    fun searchQRPreview() {
        Toast.makeText(activity, "Search for QR selected!", Toast.LENGTH_SHORT).show()
        view?.let { Snackbar.make(it, "Search for QR selected!", Snackbar.LENGTH_SHORT).show() }
        Log.d("TOAST", "MESSAGE")
        sharedViewModel.toggleLogoButton()
    }

    */

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
            // Setup the ImageAnalyzer for the ImageAnalysis use case
            val imageAnalysis = ImageAnalysis.Builder().setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, { input ->
                            @androidx.camera.core.ExperimentalGetImage
                            val mediaImage = input.image
                            @androidx.camera.core.ExperimentalGetImage
                            val inImage =
                                InputImage.fromMediaImage(mediaImage!!, input.imageInfo.rotationDegrees)
                            try {
                                val scanner = BarcodeScanning.getClient()
                                //Process the image
                                @androidx.camera.core.ExperimentalGetImage
                                val result =
                                    scanner.process(inImage).addOnSuccessListener { barcodes ->
                                        for (barcode in barcodes) {
                                            //Get information from barcodes
                                            val bounds = barcode.boundingBox
                                            val corners = barcode.cornerPoints
                                            val rawValue = barcode.rawValue
                                            /*
                                            val rect = RectF(barcode.boundingBox)
                                            rect.left = translateX(rect.left)
                                            rect.top = translateY(rect.top)
                                            rect.right = translateX(rect.right)
                                            rect.bottom = translateY(rect.bottom)
                                            canvas.drawRect(rect, mRectPaint)

                                             */

                                            // See API reference for complete list of supported types
                                            when (barcode.valueType) {
                                                Barcode.TYPE_URL -> {
                                                    val title = barcode.url!!.title
                                                    val url = barcode.url!!.url
                                                    //Log.d("QR", "Title is $title")
                                                    Log.d("QR", "Url is $url")
                                                    drawRect(bounds)
                                                }

                                            }

                                        }
                                        input.close()
                                    }

                                        .addOnFailureListener {
                                            // Task failed with an exception
                                            input.close()

                                            Toast.makeText(activity, "No QR found", Toast.LENGTH_SHORT).show()

                                        }
                            } catch (e: IOException) {
                                e.printStackTrace()
                                Toast.makeText(context, "Error!", Toast.LENGTH_SHORT).show()
                            }
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

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

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

    fun drawRect(rectangle: Rect) = binding.viewFinder.post {

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
            Log.d("QR", " boundingBox: (${rect.left}, ${rect.top}) - (${rect.right},${rect.bottom})")
        }

        binding.boxPrediction.visibility = View.VISIBLE

    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    fun searchLogoPreview() {
        Toast.makeText(context, "Search for Logo selected!", Toast.LENGTH_SHORT).show()
        view?.let { Snackbar.make(it, "Search for Logo selected!", Snackbar.LENGTH_SHORT).show() }
        sharedViewModel.toggleLogoButton()
    }

    fun searchQRPreview() {
        Toast.makeText(activity, "Search for QR selected!", Toast.LENGTH_SHORT).show()
        view?.let { Snackbar.make(it, "Search for QR selected!", Snackbar.LENGTH_SHORT).show() }
        Log.d("TOAST", "MESSAGE")
        sharedViewModel.toggleLogoButton()
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


