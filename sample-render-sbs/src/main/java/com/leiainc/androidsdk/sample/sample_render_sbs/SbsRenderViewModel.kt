package com.leiainc.androidsdk.sample.sample_render_sbs

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.*
import com.leiainc.androidsdk.photoformat.IOUtils
import com.leiainc.androidsdk.photoformat.MultiviewImageDecoder
import com.leiainc.androidsdk.sample.sample_render_sbs.utils.DiskUtils
import com.leiainc.androidsdk.sbs.MultiviewSynthesizer2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class SbsRenderViewModel(private val app: Application): AndroidViewModel(app) {

    val quadBitmapLiveData : MutableLiveData<Bitmap> by lazy {
        MutableLiveData<Bitmap>()
    }

    init {
        loadLifImageOnDisk()
    }

    private fun download(link: String, path: String) {
        URL(link).openStream().use { input ->
            FileOutputStream(File(path)).use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun loadLifImageOnDisk() = viewModelScope.launch(Dispatchers.IO) {
        val context = app.applicationContext

        /* This function searches for the Uri of the file name stored on the internal storage as 'farm-lif.jpg'. */
//        val fileUri = DiskUtils.saveResourceToFile(context, R.raw.o1k_2x1)
//        val fileUri = Uri.fromFile(File("http://localhost:5000/o1k.jpg"));
//        val fileUri = Uri.parse("http://localhost:5000/o1k_2x1.jpg")
//        val fileUri = Uri.parse("file:///home/william/py-flask-video-stream/images/o1k_2x1.jpg")
        // download image and save to internal storage
        download("http://localhost:5000/o1k_2x1.jpg", context.externalCacheDir.toString() + "/o1k_2x1.jpg")
        // get fileUri
        val testFile = File(context.externalCacheDir, "o1k_2x1.jpg")
        val fileUri = Uri.fromFile(testFile)
        if (fileUri != null) {
            val multiviewImage = MultiviewImageDecoder.getDefault().decode(context, fileUri, 1280 * 720)
            /*  Decoder returns null if */
            if (multiviewImage != null) {
                val synthesizer2 = MultiviewSynthesizer2.createMultiviewSynthesizer(context)
                synthesizer2.populateDisparityMaps(multiviewImage)

                val quadBitmap = synthesizer2.toQuadBitmap(multiviewImage)
                quadBitmapLiveData.postValue(quadBitmap)
                return@launch
            }
        }

        quadBitmapLiveData.postValue(null)
    }

    fun setImageLiveData() = viewModelScope.launch(Dispatchers.IO) {
        val context = app.applicationContext

        /* This function searches for the Uri of the file name stored on the internal storage as 'farm-lif.jpg'. */
//        val fileUri = DiskUtils.saveResourceToFile(context, R.raw.test_2x1)
//        val fileUri = Uri.parse("http://localhost:5000/o1k_2x1.jpg")
        // download image and save to internal storage
        download("http://localhost:5000/o1k_2x1.jpg", context.externalCacheDir.toString() + "/o1k_2x1.jpg")
        // get fileUri
        val testFile = File(context.externalCacheDir, "o1k_2x1.jpg")
        val fileUri = Uri.fromFile(testFile)
        if (fileUri != null) {
            val multiviewImage = MultiviewImageDecoder.getDefault().decode(context, fileUri, 1280 * 720)
            /*  Decoder returns null if */
            if (multiviewImage != null) {
                val synthesizer2 = MultiviewSynthesizer2.createMultiviewSynthesizer(context)
                synthesizer2.populateDisparityMaps(multiviewImage)

                val quadBitmap = synthesizer2.toQuadBitmap(multiviewImage)
                quadBitmapLiveData.postValue(quadBitmap)
//                return@launch
            }
        }

//        quadBitmapLiveData.postValue(null)
    }

}
