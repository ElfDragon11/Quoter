package com.example.quoter

import android.content.BroadcastReceiver // Add back receiver imports
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import com.example.quoter.data.AppDatabase
import com.example.quoter.data.GeneratedImage
import com.example.quoter.data.QuoteRepository
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException

class QuoteWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return QuoteEngine()
    }

    private inner class QuoteEngine : Engine() {

        private val handler = Handler(Looper.getMainLooper())
        private val drawRunner = Runnable { drawFrame() }
        private var visible = false
        private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        // Variables for image handling
        private lateinit var repository: QuoteRepository
        private var selectedImages: List<GeneratedImage> = emptyList()
        private var currentImageIndex = -1 // Start at -1 so first visible load gets index 0
        private var currentBitmap: Bitmap? = null
        private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)

        // Receiver for SCREEN_OFF event (now triggers the change)
        private val screenOffReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                    Log.d("QuoteEngine", "Screen OFF received, changing and drawing next image.")
                    changeToNextImageAndDraw()
                }
            }
        }
        private var receiversRegistered = false

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            repository = QuoteRepository(applicationContext)
            loadInitialImageList()
            // Register only screen off receiver
            if (!receiversRegistered) {
                applicationContext.registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
                receiversRegistered = true
                Log.d("QuoteEngine", "ScreenOffReceiver registered.")
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            handler.removeCallbacks(drawRunner)
            engineScope.cancel()
            // Unregister screen off receiver
            if (receiversRegistered) {
                try {
                    applicationContext.unregisterReceiver(screenOffReceiver)
                    receiversRegistered = false
                    Log.d("QuoteEngine", "ScreenOffReceiver unregistered.")
                } catch (e: IllegalArgumentException) {
                    Log.w("QuoteEngine", "Receiver already unregistered or never registered.")
                }
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                Log.d("QuoteEngine", "Wallpaper became visible. Redrawing current frame.")
                // Only redraw the current frame when becoming visible
                drawFrame()
            } else {
                Log.d("QuoteEngine", "Wallpaper became invisible.")
                handler.removeCallbacks(drawRunner)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            drawFrame()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunner)
        }

        private fun loadInitialImageList() {
            engineScope.launch(Dispatchers.IO) {
                selectedImages = repository.getSelectedImagesOrderedById()
                Log.d("QuoteEngine", "Loaded ${selectedImages.size} selected images initially.")
                if (selectedImages.isNotEmpty()) {
                    // Load the first image immediately for the initial display
                    currentImageIndex = 0
                    // Use the renamed function 'loadBitmap'
                    val initialBitmap = loadBitmap(currentImageIndex)
                    withContext(Dispatchers.Main) {
                        currentBitmap = initialBitmap // Assign the loaded bitmap
                        drawFrame() // Draw the first frame
                    }
                } else {
                    currentImageIndex = -1
                    currentBitmap = null
                    withContext(Dispatchers.Main) {
                        drawFrame() // Draw placeholder if no images
                    }
                }
            }
        }

        private fun changeToNextImageAndDraw() {
             if (selectedImages.isEmpty()) {
                Log.d("QuoteEngine", "changeToNextImageAndDraw: No selected images.")
                return // Don't change if no images
            }

            // Calculate next index
            val nextIndex = (currentImageIndex + 1) % selectedImages.size
            Log.d("QuoteEngine", "changeToNextImageAndDraw: Loading and drawing index $nextIndex")

            // Launch background task to load the bitmap
            engineScope.launch(Dispatchers.IO) {
                val loadedBitmap = loadBitmap(nextIndex) // Load the bitmap for the next index
                // Switch back to main thread to update state and draw
                withContext(Dispatchers.Main) {
                    if (loadedBitmap != null) {
                        currentBitmap = loadedBitmap
                        currentImageIndex = nextIndex // Update index only after successful load
                        Log.d("QuoteEngine", "changeToNextImageAndDraw: Updated currentBitmap and index to $currentImageIndex")
                        drawFrame() // Draw the newly loaded bitmap
                    } else {
                        Log.e("QuoteEngine", "changeToNextImageAndDraw: Failed to load bitmap for index $nextIndex. Wallpaper not changed.")
                        // Optionally: Keep the old bitmap and index, or draw placeholder
                    }
                }
            }
        }

        private fun loadBitmap(indexToLoad: Int): Bitmap? {
            if (selectedImages.isEmpty() || indexToLoad < 0 || indexToLoad >= selectedImages.size) {
                Log.w("QuoteEngine", "loadBitmap: Invalid state - index out of bounds ($indexToLoad)")
                return null
            }
            val imagePath = selectedImages[indexToLoad].filePath
            Log.d("QuoteEngine", "loadBitmap: Attempting to load index $indexToLoad, path: $imagePath")
            return try {
                val file = File(imagePath)
                if (file.exists()) {
                    val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                    val bitmap = BitmapFactory.decodeFile(imagePath, options)
                    if (bitmap != null) {
                        Log.d("QuoteEngine", "loadBitmap: Successfully loaded bitmap for index $indexToLoad")
                    } else {
                        Log.e("QuoteEngine", "loadBitmap: BitmapFactory.decodeFile returned null for index $indexToLoad, path: $imagePath")
                    }
                    bitmap // Return the loaded bitmap or null
                } else {
                    Log.e("QuoteEngine", "loadBitmap: Image file not found for index $indexToLoad, path: $imagePath")
                    null
                }
            } catch (e: Exception) {
                 Log.e("QuoteEngine", "loadBitmap: Exception loading bitmap for index $indexToLoad: ${e.message}", e)
                 null
            }
        }

        private fun drawFrame() {
            Log.d("QuoteEngine", "drawFrame: Called. Current index: $currentImageIndex, Bitmap is null: ${currentBitmap == null}") // Log drawFrame call
            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    if (currentBitmap != null) {
                        val canvasRatio = canvas.width.toFloat() / canvas.height.toFloat()
                        val bitmapRatio = currentBitmap!!.width.toFloat() / currentBitmap!!.height.toFloat()
                        var scale = 1f
                        var srcRect: Rect
                        var dstRect = Rect(0, 0, canvas.width, canvas.height)

                        if (bitmapRatio > canvasRatio) {
                            scale = canvas.height.toFloat() / currentBitmap!!.height.toFloat()
                            val newWidth = (currentBitmap!!.width * scale).toInt()
                            val xOffset = (canvas.width - newWidth) / 2
                            dstRect = Rect(xOffset, 0, xOffset + newWidth, canvas.height)
                        } else {
                            scale = canvas.width.toFloat() / currentBitmap!!.width.toFloat()
                            val newHeight = (currentBitmap!!.height * scale).toInt()
                            val yOffset = (canvas.height - newHeight) / 2
                            dstRect = Rect(0, yOffset, canvas.width, yOffset + newHeight)
                        }
                        Log.d("QuoteEngine", "drawFrame: Drawing bitmap for index $currentImageIndex") // Log bitmap drawing
                        canvas.drawBitmap(currentBitmap!!, null, dstRect, bitmapPaint)
                    } else {
                        canvas.drawColor(Color.DKGRAY)
                        val paint = Paint().apply {
                            color = Color.WHITE
                            textSize = 60f
                            textAlign = Paint.Align.CENTER
                        }
                        val text = if (selectedImages.isEmpty()) "No selected images" else "Loading..."
                        Log.d("QuoteEngine", "drawFrame: Drawing placeholder (bitmap was null)") // Log placeholder drawing
                        canvas.drawText(text, canvas.width / 2f, canvas.height / 2f, paint)
                    }
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
            }

            handler.removeCallbacks(drawRunner)
        }
    }
}