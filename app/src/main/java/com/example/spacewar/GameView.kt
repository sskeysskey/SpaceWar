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
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources

class GameView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val plane: Bitmap = (AppCompatResources.getDrawable(context, R.drawable.player) as BitmapDrawable).bitmap
    private val planeHit: Bitmap = (AppCompatResources.getDrawable(context, R.drawable.player_hit) as BitmapDrawable).bitmap
    private val background: Bitmap = (AppCompatResources.getDrawable(context, R.drawable.background) as BitmapDrawable).bitmap
    private var backgroundY = 0f
    private var backgroundSpeed = 3f //背景滚动速度
    var player: Player? = null

    private var updateThread: Thread? = null
    private var running = true
    private var asteroidManager: AsteroidManager? = null
    private var bulletManager: BulletManager
    private var deathImage: Bitmap = (AppCompatResources.getDrawable(context, R.drawable.death_image) as BitmapDrawable).bitmap
    private var dead = false

    private fun checkPlayerPowerUpCollision() {
        val powerUp = asteroidManager?.powerUp

        if (powerUp != null && player != null && RectF.intersects(player!!.boundingBox, powerUp.boundingBox) && player!!.powerUpLevel < 6) {
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
            player = Player(plane, planeHit, w, h, context)
        }
        if (asteroidManager == null) {
            asteroidManager = AsteroidManager(context, w, h, bulletManager.playerbullets)
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
        asteroidManager?.drawEnemyBullets(canvas)

        if (dead) {
            // 显示死亡图片
            canvas.drawBitmap(deathImage, (width - deathImage.width) / 2f, (height - deathImage.height) / 2f, null)
        }
    }

    private fun update() {
        player?.updateInvincibleState() // 更新飞机的无敌状态
        (context as Activity).runOnUiThread {
            invalidate()
        }

        backgroundY += backgroundSpeed
        if (backgroundY >= height) {
            backgroundY = 0f
        }

        bulletManager.updateBullets()
        synchronized(bulletManager.playerbullets) {
            asteroidManager?.updateAsteroids()
            asteroidManager?.checkPlayerBulletAsteroidCollision()
        }
        asteroidManager?.powerUp?.update()
        checkPlayerPowerUpCollision()

        player?.let {
            asteroidManager?.updateEnemies(it)
            asteroidManager?.checkPlayerBulletEnemyCollision()
            asteroidManager?.checkPlayerAsteroidCollision(it)
            asteroidManager?.checkPlayerEnemyCollision(it)
            asteroidManager?.checkEnemyBulletPlayerCollision(it)
            if (it.health <= 0) {
                dead = true
            }
        }
        asteroidManager?.updateEnemyBullets()
    }

    private fun handleTouchEvent(event: MotionEvent) {
        player?.let {
            val dx = event.x - it.x - it.normalBitmap.width / 2
            val dy = event.y - it.y - it.normalBitmap.height / 2
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_W -> player?.update(0f, -1f)
            KeyEvent.KEYCODE_S -> player?.update(0f, 1f)
            KeyEvent.KEYCODE_A -> player?.update(-1f, 0f)
            KeyEvent.KEYCODE_D -> player?.update(1f, 0f)
        }
        return super.onKeyDown(keyCode, event)
    }
}
