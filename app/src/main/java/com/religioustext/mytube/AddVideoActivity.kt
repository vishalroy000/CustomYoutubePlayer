package com.religioustext.mytube

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class AddVideoActivity : AppCompatActivity() {

    private lateinit var videoIdEditText: EditText
    private lateinit var addVideoButton: Button
    private lateinit var categorySpinner: Spinner
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_video)

        videoIdEditText = findViewById(R.id.videoIdEditText)
        addVideoButton = findViewById(R.id.addVideoButton)
        categorySpinner = findViewById(R.id.categorySpinner)

        // Spinner data
        val categories = arrayOf("ram", "hanuman", "mata", "ganesh ji", "shiv ji", "baba mohan ram", "santoshi mata")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        addVideoButton.setOnClickListener {
            val videoId = videoIdEditText.text.toString().trim()
            val category = categorySpinner.selectedItem.toString()
            if (videoId.isNotEmpty()) {
                addVideoIdToFirestore(videoId, category)
                videoIdEditText.text.clear()
            } else {
                Toast.makeText(this, "Please enter a video ID", Toast.LENGTH_SHORT).show()
            }
        }


        handleSharedContent()



    }

    private fun addVideoIdToFirestore(videoId: String, category: String) {
        val video = hashMapOf(
            "videoId" to videoId,
            "category" to category
        )

        db.collection("videos")
            .add(video)
            .addOnSuccessListener {
                Toast.makeText(this, "Video ID added successfully", Toast.LENGTH_SHORT).show()
                finish() // Close the activity after successful addition
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding video ID: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun handleSharedContent() {
        val intent = intent
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null && "text/plain" == type) {
            handleSharedText(intent)
        }
    }

    private fun handleSharedText(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText != null) {
            val videoId = extractVideoIdFromUrl(sharedText)
            if (videoId.isNotEmpty()) {
                videoIdEditText.setText(videoId)
            }
        }
    }

    private fun extractVideoIdFromUrl(url: String): String {
        val pattern = "(?<=youtu.be/|v=|/videos/|embed\\/|youtu.be\\?v=|watch\\?v%3D|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|watch%3Fv=|\\?v=)[^#\\&\\?\\n]*"
        val regex = Pattern.compile(pattern)
        val matcher = regex.matcher(url)
        return if (matcher.find()) {
            matcher.group()
        } else {
            ""
        }
    }
}
