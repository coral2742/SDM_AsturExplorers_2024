package sdm.com.asturexplorers

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import sdm.com.asturexplorers.db.Ruta


class RutasFavAdapter(
    private val listaRutas: List<Ruta>,
    private val onFavoriteClick: (Ruta) -> Unit,
    private val onClickListener: (Ruta?) -> Unit
) : RecyclerView.Adapter<RutasFavAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutElement = R.layout.card_ruta_favorita
        val view = LayoutInflater.from(parent.context).inflate(layoutElement, parent, false)
        return ViewHolder(view, onClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listaRutas[position], onFavoriteClick)
    }

    override fun getItemCount(): Int {
        return listaRutas.size
    }

    class ViewHolder(
        view: View,
        onClickListener: (Ruta?) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        private val tvTitulo: TextView = view.findViewById(R.id.tvTitulo)
        private val tvDificultad: TextView = view.findViewById(R.id.tvDificultadReal)
        private val tvDistancia: TextView = view.findViewById(R.id.tvDistanciaReal)
        private val tvRecorrido: TextView = view.findViewById(R.id.tvRecorridoReal)
        private val imageView: ImageView = view.findViewById((R.id.cardImage))
        private var rutaActual: Ruta? = null
        private val favoriteDeleteButton: Button = view.findViewById(R.id.deleteFavButton)

        init {
            view.setOnClickListener { it ->
                onClickListener(rutaActual)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(ruta: Ruta, onFavoriteClick: (Ruta) -> Unit) {
            rutaActual = ruta
            tvTitulo.text = ruta.nombre
            tvDificultad.text = ruta.dificultad
            tvRecorrido.text = ruta.tipoRecorrido
            tvDistancia.text = "${ruta.distancia} km"
            imageView.load(ruta.imagenUrl)

            Log.d("RutasFragment", "Ruta -> ${ruta} <-")

            // Acción de clic sobre el botón de eliminar de favoritos
            favoriteDeleteButton.setOnClickListener {
                // Si hay una ruta actual seleccionada
                rutaActual?.let { ruta ->

                    // Crear el AlertDialog de confirmación
                    val builder = AlertDialog.Builder(itemView.context) // Usamos el contexto de la vista
                    builder.setTitle("Confirmar eliminación")
                        .setMessage("¿Estás seguro de que deseas eliminar esta ruta de favoritos?")
                        .setPositiveButton("Sí") { dialog, id ->
                            // Si el usuario confirma, realiza la acción para eliminar de favoritos
                            onFavoriteClick(ruta) // Llamar a la función para eliminar la ruta
                        }
                        .setNegativeButton("No") { dialog, id ->
                            // Si el usuario cancela, se cierra el diálogo
                            dialog.dismiss()
                        }

                    // Mostrar el diálogo
                    builder.create().show()
                }
            }
        }
    }
}
