package com.fungo.baselib.theme

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import com.fungo.baselib.R
import com.fungo.baselib.utils.SpUtils
import com.fungo.baselib.utils.ViewUtils
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic


/**
 * @author Pinger
 * @since 18-8-3 上午11:29
 * 系统UI处理工具类，包括主题，图标等等
 */

object UiUtils {

    private const val KEY_THEME = "KEY_THEME"

    /**
     * 获取当前的主题
     */
    fun getCurrentTheme(): AppTheme {
        return AppTheme.valueOf(SpUtils.getString(KEY_THEME, AppTheme.Blue.name))
    }


    /**
     * 设置当前的主题
     */
    fun setCurrentTheme(context: Context, theme: AppTheme) {
        when (theme) {
            AppTheme.Blue -> context.setTheme(R.style.BlueTheme)
            AppTheme.Red -> context.setTheme(R.style.RedTheme)
            AppTheme.Brown -> context.setTheme(R.style.BrownTheme)
            AppTheme.Green -> context.setTheme(R.style.GreenTheme)
            AppTheme.Purple -> context.setTheme(R.style.PurpleTheme)
            AppTheme.Teal -> context.setTheme(R.style.TealTheme)
            AppTheme.Pink -> context.setTheme(R.style.PinkTheme)
            AppTheme.DeepPurple -> context.setTheme(R.style.DeepPurpleTheme)
            AppTheme.Orange -> context.setTheme(R.style.OrangeTheme)
            AppTheme.Indigo -> context.setTheme(R.style.IndigoTheme)
            AppTheme.LightGreen -> context.setTheme(R.style.LightGreenTheme)
            AppTheme.Lime -> context.setTheme(R.style.LimeTheme)
            AppTheme.DeepOrange -> context.setTheme(R.style.DeepOrangeTheme)
            AppTheme.Cyan -> context.setTheme(R.style.CyanTheme)
            AppTheme.Black -> context.setTheme(R.style.BlackTheme)
            AppTheme.BlueGrey -> context.setTheme(R.style.BlueGreyTheme)
        }
        saveCurrentTheme(theme)
    }

    /**
     * 获取某一个属性的主题颜色
     */
    fun getThemeColor(context: Context, attrRes: Int): Int {
        val typedArray = context.obtainStyledAttributes(intArrayOf(attrRes))
        val color = typedArray.getColor(0, 0xffffff)
        typedArray.recycle()
        return color
    }

    /**
     * 获取当前的主题颜色
     */
    fun getCurrentThemeColor(context: Context): Int {
        return getThemeColor(context, R.attr.colorPrimary)
    }

    /**
     * 保存当前的主题到本地
     */
    private fun saveCurrentTheme(currentTheme: AppTheme) {
        SpUtils.putString(KEY_THEME, currentTheme.name)
    }

    /**
     * 设置ImageView的图标
     */
    fun setIconFont(imageView: ImageView, icon: IIcon, size: Int) {
        imageView.setImageDrawable(
                IconicsDrawable(imageView.context)
                        .icon(icon)
                        .color(getThemeColor(imageView.context, R.attr.textColorPrimary))
                        .sizeDp(size))
    }

    /**
     * 设置ImageView默认的Icon大小
     */
    fun setIconFont(imageView: ImageView, icon: IIcon) {
        setIconFont(imageView, icon, 16)
    }


    /**
     * 设置TextView图标
     */
    fun setIconFont(textView: TextView, icon: IIcon, size: Int, padding: Int) {
        textView.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(textView.context)
                .icon(icon)
                .color(getThemeColor(textView.context, R.attr.textColorPrimary))
                .sizeDp(size),
                null, null, null)
        textView.compoundDrawablePadding = ViewUtils.dp2px(textView.context, padding)
    }

    /**
     * 设置TextView默认的Icon大小
     */
    fun setIconFont(textView: TextView, icon: IIcon) {
        setIconFont(textView, icon, 14, 5)
    }

    /**
     * 生成一个icon font
     */
    fun getIconFont(context: Context, icon: IIcon, size: Int): Drawable {
        return IconicsDrawable(context)
                .icon(icon)
                .color(getThemeColor(context, R.attr.textColorPrimary))
                .sizeDp(size)
    }

    /**
     * 生成一个icon font，指定大小
     */
    fun getIconFont(context: Context, icon: IIcon): Drawable {
        return getIconFont(context, icon, 16)
    }


    /**
     * 返回键
     */
    fun getBackIconFont(context: Context): Drawable {
        return getIconFont(context, MaterialDesignIconic.Icon.gmi_arrow_back)
    }

    /**
     * 添加
     */
    fun setAddIconFont(view: ImageView) {
        setIconFont(view, FontAwesome.Icon.faw_github)
    }

}