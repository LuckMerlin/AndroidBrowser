package com.luckmerlin.browser;

import android.app.Activity;
import android.os.Bundle;
import androidx.databinding.ObservableField;

import com.luckmerlin.browser.client.NasClient;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.http.Reply;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.http.Http;
import com.luckmerlin.http.OnHttpFinish;
import com.luckmerlin.http.OnResponse;
import com.luckmerlin.http.Request;
import com.luckmerlin.http.Response;
import com.luckmerlin.http.TextParser;
import com.luckmerlin.object.Parser;
import com.merlin.adapter.ListAdapter;
import com.merlin.adapter.PageListAdapter;
import com.merlin.model.OnActivityCreate;

import org.json.JSONException;
import org.json.JSONObject;

public class BrowserActivityModel extends BaseModel implements OnActivityCreate {
    private ObservableField<Client> mBrowserClient=new ObservableField<>();
    private ObservableField<ListAdapter> mContentAdapter=new ObservableField<>();
    private ObservableField<String> mNotifyText=new ObservableField<>();
    private final BrowserListAdapter mBrowserAdapter=new BrowserListAdapter(
            (Folder args, File from, int pageSize, PageListAdapter.OnPageLoad<File> callback)-> {
            Client client=mBrowserClient.get();
            return null!=client?client.loadFiles(args,from,pageSize,callback):null;
    });

    @Override
    public void onCreate(Bundle savedInstanceState, Activity activity) {
        mBrowserClient.set(new NasClient(getHttp()));
        mNotifyText.set("我们都是好孩是打发撒方大是的发送到发萨法沙发沙发啊是的发送到发的说法子");
        mContentAdapter.set(mBrowserAdapter);
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

    public ObservableField<Client> getBrowserClient() {
        return mBrowserClient;
    }

    public ObservableField<ListAdapter> getContentAdapter() {
        return mContentAdapter;
    }

    public BrowserListAdapter getBrowserAdapter() {
        return mBrowserAdapter;
    }

    public ObservableField<String> getNotifyText() {
        return mNotifyText;
    }
}
