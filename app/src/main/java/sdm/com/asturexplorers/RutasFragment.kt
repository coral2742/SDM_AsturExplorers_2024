package sdm.com.asturexplorers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

import sdm.com.asturexplorers.db.Ruta
import sdm.com.asturexplorers.db.RutasDatabase
import sdm.com.asturexplorers.db.Tramo
import sdm.com.asturexplorers.json.JsonParser


class RutasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RutasAdapter
    private var dbRutas: RutasDatabase? = null
    private val rutas = mutableListOf<Ruta>()
    private val tramos = mutableListOf<Tramo>()
    private lateinit var jsonParser: JsonParser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbRutas = RutasDatabase.getDB(requireContext())
        val gson = Gson()
        jsonParser = JsonParser(gson)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rutas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inicializar()
    }

    companion object {
        fun newInstance(): RutasFragment = RutasFragment()
    }

    private fun inicializar() {
        recyclerView = requireView().findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val json = requireContext().assets.open("rutas.json").bufferedReader().use { it.readText() }

        val (rutas, tramos) = jsonParser.parseRutas(json)

        this.rutas.addAll(rutas)
        this.tramos.addAll(tramos)

        recyclerView.adapter = RutasAdapter(rutas)
    }
}


