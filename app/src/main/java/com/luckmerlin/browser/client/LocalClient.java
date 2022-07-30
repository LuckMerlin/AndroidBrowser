package com.luckmerlin.browser.client;

import android.os.Environment;

import com.luckmerlin.browser.BrowseQuery;
import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.debug.Debug;
import com.merlin.adapter.PageListAdapter;

import org.json.JSONException;

import java.lang.reflect.Field;
import java.nio.file.spi.FileTypeDetector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LocalClient implements Client {
    private String mRootPath=Environment.getDataDirectory().getAbsolutePath()+"/../";

    @Override
    public String getName() {
        return "Local";
    }

    @Override
    public long getAvailable() {
        return 0;
    }

    @Override
    public long getTotal() {
        return 0;
    }

    @Override
    public Canceler loadFiles(BrowseQuery query, File from, int pageSize, PageListAdapter.OnPageLoadListener<File> callback) {
        String pathValue=null!=query?query.mFolderPath:null;
        String browserPath=null!=pathValue&&pathValue.length()>0?pathValue:mRootPath;
        final java.io.File browserFile=null!=browserPath&&browserPath.length()>0?new java.io.File(browserPath):null;
        if (null==browserFile){
            Debug.W("Can't load client while query file invalid."+browserPath);
            notifyFinish(false,null,callback);
            return null;
        }else if (!browserFile.exists()){
            Debug.W("Can't load client while query file not exist."+browserPath);
            notifyFinish(false,null,callback);
            return null;
        }else if (!browserFile.isDirectory()){
            Debug.W("Can't load client while query file not directory.");
            notifyFinish(false,null,callback);
            return null;
        }
        String filterName=null!=query?query.mSearchInput:null;
        Debug.D("Loading local client.name="+filterName+" from="+(null!=from?from.getName():"")+" path="+browserPath);
        final List<File> files=new ArrayList<>();
        browserFile.listFiles((java.io.File file)-> {
            if (null==file){
                return false;
            }
            String fileName=file.getName();
            if (null!=filterName&&filterName.length()>0&&(null==fileName||!fileName.contains(filterName))){
                return false;
            }
            File child=createLoadFile(file);
           if (null!=child){
               files.add(child);
           }
            return false;
        });
        Folder folder= new Folder(createLoadFile(browserFile));
        //
        final Comparator<File> comparator=(File file1, File file2)-> {
            boolean directory1=file1.isDirectory();
            boolean directory2=file2.isDirectory();
            if (directory1&&directory2){
                String file1Name=null!=file1?file1.getName():null;
                String file2Name=null!=file2?file2.getName():null;
                file1Name=null!=file1Name?file1Name:"";
                file2Name=null!=file2Name?file2Name:"";
                return file1Name.compareTo(file2Name);
            }
            return directory1?-1:directory2?1:0;
        };
        Collections.sort(files,comparator);
        //
        if (null!=from){
            int index=(files.indexOf(from)+1);
            int length=files.size();
            folder.setChildren(index>=0&&index<length?files.subList(index,length):null);
        }else{
            folder.setChildren(files);
        }
        notifyFinish(true,folder, callback);
        return ()->false;
    }

    private File createLoadFile(java.io.File file){
        if (null==file){
            return null;
        }
        long total=-1;
        if (file.isDirectory()){
            java.io.File[] files=file.listFiles();
            total=null!=files?files.length:0;
        }
        return new File().setTotal(total).setLength(file.length()).setMime("text/css").
                setSep(java.io.File.separator).
                setModifyTime(file.lastModified()).
                setParent(file.getAbsoluteFile().getParent()).setName(file.getName());
    }

    private void notifyFinish(boolean succeed, PageListAdapter.Page<File> page,PageListAdapter.OnPageLoadListener<File> callback){
        if (null!=callback){
            callback.onPageLoad(succeed,page);
        }
    }
}
