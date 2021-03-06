package com.fungo.baseuilib.activity

import android.view.View
import androidx.core.view.GravityCompat
import com.fungo.baseuilib.R
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.base_activity_drawer.*

/**
 * @author Pinger
 * @since 2018/11/3 19:24
 *
 * 带有左侧抽屉的Activity
 */
abstract class BaseDrawerActivity(override val layoutResID: Int = R.layout.base_activity_drawer) : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    final override fun initView() {
        initDrawerLayout()
        initContentLayout()
    }

    private fun initContentLayout() {
        if (baseDrawerContent.childCount > 0) {
            baseDrawerContent.removeAllViews()
        }
        // 填充内容容器
        layoutInflater.inflate(getDrawerContentResID(), baseDrawerContent)
    }


    /**
     * 初始化DrawerLayout
     */
    private fun initDrawerLayout() {
        // 抽屉顶部
        initDrawerHeaderView(baseNavView.inflateHeaderView(getDrawerHeaderResId()))
        // 抽屉菜单
        baseNavView.inflateMenu(getDrawerMenuId())
        // 抽屉菜单选择事件
        baseNavView.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressedSupport() {
        if (isDrawerOpen()) {
            closeDrawer()
        } else {
            super.onBackPressedSupport()
        }
    }


    /**
     * 抽屉是否打开
     */
    fun isDrawerOpen(): Boolean {
        return baseDrawerLayout.isDrawerOpen(GravityCompat.START)
    }

    /**
     * 打开抽屉
     */
    fun openDrawer() {
        baseDrawerLayout?.openDrawer(GravityCompat.START)
    }

    /**
     * 关闭抽屉
     */
    fun closeDrawer() {
        baseDrawerLayout?.closeDrawer(GravityCompat.START)
    }

    /**
     * 抽屉标题的布局
     */
    abstract fun getDrawerHeaderResId(): Int

    /**
     * 初始化抽屉标题的View
     */
    protected open fun initDrawerHeaderView(headerView: View) {}

    /**
     * 获取抽屉的menu
     */
    abstract fun getDrawerMenuId(): Int

    /**
     * 获取内容容器布局id
     */
    abstract fun getDrawerContentResID(): Int

    /**
     * 有抽屉时，禁用滑动返回
     */
    override fun isSwipeBackEnable(): Boolean = false
}