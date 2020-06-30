package e.ib.fruitapp.model

import android.graphics.Bitmap
import e.ib.fruitapp.Classifier

data class FruitDAO(
    val genus: String?,
    val name: String?,
    val id: Long?,
    val family: String?,
    val order: String?,
    val nutritions: NutritionsDAO?
) {

    override fun toString(): String {
        return "$name (values per 100 g): \n$nutritions"
    }

    fun getValues() :Array<String> {
        return arrayOf("$name", "(values per 100 g): \n" +
                "$nutritions")
    }
}

data class NutritionsDAO(
    val carbohydrates: Double?,
    val protein: Double?,
    val fat: Double?,
    val calories: Double?,
    val sugar: Double?
) {

    override fun toString(): String {
        return "carbohydrates:\t$carbohydrates g\nprotein:\t$protein g\nfat:\t$fat g\ncalories:\t$calories kcal\nsugar:\t$sugar g"
    }
}

data class PredictionDAO(
    val classifier: Classifier,
    val bitmap: Bitmap
)