package sdm.com.asturexplorers.json

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import sdm.com.asturexplorers.db.Ruta
import sdm.com.asturexplorers.db.Tramo

class JsonParser(private val gson: Gson) {


    fun parseRutas(json: String): Pair<List<Ruta>, List<Tramo>> {
        val rutas = mutableListOf<Ruta>()
        val tramos = mutableListOf<Tramo>()

        val jsonObject = gson.fromJson(json, JsonObject::class.java)
        val articles = jsonObject
            .getAsJsonObject("articles")
            .getAsJsonArray("article")

        for (i in 0 until articles.size()) {
            val article = articles.get(i).asJsonObject

            // Parse ruta
            val ruta = parseRuta(article)

            // Parse tramos
            val tramosElement = article.get("Tramos")
            if (tramosElement != null) {
                if (tramosElement.isJsonArray) {
                    val tramosArray = tramosElement.asJsonArray
                    for (j in 0 until tramosArray.size()) {
                        val tramoObject = tramosArray[j].asJsonObject
                        tramos.add(parseTramo(tramoObject, i))
                    }
                } else if (tramosElement.isJsonObject) {
                    val tramoObject = tramosElement.asJsonObject
                    tramos.add(parseTramo(tramoObject, i))
                }
            }

            rutas.add(ruta)
        }

        return Pair(rutas, tramos)
    }

    private fun parseRuta(article: JsonObject): Ruta {
        val nombre = article.getAsJsonObject("Nombre").get("content").asString
        val distanciaContenido = article.getAsJsonObject("Contacto").getAsJsonObject("Distancia")
        val distancia = if (distanciaContenido.has("content")) {
            distanciaContenido.get("content").asString.replace(",", ".").toDouble()
        } else {
            0.0
        }
        val dificultadContenido = article.getAsJsonObject("Contacto").getAsJsonObject("Dificultad")
        val dificultad = if (dificultadContenido.has("content")) {
            dificultadContenido.get("content").asInt
        } else {
            1
        }
        val tipoRecorrido = article.getAsJsonObject("TipoRuta").get("content").asString
        val slideElement = article.getAsJsonObject("Visualizador").takeIf { it.has("Slide") }?.get("Slide")
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
        return Ruta(
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
    }

    private fun procesarImagen(slideElement: JsonElement): String {
        return if (slideElement.isJsonArray) {
            slideElement.asJsonArray.first().asJsonObject.get("value").asString
        } else {
            slideElement.asJsonObject.get("value").asString
        }
    }

    private fun parseTramo(tramoObject: JsonObject, rutaId: Int): Tramo {
        val tieneContent = tramoObject.getAsJsonObject("DistanciaTramo")
        var tramoDistancia = 0.0
        if (tieneContent.has("content")) {
            val tipoTramo = tieneContent.get("content").asJsonPrimitive
            if (tipoTramo.isString) {
                tramoDistancia = tipoTramo.asString.split(" ")[0].replace(",", ".").toDouble()
            } else {
                tramoDistancia = tipoTramo.asDouble
            }
        }

        val tramoDescripcionTipo = tramoObject.get("DescripcionTramo")
        var tramoDescripcion = ""
        if (tramoDescripcionTipo.isJsonArray) {
            val tramosArray = tramoDescripcionTipo.asJsonArray
            for (j in 0 until tramosArray.size()) {
                tramoDescripcion += tramosArray[j].asJsonObject.get("value").asString + "\n"
            }
        } else {
            tramoDescripcion = tramoDescripcionTipo.asJsonObject.get("value").asString
        }

        return Tramo(
            rutaId = rutaId,
            distancia = tramoDistancia,
            origenDestino = "",
            descripcion = tramoDescripcion
        )
    }
}
