package ru.mrak.mafiagame

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PlayerListFragment : Fragment() {

    private lateinit var playerAdapter: PlayerAdapter
    private lateinit var playerRecyclerView: RecyclerView
    private lateinit var addPlayerButton: Button
    private lateinit var startGameButton: Button

    private val players = mutableListOf<Player>()
    private val avatars = listOf("avatar_1", "avatar_2", "avatar_3", "avatar_4", "avatar_5", "avatar_6", "avatar_7")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_player_list, container, false)

        playerAdapter = PlayerAdapter(players) { player -> removePlayer(player) }

        playerRecyclerView = view.findViewById(R.id.playerRecyclerView)
        playerRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        playerRecyclerView.adapter = playerAdapter

        addPlayerButton = view.findViewById(R.id.addPlayerButton)
        startGameButton = view.findViewById(R.id.startGameButton)

        addPlayerButton.setOnClickListener {
            showAddPlayerDialog()
        }

        startGameButton.setOnClickListener {
            assignRoles()
            val bundle = Bundle().apply {
                putParcelableArrayList("playersList", ArrayList(players))
            }
            Navigation.findNavController(view).navigate(R.id.gameFragment, bundle)
        }

        return view
    }


    // Добавляем игрока в список
    private fun addPlayer(player: Player) {
        players.add(player)
        playerAdapter.notifyItemInserted(players.size - 1)
        checkStartGameCondition()
    }

    // Удаляем игрока из списка
    private fun removePlayer(player: Player) {
        val index = players.indexOf(player)
        if (index != -1) {
            players.removeAt(index)
            playerAdapter.notifyItemRemoved(index)
            checkStartGameCondition()
        }
    }

    // Проверяем количество игроков для активации кнопки "Начать игру"
    private fun checkStartGameCondition() {
        val startGameButton = view?.findViewById<Button>(R.id.startGameButton)
        startGameButton?.isEnabled = players.size >= 4
    }

    // Диалог для добавления игрока
    private fun showAddPlayerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_player, null)
        val playerNameInput = dialogView.findViewById<EditText>(R.id.playerNameInput)

        var avatarSpinner: Spinner = dialogView.findViewById(R.id.playerAvatarSpinner)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, avatars)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        avatarSpinner.adapter = adapter

        AlertDialog.Builder(requireContext())
            .setTitle("Добавить игрока")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val playerName = playerNameInput.text.toString()
                if (playerName.isNotEmpty()) {
                    val player = Player(playerName, avatarSpinner.selectedItem.toString())
                    addPlayer(player)
                } else {
                    Toast.makeText(requireContext(), "Имя не может быть пустым", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun assignRoles() {
        val mafiaCount = 1
        val detectiveCount = 1
        val doctorCount = 1

        for (i in 0 until mafiaCount) {
            getNextRandomPlayer().role = Role.MAFIA
        }
        for (i in 0 until detectiveCount) {
            val player = getNextRandomPlayer()
            player.role = Role.DETECTIVE
            player.checkedForDetective = true
        }
        for (i in 0 until doctorCount) {
            getNextRandomPlayer().role = Role.DOCTOR
        }
    }

    private fun getNextRandomPlayer(): Player {
        var randomPlayer: Player?
        do {
            val randomIndex = (0 until players.size).random()
            randomPlayer = players[randomIndex]
        } while (randomPlayer!!.role != Role.CIVILIAN)
        return randomPlayer
    }
}