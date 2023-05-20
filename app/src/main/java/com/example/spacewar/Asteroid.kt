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

class Asteroid(private val bitmap: Bitmap, var x: Float, var y: Float, var health: Int) {
    private val speed = 5f
    private val width = bitmap.width
    private val height = bitmap.height
    private var rotation = 0f // 添加了旋转角度属性
    private val matrix = Matrix()

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
    }
}

class AsteroidManager(private val context: Context, private val width: Int, private val height: Int, private val bullets: MutableList<Bullet>) {
    private val asteroids = mutableListOf<Asteroid>()
    var powerUp: PowerUp? = null
    private var powerUpUsed = false
    private var asteroidsDestroyed = 0

    private val enemies = mutableListOf<Enemy>()
    private var totalAsteroidsCreated = 0
    private var waveInProgress = false

    private fun createEnemy(enemyType: Int): Enemy {
        val enemyResourceId = when (enemyType) {
            1 -> R.drawable.enemy1
            2 -> R.drawable.enemy2
            3 -> R.drawable.enemy3
            4 -> R.drawable.enemy4
            else -> R.drawable.enemy1
        }
        val enemyHealth = when (enemyType) {
            1 -> 10
            2 -> 11
            3 -> 12
            4 -> 13
            else -> 13
        }
        val enemyBitmap = (AppCompatResources.getDrawable(context, enemyResourceId) as BitmapDrawable).bitmap
        val x = when (enemyType) {
            2 -> -enemyBitmap.width.toFloat()
            4 -> width.toFloat()
            5 -> width.toFloat()
            else -> (0..width - enemyBitmap.width).random().toFloat()
        }
        val y = when (enemyType) {
            2, 4 -> height / 4f - enemyBitmap.height / 2f
            5 -> -enemyBitmap.height.toFloat()
            else -> -enemyBitmap.height.toFloat()
        }
        return Enemy(enemyBitmap, x, y, enemyHealth, enemyType)
    }

    fun updateEnemies() {
        synchronized(enemies) {
            val iterator = enemies.iterator()
            while (iterator.hasNext()) {
                val enemy = iterator.next()
                enemy.update()
                // 如果敌机飞出屏幕，从列表中移除
                if (enemy.y > height || enemy.x > width || enemy.x + enemy.bitmap.width < 0) {
                    iterator.remove()
                }
            }
        }
        // 当陨石数量达到 20 时，开始生成敌机
        if (totalAsteroidsCreated >= 6 && enemies.isEmpty() && !waveInProgress) {
            waveInProgress = true
            launchEnemyWave()
        }
    }

    private fun launchEnemyWave() {
        val enemyTypes = listOf(1, 2, 3, 4, 5)
        val delay = 500L

        for (i in 0 until 4) {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    synchronized(enemies) {
                        val enemyType = enemyTypes.random()
                        enemies.add(createEnemy(enemyType))
                    }
                }
            }, delay * i)
        }
        waveInProgress = false
    }

    fun checkBulletEnemyCollision() {
        synchronized(bullets) {
            synchronized(enemies) {
                val bulletsToRemove = mutableListOf<Bullet>()
                val enemiesToRemove = mutableListOf<Enemy>()

                for (bullet in bullets) {
                    for (enemy in enemies) {
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
                enemies.removeAll(enemiesToRemove)
            }
        }
    }

    fun drawEnemies(canvas: Canvas) {
        synchronized(enemies) {
            enemies.forEach { enemy -> enemy.draw(canvas) }
        }
    }

    private fun createPowerUp(x: Float, y: Float) {
        val powerUpBitmap = (AppCompatResources.getDrawable(context, R.drawable.power_up) as BitmapDrawable).bitmap
        powerUp = PowerUp(powerUpBitmap, x, y)
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
            R.drawable.asteroid3 to 9
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
    }

    fun checkBulletAsteroidCollision(){
        synchronized(bullets) {
            synchronized(asteroids) {
                val bulletsToRemove = mutableListOf<Bullet>()
                val asteroidsToRemove = mutableListOf<Asteroid>()

                for (bullet in bullets) {
                    for (asteroid in asteroids) {
                        if (RectF.intersects(bullet.boundingBox, asteroid.boundingBox)) {
                            // 子弹和陨石碰撞
                            bulletsToRemove.add(bullet)
                            asteroid.health -= 1
                            if (asteroid.health <= 0) {
                                asteroidsToRemove.add(asteroid)
                                asteroidsDestroyed += 1

                                // 如果这是第一次击碎第5块陨石，并且道具还没有被使用过，那么在陨石的位置创建一个道具
                                if (asteroidsDestroyed == 5 && !powerUpUsed) {
                                    createPowerUp(asteroid.x, asteroid.y)
                                    powerUpUsed = true
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
    }

    init {
        val asteroidTimer = Timer()
        asteroidTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val asteroid = createRandomAsteroid()
                synchronized(asteroids) {
                    asteroids.add(asteroid)
                    totalAsteroidsCreated += 1
                }
            }
        }, 0, 1500) // 每 多少 毫秒生成一个陨石
    }
}

class Enemy(
    val bitmap: Bitmap,
    var x: Float,
    var y: Float,
    var health: Int,
    private val type: Int
) {
    private val speed = 4
    private val horizontalSpeed = 2

    val boundingBox: RectF
        get() = RectF(x, y, x + bitmap.width, y + bitmap.height)

    fun update() {
        when (type) {
            1 -> y += speed
            2 -> {
                x += horizontalSpeed
                y += speed / 2f
            }
            3 -> {
                x += speed / 2f
                y += speed
            }
            4 -> {
                x -= horizontalSpeed
                y += speed / 2f
            }
            5 -> {
                x -= speed / 2f
                y += speed
            }
        }
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x, y, null)
    }
}