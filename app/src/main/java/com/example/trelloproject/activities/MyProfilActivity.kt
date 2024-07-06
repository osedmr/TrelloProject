package com.example.trelloproject.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.trelloproject.R
import com.example.trelloproject.databinding.ActivityMyProfilBinding
import com.example.trelloproject.firebase.FireStoreClass
import com.example.trelloproject.models.User
import com.example.trelloproject.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException


class MyProfilActivity : BaseActivity() {
    private lateinit var binding: ActivityMyProfilBinding

    private var mSelectedImageUri:Uri?=null
    private var mProfilImageURL: String=""
    private lateinit var mUserDetails:User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfilBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setActionBar()

        FireStoreClass().loadUserData(this)

        binding.ivUserImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this@MyProfilActivity)
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE)
            }
        }

        binding.btnUpdate.setOnClickListener {
            if (mSelectedImageUri!=null){
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updataUserProfileData()
            }
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== Constants.READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                //izin varsa direk geçiş yapıcak
                Constants.showImageChooser(this@MyProfilActivity)

            }
        }else{
            Toast.makeText(this@MyProfilActivity,"Erişim izni bulunmamaktadır",Toast.LENGTH_LONG).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode== Activity.RESULT_OK && requestCode== Constants.PICK_IMAGE_REQUEST_CODE && data!!.data!=null){
            mSelectedImageUri=data.data

            try {
                Glide
                    .with(this)
                    .load(mSelectedImageUri) // URL of the image
                    .centerCrop() // Scale type of the image.
                    .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                    .into(binding.ivUserImage)
            }catch (e:IOException){
                e.printStackTrace()
            }

        }
    }

    private fun setActionBar(){

        setSupportActionBar(binding.toolbarMyProfileActivity)
        val actionBar=supportActionBar
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.back_icon)
            actionBar.title=resources.getString(R.string.my_profile)
        }
        binding.toolbarMyProfileActivity.setNavigationOnClickListener{
            onBackPressed()
        }

    }

    fun setUserDataInUI(user:User){
        mUserDetails=user

               Glide
                .with(this)
                .load(user.image) // URL of the image
                .centerCrop() // Scale type of the image.
                .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                .into(binding.ivUserImage)


        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
        if (user.mobile!=0L){
            binding.etMobile.setText(user.mobile.toString())
        }
    }

    private  fun updataUserProfileData(){
       val userHashMap=HashMap<String,Any>()

       if (mProfilImageURL.isNotEmpty() && mProfilImageURL!=mUserDetails.image){
           userHashMap[Constants.IMAGE]=mProfilImageURL

       }
       if (binding.etName.text.toString()!=mUserDetails.name){
           userHashMap[Constants.NAME] =binding.etName.text.toString()

       }
       if (binding.etMobile.text.toString()!=mUserDetails.mobile.toString()){
           userHashMap[Constants.MOBILE] =binding.etMobile.text.toString().toLong()
       }

       FireStoreClass().uptadeUserProfile(this@MyProfilActivity,userHashMap)

   }
    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mSelectedImageUri!=null){
            val sREf:StorageReference=FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "."+ Constants.getFileExtension(this@MyProfilActivity,mSelectedImageUri!!))

            sREf.putFile(mSelectedImageUri!!).addOnSuccessListener { task ->
                Log.i("Firabase image url",task.metadata!!.reference!!.downloadUrl.toString() )
                task.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->Log.i("indirilen image url",uri.toString() )
                    mProfilImageURL=uri.toString()

                    updataUserProfileData()

                }
            }.addOnFailureListener{
                uyari->Toast.makeText(this@MyProfilActivity,uyari.message,Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }


    fun profileUpdateSuccess(){
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

}