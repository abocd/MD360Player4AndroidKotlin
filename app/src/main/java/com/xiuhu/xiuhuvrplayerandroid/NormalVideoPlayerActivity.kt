package com.xiuhu.xiuhuvrplayerandroid

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast

import com.asha.vrlib.MD360Director
import com.asha.vrlib.MD360DirectorFactory
import com.asha.vrlib.MDVRLibrary
import com.asha.vrlib.model.BarrelDistortionConfig
import com.asha.vrlib.model.MDPinchConfig

import tv.danmaku.ijk.media.player.IMediaPlayer

/**
 * Created by hzqiujiadi on 16/4/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
class NormalVideoPlayerActivity : MD360PlayerActivity() {
    private val mMediaPlayerWrapper = MediaPlayerWrapper()

    private var isVr = false
    private var isGyr = true

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
            Toast.makeText(this@NormalVideoPlayerActivity, error, Toast.LENGTH_SHORT).show()
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

        findViewById(R.id.plugin_layout).setVisibility(View.GONE)
        findViewById(R.id.plugin_layout2).setVisibility(View.GONE)
        findViewById(R.id.plugin_layout3).setVisibility(View.GONE)
        findViewById(R.id.plugin_layout4).setVisibility(View.GONE)
        findViewById(R.id.director_brief_text).setVisibility(View.GONE)
        findViewById(R.id.hotspot_text).setVisibility(View.GONE)
        findViewById(R.id.progress).setVisibility(View.GONE)
        //有陀螺仪什么显示的
        findViewById(R.id.spinner_layout).setVisibility(View.GONE)
        findViewById(R.id.layout_ctrl).setVisibility(View.VISIBLE)
        //隐藏一个光标
        findViewById(R.id.hotspot_point2).setVisibility(View.GONE)

        //开始按钮监听
        findViewById(R.id.button_vr).setOnClickListener(View.OnClickListener {
                if (isVr == true){
                    Toast.makeText(this@NormalVideoPlayerActivity, "退出VR模式", Toast.LENGTH_SHORT).show()
                    vrLibrary!!.switchDisplayMode(this@NormalVideoPlayerActivity, MDVRLibrary.DISPLAY_MODE_NORMAL)
                    findViewById(R.id.hotspot_point2).setVisibility(View.GONE)
                    isVr = false
                } else {
                    Toast.makeText(this@NormalVideoPlayerActivity, "进入VR模式", Toast.LENGTH_SHORT).show()
                    vrLibrary!!.switchDisplayMode(this@NormalVideoPlayerActivity, MDVRLibrary.DISPLAY_MODE_GLASS)
                    findViewById(R.id.hotspot_point2).setVisibility(View.VISIBLE)
                    isVr = true
                }

        })

        findViewById(R.id.button_gyr).setOnClickListener(View.OnClickListener {
            if(isGyr == true){
                Toast.makeText(this@NormalVideoPlayerActivity, "不用陀螺仪", Toast.LENGTH_SHORT).show()
                vrLibrary!!.switchInteractiveMode(this@NormalVideoPlayerActivity, MDVRLibrary.INTERACTIVE_MODE_TOUCH)
                isGyr = false
            } else {
                Toast.makeText(this@NormalVideoPlayerActivity, "使用陀螺仪", Toast.LENGTH_SHORT).show()
                vrLibrary!!.switchInteractiveMode(this@NormalVideoPlayerActivity, MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH)
                isGyr = true
            }

        })


    }


//    public fun click(v:View){
////        print("Click ....")
//    }

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
                    Toast.makeText(this@NormalVideoPlayerActivity, tip, Toast.LENGTH_SHORT).show()
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

        private val TAG = "NormalVideoPlayerActivity"
    }
}
