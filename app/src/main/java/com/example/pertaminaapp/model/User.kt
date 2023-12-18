package com.example.pertaminaapp.model

import android.os.Parcel
import android.os.Parcelable
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

data class User(
    val kode: String,
    val nama: String,
    val pola : String,
    val jenis : String,
    val tgl_lahir : String,
    val age: String,
    val jurusan: String,
    val gender: String,
    val masaKerja: String,
    val fungsi: String,
    val pendidikan: String,
    val pjp: String,
    val klasifikasi: String,
    val kota: String,
    val lokasi: String,
    val jabatan:String,
    val email:String,
    val upah:String
) : Parcelable {
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
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(kode)
        parcel.writeString(nama)
        parcel.writeString(pola)
        parcel.writeString(jenis)
        parcel.writeString(tgl_lahir)
        parcel.writeString(age)
        parcel.writeString(jurusan)
        parcel.writeString(gender)
        parcel.writeString(masaKerja)
        parcel.writeString(fungsi)
        parcel.writeString(pendidikan)
        parcel.writeString(pjp)
        parcel.writeString(klasifikasi)
        parcel.writeString(kota)
        parcel.writeString(lokasi)
        parcel.writeString(jabatan)
        parcel.writeString(email)
        parcel.writeString(upah)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}
