package com.example.nanotypos
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.nanotypos.databinding.FragmentPlayerBinding
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerFragment





class PlayerFragment: YouTubePlayerFragment()  {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val youTubePlayerFragment = childFragmentManager.findFragmentById(R.id.playerFragment) as YouTubePlayerFragment
        youTubePlayerFragment.initialize(api_key, object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                provider: YouTubePlayer.Provider,
                youTubePlayer: YouTubePlayer, b: Boolean
            ) {

                youTubePlayer.loadVideo(videoID)
                youTubePlayer.play()
                Log.d("LOGO", "PLAYYAYAYAYAY")

            }

            override fun onInitializationFailure(
                provider: YouTubePlayer.Provider,
                youTubeInitializationResult: YouTubeInitializationResult
            ) {
                Toast.makeText(context, "Youtube Failed!", Toast.LENGTH_SHORT)
                    .show()
            }
        })
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)

        val playerFragment = PlayerFragment()
        childFragmentManager.beginTransaction().apply {
            add(R.id.player, playerFragment)
            commit()
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val youTubePlayerFragment = childFragmentManager.findFragmentById(R.id.playerFragment) as YouTubePlayerFragment
        youTubePlayerFragment.initialize(api_key, object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                provider: YouTubePlayer.Provider,
                youTubePlayer: YouTubePlayer, b: Boolean
            ) {

                youTubePlayer.loadVideo(videoID)
                youTubePlayer.play()
                Log.d("LOGO", "PLAYYAYAYAYAY")

            }

            override fun onInitializationFailure(
                provider: YouTubePlayer.Provider,
                youTubeInitializationResult: YouTubeInitializationResult
            ) {
                Toast.makeText(context, "Youtube Failed!", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }


    companion object {
        const val videoID = "O2dKo_wC1Dg"
        const val api_key = "AIzaSyCZZZ93hntMuPk-RX1DKwrNvgYAAi1lZIE"
    }
}


