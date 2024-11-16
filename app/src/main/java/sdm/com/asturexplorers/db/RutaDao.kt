package sdm.com.asturexplorers.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RutaDao {

    @Query("SELECT * FROM rutas")
    suspend fun getAll() : List<Ruta>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rutas: List<Ruta>)

    @Insert
    suspend fun add(ruta: Ruta)

    @Update
    suspend fun update(ruta: Ruta)

    @Delete
    suspend fun remove(ruta: Ruta)

}