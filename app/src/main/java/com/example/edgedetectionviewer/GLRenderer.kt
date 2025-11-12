package com.example.edgedetectionviewer

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

enum class ShaderEffect {
    NORMAL,      // No effect
    GRAYSCALE,   // Grayscale filter
    INVERT,      // Color inversion
    SEPIA,       // Sepia tone
    EDGE_ENHANCE // Edge enhancement
}

class GLRenderer(private val context: Context, private val glSurfaceView: GLSurfaceView) : GLSurfaceView.Renderer {
    
    private var surfaceTexture: SurfaceTexture? = null
    private var textureId = 0
    private val programs = mutableMapOf<ShaderEffect, Int>()
    private var currentEffect = ShaderEffect.NORMAL
    private var vertexBuffer: FloatBuffer
    private var textureBuffer: FloatBuffer
    
    private var positionHandle = 0
    private var textureCoordHandle = 0
    private var textureSamplerHandle = 0
    private var mvpMatrixHandle = 0
    
    private val vertexShaderCode = """
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        uniform mat4 uMVPMatrix;
        
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vTexCoord = aTexCoord;
        }
    """.trimIndent()
    
    // Normal shader - no effects
    private val fragmentShaderNormal = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTexCoord;
        uniform samplerExternalOES uTexture;
        
        void main() {
            gl_FragColor = texture2D(uTexture, vTexCoord);
        }
    """.trimIndent()
    
    // Grayscale shader
    private val fragmentShaderGrayscale = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTexCoord;
        uniform samplerExternalOES uTexture;
        
        void main() {
            vec4 color = texture2D(uTexture, vTexCoord);
            float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
            gl_FragColor = vec4(gray, gray, gray, color.a);
        }
    """.trimIndent()
    
    // Invert shader
    private val fragmentShaderInvert = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTexCoord;
        uniform samplerExternalOES uTexture;
        
        void main() {
            vec4 color = texture2D(uTexture, vTexCoord);
            gl_FragColor = vec4(1.0 - color.rgb, color.a);
        }
    """.trimIndent()
    
    // Sepia shader
    private val fragmentShaderSepia = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTexCoord;
        uniform samplerExternalOES uTexture;
        
        void main() {
            vec4 color = texture2D(uTexture, vTexCoord);
            float r = dot(color.rgb, vec3(0.393, 0.769, 0.189));
            float g = dot(color.rgb, vec3(0.349, 0.686, 0.168));
            float b = dot(color.rgb, vec3(0.272, 0.534, 0.131));
            gl_FragColor = vec4(r, g, b, color.a);
        }
    """.trimIndent()
    
    // Edge enhance shader
    private val fragmentShaderEdgeEnhance = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTexCoord;
        uniform samplerExternalOES uTexture;
        
        void main() {
            vec2 texelSize = vec2(1.0 / 1280.0, 1.0 / 720.0);
            
            // Sobel kernel for edge detection
            vec4 center = texture2D(uTexture, vTexCoord);
            vec4 left = texture2D(uTexture, vTexCoord - vec2(texelSize.x, 0.0));
            vec4 right = texture2D(uTexture, vTexCoord + vec2(texelSize.x, 0.0));
            vec4 up = texture2D(uTexture, vTexCoord - vec2(0.0, texelSize.y));
            vec4 down = texture2D(uTexture, vTexCoord + vec2(0.0, texelSize.y));
            
            vec4 edgeX = (right - left) * 2.0;
            vec4 edgeY = (down - up) * 2.0;
            vec4 edge = sqrt(edgeX * edgeX + edgeY * edgeY);
            
            gl_FragColor = center + edge * 0.5;
        }
    """.trimIndent()
    
    // Vertex coordinates (2 triangles forming a quad)
    private val vertices = floatArrayOf(
        -1.0f, -1.0f,  // bottom left
         1.0f, -1.0f,  // bottom right
        -1.0f,  1.0f,  // top left
         1.0f,  1.0f   // top right
    )
    
    // Texture coordinates
    private val textureCoords = floatArrayOf(
        0.0f, 1.0f,  // bottom left
        1.0f, 1.0f,  // bottom right
        0.0f, 0.0f,  // top left
        1.0f, 0.0f   // top right
    )
    
    private val mvpMatrix = FloatArray(16).apply {
        // Identity matrix
        this[0] = 1f; this[5] = 1f; this[10] = 1f; this[15] = 1f
    }
    
    init {
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices)
        vertexBuffer.position(0)
        
        textureBuffer = ByteBuffer.allocateDirect(textureCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(textureCoords)
        textureBuffer.position(0)
        
        NativeLib.initNative()
    }
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        android.util.Log.d("EdgeDetection", "GLRenderer: onSurfaceCreated")
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        
        // Create texture for camera
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        
        surfaceTexture = SurfaceTexture(textureId)
        surfaceTexture?.setOnFrameAvailableListener {
            // Request render when new frame is available (for RENDERMODE_WHEN_DIRTY)
            glSurfaceView.requestRender()
        }
        android.util.Log.d("EdgeDetection", "GLRenderer: SurfaceTexture created with textureId=$textureId")
        
        // Compile all shader programs
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        
        // Create programs for each effect
        programs[ShaderEffect.NORMAL] = createProgram(vertexShader, fragmentShaderNormal)
        programs[ShaderEffect.GRAYSCALE] = createProgram(vertexShader, fragmentShaderGrayscale)
        programs[ShaderEffect.INVERT] = createProgram(vertexShader, fragmentShaderInvert)
        programs[ShaderEffect.SEPIA] = createProgram(vertexShader, fragmentShaderSepia)
        programs[ShaderEffect.EDGE_ENHANCE] = createProgram(vertexShader, fragmentShaderEdgeEnhance)
        
        // Get handles for current program
        updateHandles()
    }
    
    private fun createProgram(vertexShader: Int, fragmentShaderCode: String): Int {
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        return program
    }
    
    private fun updateHandles() {
        val programId = programs[currentEffect] ?: return
        positionHandle = GLES20.glGetAttribLocation(programId, "aPosition")
        textureCoordHandle = GLES20.glGetAttribLocation(programId, "aTexCoord")
        textureSamplerHandle = GLES20.glGetUniformLocation(programId, "uTexture")
        mvpMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
    }
    
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }
    
    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        
        surfaceTexture?.updateTexImage()
        
        val programId = programs[currentEffect]
        if (programId == null) {
            android.util.Log.e("EdgeDetection", "GLRenderer: Program is null for effect $currentEffect")
            return
        }
        GLES20.glUseProgram(programId)
        
        // Set vertex positions
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        
        // Set texture coordinates
        GLES20.glEnableVertexAttribArray(textureCoordHandle)
        GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)
        
        // Set texture sampler
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(textureSamplerHandle, 0)
        
        // Set MVP matrix
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        
        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        
        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(textureCoordHandle)
    }
    
    fun setShaderEffect(effect: ShaderEffect) {
        currentEffect = effect
        updateHandles()
    }
    
    fun getShaderEffect(): ShaderEffect = currentEffect
    
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }
    
    fun getSurfaceTexture(): SurfaceTexture? {
        android.util.Log.d("EdgeDetection", "GLRenderer: getSurfaceTexture called, texture=${if (surfaceTexture != null) "available" else "null"}")
        return surfaceTexture
    }
}
