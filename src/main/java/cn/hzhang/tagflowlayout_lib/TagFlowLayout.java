package cn.hzhang.tagflowlayout_lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by hzh on 2015/10/16.
 */
public class TagFlowLayout extends FlowLayout implements TagAdapter.OnDataChangeListener
{
    private TagAdapter mTagAdapter;
    private MotionEvent mMotionEvent;
    private OnTagClickListener mTagClickListener;
    private OnTagSelectedListener mTagSelectedListener;
    private Set<Integer> mSelectedView = new HashSet<Integer>();

    /**
     * 自定义属性
     */
    private boolean autoSelectEffect = true;   //是否可选开关，默认为true
    private int mSelectedMax = -1;           //默认为-1，不受限制

    public TagFlowLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TagFlowLayout);
        autoSelectEffect = a.getBoolean(R.styleable.TagFlowLayout_auto_select_effect, true);
        mSelectedMax = a.getInt(R.styleable.TagFlowLayout_max_select, -1);
        a.recycle();

        if(autoSelectEffect)
        {
            setClickable(true);
        }
    }

    public void setAdapter(TagAdapter adapter)
    {
        this.mTagAdapter = adapter;

        //响应notifyDataSetChanged()
        mTagAdapter.setOnDataChangeListener(this);

        changeAdatper();
    }

    /**
     * 首先移除所有子view，然后根据Adapter.getView()方法，开始逐个构造view，进行添加
     */
    private void changeAdatper()
    {
        removeAllViews();
        TagAdapter adapter = mTagAdapter;
        TagView container = null;

        for(int i = 0; i < adapter.getCount(); i++)
        {
            View tagView = adapter.getView(this, i, adapter.getItem(i));
            container = new TagView(getContext());
            //checked状态向下传递
            tagView.setDuplicateParentStateEnabled(true);
            container.setLayoutParams(tagView.getLayoutParams());
            container.addView(tagView);
            addView(container);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int count = getChildCount();

        for(int i = 0; i < count; i++)
        {
            TagView cView = (TagView) getChildAt(i);
            if(cView.getVisibility() == GONE) continue;
            if(cView.getTagView().getVisibility() == GONE)
            {
                cView.setVisibility(GONE);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 注意，如果子控件，消耗了事件，就到不了父控件的onTouchEvent
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        /**
         * 根据用户点击的坐标，判断用户是否点击了某个tag
         */
        if(event.getAction() == MotionEvent.ACTION_UP)
        {
            mMotionEvent = MotionEvent.obtain(event);
        }

        return super.onTouchEvent(event);
    }

    /**
     * 巧妙利用performClick()回调，来确定的确触发的click事件；不用自己去判断click条件
     * 但是，performClick()没有提供MotionEvent参数，我们在onTouchEvent记录了；
     * performClick()何时被执行？事件分发机制中有说明
     * @return
     */
    @Override
    public boolean performClick()
    {
        if(mMotionEvent == null)
            return super.performClick();

        //根据mMotionEvent查找点击事件，是否落在某个子view身上
        int x = (int) mMotionEvent.getX();
        int y = (int) mMotionEvent.getY();
        mMotionEvent = null;

        //通过position，找子view
        TagView child = findChild(x, y);
        if(child != null)
        {
            int pos = findPosByView(child);
            doSelect(child, pos);
            //如果autoSelectEffect开关关闭，就对外暴露click回调
            if(mTagClickListener != null)
            {
                return mTagClickListener.onTagClick(child, pos, this);
            }
        }

        return super.performClick();
    }

    private void doSelect(TagView child, int position)
    {
        //是否可选开关，默认可选；
        if(autoSelectEffect)
        {
            if(!child.isChecked())
            {
                //处理max_select=1的特殊情况，即单选特殊处理
                if(mSelectedMax == 1 && mSelectedView.size() == 1)
                {
                    int prePos = mSelectedView.iterator().next();
                    TagView preChild = (TagView) getChildAt(prePos);
                    preChild.setChecked(false);
                    child.setChecked(true);
                    mSelectedView.remove(prePos);
                    mSelectedView.add(position);
                }
                else
                {
                    if (mSelectedMax > 0 && mSelectedView.size() >= mSelectedMax)
                        return;

                    child.setChecked(true);
                    mSelectedView.add(position);
                }
            }else
            {
                child.setChecked(false);
                mSelectedView.remove(position);
            }

            //select监听器
            if(mTagSelectedListener != null)
            {
                mTagSelectedListener.onTagSelected(new HashSet(mSelectedView));
            }
        }
    }

    /**
     * 对外提供设置tag，为checked的方法
     *
     * positions: 表示需要更新的索引的集合
     */
    public void setSelectedTags(Set<Integer> positions, boolean ischecked)
    {
        if(positions.size() > 0)
        {
            for(int position : positions)
            {
                TagView tagView = (TagView) getChildAt(position);
                if(tagView == null) continue;
                tagView.setChecked(ischecked);
                if(ischecked)
                {
                    mSelectedView.add(position);
                }else
                {
                    mSelectedView.remove(position);
                }
            }
        }
    }

    public void setSelectedTag(int position, boolean isChecked)
    {
        TagView tagView = (TagView) getChildAt(position);
        if(tagView == null) return;

        tagView.setChecked(isChecked);
        if(isChecked)
        {
            mSelectedView.add(position);
        }else
        {
            mSelectedView.remove(position);
        }
    }

    public void setOnTagClickListener(OnTagClickListener listener)
    {
        this.mTagClickListener = listener;

        if(mTagClickListener != null)
        {
            //显示设置父view有消耗事件的能力
            setClickable(true);
        }
    }

    public void setOnTagSelectedListener(OnTagSelectedListener listener)
    {
        mTagSelectedListener = listener;
        if(mTagSelectedListener != null)
        {
            setClickable(true);
        }
    }

    @Override
    public void onChanged()
    {
        changeAdatper();
    }

    public Set<Integer> getSelectedList()
    {
        return new HashSet<Integer>(mSelectedView);
    }


    public static final String KEY_DEFAULT = "key_default";
    public static final String KEY_CHOOSE_POS = "key_choose_pos";
    /**
     * 状态保存
     * @return
     */
    @Override
    protected Parcelable onSaveInstanceState()
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_DEFAULT, super.onSaveInstanceState());

        String selectPos = "";
        if(mSelectedView.size() > 0)
        {
            for(int pos : mSelectedView)
            {
                selectPos += pos + "|";
            }
            selectPos = selectPos.substring(0, selectPos.length()-1);
        }
        bundle.putString(KEY_CHOOSE_POS, selectPos);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if(state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            String selectedPos = bundle.getString(KEY_CHOOSE_POS);
            if(!TextUtils.isEmpty(selectedPos))
            {
                String[] poses = selectedPos.split("\\|");
                for(String pos : poses)
                {
                    int posInt = Integer.parseInt(pos);
                    mSelectedView.add(posInt);

                    TagView tagView = (TagView) getChildAt(posInt);
                    tagView.setChecked(true);
                }
            }
            super.onRestoreInstanceState(bundle.getParcelable(KEY_DEFAULT));
            return;
        }

        super.onRestoreInstanceState(state);
    }

    private TagView findChild(int x, int y)
    {
        int count = getChildCount();
        for(int i = 0; i < count; i++)
        {
            TagView child = (TagView) getChildAt(i);
            if(child.getVisibility() == GONE) continue;

            Rect outRect = new Rect();
            child.getHitRect(outRect);
            if(outRect.contains(x, y))
            {
                return child;
            }
        }
        return null;
    }

    /**
     * 返回子view的position
     * @param child
     * @return
     */
    private int findPosByView(TagView child)
    {
        int count = getChildCount();
        for(int i = 0; i < count; i++)
        {
            TagView v = (TagView) getChildAt(i);
            if(v == child)
                return i;
        }
        return -1;
    }

    /**
     * click tag的回调
     */
    public interface OnTagClickListener
    {
        boolean onTagClick(View view, int position, FlowLayout parent);
    }

    /**
     * 选择tag的回调，多选；
     * 将已选择tag的set集合传出去
     */
    public interface OnTagSelectedListener
    {
        void onTagSelected(HashSet<Integer> selectedViews);
    }
}
