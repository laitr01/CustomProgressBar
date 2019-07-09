package com.trach.progressbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.layout_round_corner_progress_bar.view.*

class RoundProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var radius: Int = 0
    private var padding: Int = 0
    private var totalWidth: Int = 0

    private var max: Float = 0.toFloat()
    private var progress: Float = 0.toFloat()
    private var secondaryProgress: Float = 0.toFloat()

    private var colorBackground: Int = 0
    private var colorStartProgress: Int = 0
    private var colorMiddleProgress: Int = 0
    private var colorEndProgress: Int = 0

    private var progressChangedListener: OnProgressChangedListener? = null

    init {
        if (isInEditMode) {
            previewLayout(context)
        } else {
            setup(context, attrs)
        }
    }

    private fun setup(context: Context, attrs: AttributeSet) {
        setupStyleable(context, attrs)
        removeAllViews()

        LayoutInflater.from(context).inflate(R.layout.layout_round_corner_progress_bar, this)
    }

    private fun previewLayout(context: Context) {
        gravity = Gravity.CENTER
        val textView = TextView(context)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        with(textView) {
            layoutParams = params
            gravity = Gravity.CENTER
            text = javaClass.simpleName
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.GRAY)
            addView(textView)
        }
    }

    // Retrieve initial parameter from view attribute
    private fun setupStyleable(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBar)

        radius = typedArray.getDimension(R.styleable.RoundProgressBar_radius, dp2px(DEFAULT_PROGRESS_RADIUS.toFloat()))
            .toInt()
        padding = typedArray.getDimension(
            R.styleable.RoundProgressBar_backgroundPadding,
            dp2px(DEFAULT_BACKGROUND_PADDING.toFloat())
        ).toInt()

        max = typedArray.getFloat(R.styleable.RoundProgressBar_checkInMaxProgress, DEFAULT_MAX_PROGRESS.toFloat())
        progress = typedArray.getFloat(R.styleable.RoundProgressBar_checkInProgress, DEFAULT_PROGRESS.toFloat())

        val colorBackgroundDefault = ContextCompat.getColor(context, R.color.progress_bar_background_default)
        colorBackground =
            typedArray.getColor(R.styleable.RoundProgressBar_checkInBackgroundColor, colorBackgroundDefault)

        val colorStartProgressDefault = ContextCompat.getColor(context, R.color.progress_bar_start_color_default)
        colorStartProgress =
            typedArray.getColor(R.styleable.RoundProgressBar_checkInProgressStartColor, colorStartProgressDefault)

        val colorMiddleProgressDefault = ContextCompat.getColor(context, R.color.progress_bar_middle_color_default)
        colorMiddleProgress =
            typedArray.getColor(R.styleable.RoundProgressBar_checkInProgressMiddleColor, colorMiddleProgressDefault)

        val colorEndProgressDefault = ContextCompat.getColor(context, R.color.progress_bar_end_color_default)
        colorEndProgress =
            typedArray.getColor(R.styleable.RoundProgressBar_checkInProgressEndColor, colorEndProgressDefault)

        typedArray.recycle()

    }

    // Progress bar always refresh when view size has changed
    override fun onSizeChanged(newWidth: Int, newHeight: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight)
        if (!isInEditMode) {
            totalWidth = newWidth
            drawAll()
            postDelayed({
                drawPrimaryProgress()
                drawSecondaryProgress()
            }, 5)
        }
    }

    private fun drawSecondaryProgress() {
        drawProgress(
            layoutSecondaryProgress,
            max,
            secondaryProgress,
            totalWidth.toFloat(),
            radius,
            padding,
            colorStartProgress
        )
    }

    private fun drawPrimaryProgress() {
        drawProgress(
            layoutProgress,
            max, progress,
            totalWidth.toFloat(),
            radius,
            padding,
            colorStartProgress
        )
    }

    private fun drawAll() {
        drawBackgroundProgress()
        drawPadding()
        drawProgress(layoutProgress)
        drawProgress(layoutSecondaryProgress)
        drawPrimaryProgress()
        drawSecondaryProgress()
    }

    private fun drawProgress(layoutProgress: LinearLayout) {
        val progressParams = layoutProgress.layoutParams as RelativeLayout.LayoutParams
        removeLayoutParamsRule(progressParams)
        progressParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        // For support with RTL on API 17 or more
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            progressParams.addRule(RelativeLayout.ALIGN_PARENT_START)
        layoutProgress.layoutParams = progressParams
    }

    // Remove all of relative align rule
    private fun removeLayoutParamsRule(layoutParams: RelativeLayout.LayoutParams) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END)
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT)
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_START)
        } else {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0)
        }
    }

    private fun drawPadding() {
        layoutBackground.setPadding(padding, padding, padding, padding)
    }

    private fun drawBackgroundProgress() {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        gradientDrawable.setColor(colorBackground)
        val newRadius = radius - padding / 2
        gradientDrawable.cornerRadii = floatArrayOf(
            newRadius.toFloat(),
            newRadius.toFloat(),
            newRadius.toFloat(),
            newRadius.toFloat(),
            newRadius.toFloat(),
            newRadius.toFloat(),
            newRadius.toFloat(),
            newRadius.toFloat()
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            layoutBackground.background = gradientDrawable
        } else {
            layoutBackground.setBackgroundDrawable(gradientDrawable)
        }
    }


    // Draw a progress by sub class
    private fun drawProgress(
        layoutProgress: LinearLayout, max: Float, progress: Float, totalWidth: Float,
        radius: Int, padding: Int, colorProgress: Int
    ) {

        val colors = intArrayOf(colorStartProgress, colorEndProgress)
        val gradientDrawable = GradientDrawable()

        gradientDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            gradientDrawable.colors = colors
            gradientDrawable.orientation = GradientDrawable.Orientation.LEFT_RIGHT
        } else {
            gradientDrawable.setColor(colorProgress)
        }

        val newRadius = radius - padding / 2
        gradientDrawable.cornerRadii = floatArrayOf(
            newRadius.toFloat(),
            newRadius.toFloat(),
            newRadius.toFloat(),
            newRadius.toFloat(),
            newRadius.toFloat(),
            newRadius.toFloat(),
            newRadius.toFloat(),
            newRadius.toFloat()
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            layoutProgress.background = gradientDrawable
        } else {
            layoutProgress.setBackgroundDrawable(gradientDrawable)
        }

        val ratio = max / progress
        val progressWidth = ((totalWidth - padding * 2) / ratio).toInt()
        val progressParams = layoutProgress.layoutParams
        progressParams.width = progressWidth
        layoutProgress.layoutParams = progressParams
    }


    override fun invalidate() {
        super.invalidate()
        drawAll()
    }

    @SuppressLint("NewApi")
    private fun dp2px(dp: Float): Float {
        val displayMetrics = context.resources.displayMetrics
        return Math.round(dp * (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)).toFloat()
    }

    fun getRadius(): Int {
        return radius
    }

    fun setRadius(radius: Int) {
        if (radius >= 0)
            this.radius = radius
        drawBackgroundProgress()
        drawPrimaryProgress()
        drawSecondaryProgress()
    }

    fun getPadding(): Int {
        return padding
    }

    fun setPadding(padding: Int) {
        if (padding >= 0)
            this.padding = padding
        drawPadding()
        drawPrimaryProgress()
        drawSecondaryProgress()
    }

    fun getMax(): Float {
        return max
    }

    fun setMax(max: Float) {
        if (max >= 0)
            this.max = max
        if (this.progress > max)
            this.progress = max
        drawPrimaryProgress()
        drawSecondaryProgress()
    }

    fun getLayoutWidth(): Float {
        return totalWidth.toFloat()
    }

    fun getProgress(): Float {
        return progress
    }

    fun setProgress(progress: Float) {
        when {
            progress < 0 -> this.progress = 0f
            progress > max -> this.progress = max
            else -> this.progress = progress
        }
        drawPrimaryProgress()
        progressChangedListener?.onProgressChanged(id, this.progress, true, false)
    }

    fun getSecondaryProgressWidth(): Float {
        return if (layoutSecondaryProgress != null) layoutSecondaryProgress.width.toFloat() else 0f
    }

    fun getSecondaryProgress(): Float {
        return secondaryProgress
    }

    fun setSecondaryProgress(secondaryProgress: Float) {
        when {
            secondaryProgress < 0 -> this.secondaryProgress = 0f
            secondaryProgress > max -> this.secondaryProgress = max
            else -> this.secondaryProgress = secondaryProgress
        }
        drawSecondaryProgress()
        progressChangedListener?.onProgressChanged(id, this.secondaryProgress, false, true)
    }

    fun getProgressBackgroundColor(): Int {
        return colorBackground
    }

    fun setProgressBackgroundColor(colorBackground: Int) {
        this.colorBackground = colorBackground
        drawBackgroundProgress()
    }

    fun getProgressColor(): Int {
        return colorStartProgress
    }

    fun setProgressColor(colorProgress: Int) {
        this.colorStartProgress = colorProgress
        drawPrimaryProgress()
    }

    fun getSecondaryProgressColor(): Int {
        return colorMiddleProgress
    }

    fun setSecondaryProgressColor(colorSecondaryProgress: Int) {
        this.colorMiddleProgress = colorSecondaryProgress
        drawSecondaryProgress()
    }

    companion object {
        const val DEFAULT_MAX_PROGRESS = 100
        const val DEFAULT_PROGRESS = 0
        const val DEFAULT_SECONDARY_PROGRESS = 0
        const val DEFAULT_PROGRESS_RADIUS = 30
        const val DEFAULT_BACKGROUND_PADDING = 0
    }

    interface OnProgressChangedListener {
        fun onProgressChanged(viewId: Int, progress: Float, isPrimaryProgress: Boolean, isSecondaryProgress: Boolean)
    }
}