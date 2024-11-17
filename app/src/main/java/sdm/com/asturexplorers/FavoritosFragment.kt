package sdm.com.asturexplorers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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


/**
 * A simple [Fragment] subclass.
 * Use the [FavoritosFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavoritosFragment : Fragment() {
    private lateinit var tvWelcomeMessage: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favoritos, container, false)

        tvWelcomeMessage = view.findViewById(R.id.textWelcomeMessage)

        setupUI()

        return view
    }

    companion object {
        fun newInstance(): FavoritosFragment = FavoritosFragment()
    }


    private fun setupUI() {
        val currentUser = SessionManager.currentUser

        if (currentUser != null) {
            // Mostrar mensaje de bienvenida personalizado
            tvWelcomeMessage.text = "¡Bienvenido, ${currentUser.displayName}!"

            // Aquí deberías cargar las rutas favoritas del usuario
            //cargarRutasFavoritas(currentUser.uid)
        } else {
            // Si no hay usuario autenticado, muestra un mensaje genérico
            tvWelcomeMessage.text = "Inicia sesión para ver tus rutas favoritas."
            // También podrías redirigir al usuario a un fragmento de inicio de sesión si lo prefieres
        }
    }


}