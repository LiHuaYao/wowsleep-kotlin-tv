package com.idianwoo.tv.widget

import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.text.Html.ImageGetter
import android.view.View
import android.widget.TextView

import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.URI
import java.net.URL

class HtmlHttpImageGetter : ImageGetter {
    internal var container: TextView
    internal var baseUri: URI? = null
    internal var matchParentWidth: Boolean = false

    constructor(textView: TextView) {
        this.container = textView
        this.matchParentWidth = false
    }

    constructor(textView: TextView, baseUrl: String?) {
        this.container = textView
        if (baseUrl != null) this.baseUri = URI.create(baseUrl)
    }

    constructor(textView: TextView, baseUrl: String?, matchParentWidth: Boolean) {
        urlNum = 0
        this.container = textView
        this.matchParentWidth = matchParentWidth
        if (baseUrl != null) this.baseUri = URI.create(baseUrl)
    }

    override fun getDrawable(source: String): Drawable {
        val urlDrawable = UrlDrawable()
        urlNum++//限制详情页最多显示20张图片
        if (urlNum < 20) {
            // get the actual source
            val asyncTask = ImageGetterAsyncTask(urlDrawable, this, container, matchParentWidth)
            asyncTask.execute(source)
        }
        // return reference to URLDrawable which will asynchronously load the image specified in the src tag
        return urlDrawable
    }

    /**
     * Static inner [AsyncTask] that keeps a [WeakReference] to the [UrlDrawable]
     * and [HtmlHttpImageGetter].
     *
     *
     * This way, if the AsyncTask has a longer life span than the UrlDrawable,
     * we won't leak the UrlDrawable or the HtmlRemoteImageGetter.
     */
    private class ImageGetterAsyncTask(d: UrlDrawable, imageGetter: HtmlHttpImageGetter, container: View, private val matchParentWidth: Boolean) : AsyncTask<String, Void, Drawable>() {
        private val drawableReference: WeakReference<UrlDrawable>
        private val imageGetterReference: WeakReference<HtmlHttpImageGetter>
        private val containerReference: WeakReference<View>
        private var source: String? = null
        private var scale: Float = 0.toFloat()

        init {
            this.drawableReference = WeakReference(d)
            this.imageGetterReference = WeakReference(imageGetter)
            this.containerReference = WeakReference(container)
        }

        override fun doInBackground(vararg params: String): Drawable? {
            source = params[0]
            return fetchDrawable(source)
        }

        override fun onPostExecute(result: Drawable?) {
            if (result == null) {
                return
            }
            val urlDrawable = drawableReference.get() ?: return
// set the correct bound according to the result from HTTP call
            urlDrawable.setBounds(0, 0, (result.intrinsicWidth * scale).toInt(), (result.intrinsicHeight * scale).toInt())

            // change the reference of the current drawable to the result from the HTTP call
            urlDrawable.drawable = result

            val imageGetter = imageGetterReference.get() ?: return
// redraw the image by invalidating the container
            imageGetter.container.invalidate()
            // re-set text to fix images overlapping text
            imageGetter.container.text = imageGetter.container.text
        }

        /**
         * Get the Drawable from URL
         */
        fun fetchDrawable(urlString: String?): Drawable? {
            try {
                val `is` = fetch(urlString)
                val drawable = Drawable.createFromStream(`is`, "src")
                scale = getScale(drawable)
                drawable.setBounds(0, 0, (drawable.intrinsicWidth * scale).toInt(), (drawable.intrinsicHeight * scale).toInt())
                return drawable
            } catch (e: Exception) {
                return null
            }
        }

        private fun getScale(drawable: Drawable): Float {
            val container = containerReference.get()
            if (!matchParentWidth || container == null) return 1f

            val maxWidth = container.width.toFloat()
            val originalDrawableWidth = drawable.intrinsicWidth.toFloat()
            return maxWidth / originalDrawableWidth
        }

        @Throws(IOException::class)
        private fun fetch(urlString: String?): InputStream? {
            val url: URL
            val imageGetter = imageGetterReference.get() ?: return null
            if (imageGetter.baseUri != null) url = imageGetter.baseUri!!.resolve(urlString!!).toURL()
            else url = URI.create(urlString!!).toURL()

            return url.content as InputStream
        }
    }

    inner class UrlDrawable : BitmapDrawable() {
        var drawable: Drawable? = null

        override fun draw(canvas: Canvas) {
            // override the draw to facilitate refresh function later
            if (drawable != null) drawable!!.draw(canvas)
        }
    }

    companion object {
        internal var urlNum = 0
    }
}