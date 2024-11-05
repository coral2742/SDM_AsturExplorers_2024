package sdm.com.asturexplorers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button


/**
 * A simple [Fragment] subclass.
 * Use the [RutasCercanasFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RutasCercanasFragment : Fragment() {



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
        return inflater.inflate(R.layout.fragment_rutas_cercanas, container, false)
    }
}