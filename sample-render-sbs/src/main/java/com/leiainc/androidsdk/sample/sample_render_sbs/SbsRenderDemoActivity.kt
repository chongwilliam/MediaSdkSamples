package com.leiainc.androidsdk.sample.sample_render_sbs

import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.leiainc.androidsdk.core.QuadView
import com.leiainc.androidsdk.core.ScaleType
import com.leiainc.androidsdk.display.LeiaDisplayManager
import com.leiainc.androidsdk.display.LeiaSDK
import com.leiainc.androidsdk.photoformat.MultiviewImageDecoder
import com.leiainc.androidsdk.sample.sample_render_sbs.utils.DiskUtils
import com.leiainc.androidsdk.sbs.MultiviewSynthesizer2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SbsRenderDemoActivity : AppCompatActivity() {

    private val sbsRenderViewModel by viewModels<SbsRenderViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sbs_render_demo)

        /*  Get reference to QuadView */
        val quadView: QuadView = findViewById(R.id.quad_view)

        /*  Set scale type to Fit center  */
        quadView.scaleType = ScaleType.FIT_CENTER

        val quadBitmapObserver = Observer<Bitmap> { quadBitmap ->
            // Observe LiveData to update UI when Quad Bitmap is available.
            if (quadBitmap != null) {
                quadView.setQuadBitmap(quadBitmap)
            } else {
                Toast.makeText(this, "Failed to retrieve Image", Toast.LENGTH_LONG).show()
            }
        }

        sbsRenderViewModel.quadBitmapLiveData.observe(this, quadBitmapObserver)
    }

    override fun onPause() {
        super.onPause()
        val displayManager: LeiaDisplayManager? = LeiaSDK.getDisplayManager(applicationContext)
        displayManager?.requestBacklightMode(LeiaDisplayManager.BacklightMode.MODE_2D)
    }

    override fun onResume() {
        super.onResume()
        val displayManager: LeiaDisplayManager? = LeiaSDK.getDisplayManager(applicationContext)
        displayManager?.requestBacklightMode(LeiaDisplayManager.BacklightMode.MODE_3D)

        Toast.makeText(this, "On Resume", Toast.LENGTH_LONG).show()

        /*  Make app full screen */
        setFullScreenImmersive()

        /* Continuously get live data */
        val handler = Handler()
        handler.postDelayed({
//            while(true) {
                sbsRenderViewModel.setImageLiveData()  // single transfer on background task
//                Thread.sleep(1000)
//                handler.postDelayed({}, 1000)
                onResume()
        }, 1000)
    }

    private fun setFullScreenImmersive() {
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        val decorView = window.decorView
        decorView.clearFocus()
        decorView.systemUiVisibility = flags

        // Code below is to handle presses of Volume up or Volume down.
        // Without this, after pressing volume buttons, the navigation bar will
        // show up and won't hide
        decorView.setOnSystemUiVisibilityChangeListener { visibility: Int ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                decorView.systemUiVisibility = flags
            }
        }
    }

    private fun restartActivity(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 11) {
            activity.recreate()
        } else {
            activity.finish()
            activity.startActivity(activity.intent)
        }
    }

    private fun restartThis() {
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

//    private fun updateImage() {
////        val context = app.applicationContext
//
//        val context = getApplicationContext()
//
//        /* This function searches for the Uri of the file name stored on the internal storage as 'farm-lif.jpg'. */
//        val fileUri = DiskUtils.saveResourceToFile(context, R.raw.bike_2x1)
//        if (fileUri != null) {
//            val multiviewImage = MultiviewImageDecoder.getDefault().decode(context, fileUri, 1280 * 720)
//            /*  Decoder returns null if */
//            if (multiviewImage != null) {
//                val synthesizer2 = MultiviewSynthesizer2.createMultiviewSynthesizer(context)
//                synthesizer2.populateDisparityMaps(multiviewImage)
//
//                val quadBitmap = synthesizer2.toQuadBitmap(multiviewImage)
//                sbsRenderViewModel.quadBitmapLiveData.postValue(quadBitmap)
//            }
//        }
//    }



}

