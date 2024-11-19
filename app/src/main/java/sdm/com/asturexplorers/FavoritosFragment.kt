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
import sdm.com.asturexplorers.db.Tramo

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
                    recyclerView.adapter = RutasAdapter(
                        rutasFavoritas,
                        onFavoriteClick = { ruta ->
                            onFavoritoClicked(ruta) // Manejo del clic en el botón de favorito
                        },
                        onClickListener = { ruta ->
                            // Manejo del clic en un elemento de la lista
                            if (ruta != null){
                                val tramosArray = Array<Tramo>(1) { Tramo(0, ruta.id, 10.0, "", "\"value\": \"<p style=\\\"text-align: justify;\\\">La ruta da comienzo en O Teixo (Taramundi), entre las cumbres del Ouroso y el Busnovo. En las inmediaciones del albergue se emprende el camino por la carretera que va hacia Santa Mariña. En las últimas casas de O Teixo se toma a la derecha una pista, hasta encontrar un cruce en el que cogemos el desvío de la izquierda y que discurre por debajo de la carretera. Cuando llegamos a un \\u2018castañéu', la pista se transforma en una senda que lo atraviesa hasta el pueblo de Santa Mariña, ejemplo de arquitectura rural del occidente asturiano, con los característicos volúmenes herméticos, muros de mampostería con piedra vista y cubierta de pizarra.<\\/p>\\n\\n<p style=\\\"text-align: justify;\\\">Desde Santa Mariña continuamos por la carretera que sale a la derecha y se dirige hacia otro ejemplo de la arquitectura rural, y desde aquí nuestro camino se encarama en lo alto de las crestas donde encontramos una pista que continúa hacia la izquierda, entre una zona de brezales y, que nos conduce hacia el caserío de Os Armallos.<\\/p>\\n\\n<p style=\\\"text-align: justify;\\\">Aquí el camino gira a la derecha, para descender entre prados y castañales hasta la carretera que une Taramundi con As Veigas, y desde ésta por un sendero hacia el conjunto etnográfico de Os Teixóis, perfecto ejemplo de lo que en el siglo XVIII fue una próspera industria en todo el Occidente de Asturias. Os Teixóis reúne toda una serie de ingenios movidos por agua: un molino, el mazo de una ferrería, un batán para el tratamiento de la lana, piedras de afilar y una primitiva central eléctrica.<\\/p>\\n\\n<p style=\\\"text-align: justify;\\\">Una vez visitado el conjunto, nos dirigimos hacia la cercana aldea de As Mestas por un sendero que transcurre al lado del arroyo. Ignoramos los cuatro primeros cruces que encontramos, para posteriormente coger el siguiente a la derecha, que nos conducirá a una pista que tomamos a la derecha.<\\/p>\\n\\n<p style=\\\"text-align: justify;\\\">Por el camino encontraremos las aún identificables \\u2018carboeiras' que, alimentadas con el abundante roble de la zona, producían el carbón vegetal consumido en las antaño numerosas ferrerías. Una vez en As Mestas tomamos la pista que lo une con O Teixo.<\\/p>\"\n") }  // Suponiendo que Tramo tiene un constructor sin parámetros
                                val destino = RutasFragmentDirections.actionNavigationRutasToRutasDetalle(ruta,
                                    tramosArray
                                )
                                findNavController().navigate(destino)
                            }
                        }
                    )
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
