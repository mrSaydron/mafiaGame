package ru.mrak.mafiagame.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ru.mrak.mafiagame.data.Player
import ru.mrak.mafiagame.R
import ru.mrak.mafiagame.service.DataService
import ru.mrak.mafiagame.types.RoleType
import java.util.Collections

class PlayerAdapter(
    private val onPlayerClickListener: (Player) -> Unit,
    private val changePlayerList: (List<Player>) -> Unit,
) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    var players: MutableList<Player> = DataService.players ?: mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]
        holder.bind(player, onPlayerClickListener)
    }

    fun addPlayer(player: Player) {
        players.add(player)
        notifyItemInserted(players.size - 1)
        changePlayerList(players)
        DataService.players = players
    }

    private fun removePlayer(index: Int) {
        if (index != -1) {
            players.removeAt(index)
            notifyItemRemoved(index)
            changePlayerList(players)
            DataService.players = players
        }
    }

    fun replacePlayer(player: Player, newPlayer: Player) {
        val index = players.indexOf(player)
        players[index] = newPlayer
        notifyItemChanged(index)
        DataService.players = players
    }

    fun resetPlayersAdditionalData() {
        players.forEach {
            it.role = RoleType.CIVILIAN
            it.isAlive = true
            it.checkedForDetective = false
        }
    }

    override fun getItemCount(): Int = players.size

    val itemTouchHelper = object : ItemTouchHelper.Callback() {

        override fun isLongPressDragEnabled(): Boolean {
            return true
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return true
        }

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlag = ItemTouchHelper.RIGHT
            return makeMovementFlags(dragFlag, swipeFlag)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.bindingAdapterPosition
            val toPosition = target.bindingAdapterPosition
            Collections.swap(players, fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            removePlayer(viewHolder.bindingAdapterPosition)
        }
    }

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerName: TextView = itemView.findViewById(R.id.playerNameText)
        private val playerAvatar: ImageView = itemView.findViewById(R.id.playerAvatar)

        fun bind(player: Player, onPlayerClickListener: (Player) -> Unit) {
            playerName.text = player.name

            try {
                playerAvatar.setImageResource(player.avatarId)
            } catch (e: Exception) {
                Log.w("PlayerAdapter", "Avatar not found: ${player.avatarId}")
            }

            itemView.setOnClickListener {
                onPlayerClickListener(player)
            }
        }
    }

}
