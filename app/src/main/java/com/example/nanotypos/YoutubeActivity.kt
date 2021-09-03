package com.example.nanotypos

import android.os.Bundle
import android.widget.Toast
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView


/**
 * A simple YouTube Android API demo application which shows how to create a simple application that
 * displays a YouTube Video in a [YouTubePlayerView].
 *
 *
 * Note, to use a [YouTubePlayerView], your activity must extend [YouTubeBaseActivity].
 */

class YoutubeActivity : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube)
        val youTubeView = findViewById<YouTubePlayerView>(R.id.player)
        youTubeView.initialize(api_key, this)
    }

    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider?, player: YouTubePlayer,
        wasRestored: Boolean
    ) {
        if (!wasRestored) {
            player.cueVideo(videoID)
        }
    }

    override fun onInitializationFailure(
        p0: YouTubePlayer.Provider?,
        p1: YouTubeInitializationResult?
    ) {
        Toast.makeText(this,"Youtube Api Initialization Fail cause $p1", Toast.LENGTH_SHORT).show()
    }


    companion object {
        const val videoID = "O2dKo_wC1Dg"
        const val api_key = "AIzaSyCZZZ93hntMuPk-RX1DKwrNvgYAAi1lZIE"
    }
}

