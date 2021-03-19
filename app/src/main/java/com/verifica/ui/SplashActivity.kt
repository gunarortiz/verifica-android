package com.verifica.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gunar.uta.data.PairData
import com.verifica.R


class SplashActivity : AppCompatActivity() {

    private var mDelayHandler: Handler? = null
    private val SPLASH_DELAY: Long = 4 //3 seconds

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

        mDelayHandler = Handler()
        mDelayHandler!!.postDelayed(mRunnable, 1000)

//        val sharedPref = applicationContext.getSharedPreferences(
//                getString(R.string.preference_file_key), Context.MODE_PRIVATE
//        )

//        val choferId = sharedPref.getString("choferId", "")
//        val sindicatoId = sharedPref.getString("sindicatoId", "")

//        if (choferId!!.isNotEmpty()) {
//            mDelayHandler!!.removeCallbacks(mRunnable)
//
//            db.collection("chofer").document(choferId).get().addOnCompleteListener {
//
//                if (it.isSuccessful) {
//                    if (it.result!!.data != null) {
//
////                        val chofer1 = it.result!!.toObject(Chofer::class.java)
////                        chofer = PairData(it.result!!.id, chofer1!!)
//
//
//
//                    } else {
////                        goToLogin()
//                    }
//                }
//
//            }
//
//        } else {
////            val currenteUser = FirebaseAuth.getInstance().currentUser
////            if (currenteUser != null) {
////                val intent = Intent(this, ListSindicatosActivity::class.java)
////                startActivity(intent)
////                finish()
////            }
//        }


    }
//
//    fun goToLogin() {
//        val intent = Intent(this, LoginActivity::class.java)
//        startActivity(intent)
//        finish()
//    }


    var chofer: PairData<String>? = null



    fun alert(){
        val builder = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        builder.setTitle("Servicio deshabilitado")
        builder.setMessage("Contactese con el administrador")
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
}
