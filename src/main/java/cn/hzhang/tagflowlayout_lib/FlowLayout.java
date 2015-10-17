package cn.hzhang.tagflowlayout_lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup
{

    public FlowLayout(Context context)
    {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 测量子view的宽和高
     * 测量自己的宽和高
     * @param widthMeasureSpec  父亲传来的测量值
     * @param heightMeasureSpec 父亲传来的测量值
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        //1.首先通过上一级传递进来的参数，得到测量值和测量模式
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);  //因为是match_parent所以得到的是容器的宽度
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);  //wrap_content得到的也是父控件的宽度

        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        /**
         * 如果我们在布局布局文件中填写的match_parent或者精确值，
         * 那么上面得到的就是准确的宽度和高度;
         *
         * 如果写的是wrap_content，上一级是无法设置我们的宽和高；因为上一级不知道我们里面有哪些子view
         * 那么我们这个view的宽和高，就需要我们根据其内部子view计算我们这个view的宽和高；
         *
         */

        //wrap_content
        int width = 0;
        int height = 0;
        /**
         *  如果是wrap_content，计算它的宽度和高度
         */

        //每一行的宽度和高度
        int lineWidth = 0;
        int lineHeight = 0;

        int cCount = getChildCount();
        /**
         * 在遍历过程中，记录每一行的lineWidth，lineHeight
         * 然后不断比较，不断更新width，height；
         * 遍历结束后，width就为view的宽度，height就为view的高度
         *
         * 这套逻辑真赞啊啊啊啊啊啊啊
         */
        for(int i = 0; i < cCount; i++)
        {
            View child = getChildAt(i);

            //通知子view测量自己，测量子view的宽和高，系统有提供方法
            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            //得到子view的layoutparmas
            //子View.getLayoutParams();得到的LayoutParams是它所在的父view决定的；
            //如果子view在LinearLayout，那么它拿到的就是LinearLayout.LayoutParams;
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            //子view的宽和高
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            if(lineWidth + childWidth > sizeWidth)      //换行
            {
                //记录最新的控件的width
                width = Math.max(width, lineWidth);

                //重置linewidth
                lineWidth = childWidth;

                //记录最新行高
                height += lineHeight;

                //重置行高
                lineHeight = childHeight;
            }else{      //同一行

                //叠加行宽
                lineWidth += childWidth;
                //得到当前行最大高度
                lineHeight = Math.max(lineHeight, childHeight);
            }

            //最后一个控件，特殊处理一下
            if(i == cCount-1)
            {
                width = Math.max(width, lineWidth);
                height += lineHeight;
            }
        }

       /* Log.e("TAG", "sizeWidth = " + sizeWidth + " ,modeWidth = " + modeWidth);
        Log.e("TAG", "sizeHeight = " + sizeHeight+ " ,modeHeight = " + modeHeight);
        Log.e("TAG", "width = " + width);
        Log.e("TAG", "height = " + height);*/

        //wrap_content
        //报告自己的宽和高，使用计算出来的宽高或者使用上一级传入的测量值
        //之后，父view就可以通过getMeasuredWidth来获得本view的宽度
        setMeasuredDimension(modeWidth == MeasureSpec.EXACTLY ? sizeWidth : width,
                modeHeight == MeasureSpec.EXACTLY ? sizeHeight : height);
    }

    /**
     * 设置子view的位置
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        List<List<View>> mAllViews = new ArrayList<List<View>>();
        List<View> mLineViews = new ArrayList<View>();
        List<Integer> mLineHeights = new ArrayList<Integer>();

        //此时，本view的宽和高已经有了
        int width = getWidth();
        int lineWidth = 0;
        int lineHeight = 0;

        int cCount = getChildCount();
        for(int i = 0; i < cCount; i++)
        {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            if(lineWidth + childWidth > width)  //换行
            {
                //1.添加一行到mAllViews
                mAllViews.add(mLineViews);

                //2.添加行高到列表
                mLineHeights.add(lineHeight);

                //3.重置lineWidth lineHeight
                lineHeight = childHeight;
                lineWidth = childWidth;

                //4.重新new一个新的mLineViews
                mLineViews = new ArrayList<View>();
                mLineViews.add(child);
            }else   //同一行
            {
                //1.更新lineWidth lineHeight
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);

                //2.添加view到列表mLineViews
                mLineViews.add(child);
            }

            if(i == cCount - 1)
            {
                //1.添加一行到mAllViews
                mAllViews.add(mLineViews);

                //2.添加行高到列表
                mLineHeights.add(lineHeight);
            }
        }   //end for

        //layout 子view，设置子view的位置
        int left = 0;
        int top = 0;
        for(int i = 0; i < mAllViews.size(); i++)
        {
            mLineViews = mAllViews.get(i);
            lineHeight = mLineHeights.get(i);

            for(int j = 0; j < mLineViews.size(); j++)
            {
                View child = mLineViews.get(j);
                if(child.getVisibility() == View.GONE)
                    continue;

                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                int lc = left + lp.leftMargin;
                int tc = top + lp.topMargin;
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();

                //设置子view的位置
                child.layout(lc, tc, rc, bc);

                left += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            }

            left = 0;
            top += lineHeight;
        }
    }

    /**
     * 与当前viewgroup对应的layoutparams
     * @param attrs
     * @return
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs)
    {
        return new MarginLayoutParams(getContext(), attrs);
    }
}
