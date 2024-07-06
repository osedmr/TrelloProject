package com.example.trelloproject.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.example.trelloproject.databinding.ActivitySlashBinding
import com.example.trelloproject.firebase.FireStoreClass

class SlashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySlashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySlashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val typeFace:Typeface= Typeface.createFromAsset(assets,"good times rg.otf")
        binding.splashPage.typeface=typeFace

        Handler().postDelayed({

            var currentUserId=FireStoreClass().getCurrentUserId()
            if (currentUserId.isNotEmpty()){
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        }, 3000)


    }
}