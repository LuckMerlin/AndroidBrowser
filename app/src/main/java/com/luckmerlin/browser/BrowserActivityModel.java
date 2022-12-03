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
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;

import com.luckmerlin.binding.DataBindingUtil;
import com.luckmerlin.browser.client.LocalClient;
import com.luckmerlin.browser.databinding.BrowserActivityBinding;
import com.luckmerlin.browser.databinding.ItemClientNameBinding;
import com.luckmerlin.browser.dialog.CreateFileDialogContent;
import com.luckmerlin.browser.dialog.DoingContent;
import com.luckmerlin.browser.dialog.FileContextDialogContent;
import com.luckmerlin.browser.dialog.BrowserMenuContextDialogContent;
import com.luckmerlin.browser.file.DoingFiles;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.browser.task.UriFileUploadTask;
import com.luckmerlin.core.MatchedCollector;
import com.luckmerlin.core.OnConfirm;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.click.OnLongClickListener;
import com.luckmerlin.core.Response;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.dialog.FixedLayoutParams;
import com.luckmerlin.dialog.PopupWindow;
import com.luckmerlin.http.Request;
import com.luckmerlin.model.OnActivityCreate;
import com.luckmerlin.model.OnActivityNewIntent;
import com.luckmerlin.model.OnActivityStart;
import com.luckmerlin.model.OnBackPress;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.Option;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Task;
import com.luckmerlin.view.ClickableSpan;
import com.luckmerlin.view.OnViewAttachedToWindow;
import com.luckmerlin.view.OnViewDetachedFromWindow;
import com.luckmerlin.view.ViewIterator;
import com.luckmerlin.adapter.ListAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class BrowserActivityModel extends BaseModel implements OnActivityCreate, PathSpanClick.OnPathSpanClick,
        OnClickListener, OnLongClickListener, OnBackPress, OnViewAttachedToWindow,
        OnViewDetachedFromWindow, Executor.OnStatusChangeListener, OnActivityStart, OnActivityNewIntent {
    private ObservableField<ListAdapter> mContentAdapter=new ObservableField<>();
    private ObservableField<String> mNotifyText=new ObservableField<>();
    private final ObservableField<AlertText> mAlertText=new ObservableField<>();
    private final ObservableField<String> mSearchInput=new ObservableField<>();
    private final ObservableField<Integer> mClientCount=new ObservableField<>();
    private final ObservableField<Mode> mBrowserMode=new ObservableField<Mode>();
    private ServiceConnection mServiceConnection;
    private BrowserExecutor mExecutor;
    private final BrowserListAdapter mBrowserAdapter=new BrowserListAdapter();

    @Override
    protected View onCreateContent(Context context) {
        ViewDataBinding binding= DataBindingUtil.inflate(context,R.layout.browser_activity);
        if (null!= binding&&binding instanceof BrowserActivityBinding){
            ((BrowserActivityBinding)binding).setVm(this);
            return binding.getRoot();
        }
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState, Activity activity) {
        mBrowserAdapter.setOnPathSpanClick(this);
        mNotifyText.set("LMBrowser");
//        mContentAdapter.set(mBrowserAdapter);
        post(()->{
            try {
                String flag="li";
                StringBuffer buffer=new StringBuffer();
                buffer.append("\n");
                for (int i = 0; i < 10; i++) {
                    buffer.append("我爱小河马"+i);
                    buffer.append(flag+"\n");
                }
                buffer.append("我草撒旦法法术法");
                byte[] bytes=buffer.toString().getBytes();
                int[] readIndex=new int[]{0};
                InputStream inputStream=new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return readIndex[0]>=bytes.length?-1:bytes[readIndex[0]++];
                    }
                };

//                byte[] test=new byte[bytes.length];
//                inputStream.read(test);
//                Debug.D("dddd="+new String(test));
//                    InputStreamReader chunkInputStream=new InputStreamReader(inputStream,flag.getBytes());
//                    ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
//                    chunkInputStream.pipe(byteArrayOutputStream);
//                    Debug.D("dddd开始");
//                    int read=-1;
//                    while ((read=chunkInputStream.read())!=-1){
////                        Debug.D("写入 "+read);
//                        byteArrayOutputStream.write(read);
//                    }
//                    Debug.D("ddd结束"+new String(byteArrayOutputStream.toByteArray()));
            }catch (Exception e){

            }
        },2000);
        new Thread(new Runnable() {
            @Override
            public void run() {
//                new JavaHttp().setBaseUrl("http://192.168.0.10:5001").
//                        call(new Request<>().url("/file/test").
//                                setOnResponse(new ChunkParser()).post());
//                String url="http://mirror.nju.edu.cn/centos-altarch/7.9.2009/isos/aarch64/CentOS-7-aarch64-Minimal-2009.iso";
//                url="https://mirrors.neusoft.edu.cn/eclipse/oomph/epp/2022-09/R/eclipse-inst-jre-mac64.dmg";
                String url="http://192.168.0.10:5001/file/inputStream";
//                String url="http://192.168.0.10:5000";
//                Connection connection=new JavaHttp().test("POST",url,new Request().headerEncode("path",
//                        "/Volumes/Work/1983.搭错车.480P.国语中字.mp4/1983.搭错车.480P.国语中字.mp4").
//                        header("Content-Type","binary/octet-stream").header("from","0"));
//                connection.getRequested().getAnswer().getAnswerBody().getInputStream();

                Request request=new Request().headerEncode(Label.LABEL_PATH,"/Volumes/Work/1983.搭错车.480P.国语中字.mp4/1983.搭错车.480P.国语中字.mp4").
                        url("/file/inputStream").post();
//                Connection connection=new JavaHttp().setBaseUrl("http://192.168.0.10:6666").connect(request);
//                if (null==connection){
//                    Debug.W("Fail open file input stream.");
//                    return;
//                }
//                Requested requested=null!=connection?connection.getRequested():null;
//                Answer answer=null!=requested?requested.getAnswer():null;
//                AnswerBody answerBody=null!=answer?answer.getAnswerBody():null;
//                Headers headers=null!=answer?answer.getHeaders():null;
//                final long finalContentLength=headers.getLong("MerlinTotalLength",-1);
//                java.io.InputStream inputStream=null!=answerBody?answerBody.getInputStream():null;
//                try {
//                    Debug.W("skip&&&&&&&&.");
//                    inputStream.skip(100000);
//                } catch (IOException e) {
//                    Debug.W("DDDDDDDDD."+e);
//                    e.printStackTrace();
//                }
            }
        }).start();
//        mBrowserClient.set(new NasClient(getHttp()));
//        mBrowserClient.set(new LocalClient());
//        mNotifyText.set("");
//        entryMode(new Mode(Mode.MODE_MULTI_CHOOSE));
        //
//        showBrowserContextMenu(activity);
//        createFile();
//        startActivity(ConveyorActivity.class);
//        showFolderFilesChangeAlert("eeee");
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
            showAlertText(null,0);
        }),index,builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
       return showAlertText(new AlertText().setMessage(builder).setMovementMethod(LinkMovementMethod.getInstance()),0);
    }

    private boolean browserPath(File file){
        BrowserListAdapter adapter=mBrowserAdapter;
        String searchInput=mSearchInput.get();
        return null!=file&&null!=adapter&&adapter.reset(new BrowseQuery(file.getPath(),searchInput),null);
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
    public final boolean onBackPressed() {
        return browserBack();
    }

    @Override
    public void onPathSpanClick(File path, int start, int end, String value) {
        if (null!=value&&value.length()>0){
            browserPath(path.generateFile(value));
        }
    }

    public boolean entryMode(Integer modeInt, OnConfirm<Object,Boolean> onConfirm, Object... args){
        Mode current=mBrowserMode.get();
        if(null==current&&null==modeInt){
            Debug.D("Not need entry mode while not changed.");
            return true;
        }else if (null!=current&&null!=modeInt&&current.isMode(modeInt)){
            current.setOnConfirm(onConfirm);
            Debug.D("Not need entry mode while not changed."+current);
            return true;
        }
        Mode mode=null!=modeInt?new Mode(modeInt).setOnConfirm(onConfirm):null;
        mBrowserMode.set(mode);
//        mBrowserAdapter.setMode(mode);
        return true;
    }

//    @Deprecated
//    public boolean entryMode(Integer modeInt,Object obj,ModeFileTaskCreator creator){
//        Mode current=mBrowserMode.get();
//        if(null==current&&null==modeInt){
//            return true;
//        }else if (null!=current&&null!=modeInt&&current.isMode(modeInt)){
//            if (null==creator){
//                return true;
//            }
//            List args=current.getArgs();
//            if (null==args||args.size()<=0){
//                return true;
//            }
//            Client client=mBrowserAdapter.getClient();
//            Folder currentFolder=getCurrentFolder();
//            TaskGroup group=new TaskGroup();int size=0;
//            if (current.checkArgs((checkObj)->null!=checkObj&&checkObj instanceof File&& null!=group.
//                    add(creator.onCreateTask(current,client,(File)checkObj,currentFolder)))&&(size=group.getSize())>0){
//                Task task=size==1?group.find(null):group;
//                startTask(task, Executor.Option.NONE,null);
//                showTaskDialog(task,null);
//            }
//            entryMode(null,null,null);//Entry normal mode again
//            return true;
//        }
//        Mode mode=null!=modeInt?new Mode(modeInt).add(obj):null;
//        mBrowserMode.set(mode);
//        mBrowserAdapter.setMode(mode);
//        return true;
//    }

    private boolean startTask(Task task,int option){
        Executor executor=mExecutor;
        return null!=executor&&executor.execute(task,option);
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


//            new InputStreamReader(inputStream,flag.getBytes());

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
////                    Http http=new JavaHttp().setBaseUrl("http://192.168.0.10:5001");
////                    Connection connection=http.connect(new Request().url("/file/test").post());
////                    ChunkFileInputStream inputStream=new ChunkFileInputStream(connection);
////                    ChunkResponseParser<File> chunkJsonParse=new ChunkResponseParser<File>((data)->
////                            null!=data&&data instanceof JSONObject ?new File((JSONObject)data):null){
////                        @Override
////                        public Response<File> onParse(byte[] from) {
////                            Debug.D("EEEEE "+super.onParse(from));
////                            return null;
////                        }
////                    };
////                    inputStream.read(chunkJsonParse,chunkJsonParse);
////                    Utils.closeStream(connection);
////                  File fromFile=new File().setHost("192.168.0.10:5001").setSep("/").
////                            setParent("/Volumes/Work/Workspace/Browser").setName("app.py");
////                    File toFile=LocalClient.createLocalFile(new java.io.File
////                            ("/sdcard/Movies/lin.py"));
//
////                    File toFile=new File().setHost("192.168.0.10:5001").setSep("/").
////                            setParent("/Volumes/Work/Workspace/MerlinNodeServer/Common").setName("app.py1");
////                    File fromFile=LocalClient.createLocalFile(new java.io.File
////                            ("/sdcard/Movies/lin.py"));
//
//                    File fromFile=new File().setHost("192.168.0.10:5001").setSep("/").
//                            setParent("/Volumes/Work/2019/WTWD").setName("SP_Flash_Tool_exe_Windows_v5.1752.00.000.rar");
//                    File toFile=LocalClient.createLocalFile(new java.io.File
//                            ("/sdcard/Movies/SP_Flash_Tool_exe_Windows_v5.1752.00.000.rar"));
//                    new FileCopyTask1(fromFile,toFile,null).execute(new Runtime(0,null) {
//                        @Override
//                        public Executor getExecutor() {
//                            return conveyorBinder;
//                        }
//                    }, null);
//                }
//            }).start();
        }
    }

    @Override
    public void onStatusChanged(int status, Task task, Executor executor) {
        mNotifyText.set(""+status+" "+(null!=task?task.getName():""));
        switch (status){
            case Executor.STATUS_FINISH:
//                Progress progress=null!=task?task.getProgress():null;
//                Object object=null!=progress?progress.getData():null;
//                if (null!=object&&object instanceof DoingFiles){
//                    checkDoingFileSucceed((DoingFiles)object);
//                }
                break;
        }
    }

    private boolean checkDoingFileSucceed(DoingFiles files){
        BrowserListAdapter browserListAdapter=mBrowserAdapter;
        if (null==files||!files.isFinish()||null==browserListAdapter){
            return false;
        }
        if (files.isDoingMode(Mode.MODE_DELETE)){
            return browserListAdapter.removeIfInFolder(files.getFrom());
        }
        if (files.isDoingMode(Mode.MODE_MOVE)){
            browserListAdapter.removeIfInFolder(files.getFrom());
        }
        if (files.isDoingMode(Mode.MODE_COPY)||files.isDoingMode(Mode.MODE_MOVE)){
            File toFile=files.getTo();
            return null!=toFile&&browserListAdapter.isCurrentFolder(toFile)&&null!=(toFile=toFile.getParentFile())&&
                    showFolderFilesChangeAlert(toFile.getName());
        }
        return false;
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

    private boolean showAlertText(AlertText alertText,int timeout){
        mAlertText.set(alertText);
        CharSequence msg=null!=alertText?alertText.getMessage():null;
        if (null!=msg&&msg.length()>0&&(timeout=timeout>10000?10000:timeout)>0){
            return post(()->showAlertText(null,0),timeout);
        }
        return true;
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
            case R.drawable.selector_close:
                if (null!=obj&&obj instanceof AlertText){
                    showAlertText(null,0);
                }
                return true;
            case R.drawable.selector_cancel:
            case R.string.cancel:
                return entryMode(null,null)||true;
            case R.string.delete:
                return deleteFile(obj,true)||true;
            case R.drawable.selector_menu:
                return showBrowserContextMenu(view.getContext())||true;
            case R.string.multiChoose:
                return entryMode(Mode.MODE_MULTI_CHOOSE,null,obj);
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
                return null!=mode&&mode.enableAll(!mode.isAllEnabled())&&mBrowserAdapter.entryMode(mode);
            case R.string.share:
                return null!=obj&&obj instanceof File&&shareFile((File)obj);
            case R.string.exit:
                return finishActivity()||true;
            case R.string.sure:
                return makeModeSure(null)||true;
            case Mode.MODE_COPY:
            case Mode.MODE_UPLOAD:
            case Mode.MODE_DOWNLOAD:
                if (null==obj||!(obj instanceof File)){
                    return toast(getString(R.string.fail));
                }
                return entryMode(Mode.MODE_COPY, (Object obj1)-> {
                    Folder folder=mBrowserAdapter.getFolder();
                    if (null==folder||folder.isChild(obj,false)){
                        toast(R.string.canNotOperateHere,-1);
                        return null;
                    }
//                    return launchTask(new FileCopyTask((File)obj,folder,null).
//                            enableDeleteSucceed(true).setName(getString(R.string.copy)),
//                            Option.EXECUTE,true);
                    return true;
                });
            case R.string.move:
                if (null==obj||!(obj instanceof File)){
                    return toast(getString(R.string.fail));
                }
                return entryMode(Mode.MODE_MOVE,(Object obj1)-> {
                    Folder folder=mBrowserAdapter.getFolder();
                    if (null==folder||folder.isChild(obj,false)){
                        toast(R.string.canNotOperateHere,-1);
                        return null;
                    }
//                    return launchTask(new FileMoveTask((File)obj ,folder,null).
//                            enableDeleteSucceed(true).setName(getString(R.string.move)), Option.EXECUTE,true);
                    return true;
                });
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

    private boolean makeModeSure(Object arg){
        Mode mode=mBrowserMode.get();
        OnConfirm<Object,Boolean> onConfirm=null!=mode?mode.getOnConfirm():null;
        Boolean confirmed=null!=onConfirm?onConfirm.onConfirm(arg):null;
        return null!=confirmed&&confirmed&&entryMode(null,null);
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
//        FilesDeleteTask deleteTask=new FilesDeleteTask(file,null);
//        deleteTask.enableDeleteSucceed(true).setName(getString(R.string.delete));
//        return executor.execute(deleteTask, Executor.Option.NONE,null) &&showDialog&&
//                showTaskDialog(deleteTask, new DoingContent().setTitle(getString(R.string.delete)));
        return false;
    }

    private boolean shareFile(File file){
        if (null==file){
            return false;
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, "This is my text to send.");
        shareIntent.setType("text/plain");
        return startActivity(shareIntent);
    }

    private boolean openFile(File openFile){
        Client client=mBrowserAdapter.getClient();
        if (!(null!=client&&client.openFile((File)openFile,getContext()))){
            return toast(getString(R.string.whichFailed,getString(R.string.open)));
        }
        return false;
    }

    private boolean showTaskDialog(Task task, DoingContent dialogContent){
        Executor executor=mExecutor;
        if (null==executor|null==task){
            return false;
        }
        final DoingContent content=null!=dialogContent?dialogContent:new DoingContent();
        content.setTitle(task.getName());
        content.addOnAttachStateChangeListener((OnViewAttachedToWindow)(View v)->
                executor.putListener(content, (Task data)-> null!=data&&data.equals(task),true));
        content.addOnAttachStateChangeListener((OnViewDetachedFromWindow)(View v)->executor.removeListener(content));
        return null!=showContentDialog(content, new FixedLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER).setMaxHeight(0.5f).setMaxWidth(0.8f));
    }

    private boolean launchTask(Task task,int option,boolean showDialog){
        return null!=task&&startTask(task,option)&&showDialog&&showTaskDialog(task,null);
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
                return launchTask(uploadTask,Option.EXECUTE,true);
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
                return launchTask(uploadTask,Option.EXECUTE,true);
            });
        }
        return false;
    }

    private boolean showFileContextMenu(View view, File file){
        if (null==view||null==file){
            return false;
        }
        return null!=showContentDialog(new FileContextDialogContent(file),
                view.getContext(),new FixedLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER).setMaxHeight(0.5f).setMaxWidth(0.8f));
    }

    private boolean showBrowserContextMenu(Context context){
        return null!=showContentDialog(new BrowserMenuContextDialogContent().setTitle(getString(R.string.app_name)),
                context,new FixedLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER).setMaxHeight(0.5f).setMaxWidth(0.8f));
    }

    private boolean toggleSelectFile(File file){
        BrowserListAdapter listAdapter=null!=file?mBrowserAdapter:null;
        return null!=listAdapter&&listAdapter.isSelectedFile(file)?listAdapter.unSelectFile(file): listAdapter.selectFile(file);
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
                    BrowserActivityModel.this.post(()->{
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

    private boolean setCurrentAsHome(){
        Client client=mBrowserAdapter.getClient();
        Folder folder=getCurrentFolder();
        if (null==client||null==folder){
            return false;
        }
        return null!=client.setHome(folder,(Response<File> data)-> toast(getString(null!=data&&data.isSucceed()
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

    public ObservableField<AlertText> getAlertText() {
        return mAlertText;
    }

    private interface ModeFileTaskCreator{
        Task onCreateTask(Mode mode,Client client,File file,Folder folder);
    }
}
