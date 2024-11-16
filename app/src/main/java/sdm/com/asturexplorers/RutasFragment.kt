package sdm.com.asturexplorers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import sdm.com.asturexplorers.db.Ruta
import sdm.com.asturexplorers.db.RutasDatabase
import sdm.com.asturexplorers.db.Tramo


class RutasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RutasAdapter
    private var dbRutas : RutasDatabase? = null
    val rutas = mutableListOf<Ruta>()
    val tramos = mutableListOf<Tramo>()
    private val gson:Gson = Gson()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbRutas = RutasDatabase.getDB(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
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

        val jsonObject = gson.fromJson(json, JsonObject::class.java)

        val articles = jsonObject
            .getAsJsonObject("articles")
            .getAsJsonArray("article")
        procesarArticle(articles)
        recyclerView.adapter = RutasAdapter(rutas)
    }

    private fun procesarArticle(articles: JsonArray) {
        for (i in 0 until articles.size()) {
            val article = articles.get(i).asJsonObject

            val nombre = article.getAsJsonObject("Nombre")
                .get("content").asString

            val distanciaContenido = article.getAsJsonObject("Contacto")
                .getAsJsonObject("Distancia")

            var distancia = 0.0
            if (distanciaContenido.has("content")){
                distancia = distanciaContenido
                    .get("content")
                    .asString
                    .replace(",", ".")
                    .toDouble()
            }

            val dificultadContenido = article.getAsJsonObject("Contacto")
                .getAsJsonObject("Dificultad")
            var dificultad = 1
            if (dificultadContenido.has("content")){
                dificultad = dificultadContenido
                    .get("content")
                    .asInt
            }

            val tipoRecorrido = article.getAsJsonObject("TipoRuta")
                .get("content")
                .asString

            val slideElement = article.getAsJsonObject("Visualizador")
                .takeIf { it.has("Slide") }
                ?.get("Slide")

            val imageJson = slideElement?.let { procesarImagen(it) }

            var imagenUrl = ""
            if (imageJson != null && imageJson != "") {
                val imageJsonObject = gson.fromJson(imageJson, JsonObject::class.java)

                // Obtener los valores necesarios para construir la URL de la imagen
                val classPK = imageJsonObject.get("classPK").asInt
                val groupId = imageJsonObject.get("groupId").asInt
                val title = imageJsonObject.get("title").asString
                val uuid = imageJsonObject.get("uuid").asString

                // Construir la URL de la imagen
                imagenUrl =
                    "https://www.turismoasturias.es/documents/$groupId/$classPK/$title/$uuid"

            }
            val ruta = Ruta(
                nombre = nombre,
                distancia = distancia,
                dificultad = when (dificultad) {
                    1 -> "Fácil"
                    2 -> "Moderada"
                    3 -> "Difícil"
                    else -> "Desconocida"
                },
                tipoRecorrido = tipoRecorrido,
                imagenUrl = imagenUrl
            )
            rutas.add(ruta)

            val tramosElement = article.get("Tramos")
            if (tramosElement != null) {
                if (tramosElement.isJsonArray) {
                    val tramosArray = tramosElement.asJsonArray
                    for (j in 0 until tramosArray.size()) {
                        val tramoObject = tramosArray[j].asJsonObject
                        procesarTramo(tramoObject, i)
                    }
                } else if (tramosElement.isJsonObject) {
                    val tramoObject = tramosElement.asJsonObject
                    procesarTramo(tramoObject, i)
                }
            }

        }
    }

    private fun procesarImagen(slideElement: JsonElement): String {
        return if (slideElement.isJsonArray) {
            slideElement.asJsonArray
                .first().asJsonObject
                .get("value")
                .asString
        } else {
            slideElement.asJsonObject
                .get("value")
                .asString
        }
    }


    private fun procesarTramo(tramoObject: JsonObject, rutaId: Int) {
        val tieneContent = tramoObject.getAsJsonObject("DistanciaTramo")
        var tramoDistancia = 0.0
        if (tieneContent.has("content")){
            var tipoTramo = tieneContent.get("content").asJsonPrimitive
            if (tipoTramo.isString){
                tramoDistancia = tipoTramo.asString
                    .split(" ")[0]
                    .replace(",", ".")
                    .toDouble()
            } else {
                tramoDistancia = tipoTramo.asDouble
            }
        }

        val tramoDescripcionTipo = tramoObject.get("DescripcionTramo")
        var tramoDescripcion = ""
        if (tramoDescripcionTipo.isJsonArray){
            val tramosArray = tramoDescripcionTipo.asJsonArray
            for (j in 0 until tramosArray.size()) {
                tramoDescripcion += tramosArray[j].asJsonObject
                    .get("value")
                    .asString + "\n"
            }
        } else{
            tramoDescripcion = tramoDescripcionTipo.asJsonObject
                .get("value")
                .asString
        }

        // Crear el objeto Tramo
        val tramo = Tramo(
            rutaId = rutaId,
            distancia = tramoDistancia,
            origenDestino = "",
            descripcion = tramoDescripcion
        )
        tramos.add(tramo)
    }
}

