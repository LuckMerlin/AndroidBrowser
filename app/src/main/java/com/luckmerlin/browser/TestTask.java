package com.luckmerlin.browser;

import android.app.Activity;
import android.content.Context;

import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.dialog.DialogButtonBinding;
import com.luckmerlin.core.Result;
import com.luckmerlin.task.AbstractTask;
import com.luckmerlin.task.ConfirmResult1;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;

public class TestTask extends AbstractTask {
    Activity activity;

    public TestTask(Activity activity){
        super(null);
        this.activity=activity;
    }

    @Override
    protected Result onExecute(Runtime runtime) {
//        if (System.currentTimeMillis()%2!=0){
            Progress progress=new Progress().setPosition(0).setTotal(60).setTitle("progressTitle").
                    setSpeed("666MB/S");
            while (progress.getPosition()<progress.getTotal()){
                progress.setPosition(progress.getPosition()+1);
                notifyProgress(progress);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            return null;
//        }
        return new ConfirmResult1() {
            @Override
            protected Confirm onCreate(Context context) {
                return new Confirm().setMessage("确认消息").setBinding(new DialogButtonBinding(
                        ViewBinding.clickId(R.string.sure),ViewBinding.clickId(R.string.cancel)));
            }
        };
//        while (true){
//            try {
//
////                notifyProgress();
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }



//        try {
//            HttpURLConnection connection= (HttpURLConnection) new URL("http://192.168.0.10:5001/file/test").openConnection();
//            connection.setRequestMethod("POST");
//            connection.setDoInput(true);
//            connection.connect();
//            InputStream inputStream=connection.getInputStream();
//            byte[] buffer=new byte[1024];
//            int length=0;
//            Debug.D("EEEEA  "+inputStream);
//            while ((length=inputStream.read(buffer))>=0){
//                    Debug.D("EEEE  "+new String(buffer,0,length));
//            }
//        } catch (IOException e) {
//            Debug.D("EEEEA  "+e);
//            e.printStackTrace();
//        }
//        OkHttpClient.Builder builder=new OkHttpClient.Builder();
//        OkHttpClient client=null!=builder?builder.build():null;
//        Request request=new Request.Builder().method("get",null).
//                url("http://192.168.0.10:5001/file/test").
//        cacheControl(CacheControl.FORCE_NETWORK).build();
//        try {
//            Answer response= client.newCall(request).execute();
//            BufferedSource bufferedSource=response.body().source();
//            if (null!=bufferedSource){
//                byte[] buffer=new byte[1024];
//                int length=0;
//                Debug.D("EEEE  "+bufferedSource);
//                while ((length=bufferedSource.read(buffer))>=0){
//                        Debug.D("EEEE  "+length);
//                }
//
//
////                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
////                Debug.D(" InputStreamReader="+r);
////                String line = "";
////                while (true) {
////                    try {
////                        if ((line = r.readLine()) == null){
////                            break;
////                        }
////                        Debug.D(" "+line);
////                    } catch (IOException e) {
////                        e.printStackTrace();
////                    }
////                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
    }
//    @Override
//    protected Object onExecute(Object arg) {
//        while (null!=activity &&!((Activity)activity).isDestroyed()){
//            try {
//                Progress progress=getProgress();
//                int[] ddd=new int[1];
//                final Random random=new Random();
//                progress=null!=progress?progress:new Progress() {
//                    @Override
//                    public long getTotal() {
//                        return 100;
//                    }
//
//                    @Override
//                    public long getPosition() {
//                        return ++ddd[0]>100?ddd[0]=0:ddd[0];
//                    }
//
//                    @Override
//                    public String getSpeed() {
//                        return random.nextInt(10000)+"MB";
//                    }
//
//                    @Override
//                    public String getTitle() {
//                        return random.nextInt(24234242)+"发达";
//                    }
//                };
//                notifyProgress(progress);
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
}
