package com.luckmerlin.browser.client;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;

import com.luckmerlin.browser.BrowseQuery;
import com.luckmerlin.browser.ClientMeta;
import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.Label;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.Utils;
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
import com.luckmerlin.http.Answer;
import com.luckmerlin.http.AnswerBody;
import com.luckmerlin.http.Connection;
import com.luckmerlin.http.Headers;
import com.luckmerlin.http.OnHttpParse;
import com.luckmerlin.http.Request;
import com.luckmerlin.http.Http;
import com.luckmerlin.http.Requested;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.InputStreamReader;
import com.luckmerlin.stream.OutputStream;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
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
    public ClientMeta getMeta() {
        return new ClientMeta().setName(mName).setHost(mHost).setIcon(R.drawable.hidisk_icon_nas);
    }

    @Override
    public Object getIcon() {
        return R.drawable.hidisk_icon_nas;
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
    public Response<File> rename(String file, String name) {
        return null;
    }

    @Override
    public boolean openFile(File openFile, Context context) {
        String filePath=null!=openFile?openFile.getPath():null;
        String host=null!=openFile?openFile.getHost():null;
        if (null==filePath||filePath.length()<=0||null==host||host.length()<=0){
            return false;
        }else if (openFile.isLocalFile()){
            return false;
        }else if (openFile.isType(File.Type.VIDEO)||openFile.isType(File.Type.AUDIO)||
                openFile.isType(File.Type.IMAGE)){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.parse(host+"/file/play?path="+Request.encode(filePath,null));
            intent.setDataAndType(uri, openFile.getMime());
            try {
                context.startActivity(intent);
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
            return false;
        }
        return false;
    }

    @Override
    public Response<File> createFile(File parent, String name, boolean isDir) {
        String folderPath=null!=parent?parent.getPath():null;
        return mHttp.call(new Request().url("/file/create").headerEncode(Label.LABEL_PARENT,folderPath).
                headerEncode(Label.LABEL_NAME,name).header(Label.LABEL_FOLDER,isDir).post(),
                new MResponse<File>((Object from)->null!=from&&from instanceof JSONObject? new File((JSONObject) from):null));
    }

    @Override
    public Response<Folder> listFiles(File folder, long start, int size, BrowseQuery filter){
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

    @Override
    public Response<File> loadFile(String file) {
        return mHttp.call(new Request().headerEncode(Label.LABEL_PATH, file).url("/file/detail").post().post(),
                new MResponse<File>((Object data)-> null!=data&&data instanceof JSONObject?new Folder((JSONObject)data):null));
    }

    @Override
    public Response<File> deleteFile(File file, OnFileDeleteUpdate update) {
        return null;
    }

//    @Override
//    public Response<File> deleteFile(File file, OnFileDoingUpdate update) {
//        String filePath=null!=file?file.getPath():null;
//        Request request=new Request().url("/file/delete").headerEncode(Label.LABEL_PATH,filePath).post();
//        Connection connection=mHttp.connect(request);
//        if (null==connection){
//            Debug.E("Fail delete file while connect http invalid.");
//            return new Response<>(Code.CODE_FAIL,"Connect http invalid.");
//        }
//        AnswerChunkInputStreamReader reader=new AnswerChunkInputStreamReader(connection);
//        try {
//            return reader.readAllChunk(new DoingFileChunkUpdateParser(Mode.MODE_DELETE, update),
//                    (byte[] bytes)-> MResponse.parse(bytes,(data)->File.fromJson(data)), 1024);
//        } catch (IOException e) {
//            Debug.E("Exception delete file.e="+e,e);
//            e.printStackTrace();
//            return null;
//        }finally {
//            Utils.closeStream(connection);
//        }
//    }

    @Override
    public Response<InputStream> openInputStream(long skip, File file) {
        String filePath=null!=file?file.getPath():null;
        Request request=new Request().header(Label.LABEL_FROM,skip).
                headerEncode(Label.LABEL_PATH,filePath).url("/file/inputStream").post();
        Connection connection=mHttp.connect(request);
        if (null==connection){
            Debug.W("Fail open file input stream.");
            return new Response<>(Code.CODE_FAIL, "Fail open file input stream.");
        }
        Requested requested=null!=connection?connection.getRequested():null;
        Answer answer=null!=requested?requested.getAnswer():null;
        AnswerBody answerBody=null!=answer?answer.getAnswerBody():null;
        Headers headers=null!=answer?answer.getHeaders():null;
        final long finalTotalLength=headers.getLong("MerlinTotalLength",-1);
        java.io.InputStream inputStream=null!=answerBody?answerBody.getInputStream():null;
        return new Response<>(Code.CODE_SUCCEED, "Succeed.", new InputStream(0) {
            @Override
            public long length() {
                return finalTotalLength;
            }

            @Override
            public int onRead(byte[] b, int off, int len) throws IOException {
                return inputStream.read(b,off,len);
            }

            @Override
            public void close() throws IOException {
                connection.close();
            }
        });
    }

    @Override
    public Response<OutputStream> openOutputStream(File file) {
        String filePath=null!=file?file.getPath():null;
        Response<File> response=loadFile(filePath);
        response=null!=response?response:new Response<>(Code.CODE_FAIL,"Unknown error.");
        File currentFile=response.getData();
        final long currentLength=null!=currentFile?currentFile.getLength():-1;
        if (null==response||!response.isAnyCode(Code.CODE_SUCCEED,Code.CODE_NOT_EXIST)){
            Debug.E("Fail open nas file output stream while fetch file error.");
            return new Response<>(response.getCode(Code.CODE_FAIL),response.getMessage(),null);
        }
        final Request request=new Request().headerEncode(Label.LABEL_PATH, filePath).url("/file/outputStream").post();
        Connection connection=mHttp.connect(request);
        Requested requested=null!=connection?connection.getRequested():null;
        java.io.OutputStream outputStream=null!=requested?requested.getOutputStream():null;
        if (null==outputStream){
            Debug.W("Fail open file output stream.");
            com.luckmerlin.utils.Utils.closeStream(connection);
            return new Response<>(Code.CODE_FAIL, "Fail open file output stream.");
        }
        return new Response<>(Code.CODE_SUCCEED, "Succeed", new OutputStream(currentLength) {
            @Override
            protected void onWrite(byte[] b, int off, int len) throws IOException {
                outputStream.write(b,off,len);
            }

            @Override
            public void close() throws IOException {
                connection.close();
            }
        });
    }

    //    @Override
//    public Response<InputStream> openInputStream(File file) {
//        Request request=new Request().header(Label.LABEL_FROM,openLength).
//                headerEncode(Label.LABEL_PATH,null!=file?file.getPath():null).url("/file/inputStream").post();
//        Connection connection=mHttp.connect(request);
//        if (null==connection){
//            Debug.W("Fail open file input stream.");
//            return new Response<>(Code.CODE_FAIL, "Fail open file input stream.");
//        }
//        Requested requested=null!=connection?connection.getRequested():null;
//        Answer answer=null!=requested?requested.getAnswer():null;
//        AnswerBody answerBody=null!=answer?answer.getAnswerBody():null;
//        java.io.InputStream inputStream=null!=answerBody?answerBody.getInputStream():null;
//        Headers headers=null!=answer?answer.getHeaders():null;
////        long contentLength=answerBody.getContentLength();
//        final long finalContentLength=headers.getLong("MerlinTotalLength",-1);
//        ByteArrayOutputStream arrayOutputStream=new ByteArrayOutputStream();
//        if (null==inputStream){
//            Debug.W("Fail open file input stream while input stream null.");
//            return new Response<>(Code.CODE_FAIL, "Input stream null.");
//        }
//        InputStreamReader inputStreamReader=new InputStreamReader(inputStream,finalContentLength).
//                setOnEndBytesRead((byte[] buffer, int offset, int size)->
//                        arrayOutputStream.write(buffer,offset,size));
//        return new Response<InputStream>(Code.CODE_SUCCEED, "Succeed", new InputStream(openLength) {
//            @Override
//            public long length() {
//                return finalContentLength;
//            }
//
//            @Override
//            public int onRead(byte[] b, int off, int len) throws IOException {
//                return inputStreamReader.read(b,off,len);
//            }
//
//            @Override
//            public void close() throws IOException {
//                connection.close();
//                Debug.D("结束 "+new String(arrayOutputStream.toByteArray()));
//            }
//        });
//        return null;
//    }
//
//    @Override
//    public Response<OutputStream> openOutputStream(File file) {
//        if (null==file){
//            Debug.E("Fail open nas file output stream while file invalid.");
//            return new Response<>(Code.CODE_ERROR,"File invalid.",null);
//        }
//        Http http=mHttp;
//        if (null==http){
//            Debug.E("Fail open nas file output stream while none http.");
//            return new Response<>(Code.CODE_ERROR,"None http.",null);
//        }
//        Response<File> response=getFileDetail(file,false);
//        response=null!=response?response:new Response<>(Code.CODE_FAIL,"Unknown error.");
//        if (null==response||!response.isAnyCode(Code.CODE_SUCCEED,Code.CODE_NOT_EXIST)){
//            Debug.E("Fail open nas file output stream while fetch file error.");
//            return new Response<>(response.getCode(Code.CODE_FAIL),response.getMessage(),null);
//        }
//        File currentFile=null!=response?response.getData():null;
//        long length=null!=currentFile?currentFile.getLength():0;
//        Debug.D("Open nas file output stream. from="+length+" "+file.getPath());
//        String filePath=null!=file?file.getPath():null;
//        final Request request=new Request().headerEncode(Label.LABEL_PATH, filePath).
//                header(Label.LABEL_SIZE,length).url("/file/outputStream").post();
//        Connection connection=mHttp.connect(request);
//        Requested requested=null!=connection?connection.getRequested():null;
//        java.io.OutputStream outputStream=null!=requested?requested.getOutputStream():null;
//        if (null==outputStream){
//            Utils.closeStream(connection);
//            Debug.E("Fail open nas file output stream while open http output stream invalid.");
//            return new Response<>(Code.CODE_ERROR,"Open http output stream invalid.",null);
//        }
//        final OnHttpParse<Response<File>> responseParser=new MResponse<File>((Object data)-> null!=data&&data instanceof JSONObject?new Folder((JSONObject)data):null);
//        return new Response(Code.CODE_SUCCEED, "", new OutputStream(length) {
//            @Override
//            protected void onWrite(byte[] b, int off, int len) throws IOException {
//                outputStream.write(b,off,len);
//            }
//
//            @Override
//            public void close() throws IOException {
//                Response<File> response=responseParser.onParse(mHttp,requested.getAnswer());
//                Utils.closeStream(connection);
//                Debug.D("SSSS "+response);
//            }
//        });
//    }

}
