package ru.mrak.mafiagame.data

import android.os.Parcel
import android.os.Parcelable
import ru.mrak.mafiagame.types.RoleType

data class Player(
    val name: String,
    val avatarId: Int,
    var role: RoleType = RoleType.CIVILIAN,
    var isAlive: Boolean = true,
    var checkedForDetective: Boolean = false,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        RoleType.valueOf(parcel.readString() ?: RoleType.CIVILIAN.name)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(avatarId)
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