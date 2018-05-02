package com.fungo.baselib.image.progress

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.lang.ref.WeakReference
import java.util.*


/**
 * @author Pinger
 * @since 2018/4/9 21:49
 */
object ProgressEngine {

    private val mListeners = Collections.synchronizedList<WeakReference<ProgressListener>>(ArrayList<WeakReference<ProgressListener>>())

    fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .addNetworkInterceptor(ProgressInterceptor())
                .build()
    }

    private class ProgressInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)
            return response.newBuilder()
                    .body(ProgressResponseBody(response.body(), mProgressListener))
                    .build()
        }
    }

    private val mProgressListener = object : ProgressListener {
        override fun onProgress(bytesRead: Long, contentLength: Long, isDone: Boolean) {
            if (mListeners == null || mListeners.size == 0) return

            for (i in 0 until mListeners.size) {
                val listener = mListeners[i]
                val progressListener = listener.get()
                progressListener?.onProgress(bytesRead, contentLength, isDone)
            }
        }
    }

    fun addProgressListener(progressListener: ProgressListener?) {
        if (progressListener == null) return

        if (findProgressListener(progressListener) == null) {
            mListeners.add(WeakReference(progressListener))
        }
    }

    fun removeProgressListener(progressListener: ProgressListener?) {
        if (progressListener == null) return

        val listener = findProgressListener(progressListener)
        if (listener != null) {
            mListeners.remove(listener)
        }
    }

    private fun findProgressListener(listener: ProgressListener?): WeakReference<ProgressListener>? {
        if (listener == null) return null
        if (mListeners == null || mListeners.size == 0) return null
        for (i in 0 until mListeners.size) {
            val progressListener = mListeners[i]
            if (progressListener.get() === listener) {
                return progressListener
            }
        }
        return null
    }

}