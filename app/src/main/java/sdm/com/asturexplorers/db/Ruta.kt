package sdm.com.asturexplorers.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "rutas")
data class Ruta(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val distancia: Double,
    val dificultad: String,
    val tipoRecorrido: String,
    val descripcion: String,
    @ColumnInfo(name = "imagen_url")
    val imagenUrl: String
): Parcelable {
}