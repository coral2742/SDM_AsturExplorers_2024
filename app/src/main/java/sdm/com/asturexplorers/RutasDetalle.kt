package sdm.com.asturexplorers

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.kml.KmlDocument
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import sdm.com.asturexplorers.db.Ruta
import sdm.com.asturexplorers.db.Tramo


class RutasDetalle : Fragment() {
    private lateinit var imagen: ImageView
    private lateinit var tvNombreRuta: TextView
    private lateinit var tvDistanciaRuta: TextView
    private lateinit var tvDistanciaInfoData: TextView
    private lateinit var tvDificultadInfoData: TextView
    private lateinit var tvTipoRecorridonfoData: TextView
    private lateinit var tvDescipcionInfoData: TextView
    private lateinit var tvDesnivelData: TextView

    private lateinit var ruta : Ruta
    private lateinit var tramos : Array<Tramo>

    private val args : RutasDetalleArgs by this.navArgs()
    private lateinit var mapView: MapView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_rutas_detalle, container, false)
        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imagen = view.findViewById(R.id.imageRuta)
        tvNombreRuta = view.findViewById(R.id.tvNombreRuta)
        tvDistanciaRuta = view.findViewById(R.id.tvDistanciaRuta)
        tvDistanciaInfoData = view.findViewById(R.id.tvDistanciaInfoData)
        tvDificultadInfoData = view.findViewById(R.id.tvDificultadInfoData)
        tvTipoRecorridonfoData = view.findViewById(R.id.tvTipoRecorridoInfoData)
        tvDescipcionInfoData = view.findViewById(R.id.tvDescripcionInfoData)
        tvDesnivelData = view.findViewById(R.id.tvDesnivelData)

        ruta = args.ruta
        tramos = args.tramos
        imagen.load(ruta.imagenUrl)
        tvNombreRuta.text = ruta.nombre
        tvDistanciaRuta.text = ruta.distancia.toString() + " Km"
        tvDistanciaInfoData.text = ruta.distancia.toString() + " Km"
        tvDificultadInfoData.text = ruta.dificultad
        tvTipoRecorridonfoData.text = ruta.tipoRecorrido
        tvDesnivelData.text = ruta.desnivel.toString() + " m"
        var cadena = ""
        for (tramo in tramos){
            val descripcionHtml = tramo.descripcion
            val descripcionTextoPlano = HtmlCompat.fromHtml(descripcionHtml.toString(),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            ).toString()
            cadena += descripcionTextoPlano + "\n"
        }
        tvDescipcionInfoData.text = cadena

        val atras = view.findViewById<ImageButton>(R.id.favoriteImageAtras)
        atras.setOnClickListener { findNavController().popBackStack() }

        val favoritos = view.findViewById<ImageButton>(R.id.favoriteImageButtonDetalles)
        favoritos.setOnClickListener {


        }

        Configuration.getInstance().userAgentValue = BuildConfig.LIBRARY_PACKAGE_NAME
        mapView = view.findViewById(R.id.mapKml)

        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)

        //mapView.controller.setZoom(10.0)
        mapView.controller.setZoom(15.0)
        // Centrar el mapa en la Escuela de Ingeniería Informática
        //mapView.controller.setCenter(org.osmdroid.util.GeoPoint(43.354702, -5.851346))
        mapView.setMultiTouchControls(true)

        lifecycleScope.launch(Dispatchers.IO) {
            mapaKML()
        }
    }

    private fun mapaKML(){
        try {
            // Cargar KML de los datos de la ruta
            val document = KmlDocument()
            document.parseKMLUrl(ruta.trazada)

            // Verificar si el KML se ha cargado
            if (document.mKmlRoot != null) {
                val kmlOverlay = document.mKmlRoot.buildOverlay(mapView, null, null, document) as FolderOverlay
                // Agregar el overlay al mapa
                mapView.overlays.add(kmlOverlay)
                mapView.invalidate()

                GlobalScope.launch(Dispatchers.Main) {
                    val centerPoint = document.mKmlRoot.getBoundingBox().getCenter()
                    mapView.controller.setCenter(centerPoint)
                    //mapView.controller.setZoom(0)
                }


            } else {
                Log.e("KML Error", "No se pudo procesar el archivo KML.")
            }
        } catch (e: Exception) {
            Log.e("KML Error", "Error al cargar el archivo KML: ", e)
        }
    }

}