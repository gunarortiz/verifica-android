package com.verifica.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.verifica.MainActivity
import com.verifica.R
import kotlinx.android.synthetic.main.activity_successful_signup.*

class SuccessfulSignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_successful_signup)

        next_btn.setOnClickListener {
            goToMain()
        }
    }

    fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}