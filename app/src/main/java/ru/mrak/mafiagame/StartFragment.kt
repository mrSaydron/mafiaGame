package ru.mrak.mafiagame

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.findNavController

class StartFragment : Fragment() {

    private lateinit var numberOfPlayersInput: EditText
    private lateinit var startButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_start, container, false)

        numberOfPlayersInput = view.findViewById(R.id.numberOfPlayersInput)
        startButton = view.findViewById(R.id.startButton)

        startButton.setOnClickListener {
            val playersCountText = numberOfPlayersInput.text.toString()
            if (playersCountText.isNotEmpty()) {
                val numberOfPlayers = playersCountText.toIntOrNull()
                if (numberOfPlayers != null && numberOfPlayers in 4..10) {
                    val bundle = Bundle().apply {
                        putInt("numberOfPlayers", numberOfPlayers)
                    }
                    Navigation.findNavController(view)
                        .navigate(R.id.registrationFragment, bundle)
                } else {
                    Toast.makeText(activity, "Please enter between 4 and 10 players", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }

}