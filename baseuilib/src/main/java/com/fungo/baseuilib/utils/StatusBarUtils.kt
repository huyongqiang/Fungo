package com.fungo.baseuilib.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.IntDef
import com.fungo.baseuilib.R
import com.fungo.baseuilib.theme.UiUtils

/**
 * @author Pinger
 * @since 2017/4/14 0014 下午 6:00
 * 状态栏工具类
 */

object StatusBarUtils {


    const val OTHER = -1
    const val MIUI = 1
    const val FLYME = 2
    const val ANDROID_M = 3

    @IntDef(OTHER, MIUI, FLYME, ANDROID_M)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class SystemType


    /**
     * 修改状态栏为全透明（沉浸）
     */
    @TargetApi(19)
    fun setStatusBarTranslucent(activity: Activity) {
        // 先清除掉全屏模式
        clearFullScreen(activity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val window = activity.window
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    /**
     * 修改状态栏颜色，支持4.4以上版本
     * 没有设置沉浸式的时候，通过这个方法可以修改状态栏颜色
     * @param colorId 颜色的int ID
     */
    fun setStatusBarBackgroundColor(activity: Activity, colorId: Int) {
        // 只设置4.4以上的系统
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.statusBarColor = colorId
        }
    }

    /**
     * 设置默认的主题背景颜色
     */
    fun setStatusBarBackgroundColor(activity: Activity) {
        // 只设置4.4以上的系统
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.statusBarColor = UiUtils.getThemeColor(activity, R.attr.colorPrimaryDark)
        }
    }


    /**
     * 设置状态栏字体颜色
     */
    fun setStatusBarForegroundColor(activity: Activity, isBlack: Boolean) {
        // 优先处理6.0以上的系统
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setStatusBarLightMode(activity, ANDROID_M, isBlack)
            return
        }
        // 小米系统
        if (setMIUIStatusBarLightMode(activity, isBlack)) return
        // 魅族系统
        if (setFlymeStatusBarLightMode(activity.window, isBlack)) return
    }


    /**
     * 设置全屏模式
     */
    fun setFullScreen(activity: Activity) {
        activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    /**
     * 清除全屏模式
     */
    fun clearFullScreen(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }


    /**
     * 获取系统状态栏的高度
     * 获取status_bar_height资源的ID
     * 根据资源ID获取响应的尺寸值
     */
    fun getStatusBarHeight(context: Context): Int {
        var statusBarHeight = 0
        val resources = context.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }

    /**
     * 设置自定义状态栏的高度
     * @param viewStateBar
     */
    fun setStatusBarHeight(viewStateBar: View?) {
        if (viewStateBar != null) {
            val layoutParams = viewStateBar.layoutParams
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                layoutParams.height = getStatusBarHeight(viewStateBar.context)
            } else {
                layoutParams.height = 0
            }
            viewStateBar.layoutParams = layoutParams
        }
    }


    /**
     * 设置自定义状态栏高度
     * @param viewStateBar　状态栏
     * @param height　高度
     */
    fun setStatusBarHeight(viewStateBar: View?, height: Int) {
        if (viewStateBar != null) {
            val layoutParams = viewStateBar.layoutParams
            layoutParams.height = height
            viewStateBar.layoutParams = layoutParams
        }
    }


    /**
     * Android 6.0以上修改状态栏字体颜色
     */
    private fun setAndroidMStatusBarLightMode(window: Window, isBlack: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            if (decorView != null) {
                var vis = decorView.systemUiVisibility
                vis = if (isBlack) {
                    vis or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
                decorView.systemUiVisibility = vis
            }
        }
    }


    /**
     * 已知系统类型时，设置状态栏黑色字体图标。
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
     *
     * @param activity
     * @param type     1:MIUUI 2:Flyme 3:android6.0
     */
    @SuppressLint("SwitchIntDef")
    private fun setStatusBarLightMode(activity: Activity, @SystemType type: Int, isBlack: Boolean) {
        when (type) {
            MIUI -> setMIUIStatusBarLightMode(activity, isBlack)
            FLYME -> setFlymeStatusBarLightMode(activity.window, isBlack)
            ANDROID_M -> setAndroidMStatusBarLightMode(activity.window, isBlack)
        }
    }


    /**
     * 设置状态栏图标为深色和魅族特定的文字风格
     * 可以用来判断是否为Flyme用户
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    private fun setFlymeStatusBarLightMode(window: Window?, dark: Boolean): Boolean {
        var result = false
        if (window != null) {
            try {
                val lp = window.attributes
                val darkFlag = WindowManager.LayoutParams::class.java
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java
                        .getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(lp)
                if (dark) {
                    value = value or bit
                } else {
                    value = value and bit.inv()
                }
                meizuFlags.setInt(lp, value)
                window.attributes = lp
                result = true
            } catch (e: Exception) {

            }

        }
        return result
    }

    /**
     * 设置状态栏字体图标为深色，需要MIUIV6以上
     *
     * @param activity 用于获取window和decorView
     * @param dark     是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    private fun setMIUIStatusBarLightMode(activity: Activity, dark: Boolean): Boolean {
        var result = false
        val window = activity.window
        if (window != null) {
            val clazz = window.javaClass
            try {
                var darkModeFlag = 0
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                darkModeFlag = field.getInt(layoutParams)
                val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag)//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag)//清除黑色字体
                }
                result = true

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setAndroidMStatusBarLightMode(activity.window, dark)
                }
            } catch (e: Exception) {

            }

        }
        return result
    }


}