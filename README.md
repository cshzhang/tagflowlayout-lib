# tagflowlayout-lib
继承FlowLayout，实现对于Tag点击，选择交互；用户通过写selector标签自定义点击后TagView状态的变化

# 功能
* 以setAdapter形式注入数据
* 直接设置selector为background即可完成标签选则的切换，类似CheckBox
* 支持控制选择的Tag数量，比如：单选、多选
* 支持setOnTagClickListener，当点击某个Tag回调
* 支持setOnSelectListener，当选择某个Tag后回调
* 支持adapter.notifyDataChanged
* Activity重建（或者旋转）后，选择的状态自动保存

#用法
布局文件声明
```java
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
              xmlns:hzh="http://schemas.android.com/apk/res-auto"
              android:orientation="vertical"
              android:padding="16dp"
              android:layout_width="match_parent"
              android:layout_height="match_parent">

    <cn.hzhang.tagflowlayout_lib.TagFlowLayout
        android:id="@+id/id_tag_flowlayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        hzh:max_select="3"
		hzh:auto_select_effect="true"
        />

</LinearLayout>
```

支持属性：

`max_select`:-1为不限制选择数量；>=1控制选择数量；默认-1
`auto_select_effect`:true为开启选中效果，即为selector设置的效果；false为关闭；默认是true

#设置数据

```java
mFlowLayout.setAdapter(new TagAdapter<String>(mTitles)
   {
       @Override
       public View getView(FlowLayout parent, int position, String s)
       {
           TextView tv = (TextView) mInflater.inflate(R.layout.tv,
                   mFlowLayout, false);
           tv.setText(s);
           return tv;
       }
   });
```
getView的写法类似ListView中的ArrayAdapter的用法

#设置选中效果
不用写复杂的代码设置选中后标签的显示效果，写写布局文件就可以了；
```java
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:color="@color/tag_select_textcolor"
          android:drawable="@drawable/checked_bg"
          android:state_checked="true"></item>
    <item android:drawable="@drawable/normal_bg"></item>
</selector>
```
将selector标签设置为TextView的background就OK；

#事件

点击标签回调事件
```java
mTagFlowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener()
{
	@Override
	public boolean onTagClick(View view, int position, FlowLayout parent)
	{
		Toast.makeText(getActivity(), mDatas[position], Toast.LENGTH_SHORT).show();
		return true;
	}
});
```

选择多个标签回调事件
mTagFlowLayout.setOnTagSelectedListener(new TagFlowLayout.OnTagSelectedListener()
{
	@Override
	public void onTagSelected(HashSet<Integer> selectedViews)
	{
		getActivity().setTitle("Choose"+selectedViews.toString());
	}
});

#预先设置Item选中
```java
Set<Integer> set = new HashSet<Integer>();
set.add(1);
set.add(2);
set.add(3);
mTagFlowLayout.setSelectedTags(set, true);
```
