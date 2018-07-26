package com.fungo.baselib.base.page

import com.fungo.baselib.R
import com.fungo.baselib.base.basic.BaseActivity


/**
 * @author Pinger
 * @since 18-7-20 下午5:34
 * 页面的中转Activity的基类
 */

abstract class BasePageActivity : BaseActivity() {

    override val layoutResID: Int
        get() = R.layout.activity_page


    override fun initView() {
        // 设置Fragment的默认背景颜色
        setDefaultFragmentBackground(R.color.grey_f7)
        val fragment = getRootFragment()
        // 转移Activity的extras给Fragment
        fragment.arguments = intent.extras
        // 填充Fragment
        loadRootFragment(R.id.pageContainer, fragment)
    }

    /**
     * 获取跟节点的Fragment
     */
    abstract fun getRootFragment(): BasePageFragment

}