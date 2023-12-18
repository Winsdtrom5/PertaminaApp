package com.example.pertaminaapp.model

import android.os.Parcel
import android.os.Parcelable

data class LemburList(var LemburList: List<LemburData>) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(LemburData.CREATOR) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(LemburList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LemburList> {
        override fun createFromParcel(parcel: Parcel): LemburList {
            return LemburList(parcel)
        }

        override fun newArray(size: Int): Array<LemburList?> {
            return arrayOfNulls(size)
        }
    }
}
