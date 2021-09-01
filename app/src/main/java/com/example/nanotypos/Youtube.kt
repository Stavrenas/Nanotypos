package com.example.nanotypos

import android.os.Bundle
import android.widget.Toast
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView





class PlayYoutubeActivity(player: YouTubePlayerView) : YouTubeBaseActivity() {

    val youTubePlayerView = player


    override fun onCreate(savedInstanceState: Bundle?) {



        youTubePlayerView.initialize(
            api_key,
            object : YouTubePlayer.OnInitializedListener {
                override fun onInitializationSuccess(
                    provider: YouTubePlayer.Provider,
                    youTubePlayer: YouTubePlayer, b: Boolean
                ) {

                    // do any work here to cue video, play video, etc.
                    youTubePlayer.loadVideo(videoID)
                    youTubePlayer.play()
                    // or to play immediately
                    // youTubePlayer.loadVideo("5xVh-7ywKpE");
                }

                override fun onInitializationFailure(
                    provider: YouTubePlayer.Provider,
                    youTubeInitializationResult: YouTubeInitializationResult
                ) {
                    Toast.makeText(this@PlayYoutubeActivity, "Youtube Failed!", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        super.onCreate(savedInstanceState)
    }


    companion object {
        const val videoID = "O2dKo_wC1Dg"
        const val api_key = "AIzaSyCZZZ93hntMuPk-RX1DKwrNvgYAAi1lZIE"
    }
}


