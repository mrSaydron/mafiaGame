package ru.mrak.mafiagame.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.mrak.mafiagame.data.Player
import ru.mrak.mafiagame.R
import ru.mrak.mafiagame.service.DataService
import ru.mrak.mafiagame.types.RoleType

class PlayerGameAdapter(
    private val players: List<Player>,
    var onPlayerChoose: (Player) -> Unit,
) : RecyclerView.Adapter<PlayerGameAdapter.PlayerViewHolder>() {

    var acquaintancePlayer: Player? = null

    var showType: ShowType = ShowType.CIVILIAN
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player_vote, parent, false)
        return PlayerViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]
        holder.bind(player)
        holder.itemView.setOnClickListener {
            onPlayerChoose(player)
        }
    }

    override fun getItemCount(): Int = players.size

    class PlayerViewHolder(
        itemView: View,
        private val adapter: PlayerGameAdapter
    ) : RecyclerView.ViewHolder(itemView) {
        private val playerNameText: TextView = itemView.findViewById(R.id.playerNameText)
        private val playerAvatar: ImageView = itemView.findViewById(R.id.playerAvatar)
        private val playerRole: TextView = itemView.findViewById(R.id.playerRole)
        private val playerIsAlive: TextView = itemView.findViewById(R.id.playerIsAlive)

        fun bind(player: Player) {
            playerNameText.text = player.name

            // Загружаем аватар игрока
            val context = playerAvatar.context
            val resourceId = context.resources.getIdentifier(player.avatar, "drawable", context.packageName)
            playerAvatar.setImageResource(resourceId)
            playerRole.text = ""
            when (adapter.showType) {
                ShowType.CIVILIAN -> {}
                ShowType.DETECTIVE -> {
                    if (player.checkedForDetective) {
                        playerRole.text = player.role.toString()
                    }
                }
                ShowType.MAFIA -> {
                    if (player.role == RoleType.MAFIA) {
                        playerRole.text = player.role.toString()
                    }
                }
                ShowType.DOCTOR -> {
                    if (player.role == RoleType.DOCTOR) {
                        playerRole.text = player.role.toString()
                    }
                }
                ShowType.END_GAME -> {
                    playerRole.text = player.role.toString()
                }
                ShowType.ACQUAINTANCE -> {
                    if (player == adapter.acquaintancePlayer) {
                        playerRole.text = player.role.toString()
                    }
                }
            }
            if (DataService.settings!!.revealRole && !player.isAlive) {
                playerRole.text = player.role.toString()
            }
            playerIsAlive.text = ""
            itemView.setBackgroundColor(if (player.isAlive) 0xFFFFFFFF.toInt() else 0xFFC0C0C0.toInt())
        }
    }

    enum class ShowType {
        CIVILIAN,
        DETECTIVE,
        MAFIA,
        DOCTOR,
        END_GAME,
        ACQUAINTANCE,
    }
}
