package com.verifica.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.verifica.R
import kotlinx.android.synthetic.main.activity_welcom.*

class WelcomActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcom)

        go_login_btn.setOnClickListener { goToLogin() }
        go_signup_btn.setOnClickListener { goToSignup() }

    }

    fun goToLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun goToSignup(){
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
    }
}