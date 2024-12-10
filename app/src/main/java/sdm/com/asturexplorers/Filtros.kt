package sdm.com.asturexplorers

data class Filtros(
    val minDistancia: Int,
    val maxDistancia: Int,
    val pie: Boolean,
    val bicicleta: Boolean,
    val coche: Boolean,
    val facil: Boolean,
    val moderado: Boolean,
    val dificil: Boolean
)