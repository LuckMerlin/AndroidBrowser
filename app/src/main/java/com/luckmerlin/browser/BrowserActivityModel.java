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
import android.view.ViewParent;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import com.luckmerlin.browser.binding.DataBindingUtil;
import com.luckmerlin.browser.databinding.BrowserActivityBinding;
import com.luckmerlin.browser.databinding.ItemClientNameBinding;
import com.luckmerlin.browser.dialog.ConfirmDialogContent;
import com.luckmerlin.browser.dialog.CreateFileDialogContent;
import com.luckmerlin.browser.dialog.FileContextDialogContent;
import com.luckmerlin.browser.dialog.MenuContextDialogContent;
import com.luckmerlin.browser.dialog.TaskDialogContent;
import com.luckmerlin.browser.file.DoingFiles;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.browser.task.FileCopyTask;
import com.luckmerlin.browser.task.FileDeleteTask;
import com.luckmerlin.browser.task.FileMoveTask;
import com.luckmerlin.core.MatchedCollector;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.Reply;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.click.OnLongClickListener;
import com.luckmerlin.core.Response;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.dialog.FixedLayoutParams;
import com.luckmerlin.dialog.PopupWindow;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Task;
import com.luckmerlin.task.TaskGroup;
import com.luckmerlin.view.Content;
import com.luckmerlin.view.OnViewAttachedToWindow;
import com.luckmerlin.view.OnViewDetachedFromWindow;
import com.luckmerlin.view.ViewIterator;
import com.merlin.adapter.ListAdapter;
import com.merlin.adapter.PageListAdapter;
import com.merlin.model.ContentActivity;
import com.merlin.model.OnActivityCreate;
import com.merlin.model.OnBackPress;
import java.util.List;

public class BrowserActivityModel extends BaseModel implements OnActivityCreate, PathSpanClick.OnPathSpanClick,
        OnClickListener, OnLongClickListener, OnBackPress , OnViewAttachedToWindow,
        OnViewDetachedFromWindow, Executor.OnStatusChangeListener,OnProgressChange {
    private ObservableField<ListAdapter> mContentAdapter=new ObservableField<>();
    private ObservableField<String> mNotifyText=new ObservableField<>();
    private final ObservableField<String> mSearchInput=new ObservableField<>();
    private final ObservableField<Integer> mClientCount=new ObservableField<>();
    private final ObservableField<Mode> mBrowserMode=new ObservableField<Mode>();
    private ServiceConnection mServiceConnection;
    private BrowserExecutor mExecutor;
    private final BrowserListAdapter mBrowserAdapter=new BrowserListAdapter();

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
        mBrowserAdapter.setOnPathSpanClick(this);
        mContentAdapter.set(mBrowserAdapter);
//        mBrowserClient.set(new NasClient(getHttp()));
//        mBrowserClient.set(new LocalClient());
//        mNotifyText.set("");
        mNotifyText.set("LMBrowser");
//        entryMode(new Mode(Mode.MODE_MULTI_CHOOSE));
        //
//        showBrowserContextMenu(activity);
//        createFile();
//        startActivity(ConveyorActivity.class);
    }

    private boolean browserPath(File file){
        BrowserListAdapter adapter=mBrowserAdapter;
        String searchInput=mSearchInput.get();
        return null!=file&&null!=adapter&&adapter.reset(new BrowseQuery(file,searchInput),null);
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
            mClientCount.set(count[0]);
            return null!=clients[0]&&mBrowserAdapter.setClient(clients[0]);
        }
        return false;
    }

    @Override
    public void onProgressChanged(Task task, Progress progress) {
        Object object=null!=progress?progress.getData():null;
        if (null!=object&&object instanceof DoingFiles){
            DoingFiles files=(DoingFiles)object;
            if (files.isFinish()&&files.isDoingMode(Mode.MODE_DELETE)){
                File fromFile=files.getFrom();
                BrowserListAdapter browserListAdapter=mBrowserAdapter;
                if (null!=fromFile&&null!=browserListAdapter&&browserListAdapter.
                        isCurrentFolder(fromFile.getPath())){
                    post(()->browserListAdapter.remove(fromFile));
                }
            }
        }
    }

    @Override
    public final boolean onBackPressed() {
        return browserBack();
    }

    @Override
    public void onPathSpanClick(File path, int start, int end, String value) {
        if (null!=value&&value.length()>0){
            browserPath(path.generateFile(value));
        }
    }

    public boolean entryMode(Integer modeInt,Object obj,ModeFileTaskCreator creator){
        Mode current=mBrowserMode.get();
        if(null==current&&null==modeInt){
            return true;
        }else if (null!=current&&null!=modeInt&&current.isMode(modeInt)){
            if (null==creator){
                return true;
            }
            List args=current.getArgs();
            if (null==args||args.size()<=0){
                return true;
            }
            Client client=mBrowserAdapter.getClient();
            Folder currentFolder=getCurrentFolder();
            TaskGroup group=new TaskGroup();int size=0;
            if (current.checkArgs((checkObj)->null!=checkObj&&checkObj instanceof File&& null!=group.
                    add(creator.onCreateTask(current,client,(File)checkObj,currentFolder)))&&(size=group.getSize())>0){
                Task task=size==1?group.find(null):group;
                startTask(task, Executor.Option.NONE,null);
                showTaskDialog(task,null);
            }
            entryMode(null,null,null);//Entry normal mode again
            return true;
        }
        Mode mode=null!=modeInt?new Mode(modeInt).add(obj):null;
        mBrowserMode.set(mode);
        mBrowserAdapter.setMode(mode);
        return true;
    }

    private boolean startTask(Task task,int option,OnProgressChange change){
        Executor executor=mExecutor;
        return null!=executor&&executor.execute(task,option,change);
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

    private void refreshBrowserBinder(){
        BrowserExecutor conveyorBinder=mExecutor;
        if (null!=conveyorBinder){
           conveyorBinder.putListener(this,null,false);
           selectNextClient();
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

    private Folder getCurrentFolder(){
        ObservableField<Folder> field=mBrowserAdapter.getCurrentFolder();
        return null!=field?field.get():null;
    }

    public boolean browserBack(){
        Folder folder=getCurrentFolder();
        return null!=folder&&browserPath(folder.getParentFile());
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
                return deleteFile(obj,true)||true;
            case R.drawable.selector_menu:
                return showBrowserContextMenu(view.getContext())||true;
            case R.string.multiChoose:
                return entryMode(Mode.MODE_MULTI_CHOOSE,obj,null);
            case R.string.setAsHome:
                return setCurrentAsHome()||true;
            case R.layout.item_client_name:
                return selectClients(view,null!=obj&&obj instanceof Client?(Client)obj:null)||true;
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
                    return new FileCopyTask(file,folder,null).setName(getString(R.string.copy));
                });
            case R.string.move:
                return entryMode(Mode.MODE_MOVE, obj, (Mode moveMode,Client client,File file, Folder folder)-> {
                    if (null==moveMode||null==folder||folder.isChild(file,false)){
                        toast(R.string.canNotOperateHere,-1);
                        return null;
                    }
                    return new FileMoveTask(file,folder,null).setName(getString(R.string.move));
                });
            case R.string.download:
                return entryMode(Mode.MODE_DOWNLOAD, obj, (Mode downloadMode,Client client,File file, Folder folder)-> {
                    if (null==downloadMode||null==folder||folder.isChild(file,false)){
                        toast(R.string.canNotOperateHere,-1);
                        return null;
                    }
                    return new FileCopyTask(file,folder,null).setName(getString(R.string.download));
                });
            case R.string.upload:
                return entryMode(Mode.MODE_UPLOAD, obj, (Mode uploadMode,Client client,File file, Folder folder)-> {
                    if (null==uploadMode||null==folder||folder.isChild(file,false)){
                        toast(R.string.canNotOperateHere,-1);
                        return null;
                    }
                    return new FileCopyTask(file,folder,null).setName(getString(R.string.upload));
                });
        }
        if (null!=obj&&obj instanceof File){
            File file=(File)obj;
            if (file.isDirectory()){
                return browserPath(file);
            }
            return toast("点击文件 "+file.getName());
        }
        return false;
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
                        linearLayout.addView(binding.getRoot());
                    }
                }
                return false;
            }});
        if (linearLayout.getChildCount()<=0){
            return false;
        }
        popupWindow.setContentView((Context context1, ViewIterator iterator)-> linearLayout);
        return popupWindow.showAsDropDown(view,0,0,Gravity.CENTER);
    }

    private boolean deleteFile(Object obj,boolean showDialog){
        Executor executor=mExecutor;
        if (null==executor||null==obj||!(obj instanceof File)){
            return false;
        }
        File file=(File)obj;
        FileDeleteTask deleteTask=new FileDeleteTask(file,null);
        return executor.execute(deleteTask, Executor.Option.CONFIRM,null) &&showDialog&&
                showTaskDialog(deleteTask, new TaskDialogContent().setTitle(getString(R.string.delete)));
    }

    private boolean showTaskDialog(Task task,TaskDialogContent dialogContent){
        Executor executor=mExecutor;
        if (null==executor|null==task){
            return false;
        }
        final TaskDialogContent content=null!=dialogContent?dialogContent:new TaskDialogContent();
        content.setTitle(task.getName());
        content.addOnAttachStateChangeListener((OnViewAttachedToWindow)(View v)->
                executor.putListener(content, (Task data)-> null!=data&&data.equals(task),true));
        content.addOnAttachStateChangeListener((OnViewDetachedFromWindow)(View v)->executor.removeListener(content));
        return null!=showContentDialog(content, new FixedLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
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
        Client client=mBrowserAdapter.getClient();
        return null!=showContentDialog(new CreateFileDialogContent(client,getCurrentFolder()){
            @Override
            protected void onCreate(Response<File> reply) {
                boolean succeed=null!=reply&&reply.isSucceed()&&reply.getData()!=null;
                toast(getString(succeed?R.string.succeed:R.string.fail)+" "+(null!=reply?reply.getMessage():""));
                if(succeed){
                    mBrowserAdapter.reset(null);
                }
            }
        },null);
    }

    private boolean setCurrentAsHome(){
        Client client=mBrowserAdapter.getClient();
        Folder folder=getCurrentFolder();
        if (null==client||null==folder){
            return false;
        }
        return null!=client.setHome(folder,(Reply<File> data)-> toast(getString(null!=data&&data.isSucceed()
                ?R.string.whichSucceed: R.string.whichFailed,getString(R.string.setAsHome))));
    }

    public ObservableField<ListAdapter> getContentAdapter() {
        return mContentAdapter;
    }

    public BrowserListAdapter getBrowserAdapter() {
        return mBrowserAdapter;
    }

    public ObservableField<Integer> getClientCount() {
        return mClientCount;
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
