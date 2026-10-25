package io.github.yilink1.hyperselect

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat

/**
 * 自绘开关：实心蓝轨道 + 白色滑块，切换带 200ms 平滑动效，关闭态为中性灰轨道。
 * 不依赖系统各版本 ROM 碎片化的 SwitchCompat XML 机制，各版本 HyperOS 表现坚如磐石。
 */
class HyperSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    /** 用户点按切换后回调（代码里调用 setChecked 不会触发）。 */
    var onToggled: ((Boolean) -> Unit)? = null

    var checked: Boolean = false
        private set

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val trackRect = RectF()
    private val colorOn = ContextCompat.getColor(context, R.color.hyper_accent)
    private val colorOff = ContextCompat.getColor(context, R.color.hyper_switch_off)
    private val evaluator = ArgbEvaluator()

    private var progress = 0f
    private var animator: ValueAnimator? = null

    init {
        isClickable = true
        isFocusable = true
        setOnClickListener { toggle() }
    }

    fun toggle() {
        setChecked(!checked, animate = true)
        onToggled?.invoke(checked)
    }

    fun setChecked(value: Boolean, animate: Boolean) {
        checked = value
        val target = if (value) 1f else 0f
        animator?.cancel()
        if (!animate || !isAttachedToWindow) {
            progress = target
            invalidate()
        } else {
            animator = ValueAnimator.ofFloat(progress, target).apply {
                duration = 200L
                interpolator = DecelerateInterpolator(1.4f)
                addUpdateListener {
                    progress = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = if (enabled) 1f else 0.4f
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(dp(50), dp(30))
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        trackPaint.color = evaluator.evaluate(progress, colorOff, colorOn) as Int
        trackRect.set(0f, 0f, w, h)
        canvas.drawRoundRect(trackRect, h / 2f, h / 2f, trackPaint)

        val inset = dp(3).toFloat()
        val radius = h / 2f - inset
        val cx = (h / 2f) + (w - h) * progress
        canvas.drawCircle(cx, h / 2f, radius, thumbPaint)
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = "android.widget.Switch"
        info.isCheckable = true
        info.isChecked = checked
    }
}
