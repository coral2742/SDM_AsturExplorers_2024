package sdm.com.asturexplorers

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.config.Configuration
import android.content.Context
import android.location.LocationManager
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


/**
 * A simple [Fragment] subclass.
 * Use the [RutasCercanasFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RutasCercanasFragment : Fragment() {
    private lateinit var mapView: MapView

    override fun onStart() {
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_rutas_cercanas, container, false)
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        mapView = view.findViewById(R.id.map)

        // Estilo del mapa
        mapView.setTileSource(TileSourceFactory.MAPNIK)

        // Configurar zoom
        mapView.controller.setZoom(17)
        mapView.setMultiTouchControls(true) // Permite zoom con los dedos

        //
        val startPoint = GeoPoint(43.3548, -5.8512)

        //
        val contextGPS = GpsMyLocationProvider(context?.applicationContext)
        val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context),mapView)
        mapView.overlays.add(myLocationOverlay)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()

        // Centro en la ubicación indicada
        mapView.controller.setCenter(myLocationOverlay.myLocation)

        // Añadir un marcador
        val marker = Marker(mapView)
        marker.position = myLocationOverlay.myLocation
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Estas aquí"
        mapView.overlays.add(marker)
        return view
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume() // Llamado para reiniciar el mapa
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause() // Llamado para pausar el mapa
    }

    companion object {
        fun newInstance(): RutasCercanasFragment = RutasCercanasFragment()
    }
}