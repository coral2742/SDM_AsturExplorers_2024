package sdm.com.asturexplorers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.fragment.findNavController


/**
 * A simple [Fragment] subclass.
 * Use the [FavoritosFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavoritosFragment : Fragment() {
    private lateinit var tvWelcomeMessage: TextView
    private lateinit var btnIniciarSesion: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favoritos, container, false)

        tvWelcomeMessage = view.findViewById(R.id.textWelcomeMessage)
        btnIniciarSesion = view.findViewById(R.id.btnIniciarSesion)

        btnIniciarSesion.setOnClickListener {
            // redirigir al fragment de MiCuenta para iniciar sesión
            val navController = findNavController()
            navController.popBackStack(R.id.navigation_rutas, false)
            navController.navigate(R.id.navigation_mi_perfil);
        }

        setupUI()

        return view
    }

    companion object {
        fun newInstance(): FavoritosFragment = FavoritosFragment()
    }


    private fun setupUI() {
        val currentUser = SessionManager.currentUser

        if (currentUser != null) {
            tvWelcomeMessage.text = "¡Bienvenido, ${currentUser.displayName}!"
            btnIniciarSesion.visibility = View.GONE

            // Aquí deberías cargar las rutas favoritas del usuario
            //cargarRutasFavoritas(currentUser.uid)
        } else {
            tvWelcomeMessage.text = "Inicia sesión para ver tus rutas favoritas."
            btnIniciarSesion.visibility = View.VISIBLE
        }
    }


}