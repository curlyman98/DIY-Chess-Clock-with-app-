package com.example.chess_clock_app.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.chess_clock_app.R

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the shared drawer and top bar.
        super.setContentView(R.layout.activity_base)

        drawerLayout = findViewById(R.id.drawerLayout)

        val menuButton =
            findViewById<ImageButton>(R.id.menuButton)

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        setupDrawerNavigation()
    }

    protected fun setActivityContent(
        @LayoutRes layoutResource: Int
    ) {
        val contentContainer =
            findViewById<FrameLayout>(R.id.contentContainer)

        LayoutInflater.from(this).inflate(
            layoutResource,
            contentContainer,
            true
        )
    }

    private fun setupDrawerNavigation() {

        //Navigate to pairing
        val pairingButton =
            findViewById<Button>(R.id.pairingButtonDrawer)

        pairingButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)

            if (this !is PairingActivity) {
                val intent =
                    Intent(this, PairingActivity::class.java)

                startActivity(intent)
            }
        }
        //Navigate to games/sync

        val gamesButton =
            findViewById<Button>(R.id.gamesButtonDrawer)

        gamesButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)

            if (this !is GameActivity) {
                val intent =
                    Intent(this, GameActivity::class.java)

                startActivity(intent)
            }
        }

        val settingsButton =
            findViewById<Button>(R.id.settingsButtonDrawer)

        settingsButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)

            if (this !is SettingsActivity) {
                val intent =
                    Intent(this, SettingsActivity::class.java)

                startActivity(intent)
            }
        }

        // Stored Games and Settings navigation will be added
        // when those Activities exist.
    }
}