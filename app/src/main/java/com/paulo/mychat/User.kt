package com.paulo.mychat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(
    val uid: String = "",
    val name: String = "",
    val url: String = ""
):Parcelable