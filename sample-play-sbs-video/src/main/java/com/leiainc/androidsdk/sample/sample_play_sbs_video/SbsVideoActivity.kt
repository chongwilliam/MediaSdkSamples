package com.leiainc.androidsdk.sample.sample_play_sbs_video

import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Surface
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.leiainc.androidsdk.core.QuadView
import com.leiainc.androidsdk.core.SurfaceTextureReadyCallback
import com.leiainc.androidsdk.display.LeiaDisplayManager
import com.leiainc.androidsdk.display.LeiaSDK
import com.leiainc.androidsdk.sbs.video.SbsVideoSurfaceRenderer
import com.leiainc.androidsdk.sbs.video.TextureShape

class SbsVideoActivity : AppCompatActivity(), SurfaceTextureReadyCallback {

    private lateinit var exoPlayer: SimpleExoPlayer
    var mSbsVideoSurfaceRenderer: SbsVideoSurfaceRenderer?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sbs_video)
        val quadView: QuadView = findViewById(R.id.quad_view)
        exoPlayer = SimpleExoPlayer.Builder(this).build()
        quadView.getInputSurfaceTexture(this)
    }

    override fun onSurfaceTextureReady(surfaceTexture: SurfaceTexture?) {
        if (mSbsVideoSurfaceRenderer == null) {
            mSbsVideoSurfaceRenderer = SbsVideoSurfaceRenderer(
                this,
                Surface(surfaceTexture),
                TextureShape.LANDSCAPE,
            ) {
                configureExoplayer(it)
            }
        }
    }

    private fun configureExoplayer(surfaceTexture: SurfaceTexture) {
        // Note: This is appropriate for 2x2 textures on Hydrogen One.
        exoPlayer.setVideoSurface(Surface(surfaceTexture))
        val userAgent = Util.getUserAgent(this, "exoplayer2example")
        val uri = Uri.parse(
//            "https://dev.streaming.leialoft.com/out/v1/08cd49f09fbc4a1e9c063424fa0bfc00/7845cda1bdd5494db13a24f5d13374ea/adacb4edf0434177ae441f124d989fe7/index.mpd"
        "http://localhost:5000/o1k.mp4"
        )
        val dataSourceFactory: DataSource.Factory = DefaultHttpDataSourceFactory(userAgent)
//        val videoSource: MediaSource =
//            DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
//        val videoSource: MediaSource =
//            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
        val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))
//        val loopingSource = LoopingMediaSource(videoSource)
        val loopingSource = videoSource
        exoPlayer.prepare(loopingSource)
    }

    private fun updateSource() {
        val userAgent = Util.getUserAgent(this, "exoplayer2example")
        val uri = Uri.parse(
//            "https://dev.streaming.leialoft.com/out/v1/08cd49f09fbc4a1e9c063424fa0bfc00/7845cda1bdd5494db13a24f5d13374ea/adacb4edf0434177ae441f124d989fe7/index.mpd"
            "http://localhost:5000/o1k.mp4"
        )
        val dataSourceFactory: DataSource.Factory = DefaultHttpDataSourceFactory(userAgent)
//        val videoSource: MediaSource =
//            DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
//        val videoSource: MediaSource =
//            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
        val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))
//        val loopingSource = LoopingMediaSource(videoSource)
        val loopingSource = videoSource
        exoPlayer.prepare(loopingSource)

    }

    override fun onPause() {
        super.onPause()
        val displayManager: LeiaDisplayManager? = LeiaSDK.getDisplayManager(applicationContext)
        displayManager?.requestBacklightMode(LeiaDisplayManager.BacklightMode.MODE_2D)
        exoPlayer.playWhenReady = false
    }

    override fun onResume() {
        super.onResume()
        val displayManager: LeiaDisplayManager? = LeiaSDK.getDisplayManager(applicationContext)
        displayManager?.requestBacklightMode(LeiaDisplayManager.BacklightMode.MODE_3D)

        /*  Make app full screen */
        setFullScreenImmersive()
        exoPlayer.playWhenReady = true

        /* Run configuration again after mp4 duration, and restart */
        val handler = Handler()
        handler.postDelayed({
            updateSource()
            onResume()
        }, 70000)  // adjust this value based on stereo implementation (currently at 1 min), additional 10 seconds for write buffer

    }

    override fun onStop() {
        super.onStop()
        mSbsVideoSurfaceRenderer?.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
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
}