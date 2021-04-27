package com.amier.modernloginregister

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_dashboard.*

class DashboardActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        profile_button.setOnClickListener{
            startActivity(Intent(this,ProfileActivity::class.java))
        }

        cert_button.setOnClickListener{
            startActivity(Intent(this,CertificationActivity::class.java))
        }

        search_button.setOnClickListener{
            startActivity(Intent(this,MainActivity::class.java))
        } 
    }
}