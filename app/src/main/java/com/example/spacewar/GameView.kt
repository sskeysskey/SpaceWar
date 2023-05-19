package com.example.spacewar

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
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
    private var backgroundSpeed = 3f //背景滚动速度
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

    private fun checkShipPowerUpCollision() {
        val powerUp = asteroidManager?.powerUp

        if (powerUp != null && player != null && RectF.intersects(player!!.boundingBox, powerUp.boundingBox)) {
            player!!.powerUpLevel += 1
            asteroidManager?.powerUp = null
        }
    }


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
        }, 0, 200) // 每 200 毫秒发射一颗子弹


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
        asteroidManager?.powerUp?.draw(canvas) // 绘制道具
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
        asteroidManager?.powerUp?.update() // 更新道具位置
        checkShipPowerUpCollision()
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