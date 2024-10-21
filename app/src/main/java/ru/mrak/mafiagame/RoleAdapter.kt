package ru.mrak.mafiagame

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.max

class RoleAdapter(private val parent: Fragment) : RecyclerView.Adapter<RoleAdapter.RoleViewHolder>() {

    var roles: List<Role> = RoleType.entries.map { Role(it, 0) }
    var playerCount: Int = 0
        set(value) {
            field = value
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
            notifyDataSetChanged()
        }

    fun saveRoles() {
        val sharedPreferences = parent.requireContext().getSharedPreferences("mafia_game", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val jsonRoles = gson.toJson(roles)
        editor.putString("roles", jsonRoles)
        editor.apply()
    }

    fun loadRoles() {
        val sharedPreferences = parent.requireContext().getSharedPreferences("mafia_game", Context.MODE_PRIVATE)
        val jsonRoles = sharedPreferences.getString("roles", null)
        if (jsonRoles != null) {
            val gson = Gson()
            val type = object : TypeToken<List<Role>>() {}.type
            roles = gson.fromJson(jsonRoles, type)
            notifyDataSetChanged()
        }
    }

    private fun getRolesCount() = roles.sumOf { it.count }

    class RoleViewHolder(
        itemView: View,
        private val roles: List<Role>,
        private val onClickListener: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val roleIcon: ImageView = itemView.findViewById(R.id.roleIcon)
        private val roleCount: TextView = itemView.findViewById(R.id.roleCount)

        fun bind(role: Role) {
            roleIcon.setImageResource(role.role.iconResId)
            roleCount.text = role.count.toString()

            itemView.setOnClickListener {
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
