import android.content.Intent
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import sdm.com.asturexplorers.R


class MiPerfilViewModel : ViewModel() {
    private val _auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val RC_SIGN_IN = 9001
    


    private val _currentUser = MutableLiveData<FirebaseUser?>(null);
    val currentUser : MutableLiveData<FirebaseUser?>
        get() = _currentUser

    private val _googleSignInEvent = MutableLiveData<Unit>()
    val googleSignInEvent: LiveData<Unit> get() = _googleSignInEvent

    private val _authResult = MutableLiveData<Result<FirebaseUser>>()
    val authResult: LiveData<Result<FirebaseUser>> get() = _authResult

    private val _userCreated = MutableLiveData<Boolean>()
    val userCreated: LiveData<Boolean> get() = _userCreated

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage


    fun signInWithEmail(email: String, password: String) {
        _auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Asignamos el usuario si el login es exitoso
                    _currentUser.value = _auth.currentUser
                } else {
                    // Si no es exitoso, asignamos null
                    _currentUser.value = null
                    _errorMessage.value = "Error: Credenciales inválidas"
                }
            }
    }

    fun signUpWithEmail(email: String, password: String) {
        _auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = _auth.currentUser
                    _currentUser.value = user
                    saveUserDataToFirestore(user)
                }
                else {
                    _currentUser.value = null
                    _errorMessage.value = "Error: No se pudo crear la cuenta"
                }
            }
    }

    private fun saveUserDataToFirestore(user: FirebaseUser?) {
        user?.let {
            val userData = hashMapOf(
                "userId" to it.uid,
                "favoritas" to listOf<String>()
            )
            val db = FirebaseFirestore.getInstance()
            db.collection("rutas_favs")
                .document(it.uid)
                .set(userData)
                .addOnSuccessListener {
                    Log.d("Firestore", "Usuario guardado exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error al guardar los datos del usuario: $e")
                }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        _auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("MiPerfilFragment", "Se ha enviado el correo de recuperación.")
                    _errorMessage.value = "Se ha enviado el correo de recuperación."

                } else {
                    Log.w("MiPerfilFragment", "Error al enviar correo de recuperación.", task.exception)
                    _errorMessage.value = "Error: No se ha podido enviar el correo de recuperación."
                }
            }
    }

    fun signOut() {
        _auth.signOut()
        _currentUser.value = null
    }
    /*

    fun signInWithGoogle() {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        _auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = _auth.currentUser
                    _currentUser.value = user
                } else {
                    Log.w("MiPerfilFragment", "signInWithCredential:failure", task.exception)
                    _currentUser.value = null
                }
            }
    }

     */

    fun signInWithGoogle() {

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)

    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("MiPerfilFragment", "Google sign in failed", e)
            }
        }
    }


    fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        _auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = _auth.currentUser
                    _currentUser.value = user
                    _authResult.value = Result.success(user)

                    // Verificar si el usuario ya existe en la base de datos
                    val db = FirebaseFirestore.getInstance()
                    val userRef = db.collection("rutas_favs").document(user?.uid ?: "")

                    userRef.get().addOnSuccessListener { document ->
                        if (!document.exists()) {
                            // El usuario no existe, se añade a la base de datos
                            val userData = hashMapOf(
                                "userId" to user?.uid,
                                "favoritas" to listOf<String>() // Inicializamos con una lista vacía de rutas favoritas
                            )
                            userRef.set(userData)
                                .addOnSuccessListener {
                                    _userCreated.value = true
                                    Log.d("Firestore", "Usuario guardado exitosamente")
                                }
                                .addOnFailureListener { e ->
                                    _userCreated.value = false
                                    Log.e("Firestore", "Error al guardar los datos del usuario: $e")
                                }
                        }
                    }.addOnFailureListener { e ->
                        _userCreated.value = false
                        Log.e("Firestore", "Error al verificar si el usuario existe: $e")
                    }
                } else {
                    _authResult.value = Result.failure(task.exception ?: Exception("Unknown error"))
                    _currentUser.value = null
                }
            }
    }





}
