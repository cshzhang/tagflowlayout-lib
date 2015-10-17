package cn.hzhang.tagflowlayout_lib;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by hzh on 2015/10/16.
 *
 * flowlayout的数据以adapter的形式注入
 */
public abstract class TagAdapter<T>
{
    private Context mContext;
    private List<T> mTagDatas;
    private OnDataChangeListener mOnDataChangeListener;

    public TagAdapter(Context context, List<T> datas)
    {
        mContext = context;
        mTagDatas = datas;
    }

    public TagAdapter(Context context, T[] datas)
    {
        mContext = context;
        mTagDatas = new ArrayList<T>(Arrays.asList(datas));
    }

    static interface OnDataChangeListener
    {
        void onChanged();
    }

    public void setOnDataChangeListener(OnDataChangeListener listener)
    {
        mOnDataChangeListener = listener;
    }

    public int getCount()
    {
        return mTagDatas == null ? 0 : mTagDatas.size();
    }

    public T getItem(int posistion)
    {
        return mTagDatas.get(posistion);
    }

    public void notifyDataChanged()
    {
        if(mOnDataChangeListener != null)
        {
            mOnDataChangeListener.onChanged();
        }
    }

    public abstract View getView(FlowLayout parent, int position, T t);
}
