package sdm.com.asturexplorers

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.colormoon.readmoretextview.ReadMoreTextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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
import kotlin.properties.Delegates


class RutasDetalle : Fragment() {
    private lateinit var imagen: ImageView
    private lateinit var tvNombreRuta: TextView
    private lateinit var tvDistanciaRuta: TextView
    private lateinit var tvDistanciaInfoData: TextView
    private lateinit var tvDificultadInfoData: TextView
    private lateinit var tvTipoRecorridonfoData: TextView
    private lateinit var tvDescipcionInfoData: ReadMoreTextView
    private lateinit var tvDesnivelData: TextView
    private lateinit var favoriteImageButton : ImageButton

    private lateinit var ruta : Ruta
    private lateinit var tramos : Array<Tramo>

    private val args : RutasDetalleArgs by this.navArgs()
    private lateinit var mapView: MapView
    private val db = FirebaseFirestore.getInstance()


    private lateinit var scrollView: NestedScrollView
    private lateinit var bottomNavView: BottomNavigationView


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

    override fun onDestroyView() {
        super.onDestroyView()
        bottomNavView.visibility = View.VISIBLE
    }


    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imagen = view.findViewById(R.id.imageRuta)
        tvNombreRuta = view.findViewById(R.id.tvNombreRuta)
        tvDistanciaRuta = view.findViewById(R.id.tvDistanciaRuta)
        tvDistanciaInfoData = view.findViewById(R.id.tvDistanciaInfoData)
        tvDificultadInfoData = view.findViewById(R.id.tvDificultadInfoData)
        tvTipoRecorridonfoData = view.findViewById(R.id.tvTipoRecorridoInfoData)
        tvDescipcionInfoData = view.findViewById(R.id.tvDescripcionInfoData)
        tvDesnivelData = view.findViewById(R.id.tvDesnivelData)
        favoriteImageButton = view.findViewById(R.id.favoriteImageButtonDetalles)

        bottomNavView = requireActivity().findViewById<BottomNavigationView>(R.id.navigationbtn)
        bottomNavView.visibility = View.GONE


        ruta = args.ruta
        tramos = args.tramos
        var esFavorita = args.esFavorita

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

        tvDescipcionInfoData.setCollapsedText("Ver más")
        tvDescipcionInfoData.setExpandedText("Ver menos")

        tvDescipcionInfoData.setTrimLines(5)

        val atras = view.findViewById<ImageButton>(R.id.favoriteImageAtras)
        atras.setOnClickListener { findNavController().popBackStack() }

        favoriteImageButton.setImageResource(
            if (esFavorita) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )


        favoriteImageButton.setOnClickListener {
            if (esFavorita){
                esFavorita = false
                favoriteImageButton.setImageResource(android.R.drawable.btn_star_big_off)
                val user = SessionManager.currentUser
                if (user != null){
                    eliminarFavortios(user)
                }

            } else{
                esFavorita = true
                favoriteImageButton.setImageResource(android.R.drawable.btn_star_big_on)
                val user = SessionManager.currentUser
                if (user != null){
                    agregarAFavoritos(user)
                }
            }
        }

        Configuration.getInstance().userAgentValue = BuildConfig.LIBRARY_PACKAGE_NAME
        mapView = view.findViewById(R.id.mapKml)
        scrollView = view.findViewById(R.id.scrollView)

        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapView.controller.setZoom(15.0)
        mapView.setTilesScaledToDpi(true); // Tamaño de letra
        mapView.setMultiTouchControls(true)

        //variable para comprobar si el mapa está siendo tocado
        var isMapViewTouched = false

        //Que hara si tocamos o no el mapa
        mapView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isMapViewTouched = true
                    scrollView.requestDisallowInterceptTouchEvent(true) //Desactivar scroll
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isMapViewTouched = false
                    scrollView.requestDisallowInterceptTouchEvent(false) //Activar scroll
                }
            }
            //El false permite que el zoom y scroll del mapa siga funcionando
            false
        }

        //Detecta si el scroll esta siendo tocado
        scrollView.setOnTouchListener { v, event ->
            if (!isMapViewTouched) {
                //Si no estamos tocando el mapa, se vuelve a activar el scroll
                false
            } else {
                //Si tocamos el mapa se detactiva el scroll
                true
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            mapaKML()
        }
    }

    private fun agregarAFavoritos(user: FirebaseUser) {
        // Si el usuario está autenticado
        Log.d("RutasViewModel", "AÑADIENDO A FAV: Usuario autenticado: ${user.uid}")
        db.collection("rutas_favs")
            .document(user.uid)
            .update("favoritas", FieldValue.arrayUnion(ruta.id))
            .addOnSuccessListener {
                Snackbar.make(requireView(), "Ruta añadida a favoritos", Snackbar.LENGTH_SHORT).show()
                Log.w("RutasViewModel", "Ruta añadida a favoritos ${ruta.nombre}")
            }
            .addOnFailureListener { e ->
                Log.w("RutasViewModel", "Error al añadir ruta a favoritos", e)
            }
    }

    private fun eliminarFavortios(user: FirebaseUser) {
        db.collection("rutas_favs")
            .document(user.uid)
            .update("favoritas", FieldValue.arrayRemove(ruta.id))
            .addOnSuccessListener {
                Snackbar.make(requireView(), "Ruta eliminada de favoritos", Snackbar.LENGTH_SHORT).show()
                Log.w("FavoritosFragment", "Ruta eliminada de favoritos ${ruta.nombre}")
            }
            .addOnFailureListener { e ->
                Log.w("FavoritosFragment", "Error al eliminar ruta de favoritos", e)
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
                    val boundingBox = kmlOverlay.bounds
                    mapView.zoomToBoundingBox(boundingBox, true)
                }


            } else {
                Log.e("KML Error", "No se pudo procesar el archivo KML.")
            }
        } catch (e: Exception) {
            Log.e("KML Error", "Error al cargar el archivo KML: ", e)
        }
    }

}