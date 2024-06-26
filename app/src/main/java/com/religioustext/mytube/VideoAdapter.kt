package com.religioustext.mytube

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class VideoAdapter(
    private val videoList: List<YoutubeVideoResponse>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(videoId: YoutubeVideoResponse)
    }

    inner class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val thumbnailImageView: ImageView = view.findViewById(R.id.thumbnailImageView)
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val authorTextView: TextView = view.findViewById(R.id.authorTextView)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                itemClickListener.onItemClick(videoList[position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videoList[position]
        holder.titleTextView.text = video.title
        holder.authorTextView.text = video.author_name
        Glide.with(holder.itemView.context)
            .load(video.thumbnail_url)
            .into(holder.thumbnailImageView)
    }

    override fun getItemCount(): Int = videoList.size
}



