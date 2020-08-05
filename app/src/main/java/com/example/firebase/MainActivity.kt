package com.example.firebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val GOOGLE_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //lanzar eventos google analytics
        val analyics:FirebaseAnalytics= FirebaseAnalytics.getInstance(this)
        val bundle=Bundle()
        bundle.putString("message","Firebase Completado")
        analyics.logEvent("Inicioscreen", bundle)
        setup()
    }

    private fun setup() {
        title="Autenticación"
        registrarButton.setOnClickListener{
            if (emailEditText.text.isNotEmpty() && passowordEditText.text.isNotEmpty())  {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailEditText.text.toString(),passowordEditText.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful){
                        showHome(it.result?.user?.email ?: "",ProviderType.BASIC)
                    }
                    else{
                        showAlert()
                    }
                }
            }
        }
        accederButton.setOnClickListener{
            if (emailEditText.text.isNotEmpty() && passowordEditText.text.isNotEmpty())  {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEditText.text.toString(),passowordEditText.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful){
                        showHome(it.result?.user?.email ?: "",ProviderType.BASIC)
                    }
                    else{
                        showAlert()
                    }
                }
            }
        }


        googleButton.setOnClickListener {
            val googleConf: GoogleSignInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

            val googleClient: GoogleSignInClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }
    }
    private fun showAlert(){
        val builder=AlertDialog.Builder(this)
        builder.setTitle("error")
        builder.setMessage("Se ha producido un error en auth")
        builder.setPositiveButton("Aceptar",null)
        val dialog:AlertDialog=builder.create()
        dialog.show()
    }

    private fun showHome(email:String, provider: ProviderType){
    val homeIntent = Intent(this,HomeActivity::class.java).apply {
        putExtra("email",email)
        putExtra("provider",provider.name)
    }
        startActivity(homeIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {


                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                if (account != null) {
                    val credential: AuthCredential =
                        GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                showHome(account.email ?: "", ProviderType.GOOGLE)
                            } else {
                                showAlert()
                            }
                        }
                }

            } catch (e: ApiException)  {
                showAlert()
            }
        }
    }
}