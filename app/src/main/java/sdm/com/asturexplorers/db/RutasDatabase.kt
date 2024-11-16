package sdm.com.asturexplorers.db

import android.content.Context
import androidx.room.Database
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper

@Database(entities = [Ruta::class], version = 1, exportSchema = false)
abstract class RutasDatabase : RoomDatabase(){
    abstract val rutaDao : RutaDao;

    companion object {
        @Volatile
        private var SINGLETON: RutasDatabase? = null

        fun getDB(context: Context?): RutasDatabase {
            if (SINGLETON != null)
                return SINGLETON as RutasDatabase


            val instancia = Room.databaseBuilder(context!!.applicationContext, RutasDatabase::class.java, "rutas.db")
                .createFromAsset("rutas.db")
                .build()
            SINGLETON = instancia
            return SINGLETON as RutasDatabase
        }
    }
}