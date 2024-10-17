package ru.mrak.mafiagame

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private lateinit var playersList: MutableList<Player>
    private lateinit var gameStatusText: TextView
    private lateinit var nextPhaseButton: Button
    private lateinit var votingAdapter: VotingAdapter
    private lateinit var playersRecyclerView: RecyclerView
    private var currentPhase: Phases = Phases.START_NIGHT
    private var isVotingPhase: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game, container, false)

        gameStatusText = view.findViewById(R.id.gameStatusText)
        nextPhaseButton = view.findViewById(R.id.nextPhaseButton)
        playersRecyclerView = view.findViewById(R.id.playersRecyclerView)

        // Получаем список игроков из предыдущего фрагмента
        playersList = arguments?.getParcelableArrayList("playersList") ?: mutableListOf()

        votingAdapter = VotingAdapter(playersList) { votedPlayer ->
            onPlayerRemoved(votedPlayer)
        }

        playersRecyclerView.layoutManager = LinearLayoutManager(context)
        playersRecyclerView.adapter = votingAdapter

        nextPhaseButton.setOnClickListener {
            nextPhase()
        }

        // Инициализация первой фазы
        updateGamePhase()

        return view
    }

    private fun updateGamePhase() {
        when (currentPhase) {
            Phases.START_NIGHT -> {
                gameStatusText.text = "Ночь начинается, все засыпают..."
                lifecycleScope.launch {
                    for (i in 5 downTo 1) {
                        gameStatusText.text = "Ночь начинается, все засыпают... $i"
                        delay(1000)
                    }
                }
                nextPhase()
            }
            Phases.MAFIA -> {
                gameStatusText.text = "Мафия просыпается"
            }
            Phases.END_MAFIA -> {
                gameStatusText.text = "Мафия засыпает"
            }
            Phases.DOCTOR -> {
                gameStatusText.text = "Доктор просыпается"
            }
            Phases.END_DOCTOR -> {
                gameStatusText.text = "Доктор засыпает"
            }
            Phases.DETECTIVE -> {
                gameStatusText.text = "Детектив просыпается"
            }
            Phases.END_DETECTIVE -> {
                gameStatusText.text = "Детектив засыпает"
            }
            Phases.NEWS -> {
                gameStatusText.text = "Наступил день, все просыпаются. Ночью случилось..."
            }
            Phases.VOTE -> {
                gameStatusText.text = "Голосование"
                isVotingPhase = true
                nextPhaseButton.visibility = View.GONE
                votingAdapter.canRemovePlayer = true
            }
        }
    }

    private fun nextPhase() {
        val n = if (currentPhase.ordinal == Phases.entries.size - 1) 0 else currentPhase.ordinal + 1
        currentPhase = Phases.entries.toTypedArray()[n]
        lifecycleScope.launch(Dispatchers.Default) { updateGamePhase() }
    }

    // Когда игрок удален
    private fun onPlayerRemoved(player: Player) {
        // Убираем игрока из списка активных
        playersList.remove(player)
        gameStatusText.text = "${player.name} (${player.role}) has been eliminated!"
        checkWinCondition()
        isVotingPhase = false
        votingAdapter.notifyDataSetChanged()
        nextPhaseButton.visibility = View.VISIBLE
//        currentPhase = 0
        votingAdapter.canRemovePlayer = false
        updateGamePhase()
    }

    private fun showEndGameDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Game Over")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                // После окончания игры можно начать новую или вернуться в главное меню
                findNavController().navigate(R.id.registrationFragment)
            }
            .show()
    }

    private fun checkWinCondition() {
        val mafiaCount = playersList.count { it.role == Role.MAFIA }
        val civiliansCount = playersList.size - mafiaCount

        if (mafiaCount == 0) {
            showEndGameDialog("Civilians win!")
        } else if (mafiaCount >= civiliansCount) {
            showEndGameDialog("Mafia wins!")
        }
    }

    enum class Phases {
        START_NIGHT,
        MAFIA,
        END_MAFIA,
        DOCTOR,
        END_DOCTOR,
        DETECTIVE,
        END_DETECTIVE,
        NEWS,
        VOTE,
    }
}