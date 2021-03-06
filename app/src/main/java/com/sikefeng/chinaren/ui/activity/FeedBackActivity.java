package com.sikefeng.chinaren.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RadioGroup;

import com.sikefeng.chinaren.R;
import com.sikefeng.chinaren.core.BaseActivity;
import com.sikefeng.chinaren.databinding.ActivityFeedBackBinding;
import com.sikefeng.chinaren.entity.event.PermissionEvent;
import com.sikefeng.chinaren.mvpvmlib.base.RBasePresenter;
import com.sikefeng.chinaren.presenter.FeedBackPresenter;
import com.sikefeng.chinaren.presenter.vm.FeedBackViewModel;
import com.sikefeng.chinaren.utils.StringUtil;
import com.sikefeng.chinaren.utils.ToastUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FeedBackActivity extends BaseActivity<ActivityFeedBackBinding> {

    private FeedBackPresenter presenter;
    private String type="0"; //反馈类型 （0.意见反馈 1.Bug反馈）
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPermissionEvent(PermissionEvent event) {

    }

    @Override
    protected boolean isEnableEventBus() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_feed_back;
    }

    @Override
    protected RBasePresenter getPresenter() {
        if (null == presenter) {
            presenter = new FeedBackPresenter(this, new FeedBackViewModel());
        }
        return presenter;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        setToolbar();
        getBinding().setPresenter(presenter);
        getBinding().setViewModel(presenter.getViewModel());
        getBinding().btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = getBinding().etContent.getText().toString().trim();
                if (StringUtil.isBlank(content)) {
                    ToastUtils.showShort("请输入反馈内容");
                    return;
                }
                presenter.feedBack(content,type);
            }
        });
        RadioGroup radgroup = (RadioGroup) findViewById(R.id.radioGroup);
        //第一种获得单选按钮值的方法
        //为radioGroup设置一个监听器:setOnCheckedChanged()
        radgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId==R.id.rb_fun){
                        type="0";
                    }else if (checkedId==R.id.rb_bug){
                        type="1";
                    }
            }
        });
    }


    private void setToolbar() {
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        toolbar.setTitle("应用反馈");
        setSupportActionBar(toolbar);
        try {
            //给左上角图标的左边加上一个返回的图标
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
