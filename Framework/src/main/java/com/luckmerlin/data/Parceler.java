package com.luckmerlin.data;

import android.os.Build;
import android.os.Parcel;
import com.luckmerlin.debug.Debug;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Parceler {

    public final Parceler writeString(Parcel parcel,String value){
        if (null!=parcel){
            parcel.writeString(value);
        }
        return this;
    }

    public final String readString(Parcel parcel,String def){
        return null!=parcel?parcel.readString():def;
    }

    public final Parceler writeInt(Parcel parcel,int value){
        if (null!=parcel){
            parcel.writeInt(value);
        }
        return this;
    }

    public final int readInt(Parcel parcel,int def){
        return null!=parcel?parcel.readInt():def;
    }

    public final Parceler writeLong(Parcel parcel,long value){
        if (null!=parcel){
            parcel.writeLong(value);
        }
        return this;
    }

    public final long readLong(Parcel parcel,long def){
        return null!=parcel?parcel.readLong():def;
    }

    public final Parceler writeByteArray(Parcel parcel,byte[] value){
        if (null!=parcel){
            parcel.writeByteArray(value);
        }
        return this;
    }

    public final Parceler readByteArray(Parcel parcel,byte[] result){
        if (null!=parcel){
            parcel.readByteArray(result);
        }
        return this;
    }

    public final Parceler writeByte(Parcel parcel,byte value){
        if (null!=parcel){
            parcel.writeByte(value);
        }
        return this;
    }

    public final byte readByte(Parcel parcel,byte def){
        return null!=parcel?parcel.readByte():def;
    }

    public final Parceler writeByteArray(Parcel parcel,byte[] value, int offset, int len){
        if (null!=parcel){
            parcel.writeByteArray(value,offset,len);
        }
        return this;
    }

    public final Parceler writeArray(Parcel parcel,Object[] value){
        if (null!=parcel){
            parcel.writeArray(value);
        }
        return this;
    }

    public final Object[] readArray(Parcel parcel,ClassLoader loader){
        return null!=parcel?parcel.readArray(null!=loader?loader:getClass().getClassLoader()):null;
    }

    public final Parceler writeBoolean(Parcel parcel,boolean value){
        if (null!=parcel){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                parcel.writeBoolean(value);
            }else{
                parcel.writeByte(value?(byte) 1:0);
            }
        }
        return this;
    }

    public final boolean readBoolean(Parcel parcel,boolean def){
        if (null!=parcel){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return parcel.readBoolean();
            }
            return parcel.readByte()==1;
        }
        return def;
    }

    public final Parceler writeDouble(Parcel parcel,double value) {
        if (null!=parcel){
            parcel.writeDouble(value);
        }
        return this;
    }

    public final double readDouble(Parcel parcel,double def){
        return null!=parcel?parcel.readDouble():def;
    }

    public final Parceler writeFloat(Parcel parcel,float value) {
        if (null!=parcel){
            parcel.writeFloat(value);
        }
        return this;
    }

    public final float readFloat(Parcel parcel,float def){
        return null!=parcel?parcel.readFloat():def;
    }

    public final Parceler writeParcelable(Parcel parcel,Object p, int parcelableFlags) {
        Parcel par=Parcel.obtain();
        writeParcelable(parcel,par,p,parcelableFlags);
        par.recycle();
        return this;
    }

    private final Parceler writeParcelable(Parcel parcel,Parcel temp,Object p, int parcelableFlags) {
        if (null!=parcel){
            if (null==p){
                parcel.writeString("");
                parcel.writeInt(0);
            }else if (p instanceof Parcelable){
                temp.setDataPosition(0);
                temp.setDataSize(0);
                ((Parcelable)p).writeToParcel(this,temp,parcelableFlags);
                byte[] bytes=temp.marshall();
                parcel.writeString(p.getClass().getName());
                parcel.writeInt(-bytes.length);
                parcel.writeByteArray(bytes);
            }else if (p instanceof Collection){
                Collection collection=(Collection)p;
                parcel.writeString(p.getClass().getName());
                parcel.writeInt(collection.size());
                for (Object obj:collection) {
                    writeParcelable(parcel,temp,obj,parcelableFlags);
                }
            }else{
                return writeParcelable(parcel,temp,null,parcelableFlags);
            }
        }
        return this;
    }

    public final <T> T readParcelable(Parcel parcel){
        Parcel temp=Parcel.obtain();
        T result=readParcelable(parcel,temp,getClass().getClassLoader());
        temp.recycle();
        return result;
    }

    private final <T> T readParcelable(Parcel parcel,Parcel temp,ClassLoader loader){
        String clsName=parcel.readString();
        final int size=parcel.readInt();
        if (null==clsName||clsName.length()<=0){
            return null;
        }
        try {
            loader=null!=loader?loader:getClass().getClassLoader();
            Class cls=loader.loadClass(clsName);
            if (size<0&&Parcelable.class.isAssignableFrom(cls)){
                Constructor constructor=cls.getDeclaredConstructor(Parceler.class,Parcel.class);
                constructor.setAccessible(true);
                byte[] bytes=new byte[-size];
                parcel.readByteArray(bytes);
                temp.setDataSize(0);
                temp.unmarshall(bytes,0,bytes.length);
                temp.setDataPosition(0);
                return (T)constructor.newInstance(this,temp);
            }else if (size>=0&&Collection.class.isAssignableFrom(cls)){
                Constructor constructor=cls.getConstructor();
                constructor.setAccessible(true);
                Object list=constructor.newInstance();//New list
                Collection collection=(Collection)list;
                Object childData=null;
                for (int i = 0; i < size; i++) {
                    if (null!=(childData=readParcelable(parcel,temp,loader))){
                        collection.add(childData);
                    }
                }
                return (T)list;
            }
        } catch (Exception e) {
            Debug.E("Exception read parcelable.size="+size+"\n clsName="+clsName+"\n e="+e,e);
            e.printStackTrace();
        }
        return null;
    }
}
