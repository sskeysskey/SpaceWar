package com.example.spacewar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.content.res.AppCompatResources

class Boss(private val context: Context, private val bitmap: Bitmap, var x: Float, var y: Float, private val screenWidth: Int, var health: Int, private val laserResId: Int) {
    private val width = bitmap.width
    private val height = bitmap.height
    private val speed = 3f
    private var movingRight = true
    val bosslaser = createBossLaser()
    private var isFiring = false
    private var fireTimer = 0

    // 添加 healthPaint 对象
    private val healthPaint = Paint().apply {
        textSize = 50f  // 根据你的需要调整文本大小
        color = Color.WHITE  // 你可以选择其他颜色来表示血量
    }

    val boundingBox: RectF
        get() = RectF(x, y, x + width, y + height)

    fun drawHealth(canvas: Canvas) {
        val healthText = health.toString()
        val textWidth = healthPaint.measureText(healthText)
        val textX = x + width - textWidth - 10f // 将血量放在Boss右下角，离Boss边缘10个像素
        val textY = y + height - 10f // 同上
        canvas.drawText(healthText, textX, textY, healthPaint)
    }

    fun update() {
        if (movingRight) {
            if (x + width + speed <= screenWidth) {
                x += speed
            } else {
                movingRight = false
            }
        } else {
            if (x - speed >= 0) {
                x -= speed
            } else {
                movingRight = true
            }
        }
        fireTimer++

        if (fireTimer > 5 * 60) {
            isFiring = !isFiring
            bosslaser.isActive = isFiring
            fireTimer = 0
        }

        if (isFiring) {
            bosslaser.x = this.x + this.width / 2 - bosslaser.width / 2
        }
    }
    private fun createBossLaser(): BossLaser {
        val bosslaserBitmap = (AppCompatResources.getDrawable(context, laserResId) as BitmapDrawable).bitmap
        val bosslaserX = x + bitmap.width / 2f - bosslaserBitmap.width / 2f
        val bosslaserY = y + bitmap.height
        val screenHeight = context.resources.displayMetrics.heightPixels
        return BossLaser(bosslaserBitmap, bosslaserX, bosslaserY, screenHeight)
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x, y, null)
        drawHealth(canvas)

        if (isFiring) {
            bosslaser.draw(canvas)
        }
    }
}

