package e.ib.fruitapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import android.os.SystemClock
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
import androidx.core.graphics.get
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {

    companion object {
        val throwableHandler = ThrowableHandler.getInstance()
    }

    class ThrowableHandler private constructor() {

        companion object {
            private var handler = ThrowableHandler()

            fun getInstance() : ThrowableHandler {
                return handler
            }

        }

        internal lateinit var applicationContext : Context
        fun handle(t : Throwable) {
            Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
        }
    }

    private val matches = setOf(
        "strawberry",
        "orange",
        "lemon",
        "pineapple",
        "banana"
    )

    lateinit var tflite: Classifier
    lateinit var cameraView: CameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        throwableHandler.applicationContext = applicationContext

        cameraView = findViewById(R.id.camera_view)

        val capture_image = findViewById<Button>(R.id.capture_image)

        capture_image.setOnClickListener { onClickBtn(it) }

        try {
            tflite = Classifier(applicationContext)
            tflite.initializeInterpreter()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (checkPermissionsCamera()) startCameraSession() else requestPermissionsCamera()
        if (!checkPermissionsInternet()) requestPermissionsInternet()

    }


/*    private fun doInference(bitmap: Bitmap): FloatArray{
        val n_width = 300
        val n_height = 300
        val resizedImage =
            Bitmap.createScaledBitmap(bitmap, n_width, n_height, true)

        val byteBuffer = convertBitmapToByteBuffer(resizedImage)

        val output = floatArrayOf(0f, 0f, 0f, 0f)
        val startTime = SystemClock.uptimeMillis()
        tflite.run(byteBuffer, output)
        val endTime = SystemClock.uptimeMillis()

        var inferenceTime = endTime - startTime
        return output
    }
*/
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val IMAGE_MEAN = 128;
        val IMAGE_STD = 128.0f;
        val modelInputSize = 4 * 300 * 300 * 3
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until bitmap.width) {
            for (j in 0 until bitmap.height) {
                val pixelVal = pixels[pixel++]

                byteBuffer.putFloat((((pixelVal shr 16 and 0xFF).toFloat() - IMAGE_MEAN)/IMAGE_STD))
                byteBuffer.putFloat((((pixelVal shr 8 and 0xFF).toFloat() - IMAGE_MEAN)/IMAGE_STD))
                byteBuffer.putFloat((((pixelVal and 0xFF).toFloat() - IMAGE_MEAN)/IMAGE_STD))
                Log.d("XDDDDDDDD", pixel.toString())
            }
        }
        bitmap.recycle()

        return byteBuffer
    }

    private fun requestPermissionsInternet() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET),101)
    }

    private fun requestPermissionsCamera() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),101)
    }

    private fun checkPermissionsCamera(): Boolean {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissionsInternet(): Boolean {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCameraSession() {
        camera_view.bindToLifecycle(this)
    }

    private fun loadModelFile() : MappedByteBuffer {
        val fileDescriptor = this.assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }


    fun onClickBtn(view: View) {
        cameraView.takePicture(ContextCompat.getMainExecutor(this), object: ImageCapture.OnImageCapturedCallback() {
            @SuppressLint("UnsafeExperimentalUsageError")
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = image.image?.toBitmap() //image.image is experimental
                Log.d("BITMAP", "${bitmap?.height} ${bitmap?.width}")
                if (bitmap != null) {
                    val output = tflite.classify(bitmap)
                    if (matches.contains(output)) {
                        val fruitDAO = TaskRunner.fruitDetails(output)
                        Toast.makeText(this@MainActivity, fruitDAO.toString(), Toast.LENGTH_LONG).show() /////////////TODO WYSWIETLANIE OWOCA
                    } else {
                        Toast.makeText(this@MainActivity, "This is: $output", Toast.LENGTH_SHORT).show()/////////TODO WYSWIETLANIE INNYCH
                    }
                    Log.d("BITMAP", output)
                }
                image.close()
                Log.d("df", "onImageCaptured")
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(this@MainActivity, "Image Capture Failed", Toast.LENGTH_SHORT).show()
                Log.e("XD", "onError $exception")
                exception.printStackTrace()
            }
        })
    }

    fun Image.toBitmap(): Bitmap {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

















    fun toRGBArray(bitmap: Bitmap): Array<Array<Array<Float>>>{
        var otp = Array(300) {i ->
            Array(300) { j ->
                Array(3){ k -> 0f }
            }
        }
        Log.d("BITMAP", "${otp.size} ${otp[0].size} ${otp[0][0].size}")
        for (x in 0 until 300) {
            for(y in 0 until 300) {
                val p = bitmap[x, y]
                val R = p and 0xff0000 shr 16
                val G = p and 0x00ff00 shr 8
                val B = p and 0x0000ff shr 0
                otp[x][y][0] = R.toFloat()
                otp[x][y][1] = G.toFloat()
                otp[x][y][2] = B.toFloat()
            }
        }
        return otp
    }

}

