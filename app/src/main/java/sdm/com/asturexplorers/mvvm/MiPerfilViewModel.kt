import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import sdm.com.asturexplorers.R
import sdm.com.asturexplorers.SessionManager


class MiPerfilViewModel : ViewModel() {
    private val _auth: FirebaseAuth = FirebaseAuth.getInstance()


    private val _currentUser = MutableLiveData<FirebaseUser?>(SessionManager.currentUser);
    val currentUser : MutableLiveData<FirebaseUser?>
        get() = _currentUser

    private val _snackbarMessage = MutableLiveData<String>()
    val snackbarMessage: LiveData<String> get() = _snackbarMessage

    private val _isUserSaved = MutableLiveData<Boolean>()


    fun signInWithEmail(email: String, password: String) {
        _auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Asignamos el usuario si el login es exitoso
                    _currentUser.value = _auth.currentUser
                    SessionManager.currentUser = _auth.currentUser
                    _snackbarMessage.value = "Se ha iniciado sesión correctamente"
                } else {
                    // Si no es exitoso, asignamos null
                    //_currentUser.value = null
                    _snackbarMessage.value = "Error: Credenciales inválidas"
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

                    SessionManager.currentUser = user
                }
                else {
                    //_currentUser.value = null
                    _snackbarMessage.value = "Error: No se pudo crear la cuenta"
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
                    Log.d("MiPerfilFragment", "Se ha enviado el correo de recuperación")
                    _snackbarMessage.value = "Se ha enviado el correo de recuperación"

                } else {
                    Log.w(
                        "MiPerfilFragment",
                        "Error al enviar correo de recuperación",
                        task.exception
                    )
                    _snackbarMessage.value =
                        "Error: No se ha podido enviar el correo de recuperación"
                }
            }

    }


    private val _signInIntent = MutableLiveData<Intent?>()
    val signInIntent: LiveData<Intent?> get() = _signInIntent

    fun signInWithGoogle(context: Context) {
        // Configura Google Sign-In

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        val signInIntent = googleSignInClient.signInIntent

        _signInIntent.value = signInIntent
        val user = FirebaseAuth.getInstance().currentUser
        _currentUser.value = user
        SessionManager.currentUser = user
    }







    fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    _currentUser.value = user
                    SessionManager.currentUser = user

                    // Verificar si el usuario ya existe en la base de datos
                    val db = FirebaseFirestore.getInstance()
                    val userRef = db.collection("rutas_favs").document(user?.uid ?: "")

                    userRef.get().addOnSuccessListener { document ->
                        if (!document.exists()) {
                            val userData = hashMapOf(
                                "userId" to user?.uid,
                                "favoritas" to listOf<String>() // Inicializamos con una lista vacía de rutas favoritas
                            )
                            userRef.set(userData)
                                .addOnSuccessListener {
                                    _isUserSaved.value = true
                                }
                                .addOnFailureListener { e ->
                                    _snackbarMessage.value = "Error al guardar los datos del usuario: $e"
                                }
                        }
                    }.addOnFailureListener { e ->
                        _snackbarMessage.value = "Error al verificar si el usuario existe: $e"
                    }
                } else {
                    _snackbarMessage.value = "Error en la autenticación: ${task.exception?.message}"
                }
            }
    }

    fun signOut(context: Context) {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)

        _auth.signOut()
        _currentUser.value = null
        SessionManager.currentUser = null

        _snackbarMessage.value = "Se ha cerrado sesión correctamente"
        _snackbarMessage.value = ""

        googleSignInClient.signOut()
        _signInIntent.value = null

        /*

        // Cerrar sesión del cliente de Google
        googleSignInClient.signOut().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _snackbarMessage.value = "Se ha cerrado sesión correctamente"
            } else {
                _snackbarMessage.value = "Error al cerrar sesión con Google: ${task.exception?.message}"
            }
        }

         */
    }



}
