package sdm.com.asturexplorers

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Oculta la barra de estado
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val bottomNavView = findViewById<BottomNavigationView>(R.id.navigationbtn)
        val navHostFragment = findNavController(R.id.fragmentContainerView)
        bottomNavView.setupWithNavController(navHostFragment)


        setupWithNavController(bottomNavView, navHostFragment)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        SessionManager.currentUser = user

        mostrarConsejos();


    }

    private fun mostrarConsejos(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Consejo")
        builder.setMessage("Comprueba el clima, planifica tu ruta y lleva suficiente agua.")
        builder.setIcon(R.drawable.logo)
        builder.setPositiveButton("Más info") { _, _ ->
            val pdfUrl = "https://15f8034cdff6595cbfa1-1dd67c28d3aade9d3442ee99310d18bd.ssl.cf3.rackcdn.com/7202a309d373e716ec4e3d3dc959cbe4/Practica_montanismo_sin_miedo_pero_con_seguridad.pdf"

            // Abrir la guía PDF en el navegador
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
            startActivity(intent)
        }
        builder.setNegativeButton("Cerrar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()


    }


}