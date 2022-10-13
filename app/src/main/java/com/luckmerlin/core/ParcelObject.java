package com.luckmerlin.core;

import android.os.Parcel;

import com.luckmerlin.object.ObjectCreator;

public interface ParcelObject {
    void onParcelRead(Parcel parcel);
    void onParcelWrite(Parcel parcel);

    final class Parceler{

        public static <T extends ParcelObject> T read(Parcel parcel){
            return read(parcel,null);
        }

        public static <T extends ParcelObject> T read(Parcel parcel, Parser<String,T> parser){
            int size=parcel.readInt();
            T data=null;
            if (size>0){
                String className=parcel.readString();
                byte[] array=new byte[size];
                parcel.readByteArray(array);
                Parcel parcel2=Parcel.obtain();
                parcel2.unmarshall(array,0,array.length);
                parcel2.setDataPosition(0);
                data=null!=parser?parser.onParse(className):null;
                data=null!=data?data: (T) new ObjectCreator().createObject(className);
                parcel2.recycle();
            }
            return data;
        }

        public static void write(Parcel parcel,Object obj){
            byte[] bytes=null;
            if (null!=obj&&obj instanceof ParcelObject){
                Parcel objParcel=Parcel.obtain();
                objParcel.setDataPosition(0);
                ((ParcelObject)obj).onParcelWrite(objParcel);
                bytes=objParcel.marshall();
                objParcel.recycle();
            }
            int length=null!=bytes?bytes.length:0;
            parcel.writeInt(length);
            if (length>0){
                parcel.writeString(obj.getClass().getName());
                parcel.writeByteArray(bytes);
            }
        }
    }
}
