package com.example.pertaminaapp.model

import android.os.Parcel
import android.os.Parcelable

data class Holiday(
    val tanggal: String,
    val nama: String,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(tanggal)
        parcel.writeString(nama)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Holiday> {
        override fun createFromParcel(parcel: Parcel): Holiday {
            return Holiday(parcel)
        }

        override fun newArray(size: Int): Array<Holiday?> {
            return arrayOfNulls(size)
        }
    }
}