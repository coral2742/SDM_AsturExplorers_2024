package sdm.com.asturexplorers

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.slider.RangeSlider

class FiltrosActivity : AppCompatActivity() {

    private lateinit var tvMinDistancia: TextView
    private lateinit var tvMaxDistancia: TextView
    private lateinit var rango: RangeSlider
    private lateinit var chBoxFacil: CheckBox
    private lateinit var chBoxModerado: CheckBox
    private lateinit var chBoxDificil: CheckBox
    private lateinit var chBoxAPie: CheckBox
    private lateinit var chBoxBicicleta: CheckBox
    private lateinit var chBoxCoche: CheckBox
    private lateinit var filtro: Button
    private lateinit var cancelar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_filtros)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        inicializarParams()
        cargarFiltrosGuardados()

        rango.addOnChangeListener { _, _, _ ->
            val values = rango.values
            val minDistancia = values[0].toInt()
            val maxDistancia = values[1].toInt()

            tvMinDistancia.text = "$minDistancia Km"
            tvMaxDistancia.text = if (maxDistancia == 200) {
                "+$maxDistancia Km"
            } else {
                "$maxDistancia Km"
            }
        }

        filtro.setOnClickListener {
            guardarFiltrosEnSharedPreferences()
            setResult(RESULT_OK)
            finish()
        }

        cancelar.setOnClickListener {
            val sharedPreferences = getSharedPreferences(intent.getStringExtra("ventana"), Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun guardarFiltrosEnSharedPreferences() {
        val sharedPreferences = getSharedPreferences(intent.getStringExtra("ventana"), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val rangoValues = rango.values
        editor.putInt("minDistancia", rangoValues[0].toInt())
        editor.putInt("maxDistancia", rangoValues[1].toInt())
        editor.putBoolean("pie", chBoxAPie.isChecked)
        editor.putBoolean("bicicleta", chBoxBicicleta.isChecked)
        editor.putBoolean("coche", chBoxCoche.isChecked)
        editor.putBoolean("facil", chBoxFacil.isChecked)
        editor.putBoolean("moderado", chBoxModerado.isChecked)
        editor.putBoolean("dificil", chBoxDificil.isChecked)
        editor.apply()
    }

    private fun cargarFiltrosGuardados() {
        val seccion = intent.getStringExtra("ventana")
        val sharedPreferences = getSharedPreferences(seccion, Context.MODE_PRIVATE)

        val minDistancia = sharedPreferences.getInt("minDistancia", 0)
        val maxDistancia = sharedPreferences.getInt("maxDistancia", 200)

        rango.setValues(minDistancia.toFloat(), maxDistancia.toFloat())
        tvMinDistancia.text = "$minDistancia Km"
        tvMaxDistancia.text = if (maxDistancia == 200) {
            "+$maxDistancia Km"
        } else {
            "$maxDistancia Km"
        }

        chBoxFacil.isChecked = sharedPreferences.getBoolean("facil", false)
        chBoxModerado.isChecked = sharedPreferences.getBoolean("moderado", false)
        chBoxDificil.isChecked = sharedPreferences.getBoolean("dificil", false)
        chBoxAPie.isChecked = sharedPreferences.getBoolean("pie", false)
        chBoxBicicleta.isChecked = sharedPreferences.getBoolean("bicicleta", false)
        chBoxCoche.isChecked = sharedPreferences.getBoolean("coche", false)
    }

    private fun inicializarParams() {
        tvMinDistancia = findViewById(R.id.tvMinDistancia)
        tvMaxDistancia = findViewById(R.id.tvMaxDistancia)
        rango = findViewById(R.id.rango)
        chBoxFacil = findViewById(R.id.chBoxFacil)
        chBoxModerado = findViewById(R.id.chBoxModerado)
        chBoxDificil = findViewById(R.id.chBoxDificil)
        chBoxAPie = findViewById(R.id.chBoxAPie)
        chBoxBicicleta = findViewById(R.id.chBoxBicicleta)
        chBoxCoche = findViewById(R.id.chBoxCoche)
        filtro = findViewById(R.id.aplicarFiltro)
        cancelar = findViewById(R.id.cancelarFiltro)
    }
}
