package sdm.com.asturexplorers

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import sdm.com.asturexplorers.db.Ruta
import sdm.com.asturexplorers.db.RutasDatabase
import sdm.com.asturexplorers.json.JsonParser


class RutasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var dbRutas: RutasDatabase? = null
    private lateinit var jsonParser: JsonParser
    private val db = FirebaseFirestore.getInstance()
    private lateinit var svRutas: SearchView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbRutas = RutasDatabase.getDB(requireContext())
        val gson = Gson()
        jsonParser = JsonParser(gson)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rutas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inicializar()
        buscador()
    }

    private fun buscador() {
        svRutas = requireView().findViewById(R.id.svRutas)

        svRutas.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    buscarRutasPorNombre(newText)
                } else {
                    mostrarTodasLasRutas()
                }
                return true
            }
        })
    }

    private fun mostrarTodasLasRutas() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Si no hay búsqueda, muestra todas las rutas
            val listaRutas = dbRutas!!.rutaDao.getAll()
            withContext(Dispatchers.Main) {
                // Actualiza el RecyclerView con todas las rutas
                recyclerView.adapter = RutasAdapter(
                    listaRutas,
                    onFavoriteClick = { ruta -> onFavoritoClicked(ruta) },
                    onClickListener = { ruta -> onClickedRuta(ruta) }
                )
            }
        }
    }

    private fun buscarRutasPorNombre(name: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val rutasEncontradas = dbRutas!!.rutaDao.searchByName(name)

            withContext(Dispatchers.Main) {
                // Actualiza el RecyclerView con las rutas encontradas
                recyclerView.adapter = RutasAdapter(
                    rutasEncontradas,
                    onFavoriteClick = { ruta -> onFavoritoClicked(ruta) },
                    onClickListener = { ruta -> onClickedRuta(ruta) }
                )
            }
        }
    }

    companion object {
        fun newInstance(): RutasFragment = RutasFragment()
    }

    private fun inicializar() {
        recyclerView = requireView().findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Añadir las rutas a Firebase por primera vez
        //subirRutasAFirebase(rutas)

        lifecycleScope.launch(Dispatchers.IO) {
            var listaRutas = dbRutas!!.rutaDao.getAll()
            if (listaRutas.isEmpty()) {
                val json = requireContext().assets.open("rutas.json").bufferedReader().use { it.readText() }

                val (rutas, tramos) = jsonParser.parseRutas(json)
                dbRutas!!.rutaDao.insertAll(rutas)
                dbRutas!!.tramoDao.insertAll(tramos)
                listaRutas = rutas
            }
            withContext(Dispatchers.Main) {
                recyclerView.adapter = RutasAdapter(
                    listaRutas,
                    onFavoriteClick = { ruta -> onFavoritoClicked(ruta) },
                    onClickListener = { ruta -> onClickedRuta(ruta) }
                )
            }
        }
        //recyclerView.adapter = RutasAdapter(rutas)
    }

    private fun onClickedRuta(ruta: Ruta?) {
        if (ruta != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                val tramosArray = dbRutas!!.tramoDao.getTramoById(ruta.id)
                val destino = RutasFragmentDirections.actionNavigationRutasToRutasDetalle(
                    ruta,
                    tramosArray.toTypedArray()
                )
                findNavController().navigate(destino)
            }

        }
    }

    // opción de añadir a favoritos
    private fun onFavoritoClicked(ruta: Ruta) {
        // Comprobar si el usuario está autenticado
        val user = SessionManager.currentUser
        if (user != null) {
            // Si el usuario está autenticado
            Log.d("RutasFragment", "AÑADIENDO A FAV: Usuario autenticado: ${user.uid}")
            // Crear un mapa con los datos que quieres guardar (solo el id de la ruta por ahora)


            // Añadir la ruta a la colección "favoritos" del usuario
            db.collection("rutas_favs")
                .document(user.uid)
                .update("favoritas", FieldValue.arrayUnion(ruta.id))
                .addOnSuccessListener {
                    // Mostrar mensaje de SnackBar indicando que se ha añadido a favoritos
                    Snackbar.make(requireView(), "Ruta añadida a favoritos", Snackbar.LENGTH_SHORT).show()
                    Log.w("RutasFragment", "Ruta añadida a favoritos ${ruta.nombre}")

                }
                .addOnFailureListener { e ->
                    // Si hay algún error al añadir, mostrar mensaje de error
                    Snackbar.make(requireView(), "Error al añadir la ruta a favoritos", Snackbar.LENGTH_SHORT).show()
                    Log.w("RutasFragment", "Error al añadir ruta a favoritos", e)
                }

        } else {
            // Si el usuario no está autenticado
            Log.d("RutasFragment", "AÑADIENDO A FAV: No hay usuario autenticado.")
        }
    }

    private fun subirRutasAFirebase(rutas: List<Ruta>) {
        // Iterar sobre la lista de rutas y subirlas a Firestore
        for (ruta in rutas) {
            val rutaData = hashMapOf(
                "id" to ruta.id,
                "nombre" to ruta.nombre,
                "distancia" to ruta.distancia,
                "dificultad" to ruta.dificultad,
                "tipoRecorrido" to ruta.tipoRecorrido,
                "imagenUrl" to ruta.imagenUrl
            )

            // Crear un nuevo documento en la colección "rutas"
            db.collection("rutas")
                .add(rutaData)
                .addOnSuccessListener { documentReference ->
                    // Ruta subida exitosamente
                    Log.d("Firestore", "Ruta añadida con ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    // Error al añadir la ruta
                    Log.w("Firestore", "Error añadiendo la ruta", e)
                }
        }
    }

}


