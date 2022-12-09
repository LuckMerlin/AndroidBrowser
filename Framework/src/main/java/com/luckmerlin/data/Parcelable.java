package com.luckmerlin.data;

import android.os.Parcel;

public interface Parcelable {
   void writeToParcel(Parceler parceler,Parcel parcel,int flags);
}
