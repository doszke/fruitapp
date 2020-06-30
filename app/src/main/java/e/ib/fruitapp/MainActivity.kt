package e.ib.fruitapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        val throwableHandler = ThrowableHandler.getInstance()
    }

    class ThrowableHandler private constructor() {

        companion object {
            private var handler = ThrowableHandler()

            fun getInstance(): ThrowableHandler {
                return handler
            }

        }

        internal lateinit var applicationContext: Context
        fun handle(t: Throwable) {
            Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
        }
    }

    lateinit var tflite: Classifier
    lateinit var cameraView: CameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        throwableHandler.applicationContext = applicationContext

        cameraView = findViewById(R.id.camera_view)

        val capture_image = findViewById<Button>(R.id.capture_image)

        capture_image.setOnClickListener { onClickBtn(it) }

        tflite = Classifier(applicationContext)
        TaskRunner.initializeNetwork(tflite) //init of network

        if (checkPermissionsCamera()) startCameraSession() else requestPermissionsCamera()
        if (!checkPermissionsInternet()) requestPermissionsInternet()

    }

    private fun requestPermissionsInternet() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 101)
    }

    private fun requestPermissionsCamera() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
    }

    private fun checkPermissionsCamera(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissionsInternet(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.INTERNET
        )) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCameraSession() {
        camera_view.bindToLifecycle(this)
    }


    fun onClickBtn(view: View) {
        cameraView.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val vals = TaskRunner.processPicture(image, tflite, applicationContext)
                    if (vals != null) {
                        nameView.text = vals[0]
                        textView.text = vals[1]
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@MainActivity, "Image Capture Failed", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("ERROR", "onError $exception")
                    exception.printStackTrace()
                }
            })
    }

}

