package com.example.spacewar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas

class BulletBoss(private val context: Context, private val bitmap: Bitmap, private var x: Float, private var y: Float, private val directionX: Float, private val directionY: Float, private val speed: Float) {
    private val width = bitmap.width
    private val height = bitmap.height

    fun update() {
        x += directionX * speed
        y += directionY * speed
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x, y, null)
    }
}

