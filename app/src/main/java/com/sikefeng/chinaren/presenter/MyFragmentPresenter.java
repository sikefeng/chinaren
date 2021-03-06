package com.sikefeng.chinaren.presenter;

import com.alibaba.android.arouter.launcher.ARouter;
import com.hss01248.dialog.StyledDialog;
import com.sikefeng.chinaren.R;
import com.sikefeng.chinaren.core.BasePresenter;
import com.sikefeng.chinaren.core.ServiceHelper;
import com.sikefeng.chinaren.entity.model.BaseData;
import com.sikefeng.chinaren.entity.model.UserData;
import com.sikefeng.chinaren.mvpvmlib.base.IRBaseView;
import com.sikefeng.chinaren.mvpvmlib.utils.LogUtils;
import com.sikefeng.chinaren.presenter.vm.MyFragmentViewModel;
import com.sikefeng.chinaren.utils.Constants;
import com.sikefeng.chinaren.utils.LoginUtil;
import com.sikefeng.chinaren.utils.SharePreferenceUtils;
import com.sikefeng.chinaren.utils.ToastUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.http.Query;

import static com.sikefeng.chinaren.utils.Constants.TOKEN;
import static com.sikefeng.chinaren.utils.Constants.userID;

/**
 * 文件名：ForgetPwdPresenter <br>
 * 创建时间： 2017/7/21 0021 下午16:19 <br>
 * 文件描述：<br>
 * 忘记密码数据协调器，主要用于组织数据
 *
 * @version v0.1  <br>
 * @since JDK 1.8
 */
public class MyFragmentPresenter extends BasePresenter<IRBaseView, MyFragmentViewModel> {

    /**
     * 构造函数设计
     *
     * @param view      view视图
     * @param viewModel 视图相关实体
     */
    public MyFragmentPresenter(IRBaseView view, MyFragmentViewModel viewModel) {
        super(view, viewModel);
    }

    @Override
    public void onCreate() {

    }


    public void exitLogin() {
        String token = (String) SharePreferenceUtils.get(getContext(), TOKEN, "");
        StyledDialog.buildLoading(getContext().getString(R.string.exiting)).show();
        addDisposable(ServiceHelper.getUsersAS().exitLogin(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<UserData>() {
                    @Override
                    public void onNext(UserData value) {
                        int status = value.getStatus();
                        String msg = value.getMsg();
                        LogUtils.i(status + "=========" + msg);
                        if (status == 0 && msg.equals("success")) {
                            ToastUtils.showShort(getContext().getString(R.string.exit_login_success));
                            LoginUtil.exitLogin();
                            ARouter.getInstance().build(Constants.LOGIN_URL).navigation();
                            getContext().finish();
                        } else {
                            ToastUtils.showShort(msg);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        StyledDialog.dismissLoading();
                    }

                    @Override
                    public void onComplete() {
                        StyledDialog.dismissLoading();
                    }
                })
        );
    }



}


