package com.example.nanotypos

import android.os.Bundle
import android.widget.Toast
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView

class YoutubeActivity :  YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener {

    override fun onInitializationSuccess(provider: YouTubePlayer.Provider?, player: YouTubePlayer?, wasRestored: Boolean) {
        Toast.makeText(this,"Youtube Api Initialization Success", Toast.LENGTH_SHORT).show()
        if (!wasRestored) {
            player?.cueVideo(videoID);
        }
    }
    override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
        Toast.makeText(this,"Youtube Api Initialization Fail", Toast.LENGTH_SHORT).show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube)
        findViewById<YouTubePlayerView>(R.id.ytplayer).initialize(api_key, this)

    }

    companion object {
        const val videoID = "O2dKo_wC1Dg"
        const val api_key = "AIzaSyCZZZ93hntMuPk-RX1DKwrNvgYAAi1lZIE"
    }
}

