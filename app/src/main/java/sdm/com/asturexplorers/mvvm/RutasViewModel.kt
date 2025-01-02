package sdm.com.asturexplorers.mvvm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sdm.com.asturexplorers.SessionManager
import sdm.com.asturexplorers.db.Ruta
import sdm.com.asturexplorers.db.RutasDatabase
import sdm.com.asturexplorers.json.JsonParser

class RutasViewModel(
    private val dbRutas: RutasDatabase?,
    private val db : FirebaseFirestore
): ViewModel() {

    private val _rutasLiveData = MutableLiveData<List<Ruta>>()
    val rutasLiveData: LiveData<List<Ruta>> get() = _rutasLiveData

    private val _favoritoLiveData = MutableLiveData<Boolean?>()
    val favoritoLiveData: LiveData<Boolean?> get() = _favoritoLiveData

    private val _rutasFavoritas = MutableLiveData<Set<Int>>()
    val rutasFavoritas: LiveData<Set<Int>> get() = _rutasFavoritas

    private val _buscador = MutableLiveData<String>()
    val buscador: LiveData<String> get() = _buscador

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
            } else{
                _rutasLiveData.postValue(rutasLocales)
            }
        }
    }

    fun cargarRutasFavoritas() {
        val user = SessionManager.currentUser
        if (user != null){
            FirebaseFirestore.getInstance()
                .collection("rutas_favs")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    val favoritasIds = document.get("favoritas") as? List<*>
                    val ids = favoritasIds?.mapNotNull {
                        if (it is Long){
                            it.toInt()
                        } else{
                            null
                        }
                    }?.toSet() ?: emptySet()
                    _rutasFavoritas.postValue(ids)
                }
                .addOnFailureListener {
                    _rutasFavoritas.postValue(emptySet())
                }
        }

    }

    fun esFavorita(ruta: Ruta) : Boolean {
        return _rutasFavoritas.value?.contains(ruta.id) ?: false
    }

    fun manejarFavoritos(ruta : Ruta){
        val user = SessionManager.currentUser
        if (user != null){
            if (esFavorita(ruta) == true){
                eliminarFavortios(ruta, user)
                val nuevasFavoritas = _rutasFavoritas.value!! - ruta.id
                _rutasFavoritas.postValue(nuevasFavoritas)
            } else{
                agregarAFavoritos(ruta, user)
                val nuevasFavoritas = _rutasFavoritas.value!! + ruta.id
                _rutasFavoritas.postValue(nuevasFavoritas)
            }
        } else{
            resetFavoritoState()
        }
    }

    private fun eliminarFavortios(ruta: Ruta, user: FirebaseUser) {
        db.collection("rutas_favs")
            .document(user.uid)
            .update("favoritas", FieldValue.arrayRemove(ruta.id))
            .addOnSuccessListener {
                Log.w("FavoritosFragment", "Ruta eliminada de favoritos ${ruta.nombre}")
                _favoritoLiveData.value = false
            }
            .addOnFailureListener { e ->
                Log.w("FavoritosFragment", "Error al eliminar ruta de favoritos", e)
                _favoritoLiveData.value = false
            }
    }


    private fun agregarAFavoritos(ruta: Ruta, user: FirebaseUser) {
            // Si el usuario está autenticado
            Log.d("RutasViewModel", "AÑADIENDO A FAV: Usuario autenticado: ${user.uid}")
            db.collection("rutas_favs")
                .document(user.uid)
                .update("favoritas", FieldValue.arrayUnion(ruta.id))
                .addOnSuccessListener {
                    // Notificar éxito
                    _favoritoLiveData.postValue(true)
                    Log.w("RutasViewModel", "Ruta añadida a favoritos ${ruta.nombre}")
                }
                .addOnFailureListener { e ->
                    // Notificar fallo
                    _favoritoLiveData.postValue(false)
                    Log.w("RutasViewModel", "Error al añadir ruta a favoritos", e)
                }
    }

    fun aplicarFiltrosYBusqueda(
        minDistancia: Double,
        maxDistancia: Double,
        dificultades: MutableList<String>,
        tiposRecorrido: MutableList<String>,
        query: String?
    ) {

        viewModelScope.launch(Dispatchers.IO) {
            val rutasFiltradas = dbRutas!!.rutaDao.filtrarYBuscarRutas(
                minDistancia,
                maxDistancia,
                dificultades,
                tiposRecorrido,
                query ?: ""
            )
            _rutasLiveData.postValue(rutasFiltradas)
        }
    }

    fun resetFavoritoState() {
        _favoritoLiveData.postValue(null)
    }

    fun buscar(newText: String?) {
        _buscador.postValue(newText ?: "")
    }

}