package sdm.com.asturexplorers

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Ruta(
    val id: Int, val nombre: String, val distancia: Double, val dificultad: String, val tipoRecorrido: String,
    val descripcion: String, val imagenUrl: String): Parcelable {
}