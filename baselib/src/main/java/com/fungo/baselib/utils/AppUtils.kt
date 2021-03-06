package com.fungo.baselib.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.File
import java.util.*

/**
 * @author Pinger
 * @since 3/26/18 10:03 PM
 * App级别的工具类，提供系统的Context和常用的工具类
 */
object AppUtils {

    private lateinit var mHandler: Handler
    private lateinit var mApplication: Application
    private val mActivityList = LinkedList<Activity>()


    /**
     * 初始化Application，获取引用，和注册全局Handle
     */
    fun init(application: Application) {
        mApplication = application
        mHandler = Handler()
        //registerActivityCallback()
    }

    /**
     * 获取全局的Application
     * @return Application
     */
    fun getApp(): Application {
        return mApplication
    }

    /**
     * 获取全局的Context
     * @return Context
     */
    fun getContext(): Context {
        return mApplication.applicationContext
    }

    /**
     * 设置栈顶Activity
     */
    fun setTopActivity(activity: Activity) {
        if (activity.javaClass == PermissionUtils.PermissionActivity::class.java) return
        if (mActivityList.contains(activity)) {
            if (mActivityList.last != activity) {
                mActivityList.remove(activity)
                mActivityList.addLast(activity)
            }
        } else {
            mActivityList.addLast(activity)
        }
    }

    /**
     * 获取所有启动过的Activity
     */
    fun getActivityList(): LinkedList<Activity> {
        return mActivityList
    }


    /**
     * Activity生命周期回调
     */
    private fun registerActivityCallback() {
        mApplication.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, bundle: Bundle) {
                setTopActivity(activity)
            }

            override fun onActivityStarted(activity: Activity) {
                setTopActivity(activity)
            }

            override fun onActivityResumed(activity: Activity) {
                setTopActivity(activity)
            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {
                mActivityList.remove(activity)
            }
        })
    }

    /**
     * Post一个Runnable
     */
    fun post(runnable: Runnable) {
        mHandler.removeCallbacks(null)
        mHandler.post(runnable)
    }

    /**
     * 延时执行一个Runnable
     */
    fun postDelayed(runnable: Runnable, delayMillis: Long) {
        mHandler.removeCallbacks(null)
        mHandler.postDelayed(runnable, delayMillis)
    }

    /**
     * 移除之前发起的Post
     */
    fun removeCallbacks() {
        mHandler.removeCallbacks(null)
    }


    /**
     * 启动手机的主页，让当前的app进入后台
     */
    fun moveTaskToBack() {
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN)
            homeIntent.addCategory(Intent.CATEGORY_HOME)
            ActivityUtils.startActivity(homeIntent)
        } catch (e: Exception) {
            LogUtils.e(e)
        }
    }

    /**
     * 启动手机主页，这个方法更加的保险
     */
    fun moveTaskToBack(activity: Activity) {
        try {
            activity.moveTaskToBack(false)

            val homeIntent = Intent(Intent.ACTION_MAIN)
            homeIntent.addCategory(Intent.CATEGORY_HOME)
            ActivityUtils.startActivity(homeIntent)
        } catch (e: Exception) {
            LogUtils.e(e)
        }
    }

    /**
     * 安装app
     * 需要权限：<uses-permission android:name="android.permission.INSTALL_PACKAGES" />
     */
    fun installApp(filePath: String, authority: String) {
        installApp(getFileByPath(filePath), authority)
    }

    /**
     * 安装app
     * 需要权限：<uses-permission android:name="android.permission.INSTALL_PACKAGES" />
     */
    fun installApp(file: File?, authority: String) {
        if (!isFileExists(file)) return
        getContext().startActivity(IntentUtils.getInstallAppIntent(file, authority, true))
    }


    /**
     * 静默安装app
     * @param file apk文件
     * 需要权限：<uses-permission android:name="android.permission.INSTALL_PACKAGES" />
     */
    fun installAppSilent(file: File): Boolean {
        return installAppSilent(file, null)
    }


    /**
     * 静默安装app
     * @param filePath apk文件的路径
     * 需要权限：<uses-permission android:name="android.permission.INSTALL_PACKAGES" />
     */
    fun installAppSilent(filePath: String): Boolean {
        return installAppSilent(getFileByPath(filePath), null)
    }

    /**
     * 静默安装app
     * 需要权限：<uses-permission android:name="android.permission.INSTALL_PACKAGES" />
     * @param file apk文件
     * @param params 安装携带的参数
     * @return 是否安装成功
     */
    fun installAppSilent(file: File?, params: String?): Boolean {
        if (!isFileExists(file)) return false
        val isRoot = isDeviceRooted()
        val filePath = file!!.absolutePath
        val command = ("LD_LIBRARY_PATH=/vendor/lib*:/system/lib* pm install " +
                (if (params == null) "" else "$params ")
                + filePath)
        val commandResult = ShellUtils.execCmd(command, isRoot)
        return if (commandResult.successMsg != null && commandResult.successMsg?.toLowerCase()!!.contains("success")) {
            true
        } else {
            Log.e("AppUtils", "installAppSilent successMsg: " + commandResult.successMsg +
                    ", errorMsg: " + commandResult.errorMsg)
            false
        }
    }

    /**
     * 卸载指定包名的app
     * 需要权限：<uses-permission android:name="android.permission.DELETE_PACKAGES" />
     */
    fun uninstallApp(packageName: String) {
        if (isSpace(packageName)) return
        getContext().startActivity(IntentUtils.getUninstallAppIntent(packageName, true))
    }


    /**
     * 静默卸载app，不保存数据
     * 需要权限：<uses-permission android:name="android.permission.DELETE_PACKAGES" />
     */
    fun uninstallAppSilent(packageName: String): Boolean {
        return uninstallAppSilent(packageName, false)
    }

    /**
     * 静默卸载指定包名app
     * 需要权限：<uses-permission android:name="android.permission.DELETE_PACKAGES" />
     * @param isKeepData 是否保存卸载app的数据
     */
    fun uninstallAppSilent(packageName: String, isKeepData: Boolean): Boolean {
        if (isSpace(packageName)) return false
        val isRoot = isDeviceRooted()
        val command = ("LD_LIBRARY_PATH=/vendor/lib*:/system/lib* pm uninstall "
                + (if (isKeepData) "-k " else "")
                + packageName)
        val commandResult = ShellUtils.execCmd(command, isRoot, true)
        return if (commandResult.successMsg != null && commandResult.successMsg?.toLowerCase()!!.contains("success")) {
            true
        } else {
            Log.e("AppUtils", "uninstallAppSilent successMsg: " + commandResult.successMsg +
                    ", errorMsg: " + commandResult.errorMsg)
            false
        }
    }


    /**
     * 指定包名的应用是否安装
     */
    fun isAppInstalled(packageName: String): Boolean {
        return !isSpace(packageName) && IntentUtils.getLaunchAppIntent(packageName) != null
    }


    /**
     * 获取当前应用的信息
     */
    fun getAppInfo(): AppInfo? = getAppInfo(getContext().packageName)


    /**
     * 获取指定包名的应用信息
     * @param packageName 包名
     */
    fun getAppInfo(packageName: String): AppInfo? {
        return try {
            val pm = getContext().packageManager
            val pi = pm.getPackageInfo(packageName, 0)
            getAppInfo(pm, pi)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }

    }

    /**
     * 获取所有的应用信息
     */
    fun getAppsInfo(): List<AppInfo> {
        val list = ArrayList<AppInfo>()
        val pm = getContext().packageManager
        val installedPackages = pm.getInstalledPackages(0)
        for (pi in installedPackages) {
            val ai = getAppInfo(pm, pi) ?: continue
            list.add(ai)
        }
        return list
    }

    private fun getAppInfo(pm: PackageManager?, pi: PackageInfo?): AppInfo? {
        if (pm == null || pi == null) return null
        val ai = pi.applicationInfo
        val packageName = pi.packageName
        val name = ai.loadLabel(pm).toString()
        val icon = ai.loadIcon(pm)
        val packagePath = ai.sourceDir
        val versionName = pi.versionName
        val versionCode = pi.versionCode
        val isSystem = ApplicationInfo.FLAG_SYSTEM and ai.flags != 0
        return AppInfo(packageName, name, icon, packagePath, versionName, versionCode, isSystem)
    }

    private fun isFileExists(file: File?): Boolean {
        return file != null && file.exists()
    }

    private fun getFileByPath(filePath: String): File? {
        return if (isSpace(filePath)) null else File(filePath)
    }

    private fun isSpace(s: String?): Boolean {
        if (s == null) return true
        var i = 0
        val len = s.length
        while (i < len) {
            if (!Character.isWhitespace(s[i])) {
                return false
            }
            ++i
        }
        return true
    }

    private fun isDeviceRooted(): Boolean {
        val su = "su"
        val locations = arrayOf("/system/bin/", "/system/xbin/", "/sbin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/xbin/", "/data/local/bin/", "/data/local/")
        for (location in locations) {
            if (File(location + su).exists()) {
                return true
            }
        }
        return false
    }

    /**
     * The application's information.
     */
    class AppInfo(packageName: String, name: String, icon: Drawable, packagePath: String,
                  versionName: String, versionCode: Int, isSystem: Boolean) {

        var packageName: String? = null
        var name: String? = null
        var icon: Drawable? = null
        var packagePath: String? = null
        var versionName: String? = null
        var versionCode: Int = 0
        var isSystem: Boolean = false

        init {
            this.name = name
            this.icon = icon
            this.packageName = packageName
            this.packagePath = packagePath
            this.versionName = versionName
            this.versionCode = versionCode
            this.isSystem = isSystem
        }

        override fun toString(): String {
            return "pkg name: " + packageName +
                    "\napp icon: " + icon +
                    "\napp name: " + name +
                    "\napp path: " + packagePath +
                    "\napp v name: " + versionName +
                    "\napp v code: " + versionCode +
                    "\nis system: " + isSystem
        }
    }

}