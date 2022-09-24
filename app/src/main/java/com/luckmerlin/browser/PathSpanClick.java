package com.luckmerlin.browser;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import com.luckmerlin.browser.file.File;
import java.lang.ref.WeakReference;

public class PathSpanClick {
    private WeakReference<OnPathSpanClick> mReference;

    public interface OnPathSpanClick {
        void onPathSpanClick(File path, int start, int end, String value);
    }

    public PathSpanClick setOnClickListener(OnPathSpanClick callback){
        mReference=null!=callback?new WeakReference<>(callback):null;
        return this;
    }

    public CharSequence generate(File file,int color){
        String path=null!=file?file.getPath():null;
        String sep=null!=file?file.getSep():null;
        if (null!=path&&path.length()>1&&null!=sep&&sep.length()>0){
            SpannableStringBuilder builder = new SpannableStringBuilder("");
            String[] splits=path.split(sep);
            int splitLength=null!=splits?splits.length:-1;
            int startIndex=0;String child;int end=0;
            WeakReference<OnPathSpanClick> reference=mReference;
            for (int i = 0; i < splitLength; i++) {
                if (null!=(child=splits[i])&&child.length()>0){
                    builder.append(sep);
                    builder.append(child);
                    end=(startIndex+=1)+child.length();
                    builder.setSpan(new TextViewClickableSpan(file,startIndex,end,builder.toString()){
                        @Override
                        public void onClick(View view) {
                            OnPathSpanClick callback= null!=reference?reference.get():null;
                            if (null!=callback){
                                callback.onPathSpanClick(mPath,mStart,mEnd,mValue);
                            }else{
                                mReference=null;
                            }
                        }

                        @Override
                        public void updateDrawState(TextPaint textPaint) {
                            super.updateDrawState(textPaint);
                            if (null != textPaint) {
                                textPaint.bgColor= Color.TRANSPARENT;
                                textPaint.linkColor=Color.TRANSPARENT;
                                textPaint.setUnderlineText(false);
                                textPaint.setColor(color);
                                textPaint.clearShadowLayer();
                            }
                        }
                    },startIndex,end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    startIndex=end;
                }
            }
            return builder.length()>0?builder:path;
        }
        return path;
    }

    private static abstract class TextViewClickableSpan extends ClickableSpan {
        public final File mPath;
        public final int mStart;
        public final int mEnd;
        public final String mValue;

        private TextViewClickableSpan(File path,int start,int end,String value){
            mPath=path;
            mStart=start;
            mEnd=end;
            mValue=value;
        }
    }
}
