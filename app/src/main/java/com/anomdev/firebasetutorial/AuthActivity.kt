package com.anomdev.firebasetutorial

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {
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
    }

    private fun setup() {
        title = "Autenticación"

        //Aquí está la lógica para el registro
        logout_btn.setOnClickListener {

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

        //Aquí está la lógica para el login

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
}