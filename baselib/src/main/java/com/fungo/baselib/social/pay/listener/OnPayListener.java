package com.fungo.baselib.social.pay.listener;

import com.fungo.baselib.social.pay.entity.PayType;

/**
 * @author Pinger
 * @since 2018/4/30 20:06
 */
public interface OnPayListener {
    void onPaySuccess(PayType payType);

    void onPayCancel(PayType payType);

    void onPayFailure(PayType payType, int errCode);
}