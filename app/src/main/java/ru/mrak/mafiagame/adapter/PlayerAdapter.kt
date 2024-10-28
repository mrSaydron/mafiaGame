package ru.mrak.mafiagame.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.mrak.mafiagame.data.Player
import ru.mrak.mafiagame.R

class PlayerAdapter(
    private val players: List<Player>,
    private val onPlayerClickListener: (Player) -> Unit
) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]
        holder.bind(player, onPlayerClickListener)
    }

    override fun getItemCount(): Int = players.size

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerName: TextView = itemView.findViewById(R.id.playerNameText)
        private val playerAvatar: ImageView = itemView.findViewById(R.id.playerAvatar)

        fun bind(player: Player, onPlayerClickListener: (Player) -> Unit) {
            playerName.text = player.name

            playerAvatar.setImageResource(player.avatarId)

            itemView.setOnClickListener {
                onPlayerClickListener(player)
            }
        }
    }

}
