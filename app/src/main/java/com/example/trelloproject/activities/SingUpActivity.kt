package com.example.trelloproject.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.example.trelloproject.R
import com.example.trelloproject.databinding.ActivitySingUpBinding
import com.example.trelloproject.firebase.FireStoreClass
import com.example.trelloproject.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SingUpActivity : BaseActivity() {
    private lateinit var binding: ActivitySingUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()
    }

    fun userRegisteredSuccess(){
        Toast.makeText(this@SingUpActivity," başarıyla kaydedildi",Toast.LENGTH_LONG).show()
        hideProgressDialog()

        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun setupActionBar(){
        setSupportActionBar(binding.toolbar)
        val actionBar=supportActionBar
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.backpage)
        }

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        binding.btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser(){ //kaydetme işlemi
        val name:String=binding.etName.text.toString().trim{it<=' '}
        val email:String=binding.etEmail.text.toString().trim{it<=' '}
        val password:String=binding.etPassword.text.toString().trim{it<=' '}

        if (validateForm(name,email,password)){
            showProgressDialog(resources.getString(R.string.please_wait))

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task->
                    if (task.isSuccessful){
                        val firebaseUser:FirebaseUser=task.result!!.user!!
                        val registerEmail=firebaseUser.email!!
                        val user= User(firebaseUser.uid,name,registerEmail)
                        FireStoreClass().registerUser(this,user)
                    }else{
                        Toast.makeText(this@SingUpActivity,
                            task.exception!!.message,Toast.LENGTH_LONG).show()
                    }

            }
        }
    }

    private fun validateForm(name:String,email:String,password:String):Boolean{
        return when{
            TextUtils.isEmpty(name)->{
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please enter a email address")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter a password")
                false
            }else->{
                true
            }
        }
    }
}