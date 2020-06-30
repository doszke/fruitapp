package e.ib.fruitapp.async

import android.os.AsyncTask
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import e.ib.fruitapp.Classifier
import e.ib.fruitapp.model.FruitDAO
import e.ib.fruitapp.model.PredictionDAO
import e.ib.fruitapp.model.RestTemplateProvider

private val gson = Gson()


class FruitTask : AsyncTask<String, String, FruitDAO?>() {

    override fun doInBackground(vararg params: String): FruitDAO? {
        val uri = "http://www.fruityvice.com/api/fruit/${params[0]}"
        val result = RestTemplateProvider.provide().getForObject(uri, String::class.java)
        return try {
            gson.fromJson(result, FruitDAO::class.java)
        } catch (ex : JsonSyntaxException) {
            null
        }
    }

}

class InitializeClassifierTask: AsyncTask<Classifier, Unit, Unit>() {
    override fun doInBackground(vararg p0: Classifier) {
        p0[0].initializeInterpreter()
    }
}

class PredictionTask: AsyncTask<PredictionDAO, Unit, String>() {
    override fun doInBackground(vararg p0: PredictionDAO): String {
        val classifier = p0[0].classifier
        val bitmap = p0[0].bitmap
        return classifier.predict(bitmap)
    }

}