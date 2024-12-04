package com.example.savebunny

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View

class GameView (var c: Context, var gameTask: GameTask): View(c) {
    private var myPaint: Paint? = null
    var speed = 1
    var time = 0
    var score = 0
    var myBunnyPosition = 0
    val otherLi = ArrayList<HashMap<String,Any>>()
    private var isPaused = false // Flag to track if the game is paused

    //sharedPreferences
    private lateinit var sharedPreferences: SharedPreferences
    var highScore = 0 // Property to store the high score

    init {
        // Initialize shared preferences
        sharedPreferences = c.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        // Load the high score from shared preferences
        highScore = sharedPreferences.getInt("highScore", 0)
    }

    var viewWidth = 0
    var viewHeight = 0

    init {
        myPaint = Paint()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        viewWidth = this.measuredWidth
        viewHeight = this.measuredHeight

        if (!isPaused) { // Check if the game is not paused
            if (time % 700 < 10 + speed){
                val map = HashMap<String,Any>()
                map["lane"] = (0..2).random()
                map["startTime"] = time
                otherLi.add(map)
            }
            time = time + 10 + speed
            val bunnyWidth = viewWidth / 5
            val bunnyHeight = bunnyWidth + 10
            myPaint!!.style = Paint.Style.FILL

            // Load the character image based on lastPressedButtonId
            val lastPressedButtonId = sharedPreferences.getInt("lastPressedButtonId", 2)
            val characterDrawableId = when (lastPressedButtonId) {
                1 -> R.drawable.bunny
                2 -> R.drawable.bunny2
                3 -> R.drawable.bunny3
                4 -> R.drawable.bunny4
                else -> R.drawable.bunny3 // Default to bunny.png if lastPressedButtonId is unknown
            }

            //import bunny image based on lastPressedButtonId in shared preferences
            val d = context.getDrawable(characterDrawableId)

            d?.let {
                it.setBounds(
                    myBunnyPosition * viewWidth / 3 + viewWidth / 15 + 25,
                    viewHeight - 2 - bunnyHeight,
                    myBunnyPosition * viewWidth / 3 + viewWidth / 15 + bunnyWidth - 25,
                    viewHeight - 2
                )
                it.draw(canvas!!)
            }
            myPaint!!.color = Color.GREEN

            for (i in otherLi.indices) {
                try {
                    val liX = otherLi[i]["lane"] as Int * viewWidth / 3 + viewWidth / 15
                    val liY = time - otherLi[i]["startTime"] as Int
                    val d2 = resources.getDrawable(R.drawable.lightning, null)

                    d2.setBounds(
                        liX + 25 , liY - bunnyHeight , liX + bunnyWidth - 25 , liY
                    )

                    d2.draw(canvas)
                    if (otherLi[i]["lane"] as Int == myBunnyPosition) {
                        if (liY > viewHeight - 2 - bunnyHeight && liY < viewHeight - 2) {
                            if (score > highScore) {
                                highScore = score
                                sharedPreferences.edit().putInt("highScore", highScore).apply()
                            }
                            gameTask.closeGame(score)
                        }
                    }
                    if (liY > viewHeight + bunnyHeight) {
                        otherLi.removeAt(i)
                        score++
                        speed = 1 + Math.abs(score / 8)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            myPaint!!.color = Color.WHITE
            myPaint!!.textSize = 55f
            myPaint!!.isFakeBoldText = true // Make the text bold

            canvas.drawText("Score : $score",60f,100f,myPaint!!)
            canvas.drawText("Speed : $speed",60f,170f,myPaint!!)

            // Draw high score
            canvas.drawText("High Score : $highScore", 60f, 240f, myPaint!!)
        }

        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                val x1 = event.x
                if (x1 < viewWidth / 2) {
                    if (myBunnyPosition > 0) {
                        myBunnyPosition--
                    }
                }
                if (x1 > viewWidth / 2) {
                    myBunnyPosition++
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {}
        }
        return true
    }

    fun pauseGame() {
        // Pause the game logic
        isPaused = true
    }

    fun resumeGame() {
        // Resume the game logic
        isPaused = false
    }

    fun getCurrentScore(): Int {
        return score
    }
}