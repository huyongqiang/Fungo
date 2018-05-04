package com.fungo.imagego

import android.content.Context
import android.text.TextUtils
import android.widget.ImageView
import com.fungo.imagego.listener.ImageListener
import com.fungo.imagego.listener.ImageSaveListener
import com.fungo.imagego.progress.ProgressEngine
import com.fungo.imagego.progress.ProgressListener
import com.fungo.imagego.strategy.GlideImageLoaderStrategy
import com.fungo.imagego.utils.ImageGoConfig
import com.fungo.imagego.utils.ImageGoUtils
import org.fungo.baselib.image.BaseImageStrategy

/**
 * @author Pinger
 * @since 3/28/18 10:50 AM
 */
class ImageManager {

    private var mLoadModel = ImageGoConfig.MODEL_GLIDE

    companion object {  //  伴生对象获取单利
        private var mInstance: ImageManager? = null
        private var mImageStrategy: BaseImageStrategy? = null

        val instance: ImageManager
            @Synchronized get() {
                if (mInstance == null) {
                    mInstance = ImageManager()
                }
                checkStrategy()
                return mInstance!!
            }

        private fun checkStrategy() {
            if (mImageStrategy == null) {
                // 根据ImageModel生产策略模式
                mImageStrategy = GlideImageLoaderStrategy()
            }
        }
    }

    /** 普通图片 */
    fun loadImage(url: String?, imageView: ImageView?) {
        loadImage(url, imageView, null)
    }

    /** 加载图片，默认根据图片后缀加载GIF图片 */
    fun loadImage(url: String?, imageView: ImageView?, listener: ImageListener?) {
        if (!TextUtils.isEmpty(url)) {
            if (url!!.endsWith(".gif") || url.endsWith(".GIF")) {
                mImageStrategy!!.loadGifImage(url, imageView, listener)
            } else {
                mImageStrategy!!.loadImage(url, imageView, listener)
            }
        }
    }

    /** 加载图片，带进度条 */
    fun loadImageWithProgress(url: String?, imageView: ImageView?, listener: ProgressListener) {
        ProgressEngine.addProgressListener(listener)
        loadImage(url, imageView, null)
    }


    /** GIF图片 */
    fun loadGifImage(url: String?, imageView: ImageView?) {
        mImageStrategy!!.loadGifImage(url, imageView)
    }

    /** 圆角图片 */
    fun loadRoundImage(url: String?, imageView: ImageView?, roundRadius: Int) {
        mImageStrategy!!.loadRoundImage(url, imageView, ImageGoUtils.dp2px(imageView?.context, roundRadius).toFloat())
    }

    /** 灰色图片 */
    fun loadGrayImage(url: String?, imageView: ImageView?) {
        mImageStrategy!!.loadGrayImage(url, imageView)
    }

    /** 模糊图片 */
    fun loadBlurImage(url: String?, imageView: ImageView?, blurRadius: Float) {
        mImageStrategy!!.loadBlurImage(url, imageView, blurRadius)
    }

    /** 圆形图片 */
    fun loadCircleImage(url: String?, imageView: ImageView?) {
        loadCircleImage(url, imageView, 0, 0)
    }

    /** 圆形图片，带边框 */
    fun loadCircleImage(url: String?, imageView: ImageView?, borderWidth: Int, borderColor: Int) {
        mImageStrategy!!.loadCircleImage(url, imageView, ImageGoUtils.dp2px(imageView?.context, borderWidth).toFloat(), borderColor)
    }

    /** 保存图片到本地 */
    fun saveImage(context: Context?, url: String?, listener: ImageSaveListener?) {
        mImageStrategy!!.saveImage(context, url, listener)
    }


    /** 设置图片加载模式 */
    fun setLoadModel(model: Int) {
        this.mLoadModel = model
    }
}