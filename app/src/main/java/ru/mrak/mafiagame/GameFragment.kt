package ru.mrak.mafiagame

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private lateinit var playersList: MutableList<Player>
    private lateinit var gameStatusText: TextView
    private lateinit var nextPhaseButton: Button
    private lateinit var votingAdapter: VotingAdapter
    private lateinit var playersRecyclerView: RecyclerView

    private var currentPhase: Phase = Phase.START_NIGHT
    private var mafiaChosePlayer: Player? = null
    private var doctorChosePlayer: Player? = null
    private var citizenChosePlayer: Player? = null

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

        votingAdapter = VotingAdapter(playersList) {}

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
            Phase.START_GAME -> {
                Log.i("updateGamePhase", "START_GAME")
                gameStatusText.text = "Начало игры"
                nextPhaseButton.text = "Старт"
                nextPhaseButton.visibility = View.VISIBLE
            }
            Phase.START_NIGHT -> {
                Log.i("updateGamePhase", "START_NIGHT")
                gameStatusText.text = "Ночь начинается, все засыпают..."
                nextPhaseButton.text = "Старт"
                nextPhaseButton.visibility = View.INVISIBLE

                lifecycleScope.launch {
                    for (i in 5 downTo 1) {
                        gameStatusText.text = "Ночь начинается, все засыпают... $i"
                        delay(1000)
                    }
                    nextPhase()
                }
            }
            Phase.MAFIA -> {
                Log.i("updateGamePhase", "MAFIA")
                gameStatusText.text = "Мафия просыпается, выбери жертву"
                nextPhaseButton.text = "Дальше"
                nextPhaseButton.visibility = View.INVISIBLE
                votingAdapter.onPlayerChoose = { mafiaKillPlayer(it) }
                votingAdapter.showType = VotingAdapter.ShowType.MAFIA
            }
            Phase.END_MAFIA -> {
                Log.i("updateGamePhase", "END_MAFIA")
                gameStatusText.text = "Мафия засыпает"
                nextPhaseButton.visibility = View.INVISIBLE
                votingAdapter.onPlayerChoose = {}

                lifecycleScope.launch {
                    for (i in 5 downTo 1) {
                        gameStatusText.text = "Мафия засыпает... $i"
                        delay(1000)
                    }
                    votingAdapter.showType = VotingAdapter.ShowType.CIVILIAN
                    nextPhase()
                }
            }
            Phase.DOCTOR -> {
                Log.i("updateGamePhase", "DOCTOR")
                gameStatusText.text = "Доктор просыпается. Выбери кого вылечить"
                nextPhaseButton.visibility = View.INVISIBLE
                votingAdapter.onPlayerChoose = { doctorTreatPlayer(it) }
                votingAdapter.showType = VotingAdapter.ShowType.DOCTOR
            }
            Phase.END_DOCTOR -> {
                Log.i("updateGamePhase", "END_DOCTOR")
                gameStatusText.text = "Доктор засыпает"
                nextPhaseButton.visibility = View.INVISIBLE
                votingAdapter.onPlayerChoose = {}

                lifecycleScope.launch {
                    for (i in 5 downTo 1) {
                        gameStatusText.text = "Доктор засыпает... $i"
                        delay(1000)
                    }
                    votingAdapter.showType = VotingAdapter.ShowType.CIVILIAN
                    nextPhase()
                }
            }
            Phase.DETECTIVE -> {
                Log.i("updateGamePhase", "DETECTIVE")
                gameStatusText.text = "Детектив просыпается"
                nextPhaseButton.visibility = View.INVISIBLE
                votingAdapter.onPlayerChoose = { detectiveCheckPlayer(it) }
                votingAdapter.showType = VotingAdapter.ShowType.DETECTIVE

                if (getNotCheckedDetectiveCount() == 0) {
                    nextPhaseButton.visibility = View.VISIBLE
                    nextPhaseButton.text = "Дальше"
                }
            }
            Phase.END_DETECTIVE -> {
                Log.i("updateGamePhase", "END_DETECTIVE")
                votingAdapter.onPlayerChoose = {}

                gameStatusText.text = "Детектив засыпает"
                lifecycleScope.launch {
                    for (i in 5 downTo 1) {
                        gameStatusText.text = "Детектив засыпает... $i"
                        delay(1000)
                    }

                    votingAdapter.showType = VotingAdapter.ShowType.CIVILIAN
                    nextPhase()
                }
            }
            Phase.NEWS -> {
                Log.i("updateGamePhase", "NEWS")
                val newsText = if (mafiaChosePlayer == doctorChosePlayer) {
                    "Ночью мафия совершила покушение на ${mafiaChosePlayer?.name}, но доктор успел вовремя и спас его"
                } else {
                    mafiaChosePlayer?.isAlive = false
                    "Ночью жертвой мафии стал ${mafiaChosePlayer?.name}. Он оставил предсмертное сообщение:"
                }
                gameStatusText.text = "Наступил день, все просыпаются. $newsText"

                votingAdapter.notifyDataSetChanged()
                nextPhaseButton.text = "Дальше"
                nextPhaseButton.visibility = View.VISIBLE

                mafiaChosePlayer = null
                doctorChosePlayer = null

                if (!checkWinCondition()) {
                    lifecycleScope.launch {
                        delay(10000)
                        if (currentPhase == Phase.NEWS) {
                            nextPhase()
                        }
                    }
                }

            }
            Phase.VOTE -> {
                Log.i("updateGamePhase", "VOTE")
                gameStatusText.text = "Голосование"
                nextPhaseButton.visibility = View.INVISIBLE
                votingAdapter.onPlayerChoose = { voteForPlayer(it) }
            }
            Phase.AFTER_VOTE -> {
                Log.i("updateGamePhase", "AFTER_VOTE")
                gameStatusText.text = "Горожане выбрали ${citizenChosePlayer?.name.toString()}. Ваше последнее слово:"
                nextPhaseButton.visibility = View.VISIBLE
                nextPhaseButton.text = "Дальше"
            }
            Phase.END_GAME -> {
                Log.i("updateGamePhase", "END_GAME")
                gameStatusText.text = "Результат игры"
                nextPhaseButton.text = "Новая игра"
                nextPhaseButton.visibility = View.VISIBLE
                votingAdapter.onPlayerChoose = {}
                nextPhaseButton.setOnClickListener { findNavController().navigate(R.id.playerListFragment) }
                votingAdapter.showType = VotingAdapter.ShowType.END_GAME

                if (getMafiaCount() == 0) {
                    gameStatusText.text = "Горожане победили"
                } else if (getMafiaCount() >= getCivilianCount()) {
                    gameStatusText.text = "Мафия победила"
                }
            }
        }
    }

    private fun doctorTreatPlayer(player: Player) {
        if (!player.isAlive) {
            Toast.makeText(activity, "Этот игрок уже мертв", Toast.LENGTH_SHORT).show()
        } else {
            doctorChosePlayer = player
            nextPhase()
        }
    }

    private fun detectiveCheckPlayer(player: Player) {
        if (player.role == RoleType.DETECTIVE) {
            Toast.makeText(activity, "Незачем проверять самого себя", Toast.LENGTH_SHORT).show()
        } else if (!player.isAlive) {
            Toast.makeText(activity, "Этот игрок мертв", Toast.LENGTH_SHORT).show()
        } else if (player.checkedForDetective) {
            Toast.makeText(activity, "Этот игрок уже был проверен", Toast.LENGTH_SHORT).show()
        } else {
            player.checkedForDetective = true
            votingAdapter.notifyDataSetChanged()
            nextPhase()
        }
    }

    private fun nextPhase() {
        do {
            currentPhase = currentPhase.nextPhase!!
        } while (!currentPhase.canUse(playersList))
        lifecycleScope.launch() { updateGamePhase() }
    }

    private fun voteForPlayer(player: Player) {
        if (!player.isAlive) {
            Toast.makeText(activity, "Этот игрок уже мёртв", Toast.LENGTH_SHORT).show()
        } else {
            player.isAlive = false
            citizenChosePlayer = player
            votingAdapter.notifyDataSetChanged()
            if (!checkWinCondition()) {
                nextPhase()
            }
        }
    }

    private fun mafiaKillPlayer(player: Player) {
        if (getMafiaCount() == 1 && player.role == RoleType.MAFIA) {
            Toast.makeText(activity, "Единственный мафиозий не может убить сам себя", Toast.LENGTH_SHORT).show()
        } else {
            mafiaChosePlayer = player
            nextPhase()
        }
    }

    private fun checkWinCondition(): Boolean {
        if (getMafiaCount() == 0 || getMafiaCount() >= getCivilianCount()) {
            currentPhase = Phase.END_GAME
            updateGamePhase()
            return true
        }
        return false
    }

    private fun getMafiaCount() = playersList.count { it.role == RoleType.MAFIA && it.isAlive }
    private fun getCivilianCount() = playersList.count { it.role != RoleType.MAFIA && it.isAlive }
    private fun getDoctorCount() = playersList.count { it.role == RoleType.DOCTOR && it.isAlive }
    private fun getDetectiveCount() = playersList.count { it.role == RoleType.DETECTIVE && it.isAlive }
    private fun getNotCheckedDetectiveCount() = playersList.count { it.role != RoleType.DETECTIVE && it.isAlive && !it.checkedForDetective }

    enum class Phase(
        val canUse: (List<Player>) -> Boolean,
        var nextPhase: Phase? = null,
    ) {
        START_GAME({ false }),
        START_NIGHT({ true }),
        MAFIA({ it.count{ player -> player.role == RoleType.MAFIA && player.isAlive } > 0 }),
        END_MAFIA({ it.count{ player -> player.role == RoleType.MAFIA && player.isAlive } > 0 }),
        DOCTOR({ it.count { player -> player.role == RoleType.DOCTOR && player.isAlive } > 0 }),
        END_DOCTOR({ it.count { player -> player.role == RoleType.DOCTOR && player.isAlive } > 0 }),
        DETECTIVE({ it.count { player -> player.role == RoleType.DETECTIVE && player.isAlive } > 0 }),
        END_DETECTIVE({ it.count { player -> player.role == RoleType.DETECTIVE && player.isAlive } > 0 }),
        NEWS({ true }),
        VOTE({ true }),
        AFTER_VOTE({ true }),
        END_GAME({ false })
        ;

        companion object {
            init {
                START_GAME.nextPhase = START_NIGHT
                START_NIGHT.nextPhase = MAFIA
                MAFIA.nextPhase = END_MAFIA
                END_MAFIA.nextPhase = DOCTOR
                DOCTOR.nextPhase = END_DOCTOR
                END_DOCTOR.nextPhase = DETECTIVE
                DETECTIVE.nextPhase = END_DETECTIVE
                END_DETECTIVE.nextPhase = NEWS
                NEWS.nextPhase = VOTE
                VOTE.nextPhase = AFTER_VOTE
                AFTER_VOTE.nextPhase = START_NIGHT
            }
        }
    }

}