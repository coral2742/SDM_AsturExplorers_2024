package sdm.com.asturexplorers

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sdm.com.asturexplorers.db.Ruta
import sdm.com.asturexplorers.db.RutasDatabase
import sdm.com.asturexplorers.json.ImageDetails
import sdm.com.asturexplorers.json.JsonRuta

class RutasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var rutas: MutableList<Ruta>
    private lateinit var adapter: RutasAdapter
    private var dbRutas : RutasDatabase? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        val view = inflater.inflate(R.layout.fragment_rutas, container, false)
        inicializar(view)

        return view
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbRutas = RutasDatabase.getDB(requireContext())
    }

    companion object {
        fun newInstance(): RutasFragment = RutasFragment()
    }



    private fun inicializar(view: View) {
        recyclerView = view.findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

        lifecycleScope.launch(Dispatchers.IO) {
            // Obtener las rutas de la base de datos
            var rutas = dbRutas!!.rutaDao.getAll()

            if (rutas.isEmpty()) {
                val json = requireContext().assets.open("rutas.json").bufferedReader().use { it.readText() }

                val rutasDesdeJson = parseJsonToRutas(json) // Parsear el JSON
                dbRutas?.rutaDao?.insertAll(rutasDesdeJson) // Insertar las rutas en la base de datos
                rutas = rutasDesdeJson // Asignar las rutas desde el JSON a la variable rutas
                Log.d("RutasFragment", "Datos cargados desde JSON.")
            } else {
                Log.d("RutasFragment", "Datos obtenidos de la base de datos.")
            }

            // Actualizar el RecyclerView en el hilo principal
            withContext(Dispatchers.Main) {
                adapter = RutasAdapter(rutas) // Crear el adaptador con las rutas
                recyclerView.adapter = adapter // Asignar el adaptador al RecyclerView
            }
        }

        adapter = RutasAdapter(rutas)
        recyclerView.adapter = adapter
    }

    private fun parseJsonToRutas(json: String): List<Ruta> {
        val gson = Gson()
        val jsonRuta = gson.fromJson(json, JsonRuta::class.java)

        return jsonRuta.articles.article.map {
            val imageJson = gson.fromJson(it.visualizador.slide.value, ImageDetails::class.java)
            Ruta(
                id = it.id.content,
                nombre = it.nombre.content,
                distancia = it.contacto.distancia.content.replace(",", ".").toDouble(),
                dificultad = when (it.contacto.dificultad.content) {
                    1 -> "Fácil"
                    2 -> "Moderada"
                    3 -> "Difícil"
                    else -> "Desconocida"
                },
                tipoRecorrido = it.contacto.tipoDeRecorrido.content,
                descripcion = it.tramos.descripcionTramo.value,
                imagenUrl = "https://www.turismoasturias.es/"
            )
        }
    }

}
