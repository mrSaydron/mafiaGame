package ru.mrak.mafiagame.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.mrak.mafiagame.Phase
import ru.mrak.mafiagame.Player
import ru.mrak.mafiagame.R
import ru.mrak.mafiagame.RoleType
import ru.mrak.mafiagame.adapter.VotingAdapter
import ru.mrak.mafiagame.service.SpeechService

class GameFragment : Fragment() {

    private lateinit var gameStatusText: TextView
    private lateinit var nextPhaseButton: Button
    private lateinit var votingAdapter: VotingAdapter
    private lateinit var playersRecyclerView: RecyclerView

    private lateinit var playersList: MutableList<Player>
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
                nextPhaseButton.text = "Старт"
                nextPhaseButton.visibility = View.INVISIBLE

                countDown("Ночь начинается, все засыпают...", "Ночь начинается, все засыпают")
            }
            Phase.MAFIA -> {
                Log.i("updateGamePhase", "MAFIA")
                gameStatusText.text = "Мафия просыпается, выбери жертву"
                nextPhaseButton.text = "Дальше"
                nextPhaseButton.visibility = View.INVISIBLE
                votingAdapter.onPlayerChoose = { mafiaKillPlayer(it) }
                votingAdapter.showType = VotingAdapter.ShowType.MAFIA

                SpeechService.speak("Мафия просыпается")
            }
            Phase.END_MAFIA -> {
                Log.i("updateGamePhase", "END_MAFIA")
                gameStatusText.text = "Мафия засыпает"
                nextPhaseButton.visibility = View.INVISIBLE
                votingAdapter.onPlayerChoose = {}

                countDown("Мафия засыпает...", "Мафия засыпает")
            }
            Phase.DOCTOR -> {
                Log.i("updateGamePhase", "DOCTOR")
                gameStatusText.text = "Доктор просыпается. Выбери кого вылечить"
                nextPhaseButton.visibility = View.INVISIBLE
                votingAdapter.onPlayerChoose = { doctorTreatPlayer(it) }
                votingAdapter.showType = VotingAdapter.ShowType.DOCTOR

                SpeechService.speak("Доктор просыпается")
            }
            Phase.END_DOCTOR -> {
                Log.i("updateGamePhase", "END_DOCTOR")
                gameStatusText.text = "Доктор засыпает"
                nextPhaseButton.visibility = View.INVISIBLE
                votingAdapter.onPlayerChoose = {}

                countDown("Доктор засыпает...", "Доктор засыпает")
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

                SpeechService.speak("Детектив просыпается")
            }
            Phase.END_DETECTIVE -> {
                Log.i("updateGamePhase", "END_DETECTIVE")
                votingAdapter.onPlayerChoose = {}

                countDown("Детектив засыпает...", "Детектив засыпает")
            }
            Phase.NEWS -> {
                Log.i("updateGamePhase", "NEWS")
                val newsText = if (mafiaChosePlayer == doctorChosePlayer) {
                    "Ночью мафия совершила попытку убийства. Но ${mafiaChosePlayer?.name} был вовремя спасен доктором"
                } else {
                    mafiaChosePlayer?.isAlive = false
                    "Ночью мафией был убит ${mafiaChosePlayer?.name}. Он оставил предсмертное сообщение:"
                }
                gameStatusText.text = "Наступил день, все просыпаются. $newsText"
                SpeechService.speak("Наступил день, все просыпаются. $newsText")

                votingAdapter.notifyDataSetChanged()
                nextPhaseButton.text = "Дальше"
                nextPhaseButton.visibility = View.VISIBLE

                mafiaChosePlayer = null
                doctorChosePlayer = null

                if (!checkWinConditionInNewsPhase()) {
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
                gameStatusText.text = "Горожанами был выбран ${citizenChosePlayer?.name.toString()}. Ваше последнее слово:"
                nextPhaseButton.visibility = View.VISIBLE
                nextPhaseButton.text = "Дальше"

                SpeechService.speak("Горожанами был выбран ${citizenChosePlayer?.name.toString()}. Ваше последнее слово:")
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
                    SpeechService.speak("Горожане победили")
                } else if (getMafiaCount() >= getCivilianCount()) {
                    gameStatusText.text = "Мафия победила"
                    SpeechService.speak("Мафия победила")
                }
            }
        }
    }

    private fun countDown(text: String, speakText: String) {
        lifecycleScope.launch {
            gameStatusText.text = text
            SpeechService.speak(speakText)
            for (i in 5 downTo 1) {
                if (!SpeechService.isSpeaking()) {
                    SpeechService.speak(i.toString())
                }
                gameStatusText.text = "$text $i"
                delay(1000)
            }

            votingAdapter.showType = VotingAdapter.ShowType.CIVILIAN
            nextPhase()
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

    private fun checkWinConditionInNewsPhase(): Boolean {
        if (getMafiaCount() == 0 || getMafiaCount() >= getCivilianCount() + 1) {
            currentPhase = Phase.END_GAME
            updateGamePhase()
            return true
        }
        return false
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

}