package sdm.com.asturexplorers

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var rutas: MutableList<Ruta>
    private lateinit var adapter: RutasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        inicializar()
        rutas = MutableList(30) { i -> Ruta (
            id = i,
            nombre = "Ruta del Cares",
            distancia = 21.8,
            dificultad = "Media",
            tipoRecorrido = "A pie",
            descripcion = "Ruta muy mítica",
            "https://www.nachocuesta.com/sh/content/img/gal/2082/ruta-del-cares-vii_201905071522485cd1a2c85711b.sized.jpg")}
        adapter = RutasAdapter(rutas)
        recyclerView.adapter = adapter

    }

    private fun inicializar() {
        recyclerView = findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
}