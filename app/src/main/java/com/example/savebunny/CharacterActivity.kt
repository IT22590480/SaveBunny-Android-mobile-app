package com.example.savebunny

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CharacterActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_character)


        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("ImageButtonPrefs", Context.MODE_PRIVATE)

        // Set onClickListeners for each ImageButton
        val imageButton001 = findViewById<ImageButton>(R.id.imageButton001)
        val imageButton002 = findViewById<ImageButton>(R.id.imageButton002)
        val imageButton003 = findViewById<ImageButton>(R.id.imageButton003)
        val imageButton004 = findViewById<ImageButton>(R.id.imageButton004)

        imageButton001.setOnClickListener { saveAndNavigateBack(1) }
        imageButton002.setOnClickListener { saveAndNavigateBack(2) }
        imageButton003.setOnClickListener { saveAndNavigateBack(3) }
        imageButton004.setOnClickListener { saveAndNavigateBack(4) }
    }

    private fun saveAndNavigateBack(buttonId: Int) {


        // Provide feedback to the user
        val message = "You have selected bunny $buttonId"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        val intent = Intent()
        intent.putExtra("buttonId", buttonId)
        setResult(Activity.RESULT_OK, intent)

        // Navigate back to MainActivity
        // Delay the execution of finish() by 1.5 seconds
        val handler = android.os.Handler()
        handler.postDelayed({
            finish()
        }, 100)
    }
}
