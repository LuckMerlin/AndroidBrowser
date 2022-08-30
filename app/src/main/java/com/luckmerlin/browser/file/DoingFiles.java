package com.luckmerlin.browser.file;

public final class DoingFiles {
    private File mFrom;
    private File mTo;

    public DoingFiles setFrom(File file){
        mFrom=file;
        return this;
    }

    public DoingFiles setTo(File file){
        mTo=file;
        return this;
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
