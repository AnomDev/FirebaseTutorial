package com.anomdev.firebasetutorial

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.android.synthetic.main.activity_home.*

class AuthActivity : AppCompatActivity() {

    private val GOOGLE_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        /*
        //Splash Screen

        Thread.sleep(2000)
        setTheme(R.style.AppTheme)
        */

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        //Analytics Event
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integracion de Firebase completa")
        analytics.logEvent("InitScreen", bundle)

        //Setup
        setup()
        session()
    }


    private fun setup() {
        title = "Autenticación"

        //Aquí está la lógica para el registro por medio de usuario y contraseña
        signup_btn.setOnClickListener {

            //Aquí realizaremos todas las comprobaciones de los campos email y password
            if (email_et.text.isNotEmpty() && password_et.text.isNotEmpty()) {

                //En este caso sólo miramos que ambos textos no esten vacíos
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email_et.text.toString(),
                    password_et.text.toString()).addOnCompleteListener{

                        //Aquí nos notificará si la petición se ha cumplido satisfactoriamente
                        if (it.isSuccessful){
                            showHome(it.result?.user?.email ?: "",ProviderType.BASIC)
                        } else {
                            showAlert()
                        }
                }
            } else {
                showAlert()
            }
        }

        //Aquí está la lógica para el login mediante usuario y contraseña
        login_btn.setOnClickListener {

            //Aquí realizaremos todas las comprobaciones de los campos email y password
            if (email_et.text.isNotEmpty() && password_et.text.isNotEmpty()) {

                //En este caso sólo miramos que ambos textos no esten vacíos
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email_et.text.toString(),
                    password_et.text.toString()).addOnCompleteListener{

                    //Aquí nos notificará si la petición se ha cumplido satisfactoriamente
                    if (it.isSuccessful){
                        showHome(it.result?.user?.email ?: "",ProviderType.BASIC)
                    } else {
                        showAlert()
                    }
                }
            }
        }

        //Aquí está la lógica para el registro/login mediante Gmail
        google_btn.setOnClickListener{

            //Configuración de la autenticación
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut() //De esta manera cerramos la sesión que pueda estar iniciada antes cuando pinchamos el botón de Google.

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN) //Aquí iniciamos una activity pidiendo que nos responda algo (el id de autenticación GOOGLE_SIGN_IN")


        }
    }




    // Creamos una función para mostrar un Alert si algo ha ido mal
    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error en el proceso de autenticación")
        builder.setPositiveButton("Aceptar",null)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //En esta función realizamos la navegación entre pantallas (de Auth a Home) con los datos de mail y pass.
    private fun showHome(email: String, provider: ProviderType){
        val homeIntent = Intent(this, HomeActivity::class.java).apply{
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }

    //Aquí recuperaremos si existe un email y un provider, lo que significaría que existe una sesión activa (user autenticado)
    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email",null)
        val provider = prefs.getString("provider",null)

        //Si existen mail y proveedor, no mostrará el layout de Auth y nos llevará a Home con showHome
        if (email!= null && provider != null) {
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
        super.onActivityResult(requestCode, resultCode, data)

        //Esta parte autentica en los servidores de GOOGLE.
        if (requestCode == GOOGLE_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data) //Recuperamos la respuesta

            // Cuando se llama a un ActivityResult, la línea de Account tiene que estar protegida contra nulos en el caso de
            // que no sea capaz de recuperar una cuenta. Por esta razón se encapsula en un try...catch
            try {
                val account = task.getResult(ApiException::class.java) //Recuperamos la cuenta autenticada

                //Esta otra parte usa la anterior autenticación en GOOGLE para autenticarse en la consola de Firebase.
                if (account != null){
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful){
                            showHome(account.email ?: "", ProviderType.GOOGLE)
                        } else {
                            showAlert()
                        }
                    }

                }
            } catch (e: ApiException){
                showAlert()
            }


        }
    }

}