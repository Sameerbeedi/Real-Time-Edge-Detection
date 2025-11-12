package com.example.edgedetectionviewer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var renderer: GLRenderer
    private lateinit var fpsTextView: TextView
    private lateinit var modeTextView: TextView
    private lateinit var toggleButton: Button
    private lateinit var shaderButton: Button
    
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private var httpServer: HttpServer? = null
    
    private var isProcessingEnabled = false
    private var frameCount = 0
    private var lastFpsTime = System.currentTimeMillis()
    
    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1001
        
        init {
            System.loadLibrary("native-lib")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        glSurfaceView = findViewById(R.id.glSurfaceView)
        fpsTextView = findViewById(R.id.fpsTextView)
        modeTextView = findViewById(R.id.modeTextView)
        toggleButton = findViewById(R.id.toggleButton)
        shaderButton = findViewById(R.id.shaderButton)
        
        // Initialize OpenGL ES 2.0
        glSurfaceView.setEGLContextClientVersion(2)
        renderer = GLRenderer(this)
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        
        toggleButton.setOnClickListener {
            isProcessingEnabled = !isProcessingEnabled
            modeTextView.text = if (isProcessingEnabled) 
                getString(R.string.processed_mode) 
            else 
                getString(R.string.raw_mode)
        }
        
        shaderButton.setOnClickListener {
            cycleShaderEffect()
        }
        
        // Start HTTP server for web viewer
        httpServer = HttpServer(8080)
        httpServer?.start()
        Toast.makeText(this, "HTTP Server started on port 8080", Toast.LENGTH_LONG).show()
        
        // Request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, 
                arrayOf(Manifest.permission.CAMERA), 
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            startCamera()
        }
        
        // Start FPS counter
        startFpsCounter()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int, 
        permissions: Array<out String>, 
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    
    private fun startCamera() {
        startBackgroundThread()
        
        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0]
            
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
            
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createCameraPreviewSession()
                }
                
                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                }
                
                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Camera error: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            }, backgroundHandler)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun createCameraPreviewSession() {
        try {
            val surfaceTexture = renderer.getSurfaceTexture() ?: return
            surfaceTexture.setDefaultBufferSize(1280, 720)
            
            val surface = Surface(surfaceTexture)
            
            val captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder?.addTarget(surface)
            
            cameraDevice?.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        if (cameraDevice == null) return
                        
                        cameraCaptureSession = session
                        
                        captureRequestBuilder?.set(
                            CaptureRequest.CONTROL_AF_MODE,
                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                        )
                        captureRequestBuilder?.set(
                            CaptureRequest.CONTROL_AE_MODE,
                            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                        )
                        
                        val captureRequest = captureRequestBuilder?.build()
                        
                        cameraCaptureSession?.setRepeatingRequest(
                            captureRequest!!,
                            null,
                            backgroundHandler
                        )
                    }
                    
                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Camera configuration failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                backgroundHandler
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread!!.looper)
    }
    
    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
    
    private fun startFpsCounter() {
        val handler = Handler(mainLooper)
        handler.post(object : Runnable {
            override fun run() {
                frameCount++
                val currentTime = System.currentTimeMillis()
                val elapsed = currentTime - lastFpsTime
                
                if (elapsed >= 1000) {
                    val fps = (frameCount * 1000 / elapsed).toInt()
                    fpsTextView.text = getString(R.string.fps_label, fps)
                    frameCount = 0
                    lastFpsTime = currentTime
                }
                
                handler.postDelayed(this, 100)
            }
        })
    }
    
    private fun cycleShaderEffect() {
        val effects = ShaderEffect.values()
        val currentIndex = effects.indexOf(renderer.getShaderEffect())
        val nextIndex = (currentIndex + 1) % effects.size
        val nextEffect = effects[nextIndex]
        
        renderer.setShaderEffect(nextEffect)
        
        // Update button text to show current shader
        val effectName = when(nextEffect) {
            ShaderEffect.NORMAL -> "Normal"
            ShaderEffect.GRAYSCALE -> "Grayscale"
            ShaderEffect.INVERT -> "Invert"
            ShaderEffect.SEPIA -> "Sepia"
            ShaderEffect.EDGE_ENHANCE -> "Edge Enhance"
        }
        shaderButton.text = "Shader: $effectName"
    }
    
    fun isProcessingEnabled(): Boolean = isProcessingEnabled
    
    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
        cameraCaptureSession?.close()
        cameraCaptureSession = null
        cameraDevice?.close()
        cameraDevice = null
        stopBackgroundThread()
        httpServer?.stop()
    }
}
