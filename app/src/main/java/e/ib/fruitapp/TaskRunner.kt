package e.ib.fruitapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageProxy
import e.ib.fruitapp.async.FruitTask
import e.ib.fruitapp.async.InitializeClassifierTask
import e.ib.fruitapp.async.PredictionTask
import e.ib.fruitapp.model.FruitDAO
import e.ib.fruitapp.model.PredictionDAO
import java.lang.Exception

object TaskRunner {

    private val handler = MainActivity.throwableHandler

    private val matches = setOf(
        "strawberry",
        "orange",
        "lemon",
        "pineapple",
        "banana"
    )


    fun fruitDetails(param : String) : FruitDAO? {
        return try {
            val task = FruitTask()
            task.execute(param).get()
        } catch (t : Throwable) {
            Log.d("throwable", t.localizedMessage?:"")
            handler.handle(t)
            null
        }
    }


    fun initializeNetwork(param: Classifier) {
        try {
            val task = InitializeClassifierTask()
            task.execute(param)
            val e = Exception("Model loaded") //uses message of it in toast
            handler.handle(e)
        } catch (t : Throwable) {
            handler.handle(t)
        }
    }

    fun predict(model: Classifier, param: Bitmap): String {
        val predictionDAO = PredictionDAO(model, param)
        val task = PredictionTask()
        return try {
            task.execute(predictionDAO).get()
        } catch (t : Throwable) {
            handler.handle(t)
            ""
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    fun processPicture(image: ImageProxy, tflite: Classifier, context: Context): Array<String>? {
        val bitmap = image.image?.toBitmap() //image.image is experimental
        Log.d("BITMAP", "${bitmap?.height} ${bitmap?.width}")
        if (bitmap != null) {
            val output = this.predict(tflite, bitmap)
            if (matches.contains(output)) {
                val fruitDAO = this.fruitDetails(output)

                if (fruitDAO != null) {
                    image.close()
                    return fruitDAO.getValues()

                }
            } else {
                Toast.makeText(context, "This is: $output", Toast.LENGTH_SHORT).show()
            }
            Log.d("BITMAP", output)
        }
        image.close()
        Log.d("df", "onImageCaptured failed")
        return null
    }

    private fun Image.toBitmap(): Bitmap {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

}