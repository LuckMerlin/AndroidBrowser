package com.luckmerlin.browser;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;

import com.luckmerlin.browser.binding.DataBindingUtil;
import com.luckmerlin.browser.client.LocalClient;
import com.luckmerlin.browser.databinding.BrowserActivityBinding;
import com.luckmerlin.browser.dialog.ConfirmDialogContent;
import com.luckmerlin.browser.dialog.CreateFileDialogContent;
import com.luckmerlin.browser.dialog.FileContextDialogContent;
import com.luckmerlin.browser.dialog.MenuContextDialogContent;
import com.luckmerlin.browser.file.DoingFiles;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.browser.task.FileCopyTask;
import com.luckmerlin.browser.task.FileDeleteTask;
import com.luckmerlin.browser.task.FileMoveTask;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.core.Reply;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.click.OnLongClickListener;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.dialog.FixedLayoutParams;
import com.luckmerlin.dialog.WindowContentDialog;
import com.luckmerlin.object.Parser;
import com.luckmerlin.stream.Convertor;
import com.luckmerlin.task.ConfirmResult;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Task;
import com.luckmerlin.task.TaskGroup;
import com.luckmerlin.view.OnViewAttachedToWindow;
import com.luckmerlin.view.OnViewDetachedFromWindow;
import com.merlin.adapter.ListAdapter;
import com.merlin.adapter.PageListAdapter;
import com.merlin.model.OnActivityCreate;
import com.merlin.model.OnBackPress;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class BrowserActivityModel extends BaseModel implements OnActivityCreate,
        PageListAdapter.OnPageLoadListener<File>, PathSpanClick.OnPathSpanClick,
        OnClickListener, OnLongClickListener, OnBackPress , OnViewAttachedToWindow,
        OnViewDetachedFromWindow, Executor.OnStatusChangeListener {
    private ObservableField<Client> mBrowserClient=new ObservableField<>();
    private ObservableField<ListAdapter> mContentAdapter=new ObservableField<>();
    private ObservableField<String> mNotifyText=new ObservableField<>();
    private ObservableField<Folder> mCurrentFolder=new ObservableField<>();
    private ObservableField<CharSequence> mCurrentPath=new ObservableField<>();
    private final ObservableField<String> mSearchInput=new ObservableField<>();
    private ObservableField<Mode> mBrowserMode=new ObservableField<Mode>();
    private final PathSpanClick mPathSpanClick=new PathSpanClick();
    private ServiceConnection mServiceConnection;
    private IBinder mConveyorBinder;
    private final BrowserListAdapter mBrowserAdapter=new BrowserListAdapter
            ((BrowseQuery args, File from, int pageSize, PageListAdapter.OnPageLoadListener<File> callback)->
             loadFiles(args, from, pageSize, null!=callback?(Reply<Folder> data)->
             callback.onPageLoad(null!=data&&data.isSucceed(),null!=data?data.
                     parser((Object child)->null!=child?new Folder(child):null).getData():null) :null));

    @Override
    protected View onCreateContent(Context context) {
        ViewDataBinding binding=DataBindingUtil.inflate(context,R.layout.browser_activity);
        if (null!= binding&&binding instanceof BrowserActivityBinding){
            ((BrowserActivityBinding)binding).setVm(this);
            return binding.getRoot();
        }
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState, Activity activity) {
        mBrowserAdapter.setOnPageLoadedListener(this);
        mPathSpanClick.setOnClickListener(this);
//        mBrowserClient.set(new NasClient(getHttp()));
        mBrowserClient.set(new LocalClient());
//        mNotifyText.set("");
        mContentAdapter.set(mBrowserAdapter);
        mNotifyText.set("LMBrowser");
//        entryMode(new Mode(Mode.MODE_MULTI_CHOOSE));
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
//        showBrowserContextMenu(activity);
//        createFile();
//        startActivity(ConveyorActivity.class);
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

    private Canceler loadFiles(BrowseQuery args, File from, int pageSize,OnFinish<Reply<Folder>> callback){
        Client client=getClient();
        if (null==client){
            return null;
        }
        execute(()->post(()->notifyFinish(client.loadFiles(args,from,pageSize),callback)));
        return ()->false;
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
    public final boolean onBackPressed() {
        return browserBack();
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

    public boolean entryMode(Integer modeInt,Object obj,ModeFileTaskCreator creator){
        Mode current=mBrowserMode.get();
        if(null==current&&null==modeInt){
            return true;
        }else if (null!=current&&null!=modeInt&&current.isMode(modeInt)){
            if (null!=creator){
                TaskGroup group=new TaskGroup();
                Client client=mBrowserClient.get();
                Folder currentFolder=mCurrentFolder.get();
                List args=current.getArgs();int size=0;
                if (null==args||args.size()<=0){
                    return toast(getString(R.string.inputEmpty))||true;
                }else if (current.checkArgs((checkObj)->null!=checkObj&&checkObj instanceof File&&
                        null!=group.add(creator.onCreateTask(current,client,(File)checkObj,currentFolder)))&& (size=group.getSize())>0){
                    startTask(size==1?group.find(null):group, Executor.Option.NONE,null);
                }
                entryMode(null,null,null);//Entry normal mode again
            }
            return true;
        }
        Mode mode=null!=modeInt?new Mode(modeInt).add(obj):null;
        mBrowserMode.set(mode);
        mBrowserAdapter.setMode(mode);
        return true;
    }

    private boolean startTask(Task task,int option,OnProgressChange change){
        IBinder binder=mConveyorBinder;
        if (null==binder||!(binder instanceof Executor)){
            return false;
        }
        return ((Executor)binder).execute(task,option,change);
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        Intent intent=new Intent(v.getContext(), ConveyorService.class);
        bindService(intent, mServiceConnection=new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        Debug.D("EEEE onServiceConnected "+service);
                        if (null!=service&&service instanceof Executor){
                            mConveyorBinder=service;
                            refreshConveyorBinder();
                        }
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        Debug.D("EEEE onServiceDisconnected "+name);
                    }
                }, Context.BIND_ABOVE_CLIENT|Context.BIND_AUTO_CREATE);
    }

    private void refreshConveyorBinder(){
        IBinder conveyorBinder=mConveyorBinder;
        if (null!=conveyorBinder&&conveyorBinder instanceof Executor){
            ((Executor)conveyorBinder).putListener(this,null);
        }
    }

    @Override
    public void onStatusChanged(int status, Task task, Executor executor) {
        mNotifyText.set(""+status+" "+(null!=task?task.getName():""));
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        ServiceConnection serviceConnection=mServiceConnection;
        if (null!=serviceConnection){
            mServiceConnection=null;
            unbindService(serviceConnection);
        }
    }

    @Override
    public boolean onLongClick(View view, int clickId, Object obj) {
        if (null!=obj&&obj instanceof File){
            return showFileContextMenu(view,(File)obj);
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
                return entryMode(null,null,null)||true;
            case R.string.delete:
                return deleteFile(obj)||true;
            case R.drawable.selector_menu:
                return showBrowserContextMenu(view.getContext())||true;
            case R.string.multiChoose:
                return entryMode(Mode.MODE_MULTI_CHOOSE,obj,null);
            case R.string.setAsHome:
                return setCurrentAsHome()||true;
            case R.string.conveyor:
                return startActivity(ConveyorActivity.class)||true;
            case R.string.refresh:
                BrowserListAdapter browserListAdapter=mBrowserAdapter;
                return null!=browserListAdapter&&browserListAdapter.reset(null);
            case R.string.create:
                return createFile()||true;
            case R.drawable.selector_checkbox:
                return null!=obj&&obj instanceof File&&toggleSelectFile((File)obj);
            case R.drawable.selector_choose_none:
            case R.drawable.selector_choose_all:
                Mode mode=mBrowserMode.get();
                return null!=mode&&mode.enableAll(!mode.isAllEnabled())&&mBrowserAdapter.setMode(mode);
            case R.string.exit:
                return finishActivity()||true;
            case R.string.copy:
                return entryMode(Mode.MODE_COPY, obj, (Mode copyMode,Client client,File file,Folder folder)-> {
                    if (null==copyMode||null==folder||folder.isChild(file,false)){
                        toast(R.string.canNotOperateHere,-1);
                        return null;
                    }
                    return new FileCopyTask(file,folder,null);
                });
            case R.string.move:
                return entryMode(Mode.MODE_MOVE, obj, (Mode moveMode,Client client,File file, Folder folder)-> {
                    if (null==moveMode||null==folder||folder.isChild(file,false)){
                        toast(R.string.canNotOperateHere,-1);
                        return null;
                    }
                    return new FileMoveTask(file,folder,null);
                });
            case R.string.download:
                return entryMode(Mode.MODE_DOWNLOAD, obj, (Mode downloadMode,Client client,File file, Folder folder)-> {
                    if (null==downloadMode||null==folder||folder.isChild(file,false)){
                        toast(R.string.canNotOperateHere,-1);
                        return null;
                    }
                    return new FileCopyTask(file,folder,null);
                });
            case R.string.upload:
                return entryMode(Mode.MODE_UPLOAD, obj, (Mode uploadMode,Client client,File file, Folder folder)-> {
                    if (null==uploadMode||null==folder||folder.isChild(file,false)){
                        toast(R.string.canNotOperateHere,-1);
                        return null;
                    }
                    return new FileCopyTask(file,folder,null);
                });
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

    private boolean deleteFile(Object obj){
        if (null==obj||!(obj instanceof File)){
            return false;
        }
        Client client=mBrowserClient.get();
        File file=(File)obj;
        ConfirmDialogContent content=new ConfirmDialogContent(new ConfirmResult.Confirm().
                setTitle(getString(R.string.confirmWhich,getString(R.string.delete))).setMessage(file.getName()).
                setOnConfirm((boolean confirm)-> new FileDeleteTask(file,null)));
        return null!=client&&null!=showContentDialog(content.setOnConfirmFinish((boolean confirmed, Object confirmObj)-> {
                    if (null==confirmObj||!(confirmObj instanceof FileDeleteTask)||!confirmed){
                        return null;
                    }
                    IBinder binder=mConveyorBinder;
                    final Executor executor=null!=binder&&binder instanceof Executor?(Executor)binder:null;
                    if (null==executor){
                        toast(getString(R.string.error));
                        return null;
                    }
                    FileDeleteTask deleteTask=(FileDeleteTask)confirmObj;
                    final Executor.OnStatusChangeListener listener=(int status, Task task, Executor executor1)-> {
                        Progress progress=null!=task?task.getProgress():null;
                        Object progressObj=null!=progress?progress.getData():null;
                        if (null!=progressObj&&progressObj instanceof DoingFiles){
                            DoingFiles doingFiles=(DoingFiles)progressObj;
                            post(()->{
                                content.setNotify(doingFiles.getTitle());
                                BrowserListAdapter browserAdapter=mBrowserAdapter;
                                File fromFile;
                                if (null!=browserAdapter&&null!=(fromFile=doingFiles.getFrom())&&browserAdapter.
                                        isCurrentFolder(fromFile.getPath())){
                                    mBrowserAdapter.remove(fromFile);
                                }
                            });
                        }
                        if (status==Executor.STATUS_FINISH&&null!=task&&task==deleteTask){
                            post(()->content.removeFromParent());
                        }
                    };
                    content.addOnAttachStateChangeListener((OnViewDetachedFromWindow)(View v)-> executor.removeListener(listener));
                    executor.putListener(listener,(Task data) ->null!=data&&data==deleteTask);
                    return executor.execute(deleteTask, Executor.Option.NONE,null);
                }),
                new FixedLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER).setMaxHeight(0.5f));
    }

    private boolean showFileContextMenu(View view,File file){
        if (null==view||null==file){
            return false;
        }
        return null!=showContentDialog(new FileContextDialogContent(file),
                view.getContext(),new FixedLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER).setMaxHeight(0.5f));
    }

    private boolean showBrowserContextMenu(Context context){
        return null!=showContentDialog(new MenuContextDialogContent().setTitle(getString(R.string.app_name)),
                context,new FixedLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER).setMaxHeight(0.5f));
    }

    private boolean toggleSelectFile(File file){
        BrowserListAdapter listAdapter=null!=file?mBrowserAdapter:null;
        return null!=listAdapter&&listAdapter.isSelectedFile(file)?listAdapter.unSelectFile(file): listAdapter.selectFile(file);
    }

    private boolean createFile(){
        Client client=mBrowserClient.get();
        return null!=showContentDialog(new CreateFileDialogContent(client,mCurrentFolder.get()){
            @Override
            protected void onCreate(Reply<File> reply) {
                boolean succeed=null!=reply&&reply.isSucceed()&&reply.getData()!=null;
                toast(getString(succeed?R.string.succeed:R.string.fail)+" "+(null!=reply?reply.getMessage():""));
                if(succeed){
                    mBrowserAdapter.reset(null);
                }
            }
        },null);
    }

    private boolean setCurrentAsHome(){
        Client client=mBrowserClient.get();
        Folder folder=mCurrentFolder.get();
        if (null==client||null==folder){
            return false;
        }
        return null!=client.setHome(folder,(Reply<File> data)->
                toast(getString(null!=data&&data.isSucceed()?R.string.whichSucceed:
                    R.string.whichFailed,getString(R.string.setAsHome))));
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

    private interface ModeFileTaskCreator{
        Task onCreateTask(Mode mode,Client client,File file,Folder folder);
    }
}
