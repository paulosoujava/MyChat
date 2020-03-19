package com.paulo.mychat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.item_user_message.view.*

class MessagesActivity : AppCompatActivity() {

    private lateinit var mAdapter: GroupAdapter<ViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        verifyAuthentication()

        mAdapter = GroupAdapter()
        list_messages.adapter = mAdapter

        fetchLastMessage()


    }

    private fun fetchLastMessage() {
        val uid = FirebaseAuth.getInstance().uid.toString()
        FirebaseFirestore.getInstance().collection("/last-messages")
            .document(uid)
            .collection("contacts")
            .addSnapshotListener {
                    querySnapshot, firebaseFirestoreException ->
                val changes = querySnapshot?.documentChanges
                changes?.let {
                    for (doc in it) {
                        when (doc.type) {
                            DocumentChange.Type.ADDED -> {
                                val contact =
                                    doc.document.toObject(Contact::class.java)
                                mAdapter.add(ContactItem(contact))
                            }
                        }
                    }
                }
            }
    }

    private inner class ContactItem(private val mContact: Contact) : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.item_user_message
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.txt_last_message.text = mContact.lastMessage
            viewHolder.itemView.txt_username.text = mContact.username
            Picasso.get()
                .load(mContact.photoUrl)
                .into(viewHolder.itemView.img_photo)
        }
    }

    private fun verifyAuthentication() {
        if (FirebaseAuth.getInstance().uid == null) {
            val intent = Intent(this@MessagesActivity, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.message_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                verifyAuthentication()
            }
            R.id.contacts -> {
                val intent = Intent(this@MessagesActivity, ContactsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
