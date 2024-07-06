package com.example.trelloproject.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.trelloproject.activities.CardDetailsActivity
import com.example.trelloproject.activities.CreateBoardActivity
import com.example.trelloproject.activities.MainActivity
import com.example.trelloproject.activities.MembersActivity
import com.example.trelloproject.activities.MyProfilActivity
import com.example.trelloproject.activities.SingInActivity
import com.example.trelloproject.activities.SingUpActivity
import com.example.trelloproject.activities.TaskListActivity
import com.example.trelloproject.models.Board
import com.example.trelloproject.models.User
import com.example.trelloproject.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions


class FireStoreClass {

    private val mFireStore=FirebaseFirestore.getInstance()

    fun registerUser(activity:SingUpActivity,userInfo: User){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId()).set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            } .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }

    fun getBoardDetails(activity: TaskListActivity,documentId:String){
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener {document->
                Log.i(activity.javaClass.simpleName,document.toString())

              // pano ayarlarını görevlerini yapacagımız yer
                val board=document.toObject(Board::class.java)!!
                board.documentId=document.id

                activity.boardDetails(board)
            }.addOnFailureListener {e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Yükleme hatası",e)
            }

    }

    fun createBoard(activity:CreateBoardActivity,board: Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName,"Şablon eklendi")
                Toast.makeText(activity,"Şablon eklendi",Toast.LENGTH_LONG).show()
                activity.boardCreateSuccessfully()
            }.addOnFailureListener {a->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Yükleme şablon hatası",a)
                Toast.makeText(activity,"şablon yükleme hatası",Toast.LENGTH_LONG).show()

            }

    }

    fun getBoardsList(activity:MainActivity){
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserId())
            .get()
            .addOnSuccessListener {document->
                Log.i(activity.javaClass.simpleName,document.documents.toString())
                val boardList:ArrayList<Board> =ArrayList()
                for (document in document.documents){
                    val board=document.toObject(Board::class.java)!!
                    board.documentId=document.id
                    boardList.add(board)
                }
                activity.ekleBoardListe(boardList)
            }.addOnFailureListener {e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Yükleme hatası",e)
            }

    }
    fun addUpdateTaskList(activity:Activity,board:Board){

        val taskListHashMap=HashMap<String,Any>()
        taskListHashMap[Constants.TASK_LIST]=board.taskList

         mFireStore.collection(Constants.BOARDS)
             .document(board.documentId)
             .update(taskListHashMap)
             .addOnSuccessListener {
                 Log.e(activity.javaClass.simpleName,"Görev Listesi eklendi")

                 if (activity is TaskListActivity)
                     activity.addUpdateTaskListSuccess()
                 else if (activity is CardDetailsActivity)
                     activity.addUpdateTaskListSuccess()
             }.addOnFailureListener {
                 exception->
                 if (activity is TaskListActivity)
                 activity.hideProgressDialog()
                 else if (activity is CardDetailsActivity)
                     activity.hideProgressDialog()
                 Log.e(activity.javaClass.simpleName,"Yükleme hatası :)",exception)

             }

    }

    fun uptadeUserProfile(activity: MyProfilActivity,userHashMap: HashMap<String,Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId()).update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Profil güncellendi")
                Toast.makeText(activity,"Profil fotoğrafı güncellenmiştir",Toast.LENGTH_LONG).show()
                activity.profileUpdateSuccess()
            }.addOnFailureListener {e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Yükleme hatası",e)
                Toast.makeText(activity,"Profil güncelleme hatası",Toast.LENGTH_LONG).show()
            }
    }
    fun loadUserData(activity:Activity,readBoardsList:Boolean=false){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId()).get()
            .addOnSuccessListener {document->

                val loggedInUser=document.toObject(User::class.java)!!
                when (activity) {
                    is SingInActivity -> {
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser,readBoardsList)
                    }
                    is MyProfilActivity ->{
                       activity.setUserDataInUI(loggedInUser)
                    }
                    // END
                }


            }.addOnFailureListener {
                e->
                when(activity){
                    is SingInActivity ->{
                            activity.hideProgressDialog()
                    }
                    is MainActivity->{
                            activity.hideProgressDialog()
                    }
                }
                Log.e("SignInUser","Hata mesajı",e)
            }

    }

    fun getCurrentUserId():String{

        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId=""
        if (currentUser != null) {
            currentUserId=currentUser.uid
        }
         return currentUserId
    }
    fun getAssignedMembersListDetails(activity: Activity,assignedTo:ArrayList<String>){
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID,assignedTo)
            .get()
            .addOnSuccessListener {
                document->Log.e(activity.javaClass.simpleName,document.documents.toString())
                val userList:ArrayList<User> = ArrayList()

                for(i in document.documents){
                    val user=i.toObject(User::class.java)!!
                    userList.add(user)
                }
                if (activity is MembersActivity)
                activity.setupMembersList(userList)
                else if (activity is TaskListActivity)
                    activity.boardMembersDetailsList(userList)
            }.addOnFailureListener {
                    exception->
                if (activity is MembersActivity)
                    activity.hideProgressDialog()
                else if (activity is TaskListActivity)
                    activity.hideProgressDialog()

                Log.e(activity.javaClass.simpleName,"Yükleme hatası :)",exception)

            }
    }

    fun getMemberDetails(activity: MembersActivity,email:String){
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL,email)
            .get()
            .addOnSuccessListener {
                document->
                if (document.documents.size>0){
                    val user=document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }else{
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("Kullanıcı Bulunamadı, LÜtfen bilgileri kontrol ediniz")
                }
            }.addOnFailureListener {
                e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Hata mesajı" ,e)}
    }
    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {

        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Güncellendi")
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Güncellemede bir hata oluştu", e)
            }
    }
}