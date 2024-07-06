package com.example.trelloproject.activities

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.example.trelloproject.R
import com.example.trelloproject.databinding.ActivitySingInBinding
import com.example.trelloproject.models.User
import com.google.firebase.auth.FirebaseAuth

class SingInActivity : BaseActivity() {
    private lateinit var binding: ActivitySingInBinding
    lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingInBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()
        auth = FirebaseAuth.getInstance()
        binding.btnSignUp.setOnClickListener {
            singInRegisteredUser()
        }
    }

    fun signInSuccess(user: User){
        hideProgressDialog()
        startActivity(Intent(this@SingInActivity,MainActivity::class.java))
       this@SingInActivity.finish()


    }
    private fun setupActionBar(){
        setSupportActionBar(binding.toolbarIn)
        val actionBar=supportActionBar
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.backpage)
        }

        binding.toolbarIn.setNavigationOnClickListener { onBackPressed() }

    }


    private fun singInRegisteredUser(){
        val email:String=binding.etEmailSingIn.text.toString().trim{it<=' '}
        val password:String=binding.etPasswordSingIn.text.toString().trim{it<=' '}

        if (validateForm(email,password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {

                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        startActivity(Intent(this@SingInActivity,MainActivity::class.java))

                    } else {

                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Böyle bir hesap bulunmamaktadır",
                            Toast.LENGTH_SHORT,
                        ).show()

                    }
                }
        }
    }

    private fun validateForm(email:String,password:String):Boolean{
        return when{

            TextUtils.isEmpty(email)->{
                showErrorSnackBar("lütfen mail adresi giriniz")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Şİfre ni gir la")
                false
            }else->{
                true
            }
        }
    }
}