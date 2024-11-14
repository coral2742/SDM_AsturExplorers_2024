package sdm.com.asturexplorers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RutasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var rutas: MutableList<Ruta>
    private lateinit var adapter: RutasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        val view = inflater.inflate(R.layout.fragment_rutas, container, false)
        inicializar(view)

        return view
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    companion object {
        fun newInstance(): RutasFragment = RutasFragment()
    }



    private fun inicializar(view: View) {
        recyclerView = view.findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Lista de rutas
        rutas = MutableList(30) { i ->
            Ruta(
                id = i,
                nombre = "Ruta del Cares",
                distancia = 21.8,
                dificultad = "Media",
                tipoRecorrido = "A pie",
                descripcion = "Ruta muy mítica",
                imagenUrl = "https://www.nachocuesta.com/sh/content/img/gal/2082/ruta-del-cares-vii_201905071522485cd1a2c85711b.sized.jpg"
            )
        }

        adapter = RutasAdapter(rutas)
        recyclerView.adapter = adapter
    }

}
