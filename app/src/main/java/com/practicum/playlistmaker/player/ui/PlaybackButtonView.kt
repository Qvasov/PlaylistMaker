package com.practicum.playlistmaker.player.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.graphics.drawable.toBitmap
import com.practicum.playlistmaker.R

class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val playImage: Drawable?
    private val pauseImage: Drawable?
    private var currentImage: Drawable?
    private var imageRect = RectF(0f, 0f, 0f, 0f)

    init {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.PlaybackButtonView, defStyleAttr, defStyleRes
        ).apply {
            try {
                playImage = getDrawable(R.styleable.PlaybackButtonView_playImageResId)
                pauseImage = getDrawable(R.styleable.PlaybackButtonView_pauseImageResId)
                currentImage = playImage
            } finally {
                recycle()
            }

        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = (currentImage?.intrinsicWidth ?: suggestedMinimumWidth)+ paddingLeft + paddingRight
        val desiredHeight = (currentImage?.intrinsicHeight ?: suggestedMinimumHeight) + paddingTop + paddingBottom

        val width = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0)
        val height = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0)

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        imageRect = RectF(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        currentImage?.let {
            canvas.drawBitmap(it.toBitmap(), null, imageRect, null)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (currentImage == playImage) {
                updateButtonImage(false)
            } else {
                updateButtonImage(true)
            }
        }
        return super.onTouchEvent(event)
    }

    fun updateButtonImage(playState: Boolean) {
        currentImage = if (playState) playImage else pauseImage
        requestLayout()
    }
}