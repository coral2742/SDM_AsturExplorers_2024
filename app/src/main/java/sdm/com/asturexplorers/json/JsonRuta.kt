package sdm.com.asturexplorers.json

data class JsonRuta(
    val articles: Articles
)

data class Articles(
    val article: List<Article>
)

data class Article(
    val id: Codigo,
    val nombre: Nombre,
    val tramos: Tramos,
    val contacto: Contacto,
    val tipoRuta: TipoRuta,
    val visualizador: Visualizador
)

data class Codigo(
    val content: String
)

data class Nombre(
    val content: String
)

data class Tramos(
    val descripcionTramo: DescripcionTramo
)

data class DescripcionTramo(
    val value: String
)

data class Contacto(
    val distancia: Distancia,
    val dificultad: Dificultad,
    val tipoDeRecorrido: TipoDeRecorrido
)

data class Distancia(
    val content: String
)

data class Dificultad(
    val content: Int
)

data class TipoDeRecorrido(
    val content: String
)

data class TipoRuta(
    val content: String
)

data class Visualizador(
    val slide: Slide
)

data class Slide(
    val value: String
)

