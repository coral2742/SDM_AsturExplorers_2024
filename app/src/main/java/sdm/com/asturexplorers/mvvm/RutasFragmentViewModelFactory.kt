package sdm.com.asturexplorers.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import sdm.com.asturexplorers.db.RutasDatabase

class RutasFragmentViewModelFactory(
    private val dbRutas: RutasDatabase?,
    private val db : FirebaseFirestore
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RutasViewModel(dbRutas, db) as T
    }
}