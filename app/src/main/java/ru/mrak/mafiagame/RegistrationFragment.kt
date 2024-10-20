package ru.mrak.mafiagame

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.navigation.Navigation

class RegistrationFragment : Fragment() {

    private lateinit var playersList: MutableList<Player>
    private lateinit var playerNameInput: EditText
    private lateinit var nextButton: Button
    private var currentPlayerIndex: Int = 0
    private var numberOfPlayers: Int = 0

    private lateinit var avatarSpinner: Spinner
    private val avatars = listOf("avatar_1", "avatar_2", "avatar_3", "avatar_4", "avatar_5", "avatar_6", "avatar_7")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registration, container, false)

        playerNameInput = view.findViewById(R.id.playerNameInput)
        nextButton = view.findViewById(R.id.nextButton)

        // Настройка Spinner для выбора аватара
        avatarSpinner = view.findViewById(R.id.avatarSpinner)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, avatars)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        avatarSpinner.adapter = adapter

        // Получаем количество игроков из предыдущего фрагмента
        numberOfPlayers = arguments?.getInt("numberOfPlayers") ?: 0
        Log.i("RegistrationFragment", "numberOfPlayers: $numberOfPlayers")
        playersList = mutableListOf()

        nextButton.setOnClickListener {
            val playerName = playerNameInput.text.toString()
            val selectedAvatar = avatarSpinner.selectedItem.toString()

            if (playerName.isNotEmpty()) {
                Log.i("RegistrationFragment", "playerName: $playerName")
                playersList.add(Player(playerName, selectedAvatar, RoleType.CIVILIAN)) // Пока роль как мирный житель
                currentPlayerIndex++

                if (currentPlayerIndex < numberOfPlayers) {
                    playerNameInput.text.clear()
                    Toast.makeText(activity, "Enter name for player ${currentPlayerIndex + 1}", Toast.LENGTH_SHORT).show()
                } else {
                    // Переход к следующему этапу (игра)
                    assignRoles()
                    val bundle = Bundle().apply {
                        putParcelableArrayList("playersList", ArrayList(playersList))
                    }
                    Navigation.findNavController(view)
                        .navigate(R.id.gameFragment, bundle)
                }
            } else {
                Toast.makeText(activity, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun assignRoles() {
        val mafiaCount = 1
        val detectiveCount = 1
        val doctorCount = 1

        for (i in 0 until mafiaCount) {
            getNextRandomPlayer().role = RoleType.MAFIA
        }
        for (i in 0 until detectiveCount) {
            val player = getNextRandomPlayer()
            player.role = RoleType.DETECTIVE
            player.checkedForDetective = true
        }
        for (i in 0 until doctorCount) {
            getNextRandomPlayer().role = RoleType.DOCTOR
        }
    }

    private fun getNextRandomPlayer(): Player {
        var randomPlayer: Player?
        do {
            val randomIndex = (0 until playersList.size).random()
            randomPlayer = playersList[randomIndex]
        } while (randomPlayer!!.role != RoleType.CIVILIAN)
        return randomPlayer
    }

}