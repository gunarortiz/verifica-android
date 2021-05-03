package com.verifica.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import com.verifica.R
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_welcom.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class LoginActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        go_to_import_btn.setOnClickListener { goToImport() }
        go_to_recover.setOnClickListener { goToRecover() }


    }

    fun goToImport() {
        val intent = Intent(this, ImportCetificateActivity::class.java)
        startActivity(intent)
    }

    fun goToRecover() {
        val intent = Intent(this, RecoverActivity::class.java)
        startActivity(intent)
    }
}