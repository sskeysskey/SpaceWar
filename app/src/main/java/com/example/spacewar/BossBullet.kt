package com.example.spacewar

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF

class BossLaser(private val bitmap: Bitmap, var x: Float, var y: Float, private val screenHeight: Int) {
    val width = bitmap.width
    var isActive = false  // 新增的属性

    val boundingBox: RectF
        get() = RectF(x, y, x + width, y + screenHeight)

    fun draw(canvas: Canvas) {
        val bottomOfScreen = canvas.height.toFloat()
        canvas.drawBitmap(bitmap, null, RectF(x, y, x + width, bottomOfScreen), null)
    }
}



