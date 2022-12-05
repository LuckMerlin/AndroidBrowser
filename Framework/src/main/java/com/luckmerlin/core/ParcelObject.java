package com.luckmerlin.core;

import android.os.Parcel;

import com.luckmerlin.object.ObjectCreator;
import java.util.Collection;

@Deprecated
public interface ParcelObject {
    @Deprecated
    void onParcelRead(Parcel parcel);
    @Deprecated
    void onParcelWrite(Parcel parcel);

    final static class Parceler{

        public static Parcel readParcel(byte[] bytes) {
            if (null == bytes || bytes.length <= 0) {
                return null;
            }
            Parcel parcel=Parcel.obtain();
            parcel.unmarshall(bytes,0,bytes.length);
            parcel.setDataPosition(0);
            return parcel;
        }

        public static <T extends ParcelObject> T read(byte[] bytes, Parser<String,T> parser){
            Parcel parcel=readParcel(bytes);
            if (null==parcel){
                return null;
            }
            T data=read(parcel,parser);
            parcel.recycle();
            return data;
        }

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
                if (null!=data&&data instanceof ParcelObject){
                    ((ParcelObject)data).onParcelRead(parcel2);
                }
                parcel2.recycle();
            }
            return data;
        }

        public static <T extends ParcelObject> void readList(Parcel parcel,Collection<T> result, Parser<String,T> parser){
            readList(parcel, (byte[] newData)-> {
                T data=Parceler.read(newData,parser);
                if (null!=data&&null!=result){
                    result.add(data);
                }
                return false;
            });
        }

        public static void readList(Parcel parcel,OnChangeUpdate<byte[]> update){
            int count=parcel.readInt();
            int bytesLength=0;boolean interrupted=false;
            for (int i = 0; i < count; i++) {
                if ((bytesLength=parcel.readInt())<=0){
                    continue;
                }
                byte[] bytes=new byte[bytesLength];
                parcel.readByteArray(bytes);
                if (!interrupted&&null!=update&&(interrupted=update.onChangeUpdated(bytes))){
                    update.onChangeUpdated(bytes);
                }
            }
        }

        public static byte[] writeList(Collection<?extends ParcelObject> collection){
            if (null==collection){
                return null;
            }
            Parcel objParcel=Parcel.obtain();
            writeList(objParcel,collection);
            byte[] bytes=objParcel.marshall();
            objParcel.recycle();
            return bytes;
        }

        public static void writeList(Parcel parcel,Collection<?extends ParcelObject> collection){
            int count=null!=collection?collection.size():0;
            parcel.writeInt(count);
            if (count>0){
                byte[] childBytes;
                for (ParcelObject child:collection) {
                    if (null==(childBytes=(null!=child?Parceler.write(child):null))||childBytes.length<=0){
                        parcel.writeInt(0);
                    }else{
                        parcel.writeInt(childBytes.length);
                        parcel.writeByteArray(childBytes);
                    }
                }
            }
        }

        public static byte[] write(ParcelObject obj){
            if (null==obj){
                return null;
            }
            Parcel objParcel=Parcel.obtain();
            write(objParcel,obj);
            byte[] bytes=objParcel.marshall();
            objParcel.recycle();
            return bytes;
        }

        public static void write(Parcel parcel,ParcelObject parcelObject){
            byte[] bytes=null;
            if (null!=parcelObject){
                Parcel objParcel=Parcel.obtain();
                objParcel.setDataPosition(0);
                parcelObject.onParcelWrite(objParcel);
                bytes=objParcel.marshall();
                objParcel.recycle();
            }
            int length=null!=bytes?bytes.length:0;
            parcel.writeInt(length);
            if (length>0){
                parcel.writeString(parcelObject.getClass().getName());
                parcel.writeByteArray(bytes);
            }
        }
    }
}
