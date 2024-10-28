package ru.mrak.mafiagame.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import ru.mrak.mafiagame.R

class AvatarAdapter(
    private val avatars: List<Int>, // List of avatar drawable resource IDs
    private val onAvatarSelected: (Int) -> Unit
) : RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_avatar, parent, false)
        return AvatarViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        holder.bind(avatars[position])
        holder.itemView.isSelected = position == selectedPosition
    }

    override fun getItemCount(): Int = avatars.size

    inner class AvatarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)

        fun bind(avatarResId: Int) {
            avatarImageView.setImageResource(avatarResId)
            itemView.setOnClickListener {
                if (selectedPosition != adapterPosition) {
                    notifyItemChanged(selectedPosition)
                    selectedPosition = adapterPosition
                    onAvatarSelected(avatarResId)
                    notifyItemChanged(selectedPosition)
                }
            }
        }
    }
}
