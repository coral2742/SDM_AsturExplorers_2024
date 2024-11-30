package sdm.com.asturexplorers

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.library.BuildConfig


/**
 * A simple [Fragment] subclass.
 * Use the [RutasCercanasFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RutasCercanasFragment : Fragment() {
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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



        // Configurar cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Verificar permisos y obtener ubicación
        obtenerUbicacionActual()


        /*
        // Ubicación actual
        val startPoint = GeoPoint(43.3548, -5.8512)

        // Centro en la ubicación indicada
        mapView.controller.setCenter(startPoint)

        // Añadir un marcador
        val marker = Marker(mapView)
        marker.position = startPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Escuela de Ingeniería Informática"
        mapView.overlays.add(marker)

         */
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

    private fun obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicitar permisos
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1000
            )
            return
        }

        // Obtener ubicación actual
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                //Log.d("RutasCercanas", "Latitud: ${location.latitude}, Longitud: ${location.longitude}")
                actualizarMapa(location)
            } else {
                Toast.makeText(requireContext(), "No se pudo obtener la ubicación.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun actualizarMapa(location: Location) {
        val currentLocation = GeoPoint(location.latitude, location.longitude)

        // Centrar mapa en la ubicación actual
        mapView.controller.setZoom(17.0)
        mapView.controller.setCenter(currentLocation)

        // Marcador en ubicación actual
        val marker = Marker(mapView)
        marker.position = currentLocation
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Ubicación Actual"



        mapView.overlays.add(marker)
    }
}