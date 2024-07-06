package com.example.trelloproject.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.example.trelloproject.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {
    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding.btnSignUpIntro.setOnClickListener {
            startActivity(Intent(this@IntroActivity,
                SingUpActivity::class.java))
        }
        binding.btnSignInIntro.setOnClickListener {
            startActivity(Intent(this@IntroActivity,
                SingInActivity::class.java))
        }
    }
}