package com.religioustext.mytube


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    lateinit var youTubePlayerView: YouTubePlayerView
    lateinit var videoId: String

    private var youTubePlayer: YouTubePlayer? = null

    private lateinit var videoAdapter: VideoAdapter
    private lateinit var recyclerView: RecyclerView
    private val videoList = mutableListOf<YoutubeVideoResponse>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var addVideoButton: Button
    private lateinit var categorySpinner: Spinner
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        videoAdapter = VideoAdapter(videoList)
        recyclerView.adapter = videoAdapter
        youTubePlayerView = findViewById(R.id.youtube_player_view)
        addVideoButton = findViewById(R.id.addVideoButton)
        addVideoButton.setOnClickListener {
            val intent = Intent(this, AddVideoActivity::class.java)
            startActivity(intent)
        }
        categorySpinner = findViewById(R.id.categorySpinner)

        // Spinner data
        val categories = arrayOf("ram", "hanuman", "mata", "ganesh ji", "shiv ji", "baba mohan ram", "santoshi mata")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
        categorySpinner.onItemSelectedListener = this

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            val selectedCategory = categorySpinner.selectedItem.toString()
            fetchVideosByCategory(selectedCategory)
        }


        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {


            override fun onReady(player: YouTubePlayer) {
                youTubePlayer = player

//                youTubePlayer?.loadVideo(videoId, 0f)
                youTubePlayer!!.loadOrCueVideo(lifecycle, videoId, 0f)

            }
        })

        // Load default category
        fetchVideosByCategory(categories[0])
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        val selectedCategory = parent.getItemAtPosition(position).toString()
        fetchVideosByCategory(selectedCategory)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Do nothing
    }

    private fun fetchVideosByCategory(category: String) {
        swipeRefreshLayout.isRefreshing = true

        db.collection("videos")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { documents ->
                videoList.clear()
                for (document in documents) {
                     videoId = document.getString("videoId").toString()
                    if (videoId != null) {
                        fetchVideoDetails(videoId)
                    }
                }
                swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching videos: ${exception.message}", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
    }

    private fun fetchVideoDetails(videoId: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.youtube.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(YoutubeApiService::class.java)
        val videoUrl = "https://www.youtube.com/watch?v=$videoId"
        service.getVideoDetails(videoUrl).enqueue(object : Callback<YoutubeVideoResponse> {
            override fun onResponse(call: Call<YoutubeVideoResponse>, response: Response<YoutubeVideoResponse>) {
                response.body()?.let { video ->
                    videoList.add(video)
                    videoAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<YoutubeVideoResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error fetching video details", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

