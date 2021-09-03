package com.example.nanotypos
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerFragment
import com.google.android.youtube.player.YouTubePlayerSupportFragmentX


class PlayerFragment: Fragment(R.layout.fragment_player) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val youtubePlayerFragment = YouTubePlayerSupportFragmentX.newInstance()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.player, youtubePlayerFragment).commit()

        youtubePlayerFragment.initialize(api_key, object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                p0: YouTubePlayer.Provider?,
                p1: YouTubePlayer?,
                p2: Boolean
            ) {
                p1?.loadVideo(videoID)
                p1?.play()
            }

            override fun onInitializationFailure(
                p0: YouTubePlayer.Provider?,
                p1: YouTubeInitializationResult?
            ) {
            }
        })

        return inflater.inflate(R.layout.fragment_player, container, false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (childFragmentManager.findFragmentById(R.id.player) as YouTubePlayerFragment?)?.initialize(
            api_key,
            object : YouTubePlayer.OnInitializedListener {
                override fun onInitializationSuccess(
                    p0: YouTubePlayer.Provider?,
                    p1: YouTubePlayer?,
                    p2: Boolean
                ) {
                    p1?.loadVideo(videoID)
                    p1?.play()
                }

                override fun onInitializationFailure(
                    p0: YouTubePlayer.Provider?,
                    p1: YouTubeInitializationResult?
                ) {
                }
            })
    }


    companion object {
        const val videoID = "O2dKo_wC1Dg"
        const val api_key = "AIzaSyCZZZ93hntMuPk-RX1DKwrNvgYAAi1lZIE"
    }


}