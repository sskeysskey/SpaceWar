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

class GameView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val plane: Bitmap = (AppCompatResources.getDrawable(context, R.drawable.player) as BitmapDrawable).bitmap
    private val background: Bitmap = (AppCompatResources.getDrawable(context, R.drawable.background) as BitmapDrawable).bitmap
    private var backgroundY = 0f
    private var backgroundSpeed = 3f //背景滚动速度
    var player: Player? = null

    private var updateThread: Thread? = null
    private var running = true
    private var asteroidManager: AsteroidManager? = null
    private var bulletManager: BulletManager

    private fun checkPlayerPowerUpCollision() {
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

        bulletManager = BulletManager(context, this)

        updateThread = Thread {
            while (running) {
                update()
                Thread.sleep(10)
            }
        }
        updateThread!!.start()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (player == null) {
            player = Player(plane, w, h)
        }
        if (asteroidManager == null) {
            asteroidManager = AsteroidManager(context, w, h, bulletManager.bullets)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)
        drawBackground(canvas)
        player?.draw(canvas)
        bulletManager.drawBullets(canvas)
        asteroidManager?.drawAsteroids(canvas)
        asteroidManager?.powerUp?.draw(canvas)
        asteroidManager?.drawEnemies(canvas)
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

        bulletManager.updateBullets()
        synchronized(bulletManager.bullets) {
            asteroidManager?.updateAsteroids()
            asteroidManager?.checkBulletAsteroidCollision()
        }
        asteroidManager?.powerUp?.update()
        checkPlayerPowerUpCollision()
        asteroidManager?.updateEnemies()
        asteroidManager?.checkBulletEnemyCollision()
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
        bulletManager.stop() // 在这里调用 BulletManager 的 stop 方法
    }
}
