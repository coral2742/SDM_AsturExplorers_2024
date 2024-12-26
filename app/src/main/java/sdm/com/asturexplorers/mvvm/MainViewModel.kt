package sdm.com.asturexplorers.mvvm

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import sdm.com.asturexplorers.R

class MainViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _mensajeMostrado = MutableLiveData(false)
    val mensajeMostrado: LiveData<Boolean> get() = _mensajeMostrado

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    /**
     * Muestra un diálogo de consejos. Necesita el contexto de la actividad
     * para construir y mostrar el diálogo.
     */
    fun mostrarConsejos(context: Context) {
        if (_mensajeMostrado.value == true) {
            return
        }

        _mensajeMostrado.value = true

        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("Consejo")
        builder.setMessage("Comprueba el clima, planifica tu ruta y lleva suficiente agua.")
        builder.setIcon(R.drawable.logo)
        builder.setPositiveButton("Más info") { _, _ ->
            val pdfUrl = "https://15f8034cdff6595cbfa1-1dd67c28d3aade9d3442ee99310d18bd.ssl.cf3.rackcdn.com/7202a309d373e716ec4e3d3dc959cbe4/Practica_montanismo_sin_miedo_pero_con_seguridad.pdf"

            // Abrir la guía PDF en el navegador
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
            context.startActivity(intent)
        }
        builder.setNegativeButton("Cerrar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}
