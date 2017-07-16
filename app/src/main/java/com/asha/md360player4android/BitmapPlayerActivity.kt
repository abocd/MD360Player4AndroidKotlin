package com.asha.md360player4android

import android.content.ContentResolver
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.util.Log
import android.view.View

import com.asha.vrlib.MDVRLibrary
import com.asha.vrlib.model.MDRay
import com.asha.vrlib.plugins.hotspot.IMDHotspot
import com.asha.vrlib.texture.MD360BitmapTexture
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

import com.squareup.picasso.MemoryPolicy.NO_CACHE
import com.squareup.picasso.MemoryPolicy.NO_STORE

/**
 * Created by hzqiujiadi on 16/4/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
class BitmapPlayerActivity : MD360PlayerActivity() {

    private var nextUri: Uri = Uri.EMPTY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById(R.id.control_next).setOnClickListener {
            busy()
            nextUri = getDrawableUri(R.drawable.texture)
            vrLibrary!!.notifyPlayerChanged()
        }
    }

    private var mTarget: Target? = null// keep the reference for picasso.

    private fun loadImage(uri: Uri, callback: MD360BitmapTexture.Callback) {
        mTarget = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                Log.d(TAG, "loaded image, size:" + bitmap.width + "," + bitmap.height)

                // notify if size changed
                vrLibrary!!.onTextureResize(bitmap.width.toFloat(), bitmap.height.toFloat())

                // texture
                callback.texture(bitmap)
                cancelBusy()
            }

            override fun onBitmapFailed(errorDrawable: Drawable) {

            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable) {

            }
        }
        Log.d(TAG, "load image with max texture size:" + callback.maxTextureSize)
        Picasso.with(applicationContext)
                .load(uri)
                .resize(callback.maxTextureSize, callback.maxTextureSize)
                .onlyScaleDown()
                .centerInside()
                .memoryPolicy(NO_CACHE, NO_STORE)
                .into(mTarget!!)
    }

    private fun currentUri(): Uri {
        if (nextUri == null) {
            var uri:Uri = Uri.EMPTY
            return uri
        } else {
            return nextUri
        }
    }

    override fun createVRLibrary(): MDVRLibrary {
        return MDVRLibrary.with(this)
                .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_TOUCH)
                .asBitmap { callback -> loadImage(currentUri(), callback) }
                .listenTouchPick { hitHotspot, ray -> Log.d(TAG, "Ray:$ray, hitHotspot:$hitHotspot") }
                .pinchEnabled(true)
                .projectionFactory(CustomProjectionFactory())
                .build(findViewById(R.id.gl_view))
    }

    private fun getDrawableUri(@DrawableRes resId: Int): Uri {
        val resources = resources
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId) + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId))
    }

    companion object {

        private val TAG = "BitmapPlayerActivity"
    }
}
