package sdm.com.asturexplorers

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sdm.com.asturexplorers.db.Ruta

class FavoritosFragment : Fragment() {
    private lateinit var tvWelcomeMessage: TextView
    private lateinit var btnIniciarSesion: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RutasAdapter
    private val rutasFavoritas = mutableListOf<Ruta>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favoritos, container, false)

        tvWelcomeMessage = view.findViewById(R.id.textWelcomeMessage)
        btnIniciarSesion = view.findViewById(R.id.btnIniciarSesion)
        recyclerView = view.findViewById(R.id.recyclerFavoritos)

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
                val favoritas = document.get("favoritas") as? List<String> ?: emptyList()
                if (favoritas.isNotEmpty()) {
                    // Cargar los detalles de las rutas favoritas
                    obtenerDetallesRutasFavoritas(favoritas)
                } else {
                    tvWelcomeMessage.text = "No tienes rutas favoritas aún."
                }

                Log.w("FavoritosFragment", "Rutas favoritas cargadas ${favoritas}")
            }
            .addOnFailureListener { e ->
                Log.e("FavoritosFragment", "Error al cargar las rutas favoritas", e)
            }
    }

    private fun obtenerDetallesRutasFavoritas(favoritas: List<String>) {
        // Recuperar las rutas favoritas de la base de datos 'rutas'
        db.collection("rutas")
            .whereIn("nombre", favoritas) // Suponiendo que 'nombre' es el campo que almacena el nombre de la ruta
            .get()
            .addOnSuccessListener { result ->
                rutasFavoritas.clear() // Limpiar la lista de rutas previas
                for (document in result) {
                    val ruta = document.toObject(Ruta::class.java) // Asumimos que Ruta es el modelo de datos para las rutas
                    rutasFavoritas.add(ruta) // Añadimos la ruta a la lista de favoritas
                }

                Log.w("FavoritosFragment", "Rutas favoritas listas ${rutasFavoritas}")

                // Actualizamos el RecyclerView con las rutas favoritas en el hilo principal
                lifecycleScope.launch(Dispatchers.Main) {
                    adapter = RutasAdapter(rutasFavoritas) { ruta ->
                        onFavoritoClicked(ruta)
                    }
                    recyclerView.adapter = adapter
                }
            }
            .addOnFailureListener { e ->
                Log.e("FavoritosFragment", "Error al obtener detalles de las rutas", e)
            }
    }

    private fun onFavoritoClicked(ruta: Ruta) {
        Log.w("FavoritosFragment", "Ruta seleccionada: ${ruta.nombre}")
    }
}
