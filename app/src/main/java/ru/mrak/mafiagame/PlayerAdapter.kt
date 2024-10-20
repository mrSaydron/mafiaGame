package ru.mrak.mafiagame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlayerAdapter(
    private val players: List<Player>,
    private val onRemovePlayer: (Player) -> Unit
) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerName: TextView = itemView.findViewById(R.id.playerNameText)
        private val removeButton: Button = itemView.findViewById(R.id.removePlayerButton)

        fun bind(player: Player, onRemovePlayer: (Player) -> Unit) {
            playerName.text = player.name
            removeButton.setOnClickListener {
                onRemovePlayer(player)
            }
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
