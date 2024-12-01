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
    val id:Int = 0,
    val nombre: String,
    val distancia: Double,
    val dificultad: String,
    @ColumnInfo(name = "tipoRecorrido")
    val tipoRecorrido: String,
    @ColumnInfo(name = "imagenUrl")
    val imagenUrl: String
): Parcelable {
    constructor() : this(0, "", 0.0, "", "", "")
}