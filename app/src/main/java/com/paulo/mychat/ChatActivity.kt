package com.paulo.mychat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.item_from_message.view.*
import kotlinx.android.synthetic.main.item_to_message.*
import kotlinx.android.synthetic.main.item_to_message.view.*

class ChatActivity : AppCompatActivity() {

    private lateinit var mAdapter: GroupAdapter<ViewHolder>
    lateinit var mUser: User
    private var mMe: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val user = intent.extras?.getParcelable<User>(ContactsActivity.USER_KEY)
        Log.i("Teste", "username ${user?.name}")

        mAdapter = GroupAdapter()
        list_chat.adapter = mAdapter

        mUser = intent.extras?.getParcelable<User>(ContactsActivity.USER_KEY)!!
        Log.i("Teste", "username ${mUser.name}")
        supportActionBar?.title = mUser.name
        mAdapter = GroupAdapter()
        list_chat.adapter = mAdapter
        btn_send.setOnClickListener {
            sendMessage()
        }
        FirebaseFirestore.getInstance().collection("/users")
            .document(FirebaseAuth.getInstance().uid.toString()).get()
            .addOnSuccessListener {
                mMe = it.toObject(User::class.java) // 3
                fetchMessages()
            }
    }

    private fun fetchMessages() {
        mMe?.let {
            val fromId = it.uid
            val toId = mUser.uid
            FirebaseFirestore.getInstance().collection("/conversations")
                .document(fromId)
                .collection(toId)
                .orderBy("timestamp", Query.Direction.ASCENDING)  // 5
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    querySnapshot?.documentChanges?.let {
                        for (doc in it) {
                            when (doc.type) {
                                DocumentChange.Type.ADDED -> {
                                    val message = doc.document.toObject(Message::class.java)
                                    mAdapter.add(MessageItem(message))
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun sendMessage() {
        val text = edit_msg.text.toString()
        edit_msg.text = null
        val fromId = FirebaseAuth.getInstance().uid.toString()
        val toId = mUser.uid
        val timestamp = System.currentTimeMillis()
        val message = Message(
            text = text,
            timestamp = timestamp, toId = toId, fromId = fromId
        )
        if (message.text.isNotEmpty()) {
            FirebaseFirestore.getInstance().collection("/conversations")
                .document(fromId)
                .collection(toId)
                .add(message)
                .addOnSuccessListener {
                    Log.i("Teste", it.id)
                }
                .addOnFailureListener {
                    Log.e("Teste", it.message)
                }
            FirebaseFirestore.getInstance().collection("/conversations")
                .document(toId)
                .collection(fromId)
                .add(message)
                .addOnSuccessListener {
                    Log.i("Teste", it.id)
                }
                .addOnFailureListener {
                    Log.e("Teste", it.message)
                }
        }
    }

    private inner class MessageItem(private val mMessage: Message) : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return if (mMessage.fromId == FirebaseAuth.getInstance().uid)
                R.layout.item_from_message
            else R.layout.item_to_message
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            if (mMessage.fromId == FirebaseAuth.getInstance().uid) {
                viewHolder.itemView.txt_msg_from.text = mMessage.text
                Picasso.get().load(mUser.url).into(viewHolder.itemView.img_msg_from)
            } else {
                viewHolder.itemView.txt_msg.text = mMessage.text
                Picasso.get().load(mUser.url).into(viewHolder.itemView.img_msg)
            }
        }
    }
}
