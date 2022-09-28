package com.luckmerlin.browser;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.ListAdapter;

import com.luckmerlin.browser.binding.DataBindingUtil;
import com.luckmerlin.browser.client.LocalClient;
import com.luckmerlin.browser.databinding.BrowserModelBinding;
import com.luckmerlin.browser.databinding.ItemClientNameBinding;
import com.luckmerlin.browser.dialog.BrowserMenuContextDialogContent;
import com.luckmerlin.browser.dialog.CreateFileDialogContent;
import com.luckmerlin.browser.dialog.DoingContent;
import com.luckmerlin.browser.dialog.TaskDialogContent;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.browser.task.UriFileUploadTask;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.MatchedCollector;
import com.luckmerlin.core.OnConfirm;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.core.Response;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.dialog.FixedLayoutParams;
import com.luckmerlin.dialog.PopupWindow;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Task;
import com.luckmerlin.view.OnViewAttachedToWindow;
import com.luckmerlin.view.OnViewDetachedFromWindow;
import com.luckmerlin.view.ViewIterator;
import com.merlin.model.OnActivityCreate;
import com.merlin.model.OnActivityNewIntent;
import com.merlin.model.OnActivityStart;
import com.merlin.model.OnBackPress;

import java.util.ArrayList;

public class BrowserModel extends BaseModel implements OnActivityCreate, Executor.OnStatusChangeListener,
        OnViewAttachedToWindow,PathSpanClick.OnPathSpanClick,
        OnViewDetachedFromWindow, OnClickListener, OnBackPress,
        OnActivityNewIntent, OnActivityStart {
    private final BrowserListAdapter mBrowserAdapter=new BrowserListAdapter();
    private final ObservableField<String> mSearchInput=new ObservableField<>();
    private ObservableField<String> mNotifyText=new ObservableField<>();
    private final ObservableField<ListAdapter> mContentAdapter=new ObservableField<>();
    private ServiceConnection mServiceConnection;
    private BrowserExecutor mExecutor;

    @Override
    protected View onCreateContent(Context context) {
        ViewDataBinding binding= DataBindingUtil.inflate(context,R.layout.browser_model);
        if (null!= binding&&binding instanceof BrowserModelBinding){
            ((BrowserModelBinding)binding).setVm(this);
            return binding.getRoot();
        }
        return null;
    }

    @Override
    public final boolean onBackPressed() {
        return browserBack();
    }

    @Override
    public void onCreate(Bundle savedInstanceState, Activity activity) {
        mBrowserAdapter.setOnPathSpanClick(this);
        mNotifyText.set("LMBrowser");
        mContentAdapter.set(mBrowserAdapter);
        //
//        showContentDialog(new DoingContent(),null);
//        startActivity(ConveyorActivity.class);
        showBrowserContextMenu(activity);
    }

    @Override
    public boolean onClick(View view,int clickId, int count, Object obj) {
        switch (clickId) {
            case R.drawable.selector_back:
                return browserBack() || true;
            case R.drawable.selector_close:
//                if (null != obj && obj instanceof AlertText) {
//                    showAlertText(null, 0);
//                }
                return true;
            case R.layout.item_client_name:
                return selectClients(view,null!=obj&&obj instanceof Client?(Client)obj:null)||true;
            case R.drawable.selector_list:
                return mBrowserAdapter.setGirdLayout(false)||true;
            case R.drawable.selector_gird:
                return mBrowserAdapter.setGirdLayout(true)||true;
            case R.string.conveyor:
            case R.drawable.selector_transport:
                return startActivity(ConveyorActivity.class)||true;
            case R.drawable.selector_search:
                BrowserListAdapter browserListAdapter=mBrowserAdapter;
                BrowseQuery query=null!=browserListAdapter?browserListAdapter.getCurrent():null;
                return (null!=query&&browserPath(query.mFolder))||true;
            case R.drawable.selector_menu:
                return showBrowserContextMenu(view.getContext())||true;
            case R.string.refresh:
                return mBrowserAdapter.reset(null)||true;
            case R.string.create:
                return createFile()||true;
            case R.string.multiChoose:
                return entryMode(Mode.MODE_MULTI_CHOOSE,null,obj);
            case R.string.goTo:
                return true;
            case R.drawable.selector_cancel:
            case R.string.cancel:
                return entryMode(null,null)||true;
            case R.string.exit:
                return finishActivity()||true;
        }
        if (null!=obj&&obj instanceof File){
            File file=(File)obj;
            if (file.isDirectory()){
                return browserPath(file);
            }
            return openFile(file);
        }
        return false;
    }

    private boolean openFile(File openFile){
        Client client=mBrowserAdapter.getClient();
        if (!(null!=client&&client.openFile(openFile,getContext()))){
            return toast(getString(R.string.whichFailed,getString(R.string.open)));
        }
        return false;
    }

    public boolean entryMode(Integer modeInt, OnConfirm<Object,Boolean> onConfirm, Object... args){
        return mBrowserAdapter.entryMode(modeInt,onConfirm,args);
    }

    private boolean createFile(){
        Client client=mBrowserAdapter.getClient();
        if (null==client){
            return toast(getString(R.string.fail))&&false;
        }
        return null!=showContentDialog(new CreateFileDialogContent(getCurrentFolder()) {
            @Override
            protected boolean onCreate(File parent, String name, boolean createDir) {
                final OnFinish<Response<File>> callback=(Response<File> reply)-> {
                    boolean succeed=null!=reply&&reply.isSucceed()&&reply.getData()!=null;
                    BrowserModel.this.post(()->{
                        toast(getString(succeed?R.string.succeed:R.string.fail)+" "+(null!=reply?reply.getMessage():""));
                        if(succeed){
                            mBrowserAdapter.reset(null);
                        }
                    });
                };
                if (client instanceof LocalClient){
                    callback.onFinish(client.createFile(parent,name,createDir));
                    return true;
                }
                return execute(()-> callback.onFinish(client.createFile(parent,name,createDir)));
            }},null);
    }

    private boolean selectClients(View view,Client client){
        BrowserExecutor executor=mExecutor;
        if (null==view||null==executor){
            return false;
        }
        Context context=view.getContext();
        LinearLayout linearLayout=new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundResource(R.drawable.round_corner_black);
        final PopupWindow popupWindow=new PopupWindow(getContext());
        final LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin=params.rightMargin=5;
        executor.client(new MatchedCollector<Client>(-1){
            @Override
            protected Boolean onMatch(Client data) {
                if (null!=data&&(null==client||!data.equals(client))){
                    ItemClientNameBinding binding=inflate(context,R.layout.item_client_name);
                    if (null!=binding){
                        binding.setClient(data);
                        binding.setListener((OnClickListener)(View view, int clickId, int count, Object obj)-> {
                            mBrowserAdapter.setClient(data);
                            popupWindow.dismiss();
                            return true;
                        });
                        View root=binding.getRoot();
                        linearLayout.addView(root,params);
                    }
                }
                return false;
            }});
        if (linearLayout.getChildCount()<=0){
            return false;
        }
        popupWindow.setContentView((Context context1, ViewIterator iterator)-> linearLayout);
        return popupWindow.showAsDropDown(view,0,0, Gravity.CENTER);
    }

    private boolean showTaskDialog(Task task, DoingContent dialogContent){
        Executor executor=mExecutor;
        if (null==executor|null==task){
            return false;
        }
        final DoingContent content=null!=dialogContent?dialogContent:new DoingContent().setName(task.getName());
        content.addOnAttachStateChangeListener((OnViewAttachedToWindow)(View v)->
                executor.putListener(content, (Task data)-> null!=data&&data.equals(task),true));
        content.addOnAttachStateChangeListener((OnViewDetachedFromWindow)(View v)->executor.removeListener(content));
        return null!=showContentDialog(content, new FixedLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER).setMaxHeight(0.5f).setMaxWidth(0.8f));
    }

    private Folder getCurrentFolder(){
        ObservableField<Folder> field=mBrowserAdapter.getCurrentFolder();
        return null!=field?field.get():null;
    }

    public boolean browserBack(){
        Folder folder=getCurrentFolder();
        return null!=folder&&browserPath(folder.getParentFile());
    }

    @Override
    public void onPathSpanClick(File path, int start, int end, String value) {
        if (null!=value&&value.length()>0){
            browserPath(path.generateFile(value));
        }
    }

    @Override
    public void onStatusChanged(int status, Task task, Executor executor) {

    }

    private boolean browserPath(File file){
        BrowserListAdapter adapter=mBrowserAdapter;
        String searchInput=mSearchInput.get();
        return null!=file&&null!=adapter&&browserPath(new BrowseQuery(file,searchInput));
    }

    private boolean browserPath(BrowseQuery query){
        BrowserListAdapter adapter=mBrowserAdapter;
        return null!=adapter&&adapter.reset(query,null);
    }

    private boolean selectNextClient(){
        Client client=mBrowserAdapter.getClient();
        BrowserExecutor executor=mExecutor;
        if (null==client&&null!=executor){
            final Client[] clients=new Client[2];
            final int[] count=new int[]{0};
            executor.client((Client data)-> {
                if (null!=data){
                    count[0]+=1;
                    if (clients[1]==null&&clients[0]!=null){
                        return false;
                    }
                    clients[0]=null==clients[0]?data:clients[0];
                    if (clients[1]!=null){
                        clients[0]=data;
                        clients[1]=null;
                        return false;
                    }
                    clients[1]=null==client?data:client==data?data:clients[1];
                }
                return false;
            });
//            mClientCount.set(count[0]);
            return null!=clients[0]&&mBrowserAdapter.setClient(clients[0]);
        }
        return false;
    }

    private void refreshBrowserBinder(){
        BrowserExecutor conveyorBinder=mExecutor;
        if (null!=conveyorBinder) {
            conveyorBinder.putListener(this, null, false);
            selectNextClient();
        }
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        Intent intent=new Intent(v.getContext(), ConveyorService.class);
        bindService(intent, mServiceConnection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Debug.D("EEEE onServiceConnected "+service);
                if (null!=service&&service instanceof BrowserExecutor){
                    mExecutor=(BrowserExecutor) service;
                    refreshBrowserBinder();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Debug.D("EEEE onServiceDisconnected "+name);
            }
        }, Context.BIND_ABOVE_CLIENT|Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        ServiceConnection serviceConnection=mServiceConnection;
        if (null!=serviceConnection){
            mServiceConnection=null;
            unbindService(serviceConnection);
        }
    }

    private boolean launchTask(Task task,int option,boolean showDialog){
        return null!=task&&startTask(task,option,null)&&showDialog&&showTaskDialog(task,null);
    }

    private boolean startTask(Task task, int option, OnProgressChange change){
        Executor executor=mExecutor;
        return null!=executor&&executor.execute(task,option,change);
    }

    private boolean showBrowserContextMenu(Context context){
        return null!=showContentDialog(new BrowserMenuContextDialogContent().setTitle(getString(R.string.app_name)),
                context,new FixedLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
    }

    @Override
    public void onNewIntent(Activity activity, Intent intent) {
        handleIntentFileUpload(intent);
    }

    @Override
    public void onActivityStart(Activity activity) {
        handleIntentFileUpload(null!=activity?activity.getIntent():null);
    }

    private boolean handleIntentFileUpload(Intent intent){
        String action=null!=intent?intent.getAction():null;
        if (null!=action&&action.equals(Intent.ACTION_SEND)){
            Parcelable parcelable=intent.getParcelableExtra(Intent.EXTRA_STREAM);
            intent.removeExtra(Intent.EXTRA_STREAM);
            return null!=parcelable&&entryMode(Mode.MODE_UPLOAD, (Object data) ->{
                Folder folder=mBrowserAdapter.getFolder();
                if (null==folder||folder.isLocalFile()){
                    toast(getString(R.string.canNotOperateHere));
                }
                UriFileUploadTask uploadTask=new UriFileUploadTask(folder).add(parcelable);
                uploadTask.setName(getString(R.string.upload));
                return launchTask(uploadTask,Executor.Option.NONE,true);
            });
        }else if (null!=action&&action.equals(Intent.ACTION_SEND_MULTIPLE)){
            ArrayList<Parcelable> parcelables=intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            intent.removeExtra(Intent.EXTRA_STREAM);
            return entryMode(Mode.MODE_UPLOAD, (Object data)-> {
                Folder folder=mBrowserAdapter.getFolder();
                if (null==folder||folder.isLocalFile()){
                    toast(getString(R.string.canNotOperateHere));
                }
                UriFileUploadTask uploadTask=new UriFileUploadTask(folder).setUris(parcelables);
                uploadTask.setName(getString(R.string.upload));
                return launchTask(uploadTask,Executor.Option.NONE,true);
            });
        }
        return false;
    }

    public ObservableField<ListAdapter> getContentAdapter() {
        return mContentAdapter;
    }

    public BrowserListAdapter getBrowserAdapter() {
        return mBrowserAdapter;
    }
}
