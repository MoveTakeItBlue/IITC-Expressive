package org.maplibre.android.text

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.WorkerThread
import java.util.Locale

/**
 * Custom LocalGlyphRasterizer ensuring that locally rasterized CJK ideographs
 * strictly match the project's Material 3 Expressive unified Sans-Serif typography
 * (sans-serif / Noto Sans CJK SC) instead of falling back to unintended serif / songti fonts.
 */
@Keep
open class LocalGlyphRasterizer {
    private val bitmap: Bitmap = Bitmap.createBitmap(35, 35, Bitmap.Config.ARGB_8888)
    private val paint: Paint = Paint().apply {
        isAntiAlias = true
        isSubpixelText = true
        textSize = 24f
        textLocale = Locale.getDefault()
    }
    private val canvas: Canvas = Canvas(bitmap)

    @WorkerThread
    protected open fun drawGlyphBitmap(fontFamily: String?, bold: Boolean, glyphID: Char): Bitmap {
        val tf = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Typeface.create(Typeface.SANS_SERIF, if (bold) 700 else 500, false)
        } else {
            Typeface.create("sans-serif-medium", if (bold) Typeface.BOLD else Typeface.NORMAL)
        }
        paint.typeface = tf
        canvas.drawColor(Color.WHITE)
        canvas.drawText(glyphID.toString(), 5f, 25f, paint)
        return bitmap
    }
}
