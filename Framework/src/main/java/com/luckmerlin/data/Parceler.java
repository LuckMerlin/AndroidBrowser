package com.luckmerlin.data;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class Parceler {
    private Parcel mDest;

    public Parceler(){
        this(null);
    }

    public Parceler(Parcel dest){
        mDest=dest;
    }

    public final Parceler setParcel(Parcel parcel){
        mDest=parcel;
        return this;
    }

    public final Parceler writeString(String value){
        Parcel dest=mDest;
        if (null!=dest){
            dest.writeString(value);
        }
        return this;
    }

    public final String readString(String def){
        Parcel dest=mDest;
        return null!=dest?dest.readString():def;
    }

    public final Parceler writeInt(int value){
        Parcel dest=mDest;
        if (null!=dest){
            dest.writeInt(value);
        }
        return this;
    }

    public final int readInt(int def){
        Parcel dest=mDest;
        return null!=dest?dest.readInt():def;
    }

    public final Parceler writeLong(long value){
        Parcel dest=mDest;
        if (null!=dest){
            dest.writeLong(value);
        }
        return this;
    }

    public final long readLong(long def){
        Parcel dest=mDest;
        return null!=dest?dest.readLong():def;
    }

    public final Parceler writeByteArray(byte[] value){
        Parcel dest=mDest;
        if (null!=dest){
            dest.writeByteArray(value);
        }
        return this;
    }

    public final Parceler readByteArray(byte[] result){
        Parcel dest=mDest;
        if (null!=dest){
            dest.readByteArray(result);
        }
        return this;
    }

    public final Parceler writeByte(byte value){
        Parcel dest=mDest;
        if (null!=dest){
            dest.writeByte(value);
        }
        return this;
    }

    public final byte readByte(byte def){
        Parcel dest=mDest;
        return null!=dest?dest.readByte():def;
    }

    public final Parceler writeByteArray(byte[] value,int offset,int len){
        Parcel dest=mDest;
        if (null!=dest){
            dest.writeByteArray(value,offset,len);
        }
        return this;
    }

    public final Parceler writeArray(Object[] value){
        Parcel dest=mDest;
        if (null!=dest){
            dest.writeArray(value);
        }
        return this;
    }

    public final Object[] readArray(ClassLoader loader){
        Parcel dest=mDest;
        return null!=dest?dest.readArray(null!=loader?loader:getClass().getClassLoader()):null;
    }

    public final Parceler writeBoolean(boolean value){
        Parcel dest=mDest;
        if (null!=dest){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                dest.writeBoolean(value);
            }else{
                dest.writeByte(value?(byte) 1:0);
            }
        }
        return this;
    }

    public final boolean readBoolean(boolean def){
        Parcel dest=mDest;
        if (null!=dest){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return dest.readBoolean();
            }
            return dest.readByte()==1;
        }
        return def;
    }

    public final Parceler writeParcelable(Parcelable p, int parcelableFlags) {
        Parcel dest=mDest;
        if (null!=dest){
            dest.writeParcelable(p,parcelableFlags);
        }
        return this;
    }

    public final <T extends Parcelable> T readParcelable(){
        return readParcelable(getClass().getClassLoader());
    }

    public final <T extends Parcelable> T readParcelable(ClassLoader loader){
        Parcel dest=mDest;
        return null!=dest?dest.readParcelable(null!=loader?loader:getClass().getClassLoader()):null;
    }

    public final Parceler writeDouble(double value) {
        Parcel dest=mDest;
        if (null!=dest){
            dest.writeDouble(value);
        }
        return this;
    }

    public final double readDouble(double def){
        Parcel dest=mDest;
        return null!=dest?dest.readDouble():def;
    }

    public final Parceler writeFloat(float value) {
        Parcel dest=mDest;
        if (null!=dest){
            dest.writeFloat(value);
        }
        return this;
    }

    public final float readFloat(float def){
        Parcel dest=mDest;
        return null!=dest?dest.readFloat():def;
    }

    public final Parceler writeValue(Object value) {
        Parcel dest=mDest;
        if (null!=dest){
            dest.writeValue(value);
        }
        return this;
    }

    public final Object readValue(ClassLoader loader){
        Parcel dest=mDest;
        return null!=dest?dest.readValue(null!=loader?loader:getClass().getClassLoader()):null;
    }

    public final <T extends Parcelable> Parceler writeParcelableList(List<T> obj, int flags){
        Parcel parcel=mDest;
        if (null!=parcel){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                parcel.writeParcelableList(obj,flags);
            }else{
                parcel.writeList(obj);
            }
        }
        return this;
    }

    public final <T extends Parcelable> List<T> readParcelableList(){
        return readParcelableList(new ArrayList<T>(),getClass().getClassLoader());
    }

    public final <T extends Parcelable> List<T> readParcelableList
            (List<T> list){
        return readParcelableList(list,getClass().getClassLoader());
    }

    public final <T extends Parcelable> List<T> readParcelableList
            (List<T> list,ClassLoader classLoader){
        Parcel parcel=mDest;
        if (null!=parcel){
            classLoader=null!=classLoader?classLoader:getClass().getClassLoader();
            list=null!=list?list:new ArrayList<T>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return parcel.readParcelableList(list,classLoader);
            }
            parcel.readList(list,classLoader);
            return list;
        }
        return null;
    }


    public final Parceler writeAsParcelable(Object obj,int flags){
        Parcel parcel=mDest;
        if (null!=parcel){
            parcel.writeParcelable(null!=obj&&obj instanceof Parcelable?(Parcelable)obj:null,flags);
        }
        return this;
    }

}
