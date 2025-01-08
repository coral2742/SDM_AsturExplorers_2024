package sdm.com.asturexplorers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import sdm.com.asturexplorers.db.Ruta
import sdm.com.asturexplorers.db.RutasDatabase
import sdm.com.asturexplorers.json.JsonParser
import sdm.com.asturexplorers.mvvm.RutasFragmentViewModelFactory
import sdm.com.asturexplorers.mvvm.RutasViewModel


class RutasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var dbRutas: RutasDatabase? = null
    private lateinit var jsonParser: JsonParser
    private val db = FirebaseFirestore.getInstance()
    private lateinit var svRutas: SearchView
    private lateinit var btFiltros: Button
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var tvNoHayRutas: TextView
    private lateinit var viewModel : RutasViewModel
    private var rutasAdapter: RutasAdapter = RutasAdapter(
        listaRutas = emptyList(),
        onFavoriteClick = { ruta -> viewModel.manejarFavoritos(ruta) },
        onClickListener = { ruta -> onClickedRuta(ruta) },
        emptySet()
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbRutas = RutasDatabase.getDB(requireContext())
        viewModel = ViewModelProvider(this,
            RutasFragmentViewModelFactory(dbRutas, db)).get(RutasViewModel::class.java)
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
        configurarBuscador()

        viewModel.rutasLiveData.observe(viewLifecycleOwner, Observer { rutas ->
            actualizarRecyclerView(rutas)
        })

        viewModel.favoritoLiveData.observe(viewLifecycleOwner, Observer { exito ->
            when (exito) {
                true -> {
                    Snackbar.make(view, "Ruta añadida a favoritos", Snackbar.LENGTH_SHORT).show()
                }
                false -> {
                    Snackbar.make(view, "Ruta eliminada de favoritos", Snackbar.LENGTH_SHORT).show()
                }
                else -> {
                }
            }
            viewModel.resetFavoritoState()
        })

        viewModel.rutasFavoritas.observe(viewLifecycleOwner) { favoritasIds ->
            rutasAdapter.actualizarRutasFavoritas(favoritasIds)
        }

        viewModel.buscador.observe(viewLifecycleOwner) { buscador ->
            aplicarFiltrosYBusqueda(buscador)
        }
    }

    private fun configurarBuscador() {
        svRutas.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true;
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.buscar(newText)
                return true
            }
        })
    }

    private fun aplicarFiltrosYBusqueda(newText: String?) {
        val sharedPreferences = requireContext().getSharedPreferences("principal", Context.MODE_PRIVATE)

        // Obtener filtros
        val minDistancia = sharedPreferences.getInt("minDistancia", 0).toDouble()
        val maxDistancia = sharedPreferences.getInt("maxDistancia", 200).takeIf { it < 200 }?.toDouble() ?: Double.MAX_VALUE

        val dificultades = mutableListOf<String>().apply {
            if (sharedPreferences.getBoolean("facil", false)) add("Fácil")
            if (sharedPreferences.getBoolean("moderado", false)) add("Moderada")
            if (sharedPreferences.getBoolean("dificil", false)) add("Difícil")
            if (isEmpty()) addAll(listOf("Fácil", "Moderada", "Difícil"))
        }

        val tiposRecorrido = mutableListOf<String>().apply {
            if (sharedPreferences.getBoolean("pie", false)) add("Senderismo")
            if (sharedPreferences.getBoolean("bicicleta", false)) add("Bicicleta")
            if (sharedPreferences.getBoolean("coche", false)) add("Coche")
            if (isEmpty()) addAll(listOf("Senderismo", "Bicicleta", "Coche"))
        }

        viewModel.aplicarFiltrosYBusqueda(
            minDistancia,
            maxDistancia,
            dificultades,
            tiposRecorrido,
            newText
        )
    }

    private fun actualizarRecyclerView(rutas: List<Ruta>) {
        if (rutas.isEmpty()){
            tvNoHayRutas.visibility = View.VISIBLE
            tvNoHayRutas.text = "No hay rutas"
            recyclerView.visibility = View.GONE
        } else{
            tvNoHayRutas.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            rutasAdapter.actualizarRutas(rutas)
        }
    }

    companion object {
        fun newInstance(): RutasFragment = RutasFragment()
    }

    private fun procesarResultado(resultado: ActivityResult) {
        if (resultado.resultCode == RESULT_OK){
            aplicarFiltrosYBusqueda(svRutas.query.toString())
        } else{
            aplicarFiltrosYBusqueda(svRutas.query.toString())
        }
    }

    private fun inicializar() {
        recyclerView = requireView().findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        btFiltros = requireView().findViewById(R.id.btFiltros)
        tvNoHayRutas = requireView().findViewById(R.id.tvNoHayRutas)
        svRutas = requireView().findViewById(R.id.svRutas)
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                resultado -> procesarResultado(resultado)
        }
        btFiltros.setOnClickListener{
            val intent = Intent(requireContext(), FiltrosActivity::class.java)
            intent.putExtra("ventana", "principal")
            launcher.launch(intent)
        }
        recyclerView.adapter = rutasAdapter

        lifecycleScope.launch(Dispatchers.IO) {
            val listaRutas = dbRutas!!.rutaDao.getAll()
            if (listaRutas.isEmpty()) {
                val json = requireContext().assets.open("rutas.json").bufferedReader().use { it.readText() }
                viewModel.cargarRutasIniciales(json)
            } else if (rutasAdapter.itemCount == 0) {
                viewModel.cargarRutasIniciales("")
            }
        }

        val currentUser = SessionManager.currentUser
        if (currentUser != null){
            viewModel.cargarRutasFavoritas()
        }
        else{
            viewModel.limpiarFavoritos()
        }

    }

    private fun onClickedRuta(ruta: Ruta?) {
        if (ruta != null) {

            viewLifecycleOwner.lifecycleScope.launch {
                val tramosArray = dbRutas!!.tramoDao.getTramoById(ruta.id)
                val destino = RutasFragmentDirections.actionNavigationRutasToRutasDetalle(
                    ruta,
                    tramosArray.toTypedArray(),
                    viewModel.esFavorita(ruta)
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


