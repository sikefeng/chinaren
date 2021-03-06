/**
 * Copyright (C) 2014-2017 <a href="http://www.sikefeng.com>">sikefeng</a> All Rights Reserved.
 */
package com.sikefeng.chinaren;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.BuildConfig;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.facebook.drawee.view.SimpleDraweeView;
import com.iflytek.cloud.SpeechError;
import com.sikefeng.chinaren.core.BaseActivity;
import com.sikefeng.chinaren.databinding.ActivityMainBinding;
import com.sikefeng.chinaren.entity.event.MainEvent;
import com.sikefeng.chinaren.entity.event.RvScrollEvent;
import com.sikefeng.chinaren.entity.model.AppBean;
import com.sikefeng.chinaren.entity.model.PhoneBean;
import com.sikefeng.chinaren.mvpvmlib.base.RBasePresenter;
import com.sikefeng.chinaren.ui.activity.FeedBackActivity;
import com.sikefeng.chinaren.ui.activity.RecognitionActivity;
import com.sikefeng.chinaren.ui.activity.UpdateUserInfoActivity;
import com.sikefeng.chinaren.ui.activity.UserInfoActivity;
import com.sikefeng.chinaren.ui.activity.VoicerListActivity;
import com.sikefeng.chinaren.ui.adapter.SimpleFragmentPagerAdapter;
import com.sikefeng.chinaren.ui.fragment.ContactsFragment;
import com.sikefeng.chinaren.ui.fragment.DiscoverFragment;
import com.sikefeng.chinaren.ui.fragment.MyFragment;
import com.sikefeng.chinaren.utils.AppExit2Back;
import com.sikefeng.chinaren.utils.AppUtil;
import com.sikefeng.chinaren.utils.Constants;
import com.sikefeng.chinaren.utils.PhoneUtils;
import com.sikefeng.chinaren.utils.ResUtils;
import com.sikefeng.chinaren.utils.SharePreferenceUtils;
import com.sikefeng.chinaren.utils.ToastUtils;
import com.sikefeng.chinaren.utils.speech.MyAccessibilityService;
import com.sikefeng.chinaren.utils.speech.SpeechRecognizerUtils;
import com.sikefeng.chinaren.widget.MovingImageView;
import com.sikefeng.chinaren.widget.MovingViewAnimator;
import com.sikefeng.chinaren.widget.PopupDialog;
import com.zhy.changeskin.SkinManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.sikefeng.chinaren.utils.Constants.MOTTO;
import static com.sikefeng.chinaren.utils.Constants.NICKNAME;


@Route(path = Constants.MAIN_URL)
public class MainActivity extends BaseActivity<ActivityMainBinding> implements PopupDialog.OnDismissListener {
    private NetworkChangeReceive networkChangeReceive;
    /**
     * TAB名称集合
     */
    private String[] tabTitles = ResUtils.getArrStr(R.array.tabTitles);
    private SpeechRecognizerUtils speechRecognizerUtils;
    private SimpleFragmentPagerAdapter pagerAdapter;
    private MovingImageView mivMenu;
    private List<AppBean> appList;//获取已安装APP应用列表
    private boolean isOpenDrawerLayout = false;
    private Context mContext;
    private MediaPlayer mediaPlayer;
    /**
     * 图片数组
     */
    private int[] mImgs = new int[]{R.drawable.selector_tab_weixin, R.drawable.selector_tab_friends, R.drawable.selector_tab_search,
            R.drawable.selector_tab_me};

    private MyFragment myFragment;

    /**
     * 订阅事件
     *
     * @param event 自定义事件类
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MainEvent event) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected RBasePresenter getPresenter() {
        return null;
    }


    @Override
    protected void init(Bundle savedInstanceState) {
        mContext = this;
        EventBus.getDefault().register(this);
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads().detectDiskWrites().detectNetwork()
                    .penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                    .penaltyLog().penaltyDeath().build());
        }
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, getBinding().drawerLayout, getBinding().toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        getBinding().drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        List<Fragment> fragmentList = new ArrayList<>();
        myFragment = new MyFragment();
        fragmentList.add(myFragment);
//        fragmentList.add(new HomeFragment());
//        fragmentList.add(new MyFragment());
        fragmentList.add(new ContactsFragment());
        fragmentList.add(new ContactsFragment());
//        fragmentList.add(NewsTabLayout.getInstance());
        fragmentList.add(new DiscoverFragment());
//        fragmentList.add(new newsFragment());
        getBinding().viewpager.setOffscreenPageLimit(fragmentList.size());
        pagerAdapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager(), fragmentList, java.util.Arrays.asList(tabTitles));

        getBinding().viewpager.setAdapter(pagerAdapter);//给ViewPager设置适配器
        getBinding().tablayout.setupWithViewPager(getBinding().viewpager);//将TabLayout和ViewPager关联起来

        getBinding().tablayout.setSelectedTabIndicatorHeight(0);
        for (int i = 0; i < tabTitles.length; i++) {
            //获得到对应位置的Tab
            TabLayout.Tab itemTab = getBinding().tablayout.getTabAt(i);
            if (itemTab != null) {
                //设置自定义的标题
                itemTab.setCustomView(R.layout.item_tab);
                TextView textView = (TextView) itemTab.getCustomView().findViewById(R.id.tv_name);
                textView.setText(tabTitles[i]);
                ImageView imageView = (ImageView) itemTab.getCustomView().findViewById(R.id.iv_img);
                imageView.setImageResource(mImgs[i]);
                if (i == 1) {
                    LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layout.setMargins(10, 0, 15, 0);
                    textView.setLayoutParams(layout);
                }
            }
        }
        getBinding().tablayout.getTabAt(0).getCustomView().setSelected(true);
        //演示“发送事件” （功能可以用FragmentA的实例调用内部方法实现滑动到顶部，eg: fragmentA.scrollToTop(); 在FragmentA中实现滚动方法即可）
        getBinding().toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //list回到顶部
                int tabIndex = getBinding().viewpager.getCurrentItem();
                EventBus.getDefault().post(new RvScrollEvent(tabIndex, 0));
            }
        });
        getBinding().toolbarTitle.setText(tabTitles[0]);
        getBinding().viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                getBinding().toolbarTitle.setText(tabTitles[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        checkNetWork(); //检测网络状态
        initVoiver(); //语音识别
        initLeftMenu(); //左侧滑动菜单


    }

    private void initVoiver() {
        speechRecognizerUtils = SpeechRecognizerUtils.getInstance(this);
        appList = MyAccessibilityService.appList;//获取已安装的应用信息
        if (appList == null) {
            appList = AppUtil.getInstance(this).getAllApk();
        }
        speechRecognizerUtils.setOnSpeechResultListener(new SpeechRecognizerUtils.OnSpeechResultListener() {
            @Override
            public void onListenerResult(String result, boolean write) {
                if (result.startsWith("打开")) {
                    for (AppBean bean : appList) {
                        if (result.contains(bean.getAppName())) {
                            if (popupDialog.isShowing()) {
                                popupDialog.dismiss();
                            }
                            startAPP(bean.getAppPackageName());
                            break;
                        }
                    }
                } else if (result.startsWith("呼叫") || result.startsWith("打电话给")) {
                    PhoneUtils phoneUtil = new PhoneUtils(mContext);
                    List<PhoneBean> phoneDtos = phoneUtil.getPhone();
                    for (PhoneBean bean : phoneDtos) {
                        if (result.contains(bean.getName()) || result.contains(bean.getTelPhone())) {
                            if (popupDialog.isShowing()) {
                                popupDialog.dismiss();
                            }
                            phoneUtil.callPhone(MainActivity.this, bean.getTelPhone());
                            return;
                        }
                    }
                }
            }

            @Override
            public void endOfSpeech() {
                popupDialog.dismiss();
            }

            @Override
            public void speechError(SpeechError error, boolean write) {
                popupDialog.dismiss();
            }

            @Override
            public void onListenering(String result) {
                etResulting.setText(result);
            }
        });
        getBinding().fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playRing(true,mContext,R.raw.start);
            }
        });
    }

    private void initLeftMenu() {
        String motto = (String) SharePreferenceUtils.get(mContext, MOTTO, "");
        String nickName = (String) SharePreferenceUtils.get(mContext, NICKNAME, "Learn and live.");
        ((TextView) getBinding().nvMenu.getHeaderView(0).findViewById(R.id.tvMotto)).setText(motto);
        ((TextView) getBinding().nvMenu.getHeaderView(0).findViewById(R.id.nickName)).setText(nickName);
        SimpleDraweeView headView = (SimpleDraweeView) getBinding().nvMenu.getHeaderView(0).findViewById(R.id.headView);
        ImageView ivEditInfo = (ImageView) getBinding().nvMenu.getHeaderView(0).findViewById(R.id.ivEditInfo);
        ivEditInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, UpdateUserInfoActivity.class));
            }
        });
        headView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, UserInfoActivity.class));
            }
        });
        mivMenu = (MovingImageView) getBinding().nvMenu.getHeaderView(0).findViewById(R.id.miv_menu);
        // 设置主题
        String themeUrl = SharePreferenceUtils.get(mContext, "theme", "").toString();
        if (!"".equals(themeUrl)) {
            mivMenu.setImageResource(Integer.parseInt(themeUrl));
        }
        getBinding().drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                mivMenu.pauseMoving();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (mivMenu.getMovingState() == MovingViewAnimator.MovingState.stop) {
                    mivMenu.startMoving();
                } else if (mivMenu.getMovingState() == MovingViewAnimator.MovingState.pause) {
                    mivMenu.resumeMoving();
                }
                isOpenDrawerLayout = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                mivMenu.stopMoving();
                isOpenDrawerLayout = false;
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                if (mivMenu.getMovingState() == MovingViewAnimator.MovingState.stop) {
                    mivMenu.startMoving();
                } else if (mivMenu.getMovingState() == MovingViewAnimator.MovingState.pause) {
                    mivMenu.resumeMoving();
                }
            }
        });

        getBinding().nvMenu.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.group_item_github:
                        startActivity(new Intent(mContext, FeedBackActivity.class));
                        break;
                    case R.id.group_item_more:
                        startActivity(new Intent(mContext, VoicerListActivity.class));
                        break;
                    case R.id.group_item_qr_code:
//                        boolean isChecked = (boolean) SharePreferenceUtils.get(mContext, "mode", false);
//                        SharePreferenceUtils.put(mContext, "mode", !isChecked);
//                        SkinManager.getInstance().changeSkin(!isChecked ? "night" : "");
                        SkinManager.getInstance().changeSkin("night");
                        break;
                    case R.id.group_item_share_project:
//                        String test = null;
//                        test.equals("");
                        startActivity(new Intent(mContext, RecognitionActivity.class));
                        break;
                    case R.id.item_model:
                        startActivity(new Intent(mContext, UserInfoActivity.class));
                        break;
                    case R.id.item_about:

                        break;
                }
                item.setCheckable(false);
                getBinding().drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }


    private PopupDialog popupDialog = null;
    private EditText etResulting;

    private void showVoiceListener(View view) {
        if (popupDialog == null) {
            popupDialog = new PopupDialog(MainActivity.this, R.layout.popup_voicer);
        }
        if (popupDialog.isShowing()) {
            popupDialog.dismiss();
            return;
        }
        popupDialog.setAnimation(android.R.style.Animation_InputMethod);
        popupDialog.showAtLocation(view, Gravity.BOTTOM);
        etResulting = popupDialog.getView(R.id.tv_result);
        etResulting.setText("");
        speechRecognizerUtils.setWaitTime(2 * 1000);
        speechRecognizerUtils.start(true);
        ImageView imageView1 = popupDialog.getView(R.id.ivLeft);
        ImageView imageView2 = popupDialog.getView(R.id.ivRight);

        AnimationDrawable animationDrawable1 = (AnimationDrawable) imageView1.getDrawable();
        AnimationDrawable animationDrawable2 = (AnimationDrawable) imageView2.getDrawable();
        animationDrawable1.start();
        animationDrawable2.start();
        popupDialog.setOnDismissListener(this);
    }

    @Override
    public void dissmiss() {
        if (speechRecognizerUtils.isListening()) {
            speechRecognizerUtils.stop();
        }
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
        playRing(false,mContext,R.raw.speen_end);
    }

    /**
     * 功能描述：TODO: 通过包名打开其它APP应用
     * 创建时间： 2018-05-23 14:16:05
     *
     * @param appPackageName
     * @author Sikefeng
     */
    public void startAPP(String appPackageName) {
        try {
            Intent intent = this.getPackageManager().getLaunchIntentForPackage(appPackageName);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "没有安装该应用", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isOpenDrawerLayout) {
                getBinding().drawerLayout.closeDrawers();
            } else {
                AppExit2Back.exitApp(MainActivity.this);//双击退出应用程序
            }
        }
        return false;
    }


    private void checkNetWork() {
        networkChangeReceive = new NetworkChangeReceive();
        //网络监听
        IntentFilter intentFilter = new IntentFilter();
        //addAction
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceive, intentFilter);
    }


    //网络连接监听
    class NetworkChangeReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                ToastUtils.showShort("网络已连接!");
            } else {
                ToastUtils.showShort("网络已断开,请重新连接!");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceive);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        myFragment.onActivityResult(requestCode, resultCode, data);
    }

    private static final float BEEP_VOLUME = 0.10f;
    private boolean isStart=false;
    private void playRing(boolean start,Context context,int raw) {
        isStart=start;
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (isStart){
                    showVoiceListener(findViewById(R.id.tablayout));
                }
            }
        });
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            AssetFileDescriptor file = context.getResources().openRawResourceFd(raw);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            } finally {
                file.close();
            }
            mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ioe) {
            mediaPlayer.release();
        }
    }


}
