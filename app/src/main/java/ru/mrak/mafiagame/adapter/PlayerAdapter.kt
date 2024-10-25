package ru.mrak.mafiagame.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.mrak.mafiagame.data.Player
import ru.mrak.mafiagame.R

class PlayerAdapter(
    private val players: List<Player>,
    private val onRemovePlayer: (Player) -> Unit
) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerName: TextView = itemView.findViewById(R.id.playerNameText)
        private val removeButton: Button = itemView.findViewById(R.id.removePlayerButton)
        private val playerAvatar: ImageView = itemView.findViewById(R.id.playerAvatar)

        fun bind(player: Player, onRemovePlayer: (Player) -> Unit) {
            playerName.text = player.name
            removeButton.setOnClickListener {
                onRemovePlayer(player)
            }
            val context = playerAvatar.context
            val resourceId = context.resources.getIdentifier(player.avatar, "drawable", context.packageName)
            playerAvatar.setImageResource(resourceId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]
        holder.bind(player, onRemovePlayer)
    }

    override fun getItemCount(): Int = players.size
}
