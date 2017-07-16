package com.asha.md360player4android

import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.SparseArray
import android.view.View
import android.widget.EditText
import android.widget.Toast

/**
 * Created by hzqiujiadi on 16/1/26.
 * hzqiujiadi ashqalcn@gmail.com
 */
class DemoActivity : AppCompatActivity() {

    //public static final String sPath = "file:////storage/sdcard1/vr/";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        val et = findViewById(R.id.edit_text_url) as EditText

        val data = SparseArray<String>()

        data.put(data.size(), getDrawableUri(R.drawable.bitmap360).toString())
        data.put(data.size(), getDrawableUri(R.drawable.texture).toString())
        data.put(data.size(), getDrawableUri(R.drawable.dome_pic).toString())
        data.put(data.size(), getDrawableUri(R.drawable.stereo).toString())
        data.put(data.size(), getDrawableUri(R.drawable.multifisheye).toString())
        data.put(data.size(), getDrawableUri(R.drawable.multifisheye2).toString())
        data.put(data.size(), getDrawableUri(R.drawable.fish2sphere180sx2).toString())
        data.put(data.size(), getDrawableUri(R.drawable.fish2sphere180s).toString())

        data.put(data.size(), "rtsp://218.204.223.237:554/live/1/66251FC11353191F/e7ooqwcfbqjoo80j.sdp")
        data.put(data.size(), "http://sh.cdn.eihoo.com/video/yiyuan_krpano/video6.mp4")
        data.put(data.size(), sPath + "ch0_160701145544.ts")
        data.put(data.size(), sPath + "videos_s_4.mp4")
        data.put(data.size(), sPath + "28.mp4")
        data.put(data.size(), sPath + "haha.mp4")
        data.put(data.size(), sPath + "halfdome.mp4")
        data.put(data.size(), sPath + "dome.mp4")
        data.put(data.size(), sPath + "stereo.mp4")
        data.put(data.size(), sPath + "look25fps3M.mp4")
        data.put(data.size(), "http://10.240.131.39/vr/570624aae1c52.mp4")
        data.put(data.size(), "http://192.168.5.106/vr/570624aae1c52.mp4")
        data.put(data.size(), sPath + "video_31b451b7ca49710719b19d22e19d9e60.mp4")

        data.put(data.size(), "http://cache.utovr.com/201508270528174780.m3u8")
        data.put(data.size(), sPath + "AGSK6416.jpg")
        data.put(data.size(), sPath + "IJUN2902.jpg")
        data.put(data.size(), sPath + "SUYZ2954.jpg")
        data.put(data.size(), sPath + "TEJD0097.jpg")
        data.put(data.size(), sPath + "WSGV6301.jpg")

        SpinnerHelper.with(this)
                .setData(data)
                .setClickHandler (object:SpinnerHelper.ClickHandler{ override fun onSpinnerClicked(index: Int, key: Int, value: String) {
                    et.setText(value)
                  }
                })
                .init(R.id.spinner_url)

        findViewById(R.id.video_button).setOnClickListener(View.OnClickListener {
            val url = et.text.toString()
            if (!TextUtils.isEmpty(url)) {
                MD360PlayerActivity.startVideo(this@DemoActivity, Uri.parse(url))
            } else {
                Toast.makeText(this@DemoActivity, "empty url!", Toast.LENGTH_SHORT).show()
            }
        })

        findViewById(R.id.bitmap_button).setOnClickListener(View.OnClickListener {
            val url = et.text.toString()
            if (!TextUtils.isEmpty(url)) {
                MD360PlayerActivity.startBitmap(this@DemoActivity, Uri.parse(url))
            } else {
                Toast.makeText(this@DemoActivity, "empty url!", Toast.LENGTH_SHORT).show()
            }
        })

        findViewById(R.id.ijk_button).setOnClickListener(View.OnClickListener {
            val url = et.text.toString()
            if (!TextUtils.isEmpty(url)) {
                IjkPlayerDemoActivity.start(this@DemoActivity, Uri.parse(url))
            } else {
                Toast.makeText(this@DemoActivity, "empty url!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getDrawableUri(@DrawableRes resId: Int): Uri {
        val resources = resources
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId) + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId))
    }

    companion object {

        val sPath = "file:///mnt/sdcard/vr/"
    }
}
