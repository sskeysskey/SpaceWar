package com.example.spacewar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.content.res.AppCompatResources
import java.util.Timer
import java.util.TimerTask
import android.graphics.Paint
import android.graphics.Color

class Explosion(private val bitmap: Bitmap, private var x: Float, private var y: Float) {
    private var visibleDuration = 0
    val isVisible: Boolean
        get() = visibleDuration < 5

    fun draw(canvas: Canvas) {
        if (isVisible) {
            canvas.drawBitmap(bitmap, x, y, null)
            visibleDuration++
        }
    }
}

class Asteroid(private val bitmap: Bitmap, var x: Float, var y: Float, var health: Int) {
    private val speed = 5f
    private val width = bitmap.width
    private val height = bitmap.height
    private var rotation = 0f
    private val matrix = Matrix()
    //以下为绘制陨石上血量显示功能
    private val healthPaint = Paint().apply {
        textSize = 40f  // 根据你的需要调整文本大小
        color = Color.WHITE  // 你可以选择其他颜色来表示血量
    }
    //绘制结束

    val boundingBox: RectF
        get() = RectF(x, y, x + width, y + height)

    fun update() {
        y += speed
        rotation = (rotation + 1) % 360 // 在每次 update 时更新旋转角度
    }

    fun draw(canvas: Canvas) {
        matrix.reset()
        matrix.postTranslate(-bitmap.width / 2f, -bitmap.height / 2f) // move the bitmap to fix its pivot to center
        matrix.postRotate(rotation) // rotate the bitmap
        matrix.postTranslate(x + bitmap.width / 2f, y + bitmap.height / 2f) // lastly move the bitmap to its desired location
        canvas.drawBitmap(bitmap, matrix, null)
        // 以下为陨石上绘制血量
        val healthText = health.toString()
        val textWidth = healthPaint.measureText(healthText)
        val textX = x + bitmap.width - textWidth - 10 // 将血量放在陨石右下角，离陨石边缘10个像素
        val textY = y + bitmap.height - 10 // 同上
        canvas.drawText(healthText, textX, textY, healthPaint)
        //绘制结束
    }
}

class AsteroidManager(private val context: Context, private val width: Int, private val height: Int, private val bullets: MutableList<Bullet>) {
    private val asteroids = mutableListOf<Asteroid>()
    var powerUp: PowerUp? = null
    private var asteroidsDestroyed = 0
    private var totalAsteroidsCreated = 0
    private val enemyManager = EnemyManager(context, width, height)
    private var asteroidsNeededForPowerUp = 5
    private val explosions = mutableListOf<Explosion>()


    fun checkPlayerAsteroidCollision(player: Player) {
        if (player.isInvincible) return
        synchronized(asteroids) {
            val asteroidsToRemove = mutableListOf<Asteroid>()

            for (asteroid in asteroids) {
                if (RectF.intersects(player.boundingBox, asteroid.boundingBox)) {
                    // 玩家和陨石碰撞
                    player.hit()
                    player.health -= 1 //或其他数值，根据你的设计调整
                    asteroidsToRemove.add(asteroid)
                }
            }
            asteroids.removeAll(asteroidsToRemove)
        }
    }

    fun checkPlayerEnemyCollision(player: Player) {
        if (player.isInvincible) return
        synchronized(enemyManager.enemies) {
            val enemiesToRemove = mutableListOf<Enemy>()

            for (enemy in enemyManager.enemies) {
                if (RectF.intersects(player.boundingBox, enemy.boundingBox)) {
                    // 玩家和敌机碰撞
                    player.hit()
                    player.health -= 1 //或其他数值，根据你的设计调整
                    enemiesToRemove.add(enemy)
                }
            }
            enemyManager.enemies.removeAll(enemiesToRemove)
        }
    }


    fun updateEnemies() {
        synchronized(enemyManager.enemies) {
            val iterator = enemyManager.enemies.iterator()
            while (iterator.hasNext()) {
                val enemy = iterator.next()
                enemy.update()
                // 如果敌机飞出屏幕，从列表中移除
                if (enemy.y > height || enemy.x > width || enemy.x + enemy.bitmap.width < 0) {
                    iterator.remove()
                }
            }
        }
        // 当陨石数量达到 6 时，开始生成敌机
        if (totalAsteroidsCreated >= 6 && enemyManager.enemies.isEmpty() && !enemyManager.waveInProgress) {
            enemyManager.waveInProgress = true
            enemyManager.launchEnemyWave()
        }
    }

    fun drawEnemies(canvas: Canvas) {
        synchronized(enemyManager.enemies) {
            enemyManager.enemies.forEach { enemy -> enemy.draw(canvas) }
        }
    }

    fun checkBulletEnemyCollision() {
        synchronized(bullets) {
            synchronized(enemyManager.enemies) {
                val bulletsToRemove = mutableListOf<Bullet>()
                val enemiesToRemove = mutableListOf<Enemy>()

                for (bullet in bullets) {
                    for (enemy in enemyManager.enemies) {
                        if (RectF.intersects(bullet.boundingBox, enemy.boundingBox)) {
                            // 子弹和敌机碰撞
                            bulletsToRemove.add(bullet)
                            enemy.health -= 1
                            if (enemy.health <= 0) {
                                enemiesToRemove.add(enemy)
                            }
                            break
                        }
                    }
                }
                bullets.removeAll(bulletsToRemove)
                enemyManager.enemies.removeAll(enemiesToRemove)
            }
        }
    }

    private fun createPowerUp(x: Float, y: Float) {
        val powerUpBitmap = (AppCompatResources.getDrawable(context, R.drawable.powerup) as BitmapDrawable).bitmap
        powerUp = PowerUp(powerUpBitmap, x, y, width, height)
    }

    private fun scaleBitmapToMaxWidth(bitmap: Bitmap, maxWidth: Int): Bitmap {
        return if (bitmap.width <= maxWidth) {
            bitmap
        } else {
            val scaleRatio = maxWidth.toFloat() / bitmap.width
            val newHeight = (bitmap.height * scaleRatio).toInt()
            Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true)
        }
    }

    private fun createRandomAsteroid(): Asteroid {
        val asteroidBitmaps = listOf(
            R.drawable.asteroid1,
            R.drawable.asteroid2,
            R.drawable.asteroid3
        )

        val asteroidHealth = mapOf(
            R.drawable.asteroid1 to 3,
            R.drawable.asteroid2 to 5,
            R.drawable.asteroid3 to 7
        )

        val randomIndex = asteroidBitmaps.indices.random()
        val randomResourceId = asteroidBitmaps[randomIndex]
        val randomHealth = asteroidHealth.getValue(randomResourceId)

        val rawAsteroidBitmap = (AppCompatResources.getDrawable(context, randomResourceId) as BitmapDrawable).bitmap
        val asteroidBitmap = scaleBitmapToMaxWidth(rawAsteroidBitmap, width)

        val x = (0..width - asteroidBitmap.width).random().toFloat()
        val y = -asteroidBitmap.height.toFloat()

        return Asteroid(asteroidBitmap, x, y, randomHealth)
    }

    fun updateAsteroids() {
        synchronized(asteroids) {
            val iterator = asteroids.iterator()
            while (iterator.hasNext()) {
                val asteroid = iterator.next()
                asteroid.update()
                // 如果陨石飞出屏幕，从列表中移除
                if (asteroid.y > height) {
                    iterator.remove()
                }
            }
        }

        // 删除已经不再可见的爆炸效果
        synchronized(explosions) {
            explosions.removeAll { !it.isVisible }
        }
    }

    fun checkBulletAsteroidCollision(){
        synchronized(bullets) {
            synchronized(asteroids) {
                val bulletsToRemove = mutableListOf<Bullet>()
                val asteroidsToRemove = mutableListOf<Asteroid>()

                for (bullet in bullets) {
                    for (asteroid in asteroids) {
                        if (RectF.intersects(bullet.boundingBox, asteroid.boundingBox)) {
                            bulletsToRemove.add(bullet)
                            asteroid.health -= 1
                            if (asteroid.health <= 0) {
                                asteroidsToRemove.add(asteroid)
                                asteroidsDestroyed += 1

                                // 在这里创建新的爆炸效果
                                val explosionBitmap = (AppCompatResources.getDrawable(context, R.drawable.asteroid_explosion) as BitmapDrawable).bitmap
                                val explosion = Explosion(explosionBitmap, asteroid.x, asteroid.y)
                                explosions.add(explosion)

                                if (asteroidsDestroyed == asteroidsNeededForPowerUp) {
                                    // 创建道具
                                    createPowerUp(asteroid.x, asteroid.y)
                                    // 清零摧毁的陨石数量并增加下一次需要的陨石数量
                                    asteroidsDestroyed = 0
                                    asteroidsNeededForPowerUp += 5
                                }
                            }
                            break
                        }
                    }
                }
                bullets.removeAll(bulletsToRemove)
                asteroids.removeAll(asteroidsToRemove)
            }
        }
    }


    fun drawAsteroids(canvas: Canvas) {
        synchronized(asteroids) {
            asteroids.forEach { asteroid -> asteroid.draw(canvas) }
        }

        // 在陨石位置绘制爆炸效果
        synchronized(explosions) {
            explosions.forEach { it.draw(canvas) }
        }
    }


    init {
        val asteroidTimer = Timer()
        asteroidTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val asteroid = createRandomAsteroid()
                synchronized(asteroids) {
                    asteroids.add(asteroid)
                    totalAsteroidsCreated+=1
                }
            }
        }, 0, 1500) // 每 多少 毫秒生成一个陨石
    }
}