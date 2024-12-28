package sdm.com.asturexplorers

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import sdm.com.asturexplorers.db.Ruta


class RutasAdapter(
    private val listaRutas: List<Ruta>,
    private val onFavoriteClick: (Ruta) -> Unit,
    private val onClickListener: (Ruta?) -> Unit
) : RecyclerView.Adapter<RutasAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RutasAdapter.ViewHolder {
        val layoutElement = R.layout.card_ruta
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
        private val favoriteImageButton: ImageButton = view.findViewById(R.id.favoriteImageButton)

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
            tvDistancia.text = ruta.distancia.toString() + " km"
            imageView.load(ruta.imagenUrl)

            // acción de clic sobre estrella de favoritos
            favoriteImageButton.setOnClickListener {
                rutaActual?.let { ruta ->
                    val isSelected = favoriteImageButton.isSelected
                    favoriteImageButton.isSelected = !isSelected
                    onFavoriteClick(ruta)
                }
            }

        }
    }

}