package com.luckmerlin.browser.client;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.view.View;
import com.luckmerlin.browser.BrowseQuery;
import com.luckmerlin.core.Code;
import com.luckmerlin.browser.Label;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.Utils;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.http.JavaHttp;
import com.luckmerlin.browser.http.MResponse;
import com.luckmerlin.core.Canceled;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.core.Response;
import com.luckmerlin.data.Parcelable;
import com.luckmerlin.data.Parceler;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.http.Answer;
import com.luckmerlin.http.AnswerBody;
import com.luckmerlin.http.Connection;
import com.luckmerlin.http.Headers;
import com.luckmerlin.http.Request;
import com.luckmerlin.http.Http;
import com.luckmerlin.http.Requested;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;
import org.json.JSONObject;
import java.io.IOException;

public class NasClient extends AbstractClient implements Parcelable {
    private Http mHttp;

    public NasClient(String host){
        super(host);
        mHttp=new JavaHttp().setBaseUrl(host);
    }

    @Override
    public Canceler setHome(File file, OnFinish<Response<File>> onFinish) {
        return null;
    }

    @Override
    public Response<File> rename(String filePath, String name) {
        return mHttp.call(new Request().url("/file/rename").headerEncode(Label.LABEL_PATH,filePath).
                        headerEncode(Label.LABEL_NAME,name).post(),
                new MResponse<File>((Object from)->null!=from&&from instanceof JSONObject? new File((JSONObject) from):null));
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
    public Response<Folder> listFiles(String folderPath, long start, int size, BrowseQuery filter){
        return mHttp.call(new Request().url("/file/browser").
                headerEncode(Label.LABEL_BROWSER_FOLDER,folderPath).header(Label.LABEL_FROM,start).
                header(Label.LABEL_DATA,null!=filter?filter:"").header(Label.LABEL_PAGE_SIZE,size).post(),
                new MResponse<Folder>((Object data)-> null!=data? new Folder(data):null));
    }

    @Override
    public Drawable loadThumb(View root, File file, Canceled canceled) {
        return null;
    }

    @Override
    public Response<File> loadFile(String file) {
        return mHttp.call(new Request().headerEncode(Label.LABEL_PATH, file).url("/file/detail").post().post(),
                new MResponse<File>((Object data)-> null!=data?new File(data):null));
    }

    @Override
    public Response<File> deleteFile(File file, OnFileDeleteUpdate deleteUpdate) {
        String filePath=null!=file?file.getPath():null;
        Request request=new Request().url("/file/delete").headerEncode(Label.LABEL_PATH,filePath).post();
        Connection connection=mHttp.connect(request);
        if (null==connection){
            Debug.E("Fail delete file while connect http invalid.");
            return new Response<>(Code.CODE_FAIL,"Connect http invalid.");
        }
        AnswerChunkInputStreamReader reader=new AnswerChunkInputStreamReader(connection);
        try {
            return reader.readAllChunk(null==deleteUpdate?(byte[] newData)->true:(byte[] newData)-> {
                   Response<File> response=MResponse.parse(newData, (data) -> File.fromJson(data,"from"));
                   Debug.D("WWWWWWWW "+response);
                   if (null==response){
                       Debug.W("Interrupt file delete while chunk read error."+(null!=newData?new String(newData):""));
                       return false;
                   }
                   deleteUpdate.onFileDeleteUpdate(response.getCode(Code.CODE_UNKNOWN),response.getMessage(),response.getData());
                   return true;
           }, (byte[] bytes) -> MResponse.parse(bytes, (data) -> File.fromJson(data)), 1024);
        } catch (IOException e) {
            Debug.E("Exception delete file.e="+e,e);
            e.printStackTrace();
            return null;
        }finally {
            Utils.closeStream(connection);
        }
    }

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

    private NasClient(Parceler parceler, Parcel parcel){
        this(parceler.readString(parcel,null));
        setName(parceler.readString(parcel,getName()));
        setIcon(parceler.readString(parcel,getIcon()));
    }

    @Override
    public void writeToParcel(Parceler parceler, Parcel parcel, int flags) {
        parceler.writeString(parcel,getHost());
        parceler.writeString(parcel,getName());
        parceler.writeString(parcel,getIcon());
    }
}
