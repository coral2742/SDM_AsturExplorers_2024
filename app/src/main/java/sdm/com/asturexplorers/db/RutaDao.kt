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

    @Query("DELETE FROM rutas")
    suspend fun deleteAllRutas()

    @Query("""
        SELECT * FROM rutas
        WHERE distancia BETWEEN :minDistancia AND :maxDistancia
        AND dificultad IN (:dificultades)
        AND tipoRecorrido IN (:tiposRecorrido)
        AND (:nombre IS NULL OR nombre LIKE '%' || :nombre || '%') 
    """)
    suspend fun filtrarYBuscarRutas(
        minDistancia: Double,
        maxDistancia: Double,
        dificultades: MutableList<String>,
        tiposRecorrido: MutableList<String>,
        nombre: String?): List<Ruta>

    @Query("""
        SELECT * FROM rutas
        WHERE id IN (:ids)
        AND distancia BETWEEN :minDistancia AND :maxDistancia
        AND dificultad IN (:dificultades)
        AND tipoRecorrido IN (:tiposRecorrido)
        AND (:nombre IS NULL OR nombre LIKE '%' || :nombre || '%') 
    """)
    suspend fun filtrarYBuscarRutasPorIds(
        ids: List<Int>,
        minDistancia: Double,
        maxDistancia: Double,
        dificultades: MutableList<String>,
        tiposRecorrido: MutableList<String>,
        nombre: String?
    ): List<Ruta>

}