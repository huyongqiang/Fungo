package com.fungo.baseuilib.view.progress

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import com.fungo.baseuilib.R
import com.fungo.baseuilib.utils.ViewUtils


/**
 * @author Pinger
 * @since 2018/4/9 21:21
 */
class CircleProgressView : ProgressBar {

    private var mReachBarSize = ViewUtils.dp2px(context, 2).toInt() // 未完成进度条大小
    private var mNormalBarSize = ViewUtils.dp2px(context, 2).toInt() // 未完成进度条大小
    private var mReachBarColor = Color.parseColor("#108ee9") // 已完成进度颜色
    private var mNormalBarColor = Color.parseColor("#FFD3D6DA") // 未完成进度颜色
    private var mTextSize = ViewUtils.sp2px(context, 14).toInt() // 进度值字体大小
    private var mTextColor = Color.parseColor("#108ee9") // 进度的值字体颜色
    private var mTextSkewX: Float = 0.toFloat() // 进度值字体倾斜角度
    private var mTextSuffix: String? = "%" // 进度值前缀
    private var mTextPrefix: String? = "" // 进度值后缀
    private var mTextVisible = true // 是否显示进度值
    private var mReachCapRound: Boolean = false // 画笔是否使用圆角边界，normalStyle下生效
    private var mRadius = ViewUtils.dp2px(context, 20).toInt() // 半径
    private var mStartArc: Int = 0 // 起始角度
    private var mInnerBackgroundColor: Int = 0 // 内部背景填充颜色
    private var mProgressStyle = ProgressStyle.NORMAL.value // 进度风格
    private var mInnerPadding = ViewUtils.dp2px(context, 1).toInt() // 内部圆与外部圆间距
    private var mOuterColor: Int = 0 // 外部圆环颜色
    private var needDrawInnerBackground: Boolean = false // 是否需要绘制内部背景
    private lateinit var rectF: RectF // 外部圆环绘制区域。
    private lateinit var rectInner: RectF  // 内部圆环绘制区域
    private var mOuterSize = ViewUtils.dp2px(context, 1).toInt() // 外层圆环宽度
    private lateinit var mTextPaint: Paint  // 绘制进度值字体画笔
    private lateinit var mNormalPaint: Paint // 绘制未完成进度画笔
    private lateinit var mReachPaint: Paint // 绘制已完成进度画笔
    private var mInnerBackgroundPaint: Paint? = null // 内部背景画笔
    private var mOutPaint: Paint? = null // 外部圆环画笔

    private var mRealWidth: Int = 0
    private var mRealHeight: Int = 0

    enum class ProgressStyle(val value: Int) {
        NORMAL(0),
        FILL_IN(1),
        FILL_IN_ARC(2)
    }

    companion object {
        private const val STATE = "state"
        private const val PROGRESS_STYLE = "progressStyle"
        private const val TEXT_COLOR = "textColor"
        private const val TEXT_SIZE = "textSize"
        private const val TEXT_SKEW_X = "textSkewX"
        private const val TEXT_VISIBLE = "textVisible"
        private const val TEXT_SUFFIX = "textSuffix"
        private const val TEXT_PREFIX = "textPrefix"
        private const val REACH_BAR_COLOR = "reachBarColor"
        private const val REACH_BAR_SIZE = "reachBarSize"
        private const val NORMAL_BAR_COLOR = "normalBarColor"
        private const val NORMAL_BAR_SIZE = "normalBarSize"
        private const val IS_REACH_CAP_ROUND = "isReachCapRound"
        private const val RADIUS = "radius"
        private const val START_ARC = "startArc"
        private const val INNER_BG_COLOR = "innerBgColor"
        private const val INNER_PADDING = "innerPadding"
        private const val OUTER_COLOR = "outerColor"
        private const val OUTER_SIZE = "outerSize"
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        obtainAttributes(attrs)
        initPaint()
    }

    private fun initPaint() {
        mTextPaint = Paint()
        mTextPaint.color = mTextColor
        mTextPaint.style = Paint.Style.FILL
        mTextPaint.textSize = mTextSize.toFloat()
        mTextPaint.textSkewX = mTextSkewX
        mTextPaint.isAntiAlias = true

        mNormalPaint = Paint()
        mNormalPaint.color = mNormalBarColor
        mNormalPaint.style = if (mProgressStyle == ProgressStyle.FILL_IN_ARC.value) Paint.Style.FILL else Paint.Style.STROKE
        mNormalPaint.isAntiAlias = true
        mNormalPaint.strokeWidth = mNormalBarSize.toFloat()

        mReachPaint = Paint()
        mReachPaint.color = mReachBarColor
        mReachPaint.style = if (mProgressStyle == ProgressStyle.FILL_IN_ARC.value) Paint.Style.FILL else Paint.Style.STROKE
        mReachPaint.isAntiAlias = true
        mReachPaint.strokeCap = if (mReachCapRound) Paint.Cap.ROUND else Paint.Cap.BUTT
        mReachPaint.strokeWidth = mReachBarSize.toFloat()

        if (needDrawInnerBackground) {
            mInnerBackgroundPaint = Paint()
            mInnerBackgroundPaint!!.style = Paint.Style.FILL
            mInnerBackgroundPaint!!.isAntiAlias = true
            mInnerBackgroundPaint!!.color = mInnerBackgroundColor
        }
        if (mProgressStyle == ProgressStyle.FILL_IN_ARC.value) {
            mOutPaint = Paint()
            mOutPaint!!.style = Paint.Style.STROKE
            mOutPaint!!.color = mOuterColor
            mOutPaint!!.strokeWidth = mOuterSize.toFloat()
            mOutPaint!!.isAntiAlias = true
        }
    }

    private fun obtainAttributes(attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressView)
        mProgressStyle = ta.getInt(R.styleable.CircleProgressView_cpv_progressStyle, ProgressStyle.NORMAL.value)
        // 获取三种风格通用的属性
        mNormalBarSize = ta.getDimension(R.styleable.CircleProgressView_cpv_progressNormalSize, mNormalBarSize.toFloat()).toInt()
        mNormalBarColor = ta.getColor(R.styleable.CircleProgressView_cpv_progressNormalColor, mNormalBarColor)

        mReachBarSize = ta.getDimension(R.styleable.CircleProgressView_cpv_progressReachSize, mReachBarSize.toFloat()).toInt()
        mReachBarColor = ta.getColor(R.styleable.CircleProgressView_cpv_progressReachColor, mReachBarColor)

        mTextSize = ta.getDimension(R.styleable.CircleProgressView_cpv_progressTextSize, mTextSize.toFloat()).toInt()
        mTextColor = ta.getColor(R.styleable.CircleProgressView_cpv_progressTextColor, mTextColor)
        mTextSkewX = ta.getDimension(R.styleable.CircleProgressView_cpv_progressTextSkewX, 0f)
        if (ta.hasValue(R.styleable.CircleProgressView_cpv_progressTextSuffix)) {
            mTextSuffix = ta.getString(R.styleable.CircleProgressView_cpv_progressTextSuffix)
        }
        if (ta.hasValue(R.styleable.CircleProgressView_cpv_progressTextPrefix)) {
            mTextPrefix = ta.getString(R.styleable.CircleProgressView_cpv_progressTextPrefix)
        }
        mTextVisible = ta.getBoolean(R.styleable.CircleProgressView_cpv_progressTextVisible, mTextVisible)

        mRadius = ta.getDimension(R.styleable.CircleProgressView_cpv_radius, mRadius.toFloat()).toInt()
        rectF = RectF((-mRadius).toFloat(), (-mRadius).toFloat(), mRadius.toFloat(), mRadius.toFloat())

        when (mProgressStyle) {
            ProgressStyle.FILL_IN.value -> {
                mReachBarSize = 0
                mNormalBarSize = 0
                mOuterSize = 0
            }
            ProgressStyle.FILL_IN_ARC.value -> {
                mStartArc = ta.getInt(R.styleable.CircleProgressView_cpv_progressStartArc, 0) + 270
                mInnerPadding = ta.getDimension(R.styleable.CircleProgressView_cpv_innerPadding, mInnerPadding.toFloat()).toInt()
                mOuterColor = ta.getColor(R.styleable.CircleProgressView_cpv_outerColor, mReachBarColor)
                mOuterSize = ta.getDimension(R.styleable.CircleProgressView_cpv_outerSize, mOuterSize.toFloat()).toInt()
                mReachBarSize = 0// 将画笔大小重置为0
                mNormalBarSize = 0
                if (!ta.hasValue(R.styleable.CircleProgressView_cpv_progressNormalColor)) {
                    mNormalBarColor = Color.TRANSPARENT
                }
                val mInnerRadius = mRadius - mOuterSize / 2 - mInnerPadding
                rectInner = RectF((-mInnerRadius).toFloat(), (-mInnerRadius).toFloat(), mInnerRadius.toFloat(), mInnerRadius.toFloat())
            }
            ProgressStyle.NORMAL.value -> {
                mReachCapRound = ta.getBoolean(R.styleable.CircleProgressView_cpv_reachCapRound, true)
                mStartArc = ta.getInt(R.styleable.CircleProgressView_cpv_progressStartArc, 0) + 270
                if (ta.hasValue(R.styleable.CircleProgressView_cpv_innerBackgroundColor)) {
                    mInnerBackgroundColor = ta.getColor(R.styleable.CircleProgressView_cpv_innerBackgroundColor, Color.argb(0, 0, 0, 0))
                    needDrawInnerBackground = true
                }
            }
        }
        ta.recycle()
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxBarPaintWidth = Math.max(mReachBarSize, mNormalBarSize)
        val maxPaintWidth = Math.max(maxBarPaintWidth, mOuterSize)
        var height = 0
        var width = 0
        when (mProgressStyle) {
            ProgressStyle.FILL_IN.value -> {
                height = (paddingTop + paddingBottom  // 边距
                        + Math.abs(mRadius * 2))  // 直径
                width = (paddingLeft + paddingRight  // 边距

                        + Math.abs(mRadius * 2))  // 直径
            }
            ProgressStyle.FILL_IN_ARC.value -> {
                height = (paddingTop + paddingBottom  // 边距

                        + Math.abs(mRadius * 2)  // 直径

                        + maxPaintWidth)// 边框
                width = (paddingLeft + paddingRight  // 边距

                        + Math.abs(mRadius * 2)  // 直径

                        + maxPaintWidth)// 边框
            }
            ProgressStyle.NORMAL.value -> {
                height = (paddingTop + paddingBottom  // 边距

                        + Math.abs(mRadius * 2)  // 直径

                        + maxBarPaintWidth)// 边框
                width = (paddingLeft + paddingRight  // 边距

                        + Math.abs(mRadius * 2)  // 直径

                        + maxBarPaintWidth)// 边框
            }
        }

        mRealWidth = View.resolveSize(width, widthMeasureSpec)
        mRealHeight = View.resolveSize(height, heightMeasureSpec)

        setMeasuredDimension(mRealWidth, mRealHeight)
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        when (mProgressStyle) {
            ProgressStyle.NORMAL.value -> drawNormalCircle(canvas)
            ProgressStyle.FILL_IN.value -> drawFillInCircle(canvas)
            ProgressStyle.FILL_IN_ARC.value -> drawFillInArcCircle(canvas)
        }
    }

    /**
     * 绘制PROGRESS_STYLE_FILL_IN_ARC圆形
     */
    private fun drawFillInArcCircle(canvas: Canvas) {
        canvas.save()
        canvas.translate((mRealWidth / 2).toFloat(), (mRealHeight / 2).toFloat())
        // 绘制外层圆环
        canvas.drawArc(rectF, 0f, 360f, false, mOutPaint)
        // 绘制内层进度实心圆弧
        // 内层圆弧半径
        val reachArc = progress * 1.0f / max * 360
        canvas.drawArc(rectInner, mStartArc.toFloat(), reachArc, true, mReachPaint)

        // 绘制未到达进度
        if (reachArc != 360f) {
            canvas.drawArc(rectInner, reachArc + mStartArc, 360 - reachArc, true, mNormalPaint)
        }

        canvas.restore()
    }

    /**
     * 绘制PROGRESS_STYLE_FILL_IN圆形
     */
    private fun drawFillInCircle(canvas: Canvas) {
        canvas.save()
        canvas.translate((mRealWidth / 2).toFloat(), (mRealHeight / 2).toFloat())
        val progressY = progress * 1.0f / max * (mRadius * 2)
        val angle = (Math.acos(((mRadius - progressY) / mRadius).toDouble()) * 180 / Math.PI).toFloat()
        val startAngle = 90 + angle
        val sweepAngle = 360 - angle * 2
        // 绘制未到达区域
        rectF = RectF((-mRadius).toFloat(), (-mRadius).toFloat(), mRadius.toFloat(), mRadius.toFloat())
        mNormalPaint.style = Paint.Style.FILL
        canvas.drawArc(rectF, startAngle, sweepAngle, false, mNormalPaint)
        // 翻转180度绘制已到达区域
        canvas.rotate(180f)
        mReachPaint.style = Paint.Style.FILL
        canvas.drawArc(rectF, 270 - angle, angle * 2, false, mReachPaint)
        // 文字显示在最上层最后绘制
        canvas.rotate(180f)
        // 绘制文字
        if (mTextVisible) {
            val text = mTextPrefix + progress + mTextSuffix
            val textWidth = mTextPaint.measureText(text)
            val textHeight = mTextPaint.descent() + mTextPaint.ascent()
            canvas.drawText(text, -textWidth / 2, -textHeight / 2, mTextPaint)
        }
    }

    /**
     * 绘制PROGRESS_STYLE_NORMAL圆形
     */
    private fun drawNormalCircle(canvas: Canvas) {
        canvas.save()
        canvas.translate((mRealWidth / 2).toFloat(), (mRealHeight / 2).toFloat())
        // 绘制内部圆形背景色
        if (needDrawInnerBackground) {
            canvas.drawCircle(0f, 0f, (mRadius - Math.min(mReachBarSize, mNormalBarSize) / 2).toFloat(),
                    mInnerBackgroundPaint!!)
        }
        // 绘制文字
        if (mTextVisible) {
            val text = mTextPrefix + progress + mTextSuffix
            val textWidth = mTextPaint.measureText(text)
            val textHeight = mTextPaint.descent() + mTextPaint.ascent()
            canvas.drawText(text, -textWidth / 2, -textHeight / 2, mTextPaint)
        }
        // 计算进度值
        val reachArc = progress * 1.0f / max * 360
        // 绘制未到达进度
        if (reachArc != 360f) {
            canvas.drawArc(rectF, reachArc + mStartArc, 360 - reachArc, false, mNormalPaint)
        }
        // 绘制已到达进度
        canvas.drawArc(rectF, mStartArc.toFloat(), reachArc, false, mReachPaint)
        canvas.restore()
    }

    /**
     * 动画进度(0-当前进度)
     *
     * @param duration 动画时长
     */
    fun runProgressAnim(duration: Long) {
        setProgressInTime(0, duration)
    }

    /**
     * @param progress 进度值
     * @param duration 动画播放时间
     */
    fun setProgressInTime(progress: Int, duration: Long) {
        setProgressInTime(progress, getProgress(), duration)
    }

    /**
     * @param startProgress 起始进度
     * @param progress      进度值
     * @param duration      动画播放时间
     */
    fun setProgressInTime(startProgress: Int, progress: Int, duration: Long) {
        val valueAnimator = ValueAnimator.ofInt(startProgress, progress)
        valueAnimator.addUpdateListener { animator ->
            //获得当前动画的进度值，整型，1-100之间
            val currentValue = animator.animatedValue as Int
            setProgress(currentValue)
        }
        val interpolator = AccelerateDecelerateInterpolator()
        valueAnimator.interpolator = interpolator
        valueAnimator.duration = duration
        valueAnimator.start()
    }

    fun getReachBarSize(): Int {
        return mReachBarSize
    }

    fun setReachBarSize(reachBarSize: Int) {
        mReachBarSize = ViewUtils.dp2px(context, reachBarSize)
        invalidate()
    }

    fun getNormalBarSize(): Int {
        return mNormalBarSize
    }

    fun setNormalBarSize(normalBarSize: Int) {
        mNormalBarSize = ViewUtils.dp2px(context, normalBarSize)
        invalidate()
    }

    fun getReachBarColor(): Int {
        return mReachBarColor
    }

    fun setReachBarColor(reachBarColor: Int) {
        mReachBarColor = reachBarColor
        invalidate()
    }

    fun getNormalBarColor(): Int {
        return mNormalBarColor
    }

    fun setNormalBarColor(normalBarColor: Int) {
        mNormalBarColor = normalBarColor
        invalidate()
    }

    fun getTextSize(): Int {
        return mTextSize
    }

    fun setTextSize(textSize: Int) {
        mTextSize = ViewUtils.sp2px(context, textSize)
        invalidate()
    }

    fun getTextColor(): Int {
        return mTextColor
    }

    fun setTextColor(textColor: Int) {
        mTextColor = textColor
        invalidate()
    }

    fun getTextSkewX(): Float {
        return mTextSkewX
    }

    fun setTextSkewX(textSkewX: Float) {
        mTextSkewX = textSkewX
        invalidate()
    }

    fun getTextSuffix(): String? {
        return mTextSuffix
    }

    fun setTextSuffix(textSuffix: String) {
        mTextSuffix = textSuffix
        invalidate()
    }

    fun getTextPrefix(): String? {
        return mTextPrefix
    }

    fun setTextPrefix(textPrefix: String) {
        mTextPrefix = textPrefix
        invalidate()
    }

    fun isTextVisible(): Boolean {
        return mTextVisible
    }

    fun setTextVisible(textVisible: Boolean) {
        mTextVisible = textVisible
        invalidate()
    }

    fun isReachCapRound(): Boolean {
        return mReachCapRound
    }

    fun setReachCapRound(reachCapRound: Boolean) {
        mReachCapRound = reachCapRound
        invalidate()
    }

    fun getRadius(): Int {
        return mRadius
    }

    fun setRadius(radius: Int) {
        mRadius = ViewUtils.dp2px(context, radius)
        invalidate()
    }

    fun getStartArc(): Int {
        return mStartArc
    }

    fun setStartArc(startArc: Int) {
        mStartArc = startArc
        invalidate()
    }

    fun getInnerBackgroundColor(): Int {
        return mInnerBackgroundColor
    }

    fun setInnerBackgroundColor(innerBackgroundColor: Int) {
        mInnerBackgroundColor = innerBackgroundColor
        invalidate()
    }

    fun getProgressStyle(): Int {
        return mProgressStyle
    }

    fun setProgressStyle(progressStyle: Int) {
        mProgressStyle = progressStyle
        invalidate()
    }

    fun getInnerPadding(): Int {
        return mInnerPadding
    }

    fun setInnerPadding(innerPadding: Int) {
        mInnerPadding = ViewUtils.dp2px(context, innerPadding)
        val mInnerRadius = mRadius - mOuterSize / 2 - mInnerPadding
        rectInner = RectF((-mInnerRadius).toFloat(), (-mInnerRadius).toFloat(), mInnerRadius.toFloat(), mInnerRadius.toFloat())
        invalidate()
    }

    fun getOuterColor(): Int {
        return mOuterColor
    }

    fun setOuterColor(outerColor: Int) {
        mOuterColor = outerColor
        invalidate()
    }

    fun getOuterSize(): Int {
        return mOuterSize
    }

    fun setOuterSize(outerSize: Int) {
        mOuterSize = ViewUtils.dp2px(context, outerSize)
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(STATE, super.onSaveInstanceState())
        // 保存当前样式
        bundle.putInt(PROGRESS_STYLE, getProgressStyle())
        bundle.putInt(RADIUS, getRadius())
        bundle.putBoolean(IS_REACH_CAP_ROUND, isReachCapRound())
        bundle.putInt(START_ARC, getStartArc())
        bundle.putInt(INNER_BG_COLOR, getInnerBackgroundColor())
        bundle.putInt(INNER_PADDING, getInnerPadding())
        bundle.putInt(OUTER_COLOR, getOuterColor())
        bundle.putInt(OUTER_SIZE, getOuterSize())
        // 保存text信息
        bundle.putInt(TEXT_COLOR, getTextColor())
        bundle.putInt(TEXT_SIZE, getTextSize())
        bundle.putFloat(TEXT_SKEW_X, getTextSkewX())
        bundle.putBoolean(TEXT_VISIBLE, isTextVisible())
        bundle.putString(TEXT_SUFFIX, getTextSuffix())
        bundle.putString(TEXT_PREFIX, getTextPrefix())
        // 保存已到达进度信息
        bundle.putInt(REACH_BAR_COLOR, getReachBarColor())
        bundle.putInt(REACH_BAR_SIZE, getReachBarSize())

        // 保存未到达进度信息
        bundle.putInt(NORMAL_BAR_COLOR, getNormalBarColor())
        bundle.putInt(NORMAL_BAR_SIZE, getNormalBarSize())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            mProgressStyle = state.getInt(PROGRESS_STYLE)
            mRadius = state.getInt(RADIUS)
            mReachCapRound = state.getBoolean(IS_REACH_CAP_ROUND)
            mStartArc = state.getInt(START_ARC)
            mInnerBackgroundColor = state.getInt(INNER_BG_COLOR)
            mInnerPadding = state.getInt(INNER_PADDING)
            mOuterColor = state.getInt(OUTER_COLOR)
            mOuterSize = state.getInt(OUTER_SIZE)

            mTextColor = state.getInt(TEXT_COLOR)
            mTextSize = state.getInt(TEXT_SIZE)
            mTextSkewX = state.getFloat(TEXT_SKEW_X)
            mTextVisible = state.getBoolean(TEXT_VISIBLE)
            mTextSuffix = state.getString(TEXT_SUFFIX)
            mTextPrefix = state.getString(TEXT_PREFIX)

            mReachBarColor = state.getInt(REACH_BAR_COLOR)
            mReachBarSize = state.getInt(REACH_BAR_SIZE)
            mNormalBarColor = state.getInt(NORMAL_BAR_COLOR)
            mNormalBarSize = state.getInt(NORMAL_BAR_SIZE)

            initPaint()
            super.onRestoreInstanceState(state.getParcelable(STATE))
            return
        }
        super.onRestoreInstanceState(state)
    }

    override fun invalidate() {
        initPaint()
        super.invalidate()
    }
}