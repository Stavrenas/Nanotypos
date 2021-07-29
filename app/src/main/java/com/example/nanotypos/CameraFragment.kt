package com.example.nanotypos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import com.example.nanotypos.data.ViewModel
import com.example.nanotypos.databinding.FragmentCameraBinding
import com.google.common.util.concurrent.ListenableFuture


class CameraFragment: Fragment(R.layout.fragment_camera) {

    private val sharedViewModel: ViewModel by activityViewModels()

    private var _binding: FragmentCameraBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
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


    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraProviderFuture = activity?.let { ProcessCameraProvider.getInstance(it) } as ListenableFuture<ProcessCameraProvider>
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(context))
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
            }
        }
        return false
    }

    fun bindPreview(cameraProvider : ProcessCameraProvider) {
        val preview : Preview = Preview.Builder().build()

        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

        val camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview)

        /* failed autofocus attempt
        val cameraControl = camera.cameraControl
        val x = 0.500
        val y = 0.500
        val width = 1
        val height = 1
        val factory = SurfaceOrientedMeteringPointFactory(width.toFloat(), height.toFloat())
        val point = factory.createPoint(x.toFloat(), y.toFloat())
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
            // auto calling cancelFocusAndMetering in 5 seconds
            .setAutoCancelDuration(5, TimeUnit.SECONDS)
            .build()
        cameraControl.startFocusAndMetering(action)
        */


    }

    fun searchLogoPreview(){
        sharedViewModel.toggleLogoButton()
        Toast.makeText(activity, "Search for Logo selected!", Toast.LENGTH_SHORT).show()

    }
    fun searchQRPreview(){
        sharedViewModel.toggleLogoButton()
        Toast.makeText(activity, "Search for QR selected!", Toast.LENGTH_SHORT).show()
    }







}
