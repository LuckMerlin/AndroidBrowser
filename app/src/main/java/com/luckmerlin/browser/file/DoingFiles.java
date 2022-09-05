package com.luckmerlin.browser.file;

public final class DoingFiles {
    private File mFrom;
    private File mTo;
    private int mProgress;
    private Integer mMode;

    public DoingFiles setFrom(File file){
        mFrom=file;
        return this;
    }

    public DoingFiles setTo(File file){
        mTo=file;
        return this;
    }

    public DoingFiles setProgress(int progress) {
        this.mProgress = progress;
        return this;
    }

    public boolean isFinish(){
        return mProgress ==100;
    }

    public int getProgress() {
        return mProgress;
    }

    public boolean isFromToEquals(){
        File from=mFrom;
        File to=mTo;
        return (null==from&&null==to)||(null!=from&&null!=to&&from.equals(to));
    }

    public DoingFiles setDoingMode(Integer mode){
        mMode=mode;
        return this;
    }

    public boolean isDoingMode(int modeInt){
        Integer mode=mMode;
        return null!=mode&&mode== modeInt;
    }

    public String getTitle(){
        File from=mFrom;
        File to=mTo;
        return null!=from?from.getName():null!=to?to.getName():null;
    }

    public File getFrom() {
        return mFrom;
    }

    public File getTo() {
        return mTo;
    }
}
