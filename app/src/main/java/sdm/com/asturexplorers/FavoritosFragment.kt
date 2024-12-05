package sdm.com.asturexplorers

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sdm.com.asturexplorers.db.Ruta
import sdm.com.asturexplorers.db.RutasDatabase

class FavoritosFragment : Fragment() {
    private lateinit var tvWelcomeMessage: TextView
    private lateinit var btnIniciarSesion: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RutasFavAdapter
    private lateinit var svRutas: SearchView
    private val rutasFavoritas = mutableListOf<Ruta>()
    private val db = FirebaseFirestore.getInstance()
    private var dbRutas: RutasDatabase? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favoritos, container, false)

        tvWelcomeMessage = view.findViewById(R.id.textWelcomeMessage)
        btnIniciarSesion = view.findViewById(R.id.btnIniciarSesion)
        recyclerView = view.findViewById(R.id.recyclerFavoritos)
        svRutas = view.findViewById(R.id.svRutasFav)

        dbRutas = RutasDatabase.getDB(requireContext())


        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        setupUI()

        btnIniciarSesion.setOnClickListener {
            // Redirigir al fragment de MiCuenta para iniciar sesión
            val navController = findNavController()
            navController.popBackStack(R.id.navigation_rutas, false)
            navController.navigate(R.id.navigation_mi_perfil)
        }

        return view
    }

    companion object {
        fun newInstance(): FavoritosFragment = FavoritosFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inicializar()
        buscador()
    }

    private fun buscador() {
        val currentUser = SessionManager.currentUser
        if (currentUser != null) {
            svRutas.setOnQueryTextListener(object : SearchView.OnQueryTextListener {


                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (!newText.isNullOrEmpty()) {
                        buscarRutasPorNombre(newText, currentUser.uid)
                    } else {
                        cargarRutasFavoritas(currentUser.uid)
                    }
                    return true
                }
            })
        }

    }

    private fun buscarRutasPorNombre(newText: String, userId: String) {
        db.collection("rutas_favs")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val favoritas = document.get("favoritas") as? List<Int> ?: emptyList()
                if (favoritas.isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO){
                        val rutas = dbRutas!!.rutaDao.searchByNameAndId(favoritas, newText)

                        withContext(Dispatchers.Main) {
                            recyclerView.adapter = RutasFavAdapter(
                                rutas,
                                onFavoriteClick = { ruta -> onFavoritoClicked(ruta) // Manejo del clic en el botón de favorito
                                },
                                onClickListener = { ruta ->
                                    // Manejo del clic en un elemento de la lista
                                    //if (ruta != null){
                                    //val tramosArray = tramos.filter { tramo -> tramo.rutaId == ruta.id }
                                    //val destino = RutasFragmentDirections.actionNavigationRutasToRutasDetalle(ruta,
                                    //    tramosArray.toTypedArray()
                                    //)
                                    //findNavController().navigate(destino)
                                    // }
                                }
                            )
                        }
                    }
                } else {
                    tvWelcomeMessage.text = "No tienes rutas favoritas aún."
                    recyclerView.visibility = View.GONE
                }

                Log.w("FavoritosFragment", "Rutas favoritas cargadas ${favoritas}")
            }
            .addOnFailureListener { e ->
                Log.e("FavoritosFragment", "Error al cargar las rutas favoritas", e)
            }
    }

    private fun inicializar() {
        recyclerView = requireView().findViewById(R.id.recyclerFavoritos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        //val json = requireContext().assets.open("rutas.json").bufferedReader().use { it.readText() }

        //val (rutas, tramos) = jsonParser.parseRutas(json)

        //this.rutas.addAll(rutas)
        //this.tramos.addAll(tramos)

        // Añadir las rutas a Firebase por primera vez
        //subirRutasAFirebase(rutas)

        //setupUI();


        //recyclerView.adapter = RutasAdapter(rutas)
    }



    private fun setupUI() {
        val currentUser = SessionManager.currentUser

        if (currentUser != null) {
            tvWelcomeMessage.text = "¡Bienvenido, ${currentUser.displayName}!"
            btnIniciarSesion.visibility = View.GONE

            Log.w("FavoritosFragment", "Cargando las rutas favoritas")

            lifecycleScope.launch(Dispatchers.IO) {
                // Cargar las rutas favoritas del usuario en un hilo secundario
                cargarRutasFavoritas(currentUser.uid)
            }

        } else {
            tvWelcomeMessage.text = "Inicia sesión para ver tus rutas favoritas."
            btnIniciarSesion.visibility = View.VISIBLE
        }
    }

    private fun cargarRutasFavoritas(userId: String) {
        // Obtenemos las rutas favoritas del usuario
        db.collection("rutas_favs")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val favoritas = document.get("favoritas") as? List<Int> ?: emptyList()
                if (favoritas.isNotEmpty()) {
                    // Cargar los detalles de las rutas favoritas
                    obtenerDetallesRutasFavoritas(favoritas)
                } else {
                    tvWelcomeMessage.text = "No tienes rutas favoritas aún."
                    recyclerView.visibility = View.GONE
                }

                Log.w("FavoritosFragment", "Rutas favoritas cargadas ${favoritas}")
            }
            .addOnFailureListener { e ->
                Log.e("FavoritosFragment", "Error al cargar las rutas favoritas", e)
            }
    }

    private fun obtenerDetallesRutasFavoritas(favoritas: List<Int>) {
        val rutasFavoritas = mutableListOf<Ruta>()  // Lista para almacenar las rutas favoritas recuperadas

        // Recorremos la lista de IDs de rutas favoritas
        for (rutaId in favoritas) {
            getRutaById(rutaId,
                onSuccess = { ruta ->
                    ruta?.let {
                        Log.d("RutasFragment", "Ruta encontrada: imagen -> ${ruta.imagenUrl} <-")
                        rutasFavoritas.add(ruta)  // Agregamos la ruta a la lista de favoritas

                        // Verificamos si ya hemos agregado todas las rutas favoritas
                        if (rutasFavoritas.size == favoritas.size) {
                            // Aquí podemos actualizar el RecyclerView con la lista completa de rutas


                            recyclerView.adapter = RutasFavAdapter(
                                rutasFavoritas,
                                onFavoriteClick = { ruta ->
                                    onFavoritoClicked(ruta) // Manejo del clic en el botón de favorito
                                },
                                onClickListener = { ruta ->
                                    // Manejo del clic en un elemento de la lista
                                    //if (ruta != null){
                                        //val tramosArray = tramos.filter { tramo -> tramo.rutaId == ruta.id }
                                        //val destino = RutasFragmentDirections.actionNavigationRutasToRutasDetalle(ruta,
                                        //    tramosArray.toTypedArray()
                                        //)
                                        //findNavController().navigate(destino)
                                   // }

                                    Log.d("Ruta", "Clic on ruta");
                                }
                            )
                        }
                    } ?: run {
                        Log.d("RutasFragment", "No se encontró la ruta con el ID: $rutaId")
                    }
                },
                onFailure = { exception ->
                    Log.e("RutasFragment", "Error al buscar la ruta con ID: $rutaId", exception)
                }
            )
        }



    }


    private fun getRutaById(rutaId: Int, onSuccess: (Ruta?) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("rutas")
            .whereEqualTo("id", rutaId)  // Filtrar por el campo "id" dentro del documento
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Si existe al menos un documento que cumple con la condición
                    val ruta = querySnapshot.documents.firstOrNull()?.toObject(Ruta::class.java)
                    Log.d("RutasFragment", "Ruta encontrada: -> ${ruta} <-")
                    onSuccess(ruta)
                } else {
                    // No se encontró ningún documento con ese "id"
                    onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }







    private fun onFavoritoClicked(ruta: Ruta) {
        val currentUser = SessionManager.currentUser
        Log.d("RutasFragment", "Ruta encontrada: imagen -> ${ruta.imagenUrl} <-")
        if (currentUser != null) {
            Log.w("FavoritosFragment", "Ruta seleccionada para eliminar: ${ruta.nombre}")

            // Eliminar la ruta de la colección "favoritas" del usuario
            db.collection("rutas_favs")
                .document(currentUser.uid)
                .update("favoritas", FieldValue.arrayRemove(ruta.id))
                .addOnSuccessListener {
                    setupUI()  // Esto recarga las rutas favoritas en la UI
                    // Mostrar mensaje de SnackBar indicando que se ha eliminado de favoritos
                    Snackbar.make(requireView(), "Ruta eliminada de favoritos", Snackbar.LENGTH_SHORT).show()
                    Log.w("FavoritosFragment", "Ruta eliminada de favoritos ${ruta.nombre}")

                    // Volver a cargar las rutas favoritas después de la eliminación
                    setupUI()

                }
                .addOnFailureListener { e ->
                    // Si hay algún error al eliminar, mostrar mensaje de error
                    Snackbar.make(requireView(), "Error al eliminar la ruta de favoritos", Snackbar.LENGTH_SHORT).show()
                    Log.w("FavoritosFragment", "Error al eliminar ruta de favoritos", e)
                }


        } else {
            Log.w("FavoritosFragment", "Usuario no autenticado")
        }
    }


}
