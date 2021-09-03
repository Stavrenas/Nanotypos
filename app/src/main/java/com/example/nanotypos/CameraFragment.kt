package com.example.nanotypos

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.nanotypos.databinding.FragmentCameraBinding
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraFragment: Fragment(R.layout.fragment_camera)  {

    //private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    //private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    //private val sharedViewModel: ViewModel by activityViewModels()

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private var totalDetected = 0
    private var done = false



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
            Toast.makeText(activity, "Make sure the logo is on the center", Toast.LENGTH_LONG).show()
            //Dialog().show(childFragmentManager, "")
            startCamera()

        } else {
            requestPermissions(
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


        cameraProviderFuture.addListener({

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Initialize the Preview object, get a surface provider from your PreviewView,
            // and set it on the preview instance.

            val preview = Preview.Builder().apply {
                setTargetResolution(Size(binding.viewFinder.width, binding.viewFinder.height))
            }.build()
            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

            val options: ObjectDetector.ObjectDetectorOptions =
                ObjectDetector.ObjectDetectorOptions.builder().setMaxResults(1).setNumThreads(3).setScoreThreshold(0.75F).build()

            val objectDetector: ObjectDetector =
                ObjectDetector.createFromFileAndOptions(context, "model.tflite", options)

            // Setup the ImageAnalyzer for the ImageAnalysis use case
            val imageAnalysis =
                ImageAnalysis.Builder().setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(Size(binding.viewFinder.width, binding.viewFinder.height))
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, { input ->
                            try {

                                val bitmap = input.image?.toBitmap()

                                // Run inference
                                val results: List<Detection> = objectDetector.detect(TensorImage.fromBitmap(bitmap))
                                if (results.isNotEmpty()) {

                                    val boundingBox = bitmap?.let { it1 -> fixCoords(results.first().boundingBox, it1.width, it1.height) }
                                    val score = results.first().categories.first().score


                                    activity?.runOnUiThread {
                                        totalDetected++
                                        binding.scoreText.text = "Score is $score"
                                        binding.rectOverlay.post {
                                            if (boundingBox != null) {
                                                binding.rectOverlay.draw(boundingBox)
                                            }
                                        }
                                        if (totalDetected > 30 && !done) {

                                            val youtubePlayerFragment = PlayerFragment()
                                            val transaction = parentFragmentManager.beginTransaction()
                                            transaction.replace(R.id.videoFragment, youtubePlayerFragment).commit()
                                            done = true
                                            totalDetected = 0
                                        }
                                    }

                                }
                                else{
                                    totalDetected = 0
                                    if( binding.scoreText.text != getText(R.string.NoLogo) ) {
                                        activity?.runOnUiThread {
                                            binding.rectOverlay.post {
                                                binding.rectOverlay.draw(
                                                    RectF(
                                                        -1f,
                                                        -1f,
                                                        -1f,
                                                        -1f
                                                    )
                                                )
                                            }
                                        }
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


    //function used to map the coordinates for objects coming out of
    //the model into the coordinates that the user sees on the screen.

    private fun fixCoords(box: RectF, sourceWidth: Int, sourceHeight: Int): RectF {
        val targetWidth = binding.viewFinder.width.toFloat()
        val targetHeight = binding.viewFinder.height.toFloat()

        val tempLeft = targetWidth * (1 - box.top / sourceHeight.toFloat())
        val tempRight = targetWidth * (1 -  box.bottom / sourceHeight.toFloat())
        val tempBottom = (box.left  / sourceWidth.toFloat() ) * targetHeight * 1.02F
        val tempTop =  (box.right / sourceWidth.toFloat() ) * targetHeight * 0.98F

        box.left = tempLeft
        box.right = tempRight
        box.bottom = tempBottom
        box.top = tempTop

        return box
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
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
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


}

class RectOverlay constructor(context: Context?, attributeSet: AttributeSet?) :
    View(context, attributeSet) {

    private var rectangle =  RectF()
    private val rectangles: MutableList<RectF> = mutableListOf()
    private val tagSize = Rect(0, 0, 0, 0)


    private val label = "Nanotypos is\nthe best company\nthat has ever existed."
    val pen = Paint().apply {
        textAlign = Paint.Align.LEFT
        color = resources.getColor(R.color.teal)
        typeface = Typeface.create("HELVETICA", Typeface.BOLD)
        setShadowLayer(0F, 0F, 0F, Color.DKGRAY)
    }

    private val nicePen = Paint(pen).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 8F
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        pen.getTextBounds(label, 0, label.length, tagSize)
        pen.textSize = rectangle.width().times(-0.2F)
        nicePen.getTextBounds(label, 0, label.length, tagSize)
        nicePen.textSize = rectangle.width().times(-0.205F)

        //rectangle points are inverted!?
        var y= rectangle.bottom - rectangle.height()/2

        for (line in label.split("\n")) {
            canvas.drawText(line, rectangle.left, y, nicePen)
            canvas.drawText(line, rectangle.left, y, pen)
            y += pen.descent() - pen.ascent()
        }
    }


    fun draw(rect: RectF) {
        this.rectangle = rect
        invalidate()
    }

    fun drawBoxes(boxes: List<RectF>){
        this.rectangles.clear()
        this.rectangles.addAll(boxes)
        invalidate()
    }
}

class Dialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage("Make sure the logo is on the center")
            .setPositiveButton("OK") { _,_ -> }
            .create()

    companion object {
        const val TAG = "Dialog"
    }
}



