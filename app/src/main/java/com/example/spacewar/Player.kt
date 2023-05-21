package com.example.spacewar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.content.res.AppCompatResources

class Player(val bitmap: Bitmap, private val screenWidth: Int, private val screenHeight: Int, context: Context) {
    internal var x: Float = (screenWidth - bitmap.width) / 2f
    internal var y: Float = screenHeight * 5 / 6f - bitmap.height / 2f

    var speed = 100f
    var powerUpLevel = 0

    private val width = bitmap.width
    private val height = bitmap.height

    private val healthUnit: Bitmap = (AppCompatResources.getDrawable(context, R.drawable.healthbar) as BitmapDrawable).bitmap
    var health: Int = 7
    private val healthBarX: Int = 50
    private val healthBarY: Int = 50

    // Added a spacing constant for the health bar units
    private val healthBarSpacing: Int = 10

    val boundingBox: RectF
        get() = RectF(x, y, x + width, y + height)

    fun update(dx: Float, dy: Float) {
        x += dx * speed
        y += dy * speed

        // 防止飞机飞出屏幕
        if (x < 0) x = 0f
        if (y < 0) y = 0f
        if (x + width > screenWidth) x = screenWidth - width.toFloat()
        if (y + height > screenHeight) y = screenHeight - height.toFloat()
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x, y, null)

        // Draw HealthBar with spacing between units
        for (i in 0 until health) {
            val dst = Rect(healthBarX + i * (healthUnit.width + healthBarSpacing), healthBarY, healthBarX + i * (healthUnit.width + healthBarSpacing) + healthUnit.width, healthBarY + healthUnit.height)
            canvas.drawBitmap(healthUnit, null, dst, null)
        }
    }
}
