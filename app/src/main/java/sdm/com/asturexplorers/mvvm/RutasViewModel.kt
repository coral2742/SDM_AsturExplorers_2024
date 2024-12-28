package sdm.com.asturexplorers.mvvm

import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sdm.com.asturexplorers.db.Ruta
import sdm.com.asturexplorers.db.RutasDatabase
import sdm.com.asturexplorers.json.JsonParser

class RutasViewModel(
    private val dbRutas: RutasDatabase?
): ViewModel() {

    private val _rutasLiveData = MutableLiveData<List<Ruta>>()
    val rutasLiveData: LiveData<List<Ruta>> get() = _rutasLiveData

    fun cargarRutasIniciales(json: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val rutasLocales = dbRutas!!.rutaDao.getAll()
            if (rutasLocales.isEmpty()) {
                val gson = Gson()
                val jsonParser = JsonParser(gson)

                // Parseamos el JSON que hemos recibido
                val (rutas, tramos) = jsonParser.parseRutas(json)

                // Insertamos las rutas y tramos en la base de datos
                dbRutas.rutaDao.insertAll(rutas)
                dbRutas.tramoDao.insertAll(tramos)

                _rutasLiveData.postValue(rutas)
            }
        }
    }

}