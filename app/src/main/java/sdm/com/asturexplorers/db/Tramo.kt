package sdm.com.asturexplorers.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tramos")
data class Tramo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "ruta_id")
    val rutaId: Int,
    val distancia: Double,
    @ColumnInfo(name = "origen_destino")
    val origenDestino: String,
    val descripcion: String
) : Parcelable