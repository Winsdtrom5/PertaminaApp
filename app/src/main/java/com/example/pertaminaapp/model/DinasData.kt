package com.example.pertaminaapp.model

import android.os.Parcel
import android.os.Parcelable

data class DinasData(
    val nomor: String,
    val tanggal_pengajuan: String,
    val kode_pekerja: String,
    val nama: String,
    val keterangan: String,
    val kendaraan: String,
    val asal:String,
    val tujuan: String,
    val mulai: String,
    val akhir: String,
    val data_upload: String
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nomor)
        parcel.writeString(tanggal_pengajuan)
        parcel.writeString(kode_pekerja)
        parcel.writeString(nama)
        parcel.writeString(keterangan)
        parcel.writeString(kendaraan)
        parcel.writeString(asal)
        parcel.writeString(tujuan)
        parcel.writeString(mulai)
        parcel.writeString(akhir)
        parcel.writeString(data_upload)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DinasData> {
        override fun createFromParcel(parcel: Parcel): DinasData {
            return DinasData(parcel)
        }

        override fun newArray(size: Int): Array<DinasData?> {
            return arrayOfNulls(size)
        }
    }
}
