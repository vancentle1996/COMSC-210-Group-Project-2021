package com.amier.modernloginregister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    lateinit var email: EditText
    lateinit var password: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)

        btnRegLogin.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
            overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left)
        }

        LoginButton.setOnClickListener {

            saveData()
        }
    }

    private fun saveData() {

        val e = email.text.toString().trim()
        val p = password.text.toString().trim()

        if (p.isEmpty() && e.isEmpty()){
            email.error = "Please enter an email address"
            password.error = "Please enter a password"
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(e,p)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    Toast.makeText(this@LoginActivity, "LOGGED IN SUCCESSFULLY!", Toast.LENGTH_LONG).show()

                    val intent = Intent(  this@LoginActivity, DashboardActivity::class.java )
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra("user_id", FirebaseAuth.getInstance().currentUser!!.uid)
                    intent.putExtra("email_id", e)
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(this@LoginActivity, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }

            }

        val ref = FirebaseDatabase.getInstance().getReference("login_info")
        val idValue = ref.push().key
        val registrationInputs = idValue?.let { LoginClass(it, e, p) }

        if (idValue != null) {
            ref.child(idValue).setValue(registrationInputs).addOnCompleteListener{
                Toast.makeText(applicationContext, "SUCCESSFUL!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
