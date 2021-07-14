package com.example.nanotypos

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rollButton: Button = findViewById(R.id.button)
        rollButton.setOnClickListener {
            rollDice()
            Toast.makeText(this, "Dice Rolled!", Toast.LENGTH_SHORT).show()}

        val logoButton: Button = findViewById(R.id.logo)
        var logo = true
        logoButton.setOnClickListener{
            val logoImage: ImageView = findViewById(R.id.logo_image)

            logo = if (logo) {
                logoImage.setImageResource(R.drawable.nanotypos_logo1)
                false
            } else {
                logoImage.setImageResource(R.drawable.nanotypus)
                true
            }

        }
    }


    private fun rollDice() {
        val dice = Dice(6)
        val diceRoll = dice.roll()
        val resultTextView: TextView = findViewById(R.id.textView)
        resultTextView.text = diceRoll.toString()

    }
}

class Dice(val numSides: Int) {

    fun roll(): Int {
        return (1..numSides).random()
    }
}