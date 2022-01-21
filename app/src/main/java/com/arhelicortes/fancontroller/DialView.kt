/**
 * This custom view resembles a physical fan control
 * with settings for off (0), low (1), medium (2), and high (3)
 * The code was taken by Advanced Android in Kotlin
 * 02.1: Creating Custom Views
 *
 * Created by arheli.cortes
 */
package com.arhelicortes.fancontroller

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.withStyledAttributes
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class DialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var radius = 0.0f
    private val pointPosition: PointF = PointF(0.0f, 0.0f)

    // fan speed
    private var fanSpeed = FanSpeed.OFF
    private var fanSpeedLowColor = 0
    private var fanSpeedMediumColor = 0
    private var fanSpeedHighColor = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    init {
        isClickable = true
        // custom the color for each fan dial position
        context.withStyledAttributes(attrs, R.styleable.DialView) {
            fanSpeedLowColor = getColor(R.styleable.DialView_fanColor1, 0)
            fanSpeedMediumColor = getColor(R.styleable.DialView_fanColor2, 0)
            fanSpeedHighColor = getColor(R.styleable.DialView_fanColor3, 0)
        }
        customClickAction()
        updateContentDescription()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        setDialBackgroundColor()
        drawDial(canvas)
        drawIndicatorCircle(canvas)
        drawLabels(canvas)
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true
        fanSpeed = fanSpeed.next()
        contentDescription = resources.getString(fanSpeed.label)
        updateContentDescription()
        invalidate()
        return true
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        radius = (min(width, height) / 2.0 * 0.8).toFloat()
    }

    private fun drawDial(canvas: Canvas?) {
        canvas?.apply {
            drawCircle(
                (width / 2).toFloat(),
                (height / 2).toFloat(),
                radius, paint
            )
        }
    }

    private fun drawIndicatorCircle(canvas: Canvas?) {
        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(fanSpeed, markerRadius)
        paint.color = Color.BLACK
        canvas?.apply {
            drawCircle(
                pointPosition.x,
                pointPosition.y,
                radius / 12,
                paint
            )
        }
    }

    private fun drawLabels(canvas: Canvas?) {
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        var label: String
        for (i in FanSpeed.values()) {
            pointPosition.computeXYForSpeed(i, labelRadius)
            label = resources.getString(i.label)
            canvas?.apply {
                drawText(
                    label,
                    pointPosition.x,
                    pointPosition.y,
                    paint
                )
            }
        }
    }

    private fun PointF.computeXYForSpeed(pos: FanSpeed, radius: Float) {
        val startAngle = Math.PI * (9 / 8.0)
        val angle = startAngle + pos.ordinal * (Math.PI / 4)
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2
    }

    private fun setDialBackgroundColor() {
        paint.color = when (fanSpeed) {
            FanSpeed.OFF -> Color.GRAY
            FanSpeed.LOW -> fanSpeedLowColor
            FanSpeed.MEDIUM -> fanSpeedMediumColor
            FanSpeed.HIGH -> fanSpeedHighColor
        }
    }

    /**
     * this function add more information for the click action
     */
    private fun customClickAction() {
        ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View?,
                info: AccessibilityNodeInfoCompat?
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val customClick = AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                    AccessibilityNodeInfo.ACTION_CLICK,
                    getActionLabel()
                )
                info?.apply { addAction(customClick) }
            }
        })
    }

    private fun getActionLabel(): String =
        context.getString(
            if (fanSpeed != FanSpeed.HIGH) {
                R.string.change
            } else {
                R.string.reset
            }
        )

    fun updateContentDescription() {
        contentDescription = resources.getString(fanSpeed.label)
    }

    companion object {
        private const val RADIUS_OFFSET_LABEL = 30
        private const val RADIUS_OFFSET_INDICATOR = -35
    }
}

private enum class FanSpeed(val label: Int) {
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);

    fun next() = when (this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }
}

