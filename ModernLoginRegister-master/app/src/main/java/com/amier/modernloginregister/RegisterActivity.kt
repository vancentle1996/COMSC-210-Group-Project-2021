package com.amier.modernloginregister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    lateinit var name: EditText
    lateinit var age: EditText
    lateinit var zipcode: EditText
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var registerBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        name = findViewById(R.id.name)
        age = findViewById(R.id.age)
        zipcode = findViewById(R.id.zipcode)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        registerBtn = findViewById(R.id.LoginButton)

        btnLogRegister.setOnClickListener {
            onBackPressed()
        }

        // Save data to firestore
        LoginButton.setOnClickListener {
            saveData()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
    }

    //RegistrationClass (val id: String, val inputname: String, val age: String, val zipcode: String, val email: String, val pass: String)
    private fun saveData(){

        val n = name.text.toString().trim()
        val a = age.text.toString().trim()
        val z = zipcode.text.toString().trim()
        val e = email.text.toString().trim()
        val p = password.text.toString().trim()

        if (n.isEmpty() && a.isEmpty() && z.isEmpty() && p.isEmpty() && e.isEmpty()){
            name.error = "Please enter your name"
            age.error = "Please enter your age"
            zipcode.error = "Please enter the zipcode"
            email.error = "Please enter an email address"
            password.error = "Please enter a password"
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(e,p)
            .addOnCompleteListener(
                OnCompleteListener<AuthResult> { task ->

                    if (task.isSuccessful) {

                        val firebaseUser: FirebaseUser = task.result!!.user!!

                        Toast.makeText(this@RegisterActivity, "REGISTERED SUCCESSFULLY!", Toast.LENGTH_LONG).show()

                        val intent = Intent(  this@RegisterActivity, DashboardActivity::class.java )
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.putExtra("user_id", firebaseUser.uid)
                        intent.putExtra("email_id", e)
                        startActivity(intent)
                        finish()
                    } else {

                        Toast.makeText(this@RegisterActivity, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                    }

                }
            )

        val ref = FirebaseDatabase.getInstance().getReference("registration_info")
        val idValue = ref.push().key
        val registrationInputs = idValue?.let { RegistrationClass(it, n, a, z, e, p) }

        if (idValue != null) {
            ref.child(idValue).setValue(registrationInputs).addOnCompleteListener{
                Toast.makeText(applicationContext, "SUCCESSFUL!", Toast.LENGTH_LONG).show()
            }
        }

    }


}
