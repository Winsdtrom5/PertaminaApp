package com.example.pertaminaapp.model

import android.os.Parcel
import android.os.Parcelable

data class DinasList(var DinasList: List<DinasData>) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(DinasData.CREATOR) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(DinasList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DinasList> {
        override fun createFromParcel(parcel: Parcel): DinasList {
            return DinasList(parcel)
        }

        override fun newArray(size: Int): Array<DinasList?> {
            return arrayOfNulls(size)
        }
    }
}
