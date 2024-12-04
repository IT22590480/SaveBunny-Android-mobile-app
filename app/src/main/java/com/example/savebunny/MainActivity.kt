package com.example.savebunny

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.activity.OnBackPressedCallback

class MainActivity : AppCompatActivity(),GameTask {

    // Constants
    companion object {
        private const val CHARACTER_ACTIVITY_REQUEST_CODE = 1
    }

    lateinit var rootLayout : LinearLayout
    lateinit var startBtn : Button
    lateinit var ccBtn : Button
    lateinit var mGameView: GameView
    lateinit var score : TextView
    lateinit var title : TextView
    lateinit var menuTitle : TextView
    lateinit var mediaPlayer: MediaPlayer
    lateinit var musicSwitch: Switch
    var isMusicPlaying = false // Track if music is currently playing


    //shared preferences
    private lateinit var sharedPreferences: SharedPreferences
    private var highScore = 0 // Initialize high score to 0 initially

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        // Load the high score from shared preferences
        highScore = sharedPreferences.getInt("highScore", 0)

        // Pass the high score to the GameView when it's created
        mGameView = GameView(this, this)
        mGameView.highScore = highScore // Set the high score in GameView

        startBtn = findViewById(R.id.startBtn)
        ccBtn = findViewById(R.id.ccBtn)
        rootLayout = findViewById(R.id.rootlayout)
        score = findViewById(R.id.score)
        title = findViewById(R.id.title)
        menuTitle = findViewById(R.id.menuTitle)

        // Initialize the Switch button and set its state based on SharedPreferences
        musicSwitch = findViewById(R.id.music)
        musicSwitch.isChecked = sharedPreferences.getBoolean("musicState", true)

        // Set the initial state of music based on the Switch state
        if (musicSwitch.isChecked) {
            startBackgroundMusic()
        } else {
            stopBackgroundMusic()
        }

        // Set a click listener to the Switch button
        musicSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save the state of the Switch button in SharedPreferences
            sharedPreferences.edit().putBoolean("musicState", isChecked).apply()

            // Check if the Switch is checked (ON) or unchecked (OFF)
            if (isChecked) {
                // If checked, start playing the background music
                startBackgroundMusic()
            } else {
                // If unchecked, stop the background music
                stopBackgroundMusic()
            }
        }



        startBtn.setOnClickListener{
            // Check if GameView is already added to the layout
            if (mGameView.parent == null) {
                // GameView is not added, so add it to the layout
                mGameView.setBackgroundResource(R.drawable.rain_play)
                rootLayout.addView(mGameView)
            } else {
                // GameView is already added, make sure it's visible
                mGameView.visibility = View.VISIBLE
            }

            // Hide the startBtn and score TextView
            startBtn.visibility = View.GONE
            score.visibility = View.GONE
            title.visibility = View.GONE
            musicSwitch.visibility = View.GONE
            ccBtn.visibility = View.GONE
            menuTitle.visibility = View.GONE
        }

        ccBtn.setOnClickListener {
            // Start CharacterActivity
            val intent = Intent(this, CharacterActivity::class.java)
            startActivityForResult(intent, CHARACTER_ACTIVITY_REQUEST_CODE)
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Pause the game loop
                pauseGameLoop()

                // Prompt the user if they want to exit
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Exit Game")
                    .setMessage("Do you want to exit?")
                    .setPositiveButton("Yes") { _, _ ->
                        if (title.visibility == View.VISIBLE) {
                            // If the title is visible, close the app
                            finish()
                        } else {
                            // If the title is not visible, proceed with closing the game
                            val currentScore = mGameView.getCurrentScore()
                            closeGame(currentScore)
                        }
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        // Resume the game loop when the dialog is dismissed
                        resumeGameLoop()
                        dialog.dismiss() // Dismiss the dialog
                    }
                    .setOnDismissListener {
                        // Resume the game loop when the dialog is dismissed
                        resumeGameLoop()
                    }
                    .show()
            }
        }





        onBackPressedDispatcher.addCallback(this, callback)

    }

    override fun closeGame(mScore: Int) {


        score.text = "Score = $mScore"
        rootLayout.removeView(mGameView)
        startBtn.visibility = View.VISIBLE
        score.visibility = View.VISIBLE
        title.visibility = View.VISIBLE
        musicSwitch.visibility = View.VISIBLE
        ccBtn.visibility = View.VISIBLE
        menuTitle.visibility = View.VISIBLE

        startBtn.setOnClickListener{

            mGameView.score = 0
            mGameView.speed = 1
            mGameView.time = 0
            mGameView.myBunnyPosition = 0
            mGameView.otherLi.clear()
            // Check if GameView is already added to the layout
            if (mGameView.parent == null) {
                // GameView is not added, so add it to the layout
                mGameView.setBackgroundResource(R.drawable.rain_play)
                rootLayout.addView(mGameView)
            } else {
                // GameView is already added, make sure it's visible
                mGameView.visibility = View.VISIBLE
            }

            // Hide the startBtn and score TextView and title
            startBtn.visibility = View.GONE
            score.visibility = View.GONE
            title.visibility = View.GONE
            musicSwitch.visibility = View.GONE
            ccBtn.visibility = View.GONE
            menuTitle.visibility = View.GONE
        }

    }

    private fun startBackgroundMusic() {
        if (!isMusicPlaying) {
            // Initialize the MediaPlayer for background music
            mediaPlayer = MediaPlayer.create(this, R.raw.backgroundmusic01)
            mediaPlayer.isLooping = true
            mediaPlayer.start()
            isMusicPlaying = true
        }
    }

    private fun stopBackgroundMusic() {
        if (isMusicPlaying) {
            // Pause and release the MediaPlayer
            mediaPlayer.pause()
            mediaPlayer.release()
            isMusicPlaying = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release the MediaPlayer to avoid memory leaks
        mediaPlayer.release()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHARACTER_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Receive the selected button ID from CharacterActivity
            val buttonId = data?.getIntExtra("buttonId", 1) ?: 1
            // Save the selected button ID in shared preferences
            sharedPreferences.edit().putInt("lastPressedButtonId", buttonId).apply()
        }
    }

    fun pauseGameLoop() {
        // Stop the game loop or any other ongoing game logic
        // For example, you can pause animations, stop timers, etc.
        mGameView.pauseGame()
    }

    fun resumeGameLoop() {
        // Resume the game loop or any other paused game logic
        // For example, you can resume animations, start timers, etc.
        mGameView.resumeGame()
    }



}