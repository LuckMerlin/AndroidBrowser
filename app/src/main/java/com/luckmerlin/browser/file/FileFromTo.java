package com.luckmerlin.browser.file;

import android.os.Parcel;
import android.os.Parcelable;
import com.luckmerlin.task.FromTo;

public class FileFromTo extends FromTo<File,File> implements Parcelable {
    private int mMode;

    public FileFromTo() {

    }

    private FileFromTo(Parcel in) {
        setFrom(in.readParcelable(getClass().getClassLoader()));
        setTo(in.readParcelable(getClass().getClassLoader()));
        mMode=in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        File from=getFrom();
        dest.writeParcelable(null!=from&&from instanceof Parcelable?(Parcelable) from:null,flags);
        File to=getTo();
        dest.writeParcelable(null!=to&&to instanceof Parcelable?(Parcelable) to:null,flags);
        dest.writeInt(mMode);
    }

    public int getMode() {
        return mMode;
    }

    public FileFromTo setMode(int mode) {
        this.mMode = mode;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FileFromTo> CREATOR = new Creator<FileFromTo>() {
        @Override
        public FileFromTo createFromParcel(Parcel in) {
            return new FileFromTo(in);
        }

        @Override
        public FileFromTo[] newArray(int size) {
            return new FileFromTo[size];
        }
    };
}
