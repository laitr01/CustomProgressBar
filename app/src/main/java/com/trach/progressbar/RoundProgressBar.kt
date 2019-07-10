package com.trach.progressbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
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
    private var progressHeight: Int = 0

    private var max: Int = 0
    private var progress = 0

    private var colorBackground: Int = 0
    private var colorStartProgress: Int = 0
    private var colorMiddleProgress: Int = 0
    private var colorEndProgress: Int = 0

    private var dateOffset: Int = 0

    private var hasLabel: Boolean = false

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

        hasLabel = typedArray.getBoolean(R.styleable.RoundProgressBar_hasLabel, false)

        max = typedArray.getInt(
            R.styleable.RoundProgressBar_checkInMaxProgress,
            DEFAULT_MAX_PROGRESS
        ) + if (hasLabel) 1 else 0
        progress =
            typedArray.getInt(R.styleable.RoundProgressBar_checkInProgress, DEFAULT_PROGRESS) + if (hasLabel) 1 else 0
        dateOffset = typedArray.getInt(R.styleable.RoundProgressBar_dateOffset, DEFAULT_DATE_OFFSET)

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
            progressHeight = (newHeight / 3.0f).toInt()
            removeAll()
            drawAll()
            postDelayed({
                drawPrimaryProgress()
            }, 5)
        }
    }

    private fun drawPrimaryProgress() {
        drawProgress(
            layoutProgress,
            max,
            progress,
            totalWidth.toFloat(),
            progressHeight,
            radius,
            padding,
            colorStartProgress
        )
    }

    private fun drawAll() {
        drawBackgroundProgress()
        drawPadding()
        drawPrimaryProgress()
        drawDateProgress()
        drawMarker()
        drawPointProgress()
    }

    private fun removeAll() {
        badgeLayout.removeAllViews()
        layoutDate.removeAllViews()
        pointLayout.removeAllViews()
    }

    private fun drawPointProgress() {
        val params = pointLayout.layoutParams.apply {
            width = totalWidth
            height = progressHeight
        }
        pointLayout.layoutParams = params

        val texViewWidth = (totalWidth - padding * 2) / max
        val textViewLayoutParam = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        textViewLayoutParam.width = texViewWidth

        val max = if (hasLabel) max - 1 else max
        val currentProgress = if (hasLabel) progress - 1 else progress

        for (i in 1..max step dateOffset) {
            val pointTextView = TextView(context).apply {
                text = "+${i * 2}"
                setTextColor(if (i <= currentProgress) ContextCompat.getColor(context, R.color.orange) else ContextCompat.getColor(context, R.color.gray))
                gravity = Gravity.CENTER or Gravity.BOTTOM
                textSize = if (i == currentProgress) 14f else 10f
                layoutParams = textViewLayoutParam
            }
            pointLayout.addView(pointTextView)
        }
    }

    private fun drawMarker() {
        val params = badgeLayout.layoutParams.apply {
            height = progressHeight
        }
        badgeLayout.layoutParams = params
        val badgeSize = progressHeight / 3
        val badge = ImageView(context).apply {
            layoutParams = LayoutParams(badgeSize, badgeSize).apply {
                val ratio = max.toFloat() / progress
                val width = (totalWidth - padding * 2)
                val texViewWidth = width / max

                leftMargin = (width / ratio).toInt() - texViewWidth / 2 - badgeSize / 2
            }

            setImageResource(R.drawable.marker_circlular)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        badgeLayout.addView(badge)
    }

    private fun drawDateProgress() {

        val progressParams = layoutDate.layoutParams
        progressParams.width = totalWidth
        progressParams.height = progressHeight
        layoutDate.layoutParams = progressParams
        layoutDate.bringToFront()

        val texViewWidth = (totalWidth - padding * 2) / max
        val textViewLayoutParam = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        textViewLayoutParam.width = texViewWidth

        if (hasLabel) {
            val labelTextView = TextView(context).apply {
                layoutParams = textViewLayoutParam
                text = "NGÀY"
                setTextColor(Color.WHITE)
                setPadding(10, 0, 0, 0)
                gravity = Gravity.CENTER
                textSize = 10f
            }
            layoutDate.addView(labelTextView)
        }
        val max = if (hasLabel) max - 1 else max
        val currentProgress = if (hasLabel) progress - 1 else progress
        for (i in 1..max step dateOffset) {
            val dateTextView = TextView(context).apply {
                text = i.toString()
                setTextColor(if (i <= currentProgress) Color.WHITE else ContextCompat.getColor(context, R.color.gray))
                gravity = Gravity.CENTER
                textSize = 10f
                layoutParams = textViewLayoutParam
            }
            layoutDate.addView(dateTextView)
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

        val params = layoutBackground.layoutParams
        params.height = progressHeight
        layoutBackground.layoutParams = params
    }


    // Draw a progress by sub class
    private fun drawProgress(
        layoutProgress: LinearLayout,
        max: Int,
        progress: Int,
        totalWidth: Float,
        progressHeight: Int,
        radius: Int,
        padding: Int,
        colorProgress: Int
    ) {

        val colors = intArrayOf(colorStartProgress, colorMiddleProgress, colorEndProgress)
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

        val ratio = max.toFloat() / progress
        val progressWidth = ((totalWidth - padding * 2) / ratio).toInt()
        val progressParams = layoutProgress.layoutParams
        progressParams.width = progressWidth
        progressParams.height = progressHeight
        layoutProgress.layoutParams = progressParams
    }


    override fun invalidate() {
        super.invalidate()
        removeAll()
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
    }

    fun getPadding(): Int {
        return padding
    }

    fun setPadding(padding: Int) {
        if (padding >= 0)
            this.padding = padding
        drawPadding()
        drawPrimaryProgress()
    }

    fun getMax() = max

    fun setMax(max: Int) {
        if (max >= 0)
            this.max = max + if (hasLabel) 1 else 0
        if (this.progress > max)
            this.progress = max
        drawPrimaryProgress()
    }

    fun getLayoutWidth(): Float {
        return totalWidth.toFloat()
    }

    fun getProgress() = progress

    fun setProgress(progress: Int) {

        when {
            progress < 0 -> this.progress = 0
            progress > max -> this.progress = max
            else -> this.progress = progress + if (hasLabel) 1 else 0
        }
        drawPrimaryProgress()
        progressChangedListener?.onProgressChanged(id, this.progress, true)
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

    companion object {
        const val DEFAULT_MAX_PROGRESS = 7
        const val DEFAULT_PROGRESS = 0
        const val DEFAULT_PROGRESS_RADIUS = 3
        const val DEFAULT_BACKGROUND_PADDING = 0
        const val DEFAULT_DATE_OFFSET = 1
    }

    interface OnProgressChangedListener {
        fun onProgressChanged(viewId: Int, progress: Int, isPrimaryProgress: Boolean)
    }
}