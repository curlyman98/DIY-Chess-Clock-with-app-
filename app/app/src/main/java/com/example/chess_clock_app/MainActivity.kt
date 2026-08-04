package com.example.chess_clock_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

import android.widget.ImageButton


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        val menuButton = findViewById<ImageButton>(R.id.menuButton)

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }
}