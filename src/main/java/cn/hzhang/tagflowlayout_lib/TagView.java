package cn.hzhang.tagflowlayout_lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;

/**
 * Created by hzh on 2015/10/16.
 */
public class TagView extends FrameLayout implements Checkable
{
    private boolean isChecked;
    private static final int[] CHACK_STATE = new int[]{android.R.attr.state_checked};

    public TagView(Context context)
    {
        super(context);
    }

    public View getTagView()
    {
        return getChildAt(0);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace)
    {
        int[] states =  super.onCreateDrawableState(extraSpace+1);
        if (isChecked())
        {
           mergeDrawableStates(states, CHACK_STATE);
        }
        return states;
    }

    @Override
    public void setChecked(boolean checked)
    {
        if(this.isChecked != checked)
        {
            this.isChecked = checked;
            refreshDrawableState();
        }
    }

    @Override
    public boolean isChecked()
    {
        return isChecked;
    }

    @Override
    public void toggle()
    {
        setChecked(!isChecked);
    }
}
