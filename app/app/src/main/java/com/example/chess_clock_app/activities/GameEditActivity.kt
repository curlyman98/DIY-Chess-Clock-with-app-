package com.example.chess_clock_app.activities

import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chess_clock_app.R
import com.example.chess_clock_app.adapters.GameAdapter
import com.example.chess_clock_app.models.Game
import android.widget.Toast
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

class GameEditActivity : AppCompatActivity() {
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

        override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_game_edit)

            val gameName =
                intent.getStringExtra("GAME_NAME")

            println(gameName)
            val gameNameField =
                findViewById<TextView>(R.id.titleText)

            gameNameField.text = gameName
        }


    }
