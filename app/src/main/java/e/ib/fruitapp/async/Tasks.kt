package e.ib.fruitapp.async

import android.os.AsyncTask
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import e.ib.fruitapp.model.FruitDAO
import e.ib.fruitapp.model.RestTemplateProvider


private val gson = Gson()

//JSON mapuję GSON'em, gdyż w spring-android, w przeciwieństwie do normalnego springa,
// nie ma ParameterizedTypeReference pozwalającego mapować na typy generyczne

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




