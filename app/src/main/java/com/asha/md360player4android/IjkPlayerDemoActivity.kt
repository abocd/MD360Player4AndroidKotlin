package com.asha.md360player4android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.Window
import android.view.WindowManager

import tv.danmaku.ijk.media.player.IMediaPlayer


/**
 * Created by hzqiujiadi on 16/7/6.
 * hzqiujiadi ashqalcn@gmail.com
 */
class IjkPlayerDemoActivity : Activity(), TextureView.SurfaceTextureListener {

    private var surface: Surface? = null

    private val mMediaPlayerWrapper = MediaPlayerWrapper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // full screen
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_ijkdemo)

        mMediaPlayerWrapper.init()
        mMediaPlayerWrapper.setPreparedListener (IMediaPlayer.OnPreparedListener{
            cancelBusy()
        })

        val textureView = findViewById(R.id.video_view) as TextureView
        textureView.surfaceTextureListener = this

        val uri = uri
        if (uri != null) {
            mMediaPlayerWrapper.openRemoteFile(uri.toString())
            mMediaPlayerWrapper.prepare()
        }

    }

    protected val uri: Uri?
        get() {
            val i = intent
            if (i == null || i.data == null) {
                return null
            }
            return i.data
        }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        surface = Surface(surfaceTexture)
        mMediaPlayerWrapper.setSurface(surface)

    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        mMediaPlayerWrapper.setSurface(null)
        this.surface = null
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayerWrapper.destroy()
    }

    override fun onPause() {
        super.onPause()
        mMediaPlayerWrapper.pause()
    }

    override fun onResume() {
        super.onResume()
        mMediaPlayerWrapper.resume()
    }

    fun cancelBusy() {
        findViewById(R.id.progress).visibility = View.GONE
    }

    companion object {

        fun start(context: Context, uri: Uri) {
            val i = Intent(context, IjkPlayerDemoActivity::class.java)
            i.data = uri
            context.startActivity(i)
        }
    }
}
