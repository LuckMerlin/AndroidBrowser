package com.luckmerlin.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.luckmerlin.binding.DataBindingUtil;
import com.luckmerlin.core.ChangeUpdate;
import com.luckmerlin.core.OnChangeUpdate;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListAdapter<T> extends androidx.recyclerview.widget.ListAdapter<T,RecyclerView.ViewHolder> {
    private List<T> mDataList;
    private Map<Integer,Object> mFixedViewHolder;
    private WeakReference<RecyclerView> mRecyclerView;
    public static final int VIEW_TYPE_HEAD=-1;
    public static final int VIEW_TYPE_TAIL=-2;
    public static final int VIEW_TYPE_EMPTY=-3;
    public static final int VIEW_TYPE_DATA=-4;
    private OnItemAttachChange<T> mOnItemAttachChange;
    private Map<RecyclerView.ViewHolder,Object> mAttachViewHolders;

    protected ListAdapter() {
        this(new ItemCallback<>());
    }

    protected ListAdapter(DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    protected ListAdapter(AsyncDifferConfig<T> config) {
        super(config);
    }

    public final ListAdapter<T> setFixedHolder(int viewType,Object holder){
        Map<Integer,Object> viewHolders=mFixedViewHolder;
        if (null==holder){
            if (null!=viewHolders&&viewHolders.remove(viewType)!=null){
                if (viewHolders.size()<=0){
                    mFixedViewHolder=null;
                }
            }
        }else{
            (null!=viewHolders?viewHolders:(mFixedViewHolder=new HashMap<>())).put(viewType,holder);
        }
        return this;
    }

    public final boolean setData(List<T> data){
        int currentSize=getItemCount();
        mDataList=null!=data?new ArrayList<>(data):null;
        int dataSize=null!=data?data.size():0;
        if (currentSize>dataSize){
            notifyItemRangeRemoved(dataSize,currentSize-dataSize);
        }else if (currentSize<dataSize){
            notifyItemRangeInserted(currentSize,dataSize-currentSize);
        }
        if (currentSize>0){
            notifyItemRangeChanged(0,currentSize,"ItemData");
        }
        return true;
    }

    public final boolean add(T data){
        return add(Integer.MAX_VALUE,data);
    }

    public final boolean add(final int index,T data){
        if (null!=data){
            if (!isUiThread()){
                return postIfPossible(()->add(index,data),0);
            }
            int current=getSize();
            List<T> dataList=mDataList;
            dataList=null!=dataList?dataList:(mDataList=new ArrayList<>());
            int currentSize=0;
            int finalIndex=index<=0?0:index>(currentSize=dataList.size())?currentSize:index;
            dataList.add(finalIndex,data);
            if (current<=0){
                notifyDataSetChanged();
                return true;
            }
            notifyItemInserted(finalIndex);
            return true;
        }
        return false;
    }

    public final boolean remove(T data){
        if (!isUiThread()){
            return postIfPossible(()->remove(data),0);
        }
        List<T> dataList=null!=data?mDataList:null;
        if (null!=dataList){
            int index=dataList.indexOf(data);
            if (index>=0){
                dataList.remove(index);
                notifyItemRemoved(index);
                return true;
            }
        }
        return false;
    }

    public final boolean addAll(List<T> data){
        return addAll(Integer.MAX_VALUE,data);
    }

    public final boolean notifyAttachedItemChanged(){
        return notifyAttachedItemChanged(null);
    }

    public final boolean notifyAttachedItemChanged(Object payload){
        Map<RecyclerView.ViewHolder,Object> attachViewHolders=mAttachViewHolders;
        Collection<Object> collection=null!=attachViewHolders?attachViewHolders.values():null;
        if (null!=collection){
            for (Object child:collection) {
                if (child instanceof UpdateData){
                    child=((UpdateData)child).mData;
                }
                notifyFirstItemChanged(child,null!=payload?payload:"NotifyAttachedChanged");
            }
            return true;
        }
        return false;
    }

    public final boolean notifyFirstItemChanged(Object object,Object payload){
        int index=null!=object?indexPosition(object):null;
        if (index>=0){
            notifyItemChanged(index,null!=payload?payload:"NotifyFirst");
            return true;
        }
        return false;
    }

    public final boolean addAll(int start,List<T> data){
        int size=null!=data?data.size():-1;
        if (size>0){
            if (start<=0){
                for (int i = size-1; i >= 0; i--) {
                    add(0,data.get(i));
                }
            }else{
                for (int i = 0; i < size; i++) {
                    add(start,data.get(i));
                }
            }
            return true;
        }
        return false;
    }

    public final boolean clean(){
        List<T> dataList=mDataList;
        if (null!=dataList){
            mDataList=null;
            notifyDataSetChanged();
            return true;
        }
        return false;
    }

    public final T getFirst(){
        return getItem(0);
    }

    public final T getLatest(){
        return getItem(getSize()-1);
    }

    public final ListAdapter<T> setOnItemAttachChange(OnItemAttachChange<T> callback){
        mOnItemAttachChange=callback;
        return this;
    }

    public final OnItemAttachChange getOnItemAttachChange() {
        return mOnItemAttachChange;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position,  List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        onBindData(holder,position,payloads);
        tryMakeItemAttach(holder,getItem(position));
    }

    protected void onBindData(RecyclerView.ViewHolder holder, int position, List<Object> payloads){
        //Do nothing
    }

    public final boolean replace(T data){
        return replace(data,data);
    }

    public final boolean replace(Object obj,T data){
        List<T> dataList=mDataList;
        if (null==obj||null==data||null==dataList){
            return false;
        }else if (isUiThread()){
            int index = dataList.indexOf(obj);
            if (index>=0){
                dataList.remove(index);
                dataList.add(index,data);
                notifyItemChanged(index,"replace");
                return true;
            }
            return false;
        }
        return postIfPossible(()->replace(obj,data),0);
    }

    public final int indexPosition(Object data){
        List<T> dataList=null!=data?mDataList:null;
        return null!=dataList?dataList.indexOf(data):-1;
    }

//    public final boolean isAttached(T data){
//        Map<T,OnChangeUpdate> attachViewHolders=null!=data?mAttachViewHolders:null;
//        Set<T> set=null!=attachViewHolders?attachViewHolders.keySet():null;
//        if (null!=set){
//            for (T child:set) {
//                if (null!=child&&child.equals(data)){
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    @Override
    public void onAttachedToRecyclerView( RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        WeakReference<RecyclerView> current=mRecyclerView;
        mRecyclerView=null;
        if (null!=recyclerView){
            mRecyclerView=new WeakReference<>(recyclerView);
            if (recyclerView.getLayoutManager()==null){
                setLayoutManager(createLinearLayout(recyclerView.getContext()));
            }
        }
        if (null!=current){
            current.clear();
        }
    }

    @Override
    public void onDetachedFromRecyclerView( RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        WeakReference<RecyclerView> current=mRecyclerView;
        mRecyclerView=null;
        if (null!=current){
            current.clear();
        }
    }

    public Object getFixedViewHolder(int viewType){
        Map<Integer,Object> fixedViewHolder=mFixedViewHolder;
        return null!=fixedViewHolder?fixedViewHolder.get(viewType):null;
    }

    @Override
    public int getItemViewType(int position) {
        long total=getSize();
        if (position<=0){
            if (total<=0){
                return VIEW_TYPE_EMPTY;
            }
            return getFixedViewHolder(VIEW_TYPE_HEAD)==null?VIEW_TYPE_DATA:VIEW_TYPE_HEAD;
        }
        return position==total-1&&getFixedViewHolder(VIEW_TYPE_TAIL)!=null? VIEW_TYPE_TAIL:VIEW_TYPE_DATA;
    }

    public final Context getContext(){
        RecyclerView recyclerView=getRecyclerView();
        return null!=recyclerView?recyclerView.getContext():null;
    }

    public final RecyclerView getRecyclerView(){
        WeakReference<RecyclerView> reference=mRecyclerView;
        return null!=reference?reference.get():null;
    }

    public final static LinearLayoutManager createLinearLayout(Context context) {
        return createLinearLayout(context,RecyclerView.VERTICAL,false);
    }

    public final static LinearLayoutManager createLinearLayout(Context context,int orientation,boolean reverseLayout){
        return null!=context?new LinearLayoutManager(context,orientation,reverseLayout):null;
    }

    public final static GridLayoutManager createGridLayout(Context context, int spanCount){
        return createGridLayout(context,spanCount,RecyclerView.VERTICAL,false);
    }

    public final static GridLayoutManager createGridLayout(Context context, int spanCount,
                                                    int orientation, boolean reverseLayout){
        return null!=context?new GridLayoutManager(context,spanCount,orientation,reverseLayout){
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                super.onLayoutChildren(recycler, state);
                View view=state.getItemCount()==1&&getChildCount()==1?getChildAt(0):null;
                if (null!=view&&getItemViewType(view)==VIEW_TYPE_EMPTY){
                    int vWidth=view.getWidth();
                    int margin=(getWidth()-vWidth)>>1;
                    int top=view.getTop();
                    view.layout(margin,top,margin+vWidth,top+view.getHeight());
                }
            }
        }:null;
    }

    public final ListAdapter<T> setLayoutManager(RecyclerView.LayoutManager manager){
        RecyclerView recyclerView=null!=manager?getRecyclerView():null;
        if (null!=recyclerView){
            recyclerView.setLayoutManager(manager);
        }
        return this;
    }

    protected ViewGroup.LayoutParams onCreateViewHolderLayoutParams(ViewGroup parent, int viewType,RecyclerView.ViewHolder viewHolder){
        return null;
    }

    protected void onViewHolderCreated(ViewGroup parent, int viewType,RecyclerView.ViewHolder viewHolder){
        View view=null!=viewHolder?viewHolder.itemView:null;
        if (null!=view&&view.getLayoutParams()==null){
            ViewGroup.LayoutParams params=onCreateViewHolderLayoutParams(parent,viewType,viewHolder);
            if (null==params){
                params=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        viewType==VIEW_TYPE_EMPTY?ViewGroup.LayoutParams.MATCH_PARENT:ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            view.setLayoutParams(params);
        }
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Map<Integer,Object> fixedViewHolder=mFixedViewHolder;
        Context context=null!=parent?parent.getContext():null;
        Object object=null!=fixedViewHolder?fixedViewHolder.get(viewType):null;
        RecyclerView.ViewHolder viewHolder=null!=object?inflateViewHolder(context,object):null;
        viewHolder=null==viewHolder?inflateViewHolder(context, onCreateViewTypeHolder(viewType,parent)):viewHolder;
        View view=null!=viewHolder?viewHolder.itemView:null;
        if (null==view){
            viewHolder=new RecyclerView.ViewHolder(new View(parent.getContext())) {};
        }
        onViewHolderCreated(parent,viewType,viewHolder);
        return viewHolder;
    }

    protected Object onCreateViewTypeHolder(int viewType,ViewGroup parent){
        return null;
    }

    private void tryMakeItemAttach(RecyclerView.ViewHolder holder,T data){
        if (null!=data){
            Map<RecyclerView.ViewHolder,Object> attachViewHolders=mAttachViewHolders;
            attachViewHolders=null!=attachViewHolders?attachViewHolders:(mAttachViewHolders=new HashMap<>());
            if (data instanceof ChangeUpdate){
                final UpdateData updateData=new UpdateData(data) {
                    @Override
                    public boolean onChangeUpdated(Object newData) {
                        return replace(data);
                    }
                };
                ((ChangeUpdate)data).addChangeListener(updateData);
                attachViewHolders.put(holder,updateData);
            }else{
                attachViewHolders.put(holder,data);
            }
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        final T data=getItem(holder.getAdapterPosition());
        tryMakeItemAttach(holder,data);
        //
        OnItemAttachChange attachChange=mOnItemAttachChange;
        if (null!=attachChange){
            attachChange.onItemAttachChanged(true,holder,this);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        Map<RecyclerView.ViewHolder,Object> attachViewHolders=mAttachViewHolders;
        Object data=null!=holder&&null!=attachViewHolders?attachViewHolders.remove(holder):null;
        if (null!=data&&data instanceof UpdateData){
            OnChangeUpdate onChangeUpdate=((UpdateData)data);
            data=((UpdateData)data).mData;
            if (null!=data&&data instanceof ChangeUpdate){
                ((ChangeUpdate)data).removeChangeListener(onChangeUpdate);
            }
        }
        OnItemAttachChange attachChange=mOnItemAttachChange;
        if (null!=attachChange){
            attachChange.onItemAttachChanged(false,holder,this);
        }
    }

    @Override
    public int getItemCount() {
        int size=getSize();
        size=size<=0?0:size;
        Map<Integer,Object> holders=mFixedViewHolder;
        if (size<=0){
            return null!=holders&&null!=holders.get(VIEW_TYPE_EMPTY)?1:0;
        }
        return size+(null!=holders?(null!=holders.get(VIEW_TYPE_HEAD)?1:0)
                +(null!=holders.get(VIEW_TYPE_TAIL)?1:0):0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    protected final T getItem(int position) {
        List<T> dataList=mDataList;
        return position>=0&&null!=dataList&&dataList.size()>position?dataList.get(position):null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    public final boolean postIfPossible(Runnable runnable,int delay){
        RecyclerView recyclerView=null!=runnable?getRecyclerView():null;
        return null!=recyclerView&&(delay>0?recyclerView.postDelayed(runnable,delay):recyclerView.post(runnable));
    }

    public final boolean isUiThread(){
        Looper looper=Looper.myLooper();
        Looper uiLooper=Looper.getMainLooper();
        return null!=looper&&null!=uiLooper&&looper==uiLooper;
    }

    public final boolean checkContains(List<Object> list,Object obj){
        if (null!=list&&null!=obj){
            for (Object child:list) {
                if (null!=child&&child.equals(obj)){
                    return true;
                }
            }
        }
        return false;
    }

    public final RecyclerView.ViewHolder inflateViewHolder(Context context, Object viewHolder){
        if (null==viewHolder){
            return null;
        }else if (viewHolder instanceof RecyclerView.ViewHolder){
            return ((RecyclerView.ViewHolder)viewHolder).itemView==null?null:((RecyclerView.ViewHolder)viewHolder);
        }else if (viewHolder instanceof View){
            return inflateViewHolder(context,new RecyclerView.ViewHolder((View)viewHolder ) {});
        }else if (viewHolder instanceof ViewDataBinding){
            return inflateViewHolder(context,((ViewDataBinding)viewHolder).getRoot());
        }else if (viewHolder instanceof String){
            TextView textView=new TextView(context);
            textView.setText((String)viewHolder);
            return inflateViewHolder(context,textView);
        }else if (viewHolder instanceof Drawable){
            ImageView imageView=new ImageView(context);
            imageView.setImageDrawable((Drawable)viewHolder);
            return inflateViewHolder(context,imageView);
        }else if (viewHolder instanceof Bitmap){
            ImageView imageView=new ImageView(context);
            imageView.setImageBitmap((Bitmap)viewHolder);
            return inflateViewHolder(context,imageView);
        }
        Resources resources=null!=context?context.getResources():null;
        if (null==resources){
            return inflateViewHolder(context,"Error inflate view holder while invalid resources.");
        } else if (viewHolder instanceof Integer){
            try {
                String name=resources.getResourceTypeName((Integer)viewHolder);
                if (null==name){
                    return inflateViewHolder(context,(Integer)viewHolder);
                }else if (name.equals("string")){
                    return inflateViewHolder(context,resources.getString((Integer)viewHolder));
                }else if (name.equals("drawable")){
                    return inflateViewHolder(context,resources.getDrawable((Integer)viewHolder));
                }else if (name.equals("layout")){
                    try{
                        ViewDataBinding binding= DataBindingUtil.inflate(LayoutInflater.from(context),(Integer)viewHolder,null,true);
                        return inflateViewHolder(context,binding);
                    }catch (Exception e){
                        //Do nothing
                    }
                    return inflateViewHolder(context,LayoutInflater.from(context) .inflate((Integer)viewHolder,null,true));
                }
            }catch (Exception e){
                return inflateViewHolder(context,(Integer)viewHolder);
            }
        }
        return inflateViewHolder(context,viewHolder.toString());
    }

    public final int getSize(){
        List<T> dataList=mDataList;
        return null!=dataList?dataList.size():-1;
    }

    static class ItemCallback<T> extends DiffUtil.ItemCallback<T>{

        @Override
        public boolean areItemsTheSame( T oldItem,  T newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame( T oldItem,  T newItem) {
            return false;
        }
    }

    public interface OnItemAttachChange<T>{
        void onItemAttachChanged(boolean attach,RecyclerView.ViewHolder holder,ListAdapter<T> listAdapter);
    }

    private static abstract class UpdateData implements OnChangeUpdate{
        private final Object mData;

        public UpdateData(Object data){
            mData=data;
        }
    }

}
