package ru.mrak.mafiagame

import android.os.Parcel
import android.os.Parcelable

enum class Role {
    MAFIA, DETECTIVE, DOCTOR, CIVILIAN
}

data class Player(
    val name: String,
    val avatar: String,
    var role: Role = Role.CIVILIAN,
    var isAlive: Boolean = true,
    var checkedForDetective: Boolean = false,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        Role.valueOf(parcel.readString() ?: Role.CIVILIAN.name)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(avatar)
        parcel.writeString(role.name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Player> {
        override fun createFromParcel(parcel: Parcel): Player {
            return Player(parcel)
        }

        override fun newArray(size: Int): Array<Player?> {
            return arrayOfNulls(size)
        }
    }
}