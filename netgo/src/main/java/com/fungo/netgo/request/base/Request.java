package com.fungo.netgo.request.base;

import android.text.TextUtils;

import com.fungo.netgo.NetGo;
import com.fungo.netgo.cache.CacheMode;
import com.fungo.netgo.callback.CallBack;
import com.fungo.netgo.model.HttpHeaders;
import com.fungo.netgo.model.HttpParams;
import com.fungo.netgo.request.RequestMethod;
import com.fungo.netgo.subscribe.RxSubscriber;
import com.fungo.netgo.utils.HttpUtils;
import com.fungo.netgo.utils.RxUtils;

import java.util.Map;

import io.reactivex.Flowable;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * @author Pinger
 * @since 18-10-23 上午11:03
 * <p>
 * 请求基类，封装请求
 */
public abstract class Request<T, R extends Request> {

    protected String mUrl;
    protected transient OkHttpClient mClient;
    protected int mRetryCount;
    protected CacheMode mCacheMode;
    protected String mCacheKey;
    protected long mCacheTime;                           //默认缓存的超时时间
    protected HttpParams mParams = new HttpParams();     //添加的param
    protected HttpHeaders mHeaders = new HttpHeaders();  //添加的header

    protected transient CallBack<T> mCallBack;

    // retrofit请求执行者，如果没有的话，就不用retrofit
    protected transient ApiService mApiService;

    public Request(String url, ApiService service) {
        this.mUrl = url;
        this.mApiService = service;
        NetGo netGo = NetGo.getInstance();

        mClient = netGo.getOkHttpClient();

        //默认添加 Accept-Language
        String acceptLanguage = HttpHeaders.getAcceptLanguage();
        if (!TextUtils.isEmpty(acceptLanguage))
            headers(HttpHeaders.HEAD_KEY_ACCEPT_LANGUAGE, acceptLanguage);

        //默认添加 User-Agent
        String userAgent = HttpHeaders.getUserAgent();
        if (!TextUtils.isEmpty(userAgent)) headers(HttpHeaders.HEAD_KEY_USER_AGENT, userAgent);

        //添加公共请求参数
        if (netGo.getCommonParams() != null) params(netGo.getCommonParams());
        if (netGo.getCommonHeaders() != null) headers(netGo.getCommonHeaders());

        //添加缓存模式
        mRetryCount = netGo.getRetryCount();
        mCacheMode = netGo.getCacheMode();
        mCacheTime = netGo.getCacheTime();
    }

    public R headers(HttpHeaders headers) {
        this.mHeaders.put(headers);
        return (R) this;
    }

    public R headers(String key, String value) {
        this.mHeaders.put(key, value);
        return (R) this;
    }

    public R removeHeader(String key) {
        this.mHeaders.remove(key);
        return (R) this;
    }

    public R removeAllHeaders() {
        this.mHeaders.clear();
        return (R) this;
    }

    public R params(HttpParams params) {
        this.mParams.put(params);
        return (R) this;
    }

    public R params(Map<String, String> params) {
        this.mParams.put(params);
        return (R) this;
    }


    public R params(String key, String value) {
        this.mParams.put(key, value);
        return (R) this;
    }

    public R params(String key, int value) {
        this.mParams.put(key, value);
        return (R) this;
    }


    public R params(String key, boolean value) {
        this.mParams.put(key, value);
        return (R) this;
    }


    public R params(String key, float value) {
        this.mParams.put(key, value);
        return (R) this;
    }

    public R params(String key, long value) {
        this.mParams.put(key, value);
        return (R) this;
    }

    public R removeParam(String key) {
        this.mParams.remove(key);
        return (R) this;
    }

    public R removeAllParams() {
        this.mParams.clear();
        return (R) this;
    }


    public HttpParams getParams() {
        return mParams;
    }

    public HttpHeaders getHeaders() {
        return mHeaders;
    }

    public String getUrl() {
        return mUrl;
    }

    public CacheMode getCacheMode() {
        return mCacheMode;
    }

    public String getCacheKey() {
        return mCacheKey;
    }

    public long getCacheTime() {
        return mCacheTime;
    }

    public int getRetryCount() {
        return mRetryCount;
    }


    public ApiService getApiService() {
        return mApiService;
    }


    /**
     * 获取请求方式
     */
    public abstract RequestMethod getMethod();

    /**
     * 根据不同的请求方式和参数，生成不同的RequestBody
     */
    public abstract RequestBody generateRequestBody();

    /**
     * 订阅请求
     * TODO 结合缓存统一处理
     */
    public void execute(CallBack<T> callBack) {
        HttpUtils.checkNotNull(mApiService, "retrofit service not be null,please call NetGo.getApi() fist.");

        Flowable<ResponseBody> flowable = null;
        switch (getMethod()) {
            case GET:
                flowable = mApiService.get(mUrl, mHeaders.getHeaderParams(), mParams.getUrlParams());
                break;
            case POST:
                flowable = mApiService.post(mUrl, mHeaders.getHeaderParams(), mParams.getUrlParams());
                break;
        }

        if (flowable != null) {
            flowable.onErrorResumeNext(RxUtils.getErrorFuntion())
                    .compose(RxUtils.<ResponseBody>getScheduler())
                    .subscribe(new RxSubscriber<>(callBack));
        }
    }


}
