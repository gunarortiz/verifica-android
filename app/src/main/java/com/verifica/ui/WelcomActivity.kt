package com.verifica.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyInfo
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.verifica.R
import kotlinx.android.synthetic.main.activity_welcom.*
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.*
import javax.security.auth.x500.X500Principal


class WelcomActivity : AppCompatActivity() {

    private val REQUEST_CODE = 101
    private val KEY_ALIAS = "Verifica_Key_Alias1"
    private val KEYSTORE_TYPE = "AndroidKeyStore"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcom)

        go_login_btn.setOnClickListener { goToLogin() }
        go_signup_btn.setOnClickListener { goToSignup() }



        if (isKeyPresent()){
            if(isHardwareBackedKeyStore()){
                ll_si_secure.visibility = View.VISIBLE

            }
            else{
                ll_no_secure.visibility = View.VISIBLE
            }
        }
        else{
            createKey(applicationContext)
        }
    }



    fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun goToSignup() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
    }

    private fun isKeyPresent(): Boolean{
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val privateKey = keyStore.getKey(KEY_ALIAS, null)
        if (privateKey != null){
            return true
        }
        return false
    }


    fun createKey(context: Context): Boolean {
        try {

            return if (!isKeyPresent()) {
                Log.d("TAG", "Creating new KEY. KEY ALIAS NOT FOUND")
                val start = Calendar.getInstance()
                val end = Calendar.getInstance()
                end.add(Calendar.YEAR, 10)
                val spec = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    KeyPairGeneratorSpec.Builder(context)
                        .setAlias(KEY_ALIAS)
                        .setSubject(X500Principal("CN=" + KEY_ALIAS.toString() + ", O=Android Authority"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.time)
                        .setEndDate(end.time)
                        .build()
                } else {
                    TODO("VERSION.SDK_INT < JELLY_BEAN_MR2")
                }
                val generator: KeyPairGenerator = KeyPairGenerator.getInstance("RSA", KEYSTORE_TYPE)
                generator.initialize(spec)
                generator.generateKeyPair()

                if(isHardwareBackedKeyStore()){
                    ll_si_secure.visibility = View.VISIBLE

                }
                else{
                    ll_no_secure.visibility = View.VISIBLE
                }

                true
            } else {
                Log.d("TAG", "KEY ALIAS Exists")
                false
            }
        } catch (e: java.lang.Exception) {
//            Log.e(TAG, Log.getStackTraceString(e))
        }
        return false
    }

    private fun isHardwareBackedKeyStore(): Boolean {
        if (Build.VERSION.SDK_INT < 23) {
            return false
        } else {
            try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                val privateKey = keyStore.getKey(KEY_ALIAS, null)
                val keyFactory = KeyFactory.getInstance(privateKey.algorithm, "AndroidKeyStore")
                val keyInfo = keyFactory.getKeySpec(
                    privateKey,
                    KeyInfo::class.java
                )
                return keyInfo.isInsideSecureHardware
            } catch (e: Exception) {
            }
        }
        return false
    }
}