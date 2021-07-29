package com.example.nanotypos

import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
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



class CameraFragment: Fragment(R.layout.fragment_camera) {


    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService

    private val sharedViewModel: ViewModel by activityViewModels()

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!


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
}


