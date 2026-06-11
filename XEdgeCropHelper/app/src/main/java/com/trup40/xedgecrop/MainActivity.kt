package com.trup40.xedgecrop

import android.app.Activity
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import java.io.File

class MainActivity : Activity() {

    private lateinit var cropView: CropView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Add our custom View directly to the screen without an XML layout
        cropView = CropView(this)
        setContentView(cropView, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))

        // Triggered when the user finishes the selection
        cropView.onSelectionFinished = { rect ->
            // 1. Hide our black layer and border from the screen
            cropView.visibility = View.GONE

            // 2. Wait 200ms for the screen to return to its original state, then start capturing
            Handler(Looper.getMainLooper()).postDelayed({
                processScreenshot(rect)
            }, 200)
        }
    }

    private fun processScreenshot(rect: RectF) {
        // 1. Create the file directly in the app's permitted cache directory
        val tempFile = File(externalCacheDir, "temp_xedge.png")
        val tempPath = tempFile.absolutePath

        Thread {
            try {
                // 2. Run the screencap command and immediately make it readable with chmod 666
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "screencap -p $tempPath && chmod 666 $tempPath"))
                process.waitFor()

                if (!tempFile.exists()) {
                    runOnUiThread { Toast.makeText(this, "Capture failed!", Toast.LENGTH_SHORT).show(); finish() }
                    return@Thread
                }

                // 3. Load the image into memory
                val originalBitmap = BitmapFactory.decodeFile(tempPath)
                if (originalBitmap == null) {
                    runOnUiThread { Toast.makeText(this, "Failed to read file (Permission error)!", Toast.LENGTH_SHORT).show(); finish() }
                    return@Thread
                }

                // Secure the crop bounds against overflow
                val x = maxOf(0, rect.left.toInt())
                val y = maxOf(0, rect.top.toInt())
                val width = minOf(originalBitmap.width - x, rect.width().toInt())
                val height = minOf(originalBitmap.height - y, rect.height().toInt())

                // 4. Perform the crop
                val croppedBitmap = Bitmap.createBitmap(originalBitmap, x, y, width, height)

                // Save to gallery
                saveToGallery(croppedBitmap)

                // 5. Clear memory and the temporary file
                tempFile.delete()
                originalBitmap.recycle()
                croppedBitmap.recycle()

                runOnUiThread {
                    Toast.makeText(this, "Partial screenshot saved", Toast.LENGTH_SHORT).show()
                    finish()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }.start()
    }

    private fun saveToGallery(bitmap: Bitmap) {
        val filename = "Screenshot_${System.currentTimeMillis()}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            // The cleanest and standard saving method for Android 14+
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Screenshots")
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Close the app if the user presses the home button etc. without making a selection, so it doesn't hang in the background
        if (!isFinishing) finish()
    }
}