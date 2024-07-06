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
import com.example.trelloproject.databinding.ActivityCreateBoardBinding
import com.example.trelloproject.firebase.FireStoreClass
import com.example.trelloproject.models.Board
import com.example.trelloproject.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class CreateBoardActivity : BaseActivity() {
    private lateinit var binding: ActivityCreateBoardBinding
    private var mSelectedImageUri_Create: Uri?=null
    private lateinit var mUserName:String
    private var mBoardImageURL:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setActionBar()

        if (intent.hasExtra(Constants.NAME)){
            mUserName= intent.getStringExtra(Constants.NAME).toString()
        }

        binding.ivBoardImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this@CreateBoardActivity)
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE)
            }
        }

        binding.btnCreate.setOnClickListener {
            if (mSelectedImageUri_Create!=null){
                uploadBoardImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }

    }
    private fun createBoard(){
        val assignedUSersArrayList:ArrayList<String> =ArrayList()
        assignedUSersArrayList.add(getCurrentUserID())

        var board= Board(
            binding.etBoardName.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUSersArrayList
        )
        FireStoreClass().createBoard(this@CreateBoardActivity,board)
    }

    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        val sREf: StorageReference = FirebaseStorage.getInstance().reference.child(
            "Board_IMAGE" + System.currentTimeMillis() + "."+ Constants.getFileExtension(this@CreateBoardActivity,mSelectedImageUri_Create!!))

        sREf.putFile(mSelectedImageUri_Create!!).addOnSuccessListener { task ->
            Log.i("Firabase board image url",task.metadata!!.reference!!.downloadUrl.toString() )
            task.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                Log.i("indirilen image url",uri.toString() )
                mBoardImageURL=uri.toString()

                createBoard()

            }
        }.addOnFailureListener{
                uyari->Toast.makeText(this@CreateBoardActivity,uyari.message,Toast.LENGTH_LONG).show()
            hideProgressDialog()
        }
    }

    fun boardCreateSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }


    private fun setActionBar(){

        setSupportActionBar(binding.toolbarCreateBoardActivity)
        val actionBar=supportActionBar
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.back_icon)
            actionBar.title=resources.getString(R.string.create_board_title)
        }
        binding.toolbarCreateBoardActivity.setOnClickListener {
            onBackPressed()
        }

    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== Constants.READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                //izin varsa direk geçiş yapıcak
                Constants.showImageChooser(this@CreateBoardActivity)

            }
        }else{
            Toast.makeText(this@CreateBoardActivity,"Erişim izni bulunmamaktadır", Toast.LENGTH_LONG).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode== Activity.RESULT_OK && requestCode== Constants.PICK_IMAGE_REQUEST_CODE && data!!.data!=null){
            mSelectedImageUri_Create=data.data

            try {
                Glide
                    .with(this@CreateBoardActivity)
                    .load(mSelectedImageUri_Create) // URL of the image
                    .centerCrop() // Scale type of the image.
                    .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                    .into(binding.ivBoardImage)
            }catch (e: IOException){
                e.printStackTrace()
            }

        }
    }
}