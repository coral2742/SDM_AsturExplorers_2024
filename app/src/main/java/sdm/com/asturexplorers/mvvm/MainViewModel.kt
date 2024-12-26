package sdm.com.asturexplorers.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainViewModel : ViewModel() {
    private val auth : FirebaseAuth = FirebaseAuth.getInstance();

    private val _currentUser = MutableLiveData<FirebaseUser?>();
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    init {
        _currentUser.value = auth.currentUser;
    }


    fun logout() {
        auth.signOut();
        _currentUser.value = null;

    }
}