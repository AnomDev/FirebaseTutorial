package com.anomdev.firebasetutorial

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_home.*

//Creamos una EnumClass con los diferentes tipos de proveedores de registro y login que vamos a tener en nuestra app.
enum class ProviderType{
    BASIC,
    GOOGLE,
    FACEBOOK
}
//Cuando el usuario llega a esta pantalla es porque ya ha hecho el registro y ha sido satisfactorio.
class HomeActivity : AppCompatActivity() {

    //Creamos una instancia global a nuestra base de datos para poder utilizarla en el proyecto
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)



        // Setup
        val bundle: Bundle? = intent.extras
        val email: String? = bundle?.getString("email")
        val provider: String? = bundle?.getString("provider")
        setup(email ?: "", provider ?: "")

        //Guardamos los datos del usuario que se ha autenticado (aunque la App muera, los datos quedan guardados)
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()

    }

    private fun setup(email: String, provider: String) {
        title = "Inicio"
        email_tv.text = email
        provider_tv.text = provider

        //Lógica del botón de Cerrar sesión
        logout_btn.setOnClickListener{
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            if(provider == ProviderType.FACEBOOK.name){
                LoginManager.getInstance().logOut()
        }

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }

        //Lógica del botón de guardar datos en Cloud Firestore
        savedata_btn.setOnClickListener {
            db.collection("users").document(email).set(
                hashMapOf("provider" to provider,
                "address" to address_et.text.toString(),
                "phone" to phone_et.text.toString())
            )
        }

        //Lógica del botón de recuperar datos de Cloud Firestore
        fetchdata_btn.setOnClickListener {
            db.collection("users").document(email).get().addOnSuccessListener {
                address_et.setText(it.get("address")as String?)
                phone_et.setText(it.get("phone")as String?)
            }
        }

        //Lógica del botón de eliminar datos en Cloud Firestore
        erasedata_btn.setOnClickListener {
            db.collection("users").document(email).delete()
        }

    }

    /*fun toastAnomDev () {
    }*/

}