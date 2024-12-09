package sdm.com.asturexplorers

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.kml.KmlDocument
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import sdm.com.asturexplorers.db.Ruta
import sdm.com.asturexplorers.db.RutasDatabase
import sdm.com.asturexplorers.json.JsonParser


class RutasCercanasFragment : Fragment() {
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var dbRutas: RutasDatabase? = null
    private lateinit var jsonParser: JsonParser
    private val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbRutas = RutasDatabase.getDB(requireContext())
        val gson = Gson()
        jsonParser = JsonParser(gson)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inicializar()
    }

    private fun inicializar() {
        lifecycleScope.launch(Dispatchers.IO) {
            var listaRutas = dbRutas!!.rutaDao.getAll()
            if (listaRutas.isEmpty()) {
                val json = requireContext().assets.open("rutas.json").bufferedReader()
                    .use { it.readText() }

                val (rutas, tramos) = jsonParser.parseRutas(json)
                dbRutas!!.rutaDao.insertAll(rutas)
                listaRutas = rutas
            }

            lifecycleScope.launch(Dispatchers.Main) {
                agregarRutasAlMapa(listaRutas)
            }
        }
    }

    private fun agregarRutasAlMapa(listaRutas: List<Ruta>) {
        for (ruta in listaRutas) {
            if (ruta.latitud != "" && ruta.longitud != "") {
                val latitud = ruta.latitud.toDouble()
                val longitud = ruta.longitud.toDouble()

                Log.d("RutasCercanas", "Latitud: $latitud, Longitud: $longitud")

                val marker = Marker(mapView)
                val currentLocation = GeoPoint(latitud, longitud)
                marker.position = currentLocation
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                marker.title = "Ruta: " + ruta.nombre

                marker.snippet = "Dificultad: " + ruta.dificultad + " (" + ruta.distancia.toString() + " km)"

                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.marker)
                val bitmap = (drawable as BitmapDrawable).bitmap
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 80, 90, false)
                marker.icon = BitmapDrawable(resources, scaledBitmap)

                mapView.overlays.add(marker)
            }
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rutas_cercanas, container, false)
        Configuration.getInstance().userAgentValue = BuildConfig.LIBRARY_PACKAGE_NAME
        mapView = view.findViewById(R.id.map)

        mapView.setTileSource(TileSourceFactory.MAPNIK)

        mapView.controller.setZoom(17)
        mapView.setMultiTouchControls(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Verificar permisos y obtener ubicación
        obtenerUbicacionActual()

        return view
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
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
                actualizarMapa(location)
            } else {
                Toast.makeText(requireContext(), "No se pudo obtener la ubicación.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarMapa(location: Location) {
        val currentLocation = GeoPoint(location.latitude, location.longitude)

        mapView.controller.setZoom(17.0)
        mapView.controller.setCenter(currentLocation)

        val marker = Marker(mapView)
        marker.position = currentLocation
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Ubicación Actual"
        mapView.overlays.add(marker)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) &&
                (grantResults.isNotEmpty() && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                obtenerUbicacionActual()
            } else {
                // Permisos denegados
                Toast.makeText(requireContext(), "Error: No se puede acceder a tu ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
