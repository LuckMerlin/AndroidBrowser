package com.merlin.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.luckmerlin.debug.Debug;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ListAdapter<T> extends androidx.recyclerview.widget.ListAdapter<T,RecyclerView.ViewHolder> {
    private List<T> mDataList;
    private Map<Integer,Object> mFixedViewHolder;
    private WeakReference<RecyclerView> mRecyclerView;
    public static final int VIEW_TYPE_HEAD=0;
    public static final int VIEW_TYPE_TAIL=1;
    public static final int VIEW_TYPE_EMPTY=2;
    public static final int VIEW_TYPE_DATA=3;

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
        List<T> current=mDataList;
        int currentSize=null!=current?current.size():0;
        int dataSize=null!=data?data.size():0;
        mDataList=null!=data?new ArrayList<>(data):null;
        if (currentSize>dataSize){
            notifyItemRangeRemoved(dataSize,currentSize-dataSize);
        }else if (currentSize<dataSize){
            notifyItemRangeInserted(currentSize,dataSize-currentSize);
        }
        notifyItemRangeChanged(0,currentSize,"ItemData");
        return true;
    }

    public final boolean add(int index,T data){
        if (null!=data){
            List<T> dataList=mDataList;
            dataList=null!=dataList?dataList:(mDataList=new ArrayList<>());
            int currentSize=0;
            index=index<=0?0:index>(currentSize=dataList.size())?currentSize:index;
            dataList.add(index,data);
            notifyItemInserted(index);
            return true;
        }
        return false;
    }

    public final boolean remove(T data){
        if (null!=data){
            List<T> dataList=mDataList;
            int index=null!=dataList?dataList.indexOf(data):-1;
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

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        onBindData(holder,position,payloads);
    }

    protected void onBindData(RecyclerView.ViewHolder holder, int position, List<Object> payloads){
        //Do nothing
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        WeakReference<RecyclerView> current=mRecyclerView;
        mRecyclerView=null;
        if (null!=recyclerView){
            mRecyclerView=new WeakReference<>(recyclerView);
            if (recyclerView.getLayoutManager()==null){
                linearLayout();
            }
        }
        if (null!=current){
            current.clear();
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
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
        return position==total-1&&getFixedViewHolder(VIEW_TYPE_HEAD)!=null?VIEW_TYPE_TAIL:VIEW_TYPE_DATA;
    }

    public final RecyclerView getRecyclerView(){
        WeakReference<RecyclerView> reference=mRecyclerView;
        return null!=reference?reference.get():null;
    }

    public final ListAdapter<T> linearLayout() {
        return linearLayout(RecyclerView.VERTICAL,false);
    }

    public final ListAdapter<T> linearLayout(int orientation,boolean reverseLayout){
        RecyclerView recyclerView=getRecyclerView();
        Context context=null!=recyclerView?recyclerView.getContext():null;
        return null!=context?setLayoutManager(new LinearLayoutManager(context,orientation,reverseLayout)):this;
    }

    public final ListAdapter<T> setLayoutManager(RecyclerView.LayoutManager manager){
        RecyclerView recyclerView=null!=manager?getRecyclerView():null;
        if (null!=recyclerView){
            recyclerView.setLayoutManager(manager);
        }
        return this;
    }

    protected void onCreateViewHolderCreated(ViewGroup parent, int viewType,RecyclerView.ViewHolder viewHolder){
        View view=null!=viewHolder?viewHolder.itemView:null;
        if (null!=view&&view.getLayoutParams()==null){
            ViewGroup.LayoutParams params=new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(params);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Map<Integer,Object> fixedViewHolder=mFixedViewHolder;
        Context context=null!=parent?parent.getContext():null;
        Object object=null!=fixedViewHolder?fixedViewHolder.get(viewType):null;
        RecyclerView.ViewHolder viewHolder=null!=object?inflateViewHolder(context,object):null;
        viewHolder=null==viewHolder&&viewType==VIEW_TYPE_DATA?inflateViewHolder(context,
                onCreateDataViewHolder(parent)):viewHolder;
        View view=null!=viewHolder?viewHolder.itemView:null;
        if (null==view){
            viewHolder=new RecyclerView.ViewHolder(new View(parent.getContext())) {};
        }
        onCreateViewHolderCreated(parent,viewType,viewHolder);
        return viewHolder;
    }

    protected Object onCreateDataViewHolder(ViewGroup parent){
        return null;
    }

    @Override
    public int getItemCount() {
        int size=getSize();
        size=size<=0?0:size;
        Map<Integer,Object> holders=mFixedViewHolder;
        if (size<=0){
            return null!=holders.get(VIEW_TYPE_EMPTY)?1:0;
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
        return null!=dataList&&dataList.size()>position?dataList.get(position):null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

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
                        ViewDataBinding binding=DataBindingUtil.inflate(LayoutInflater.from(context),(Integer)viewHolder,null,true);
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
        public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
            return false;
        }
    }
}
