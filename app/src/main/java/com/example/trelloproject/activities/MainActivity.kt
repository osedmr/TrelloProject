package com.example.trelloproject.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloproject.R
import com.example.trelloproject.adapters.BoardItemAdapter
import com.example.trelloproject.databinding.ActivityMainBinding
import com.example.trelloproject.databinding.NavHeaderMainBinding
import com.example.trelloproject.firebase.FireStoreClass
import com.example.trelloproject.models.Board
import com.example.trelloproject.models.User
import com.example.trelloproject.utils.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity(),NavigationView.OnNavigationItemSelectedListener{
    private lateinit var binding: ActivityMainBinding
    private lateinit var mUserName:String




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setActionBar()
        binding.navView.setNavigationItemSelectedListener(this@MainActivity)
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().loadUserData(this@MainActivity,true)
        val fabButton=findViewById<FloatingActionButton>(R.id.fab_create_board)
        fabButton.setOnClickListener {

            val intent=Intent(this@MainActivity,CreateBoardActivity::class.java)

            intent.putExtra(Constants.NAME,mUserName)

            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }

    }

    fun ekleBoardListe(boardsList:ArrayList<Board>){
        hideProgressDialog()
        if (boardsList.size>0){

            findViewById<RecyclerView>(R.id.rv_boards_list).visibility= View.VISIBLE
            findViewById<TextView>(R.id.tv_no_boards_available).visibility=View.GONE

            findViewById<RecyclerView>(R.id.rv_boards_list).layoutManager=LinearLayoutManager(this@MainActivity)
            findViewById<RecyclerView>(R.id.rv_boards_list).setHasFixedSize(true)

            val adapter=BoardItemAdapter(this@MainActivity,boardsList)

            findViewById<RecyclerView>(R.id.rv_boards_list).adapter=adapter

            adapter.setOnClickListener(object :BoardItemAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent=Intent(this@MainActivity,TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.documentId)
                    startActivity(intent)
                }
            })
        }else{
            findViewById<RecyclerView>(R.id.rv_boards_list).visibility= View.GONE
            findViewById<TextView>(R.id.tv_no_boards_available).visibility=View.VISIBLE
        }
    }

    private fun setActionBar(){
        val toolbarMain=findViewById<Toolbar>(R.id.toolbar_main)
        setSupportActionBar(toolbarMain)
        toolbarMain.setNavigationIcon(R.drawable.open_menu)

        toolbarMain.setNavigationOnClickListener {  openMenu() }


    }
    private fun openMenu(){
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }
    fun updateNavigationUserDetails(user: User,readBoardsList:Boolean) {
        hideProgressDialog()
        mUserName=user.name

        val viewHeader = binding?.navView?.getHeaderView(0)
        val headerBinding = viewHeader?.let { NavHeaderMainBinding.bind(it) }
        headerBinding?.navUserImage?.let {
            Glide
                .with(this)
                .load(user.image) // URL of the image
                .centerCrop() // Scale type of the image.
                .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                .into(headerBinding.navUserImage)
        }
        headerBinding?.tvUsername?.text = user.name

        if (readBoardsList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardsList(this@MainActivity)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode== Activity.RESULT_OK && requestCode== MY_PROFILE){
            FireStoreClass().loadUserData(this@MainActivity)
        }else if (resultCode== Activity.RESULT_OK && requestCode== CREATE_BOARD_REQUEST_CODE){
            FireStoreClass().getBoardsList(this@MainActivity)

        }
        else{
            Log.e("fotograf yükleme hatası","image_Error")
        }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        //açılan menu seçeneklerine tıklandıgında oluşacak etkileri yaz
        when(item.itemId){
            R.id.nav_my_profil->{
                startActivityForResult(Intent(this@MainActivity,MyProfilActivity::class.java),
                    MY_PROFILE)
            }
            R.id.nav_sign_out->{
                FirebaseAuth.getInstance().signOut()
                val intent=Intent(this@MainActivity,IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

                startActivity(intent)
                 finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    companion object{
        const val MY_PROFILE : Int=11
        const val CREATE_BOARD_REQUEST_CODE:Int=12
    }
}