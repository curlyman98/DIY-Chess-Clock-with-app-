package com.example.chess_clock_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

import android.widget.ImageButton
import android.content.Intent
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        val menuButton = findViewById<ImageButton>(R.id.menuButton)

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        // Open the pair menu from the drawer
        val pairingButton =
            findViewById<Button>(R.id.pairingButtonDrawer)

        pairingButton.setOnClickListener {

            val intent =
                Intent(this, PairingActivity::class.java)

            startActivity(intent)
        }


    }
}