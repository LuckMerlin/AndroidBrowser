package com.luckmerlin.browser.client;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.Label;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.browser.http.JavaHttp;
import com.luckmerlin.browser.http.MResponse;
import com.luckmerlin.core.Canceled;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.core.Reply;
import com.luckmerlin.core.Response;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.http.Connection;
import com.luckmerlin.http.OnHttpParse;
import com.luckmerlin.http.Request;
import com.luckmerlin.http.Http;
import com.luckmerlin.http.Requested;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;

import org.json.JSONObject;

import java.io.IOException;

public class NasClient extends AbstractClient{
    private String mName;
    private final String mHost;
    private final Http mHttp;

    public NasClient(String host,String name){
        mHttp=new JavaHttp().setBaseUrl(mHost=host);
        mName=name;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getHost() {
        return mHost;
    }

    @Override
    public Canceler setHome(File file, OnFinish<Reply<File>> onFinish) {
        return null;
    }

    @Override
    public Response<File> createFile(File parent, String name, boolean isDir) {
        String folderPath=null!=parent?parent.getPath():null;
        return mHttp.call(new Request().url("/file/create").headerEncode(Label.LABEL_PARENT,folderPath).
                headerEncode(Label.LABEL_NAME,name).header(Label.LABEL_FOLDER,isDir).post(),
                new MResponse<File>((Object from)->null!=from&&from instanceof JSONObject? new File((JSONObject) from):null));
    }

    @Override
    public Response<File> deleteFile(File file, OnFileDoingUpdate update) {
        String filePath=null!=file?file.getPath():null;
        return mHttp.call(new Request().url("/file/delete").headerEncode(Label.LABEL_PATH,filePath).post(),
                new FileChunkParser(Mode.MODE_DELETE,update));
    }

    @Override
    public Response<Folder> listFiles(File folder, long start, int size, Filter filter){
        String folderPath=null!=folder?folder.getPath():null;
        return mHttp.call(new Request().url("/file/browser").
                headerEncode(Label.LABEL_BROWSER_FOLDER,folderPath).header(Label.LABEL_FROM,start).
                header(Label.LABEL_DATA,null!=filter?filter:"").header(Label.LABEL_PAGE_SIZE,size).post(),
                new MResponse<Folder>((Object data)-> null!=data&&data instanceof JSONObject?new Folder((JSONObject)data):null));
    }

    @Override
    public Drawable loadThumb(View root, File file, Canceled canceled) {
        return null;
    }

    public Response<File> getFileDetail(File file,boolean detail){
        return mHttp.call(new Request().headerEncode(Label.LABEL_PATH, null!=file?file.getPath():null).url("/file/detail").post().post(),
                new MResponse<File>((Object data)-> null!=data&&data instanceof JSONObject?new Folder((JSONObject)data):null));
    }

    @Override
    public Response<InputStream> openInputStream(long openLength, File file) {
        Http http=mHttp;
        if (null==http){
            Debug.E("Fail open nas file input stream while none http.");
            return new Response<>(Code.CODE_ERROR,"None http.",null);
        }
        ChunkInputStreamParser parser=new ChunkInputStreamParser();
        return http.call(new Request().header(Label.LABEL_FROM,openLength).
                headerEncode(Label.LABEL_PATH,null!=file?file.getPath():null).
                url("/file/inputStream").post(),parser);
    }

    @Override
    public Response<OutputStream> openOutputStream(File file) {
        if (null==file){
            Debug.E("Fail open nas file output stream while file invalid.");
            return new Response<>(Code.CODE_ERROR,"File invalid.",null);
        }
        Http http=mHttp;
        if (null==http){
            Debug.E("Fail open nas file output stream while none http.");
            return new Response<>(Code.CODE_ERROR,"None http.",null);
        }
        Response<File> response=getFileDetail(file,false);
        response=null!=response?response:new Response<>(Code.CODE_FAIL,"Unknown error.");
        if (null==response||!response.isAnyCode(Code.CODE_SUCCEED,Code.CODE_NOT_EXIST)){
            Debug.E("Fail open nas file output stream while fetch file error.");
            return new Response<>(response.getCode(Code.CODE_FAIL),response.getMessage(),null);
        }
        File currentFile=null!=response?response.getData():null;
        long length=null!=currentFile?currentFile.getLength():0;
        Debug.D("Open nas file output stream. from="+length+" "+file.getPath());
        String filePath=null!=file?file.getPath():null;
        final Request request=new Request().headerEncode(Label.LABEL_PATH, filePath).
                header(Label.LABEL_SIZE,length).url("/file/outputStream").post();
        Connection connection=mHttp.connect(request);
        Requested requested=null!=connection?connection.getRequested():null;
        java.io.OutputStream outputStream=null!=requested?requested.getOutputStream():null;
        if (null==outputStream){
            Debug.E("Fail open nas file output stream while open http output stream invalid.");
            return new Response<>(Code.CODE_ERROR,"Open http output stream invalid.",null);
        }
        final OnHttpParse<Response<File>> responseParser=new MResponse<File>((Object data)-> null!=data&&data instanceof JSONObject?new Folder((JSONObject)data):null);
        return new Response(Code.CODE_SUCCEED, "", new OutputStream(length,null) {
            @Override
            protected void onWrite(int b) throws IOException {
                outputStream.write(b);
            }

            @Override
            public void close() throws IOException {
                Response<File> response=responseParser.onParse(mHttp,requested.getAnswer());
                connection.close();
                Debug.D("SSSS "+response);
            }
        });
    }

}
