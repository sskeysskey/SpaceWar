package com.example.spacewar

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.content.res.AppCompatResources
import kotlin.math.sqrt
import kotlin.random.Random
import java.util.Timer
import java.util.TimerTask

class EnemyManager(private val context: Context, private val width: Int, private val height: Int) {
    val enemies = mutableListOf<Enemy>()
    var waveInProgress = false

    fun createEnemy(enemyType: Int): Enemy {
        val enemyResourceId = when (enemyType) {
            1 -> R.drawable.enemy1
            2 -> R.drawable.enemy2
            3 -> R.drawable.enemy3
            4 -> R.drawable.enemy4
            else -> R.drawable.enemy1
        }
        val enemyHealth = when (enemyType) {
            1 -> 8
            2 -> 9
            3 -> 10
            4 -> 10
            else -> 10
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
        return Enemy(context, enemyBitmap, x, y, enemyHealth, enemyType)
    }

    fun launchEnemyWave() {
        val enemyTypes = listOf(1, 2, 3, 4, 5)
        val delay = 500L

        for (i in 0 until 2) {
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

class Enemy(private val context: Context, val bitmap: Bitmap, var x: Float, var y: Float, var health: Int, private val type: Int) {
    private val speed = 4
    private val horizontalSpeed = 2
    val enemybullets = mutableListOf<EnemyBullet>()

    fun getEnemyBullets(): List<EnemyBullet> {
        return enemybullets.toList()
    }

    val boundingBox: RectF
        get() = RectF(x, y, x + bitmap.width, y + bitmap.height)

    fun update(playerX: Float, playerY: Float) {
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
        enemybullets.forEach { it.update() }

        // 敌机有一定的几率发射新的子弹
        if (Random.nextInt(100) < 0.005) {
            enemybullets.add(createEnemyBullet(playerX, playerY))
        }
    }

    // 创建新的子弹
    private fun createEnemyBullet(playerX: Float, playerY: Float): EnemyBullet {
        var enemybulletBitmap = (AppCompatResources.getDrawable(context, R.drawable.bulletenemy) as BitmapDrawable).bitmap
        enemybulletBitmap = Bitmap.createScaledBitmap(enemybulletBitmap, enemybulletBitmap.width / 2, enemybulletBitmap.height / 2, true)

        val enemybulletX = x + bitmap.width / 2f - enemybulletBitmap.width / 2f
        val enemybulletY = y + bitmap.height
        return EnemyBullet(enemybulletBitmap, enemybulletX, enemybulletY, playerX, playerY)
    }

    fun drawEnemyBullet(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x, y, null)
        enemybullets.forEach { it.draw(canvas) }
    }
}

class EnemyBullet(val bitmap: Bitmap, var x: Float, var y: Float, targetX: Float, targetY: Float) {
    private val speed = 6f
    private var deltaX = 0f
    private var deltaY = 0f

    val boundingBox: RectF
        get() = RectF(x, y, x + bitmap.width, y + bitmap.height)

    init {
        val diffX = targetX - x
        val diffY = targetY - y
        val length = sqrt(diffX * diffX + diffY * diffY)

        deltaX = diffX / length * speed
        deltaY = diffY / length * speed
    }

    fun update() {
        x += deltaX
        y += deltaY
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x, y, null)
    }
}
