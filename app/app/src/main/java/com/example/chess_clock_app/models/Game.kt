package com.example.chess_clock_app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Game(
    val name: String = "",
    val pgn: String = "",
    val iD: String = ""
) : Parcelable