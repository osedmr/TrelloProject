package com.example.trelloproject.activities

import android.app.Dialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.trelloproject.R
import com.example.trelloproject.databinding.ActivityBaseBinding
import com.example.trelloproject.databinding.ActivitySingInBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBaseBinding
    private var doubleBackToExitPressedOnce=false
    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    fun showProgressDialog(text: String) { // yükleme diaalog uyarı mesajı
        mProgressDialog = Dialog(this)
        mProgressDialog.setContentView(R.layout.dialog_progress)

        val textView = mProgressDialog.findViewById<TextView>(R.id.tv_progress_text) // text view'in id'sini buraya ekleyin
        textView.text = text

        mProgressDialog.show()
    }
    fun hideProgressDialog(){
        mProgressDialog.dismiss()
    }

    fun getCurrentUserID():String{ //olan kullanıcıyı göstermek için kullanırız
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
    fun doubleBackToExit(){
        if(doubleBackToExitPressedOnce){
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce=true
        Toast.makeText(this@BaseActivity,resources.getString(R.string.please_click_back_again_to_exit),Toast.LENGTH_LONG).show()

        Handler().postDelayed({doubleBackToExitPressedOnce==false},2000)
    }

    fun showErrorSnackBar(message:String){
        val snackBar=Snackbar.make(findViewById(android.R.id.content),message,Snackbar.LENGTH_LONG)

        val snackBarView=snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this,R.color.snackBar_error))
        snackBar.show()
    }

}