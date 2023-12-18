package com.example.pertaminaapp.model

import android.os.Parcel
import android.os.Parcelable

data class Reviewer(
    val kode: String,
    val nama: String,
    val email:String,
    val posisi:String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(kode)
        parcel.writeString(nama)
        parcel.writeString(email)
        parcel.writeString(posisi)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Reviewer> {
        override fun createFromParcel(parcel: Parcel): Reviewer {
            return Reviewer(parcel)
        }

        override fun newArray(size: Int): Array<Reviewer?> {
            return arrayOfNulls(size)
        }
    }
}