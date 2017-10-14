package com.sikefeng.chinaren.presenter;

import com.alibaba.android.arouter.launcher.ARouter;
import com.hss01248.dialog.StyledDialog;
import com.sikefeng.chinaren.R;
import com.sikefeng.chinaren.core.BasePresenter;
import com.sikefeng.chinaren.core.ServiceHelper;
import com.sikefeng.chinaren.entity.event.ForgetPwdvent;
import com.sikefeng.chinaren.entity.model.BaseData;
import com.sikefeng.chinaren.entity.model.UserBean;
import com.sikefeng.chinaren.entity.model.UserData;
import com.sikefeng.chinaren.presenter.vm.ForgetPwdViewModel;
import com.sikefeng.chinaren.utils.Constants;
import com.sikefeng.chinaren.utils.LoginUtil;
import com.sikefeng.chinaren.utils.ToastUtils;
import com.sikefeng.mvpvmlib.base.IRBaseView;
import com.sikefeng.mvpvmlib.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * 文件名：ForgetPwdPresenter <br>
 * 创建时间： 2017/7/21 0021 下午16:19 <br>
 * 文件描述：<br>
 * 忘记密码数据协调器，主要用于组织数据
 *
 * @version v0.1  <br>
 * @since JDK 1.8
 */
public class ForgetPwdPresenter extends BasePresenter<IRBaseView, ForgetPwdViewModel> {

    /**
     * 构造函数设计
     *
     * @param view      view视图
     * @param viewModel 视图相关实体
     */
    public ForgetPwdPresenter(IRBaseView view, ForgetPwdViewModel viewModel) {
        super(view, viewModel);
    }

    @Override
    public void onCreate() {

    }

    /**
     * 功能描述：获取手机验证码
     * <br>创建时间： 2017-07-21 16:22:26
     *
     * @param tel 手机号码
     */
    public void getCode(String tel) {
        StyledDialog.buildLoading(getContext().getString(R.string.ver_code)).show();
        addDisposable(ServiceHelper.getUsersAS().getCode(tel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<UserData>() {
                    @Override
                    public void onNext(UserData value) {
                        int status = value.getStatus();
                        String msg = value.getMsg();
                        LogUtils.i(status + "=========" + msg);
                        if (status == 0 && msg.equals("success")) {
                            EventBus.getDefault().post(new ForgetPwdvent().withType("success"));
                            ToastUtils.showShort(getContext().getString(R.string.send_code_success));
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

    /**
     * 功能描述：修改用户密码
     * <br>创建时间： 2017-07-21 16:23:06
     *
     * @param userBean 用户实体类
     */
    public void forgetPwd(UserBean userBean) {
        StyledDialog.buildLoading(getContext().getString(R.string.update_pwd_tips)).show();
        addDisposable(ServiceHelper.getUsersAS().forgetPwd(userBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<BaseData>() {
                    @Override
                    public void onNext(BaseData value) {
                        int status = value.getStatus();
                        String msg = value.getMsg();
                        LogUtils.i(status + "=========" + msg);
                        if (status == 0 && msg.equals("success")) {
                            ToastUtils.showShort(getContext().getString(R.string.update_pwd_success));
                            login(userBean);
                        } else {
                            ToastUtils.showShort(msg);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtils.i(e.getMessage() + "--------Throwable----------" + e.toString());
                        StyledDialog.dismissLoading();
                    }

                    @Override
                    public void onComplete() {
                        StyledDialog.dismissLoading();
                    }
                })
        );
    }

    /**
     * 功能描述：用户登录方法
     * <br>创建时间： 2017-07-21 16:25:56
     *
     * @param userBean 用户实体类

     */
    public void login(UserBean userBean) {
        StyledDialog.buildLoading(getContext().getString(R.string.isLogining)).show();
        addDisposable(ServiceHelper.getUsersAS().login(userBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<UserData>() {
                    @Override
                    public void onNext(UserData value) {
                        int status = value.getStatus();
                        String msg = value.getMsg();
                        LogUtils.i(status + "=========" + msg);
                        if (status == 0 && msg.equals("success")) {
                            LoginUtil.saveUserData(getContext(), value.getData());
                            ARouter.getInstance().build(Constants.MAIN_URL).navigation();
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
