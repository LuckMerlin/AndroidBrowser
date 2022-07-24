package com.luckmerlin.browser;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.databinding.ObservableField;

import com.luckmerlin.browser.client.NasClient;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.browser.http.Reply;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.click.OnLongClickListener;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.dialog.WindowDialog;
import com.luckmerlin.http.Http;
import com.luckmerlin.http.OnHttpFinish;
import com.luckmerlin.http.OnResponse;
import com.luckmerlin.http.Request;
import com.luckmerlin.http.Response;
import com.luckmerlin.http.TextParser;
import com.luckmerlin.object.Parser;
import com.luckmerlin.view.ViewCreator;
import com.merlin.adapter.ListAdapter;
import com.merlin.adapter.PageListAdapter;
import com.merlin.model.OnActivityCreate;

import org.json.JSONException;
import org.json.JSONObject;

public class BrowserActivityModel extends BaseModel implements OnActivityCreate,
        PageListAdapter.OnPageLoadListener<File>, PathSpanClick.OnPathSpanClick,
        OnClickListener, OnLongClickListener {
    private ObservableField<Client> mBrowserClient=new ObservableField<>();
    private ObservableField<ListAdapter> mContentAdapter=new ObservableField<>();
    private ObservableField<String> mNotifyText=new ObservableField<>();
    private ObservableField<Folder> mCurrentFolder=new ObservableField<>();
    private ObservableField<CharSequence> mCurrentPath=new ObservableField<>();
    private final ObservableField<String> mSearchInput=new ObservableField<>();
    private ObservableField<Mode> mBrowserMode=new ObservableField<Mode>();
    private final PathSpanClick mPathSpanClick=new PathSpanClick();
    private final BrowserListAdapter mBrowserAdapter=new BrowserListAdapter
            ((BrowseQuery args, File from, int pageSize, PageListAdapter.OnPageLoadListener<File> callback)->
             loadFiles(args,from,pageSize,callback));

    @Override
    public void onCreate(Bundle savedInstanceState, Activity activity) {
        mBrowserAdapter.setOnPageLoadedListener(this);
        mPathSpanClick.setOnClickListener(this);
        mBrowserClient.set(new NasClient(getHttp()));
//        mNotifyText.set("");
        mContentAdapter.set(mBrowserAdapter);
        entryMode(new Mode(Mode.MODE_MULTI_CHOOSE));
        //
        JSONObject json=new JSONObject();
        try {
            json.put("code",1002);
            json.put("msg",1000);
            json.put("data",1000);
            json.put("mMedias","我爱");
            json.put("price","99.9991");
            json.put("data","99.9991");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //
//        Reply<TypeWrapper<DDD>> input=new Reply<TypeWrapper<DDD>>();
//        Object reply=new JsonIterator().applySafe(new TypeToken<Reply>(){}.getType(),json);
//        Debug.D("结果 "+" "+reply);
//        Debug.D("结2 "+input.getData().getCount());
//        request(new Request<Reply<Folder>>().onParse((String text, Http http, Response res)->
//                new Reply<Folder>(text).parser((Object from)-> null!=from?new Folder(from):null)).
//                onFinish((Reply<Folder> data, Response response)-> {
//            Debug.D("CVVVVVVVV "+data.getData().getChildren());
//        }).url("/file/browser/").
//                header(Label.LABEL_BROWSER_FOLDER,"./").
//                header(Label.LABEL_FROM_INDEX,0).
//                header(Label.LABEL_PAGE_SIZE,10).
//                header(Label.LABEL_ORDER_BY,"size").
//                post());
        //
//        ddd(new HttpParser<Activity>());
    }

    private Canceler loadFiles(BrowseQuery args, File from, int pageSize, PageListAdapter.OnPageLoadListener<File> callback){
        Client client=getClient();
        return null!=client?client.loadFiles(args,from,pageSize,callback):null;
    }

    private boolean browserPath(String path){
        if (null==path||path.length()<=0){
            return false;
        }
        BrowserListAdapter adapter=mBrowserAdapter;
        String searchInput=mSearchInput.get();
        return null!=adapter&&adapter.reset(new BrowseQuery(path,searchInput),null);
    }

    @Override
    public void onPageLoad(boolean succeed, PageListAdapter.Page<File> page) {
        if (succeed){
            if (null!=page&&page instanceof Folder){
                Folder folder=(Folder)page;
                mCurrentFolder.set(folder);
                mCurrentPath.set(mPathSpanClick.generate(folder));
                if (mBrowserAdapter.getSize()>0&&folder.isEmpty()){
                    toast(R.string.noMoreData,500);
                }
            }
        }
    }

    @Override
    public void onPathSpanClick(File path, int start, int end, String value) {
        if (null!=value&&value.length()>0){
            browserPath(value);
        }
    }

    public boolean entryMode(Mode mode){
        Mode current=mBrowserMode.get();
        if ((null==current&&null==mode)||(null!=current&&null!=mode&&current==mode)){
            return true;
        }
        mBrowserMode.set(mode);
        return true;
    }

    @Override
    public boolean onLongClick(View view, int clickId, Object obj) {
        if (null!=obj&&obj instanceof File){
//            File file=(File)obj;
//            return toast("点击文件 "+file.getName());
            entryMode(new Mode(Mode.MODE_MULTI_CHOOSE));
            return true;
        }
        return true;
    }

    public boolean browserBack(){
        Folder folder=mCurrentFolder.get();
        String parent=null!=folder?folder.getParent():null;
        return null!=parent&&parent.length()>0&&browserPath(parent);
    }

    @Override
    public boolean onClick(View view,int clickId, int count, Object obj) {
        switch (clickId){
            case R.drawable.selector_back:
                return browserBack()||true;
            case R.drawable.selector_cancel:
            case R.string.cancel:
                return entryMode(null)||true;
            case R.drawable.selector_menu:
                return showBrowserContextMenu(view.getContext())||true;
        }
        if (null!=obj&&obj instanceof File){
            File file=(File)obj;
            if (file.isDirectory()){
                return browserPath(file.getPath());
            }
            return toast("点击文件 "+file.getName());
        }
        return false;
    }

    private boolean showBrowserContextMenu(Context context){
        WindowDialog windowDialog=new WindowDialog();
        windowDialog.setContentView(new ViewCreator().inflate(context,R.layout.browser_content_menus),null);
        return windowDialog.show(null);
    }

    private Client getClient(){
        return mBrowserClient.get();
    }

    public ObservableField<Client> getBrowserClient() {
        return mBrowserClient;
    }

    public ObservableField<ListAdapter> getContentAdapter() {
        return mContentAdapter;
    }

    public BrowserListAdapter getBrowserAdapter() {
        return mBrowserAdapter;
    }

    public ObservableField<Folder> getCurrentFolder() {
        return mCurrentFolder;
    }

    public ObservableField<CharSequence> getCurrentPath() {
        return mCurrentPath;
    }

    public ObservableField<String> getNotifyText() {
        return mNotifyText;
    }

    public final ObservableField<Mode> getBrowserMode() {
        return mBrowserMode;
    }
}
