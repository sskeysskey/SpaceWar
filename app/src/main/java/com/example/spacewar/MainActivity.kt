package com.example.spacewar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameView = GameView(this)
        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        gameView.requestFocus()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameView.stop()
    }
}
