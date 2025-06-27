package com.ecodeli

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CardInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_info)

        val ownerName = intent.getStringExtra("cardOwner")
        val cardId = intent.getStringExtra("cardId")

        val ownerTextView = findViewById<TextView>(R.id.tvCardOwner)
        val cardIdTextView = findViewById<TextView>(R.id.tvCardId)

        ownerTextView.text = "Propriétaire : $ownerName"
        cardIdTextView.text = "ID Carte : $cardId"
    }
}
