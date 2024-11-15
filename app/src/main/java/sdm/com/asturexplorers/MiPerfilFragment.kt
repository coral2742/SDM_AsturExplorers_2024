package sdm.com.asturexplorers

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import coil.load
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser

class MiPerfilFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    private lateinit var tvTitle: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var ivUserProfilePic: ImageView
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var btnGoogleSignIn: Button
    private lateinit var btnSignOut: Button
    private lateinit var btnLogin: Button
    private lateinit var txtOlvidarPass: TextView
    private lateinit var txtNoTienes: TextView
    private lateinit var txtRegistraAqui: TextView
    private lateinit var btnGoogleSignUp: Button
    private lateinit var inputRepePassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var txtIniciaAqui: TextView
    private lateinit var txtYaTienes: TextView
    private lateinit var divider: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configura Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Coloca aquí tu ID de cliente web de Firebase
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mi_perfil, container, false)

        btnGoogleSignIn = view.findViewById(R.id.btnGoogleSignIn)

        btnLogin = view.findViewById(R.id.btnLogin)

        btnSignOut = view.findViewById(R.id.btnSignOut)

        btnSignUp = view.findViewById(R.id.btnSignUp)


        

        tvTitle = view.findViewById(R.id.textTitle)
        tvUserName = view.findViewById(R.id.txtNombre)
        tvUserEmail = view.findViewById(R.id.txtEmail)
        ivUserProfilePic = view.findViewById(R.id.imageUsuario)
        inputEmail = view.findViewById(R.id.inputEmail)
        inputPassword = view.findViewById(R.id.inputPassword)
        txtOlvidarPass = view.findViewById(R.id.txtOlvidarPass)
        txtNoTienes = view.findViewById(R.id.txtNoTienes)
        txtRegistraAqui = view.findViewById(R.id.txtRegistraAqui)
        inputRepePassword = view.findViewById(R.id.inputRepePassword)
        txtIniciaAqui = view.findViewById(R.id.txtIniciaAqui)
        txtYaTienes = view.findViewById(R.id.txtYaTienes)
        btnGoogleSignUp = view.findViewById(R.id.btnGoogleSignUp)
        divider = view.findViewById(R.id.divider)


        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        btnGoogleSignUp.setOnClickListener {
            signInWithGoogle()
        }

        btnLogin.setOnClickListener {
            signInWithEmail()
        }

        btnSignOut.setOnClickListener {
            signOut()
        }

        txtIniciaAqui.setOnClickListener{
            updateUI(null)
        }

        btnSignUp.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            val password2 = inputRepePassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && password2.isNotEmpty()) {
                if (password.length < 6){
                    inputPassword.error = "La contraseña debe tener al menos 6 caracteres"
                }
                else if (password == password2){
                    signUpWithEmail(email, password)
                }
                else{
                    inputRepePassword.error = "Las contraseñas no coinciden"
                }

            }
            else{
                if (email.isEmpty()) {
                    inputEmail.error = "Por favor, introduce tu email"
                }
                if (password.isEmpty()) {
                    inputPassword.error = "Por favor, introduce tu contraseña"
                }
                if (password2.isEmpty()) {
                    inputRepePassword.error = "Por favor, repite tu contraseña"
                }

            }
        }


        txtOlvidarPass.setOnClickListener {
            val email = inputEmail.text.toString()
            if (email.isEmpty()) {
                inputEmail.error = "Por favor ingresa tu correo electrónico."
            } else {
                sendPasswordResetEmail(email)
            }
        }

        txtRegistraAqui.setOnClickListener {
            updateUISignUp()
        }

        // Verifica si el usuario ya está autenticado
        val currentUser = auth.currentUser
        updateUI(currentUser)

        return view
    }

    private fun signUpWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Snackbar.make(requireView(), "Sign-up successful!", Snackbar.LENGTH_SHORT).show()
                    updateUI(user)
                } else {
                    Log.w("MiPerfilFragment", "createUserWithEmail:failure", task.exception)
                    Snackbar.make(requireView(), "Error: No se pudo crear la cuenta", Snackbar.LENGTH_SHORT).show()
                }
            }
    }

    // UI Crear cuenta
    private fun updateUISignUp() {
        // Botones de inicio de sesión
        btnGoogleSignIn.visibility = View.GONE

        inputRepePassword.visibility = View.VISIBLE
        btnSignUp.visibility = View.VISIBLE 

        btnLogin.visibility = View.GONE
        btnSignOut.visibility = View.GONE

        // Cambiar título
        tvTitle.text = "Crear una cuenta"

        // Ocultar la información del usuario
        tvUserName.visibility = View.GONE
        tvUserEmail.visibility = View.GONE
        ivUserProfilePic.visibility = View.GONE

        // Mostrar campos de email y contraseña
        inputEmail.visibility = View.VISIBLE
        inputPassword.visibility = View.VISIBLE
        inputEmail.text.clear()
        inputPassword.text.clear()
        txtOlvidarPass.visibility = View.GONE
        txtNoTienes.visibility = View.GONE
        txtRegistraAqui.visibility = View.GONE

        txtIniciaAqui.visibility = View.VISIBLE
        txtYaTienes.visibility = View.VISIBLE

        btnGoogleSignUp.visibility = View.VISIBLE
        divider.visibility = View.GONE


    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("MiPerfilFragment", "Se ha enviado el correo de recuperación.")
                    // Mostrar un mensaje de éxito o algo para que el usuario sepa que se envió el correo
                    Snackbar.make(requireView(), "Se ha enviado el correo de recuperación.", Snackbar.LENGTH_SHORT).show()

                } else {
                    Log.w("MiPerfilFragment", "Error al enviar correo de recuperación.", task.exception)
                    // Mostrar un mensaje de error si algo falla
                    Snackbar.make(requireView(), "Error: No se ha podido enviar el correo de recuperación.", Snackbar.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d("MiPerfilFragment", "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w("MiPerfilFragment", "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        // Usuario con sesión iniciada
        if (user != null) {
            tvUserName.text = user.displayName
            tvUserEmail.text = user.email
            user.photoUrl?.let {
                ivUserProfilePic.load(it)
            }

            inputRepePassword.visibility = View.GONE
            btnSignUp.visibility = View.GONE

            // Ocultar el botón de inicio de sesión y mostrar el de cerrar sesión
            btnGoogleSignIn.visibility = View.GONE
            btnSignOut.visibility = View.VISIBLE

            // Cambiar título
            tvTitle.text = "Mi Perfil"

            // Mostrar la información del usuario
            if (tvUserName.text.isEmpty() || tvUserEmail.text.isEmpty()){
                tvUserName.visibility = View.GONE
            }else{
                tvUserName.visibility = View.VISIBLE
            }
            tvUserEmail.visibility = View.VISIBLE
            ivUserProfilePic.visibility = View.VISIBLE

            btnLogin.visibility = View.GONE
            inputEmail.visibility = View.GONE
            inputPassword.visibility = View.GONE

            txtOlvidarPass.visibility = View.GONE
            txtNoTienes.visibility = View.GONE
            txtRegistraAqui.visibility = View.GONE
            txtIniciaAqui.visibility = View.GONE
            txtYaTienes.visibility = View.GONE

            btnGoogleSignUp.visibility = View.GONE
            divider.visibility = View.GONE

        } else {
            // Mostrar botones de inicio de sesión
            btnGoogleSignIn.visibility = View.VISIBLE
            btnLogin.visibility = View.VISIBLE
            btnSignOut.visibility = View.GONE

            inputRepePassword.visibility = View.GONE
            btnSignUp.visibility = View.GONE

            // Cambiar título
            tvTitle.text = "Inicia Sesión"

            // Ocultar la información del usuario
            tvUserName.visibility = View.GONE
            tvUserEmail.visibility = View.GONE
            ivUserProfilePic.visibility = View.GONE

            // Mostrar campos de email y contraseña
            inputEmail.visibility = View.VISIBLE
            inputPassword.visibility = View.VISIBLE
            inputEmail.text.clear()
            inputPassword.text.clear()
            txtOlvidarPass.visibility = View.VISIBLE
            txtNoTienes.visibility = View.VISIBLE
            txtRegistraAqui.visibility = View.VISIBLE
            txtIniciaAqui.visibility = View.GONE
            txtYaTienes.visibility = View.GONE

            btnGoogleSignUp.visibility = View.GONE
            divider.visibility = View.VISIBLE
        }
    }

    private fun signInWithEmail() {
        val email = inputEmail.text.toString()
        val password = inputPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            if (email.isEmpty()) {
                inputEmail.error = "Por favor, introduce tu email"
            }
            if (password.isEmpty()) {
                inputPassword.error = "Por favor, introduce tu contraseña"
            }
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w("MiPerfilFragment", "signInWithEmail:failure", task.exception)
                    Snackbar.make(requireView(), "No se ha podido iniciar sesión. Inténtalo de nuevo.", Snackbar.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun signOut() {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener(requireActivity()) {
            updateUI(null)
        }
    }

    companion object {
        fun newInstance(): MiPerfilFragment = MiPerfilFragment()
    }
}
