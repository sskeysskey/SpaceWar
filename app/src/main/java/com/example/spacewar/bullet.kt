package com.example.spacewar

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF

class Bullet(val bitmap: Bitmap, private var x: Float, var y: Float) {
    private val speed = 20f // 子弹发射出去的速度
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