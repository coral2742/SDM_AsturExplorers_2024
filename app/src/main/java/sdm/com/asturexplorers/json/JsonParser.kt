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
            val ruta = parseRuta(article, i)

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

    private fun parseRuta(article: JsonObject, index: Int): Ruta {
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
        val desnivel = article.getAsJsonObject("Contacto").getAsJsonObject("Desnivel")
        var desnivelContent = 0

        if (desnivel.has("content")) {
            val content = desnivel.get("content")

            desnivelContent = if (content.isJsonPrimitive) {
                val primitive = content.asJsonPrimitive
                when {
                    primitive.isNumber -> primitive.asInt // Es un número directamente
                    primitive.isString -> {
                        val regex = Regex("\\d+")
                        regex.find(primitive.asString)?.value?.toIntOrNull() ?: 0
                    }
                    else -> 0
                }
            } else {
                0 // No es un JsonPrimitive
            }
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



        val trazadaJson = article.getAsJsonObject("TrazadoRuta").get("content")

        var trazadaUrl = ""

        if (trazadaJson != null && trazadaJson.isJsonNull && trazadaJson.isJsonPrimitive) {
            // Si el contenido no es nulo y es un string válido
            val trazadaJsonString = trazadaJson.asString

            if (trazadaJsonString.isNotEmpty()) {
                // Si el contenido no está vacío, procesamos el JSON
                val trazadaJsonObject = gson.fromJson(trazadaJsonString, JsonObject::class.java)

                // Obtener los valores necesarios para construir la URL de la imagen
                val classPK = trazadaJsonObject.get("classPK").asInt
                val groupId = trazadaJsonObject.get("groupId").asInt
                val title = trazadaJsonObject.get("title").asString
                val uuid = trazadaJsonObject.get("uuid").asString

                // Construir la URL de la imagen
                trazadaUrl = "https://www.turismoasturias.es/documents/$groupId/$classPK/$title/$uuid"
            }
        }
        return Ruta(
            index,
            nombre,
            distancia,
            dificultad = when (dificultad) {
                1 -> "Fácil"
                2 -> "Moderada"
                3 -> "Difícil"
                else -> "Desconocida"
            },
            tipoRecorrido,
            desnivelContent,
            imagenUrl,
            trazadaUrl
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
        val origenDestino = tramoObject.getAsJsonObject("OrigenDestino")
        var origrenDestinoContent = ""
        if (origenDestino.has("content")){
            origrenDestinoContent = origenDestino.get("content").asString
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
            origenDestino = origrenDestinoContent,
            descripcion = tramoDescripcion
        )
    }
}
