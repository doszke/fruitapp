package e.ib.fruitapp

import android.util.Log
import e.ib.fruitapp.async.FruitTask
import e.ib.fruitapp.model.FruitDAO

object TaskRunner {

    private val handler = MainActivity.throwableHandler


    fun fruitDetails(param : String) : FruitDAO? {
        return try {
            val task = FruitTask()
            task.execute(param).get()
        } catch (t : Throwable) {
            Log.d("throwable", t.localizedMessage)
            handler.handle(t)
            null
        }
    }



}