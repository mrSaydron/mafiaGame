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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.mrak.mafiagame.data.Player
import ru.mrak.mafiagame.adapter.PlayerAdapter
import ru.mrak.mafiagame.R
import ru.mrak.mafiagame.adapter.AvatarAdapter
import ru.mrak.mafiagame.adapter.RoleAdapter
import ru.mrak.mafiagame.service.DataService
import ru.mrak.mafiagame.types.RoleType
import java.util.Collections

class PlayerListFragment : Fragment() {

    private lateinit var playerAdapter: PlayerAdapter
    private lateinit var playerRecyclerView: RecyclerView

    private lateinit var addPlayerButton: Button
    private lateinit var startGameButton: Button
    private lateinit var settingsButton: ImageView

    private lateinit var roleAdapter: RoleAdapter
    private lateinit var rolesRecyclerView: RecyclerView

    private val avatars = listOf(
        R.drawable.avatar_1,
        R.drawable.avatar_2,
        R.drawable.avatar_3,
        R.drawable.avatar_4,
        R.drawable.avatar_5,
        R.drawable.avatar_6,
        R.drawable.avatar_7
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_player_list, container, false)

        roleAdapter = RoleAdapter()

        rolesRecyclerView = view.findViewById(R.id.rolesRecyclerView)
        rolesRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rolesRecyclerView.adapter = roleAdapter

        playerAdapter = PlayerAdapter(
            { showEditPlayerDialog(it) },
            {
                roleAdapter.playerCount = it.size
                checkStartGameCondition(it)
            }
        )
        playerRecyclerView = view.findViewById(R.id.playerRecyclerView)
        playerRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        playerRecyclerView.adapter = playerAdapter
        ItemTouchHelper(playerAdapter.itemTouchHelper).attachToRecyclerView(playerRecyclerView)

        addPlayerButton = view.findViewById(R.id.addPlayerButton)
        startGameButton = view.findViewById(R.id.startGameButton)
        settingsButton = view.findViewById(R.id.settingsButton)

        addPlayerButton.setOnClickListener {
            showAddPlayerDialog()
        }

        startGameButton.setOnClickListener {
            playerAdapter.resetPlayersAdditionalData()
            
            assignRoles(playerAdapter.players)
            val bundle = Bundle().apply {
                putParcelableArrayList("playersList", ArrayList(playerAdapter.players))
            }
            Navigation.findNavController(view).navigate(R.id.gameFragment, bundle)
        }

        settingsButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.settingsFragment)
        }

        checkStartGameCondition(playerAdapter.players)

        return view
    }

    // Проверяем количество игроков для активации кнопки "Начать игру"
    private fun checkStartGameCondition(players: List<Player>) {
        startGameButton.isEnabled = players.size >= 4
    }

    private fun showAddPlayerDialog() {
        var avatarId: Int? = null

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_player, null)
        val playerNameInput = dialogView.findViewById<EditText>(R.id.playerNameInput)
        val avatarImageView = dialogView.findViewById<ImageView>(R.id.avatarImageView)
        avatarImageView.visibility = View.GONE

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.avatarRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val usingAvatars = playerAdapter.players.map { it.avatarId }.toSet()
        val leftAvatars = avatars.filter { !usingAvatars.contains(it) }

        val avatarAdapter = AvatarAdapter(leftAvatars) { selectedAvatarResId ->
            avatarImageView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            avatarImageView.setImageResource(selectedAvatarResId)
            avatarId = selectedAvatarResId
        }
        recyclerView.adapter = avatarAdapter

        avatarImageView.setOnClickListener {
            avatarImageView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            avatarId = null
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Добавить игрока")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val playerName = playerNameInput.text.toString()
                if (playerName.isEmpty()) {
                    Toast.makeText(requireContext(), "Имя не может быть пустым", Toast.LENGTH_SHORT).show()
                } else if (avatarId == null) {
                    Toast.makeText(requireContext(), "Выберите аватарку", Toast.LENGTH_SHORT).show()
                } else {
                    val player = Player(playerName, avatarId!!)
                    playerAdapter.addPlayer(player)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showEditPlayerDialog(player: Player) {
        var avatarId: Int? = player.avatarId

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_player, null)
        val playerNameInput = dialogView.findViewById<EditText>(R.id.playerNameInput)
        playerNameInput.setText(player.name)

        val avatarImageView = dialogView.findViewById<ImageView>(R.id.avatarImageView)
        avatarImageView.setImageResource(avatarId ?: 0)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.avatarRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val usingAvatars = playerAdapter.players.map { it.avatarId }.toSet()
        val leftAvatars = avatars.filter { !usingAvatars.contains(it) || it == player.avatarId}

        val avatarAdapter = AvatarAdapter(leftAvatars) { selectedAvatarResId ->
            avatarImageView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            avatarImageView.setImageResource(selectedAvatarResId)
            avatarId = selectedAvatarResId
        }
        recyclerView.adapter = avatarAdapter
        recyclerView.visibility = View.GONE

        avatarImageView.setOnClickListener {
            avatarImageView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            avatarId = null
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Редактирование игрока")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val playerName = playerNameInput.text.toString()
                if (playerName.isEmpty()) {
                    Toast.makeText(requireContext(), "Имя не может быть пустым", Toast.LENGTH_SHORT).show()
                } else if (avatarId == null) {
                    Toast.makeText(requireContext(), "Выберите аватарку", Toast.LENGTH_SHORT).show()
                } else {
                    val newPlayer = Player(playerName, avatarId!!)
                    playerAdapter.replacePlayer(player, newPlayer)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun assignRoles(players: List<Player>) {
        val mafiaCount = roleAdapter.roles.find { it.role == RoleType.MAFIA }!!.count
        val detectiveCount = roleAdapter.roles.find { it.role == RoleType.DETECTIVE }!!.count
        val doctorCount = roleAdapter.roles.find { it.role == RoleType.DOCTOR }!!.count

        players.forEach { it.role = RoleType.CIVILIAN }

        for (i in 0 until mafiaCount) {
            getNextRandomPlayer(players).role = RoleType.MAFIA
        }
        for (i in 0 until detectiveCount) {
            val player = getNextRandomPlayer(players)
            player.role = RoleType.DETECTIVE
            player.checkedForDetective = true
        }
        for (i in 0 until doctorCount) {
            getNextRandomPlayer(players).role = RoleType.DOCTOR
        }
    }

    private fun getNextRandomPlayer(players: List<Player>): Player {
        var randomPlayer: Player?
        do {
            randomPlayer = players.random()
        } while (randomPlayer!!.role != RoleType.CIVILIAN)
        return randomPlayer
    }


}