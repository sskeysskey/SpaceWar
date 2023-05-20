package com.example.spacewar

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF

class Player(val bitmap: Bitmap, private val screenWidth: Int, private val screenHeight: Int) {
    internal var x: Float = (screenWidth - bitmap.width) / 2f
    internal var y: Float = screenHeight * 5 / 6f - bitmap.height / 2f

    // 初始速度
    var speed = 100f

    // 飞机的大小
    private val width = bitmap.width
    private val height = bitmap.height

    // 添加 powerUpLevel
    var powerUpLevel = 0

    // 飞机的碰撞箱
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
    }
}