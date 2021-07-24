package com.anomdev.firebasetutorial

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_auth.*
import java.util.regex.Pattern

class AuthActivity : AppCompatActivity() {

    private val GOOGLE_SIGN_IN = 100
    private val callBackManager = CallbackManager.Factory.create()

    //TODO: Esto me servirá para validar el formato del email pero falta implementarlo.
    private val EMAIL_ADDRESS_PATTERN = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"


    override fun onCreate(savedInstanceState: Bundle?) {

        //Splash Screen

        Thread.sleep(2500)
        setTheme(R.style.Theme_FirebaseTutorial)


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        //Analytics Event
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integracion de Firebase completa")
        analytics.logEvent("InitScreen", bundle)

        //Registros y login
        setup()

        //Comprobación de sesión abierta anterior
        session()
    }


    private fun setup() {
        title = "Autenticación"

        //Aquí está la lógica para el registro por medio de usuario y contraseña
        signup_btn.setOnClickListener {
            validateSignUpForm()

            if (validateSignUpForm()) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    email_et.text.toString(),
                    password_et.text.toString()
                ).addOnCompleteListener {

                    //Aquí nos notificará si la petición se ha cumplido satisfactoriamente
                    if (it.isSuccessful) {
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)

                    }
                }
            }
        /*

            //TODO: La gracia sería meter todas las validaciones en una función y en Setup() llamarla para no llenarlo todo de código
            //if (validateForm())


            //Aquí realizaremos todas las comprobaciones de los campos email y password
            if (email_et.text.isNotEmpty()
                && email_et.text.isNotBlank()
                && email_et.text.contains("@" + ".")
                && password_et.text.isNotEmpty()
                && password_et.text.isNotBlank()
                && password_et.text.length >= 6
            ) {

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    email_et.text.toString(),
                    password_et.text.toString()
                ).addOnCompleteListener {

                    //Aquí nos notificará si la petición se ha cumplido satisfactoriamente
                    if (it.isSuccessful) {
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                    } else {
                        showAlert(
                            "Error en el registro",
                            "Algo ha fallado en el registro con usuario y contraseña. Vuelve a intentarlo más adelante."
                        )
                    }
                }
            } else {
                if (!email_et.text.isNotEmpty()) {
                    showAlert(
                        "Email sin rellenar",
                        "Por favor, asegúrate de rellenar el email"
                    )

                }
                if (!email_et.text.isNotBlank()) {
                    showAlert(
                        "Espacios en blanco",
                        "Por favor, asegúrate de no dejar espacios en blanco"
                    )
                }
                if (!email_et.text.contains("@")) {
                    showAlert(
                        "Formato incorrecto",
                        "El formato del email no tiene la sintáxis correcta. Revísalo."
                    )
                }
                if (!password_et.text.isNotEmpty()) {
                    showAlert(
                        "Contraseña sin rellenar",
                        "Por favor, asegúrate de rellenar la contraseña"
                    )
                }
                if (!password_et.text.isNotBlank()) {
                    showAlert(
                        "Espacios en blanco",
                        "La contraseña no puede tener espacios en blanco"
                    )
                }
                if (password_et.text.length < 6) {
                    showAlert(
                        "Contraseña demasiado corta",
                        "La contraseña debe tener como mínimo 6 caractéres"
                    )
                }

            }*/
        }

        //Aquí está la lógica para el login mediante usuario y contraseña
        login_btn.setOnClickListener {

            //Aquí realizaremos todas las comprobaciones de los campos email y password
            if (email_et.text.isNotEmpty() && password_et.text.isNotEmpty()) {

                //En este caso sólo miramos que ambos textos no esten vacíos
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    email_et.text.toString(),
                    password_et.text.toString()
                ).addOnCompleteListener {

                    //Aquí nos notificará si la petición se ha cumplido satisfactoriamente
                    if (it.isSuccessful) {
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                    } else {
                        showAlert(
                            "Error en el login",
                            "Algo ha fallado en el login con usuario y contraseña. Vuelve a intentarlo más adelante."
                        )
                    }
                }
            } else {
                showAlert(
                    "Campos sin rellenar",
                    "Por favor, asegúrate de rellenar todos los campos"
                )
            }
        }

        //Aquí está la lógica para el registro/login mediante Gmail
        google_btn.setOnClickListener {

            //Configuración de la autenticación
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut() //De esta manera cerramos la sesión que pueda estar iniciada antes cuando pinchamos el botón de Google.

            startActivityForResult(
                googleClient.signInIntent,
                GOOGLE_SIGN_IN
            ) //Aquí iniciamos una activity pidiendo que nos responda algo (el id de autenticación GOOGLE_SIGN_IN")

        }

        //Aquí está la lógica para el registro/login mediante Facebook
        /*TODO:
           No se yo si este login va a funcionar ya que creo que no está bien puesta la hash de clave de desarrollo
           He puesto esta:
                4juqxR+bfCEhOEDslBTwAePpz74=
           usando este comando:
                keytool -exportcert -alias androiddebugkey -keystore "C:\Documents and Settings\Administrator.android\debug.keystore" | "C:\OpenSSL\bin\openssl" sha1 -binary |"C:\OpenSSL\bin\openssl" base64
           usando la CMD en la ruta:
                 C:\Program Files\Java\jdk-16.0.2\bin>
        */
        facebook_btn.setOnClickListener {

            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))

            LoginManager.getInstance().registerCallback(callBackManager,
                object : FacebookCallback<LoginResult> {

                    override fun onSuccess(result: LoginResult?) { //Aquí nos habríamos autenticado en el servidor de Facebook
                        result?.let {
                            val tokenAuthFacebook = it.accessToken

                            val credential =
                                FacebookAuthProvider.getCredential(tokenAuthFacebook.token)
                            FirebaseAuth.getInstance().signInWithCredential(credential)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        showHome(
                                            it.result?.user?.email ?: "",
                                            ProviderType.FACEBOOK
                                        )
                                    } else {
                                        showAlert(
                                            "Facebook Error",
                                            "Algo ha fallado en el login con Facebook. Vuelve a intentarlo más adelante."
                                        )
                                    }
                                }
                        }
                    }

                    override fun onCancel() {

                    }

                    override fun onError(error: FacebookException?) {
                        showAlert()
                    }
                })
        }
    }


    //En esta función realizamos la navegación entre pantallas (de Auth a Home) con los datos de mail y pass.
    private fun showHome(email: String, provider: ProviderType) {
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }

    //Aquí recuperaremos si existe un email y un provider, lo que significaría que existe una sesión activa (user autenticado)
    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        //Si existen mail y proveedor, no mostrará el layout de Auth y nos llevará a Home con showHome
        if (email != null && provider != null) {
            auth_layout.visibility = View.INVISIBLE
            showHome(email, ProviderType.valueOf(provider))
        }
    }

    //Aquí sobreescribimos la función onStart para cuando cerremos sesión y pasemos de Home a AuthActivity,
    //haciendo visible de nuevo el layaout de Auth
    override fun onStart() {
        super.onStart()
        auth_layout.visibility = View.VISIBLE

    }

    //Al iniciar una actividad pidiendole una respuesta, necesitamos sobreescribir onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callBackManager.onActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)

        //Esta parte autentica en los servidores de GOOGLE.
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data) //Recuperamos la respuesta

            // Cuando se llama a un ActivityResult, la línea de Account tiene que estar protegida contra nulos en el caso de
            // que no sea capaz de recuperar una cuenta. Por esta razón se encapsula en un try...catch
            try {
                val account =
                    task.getResult(ApiException::class.java) //Recuperamos la cuenta autenticada

                //Esta otra parte usa la anterior autenticación en GOOGLE para autenticarse en la consola de Firebase.
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                showHome(account.email ?: "", ProviderType.GOOGLE)
                            } else {
                                showAlert(
                                    "Gmail Error",
                                    "Algo ha fallado en el login con Gmail. Vuelve a intentarlo más adelante."
                                )
                            }
                        }

                }
            } catch (e: ApiException) {
                showAlert("Error TryCatch", "Error en el try...catch")
            }
        }
    }

    private fun validateSignUpForm(): Boolean{
        //Aquí realizaremos todas las comprobaciones de los campos email y password
        if (email_et.text.isNotEmpty()
            && email_et.text.isNotBlank()
            && email_et.text.contains("@")
            && password_et.text.isNotEmpty()
            && password_et.text.isNotBlank()
            && password_et.text.length >= 6
        ) {
            //TODO: Realmente esto está vacío, por tanto si todo es correcto no hace nada (salvo que el return true haga referencia a este código).
        } else {
            if (!email_et.text.isNotEmpty()) {
                showAlert(
                    "Email sin rellenar",
                    "Por favor, asegúrate de rellenar el email"
                )

            }
            if (!email_et.text.isNotBlank()) {
                showAlert(
                    "Espacios en blanco",
                    "Por favor, asegúrate de no dejar espacios en blanco"
                )
            }
            if (!email_et.text.contains("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                        "\\@" +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                        "(" +
                        "\\." +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                        ")+")) {
                showAlert(
                    "Formato incorrecto",
                    "El formato del email no tiene la sintáxis correcta. Revísalo."
                )
            }
            if (!password_et.text.isNotEmpty()) {
                showAlert(
                    "Contraseña sin rellenar",
                    "Por favor, asegúrate de rellenar la contraseña"
                )
            }
            if (!password_et.text.isNotBlank()) {
                showAlert(
                    "Espacios en blanco",
                    "La contraseña no puede tener espacios en blanco"
                )
            }
            if (password_et.text.length < 6) {
                showAlert(
                    "Contraseña demasiado corta",
                    "La contraseña debe tener como mínimo 6 caractéres"
                )
            }
        }
        // TODO: En cambio, en mi cabeza aquí debería devolver un false, porque está después del else :S
        return true
    }

    // Creamos una función para mostrar un Alert si algo ha ido mal
    private fun showAlert(
        title: String = "Error",
        message: String = "Algo ha ido mal. Pruebe de nuevo más adelante."
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}