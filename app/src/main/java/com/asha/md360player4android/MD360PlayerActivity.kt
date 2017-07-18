package com.asha.md360player4android

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.util.SimpleArrayMap
import android.util.SparseArray
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast

import com.asha.vrlib.MDDirectorCamUpdate
import com.asha.vrlib.MDVRLibrary
import com.asha.vrlib.model.MDHotspotBuilder
import com.asha.vrlib.model.MDPosition
import com.asha.vrlib.model.MDRay
import com.asha.vrlib.model.MDViewBuilder
import com.asha.vrlib.model.position.MDMutablePosition
import com.asha.vrlib.plugins.MDAbsPlugin
import com.asha.vrlib.plugins.MDWidgetPlugin
import com.asha.vrlib.plugins.hotspot.IMDHotspot
import com.asha.vrlib.plugins.hotspot.MDAbsHotspot
import com.asha.vrlib.plugins.hotspot.MDAbsView
import com.asha.vrlib.plugins.hotspot.MDSimpleHotspot
import com.asha.vrlib.plugins.hotspot.MDView
import com.asha.vrlib.texture.MD360BitmapTexture
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

import java.io.FileNotFoundException
import java.util.LinkedList
import java.util.Locale

import android.animation.PropertyValuesHolder.ofFloat
import com.squareup.picasso.MemoryPolicy.NO_CACHE
import com.squareup.picasso.MemoryPolicy.NO_STORE

/**
 * using MD360Renderer

 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
abstract class MD360PlayerActivity : Activity() {

    var vrLibrary: MDVRLibrary? = null
        private set

    // load resource from android drawable and remote url.
    private val mImageLoadProvider = ImageLoadProvider()

    // load resource from android drawable only.
    private val mAndroidProvider = AndroidProvider(this)

    private val plugins = LinkedList<MDAbsPlugin>()

    private val logoPosition = MDMutablePosition.newInstance().setY(-8.0f).setYaw(-90.0f)

    private val positions = arrayOf<MDPosition>(MDPosition.newInstance().setZ(-8.0f).setYaw(-45.0f), MDPosition.newInstance().setZ(-18.0f).setYaw(15.0f).setAngleX(15f), MDPosition.newInstance().setZ(-10.0f).setYaw(-10.0f).setAngleX(-15f), MDPosition.newInstance().setZ(-10.0f).setYaw(30.0f).setAngleX(30f), MDPosition.newInstance().setZ(-10.0f).setYaw(-30.0f).setAngleX(-30f), MDPosition.newInstance().setZ(-5.0f).setYaw(30.0f).setAngleX(60f), MDPosition.newInstance().setZ(-3.0f).setYaw(15.0f).setAngleX(-45f), MDPosition.newInstance().setZ(-3.0f).setYaw(15.0f).setAngleX(-45f).setAngleY(45f), MDPosition.newInstance().setZ(-3.0f).setYaw(0.0f).setAngleX(90f))

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // full screen
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // set content view
        setContentView(R.layout.activity_md_using_surface_view)

        // init VR Library
        vrLibrary = createVRLibrary()

        val activity = this

        val hotspotPoints = LinkedList<View>()
        hotspotPoints.add(findViewById(R.id.hotspot_point1))
        hotspotPoints.add(findViewById(R.id.hotspot_point2))

        SpinnerHelper.with(this)
                .setData(sDisplayMode)
                .setDefault(vrLibrary!!.displayMode)
                .setClickHandler (object:SpinnerHelper.ClickHandler{ override fun onSpinnerClicked(index: Int, key: Int, value: String) {
                    vrLibrary!!.switchDisplayMode(this@MD360PlayerActivity, key)
                    var i = 0
                    val size = if (key == MDVRLibrary.DISPLAY_MODE_GLASS) 2 else 1
                    for (point in hotspotPoints) {
                        point.visibility = if (i < size) View.VISIBLE else View.GONE
                        i++
                    }
                }})
                .init(R.id.spinner_display)

        SpinnerHelper.with(this)
                .setData(sInteractiveMode)
                .setDefault(vrLibrary!!.interactiveMode)
                .setClickHandler (object:SpinnerHelper.ClickHandler{ override fun onSpinnerClicked(index: Int, key: Int, value: String) {
                        vrLibrary!!.switchInteractiveMode(this@MD360PlayerActivity, key)
                    } }).init(R.id.spinner_interactive)

        SpinnerHelper.with(this)
                .setData(sProjectionMode)
                .setDefault(vrLibrary!!.projectionMode)
                .setClickHandler (object:SpinnerHelper.ClickHandler{ override fun onSpinnerClicked(index: Int, key: Int, value: String) {
                        vrLibrary!!.switchProjectionMode(this@MD360PlayerActivity, key)
                    } })
                .init(R.id.spinner_projection)

        SpinnerHelper.with(this)
                .setData(sAntiDistortion)
                .setDefault(if (vrLibrary!!.isAntiDistortionEnabled) 1 else 0)
                .setClickHandler (object:SpinnerHelper.ClickHandler{ override fun onSpinnerClicked(index: Int, key: Int, value: String) {
                    vrLibrary!!.isAntiDistortionEnabled = key != 0
                }})
                .init(R.id.spinner_distortion)

        findViewById(R.id.button_add_plugin).setOnClickListener {
            val index = (Math.random() * 100).toInt() % positions.size
            val position = positions[index]
            val builder = MDHotspotBuilder.create(mImageLoadProvider)
                    .size(4f, 4f)
                    .provider(0, activity, android.R.drawable.star_off)
                    .provider(1, activity, android.R.drawable.star_on)
                    .provider(10, activity, android.R.drawable.checkbox_off_background)
                    .provider(11, activity, android.R.drawable.checkbox_on_background)
                    .listenClick { hitHotspot, ray ->
                        if (hitHotspot is MDWidgetPlugin) {
                            val widgetPlugin = hitHotspot
                            widgetPlugin.checked = !widgetPlugin.checked
                        }
                    }
                    .title("star" + index)
                    .position(position)
                    .status(0, 1)
                    .checkedStatus(10, 11)

            val plugin = MDWidgetPlugin(builder)

            plugins.add(plugin)
            vrLibrary!!.addPlugin(plugin)
            Toast.makeText(this@MD360PlayerActivity, "add plugin position:" + position, Toast.LENGTH_SHORT).show()
        }

//        findViewById(R.id.button_add_plugin_logo).setOnClickListener {
//            val builder = MDHotspotBuilder.create(mImageLoadProvider)
//                    .size(4f, 4f)
//                    .provider(activity, R.drawable.moredoo_logo)
//                    .title("logo")
//                    .position(logoPosition)
//                    .listenClick { hitHotspot, ray -> Toast.makeText(this@MD360PlayerActivity, "click logo", Toast.LENGTH_SHORT).show() }
//            val hotspot = MDSimpleHotspot(builder)
//            plugins.add(hotspot)
//            vrLibrary!!.addPlugin(hotspot)
//            Toast.makeText(this@MD360PlayerActivity, "add plugin logo", Toast.LENGTH_SHORT).show()
//        }

        findViewById(R.id.button_remove_plugin).setOnClickListener {
            if (plugins.size > 0) {
                val plugin = plugins.removeAt(plugins.size - 1)
                vrLibrary!!.removePlugin(plugin)
            }
        }

        findViewById(R.id.button_remove_plugins).setOnClickListener {
            plugins.clear()
            vrLibrary!!.removePlugins()
        }

//        findViewById(R.id.button_add_hotspot_front).setOnClickListener {
//            val builder = MDHotspotBuilder.create(mImageLoadProvider)
//                    .size(4f, 4f)
//                    .provider(activity, R.drawable.moredoo_logo)
//                    .title("front logo")
//                    .tag("tag-front")
//                    .position(MDPosition.newInstance().setZ(-12.0f).setY(-1.0f))
//            val hotspot = MDSimpleHotspot(builder)
//            hotspot.rotateToCamera()
//            plugins.add(hotspot)
//            vrLibrary!!.addPlugin(hotspot)
//        }

        findViewById(R.id.button_rotate_to_camera_plugin).setOnClickListener {
            val hotspot = vrLibrary!!.findHotspotByTag("tag-front")
            hotspot?.rotateToCamera()
        }

        findViewById(R.id.button_add_md_view).setOnClickListener {
            val textView = TextView(activity)
            textView.setBackgroundColor(0x55FFCC11)
            textView.text = "Hello world."

            val builder = MDViewBuilder.create()
                    .provider(textView, 400/*view width*/, 100/*view height*/)
                    .size(4f, 1f)
                    .position(MDPosition.newInstance().setZ(-12.0f))
                    .title("md view")
                    .tag("tag-md-text-view")

            val mdView = MDView(builder)
            plugins.add(mdView)
            vrLibrary!!.addPlugin(mdView)
        }

        findViewById(R.id.button_update_md_view).setOnClickListener {
            val mdView = vrLibrary!!.findViewByTag("tag-md-text-view")
            if (mdView != null) {
                val textView = mdView.castAttachedView<TextView>(TextView::class.java)
                textView.text = "Cheer up!"
                textView.setBackgroundColor(0x8800FF00.toInt())
                mdView.invalidate()
            }
        }

        findViewById(R.id.button_md_view_hover).setOnClickListener {
            val view = HoverView(activity)
            view.setBackgroundColor(0x55FFCC11)

            val builder = MDViewBuilder.create()
                    .provider(view, 300/*view width*/, 200/*view height*/)
                    .size(3f, 2f)
                    .position(MDPosition.newInstance().setZ(-8.0f))
                    .title("md view")
                    .tag("tag-md-text-view")

            val mdView = MDView(builder)
            mdView.rotateToCamera()
            plugins.add(mdView)
            vrLibrary!!.addPlugin(mdView)
        }

        val hotspotText = findViewById(R.id.hotspot_text) as TextView
        val directorBriefText = findViewById(R.id.director_brief_text) as TextView
        vrLibrary!!.setEyePickChangedListener(MDVRLibrary.IEyePickListener { hotspot, hitTimestamp ->
            val text = if (hotspot == null) "nop" else String.format(Locale.CHINESE, "%s  %fs", hotspot.title, (System.currentTimeMillis() - hitTimestamp) / 1000.0f)
            hotspotText.text = text

            val brief = vrLibrary!!.getDirectorBrief().toString()
            directorBriefText.text = brief

            if (System.currentTimeMillis() - hitTimestamp > 5000) {
                vrLibrary!!.resetEyePick()
            }
        })

        findViewById(R.id.button_camera_little_planet).setOnClickListener {
            val cameraUpdate = vrLibrary!!.updateCamera()
            val near = ofFloat("near", cameraUpdate.nearScale, -0.5f)
            val eyeZ = PropertyValuesHolder.ofFloat("eyeZ", cameraUpdate.eyeZ, 18f)
            val pitch = PropertyValuesHolder.ofFloat("pitch", cameraUpdate.pitch, 90f)
            val yaw = PropertyValuesHolder.ofFloat("yaw", cameraUpdate.yaw, 90f)
            val roll = PropertyValuesHolder.ofFloat("roll", cameraUpdate.roll, 0f)
            startCameraAnimation(cameraUpdate, near, eyeZ, pitch, yaw, roll)
        }

        findViewById(R.id.button_camera_to_normal).setOnClickListener {
            val cameraUpdate = vrLibrary!!.updateCamera()
            val near = ofFloat("near", cameraUpdate.nearScale, 0f)
            val eyeZ = PropertyValuesHolder.ofFloat("eyeZ", cameraUpdate.eyeZ, 0f)
            val pitch = PropertyValuesHolder.ofFloat("pitch", cameraUpdate.pitch, 0f)
            val yaw = PropertyValuesHolder.ofFloat("yaw", cameraUpdate.yaw, 0f)
            val roll = PropertyValuesHolder.ofFloat("roll", cameraUpdate.roll, 0f)
            startCameraAnimation(cameraUpdate, near, eyeZ, pitch, yaw, roll)
        }

        SpinnerHelper.with(this)
                .setData(sPitchFilter)
                .setDefault(0)
                .setClickHandler (object:SpinnerHelper.ClickHandler{ override fun onSpinnerClicked(index: Int, key: Int, value: String) {
                    val filter = if (key == 0)
                        null
                    else
                        object : MDVRLibrary.DirectorFilterAdatper() {
                            override fun onFilterPitch(input: Float): Float {
                                if (input > 70) {
                                    return 70f
                                }

                                if (input < -70) {
                                    return -70f
                                }

                                return input
                            }
                        }

                    vrLibrary!!.setDirectorFilter(filter)
                }})
                .init(R.id.spinner_pitch_filter)

        SpinnerHelper.with(this)
                .setData(sFlingEnabled)
                .setDefault(if (vrLibrary!!.isFlingEnabled()) 1 else 0)
                .setClickHandler (object:SpinnerHelper.ClickHandler{ override fun onSpinnerClicked(index: Int, key: Int, value: String) {
                    vrLibrary!!.setFlingEnabled(key == 1)
                } })
                .init(R.id.spinner_fling_enable)
    }


    private var animator: ValueAnimator? = null

    private fun startCameraAnimation(cameraUpdate: MDDirectorCamUpdate, vararg values: PropertyValuesHolder) {
        if (animator != null) {
            animator!!.cancel()
        }

        animator = ValueAnimator.ofPropertyValuesHolder(*values).setDuration(2000)
        animator!!.addUpdateListener { animation ->
            val near = animation.getAnimatedValue("near") as Float
            val eyeZ = animation.getAnimatedValue("eyeZ") as Float
            val pitch = animation.getAnimatedValue("pitch") as Float
            val yaw = animation.getAnimatedValue("yaw") as Float
            val roll = animation.getAnimatedValue("roll") as Float
            cameraUpdate.setEyeZ(eyeZ).setNearScale(near).setPitch(pitch).setYaw(yaw).roll = roll
        }
        animator!!.start()
    }

    protected abstract fun createVRLibrary(): MDVRLibrary

    override fun onResume() {
        super.onResume()
        vrLibrary!!.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        vrLibrary!!.onPause(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        vrLibrary!!.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        vrLibrary!!.onOrientationChanged(this)
    }

    protected val uri: Uri?
        get() {
            val i = intent
            if (i == null || i.data == null) {
                return null
            }
            return i.data
        }

    fun cancelBusy() {
        findViewById(R.id.progress).visibility = View.GONE
    }

    fun busy() {
        findViewById(R.id.progress).visibility = View.VISIBLE
    }

    // android impl
    private inner class AndroidProvider(internal var activity: Activity) : MDVRLibrary.IImageLoadProvider {

        override fun onProvideBitmap(uri: Uri, callback: MD360BitmapTexture.Callback) {
            try {
                val bitmap = BitmapFactory.decodeStream(activity.contentResolver.openInputStream(uri))
                callback.texture(bitmap)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

        }
    }

    // picasso impl
    private inner class ImageLoadProvider : MDVRLibrary.IImageLoadProvider {

        private val targetMap = SimpleArrayMap<Uri, Target>()

        override fun onProvideBitmap(uri: Uri, callback: MD360BitmapTexture.Callback) {

            val target = object : Target {

                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                    // texture
                    callback.texture(bitmap)
                    targetMap.remove(uri)
                }

                override fun onBitmapFailed(errorDrawable: Drawable) {
                    targetMap.remove(uri)
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable) {

                }
            }
            targetMap.put(uri, target)
            Picasso.with(applicationContext).load(uri).resize(callback.maxTextureSize, callback.maxTextureSize).onlyScaleDown().centerInside().memoryPolicy(NO_CACHE, NO_STORE).into(target)
        }
    }

    companion object {

        private val TAG = "XiuhuVrPlayer"

        private val sDisplayMode = SparseArray<String>()
        private val sInteractiveMode = SparseArray<String>()
        private val sProjectionMode = SparseArray<String>()
        private val sAntiDistortion = SparseArray<String>()
        private val sPitchFilter = SparseArray<String>()
        private val sFlingEnabled = SparseArray<String>()

        init {
            sDisplayMode.put(MDVRLibrary.DISPLAY_MODE_NORMAL, "NORMAL")
            sDisplayMode.put(MDVRLibrary.DISPLAY_MODE_GLASS, "GLASS")

            sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_MOTION, "MOTION")
            sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_TOUCH, "TOUCH")
            sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH, "M & T")
            sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION, "CARDBOARD M")
            sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION_WITH_TOUCH, "CARDBOARD M&T")

            sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_SPHERE, "SPHERE")
            sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME180, "DOME 180")
            sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME230, "DOME 230")
            sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME180_UPPER, "DOME 180 UPPER")
            sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME230_UPPER, "DOME 230 UPPER")
            sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE_HORIZONTAL, "STEREO H SPHERE")
            sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE_VERTICAL, "STEREO V SPHERE")
            sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_PLANE_FIT, "PLANE FIT")
            sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_PLANE_CROP, "PLANE CROP")
            sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_PLANE_FULL, "PLANE FULL")
            sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_MULTI_FISH_EYE_HORIZONTAL, "MULTI FISH EYE HORIZONTAL")
            sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_MULTI_FISH_EYE_VERTICAL, "MULTI FISH EYE VERTICAL")
            sProjectionMode.put(CustomProjectionFactory.CUSTOM_PROJECTION_FISH_EYE_RADIUS_VERTICAL, "CUSTOM MULTI FISH EYE")

            sAntiDistortion.put(1, "ANTI-ENABLE")
            sAntiDistortion.put(0, "ANTI-DISABLE")

            sPitchFilter.put(1, "FILTER PITCH")
            sPitchFilter.put(0, "FILTER NOP")

            sFlingEnabled.put(1, "FLING ENABLED")
            sFlingEnabled.put(0, "FLING DISABLED")
        }

        fun startNormalVideo(context: Context, uri: Uri) {
            start(context, uri, NormalVideoPlayerActivity::class.java)
        }

        fun startVideo(context: Context, uri: Uri) {
            start(context, uri, VideoPlayerActivity::class.java)
        }

        fun startBitmap(context: Context, uri: Uri) {
            start(context, uri, BitmapPlayerActivity::class.java)
        }

        private fun start(context: Context, uri: Uri, clz: Class<out Activity>) {
            val i = Intent(context, clz)
            i.data = uri
            context.startActivity(i)
        }
    }
}