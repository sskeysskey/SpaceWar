package com.example.spacewar

import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: GameView
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameView = GameView(this)
        setContentView(gameView)

        mediaPlayer = MediaPlayer.create(this, R.raw.background)
        mediaPlayer.isLooping = true // 设置为循环播放
        mediaPlayer.start() // 开始播放
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.pause() // 暂停音乐
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.stop() // 停止音乐
    }
    override fun onResume() {
        super.onResume()
        gameView.requestFocus()
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start() // 重新开始播放
        }
    }

    override fun onStart() {
        super.onStart()
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start() // 重新开始播放
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gameView.stop()
        mediaPlayer.release() // 释放MediaPlayer占用的资源
    }
}