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
import ru.mrak.mafiagame.types.PhaseType
import ru.mrak.mafiagame.data.Player
import ru.mrak.mafiagame.R
import ru.mrak.mafiagame.types.RoleType
import ru.mrak.mafiagame.adapter.PlayerGameAdapter
import ru.mrak.mafiagame.data.Game
import ru.mrak.mafiagame.service.DataService
import ru.mrak.mafiagame.service.SpeechService

class GameFragment : Fragment() {

    private lateinit var gameStatusText: TextView
    private lateinit var nextPhaseButton: Button
    private lateinit var playerGameAdapter: PlayerGameAdapter
    private lateinit var playersRecyclerView: RecyclerView

    private val game: Game = Game()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game, container, false)

        gameStatusText = view.findViewById(R.id.gameStatusText)
        nextPhaseButton = view.findViewById(R.id.nextPhaseButton)
        playersRecyclerView = view.findViewById(R.id.playersRecyclerView)

        // Получаем список игроков из предыдущего фрагмента
        game.players = arguments?.getParcelableArrayList("playersList") ?: mutableListOf()

        playerGameAdapter = PlayerGameAdapter(game.players) {}

        playersRecyclerView.layoutManager = LinearLayoutManager(context)
        playersRecyclerView.adapter = playerGameAdapter

        nextPhaseButton.setOnClickListener {
            nextPhase()
        }

        // Инициализация первой фазы
        updateGamePhase()

        return view
    }

    private fun updateGamePhase() {
        when (game.currentPhaseType) {
            PhaseType.START_GAME -> {
                Log.i("updateGamePhase", "START_GAME")
                gameStatusText.text = "Начало игры"
                nextPhaseButton.text = "Старт"
                nextPhaseButton.visibility = View.VISIBLE
            }
            PhaseType.START_NIGHT -> {
                Log.i("updateGamePhase", "START_NIGHT")
                nextPhaseButton.text = "Старт"
                nextPhaseButton.visibility = View.INVISIBLE

                countDown("Ночь начинается, все засыпают...", "Ночь начинается, все засыпают")
            }
            PhaseType.ACQUAINTANCE -> {
                Log.i("updateGamePhase", "ACQUAINTANCE")
                gameStatusText.text = "Давайте знакомиться"
                acquaintancePlayers()
            }
            PhaseType.MAFIA -> {
                Log.i("updateGamePhase", "MAFIA")
                gameStatusText.text = "Мафия просыпается, выбери жертву"
                nextPhaseButton.text = "Дальше"
                nextPhaseButton.visibility = View.INVISIBLE
                playerGameAdapter.onPlayerChoose = { mafiaKillPlayer(it) }
                playerGameAdapter.showType = PlayerGameAdapter.ShowType.MAFIA

                SpeechService.speak("Мафия просыпается")
            }
            PhaseType.END_MAFIA -> {
                Log.i("updateGamePhase", "END_MAFIA")
                gameStatusText.text = "Мафия засыпает"
                nextPhaseButton.visibility = View.INVISIBLE
                playerGameAdapter.onPlayerChoose = {}

                countDown("Мафия засыпает...", "Мафия засыпает")
            }
            PhaseType.DOCTOR -> {
                Log.i("updateGamePhase", "DOCTOR")
                gameStatusText.text = "Доктор просыпается. Выбери кого вылечить"
                nextPhaseButton.visibility = View.INVISIBLE
                playerGameAdapter.onPlayerChoose = { doctorTreatPlayer(it) }
                playerGameAdapter.showType = PlayerGameAdapter.ShowType.DOCTOR

                SpeechService.speak("Доктор просыпается")
            }
            PhaseType.END_DOCTOR -> {
                Log.i("updateGamePhase", "END_DOCTOR")
                gameStatusText.text = "Доктор засыпает"
                nextPhaseButton.visibility = View.INVISIBLE
                playerGameAdapter.onPlayerChoose = {}

                countDown("Доктор засыпает...", "Доктор засыпает")
            }
            PhaseType.DETECTIVE -> {
                Log.i("updateGamePhase", "DETECTIVE")
                gameStatusText.text = "Детектив просыпается"
                nextPhaseButton.visibility = View.INVISIBLE
                playerGameAdapter.onPlayerChoose = { detectiveCheckPlayer(it) }
                playerGameAdapter.showType = PlayerGameAdapter.ShowType.DETECTIVE

                if (getNotCheckedDetectiveCount() == 0) {
                    nextPhaseButton.visibility = View.VISIBLE
                    nextPhaseButton.text = "Дальше"
                }

                SpeechService.speak("Детектив просыпается")
            }
            PhaseType.END_DETECTIVE -> {
                Log.i("updateGamePhase", "END_DETECTIVE")
                playerGameAdapter.onPlayerChoose = {}

                countDown("Детектив засыпает...", "Детектив засыпает")
            }
            PhaseType.NEWS -> {
                Log.i("updateGamePhase", "NEWS")
                val newsText = if (game.mafiaChose == game.doctorChose) {
                    "Ночью мафия совершила попытку убийства. Но игрок ${game.mafiaChose?.name} был вовремя спасен доктором"
                } else {
                    game.mafiaChose?.isAlive = false
                    "Ночью мафией был убит игрок: ${game.mafiaChose?.name}. Игрок оставил предсмертное сообщение:"
                }
                gameStatusText.text = "Наступил день, все просыпаются. $newsText"
                SpeechService.speak("Наступил день, все просыпаются. $newsText")

                playerGameAdapter.notifyDataSetChanged()
                nextPhaseButton.text = "Дальше"
                nextPhaseButton.visibility = View.VISIBLE

                game.mafiaChose = null
                game.doctorChose = null

                if (!checkWinConditionInNewsPhase()) {
                    lifecycleScope.launch {
                        delay(10000)
                        if (game.currentPhaseType == PhaseType.NEWS) {
                            nextPhase()
                        }
                    }
                }

            }
            PhaseType.VOTE -> {
                Log.i("updateGamePhase", "VOTE")
                gameStatusText.text = "Голосование"
                nextPhaseButton.visibility = View.INVISIBLE
                playerGameAdapter.onPlayerChoose = { voteForPlayer(it) }
            }
            PhaseType.AFTER_VOTE -> {
                Log.i("updateGamePhase", "AFTER_VOTE")
                gameStatusText.text = "Горожанами был выбран ${game.citizenChose?.name.toString()}. Ваше последнее слово:"
                nextPhaseButton.visibility = View.VISIBLE
                nextPhaseButton.text = "Дальше"

                SpeechService.speak("Горожанами был выбран ${game.citizenChose?.name.toString()}. Ваше последнее слово:")
            }
            PhaseType.END_GAME -> {
                Log.i("updateGamePhase", "END_GAME")
                gameStatusText.text = "Результат игры"
                nextPhaseButton.text = "Новая игра"
                nextPhaseButton.visibility = View.VISIBLE
                playerGameAdapter.onPlayerChoose = {}
                nextPhaseButton.setOnClickListener { findNavController().navigate(R.id.playerListFragment) }
                playerGameAdapter.showType = PlayerGameAdapter.ShowType.END_GAME

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

    private fun acquaintancePlayers() {
        lifecycleScope.launch {
            for (player in game.players) {
                playerGameAdapter.acquaintancePlayer = player
                playerGameAdapter.showType = PlayerGameAdapter.ShowType.ACQUAINTANCE
                SpeechService.speakAndWait("Просыпается ${player.name}")

                delay(2000)

                SpeechService.speakAndWait("${player.name} засыпает")
                playerGameAdapter.showType = PlayerGameAdapter.ShowType.CIVILIAN
                delay(1000)
            }
            playerGameAdapter.showType = PlayerGameAdapter.ShowType.CIVILIAN
            game.acquaintanceAlready = true
            nextPhase()
        }
    }

    private fun countDown(text: String, speakText: String) {
        lifecycleScope.launch {
            gameStatusText.text = text
            SpeechService.speakAndWait(speakText)
            delay(2000)
            playerGameAdapter.showType = PlayerGameAdapter.ShowType.CIVILIAN
            nextPhase()
        }
    }

    private fun doctorTreatPlayer(player: Player) {
        if (!player.isAlive) {
            Toast.makeText(activity, "Этот игрок уже мертв", Toast.LENGTH_SHORT).show()
        } else if (!DataService.settings!!.doctorHealSame && game.doctorChoseLast == player) {
            Toast.makeText(activity, "Нельзя лечить одного игрока дважды", Toast.LENGTH_SHORT).show()
        } else if (!DataService.settings!!.doctorSelfHeal && getDoctor() == player) {
            Toast.makeText(activity, "Доктор не может лечить себя самого", Toast.LENGTH_SHORT).show()
        } else {
            game.doctorChose = player
            game.doctorChoseLast = player
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
            playerGameAdapter.notifyDataSetChanged()
            nextPhase()
        }
    }

    private fun nextPhase() {
        do {
            game.currentPhaseType = game.currentPhaseType.nextPhaseType!!
        } while (!game.currentPhaseType.canUse(game))
        lifecycleScope.launch() { updateGamePhase() }
    }

    private fun voteForPlayer(player: Player) {
        if (!player.isAlive) {
            Toast.makeText(activity, "Этот игрок уже мёртв", Toast.LENGTH_SHORT).show()
        } else {
            player.isAlive = false
            game.citizenChose = player
            playerGameAdapter.notifyDataSetChanged()
            if (!checkWinCondition()) {
                nextPhase()
            }
        }
    }

    private fun mafiaKillPlayer(player: Player) {
        if (getMafiaCount() == 1 && player.role == RoleType.MAFIA) {
            Toast.makeText(activity, "Единственный мафиозий не может убить сам себя", Toast.LENGTH_SHORT).show()
        } else {
            game.mafiaChose = player
            nextPhase()
        }
    }

    private fun checkWinConditionInNewsPhase(): Boolean {
        if (getMafiaCount() == 0 || getMafiaCount() >= getCivilianCount() + 1) {
            game.currentPhaseType = PhaseType.END_GAME
            updateGamePhase()
            return true
        }
        return false
    }

    private fun checkWinCondition(): Boolean {
        if (getMafiaCount() == 0 || getMafiaCount() >= getCivilianCount()) {
            game.currentPhaseType = PhaseType.END_GAME
            updateGamePhase()
            return true
        }
        return false
    }

    private fun getMafiaCount() = game.players.count { it.role == RoleType.MAFIA && it.isAlive }
    private fun getCivilianCount() = game.players.count { it.role != RoleType.MAFIA && it.isAlive }
    private fun getDoctorCount() = game.players.count { it.role == RoleType.DOCTOR && it.isAlive }
    private fun getDetectiveCount() = game.players.count { it.role == RoleType.DETECTIVE && it.isAlive }
    private fun getNotCheckedDetectiveCount() = game.players.count { it.role != RoleType.DETECTIVE && it.isAlive && !it.checkedForDetective }
    private fun getDoctor() = game.players.firstOrNull { it.role == RoleType.DOCTOR }

}