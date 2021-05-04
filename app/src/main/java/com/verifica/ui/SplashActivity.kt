package com.verifica.ui

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.gunar.uta.data.PairData
import com.gunar.uta.data.Static.Companion.CODE
import com.verifica.MainActivity
import com.verifica.R
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class SplashActivity : AppCompatActivity() {

    var newExecutor: Executor = Executors.newSingleThreadExecutor()
    var activity: FragmentActivity = this


    private var mDelayHandler: Handler? = null
    private val SPLASH_DELAY: Long = 4 //3 seconds

    private var mKeyguardManager: KeyguardManager? = null
    private val REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 1


//    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val mRunnable: Runnable = Runnable {

        if (!isFinishing) {

            val intent = Intent(this, WelcomActivity::class.java)
            startActivity(intent)
            finish()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        mKeyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager

        if (!mKeyguardManager!!.isKeyguardSecure) {
            // Show a message that the user hasn't set up a lock screen.
            alert(
                "Error de Credenciales",
                "Necesitas definir algun tipo de protección para usar la app"
            )
            return
        }


        val myBiometricPrompt = BiometricPrompt(
            activity,
            newExecutor,
            object : BiometricPrompt.AuthenticationCallback() {
                //onAuthenticationError is called when a fatal error occurrs//
                override fun onAuthenticationError(
                    errorCode: Int,
                    @NonNull errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        var a = 4
                    } else {

                    }
                    if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS) {
                        var a = 4
                    }
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        var a = 4
                    }
                    if (errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT) {
                        var a = 4
                    }
                }

                //onAuthenticationSucceeded is called when a fingerprint is matched successfully//
                override fun onAuthenticationSucceeded(@NonNull result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    goToMainActivity()
//                    Log.d(TAG, "Fingerprint recognised successfully")
                }

                //onAuthenticationFailed is called when the fingerprint doesn’t match//
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    alert("No se pudo identificar el acceso", "Vuelve a intentar")
//                    Log.d(TAG, "Fingerprint not recognised")
                }
            })

        var promptInfo: BiometricPrompt.PromptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Necesitamos que te identifiques")
                .setDescription("Si perdiste el acceso reinstala la app, se perderan los datos definitivamente")
                .setNegativeButtonText("Cancelar").build()

        val sharedPref = applicationContext.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )

        val code = sharedPref.getString(CODE, "")

        if (code.isNullOrEmpty()) {
            mDelayHandler = Handler()
            mDelayHandler!!.postDelayed(mRunnable, 1000)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                showAuthenticationScreen()
            } else {
                myBiometricPrompt.authenticate(promptInfo)
            }
        }
    }

    fun alert(title: String, message: String) {
        val builder = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(false)
        builder.setPositiveButton("Listo") { dialog, which ->
            finish()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()

        return
    }

    public override fun onDestroy() {
        if (mDelayHandler != null) {
            mDelayHandler!!.removeCallbacks(mRunnable)
        }
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showAuthenticationScreen() {
        val intent: Intent = mKeyguardManager!!.createConfirmDeviceCredentialIntent(null, null)
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            if (resultCode == RESULT_OK) {
//                if (tryEncrypt()) {
//                    showPurchaseConfirmation()
//                }
                goToMainActivity()

            } else {
                alert("No se pudo identificar el acceso", "Vuelve a intentar")
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun goToMainActivity() {

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
