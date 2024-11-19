package sdm.com.asturexplorers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import sdm.com.asturexplorers.db.Ruta
import sdm.com.asturexplorers.db.Tramo

/**
 * A simple [Fragment] subclass.
 * Use the [RutasDetalle.newInstance] factory method to
 * create an instance of this fragment.
 */
class RutasDetalle : Fragment() {
    private lateinit var imagen: ImageView
    private lateinit var tvNombreRuta: TextView
    private lateinit var tvDistanciaRuta: TextView
    private lateinit var tvDistanciaInfoData: TextView
    private lateinit var tvDificultadInfoData: TextView
    private lateinit var tvTipoRecorridonfoData: TextView
    private lateinit var tvDescipcionInfoData: TextView

    private lateinit var ruta : Ruta
    private lateinit var tramos : Array<Tramo>

    private val args : RutasDetalleArgs by this.navArgs()

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rutas_detalle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imagen = view.findViewById(R.id.imageRuta)
        tvNombreRuta = view.findViewById(R.id.tvNombreRuta)
        tvDistanciaRuta = view.findViewById(R.id.tvDistanciaRuta)
        tvDistanciaInfoData = view.findViewById(R.id.tvDistanciaInfoData)
        tvDificultadInfoData = view.findViewById(R.id.tvDificultadInfoData)
        tvTipoRecorridonfoData = view.findViewById(R.id.tvTipoRecorridoInfoData)
        tvDescipcionInfoData = view.findViewById(R.id.tvDescripcionInfoData)

        ruta = args.ruta
        tramos = args.tramos
        imagen.load(ruta.imagenUrl)
        tvNombreRuta.text = ruta.nombre
        tvDistanciaRuta.text = ruta.distancia.toString()
        tvDistanciaInfoData.text = ruta.distancia.toString()
        tvDificultadInfoData.text = ruta.dificultad
        tvTipoRecorridonfoData.text = ruta.tipoRecorrido
        var cadena = ""
        for (tramo in tramos){
            cadena += tramo.descripcion + "\n"
        }
        tvDescipcionInfoData.text = cadena

        val atras = view.findViewById<Button>(R.id.favoriteImageAtras)
        atras.setOnClickListener { findNavController().popBackStack() }

        val favoritos = view.findViewById<Button>(R.id.favoriteImageButtonDetalles)
        favoritos.setOnClickListener {


        }

    }

}