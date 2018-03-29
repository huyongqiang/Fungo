package com.leibo.baselib.image.transfmer

import android.content.Context
import android.graphics.*
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest


/**
 * @author Pinger
 * @since 3/29/18 4:06 PM
 */
class RoundTransformation(val context: Context, private val roundRadius: Float) : BitmapTransformation() {

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        return roundCrop(pool, toTransform)
    }

    private fun roundCrop(pool: BitmapPool, source: Bitmap): Bitmap {
        var result: Bitmap? = pool.get(source.width, source.height, Bitmap.Config.ARGB_8888)
        if (result == null) {
            result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(result)
        val paint = Paint()
        paint.shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.isAntiAlias = true
        val rectF = RectF(0f, 0f, source.width.toFloat(), source.height.toFloat())
        canvas.drawRoundRect(rectF, roundRadius, roundRadius, paint)
        return result!!
    }


    override fun updateDiskCacheKey(messageDigest: MessageDigest) {

    }

}