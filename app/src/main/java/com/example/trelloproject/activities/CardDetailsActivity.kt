package com.example.trelloproject.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.trelloproject.R
import com.example.trelloproject.adapters.CardMemberListItemsAdapter
import com.example.trelloproject.databinding.ActivityCardDetailsBinding
import com.example.trelloproject.databinding.ActivityMyProfilBinding
import com.example.trelloproject.dialogs.LabelColorListDialog
import com.example.trelloproject.dialogs.MembersListDialog
import com.example.trelloproject.firebase.FireStoreClass
import com.example.trelloproject.models.Board
import com.example.trelloproject.models.Card
import com.example.trelloproject.models.SelectedMembers
import com.example.trelloproject.models.Task
import com.example.trelloproject.models.User
import com.example.trelloproject.utils.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CardDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityCardDetailsBinding
    private lateinit var mBoardDetails: Board
    private var mTaskListPosition = -1
    private var mCardListPosition = -1
    private var mSelectedColor = ""
    private lateinit var mMembersDetailList: ArrayList<User>
    private var mSelectedDueDateMilliSeconds :Long =0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        getIntentData()
        setActionBar()

        binding.etNameCardDetails.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name)
        binding.etNameCardDetails.setSelection(binding.etNameCardDetails.text.toString().length)

        mSelectedColor=mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].labelColor
        if (mSelectedColor.isNotEmpty()){
            setColor()
        }
        binding.btnUpdateCardDetails.setOnClickListener {
            if (binding.etNameCardDetails.text.toString().isNotEmpty())
                updateCardDetails()
            else {
                Toast.makeText(this@CardDetailsActivity, "Kart adını giriniz", Toast.LENGTH_LONG)
                    .show()
            }
        }
        binding.tvSelectLabelColor.setOnClickListener {
            labelColorListDialog()
        }
        binding.tvSelectMembers.setOnClickListener {
            membersListDialog()
        }
        setupSelectedMembersList()

        mSelectedDueDateMilliSeconds=mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].dueDate

        if (mSelectedDueDateMilliSeconds > 0){
            val simpleDateFormat=SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH)
            val selectedDate= simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            binding.tvSelectDueDate.text=selectedDate
        }

        binding.tvSelectDueDate.setOnClickListener {
            showDataPicker()
        }
    }

    private fun setActionBar() {

        setSupportActionBar(binding.toolbarCardDetailsActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.back_icon)
            actionBar.title =
                mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name
        }
        binding.toolbarCardDetailsActivity.setNavigationOnClickListener { onBackPressed() }
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun colorList(): ArrayList<String> {
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")
        return colorsList
    }

    private fun setColor() {
        binding.tvSelectLabelColor.text = ""
        binding.tvSelectLabelColor.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteCard(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            mCardListPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }

    }
    private fun membersListDialog() {

        // Here we get the updated assigned members list
        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo

        if (cardAssignedMembersList.size > 0) {
            // Here we got the details of assigned members list from the global members list which is passed from the Task List screen.
            for (i in mMembersDetailList.indices) {
                for (j in cardAssignedMembersList) {
                    if (mMembersDetailList[i].id == j) {
                        mMembersDetailList[i].selected = true
                    }
                }
            }
        } else {
            for (i in mMembersDetailList.indices) {
                mMembersDetailList[i].selected = false
            }
        }

        val listDialog = object : MembersListDialog(
            this@CardDetailsActivity,
            mMembersDetailList,
            resources.getString(R.string.str_select_member)
        ) {
            override fun onItemSelected(user: User, action: String) {

                if (action == Constants.SELECT) {
                    if (!mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.contains(
                            user.id
                        )
                    ) {
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.add(
                            user.id
                        )
                    }
                } else {
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.remove(
                        user.id
                    )

                    for (i in mMembersDetailList.indices) {
                        if (mMembersDetailList[i].id == user.id) {
                            mMembersDetailList[i].selected = false
                        }
                    }
                }

                setupSelectedMembersList()
            }
        }
        listDialog.show()
    }

    private fun updateCardDetails() {
        val card = Card(
            binding.etNameCardDetails.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliSeconds
        )

        val taskList: ArrayList<Task> =mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)
        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition] = card
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
    }

    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle(resources.getString(R.string.alert))

        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_card,
                cardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, which ->
            dialogInterface.dismiss()

            deleteCard()

        }

        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()

        alertDialog.setCancelable(false)
        alertDialog.show()
    }


    private fun deleteCard() {

        val cardsList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        cardsList.removeAt(mCardListPosition)
        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)
        taskList[mTaskListPosition].cards = cardsList


        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
    }

    private fun labelColorListDialog() {
        val colorList: ArrayList<String> = colorList()
        val listDialog = object :
            LabelColorListDialog(this@CardDetailsActivity,
            colorList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor
                ){
            override fun onItemSelected(color: String) {
                mSelectedColor=color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun setupSelectedMembersList() {

        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo
        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for (i in mMembersDetailList.indices) {
            for (j in cardAssignedMembersList) {
                if (mMembersDetailList[i].id == j) {
                    val selectedMember = SelectedMembers(
                        mMembersDetailList[i].id,
                        mMembersDetailList[i].image
                    )

                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0) {

            selectedMembersList.add(SelectedMembers("", ""))

           binding.tvSelectMembers.visibility = View.GONE
            binding.rvSelectedMembersList.visibility = View.VISIBLE

            binding.rvSelectedMembersList.layoutManager = GridLayoutManager(this@CardDetailsActivity, 6)
            val adapter = CardMemberListItemsAdapter(this@CardDetailsActivity, selectedMembersList,true)
            binding.rvSelectedMembersList.adapter = adapter
            adapter.setOnClickListener(object :
                CardMemberListItemsAdapter.OnClickListener {
                override fun onClick() {
                    membersListDialog()
                }
            })
        } else {
            binding.tvSelectMembers.visibility = View.VISIBLE
            binding.rvSelectedMembersList.visibility = View.GONE
        }
    }
    private fun showDataPicker() {

        val c = Calendar.getInstance()
        val year =
            c.get(Calendar.YEAR) // Returns the value of the given calendar field. This indicates YEAR
        val month = c.get(Calendar.MONTH) // This indicates the Month
        val day = c.get(Calendar.DAY_OF_MONTH) // This indicates the Day


        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"

                val sMonthOfYear =
                    if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"

                binding.tvSelectDueDate.text = selectedDate


                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)


                val theDate = sdf.parse(selectedDate)


                mSelectedDueDateMilliSeconds = theDate!!.time
            },
            year,
            month,
            day
        )
        dpd.show()
    }

}