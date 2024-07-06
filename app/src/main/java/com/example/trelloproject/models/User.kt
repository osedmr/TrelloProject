package com.example.trelloproject.models

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.common.internal.safeparcel.SafeParcelWriter.writeLong
import com.google.android.gms.common.internal.safeparcel.SafeParcelWriter.writeString

data class User(
    val id:String="",
    val name:String="",
    val email:String="",
    val image:String="",
    val mobile:Long=0,
    val fmcToken:String="",
    var selected:Boolean = false

) :Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString()!!,

    ) {
    }

    override fun describeContents()=0

    override fun writeToParcel(p0: Parcel, p1: Int) = with(p0){
        writeString(id)
        writeString(name)
        writeString(email)
        writeString(image)
        writeLong(mobile)
        writeString(fmcToken)
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