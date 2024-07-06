package com.example.trelloproject.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloproject.R
import com.example.trelloproject.adapters.TaskListItemAdapter
import com.example.trelloproject.databinding.ActivityMainBinding
import com.example.trelloproject.databinding.ActivityTaskListBinding
import com.example.trelloproject.firebase.FireStoreClass
import com.example.trelloproject.models.Board
import com.example.trelloproject.models.Card
import com.example.trelloproject.models.Task
import com.example.trelloproject.models.User
import com.example.trelloproject.utils.Constants

class TaskListActivity : BaseActivity() {
    private lateinit var binding: ActivityTaskListBinding

    private lateinit var mBoardDetails:Board
    private lateinit var mBoardDocumentId: String
     lateinit var mAssignedMemberDetailList: ArrayList<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            mBoardDocumentId= intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getBoardDetails(this@TaskListActivity,mBoardDocumentId)
    }
    //onResume ile de olabilir
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode== Activity.RESULT_OK && requestCode == MEMBERS_REQUEST_CODE || requestCode == CARD_DETAILS_REQUEST_CODE){
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardDetails(this@TaskListActivity,mBoardDocumentId)
        }else{
            Log.e("iptal","iptal")
        }
    }
    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent=Intent(this@TaskListActivity, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST,mAssignedMemberDetailList)


        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members ->{
                val intent=Intent(this@TaskListActivity,MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setActionBar(){

        setSupportActionBar(binding.toolbarTaskListActivity)
        val actionBar=supportActionBar
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.back_icon)
            actionBar.title=mBoardDetails.name
        }
        binding.toolbarTaskListActivity.setNavigationOnClickListener { onBackPressed() }

    }

    fun boardDetails(board:Board){
        mBoardDetails=board
        hideProgressDialog()
        setActionBar()


        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getAssignedMembersListDetails(this@TaskListActivity,mBoardDetails.assignedTo)
    }


    fun createTaskList(taskListName:String){
        val task=Task(taskListName,FireStoreClass().getCurrentUserId())
        mBoardDetails.taskList.add(0,task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity,mBoardDetails)
    }

    fun updateTaskList(position:Int,listName:String,model:Task){
        val task=Task(listName,model.createBy)
        mBoardDetails.taskList[position]=task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity,mBoardDetails)

    }
    fun deleteTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity,mBoardDetails)
    }
    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getBoardDetails(this@TaskListActivity,mBoardDetails.documentId)
    }

    fun addCardToTaskList(position: Int,cardName:String){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        val cardAssignedUserList:ArrayList<String> = ArrayList()
        cardAssignedUserList.add(FireStoreClass().getCurrentUserId())
        val card=Card(cardName,FireStoreClass().getCurrentUserId(),cardAssignedUserList)
        val cardsList=mBoardDetails.taskList[position].cards
        cardsList.add(card)

        val task=Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createBy,
            cardsList
        )

        mBoardDetails.taskList[position]=task
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity,mBoardDetails)

    }
    fun boardMembersDetailsList(list:ArrayList<User>){
        mAssignedMemberDetailList=list
        hideProgressDialog()

        val addTaskList=Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)
        binding.rvTaskList.layoutManager=LinearLayoutManager(this@TaskListActivity,LinearLayoutManager.HORIZONTAL,false)

        binding.rvTaskList.setHasFixedSize(true)
        val adapter=TaskListItemAdapter(this@TaskListActivity, mBoardDetails.taskList)
        binding.rvTaskList.adapter=adapter
    }
    companion object{
        const val MEMBERS_REQUEST_CODE:Int=13
        const val CARD_DETAILS_REQUEST_CODE:Int=14
    }
}