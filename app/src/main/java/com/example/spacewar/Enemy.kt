package com.example.spacewar

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.content.res.AppCompatResources
import java.util.Timer
import java.util.TimerTask

class EnemyManager(private val context: Context, private val width: Int, private val height: Int, private val bullets: MutableList<Bullet>) {
    val enemies = mutableListOf<Enemy>()
    var waveInProgress = false

    // 将 createEnemy 方法从 AsteroidManager 移动到这里
    fun createEnemy(enemyType: Int): Enemy {
        val enemyResourceId = when (enemyType) {
            1 -> R.drawable.enemy1
            2 -> R.drawable.enemy2
            3 -> R.drawable.enemy3
            4 -> R.drawable.enemy4
            else -> R.drawable.enemy1
        }
        val enemyHealth = when (enemyType) {
            1 -> 10
            2 -> 11
            3 -> 12
            4 -> 13
            else -> 13
        }
        val enemyBitmap = (AppCompatResources.getDrawable(context, enemyResourceId) as BitmapDrawable).bitmap
        val x = when (enemyType) {
            2 -> -enemyBitmap.width.toFloat()
            4 -> width.toFloat()
            5 -> width.toFloat()
            else -> (0..width - enemyBitmap.width).random().toFloat()
        }
        val y = when (enemyType) {
            2, 4 -> height / 4f - enemyBitmap.height / 2f
            5 -> -enemyBitmap.height.toFloat()
            else -> -enemyBitmap.height.toFloat()
        }
        return Enemy(enemyBitmap, x, y, enemyHealth, enemyType)
    }

    fun launchEnemyWave() {
        val enemyTypes = listOf(1, 2, 3, 4, 5)
        val delay = 500L

        for (i in 0 until 4) {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    synchronized(enemies) {
                        val enemyType = enemyTypes.random()
                        enemies.add(createEnemy(enemyType))
                    }
                }
            }, delay * i)
        }
        waveInProgress = false
    }
}

class Enemy(
    val bitmap: Bitmap,
    var x: Float,
    var y: Float,
    var health: Int,
    private val type: Int
) {
    private val speed = 4
    private val horizontalSpeed = 2

    val boundingBox: RectF
        get() = RectF(x, y, x + bitmap.width, y + bitmap.height)

    fun update() {
        when (type) {
            1 -> y += speed
            2 -> {
                x += horizontalSpeed
                y += speed / 2f
            }
            3 -> {
                x += speed / 2f
                y += speed
            }
            4 -> {
                x -= horizontalSpeed
                y += speed / 2f
            }
            5 -> {
                x -= speed / 2f
                y += speed
            }
        }
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x, y, null)
    }
}