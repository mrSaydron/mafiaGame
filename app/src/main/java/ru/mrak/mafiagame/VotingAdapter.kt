package ru.mrak.mafiagame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VotingAdapter(
    private val players: MutableList<Player>,
    var canRemovePlayer: Boolean = false,
    private val onPlayerRemoved: (Player) -> Unit,
) : RecyclerView.Adapter<VotingAdapter.PlayerViewHolder>() {

    private val votes = mutableMapOf<Player, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player_vote, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]
        holder.bind(player)
        holder.itemView.setOnClickListener {
            if (canRemovePlayer) {
                onPlayerRemoved(player)
                removePlayer(player)
            }
        }
    }

    override fun getItemCount(): Int = players.size

    // Метод для удаления игрока
    private fun removePlayer(player: Player) {
        val index = players.indexOf(player)
        if (index != -1) {
            players.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerNameText: TextView = itemView.findViewById(R.id.playerNameText)
        private val playerAvatar: ImageView = itemView.findViewById(R.id.playerAvatar)

        fun bind(player: Player) {
            playerNameText.text = player.name

            // Загружаем аватар игрока
            val context = playerAvatar.context
            val resourceId = context.resources.getIdentifier(player.avatar, "drawable", context.packageName)
            playerAvatar.setImageResource(resourceId)
        }
    }
}
