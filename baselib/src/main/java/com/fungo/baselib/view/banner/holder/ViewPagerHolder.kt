package com.fungo.baselib.view.banner.holder

import android.content.Context
import android.view.View

/**
 * Created by zhouwei on 17/5/26.
 */

interface ViewPagerHolder<in T> {
    /**
     * 创建View
     * @param context
     * @return
     */
    fun createView(context: Context): View

    /**
     * 绑定数据
     * @param context
     * @param position
     * @param data
     */
    fun onBindData(context: Context, position: Int, data: T)
}
