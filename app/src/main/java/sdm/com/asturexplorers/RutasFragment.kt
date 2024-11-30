package sdm.com.asturexplorers

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

import sdm.com.asturexplorers.db.Ruta
import sdm.com.asturexplorers.db.RutasDatabase
import sdm.com.asturexplorers.db.Tramo
import sdm.com.asturexplorers.json.JsonParser


class RutasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var dbRutas: RutasDatabase? = null
    private val rutas = mutableListOf<Ruta>()
    private val tramos = mutableListOf<Tramo>()
    private lateinit var jsonParser: JsonParser
    private val db = FirebaseFirestore.getInstance()


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
    }

    companion object {
        fun newInstance(): RutasFragment = RutasFragment()
    }

    private fun inicializar() {
        recyclerView = requireView().findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val json = requireContext().assets.open("rutas.json").bufferedReader().use { it.readText() }

        val (rutas, tramos) = jsonParser.parseRutas(json)

        this.rutas.addAll(rutas)
        this.tramos.addAll(tramos)

        // Añadir las rutas a Firebase por primera vez
        //subirRutasAFirebase(rutas)

        recyclerView.adapter = RutasAdapter(
            rutas,
            onFavoriteClick = { ruta ->
                onFavoritoClicked(ruta) // Manejo del clic en el botón de favorito
            },
            onClickListener = { ruta ->
                // Manejo del clic en un elemento de la lista
                if (ruta != null){
                    val tramosArray = tramos.filter { tramo -> tramo.rutaId == ruta.id }
                    val destino = RutasFragmentDirections.actionNavigationRutasToRutasDetalle(ruta,
                        tramosArray.toTypedArray()
                    )
                    findNavController().navigate(destino)
                }
            }
        )
        //recyclerView.adapter = RutasAdapter(rutas)
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
                "tipo_recorrido" to ruta.tipoRecorrido,
                "imagen_url" to ruta.imagenUrl
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


