package com.asha.md360player4android

import android.net.Uri
import android.os.Bundle
import android.view.Surface
import android.view.View
import android.widget.Toast

import com.asha.vrlib.MD360Director
import com.asha.vrlib.MD360DirectorFactory
import com.asha.vrlib.MDVRLibrary
import com.asha.vrlib.model.BarrelDistortionConfig
import com.asha.vrlib.model.MDPinchConfig

import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

/**
 * Created by hzqiujiadi on 16/4/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
class VideoPlayerActivity : MD360PlayerActivity() {
    private val mMediaPlayerWrapper = MediaPlayerWrapper()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMediaPlayerWrapper.init()
        mMediaPlayerWrapper.setPreparedListener(  IMediaPlayer.OnPreparedListener{
//             fun onPrepared(var1:IMediaPlayer) {
                cancelBusy()
                if (vrLibrary != null) {
                    vrLibrary!!.notifyPlayerChanged()
                }
//            }
        })

        mMediaPlayerWrapper.player!!.setOnErrorListener { mp, what, extra ->
            val error = String.format("Play Error what=%d extra=%d", what, extra)
            Toast.makeText(this@VideoPlayerActivity, error, Toast.LENGTH_SHORT).show()
            true
        }

        mMediaPlayerWrapper.player!!.setOnVideoSizeChangedListener (object:IMediaPlayer.OnVideoSizeChangedListener{
            override fun onVideoSizeChanged(mp:IMediaPlayer , width :Int, height:Int, sar_num:Int, sar_den:Int) {
                vrLibrary!!.onTextureResize(width.toFloat(), height.toFloat())
            }
        })

        val uri = uri
        if (uri != null) {
            mMediaPlayerWrapper.openRemoteFile(uri.toString())
            mMediaPlayerWrapper.prepare()
        }

        findViewById(R.id.control_next).setOnClickListener {
            mMediaPlayerWrapper.pause()
            mMediaPlayerWrapper.destroy()
            mMediaPlayerWrapper.init()
            mMediaPlayerWrapper.openRemoteFile(DemoActivity.sPath + "video_31b451b7ca49710719b19d22e19d9e60.mp4")
            mMediaPlayerWrapper.prepare()
        }

    }

    override fun createVRLibrary(): MDVRLibrary {
        return MDVRLibrary.with(this)
                .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION)
                .asVideo { surface -> mMediaPlayerWrapper.setSurface(surface) }
                .ifNotSupport { mode ->
                    val tip = if (mode == MDVRLibrary.INTERACTIVE_MODE_MOTION)
                        "onNotSupport:MOTION"
                    else
                        "onNotSupport:" + mode.toString()
                    Toast.makeText(this@VideoPlayerActivity, tip, Toast.LENGTH_SHORT).show()
                }
                .pinchConfig(MDPinchConfig().setMin(1.0f).setMax(8.0f).setDefaultValue(0.1f))
                .pinchEnabled(true)
                .directorFactory(object : MD360DirectorFactory() {
                    override fun createDirector(index: Int): MD360Director {
                        return MD360Director.builder().setPitch(90f).build()
                    }
                })
                .projectionFactory(CustomProjectionFactory())
                .barrelDistortionConfig(BarrelDistortionConfig().setDefaultEnabled(false).setScale(0.95f))
                .build(findViewById(R.id.gl_view))
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

    companion object {

        private val TAG = "VideoPlayerActivity"
    }
}
