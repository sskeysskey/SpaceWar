package com.example.spacewar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.media.SoundPool
import androidx.appcompat.content.res.AppCompatResources
import java.util.Timer
import java.util.TimerTask

class Bullet(val bitmap: Bitmap, private var x: Float, var y: Float, private val speedX: Float = 0f) {
    private val speedY = 20f // 子弹发射的速度
    private val width = bitmap.width
    private val height = bitmap.height

    val boundingBox: RectF
        get() = RectF(x, y, x + width, y + height)

    fun update() {
        y -= speedY
        x += speedX // 更新x坐标
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x, y, null)
    }
}

class BulletManager(context: Context, private val gameView: GameView) {
    val bullets = mutableListOf<Bullet>()
    private val bulletTimer = Timer()

    private val soundPool = SoundPool.Builder().setMaxStreams(5).build()
    private val shootSoundId: Int

    private val bulletBitmap: Bitmap = run {
        val originalBulletBitmap = (AppCompatResources.getDrawable(context, R.drawable.bullet) as BitmapDrawable).bitmap
        val scaleFactor = 0.3f // 子弹的尺寸和大小
        val newWidth = (originalBulletBitmap.width * scaleFactor).toInt()
        val newHeight = (originalBulletBitmap.height * scaleFactor).toInt()
        Bitmap.createScaledBitmap(originalBulletBitmap, newWidth, newHeight, false)
    }

    init {
        shootSoundId = soundPool.load(context, R.raw.playershoot, 1)
        bulletTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                gameView.player?.let {
                    val bulletY = it.y - bulletBitmap.height
                    synchronized(bullets) {
                        when (it.powerUpLevel) {
                            0 -> {
                                // 如果火力等级为 0，只发射一颗子弹
                                val bulletX = it.x + it.normalBitmap.width / 2f - bulletBitmap.width / 2f
                                val bullet = Bullet(bulletBitmap, bulletX, bulletY)
                                bullets.add(bullet)
                            }
                            1 -> {
                                // 如果火力等级为 1，发射两颗子弹
                                val bulletX1 = it.x + it.normalBitmap.width * 0.25f - bulletBitmap.width / 2f
                                val bulletX2 = it.x + it.normalBitmap.width * 0.75f - bulletBitmap.width / 2f
                                val bullet1 = Bullet(bulletBitmap, bulletX1, bulletY)
                                val bullet2 = Bullet(bulletBitmap, bulletX2, bulletY)
                                bullets.add(bullet1)
                                bullets.add(bullet2)
                            }
                            2 -> {
                                // 创建中间的子弹
                                val middleBulletX = it.x + it.normalBitmap.width * 0.5f - bulletBitmap.width / 2f
                                bullets.add(Bullet(bulletBitmap, middleBulletX, bulletY))

                                // 创建向左和向右的子弹，它们的x轴速度为负值和正值
                                val leftBulletX = it.x + it.normalBitmap.width * 0.5f - bulletBitmap.width
                                val rightBulletX = it.x + it.normalBitmap.width * 0.5f
                                bullets.add(Bullet(bulletBitmap, leftBulletX, bulletY, speedX = -5f))
                                bullets.add(Bullet(bulletBitmap, rightBulletX, bulletY, speedX = 5f))
                            }
                            3 -> {
                                val leftBulletX = it.x + it.normalBitmap.width * 0.5f - bulletBitmap.width
                                val rightBulletX = it.x + it.normalBitmap.width * 0.5f
                                bullets.add(Bullet(bulletBitmap, leftBulletX, bulletY, speedX = -5f))
                                bullets.add(Bullet(bulletBitmap, rightBulletX, bulletY, speedX = 5f))

                                // 在中间位置创建两颗子弹，间隔更大
                                val middleBulletX1 = it.x + it.normalBitmap.width * 0.5f - bulletBitmap.width - bulletBitmap.width / 2f
                                val middleBulletX2 = it.x + it.normalBitmap.width * 0.5f + bulletBitmap.width / 2f
                                bullets.add(Bullet(bulletBitmap, middleBulletX1, bulletY))
                                bullets.add(Bullet(bulletBitmap, middleBulletX2, bulletY))
                            }
                            4 -> {
                                // 创建向左和向右的子弹，它们的x轴速度为负值和正值，间隔更大
                                val leftBulletX1 = it.x + it.normalBitmap.width * 0.5f - bulletBitmap.width - 3 * bulletBitmap.width
                                val rightBulletX1 = it.x + it.normalBitmap.width * 0.5f + 3 * bulletBitmap.width
                                bullets.add(Bullet(bulletBitmap, leftBulletX1, bulletY, speedX = -5f))
                                bullets.add(Bullet(bulletBitmap, rightBulletX1, bulletY, speedX = 5f))

                                // 在左右两侧各再增加一颗子弹，间隔更大
                                val leftBulletX2 = it.x + it.normalBitmap.width * 0.5f - bulletBitmap.width - bulletBitmap.width / 2f
                                val rightBulletX2 = it.x + it.normalBitmap.width * 0.5f + bulletBitmap.width + 4 * bulletBitmap.width
                                bullets.add(Bullet(bulletBitmap, leftBulletX2, bulletY, speedX = -5f))
                                bullets.add(Bullet(bulletBitmap, rightBulletX2, bulletY, speedX = 5f))

                                // 在中间位置创建两颗子弹，间隔更大
                                val middleBulletX1 = it.x + it.normalBitmap.width * 0.5f - bulletBitmap.width - bulletBitmap.width / 2f
                                val middleBulletX2 = it.x + it.normalBitmap.width * 0.5f + bulletBitmap.width / 2f
                                bullets.add(Bullet(bulletBitmap, middleBulletX1, bulletY))
                                bullets.add(Bullet(bulletBitmap, middleBulletX2, bulletY))
                            }
                            else -> {
                                // 如果火力等级大于 3，打印一个错误消息
                                println("Invalid powerUpLevel: ${it.powerUpLevel}")
                            }
                        }
                    }
                    soundPool.play(shootSoundId, 0.5f, 0.5f, 0, 0, 2f)
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
        soundPool.stop(shootSoundId) // 停止播放子弹发射的声音
        soundPool.release()
    }

}
