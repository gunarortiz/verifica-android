package com.verifica.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.gunar.uta.data.Static.Companion.KEYSTORE_TYPE
import com.gunar.uta.data.Static.Companion.KEY_ALIAS
import com.verifica.R
import kotlinx.android.synthetic.main.activity_welcom.*
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.InvalidKeySpecException
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.security.auth.x500.X500Principal


class WelcomActivity : AppCompatActivity() {

    private val REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcom)

        go_login_btn.setOnClickListener { goToLogin() }
        go_signup_btn.setOnClickListener { goToSignup() }



        if (isKeyPresent()) {
            if (isHardwareBackedKeyStore()) {
                ll_si_secure.visibility = View.VISIBLE

            } else {
                ll_no_secure.visibility = View.VISIBLE
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                createKey(applicationContext)
            }
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

    private fun isKeyPresent(): Boolean {
        val keyStore = KeyStore.getInstance(KEYSTORE_TYPE)
        keyStore.load(null)
        val privateKey = keyStore.getKey(KEY_ALIAS, null)

        if (privateKey != null) {
            Log.d("TAG", "esta presente" + privateKey.toString())

            return true
        }
        return false
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun createKey(context: Context): Boolean {

        val keyGenerator: KeyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_TYPE
        );

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()


        keyGenerator.init(keyGenParameterSpec)
        val secretKey: SecretKey = keyGenerator.generateKey()

        Log.d("TAG", "Creating new KEY. KEY ALIAS NOT FOUND" + secretKey.toString())
        if (isHardwareBackedKeyStore()) {
            ll_si_secure.visibility = View.VISIBLE

        } else {
            ll_no_secure.visibility = View.VISIBLE
        }

//        try {
//
//            return if (!isKeyPresent()) {
//                Log.d("TAG", "Creating new KEY. KEY ALIAS NOT FOUND")
//                val start = Calendar.getInstance()
//                val end = Calendar.getInstance()
//                end.add(Calendar.YEAR, 10)
//                val spec = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                    KeyPairGeneratorSpec.Builder(context)
//                        .setAlias(KEY_ALIAS)
//                        .setSubject(X500Principal("CN=" + KEY_ALIAS.toString() + ", O=Android Authority"))
//                        .setSerialNumber(BigInteger.ONE)
//                        .setStartDate(start.time)
//                        .setEndDate(end.time)
//                        .build()
//                } else {
//                    TODO("VERSION.SDK_INT < JELLY_BEAN_MR2")
//                }
//                val generator: KeyPairGenerator = KeyPairGenerator.getInstance("RSA", KEYSTORE_TYPE)
//                generator.initialize(spec)
//                generator.generateKeyPair()
//
//                if(isHardwareBackedKeyStore()){
//                    ll_si_secure.visibility = View.VISIBLE
//
//                }
//                else{
//                    ll_no_secure.visibility = View.VISIBLE
//                }
//
//                true
//            } else {
//                Log.d("TAG", "KEY ALIAS Exists")
//                false
//            }
//        } catch (e: java.lang.Exception) {
////            Log.e(TAG, Log.getStackTraceString(e))
//        }
        return false
    }

    private fun isHardwareBackedKeyStore(): Boolean {
        val keyStore = KeyStore.getInstance(KEYSTORE_TYPE)
        keyStore.load(null)
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey

        //check key info
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            var factory: SecretKeyFactory =
                SecretKeyFactory.getInstance(secretKey.getAlgorithm(), KEYSTORE_TYPE);
            var keyInfo: KeyInfo

            try {
                keyInfo = factory.getKeySpec(secretKey, KeyInfo::class.java) as KeyInfo

                return keyInfo.isInsideSecureHardware
            } catch (e: InvalidKeySpecException) {
//                String checkKeyInfo = "";
            }
            }
            return false
        }
    }