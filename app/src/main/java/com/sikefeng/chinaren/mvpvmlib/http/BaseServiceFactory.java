/**
 * Copyright (C) 2014-2017 <a href="http://www.sikefeng.com>">sikefeng</a> All Rights Reserved.
 */
package com.sikefeng.chinaren.mvpvmlib.http;

import android.support.annotation.Nullable;

import com.sikefeng.chinaren.mvpvmlib.http.converter.GsonConverterFactory;
import com.sikefeng.chinaren.mvpvmlib.utils.LogUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;


public abstract class BaseServiceFactory<S> {

    /**
     * 定义MAP
     */
    private Map<Class<S>, S> serviceMap;

    /**
     * 获取服务
     * @param serviceClass Class&lt;S&gt;
     * @return S
     */
    public S getService(Class<S> serviceClass) {
        if (null == serviceMap) {
            serviceMap = new HashMap<>();
        }
        if (null != serviceMap.get(serviceClass)) {
            return serviceMap.get(serviceClass);
        } else {
            S service = createService(serviceClass);
            serviceMap.put(serviceClass, service);
            return service;
        }
    }

    /**
     * 创建服务
     * @param serviceClass Class&lt;S&gt;
     * @return S
     */
    public S createService(Class<S> serviceClass) {
        String baseUrl = getDefaultBaseUrl();
        try {
            Field field1 = serviceClass.getField("BASE_URL");
            baseUrl = (String) field1.get(serviceClass);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            LogUtils.e("Your Service NoSuchFieldException: BASE_URL");
        } catch (IllegalAccessException e) {
            LogUtils.e("IllegalAccessException");
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit.create(serviceClass);
    }

    /**
     * 定义超类 OkHttpClient
     * @return OkHttpClient
     */
    protected abstract OkHttpClient getOkHttpClient();

    /**
     * 获取基本URL
     * @return 基本URL
     */
    @Nullable
    protected abstract String getDefaultBaseUrl();

}
