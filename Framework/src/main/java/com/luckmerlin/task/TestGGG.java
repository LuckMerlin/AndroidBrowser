package com.luckmerlin.task;

import android.os.Parcel;
import android.os.Parcelable;

import com.luckmerlin.debug.Debug;

public class TestGGG {
    static boolean test=false;

    public void testR(Task task,int option){
        if (!test){
            test=true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        test(task,option,0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void test(Task task,int option,int i) throws InterruptedException {
        Debug.D("写入到 "+i+" "+task);
        Parcel parcel1=Parcel.obtain();
        parcel1.setDataPosition(0);
        parcel1.writeInt(option);
        parcel1.writeParcelable((Parcelable)task,0);
        byte[] bytes=parcel1.marshall();
        parcel1.recycle();

        Parcel parcel=Parcel.obtain();
        Parcelable parcelable=null;
        try {
            parcel.unmarshall(bytes,0,bytes.length);
            parcel.setDataPosition(0);
            option=parcel.readInt();
            parcelable=parcel.readParcelable(getClass().getClassLoader());
            parcel.recycle();
            parcel=null;
            Debug.D("读取到 "+i+" "+parcelable);
        }catch (Exception e){
            Debug.E("Exception execute saved task.e="+e,e);
            e.printStackTrace();
        }finally {
            if (null!=parcel){
                parcel.recycle();
            }
        }
        Thread.sleep(3000);
        test((Task)parcelable,option,++i );
    }
}
