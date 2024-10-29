package ru.mrak.mafiagame.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.mrak.mafiagame.R
import ru.mrak.mafiagame.data.Role
import ru.mrak.mafiagame.service.DataService
import ru.mrak.mafiagame.types.RoleType
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class RoleAdapter : RecyclerView.Adapter<RoleAdapter.RoleViewHolder>() {

    public var autoCount = true

    var roles: MutableList<Role> = DataService.roles ?: RoleType.entries.map { Role(it, 0) }.toMutableList()
    var playerCount: Int = 0
        set(value) {
            field = value

            if (autoCount) {
                val mafiaCount = if (value > 1) ceil(value / 4.0) else 0
                val detectiveCount = if (value > 3) min(value / 4, 1) else 0
                val doctorCount = if (value > 3) min(value / 4, 1) else 0
                val civilCount = value - mafiaCount.toInt() - detectiveCount - doctorCount

                roles.find { it.role == RoleType.CIVILIAN }!!.count = civilCount
                roles.find { it.role == RoleType.MAFIA }!!.count = mafiaCount.toInt()
                roles.find { it.role == RoleType.DETECTIVE }!!.count = detectiveCount
                roles.find { it.role == RoleType.DOCTOR }!!.count = doctorCount
            } else {
                if (playerCount > getRolesCount()) {
                    roles.find { it.role == RoleType.CIVILIAN }!!.count += playerCount - getRolesCount()
                } else {
                    roles.find { it.role == RoleType.CIVILIAN }.let {
                        it!!.count -= getRolesCount() - playerCount
                        it!!.count = max(it.count, 0)
                    }
                    roles.find { it.role == RoleType.MAFIA }.let {
                        it!!.count -= getRolesCount() - playerCount
                        it!!.count = max(it.count, 0)
                    }
                    roles.find { it.role == RoleType.DETECTIVE }.let {
                        it!!.count -= getRolesCount() - playerCount
                        it!!.count = max(it.count, 0)
                    }
                    roles.find { it.role == RoleType.DOCTOR }.let {
                        it!!.count -= getRolesCount() - playerCount
                        it!!.count = max(it.count, 0)
                    }
                }
            }
            DataService.roles = roles
            notifyDataSetChanged()
        }

    private fun getRolesCount() = roles.sumOf { it.count }

    class RoleViewHolder(
        itemView: View,
        private val roles: MutableList<Role>,
        private val onClickListener: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val roleIcon: ImageView = itemView.findViewById(R.id.roleIcon)
        private val roleCount: TextView = itemView.findViewById(R.id.roleCount)

        fun bind(role: Role) {
            roleIcon.setImageResource(role.role.iconResId)
            roleCount.text = role.count.toString()

            itemView.setOnClickListener {
                if (role.role == RoleType.CIVILIAN) {
                    if (roles.find { it.role == RoleType.MAFIA }!!.count > 0) {
                        role.count++
                        roles.find { it.role == RoleType.MAFIA }!!.count--
                    } else if (roles.find { it.role == RoleType.DETECTIVE }!!.count > 0) {
                        role.count++
                        roles.find { it.role == RoleType.DETECTIVE }!!.count--
                    } else if (roles.find { it.role == RoleType.DOCTOR }!!.count > 0) {
                        role.count++
                        roles.find { it.role == RoleType.DOCTOR }!!.count--
                    }
                    roleCount.text = role.count.toString()
                    onClickListener()
                } else {
                    if (roles.find { it.role == RoleType.CIVILIAN }!!.count > 0) {
                        if (role.role == RoleType.DETECTIVE || role.role == RoleType.DOCTOR) {
                            if (role.count == 0) {
                                role.count++
                                roles.find { it.role == RoleType.CIVILIAN }!!.count--
                            } else {
                                role.count = 0
                                roles.find { it.role == RoleType.CIVILIAN }!!.count++
                            }
                        } else {
                            role.count++
                            roles.find { it.role == RoleType.CIVILIAN }!!.count--
                        }
                    } else {
                        roles.find { it.role == RoleType.CIVILIAN }!!.count += role.count
                        role.count = 0
                    }
                    roleCount.text = role.count.toString()
                    onClickListener()
                }
                DataService.roles = roles
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_role, parent, false)
        return RoleViewHolder(view, roles) { notifyDataSetChanged() }
    }

    override fun onBindViewHolder(holder: RoleViewHolder, position: Int) {
        val role = roles[position]
        holder.bind(role)
    }

    override fun getItemCount(): Int = roles.size
}
