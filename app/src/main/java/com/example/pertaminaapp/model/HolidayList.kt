package com.example.pertaminaapp.model

import android.os.Parcel
import android.os.Parcelable

data class HolidayList(var holidayList: List<Holiday>) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(Holiday.CREATOR) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(holidayList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HolidayList> {
        override fun createFromParcel(parcel: Parcel): HolidayList {
            return HolidayList(parcel)
        }

        override fun newArray(size: Int): Array<HolidayList?> {
            return arrayOfNulls(size)
        }
    }
}
