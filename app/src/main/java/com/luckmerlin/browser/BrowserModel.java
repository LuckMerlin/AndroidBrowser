package com.luckmerlin.browser;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.luckmerlin.binding.Binding;
import com.luckmerlin.binding.BindingGroup;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.binding.DataBindingUtil;
import com.luckmerlin.browser.client.LocalClient;
import com.luckmerlin.browser.databinding.BrowserModelBinding;
import com.luckmerlin.browser.databinding.ItemClientNameBinding;
import com.luckmerlin.browser.dialog.BrowserMenuContextDialogContent;
import com.luckmerlin.browser.dialog.ConfirmContent;
import com.luckmerlin.browser.dialog.CreateFileContent;
import com.luckmerlin.browser.dialog.DialogButtonBinding;
import com.luckmerlin.browser.dialog.DoingContent;
import com.luckmerlin.browser.dialog.FileContextDialogContent;
import com.luckmerlin.browser.dialog.GoToFolderContent;
import com.luckmerlin.browser.dialog.ModelMenuItemModel;
import com.luckmerlin.browser.dialog.RenameFileContent;
import com.luckmerlin.browser.file.Doing;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.FileArrayList;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.browser.task.FilesCopyTask;
import com.luckmerlin.browser.task.FilesDeleteTask;
import com.luckmerlin.browser.task.FilesMoveTask;
import com.luckmerlin.browser.task.UriFileUploadTask;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.click.OnLongClickListener;
import com.luckmerlin.core.Brief;
import com.luckmerlin.core.MatchedCollector;
import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.core.OnConfirm;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.core.Parser;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.dialog.FixedLayoutParams;
import com.luckmerlin.dialog.PopupWindow;
import com.luckmerlin.stream.ChunkInputStreamReader;
import com.luckmerlin.task.Confirm;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.Option;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Task;
import com.luckmerlin.view.ClickableSpan;
import com.luckmerlin.view.OnViewAttachedToWindow;
import com.luckmerlin.view.OnViewDetachedFromWindow;
import com.luckmerlin.view.ViewIterator;
import com.merlin.model.OnActivityCreate;
import com.merlin.model.OnActivityNewIntent;
import com.merlin.model.OnActivityStart;
import com.merlin.model.OnBackPress;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class BrowserModel extends BaseModel implements OnActivityCreate, Executor.OnStatusChangeListener,
        OnViewAttachedToWindow,PathSpanClick.OnPathSpanClick,
        OnViewDetachedFromWindow, OnClickListener, OnLongClickListener, OnBackPress,
        OnActivityNewIntent, OnActivityStart {
    private final BrowserListAdapter mBrowserAdapter=new BrowserListAdapter();
    private final ObservableField<ListAdapter> mContentAdapter=new ObservableField<>();
    private final ObservableField<AlertText> mAlertText=new ObservableField<>();
    private ServiceConnection mServiceConnection;
    private BrowserExecutor mExecutor;
    private Runnable mShowingAlert;
    private final LinkedList<AlertText> mAlertTextLinkList=new LinkedList<>();

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
        Mode mode=mBrowserAdapter.getCurrentMode();
        return (null!=mode&&mode.isMode(Mode.MODE_MULTI_CHOOSE)&&entryMode(null))||browserBack();
    }

    @Override
    public void onCreate(Bundle savedInstanceState, Activity activity) {
        mBrowserAdapter.setOnPathSpanClick(this);
        mContentAdapter.set(mBrowserAdapter);
        final ModelMenuItemModel rightMenu=new ModelMenuItemModel(R.drawable.selector_menu);
        setRightMenuBinding(new BindingGroup(new ModelMenuItemModel(R.drawable.selector_transport).
                setRotate(90),rightMenu));
        final Observable.OnPropertyChangedCallback changedCallback=new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                rightMenu.setMenuBinding(ViewBinding.clickId(mBrowserAdapter.getCurrentMode()!=null?
                        R.drawable.selector_cancel: R.drawable.selector_menu));
            }
        };
        mBrowserAdapter.getMode().addOnPropertyChangedCallback(changedCallback);
        changedCallback.onPropertyChanged(null,0);
//        showContentDialog(new DoingContent(),null);
//        showBrowserContextMenu(activity);
    }

    @Override
    public boolean onClick(View view,int clickId, int count, Object obj) {
        switch (clickId) {
            case R.drawable.selector_back:
                return browserBack() || true;
            case R.drawable.selector_close:
                if (null != obj && obj instanceof AlertText) {
                    Runnable runnable=mShowingAlert;
                    if (null!=runnable){
                        runnable.run();
                    }
                }
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
                return (null!=browserListAdapter&&browserPath(browserListAdapter.getFolder()))||true;
            case R.drawable.selector_menu:
                return showBrowserContextMenu(view.getContext())||true;
            case R.string.refresh:
                return mBrowserAdapter.reset(null)||true;
            case R.string.create:
                return createFile()||true;
            case R.string.rename:
                return renameFile(null!=obj&&obj instanceof File?(File)obj:null)||true;
            case R.string.multiChoose:
                return entryMode(new Mode(Mode.MODE_MULTI_CHOOSE).addArg(null!=obj&&obj instanceof File?(File)obj:null).
                        setBinding(new DialogButtonBinding(
                        ViewBinding.clickId(R.string.move),ViewBinding.clickId(R.string.copy),
                        ViewBinding.clickId(R.string.delete)).setListener((OnClickListener)
                        (View view1, int clickId1, int count1, Object obj1)->{
                        Mode mode=mBrowserAdapter.getCurrentMode();
                        FileArrayList args=null!=mode&&mode.isMode(Mode.MODE_MULTI_CHOOSE)?mode.getArgs():null;
                        entryMode(null);
                        return (null!=args&&args.size()>0&&BrowserModel.this.onClick(view1,clickId1,count,args))||true;
                })));
            case R.drawable.selector_checkbox:
                return null!=obj&&obj instanceof File&&toggleSelectFile((File)obj);
            case R.drawable.selector_cancel:
            case R.string.cancel:
                return entryMode(null)||true;
            case R.string.exit:
                return finishActivity()||true;
            case R.string.share:
                return null!=obj&&obj instanceof File&&shareFile((File)obj);
            case R.string.copy:
                return launchCopyFile(obj,Mode.MODE_COPY,getString(R.string.copy),Option.LAUNCH);
            case R.string.move:
                return launchCopyFile(obj,Mode.MODE_MOVE,getString(R.string.move),Option.LAUNCH);
            case R.string.delete:
                 return deleteFile(obj, true, false, (Result result)->
                         null!=result&&result.isSucceed()?1000:-1)||true;
            case R.string.goTo:
                return goToFolder()||true;
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

    private boolean launchCopyFile(Object fileObj,int mode,String taskName,int option){
        final File file=null!=fileObj&&fileObj instanceof File?(File)fileObj:null;
        return null!=file&&entryMode(new Mode(mode).makeSureBinding(
            (OnClickListener)(View view1, int clickId1, int count1, Object obj1)-> {
                Folder folder=mBrowserAdapter.getFolder();
                if (null==folder||folder.isChild(fileObj,false,true)){
                    toast(R.string.canNotOperateHere,-1);
                    return true;
                }
                String name=taskName;
                name=null!=name?name:folder.isLocalFile()?getString(file.isLocalFile()?R.string.copy:
                        R.string.download): getString(file.isLocalFile()?R.string.upload:R.string.copy);
                entryMode(null);
                return launchTask(new FilesCopyTask(new FileArrayList((File) fileObj),folder).
                        setName(name), option,true)||true;}));
    }

    private boolean openFile(File openFile){
        Client client=mBrowserAdapter.getClient();
        if (!(null!=client&&client.openFile(openFile,getContext()))){
            return toast(getString(R.string.whichFailed,getString(R.string.open)));
        }
        return false;
    }

    private Client getFileClient(Object obj){
        return mBrowserAdapter.getClient();
    }

    private boolean toggleSelectFile(File file){
        BrowserListAdapter listAdapter=null!=file?mBrowserAdapter:null;
        return null!=listAdapter&&listAdapter.isSelectedFile(file)?listAdapter.unSelectFile(file): listAdapter.selectFile(file);
    }

    public boolean entryMode(Object mode, Object... args){
        return entryMode(mode,null,args);
    }

    public boolean entryMode(Object mode, OnConfirm<Object,Boolean> onConfirm, Object... args){
        return mBrowserAdapter.entryMode(mode,onConfirm,args);
    }

    private boolean deleteFile(Object obj, boolean showDialog, boolean confirmed, DoingContent.AutoDismiss autoDismiss){
        Executor executor=mExecutor;
        if (null==executor||null==obj){
            toast(getString(R.string.whichFailed,getString(R.string.delete)));
            return false;
        }else if (obj instanceof File){
            return deleteFile(new FileArrayList((File) obj),showDialog,confirmed,autoDismiss);
        }else if (!(obj instanceof FileArrayList)||((FileArrayList)obj).size()<=0){
            return deleteFile(null,showDialog,confirmed,autoDismiss);
        }
        final FileArrayList files=(FileArrayList)obj;
        if (!confirmed){
            String message=files.makeDescription(getContext());
            final ConfirmContent confirmContent=new ConfirmContent();
            String title=getString(R.string.sureWhich,getString(R.string.delete));
            Confirm confirm=new Confirm();
            confirm.setName(title).setMessage(message);
            confirm.setBinding(new DialogButtonBinding(
            ViewBinding.clickId(R.string.sure).setListener((OnClickListener) (View view1, int clickId1, int count1, Object obj1)->
                    ((confirmContent.removeFromParent()||true)&&deleteFile(obj,showDialog,true,autoDismiss))||true),
            ViewBinding.clickId(R.string.cancel).setListener((OnClickListener) (View view1, int clickId1, int count1, Object obj1)->
                    confirmContent.removeFromParent()||true)));
            confirmContent.setConfirm(confirm);
            return null!=showContentDialog(confirmContent, new FixedLayoutParams().wrapContentAndCenter());
        }
        FilesDeleteTask filesDeleteTask=new FilesDeleteTask(files);
        filesDeleteTask.setCursor(0).setName(getString(R.string.delete));
        startTask(filesDeleteTask, Option.LAUNCH);
        return (showDialog&&showTaskDialog(mExecutor,filesDeleteTask,new DoingContent().setAutoDismiss(autoDismiss)))||true;
    }

    private boolean createFile(){
        Client client=mBrowserAdapter.getClient();
        Folder parent=getCurrentFolder();
        return null!=parent&&null!=client&&null!=showContentDialog(new CreateFileContent(){
            @Override
            protected boolean onCreateFile(String inputName, boolean createDir) {
                final OnFinish<Response<File>> callback=(Response<File> reply)-> {
                    boolean succeed=null!=reply&&reply.isSucceed()&&reply.getData()!=null;
                    BrowserModel.this.post(()->{
                        toast(getString(succeed?R.string.succeed:R.string.fail)+" "+(null!=reply?reply.getMessage():""));
                        if(succeed){
                            mBrowserAdapter.reset(null);
                        }
                    });
                };
                removeFromParent();
                if (client instanceof LocalClient){
                    callback.onFinish(client.createFile(parent,inputName,createDir));
                    return true;
                }
                return execute(()-> callback.onFinish(client.createFile(parent,inputName,createDir)));
            }}.setLayoutParams(new FixedLayoutParams().wrapContentAndCenter().
               setWidth(0.8f)).outsideDismiss(), new FixedLayoutParams().fillParentAndCenter());
    }

    private boolean renameFile(File file){
        final String path=null!=file?file.getPath():null;
        Client client=null==path||path.length()<=0?null:getFileClient(file);
        if(null==client){
            return toast(getString(R.string.whichFailed,getString(R.string.rename)))&&false;
        }
        return null!=showContentDialog(new RenameFileContent(){
            @Override
            protected boolean onRenameFile(String inputName, boolean createDir) {
                final OnFinish<Response<File>> callback=(Response<File> reply)-> {
                    File newFile=null!=reply&&reply.isSucceed()?reply.getData():null;
                    mBrowserAdapter.replace(file,newFile);
                    toast(getString(null!=newFile?R.string.succeed:R.string.fail)+" "+(null!=reply?reply.getMessage():""));
                };
                removeFromParent();
                if (client instanceof LocalClient){
                    callback.onFinish(client.rename(path,inputName));
                    return true;
                }
                return execute(()-> callback.onFinish(client.rename(path,inputName)));
        }}.setLayoutParams(new FixedLayoutParams().wrapContentAndCenter().
                setWidth(0.8f)).outsideDismiss(), new FixedLayoutParams().fillParentAndCenter());
    }


    private boolean goToFolder(){
        return null!=showContentDialog(new GoToFolderContent(){
            @Override
            protected boolean onGoToFolder(String inputPath) {
                removeFromParent();
                return browserPath(inputPath);
            }
        }.setLayoutParams(new FixedLayoutParams().wrapContentAndCenter().
                setWidth(0.8f)).outsideDismiss(), new FixedLayoutParams().fillParentAndCenter());
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
                            selectClient(data);
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

    private boolean showFileContextMenu(View view, File file){
        return null!=view&&null!=file&&null!=showContentDialog(new FileContextDialogContent(file).
                        outsideDismiss().setLayoutParams(new FixedLayoutParams().wrapContentAndCenter()),
                view.getContext(),new FixedLayoutParams().fillParentAndCenter());
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
    public boolean onLongClick(View view, int clickId, Object obj) {
        if (null!=obj&&obj instanceof File){
            return showFileContextMenu(view,(File)obj);
        }
        return true;
    }

    @Override
    public void onStatusChanged(int status, Task task, Executor executor) {
        showAlertText(new AlertText().setMessage(""+status+" "+(null!=task?task.getName():"")).setTimeout(1000));
        switch (status){
            case Executor.STATUS_FINISH:
                checkDoingFileSucceed(null!=task?task.getProgress():null);
                break;
        }
    }

    private boolean checkDoingFileSucceed(Progress progress){
        if (!isUiThread()){
            return post(()->checkDoingFileSucceed(progress));
        }
        BrowserListAdapter browserListAdapter=mBrowserAdapter;
        Doing doing=null!=progress?progress.getDoing():null;
        if (null==doing||!doing.isSucceed()||null==browserListAdapter){
            return false;
        }
        if (doing.isDoingMode(Mode.MODE_DELETE)){
            Brief brief=doing.getFrom();
            brief=null!=brief?brief:doing.getTo();
            return null!=brief&&brief instanceof File&&browserListAdapter.removeIfInFolder((File)brief);
        }else if (doing.isDoingMode(Mode.MODE_COPY)||doing.isDoingMode(Mode.MODE_UPLOAD)||doing.isDoingMode(Mode.MODE_DOWNLOAD)){
            Brief to=doing.getTo();
            File toFile=null!=to&&to instanceof File?(File)to:null;
            return null!=toFile&&browserListAdapter.isCurrentFolder(toFile)&& null!=(toFile=toFile.getParentFile())&&
                    showFolderFilesChangeAlert(toFile.getName());
        }else if (doing.isDoingMode(Mode.MODE_MOVE)){
            Brief brief=doing.getFrom();
            brief=null!=brief?brief:doing.getTo();
            return null!=brief&&brief instanceof File&&browserListAdapter.removeIfInFolder((File)brief);
        }
        return false;
    }

    private boolean browserPath(Object fileObj){
        if (null==fileObj){
            return false;
        }else if (fileObj instanceof File){
            return browserPath(((File)fileObj).getPath());
        }else if (fileObj instanceof String){
            BrowserListAdapter adapter=mBrowserAdapter;
            ObservableField<String> field=getSearchInput();
            String searchInput=null!=field?field.get():null;
            return null!=adapter&&browserPath(new BrowseQuery((String)fileObj,searchInput));
        }else if (fileObj instanceof BrowseQuery){
            BrowserListAdapter adapter=mBrowserAdapter;
            return null!=adapter&&adapter.reset((BrowseQuery) fileObj,null);
        }
        return false;
    }

    private boolean showFolderFilesChangeAlert(String name){
        SpannableStringBuilder builder=new SpannableStringBuilder();
        final int maxTitle=10;
        name=null!=name&&name.length()>maxTitle?name.substring(0,maxTitle)+"...":name;
        name=null!=name?name+" ":" ";
        boolean nameEmpty=null==name||name.length()<=0;
        String value=""+getString(R.string.contentChanged);
        builder.append(name).append(nameEmpty?value:value.toLowerCase()).append(".");
        int index=builder.length();
        builder.append((" "+getText(R.string.refresh)).toLowerCase());
        builder.setSpan(new ClickableSpan((View widget)->{
            mBrowserAdapter.reset(null);
            showAlertText(null);
        }),index,builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return showAlertText(new AlertText().setMessage(builder).setMovementMethod
                (LinkMovementMethod.getInstance()).setTimeout(-1));
    }

    private boolean showAlertText(AlertText alertText){
        if (null==alertText){//Clean all timeout alert
            mAlertText.set(null);
            return false;
        }else if (!isUiThread()){
            return post(()->showAlertText(alertText));
        }
        LinkedList<AlertText> list=mAlertTextLinkList;
        if (null==list){
            return false;
        }
        if (mShowingAlert==null){
            int timeout=-1;
            mAlertText.set(alertText);
            Runnable showingRunnable=mShowingAlert=new Runnable() {
                @Override
                public void run() {
                    Runnable runnable=mShowingAlert;
                    if (null!=runnable&&runnable==this){
                        removePost(this);
                        mShowingAlert=null;
                        showAlertText(list.size()<=0?null:list.removeLast());
                    }
                }
            };
            if (null!=alertText&&(timeout=(timeout=alertText.getTimeout())>10000?10000:timeout)>0){
                return post(showingRunnable,timeout);
            }
            return true;
        }
        list.remove(alertText);
        if (alertText.getTimeout()>=0){
            list.addLast(alertText);
        }else{
            list.addFirst(alertText);
        }
        return true;
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
            return null!=clients[0]&&selectClient(clients[0]);
        }
        return false;
    }

    private boolean selectClient(Client client){
        if (null!=client&&mBrowserAdapter.setClient(client)){
//            return client.loadMeta(getExecutor(), (Response<ClientMeta> data)-> {
//                mBrowserAdapter.setClient(client);
//            });
        }
        return false;
    }

    private void refreshBrowserBinder(){
        BrowserExecutor conveyorBinder=mExecutor;
        if (null!=conveyorBinder) {
            conveyorBinder.putListener(this, null, false);
            selectNextClient();
            //Test
//            startTask(new TestTask(getActivity()).setName("沙发沙发大a"),Option.EXECUTE_NOT_SAVE);
//            post(()-> startActivity(ConveyorActivity.class),1000);
            //Test
//            TestTask testTask=new TestTask(getActivity());
//            testTask.setName("eeeeeeeee");
//            createFile();
//            deleteFile(LocalClient.createLocalFile(new java.io.File("/")),true,true);
//            launchTask(testTask, Option.EXECUTE,true);
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
        return null!=task&&startTask(task,option)&&showDialog&&showTaskDialog(mExecutor,task,null);
    }

    private boolean startTask(Task task, int option){
        Executor executor=mExecutor;
        return null!=executor&&executor.execute(task,option);
    }

    private boolean showBrowserContextMenu(Context context){
        return null!=showContentDialog(new BrowserMenuContextDialogContent().setTitle(getString(R.string.app_name)).
                setLayoutParams(new FixedLayoutParams().wrapContentAndCenter().setMaxHeight(0.5f)).
                outsideDismiss(), context, new FixedLayoutParams().fillParentAndCenter());
    }

    private boolean shareFile(File file){
        if (null==file){
            return false;
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, "This is my text to send.");
        shareIntent.setType(file.getMime());
        return startActivity(shareIntent);
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
            return null!=parcelable&&entryMode(new Mode(Mode.MODE_UPLOAD).makeSureBinding((OnClickListener) (View view, int clickId, int count, Object obj)-> {
                Folder folder=mBrowserAdapter.getFolder();
                if (null==folder||folder.isLocalFile()){
                    toast(getString(R.string.canNotOperateHere));
                    return true;
                }
                entryMode(null);
                UriFileUploadTask uploadTask=new UriFileUploadTask(folder).add(parcelable);
                uploadTask.setName(getString(R.string.upload));
                return launchTask(uploadTask,Option.LAUNCH,true)&&false;
            }));
        }else if (null!=action&&action.equals(Intent.ACTION_SEND_MULTIPLE)){
            ArrayList<Parcelable> parcelables=intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            intent.removeExtra(Intent.EXTRA_STREAM);
            return entryMode(new Mode(Mode.MODE_UPLOAD).makeSureBinding((OnClickListener) (View view, int clickId, int count, Object obj)-> {
                Folder folder=mBrowserAdapter.getFolder();
                if (null==folder||folder.isLocalFile()){
                    toast(getString(R.string.canNotOperateHere));
                    return true;
                }
                entryMode(null);
                UriFileUploadTask uploadTask=new UriFileUploadTask(folder).setUris(parcelables);
                uploadTask.setName(getString(R.string.upload));
                return launchTask(uploadTask,Option.LAUNCH,true)&&false;
            }));
        }
        return false;
    }

    public ObservableField<ListAdapter> getContentAdapter() {
        return mContentAdapter;
    }

    public BrowserListAdapter getBrowserAdapter() {
        return mBrowserAdapter;
    }

    public ObservableField<AlertText> getAlertText() {
        return mAlertText;
    }
}
