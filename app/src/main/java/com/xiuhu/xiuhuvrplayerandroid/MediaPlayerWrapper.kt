package com.xiuhu.xiuhuvrplayerandroid

import android.content.Context
import android.view.Surface

import java.io.IOException

import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import tv.danmaku.ijk.media.player.misc.IMediaDataSource

/**
 * Created by hzqiujiadi on 16/4/5.
 * hzqiujiadi ashqalcn@gmail.com

 * http://developer.android.com/intl/zh-cn/reference/android/media/MediaPlayer.html
 * status
 */
class MediaPlayerWrapper : IMediaPlayer.OnPreparedListener {
    var player: IMediaPlayer? = null
        protected set
//    private var mPreparedListener: IjkMediaPlayer.OnPreparedListener? = null
    private var mPreparedListener: IMediaPlayer.OnPreparedListener? = null
    private var mStatus = STATUS_IDLE

    fun init() {
        mStatus = STATUS_IDLE
        player = IjkMediaPlayer()
        player!!.setOnPreparedListener(this)
        player!!.setOnInfoListener { mp, what, extra -> false }

        enableHardwareDecoding()
    }

    private fun enableHardwareDecoding() {
        if (player is IjkMediaPlayer) {
            val player = this.player as IjkMediaPlayer?
            player!!.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32.toLong())
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 60)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-fps", 0)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48)
        }
    }

    fun setSurface(surface: Surface?) {
        if (player != null) {
            player!!.setSurface(surface)
        }
    }

    fun openRemoteFile(url: String) {
        try {
            player!!.dataSource = url
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun openAssetFile(context: Context, assetPath: String) {
        try {
            val am = context.resources.assets
            val `is` = am.open(assetPath)
            player!!.setDataSource(object : IMediaDataSource {
                @Throws(IOException::class)
                override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
                    return `is`.read(buffer, offset, size)
                }

                @Throws(IOException::class)
                override fun getSize(): Long {
                    return `is`.available().toLong()
                }

                @Throws(IOException::class)
                override fun close() {
                    `is`.close()
                }
            })
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun prepare() {
        if (player == null) return
        if (mStatus == STATUS_IDLE || mStatus == STATUS_STOPPED) {
            player!!.prepareAsync()
            mStatus = STATUS_PREPARING
        }
    }

    fun stop() {
        if (player == null) return
        if (mStatus == STATUS_STARTED || mStatus == STATUS_PAUSED) {
            player!!.stop()
            mStatus = STATUS_STOPPED
        }
    }

    fun pause() {
        if (player == null) return
        if (player!!.isPlaying && mStatus == STATUS_STARTED) {
            player!!.pause()
            mStatus = STATUS_PAUSED
        }
    }

    private fun start() {
        if (player == null) return
        if (mStatus == STATUS_PREPARED || mStatus == STATUS_PAUSED) {
            player!!.start()
            mStatus = STATUS_STARTED
        }

    }

    fun setPreparedListener(mPreparedListener: IMediaPlayer.OnPreparedListener) {
        this.mPreparedListener = mPreparedListener
    }

    override fun onPrepared(mp: IMediaPlayer) {
        mStatus = STATUS_PREPARED
        start()
        if (mPreparedListener != null) mPreparedListener!!.onPrepared(mp)
    }

    fun resume() {
        start()
    }

    fun destroy() {
        stop()
        if (player != null) {
            player!!.setSurface(null)
            player!!.release()
        }
        player = null
    }

    companion object {
        private val STATUS_IDLE = 0
        private val STATUS_PREPARING = 1
        private val STATUS_PREPARED = 2
        private val STATUS_STARTED = 3
        private val STATUS_PAUSED = 4
        private val STATUS_STOPPED = 5
    }
}
