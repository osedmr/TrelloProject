package com.example.trelloproject.activities

import android.app.Activity
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloproject.R
import com.example.trelloproject.databinding.ActivityMainBinding
import com.example.trelloproject.databinding.ActivityMembersBinding
import com.example.trelloproject.firebase.FireStoreClass
import com.example.trelloproject.models.Board
import com.example.trelloproject.models.User
import com.example.trelloproject.utils.Constants
import com.projemanag.adapters.MemberListItemsAdapter

class MembersActivity : BaseActivity() {
    private lateinit var binding: ActivityMembersBinding

    private lateinit var mBoardDetails:Board
    private var anyChangesMAade:Boolean=false
    private lateinit var mAssignedMembersList:ArrayList<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setActionBar()

        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails= intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getAssignedMembersListDetails(this@MembersActivity,mBoardDetails.assignedTo)
    }

    fun setupMembersList(list:ArrayList<User>){
        mAssignedMembersList=list
        hideProgressDialog()

        binding.rvMembersList.layoutManager=LinearLayoutManager(this@MembersActivity)
        binding.rvMembersList.setHasFixedSize(true)

        val adapter=MemberListItemsAdapter(this,list)
        binding.rvMembersList.adapter=adapter
    }
    fun memberDetails(user:User){

        mBoardDetails.assignedTo.add(user.id)
        FireStoreClass().assignMemberToBoard(this@MembersActivity,mBoardDetails,user)
    }

    private fun setActionBar(){

        setSupportActionBar(binding.toolbarMembersActivity)
        val actionBar=supportActionBar
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.back_icon)
            actionBar.title=resources.getString(R.string.members)
        }
        binding.toolbarMembersActivity.setNavigationOnClickListener{ onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.action_add_member->{
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun dialogSearchMember(){
        val dialog=Dialog(this@MembersActivity)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener {
            val email=dialog.findViewById<EditText>(R.id.et_email_search_member).text.toString()
            if (email.isNotEmpty()){
                dialog.dismiss()
                //kontrol gerçekleitiricez
                showProgressDialog(resources.getString(R.string.please_wait))
                FireStoreClass().getMemberDetails(this@MembersActivity,email)

            }else{
                Toast.makeText(this@MembersActivity,
                    "Lütfen mail adresi giriniz",Toast.LENGTH_LONG).show()
            }
        }
        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
      }

    override fun onBackPressed() {
        if (anyChangesMAade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    fun memberAssignSuccess(user:User){
        hideProgressDialog()
        mAssignedMembersList.add(user)

        anyChangesMAade=true
        setupMembersList(mAssignedMembersList)
    }
}