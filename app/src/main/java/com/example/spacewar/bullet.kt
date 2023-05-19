package com.example.spacewar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.content.res.AppCompatResources
import java.util.Timer
import java.util.TimerTask

class Bullet(val bitmap: Bitmap, private var x: Float, var y: Float) {
    private val speed = 20f // 子弹发射的速度
    private val width = bitmap.width
    private val height = bitmap.height

    val boundingBox: RectF
        get() = RectF(x, y, x + width, y + height)

    fun update() {
        y -= speed
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x, y, null)
    }
}

class BulletManager(context: Context, private val gameView: GameView) {
    val bullets = mutableListOf<Bullet>()
    private val bulletTimer = Timer()

    private val bulletBitmap: Bitmap = run {
        val originalBulletBitmap = (AppCompatResources.getDrawable(context, R.drawable.bullet) as BitmapDrawable).bitmap
        val scaleFactor = 0.3f // 子弹的尺寸和大小
        val newWidth = (originalBulletBitmap.width * scaleFactor).toInt()
        val newHeight = (originalBulletBitmap.height * scaleFactor).toInt()
        Bitmap.createScaledBitmap(originalBulletBitmap, newWidth, newHeight, false)
    }

    init {
        bulletTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // 发射新的子弹
                gameView.player?.let {
                    when (it.powerUpLevel) {
                        0 -> {
                            // 如果火力等级为 0，只发射一颗子弹
                            val bulletX = it.x + it.bitmap.width / 2f - bulletBitmap.width / 2f
                            val bulletY = it.y - bulletBitmap.height
                            val bullet = Bullet(bulletBitmap, bulletX, bulletY)
                            synchronized(bullets) {
                                bullets.add(bullet)
                            }
                        }
                        else -> {
                            // 如果火力等级大于 0，发射两颗子弹
                            // 修改这里，让子弹的发射位置更加靠近飞船的中心
                            val bulletX1 = it.x + it.bitmap.width * 0.25f - bulletBitmap.width / 2f
                            val bulletX2 = it.x + it.bitmap.width * 0.75f - bulletBitmap.width / 2f
                            val bulletY = it.y - bulletBitmap.height
                            val bullet1 = Bullet(bulletBitmap, bulletX1, bulletY)
                            val bullet2 = Bullet(bulletBitmap, bulletX2, bulletY)
                            synchronized(bullets) {
                                bullets.add(bullet1)
                                bullets.add(bullet2)
                            }
                        }
                    }
                }
            }
        }, 0, 200) // 每 多少 毫秒创建一颗子弹
    }

    fun updateBullets() {
        synchronized(bullets) {
            val iterator = bullets.iterator()
            while (iterator.hasNext()) {
                val bullet = iterator.next()
                bullet.update()
                // 如果子弹飞出屏幕，从列表中移除
                if (bullet.y + bullet.bitmap.height < 0) {
                    iterator.remove()
                }
            }
        }
    }

    fun drawBullets(canvas: Canvas) {
        synchronized(bullets) {
            bullets.forEach { it.draw(canvas) }
        }
    }

    fun stop() {
        bulletTimer.cancel()
        synchronized(bullets) {
            bullets.clear()
        }
    }
}
