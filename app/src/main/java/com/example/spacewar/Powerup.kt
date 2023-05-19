package com.example.spacewar

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF

class PowerUp(private val bitmap: Bitmap, private var x: Float, private var y: Float) {
    private val speed = 3f      //火力加强道具掉落速度
    private val width = bitmap.width
    private val height = bitmap.height

    val boundingBox: RectF
        get() = RectF(x, y, x + width, y + height)

    fun update() {
        y += speed
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x, y, null)
    }
}
