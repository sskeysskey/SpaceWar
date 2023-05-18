package com.example.spacewar

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import java.util.Timer
import java.util.TimerTask

class GameView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val plane: Bitmap = (AppCompatResources.getDrawable(context, R.drawable.player) as BitmapDrawable).bitmap
    private val background: Bitmap = (AppCompatResources.getDrawable(context, R.drawable.background) as BitmapDrawable).bitmap
    private var backgroundY = 0f
    private var backgroundSpeed = 5f
    private var player: Player? = null

    private val bulletBitmap: Bitmap = run {
        val originalBulletBitmap = (AppCompatResources.getDrawable(context, R.drawable.bullet) as BitmapDrawable).bitmap
        val scaleFactor = 0.3f // 子弹的尺寸和大小
        val newWidth = (originalBulletBitmap.width * scaleFactor).toInt()
        val newHeight = (originalBulletBitmap.height * scaleFactor).toInt()
        Bitmap.createScaledBitmap(originalBulletBitmap, newWidth, newHeight, false)
    }
    private val bullets = mutableListOf<Bullet>()
    private val bulletTimer = Timer()
    private var updateThread: Thread? = null
    private var running = true
    private var asteroidManager: AsteroidManager? = null

    init {
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                handleTouchEvent(event)
            }
            if (event.action == MotionEvent.ACTION_UP) {
                performClick()
            }
            true
        }
        isFocusable = true
        isFocusableInTouchMode = true

        updateThread = Thread {
            while (running) {
                update()
                Thread.sleep(10)
            }
        }
        updateThread!!.start()
        bulletTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (!running) return
                player?.let {
                    val bulletX = it.x + it.bitmap.width / 2f - bulletBitmap.width / 2f
                    val bulletY = it.y - bulletBitmap.height
                    val bullet = Bullet(bulletBitmap, bulletX, bulletY)
                    synchronized(bullets) {
                        bullets.add(bullet)
                    }
                }
            }
        }, 0, 200) // 每 多少 毫秒发射一颗子弹
    }
    private fun updateBullets() {
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (player == null) {
            player = Player(plane, w, h)
        }
        if (asteroidManager == null) {
            asteroidManager = AsteroidManager(context, w, h, bullets)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)
        drawBackground(canvas)
        player?.draw(canvas)
        synchronized(bullets) {
            bullets.forEach { it.draw(canvas) }
        }
        asteroidManager?.drawAsteroids(canvas)
    }

    private fun update() {
        (context as Activity).runOnUiThread {
            invalidate() // 请求重绘视图
        }

        // 更新背景位置
        backgroundY += backgroundSpeed
        if (backgroundY >= height) {
            backgroundY = 0f
        }
        updateBullets()
        synchronized(bullets) {
            asteroidManager?.updateAsteroids()
            asteroidManager?.checkBulletAsteroidCollision()
        }
    }


    private fun handleTouchEvent(event: MotionEvent) {
        player?.let {
            val dx = event.x - it.x - it.bitmap.width / 2
            val dy = event.y - it.y - it.bitmap.height / 2
            it.update(dx / it.speed, dy / it.speed)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        val aspectRatio = background.width.toFloat() / background.height.toFloat()
        val screenHeight = height
        val screenWidth = (aspectRatio * screenHeight).toInt()

        val src = Rect(0, 0, background.width, background.height)
        val dst = Rect(0, backgroundY.toInt(), screenWidth, screenHeight + backgroundY.toInt())
        val dst2 = Rect(0, (backgroundY - screenHeight).toInt(), screenWidth, backgroundY.toInt())

        canvas.drawBitmap(background, src, dst, null)
        canvas.drawBitmap(background, src, dst2, null)
    }

    fun stop() {
        running = false
        updateThread?.interrupt()
        bulletTimer.cancel()
    }
}