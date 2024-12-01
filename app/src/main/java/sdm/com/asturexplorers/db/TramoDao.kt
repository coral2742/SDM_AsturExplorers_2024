package sdm.com.asturexplorers.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TramoDao {

    @Query("DELETE FROM tramos")
    suspend fun deleteAllTramos()

    @Query("SELECT * FROM tramos")
    suspend fun getAll() : List<Tramo>

    @Query("SELECT * FROM tramos WHERE ruta_id = :rutaId")
    suspend fun getTramoById(rutaId: Int): List<Tramo>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tramos: List<Tramo>)

    @Insert
    suspend fun add(tramo: Tramo)

    @Update
    suspend fun update(tramo: Tramo)

    @Delete
    suspend fun remove(tramo: Tramo)
}