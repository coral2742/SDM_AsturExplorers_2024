package sdm.com.asturexplorers

import MiPerfilViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import coil.load
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseUser

class MiPerfilFragment : Fragment() {
    private val viewModel: MiPerfilViewModel by viewModels()

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    private lateinit var tvTitle: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var ivUserProfilePic: ImageView
    private lateinit var inputEmail: TextInputLayout
    private lateinit var inputPassword: TextInputLayout
    private lateinit var btnGoogleSignIn: Button
    private lateinit var btnSignOut: Button
    private lateinit var btnLogin: Button
    private lateinit var txtOlvidarPass: TextView
    private lateinit var txtNoTienes: TextView
    private lateinit var txtRegistraAqui: TextView
    private lateinit var btnGoogleSignUp: Button
    private lateinit var inputRepePassword: TextInputLayout
    private lateinit var btnSignUp: Button
    private lateinit var txtIniciaAqui: TextView
    private lateinit var txtYaTienes: TextView
    private lateinit var divider: View


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.currentUser.observe(viewLifecycleOwner) { newUser ->
            updateUI(newUser)
        }

        viewModel.snackbarMessage.observe(viewLifecycleOwner, Observer { message ->
            if (message != null && message.isNotEmpty()) {
                Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
            }
        })

        // Inicio de sesión con Google
        viewModel.signInIntent.observe(viewLifecycleOwner, Observer { intent ->
            intent?.let {
                if (viewModel.signInIntent.value != null) {
                    // Iniciar la actividad de Google Sign-In - necesario para que aparezca pestaña de Google
                    startActivityForResult(it, RC_SIGN_IN)
                }
            }
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configura Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
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
            checkSignUp()
        }


        txtOlvidarPass.setOnClickListener {
            val email = inputEmail.editText?.text.toString()
            if (email.isEmpty()) {
                inputEmail.error = "Por favor ingresa tu correo electrónico para recuperar la contraseña."
            } else {
                sendPasswordResetEmail(email)
            }
        }

        txtRegistraAqui.setOnClickListener {
            updateUISignUp()
        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                viewModel.firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("MiPerfilFragment", "Google sign in failed", e)
            }
        }
    }

    /**
     * Comprueba que los campos de registro están rellenos correctamente
     */
    private fun checkSignUp() {
        //Borrar posibles avisos de error
        inputPassword.isErrorEnabled=false
        inputEmail.isErrorEnabled=false
        inputRepePassword.isErrorEnabled=false

        val email = inputEmail.editText?.text.toString()
        val password = inputPassword.editText?.text.toString()
        val password2 = inputRepePassword.editText?.text.toString()

        var isValid=true

        //Compruebo email
        if (email.isEmpty()) {
            inputEmail.error = "Por favor, introduce tu email"
            isValid=false
        }else{
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                inputEmail.error = "Por favor, ingresa un correo electrónico válido"
                isValid=false
            }
        }

        //Compruebo contraseña
        if (password.isEmpty()) {
            inputPassword.error = "Por favor, introduce tu contraseña"
            isValid=false
        }else{
            if (password.length < 6) {
                inputPassword.error = "La contraseña debe tener al menos 6 caracteres"
                isValid=false
            }
        }

        //Compruebo repetición contraseña
        if (password2.isEmpty()) {
            inputRepePassword.error = "Por favor, repite tu contraseña"
            isValid=false
        }else{
            if (password != password2) {
                inputRepePassword.error = "Las contraseñas no coinciden"
                isValid=false
            }
        }

        //Si todos los campos están rellenos correctamente, crear cuenta
        if(isValid){
            viewModel.signUpWithEmail(email, password)
        }
    }

    /**
     * Comprobación de campos. Inicia sesión con email y contraseña
     */
    private fun signInWithEmail() {
        val email = inputEmail.editText?.text.toString()
        val password = inputPassword.editText?.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            if (email.isEmpty()) {
                inputEmail.error = "Por favor, introduce tu email"
            }
            if (password.isEmpty()) {
                inputPassword.error = "Por favor, introduce tu contraseña"
            }
            return
        }
        viewModel.signInWithEmail(email, password)

        // Observamos el LiveData para manejar la respuesta
        viewModel.currentUser.observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                SessionManager.currentUser = user
                updateUI(user)
            }
            else {
                updateUI(null)
            }
        })
    }

    /**
     * Actualiza la interfaz de usuario para el registro
     */
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
        inputRepePassword.visibility = View.VISIBLE
        inputEmail.editText?.text?.clear()
        inputPassword.editText?.text?.clear()
        inputRepePassword.editText?.text?.clear()
        txtOlvidarPass.visibility = View.GONE
        txtNoTienes.visibility = View.GONE
        txtRegistraAqui.visibility = View.GONE

        txtIniciaAqui.visibility = View.VISIBLE
        txtYaTienes.visibility = View.VISIBLE

        btnGoogleSignUp.visibility = View.VISIBLE
        divider.visibility = View.GONE

        //Borrar posibles avisos de error
        inputPassword.isErrorEnabled=false
        inputEmail.isErrorEnabled=false
        inputRepePassword.isErrorEnabled=false
    }

    /**
     * Actualiza la interfaz de usuario con la información del usuario
     * o la pantalla de inicio de sesión si no hay usuario logueado
     */
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
            inputRepePassword.visibility = View.GONE

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
            inputEmail.editText?.text?.clear()
            inputPassword.editText?.text?.clear()
            txtOlvidarPass.visibility = View.VISIBLE
            txtNoTienes.visibility = View.VISIBLE
            txtRegistraAqui.visibility = View.VISIBLE
            txtIniciaAqui.visibility = View.GONE
            txtYaTienes.visibility = View.GONE

            btnGoogleSignUp.visibility = View.GONE
            divider.visibility = View.VISIBLE

            //Borrar posibles avisos de error
            inputPassword.isErrorEnabled=false
            inputEmail.isErrorEnabled=false
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        viewModel.sendPasswordResetEmail(email);
    }

    private fun signInWithGoogle() {
        viewModel.signInWithGoogle(requireContext())
    }

    /**
     * Cierra la sesión del usuario
     */
    private fun signOut() {
        viewModel.signOut(requireContext())
    }


}
