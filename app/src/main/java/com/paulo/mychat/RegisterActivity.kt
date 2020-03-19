package com.paulo.mychat

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.edit_email
import kotlinx.android.synthetic.main.activity_login.edit_password
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private var mSelectedUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        btn_register.setOnClickListener {
            createUser()
        }
        btn_select_photo.setOnClickListener {
            selectPhoto()
        }
    }

    private fun selectPhoto() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0) // 3
    }

    private fun createUser() {
        val email = edit_email.text.toString()
        val password = edit_password.text.toString()
        if (email.isEmpty() || password.isEmpty()) { // 2 Toast.makeText(this,
            Toast.makeText(this, "email e senha devem ser informados", Toast.LENGTH_LONG).show()
            return
        }
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.i("Teste", "UserID é ${it.result?.user?.uid}")
                    saveUserInFirebase()
                }
            }.addOnFailureListener {
                Log.e("Teste", it.message, it)
            }
    }

    private fun saveUserInFirebase() {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/${filename}")
        mSelectedUri?.let {
            ref.putFile(it)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        Log.i("Teste", it.toString())
                        val url = it.toString() // 1
                        val name = edit_name.text.toString()
                        val uid = FirebaseAuth.getInstance().uid!!
                        val user = User(uid, name, url) // 4
                        FirebaseFirestore.getInstance().collection("users")
                            .document(uid)
                             .set(user)
                            .addOnSuccessListener {  // 7
                                Log.i("Teste", it.toString())
                                val intent = Intent(this@RegisterActivity, MessagesActivity ::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }
                            .addOnFailureListener {  // 8
                                Log.e("Teste", it.message, it)
                            }
                    }
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            mSelectedUri = data?.data
            Log.i("Teste", mSelectedUri.toString())
            val bitmap = MediaStore.Images.Media.getBitmap(
                contentResolver,
                mSelectedUri
            )
            img_photo.setImageBitmap(bitmap)
            btn_select_photo.alpha = 0f
        }
    }


}
