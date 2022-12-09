package com.luckmerlin.browser.file;

import android.content.Context;
import android.os.Parcel;
import com.luckmerlin.browser.R;
import com.luckmerlin.data.Parcelable;
import com.luckmerlin.data.Parceler;
import java.util.ArrayList;
import java.util.List;

public class FileArrayList extends ArrayList<File> {

    public FileArrayList(){
        this(null);
    }

    public FileArrayList(List<File> files){
        if (null!=files&&files.size()>0){
            addAll(files);
        }
    }

    public final File[] toArrays() {
        return super.toArray(new File[size()]);
    }

    public final String makeDescription(Context context){
        final int size=size();
        if (size<=0){
            return null!=context?context.getString(R.string.emptyContent):"Empty";
        }
        StringBuilder builder=new StringBuilder();
        String fileName=null;int nameLength=0;File child=null;boolean moreThanOne=false;
        for (int i = Math.min(size-1,5); i >=0; i--) {
            if (null!=(child=get(i))&&null!=(fileName=child.getName())&&null!=
                    (fileName=fileName.trim())&&(nameLength=fileName.length())>0){
                if (moreThanOne){
                    builder.append("、");
                }
                builder.append(nameLength>8?fileName.substring(0,8)+"...":fileName);
            }
        }
        builder.append(" ").append(null!=context?context.getString(R.string.summeryItemWhich,size):size+" Items");
        return builder.toString();
    }
}
