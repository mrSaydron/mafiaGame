package ru.mrak.mafiagame.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.mrak.mafiagame.data.Player
import ru.mrak.mafiagame.adapter.PlayerAdapter
import ru.mrak.mafiagame.R
import ru.mrak.mafiagame.adapter.RoleAdapter
import ru.mrak.mafiagame.service.DataService
import ru.mrak.mafiagame.types.RoleType

class PlayerListFragment : Fragment() {

    private lateinit var playerAdapter: PlayerAdapter
    private lateinit var playerRecyclerView: RecyclerView

    private lateinit var addPlayerButton: Button
    private lateinit var startGameButton: Button
    private lateinit var settingsButton: ImageView

    private lateinit var roleAdapter: RoleAdapter
    private lateinit var rolesRecyclerView: RecyclerView


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

        roleAdapter = RoleAdapter(this)

        rolesRecyclerView = view.findViewById(R.id.rolesRecyclerView)
        rolesRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rolesRecyclerView.adapter = roleAdapter

        addPlayerButton = view.findViewById(R.id.addPlayerButton)
        startGameButton = view.findViewById(R.id.startGameButton)
        settingsButton = view.findViewById(R.id.settingsButton)

        addPlayerButton.setOnClickListener {
            showAddPlayerDialog()
        }

        startGameButton.setOnClickListener {
            resetPlayersAdditionalData()
            DataService.players = players
            roleAdapter.saveRoles()
            
            assignRoles()
            val bundle = Bundle().apply {
                putParcelableArrayList("playersList", ArrayList(players))
            }
            Navigation.findNavController(view).navigate(R.id.gameFragment, bundle)
        }

        settingsButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.settingsFragment)
        }

        loadPlayers()
        roleAdapter.loadRoles()

        return view
    }

    private fun resetPlayersAdditionalData() {
        players.forEach {
            it.role = RoleType.CIVILIAN
            it.isAlive = true
        }
    }

    private fun addPlayer(player: Player) {
        players.add(player)
        playerAdapter.notifyItemInserted(players.size - 1)
        roleAdapter.playerCount = players.size
        checkStartGameCondition()
    }

    private fun loadPlayers() {
        removeAllPlayers()
        DataService.players!!.forEach {
            addPlayer(it)
        }
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

    private fun removeAllPlayers() {
        playerAdapter.notifyItemRangeRemoved(0, players.size)
        players.clear()
        roleAdapter.playerCount = 0
        checkStartGameCondition()
    }

    // Проверяем количество игроков для активации кнопки "Начать игру"
    private fun checkStartGameCondition() {
        startGameButton.isEnabled = players.size >= 4
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
        val mafiaCount = roleAdapter.roles.find { it.role == RoleType.MAFIA }!!.count
        val detectiveCount = roleAdapter.roles.find { it.role == RoleType.DETECTIVE }!!.count
        val doctorCount = roleAdapter.roles.find { it.role == RoleType.DOCTOR }!!.count

        players.forEach { it.role = RoleType.CIVILIAN }

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
            val randomIndex = (0 until players.size).random()
            randomPlayer = players[randomIndex]
        } while (randomPlayer!!.role != RoleType.CIVILIAN)
        return randomPlayer
    }

}