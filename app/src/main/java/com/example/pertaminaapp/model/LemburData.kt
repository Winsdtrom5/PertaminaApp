package com.example.pertaminaapp.model

import android.os.Parcel
import android.os.Parcelable

data class LemburData(
    val nomor: String,
    val tanggal_pengajuan: String,
    val kode_pekerja: String,
    val nama: String,
    val pekerjaan: String,
    val tanggal: String,
    val posisi: String,
    val mulai: String,
    val akhir: String,
    val uang_lembur: String,
    val bukti_lembur:String
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
        parcel.writeString(pekerjaan)
        parcel.writeString(tanggal)
        parcel.writeString(posisi)
        parcel.writeString(mulai)
        parcel.writeString(akhir)
        parcel.writeString(uang_lembur)
        parcel.writeString(bukti_lembur)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LemburData> {
        override fun createFromParcel(parcel: Parcel): LemburData {
            return LemburData(parcel)
        }

        override fun newArray(size: Int): Array<LemburData?> {
            return arrayOfNulls(size)
        }
    }
}
