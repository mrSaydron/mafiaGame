package ru.mrak.mafiagame

import android.os.Parcel
import android.os.Parcelable

data class Player(
    val name: String,
    val avatar: String,
    var role: RoleType = RoleType.CIVILIAN,
    var isAlive: Boolean = true,
    var checkedForDetective: Boolean = false,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        RoleType.valueOf(parcel.readString() ?: RoleType.CIVILIAN.name)
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