package com.fungo.netgo.subscribe;

import com.fungo.netgo.error.ApiException;
import com.fungo.netgo.error.NetError;

import okhttp3.ResponseBody;

/**
 * @author Pinger
 * @since 18-10-19 上午11:08
 * <p>
 * 订阅者基类，抽离出来，应对其他的变化
 */
public abstract class BaseSubscriber extends ResourceSubscriber<ResponseBody> {


    /**
     * 分发一下异常
     */
    @Override
    final public void onError(Throwable e) {
        if (e instanceof ApiException) {
            onError((ApiException) e);
        } else {
            onError(new ApiException(e, NetError.UNKNOWN));
        }
    }


    protected void onError(ApiException e) {
    }


    @Override
    public void onComplete() {

    }

}
