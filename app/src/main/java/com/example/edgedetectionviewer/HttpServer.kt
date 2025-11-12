package com.example.edgedetectionviewer

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue

class HttpServer(private val port: Int = 8080) {
    
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val frameQueue = ConcurrentLinkedQueue<FrameData>()
    
    data class FrameData(
        val bitmap: Bitmap,
        val processingTime: Long,
        val filterType: String,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    fun start() {
        if (isRunning) return
        
        Thread {
            try {
                serverSocket = ServerSocket(port)
                isRunning = true
                Log.i(TAG, "HTTP Server started on port $port")
                
                while (isRunning) {
                    try {
                        val client = serverSocket?.accept()
                        client?.let {
                            Thread { handleClient(it) }.start()
                        }
                    } catch (e: Exception) {
                        if (isRunning) {
                            Log.e(TAG, "Error accepting client", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Server error", e)
            }
        }.start()
    }
    
    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
            serverSocket = null
            Log.i(TAG, "HTTP Server stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server", e)
        }
    }
    
    fun updateFrame(bitmap: Bitmap, processingTime: Long, filterType: String) {
        val frameData = FrameData(bitmap, processingTime, filterType)
        frameQueue.offer(frameData)
        
        // Keep only the latest frame
        if (frameQueue.size > 1) {
            frameQueue.poll()
        }
    }
    
    private fun handleClient(client: Socket) {
        try {
            val input = BufferedReader(InputStreamReader(client.getInputStream()))
            val output = BufferedOutputStream(client.getOutputStream())
            
            // Read request line
            val requestLine = input.readLine() ?: return
            Log.d(TAG, "Request: $requestLine")
            
            // Read headers
            var line: String?
            do {
                line = input.readLine()
            } while (!line.isNullOrEmpty())
            
            // Parse request
            val parts = requestLine.split(" ")
            if (parts.size >= 2) {
                val method = parts[0]
                val path = parts[1]
                
                when {
                    path == "/latest-frame" && method == "GET" -> {
                        sendLatestFrame(output)
                    }
                    path == "/status" && method == "GET" -> {
                        sendStatus(output)
                    }
                    path == "/" && method == "GET" -> {
                        sendHomePage(output)
                    }
                    else -> {
                        send404(output)
                    }
                }
            }
            
            output.flush()
            client.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error handling client", e)
        }
    }
    
    private fun sendLatestFrame(output: OutputStream) {
        val frame = frameQueue.peek()
        
        if (frame != null) {
            try {
                // Convert bitmap to Base64
                val byteArrayOutputStream = ByteArrayOutputStream()
                frame.bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                val base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                
                // Create JSON response
                val json = JSONObject().apply {
                    put("width", frame.bitmap.width)
                    put("height", frame.bitmap.height)
                    put("data", "data:image/jpeg;base64,$base64Image")
                    put("timestamp", frame.timestamp)
                    put("processingTime", frame.processingTime)
                    put("filterType", frame.filterType)
                }
                
                val response = json.toString()
                val headers = """
                    HTTP/1.1 200 OK
                    Content-Type: application/json
                    Content-Length: ${response.length}
                    Access-Control-Allow-Origin: *
                    Connection: close
                    
                    
                """.trimIndent()
                
                output.write(headers.toByteArray())
                output.write(response.toByteArray())
            } catch (e: Exception) {
                Log.e(TAG, "Error sending frame", e)
                send500(output)
            }
        } else {
            val response = JSONObject().apply {
                put("error", "No frame available")
            }.toString()
            
            val headers = """
                HTTP/1.1 404 Not Found
                Content-Type: application/json
                Content-Length: ${response.length}
                Access-Control-Allow-Origin: *
                Connection: close
                
                
            """.trimIndent()
            
            output.write(headers.toByteArray())
            output.write(response.toByteArray())
        }
    }
    
    private fun sendStatus(output: OutputStream) {
        val json = JSONObject().apply {
            put("status", "running")
            put("port", port)
            put("hasFrame", frameQueue.isNotEmpty())
        }
        
        val response = json.toString()
        val headers = """
            HTTP/1.1 200 OK
            Content-Type: application/json
            Content-Length: ${response.length}
            Access-Control-Allow-Origin: *
            Connection: close
            
            
        """.trimIndent()
        
        output.write(headers.toByteArray())
        output.write(response.toByteArray())
    }
    
    private fun sendHomePage(output: OutputStream) {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Edge Detection Server</title>
                <style>
                    body { font-family: Arial; padding: 20px; background: #1a1a1a; color: #fff; }
                    .status { padding: 10px; background: #2a2a2a; border-radius: 5px; }
                    a { color: #4CAF50; }
                </style>
            </head>
            <body>
                <h1>🎨 Edge Detection HTTP Server</h1>
                <div class="status">
                    <p>✅ Server is running on port $port</p>
                    <p>📡 Available endpoints:</p>
                    <ul>
                        <li><a href="/latest-frame">/latest-frame</a> - Get latest processed frame (JSON)</li>
                        <li><a href="/status">/status</a> - Server status</li>
                    </ul>
                    <p>💡 Access the web viewer at: <a href="http://192.168.1.16:3000">http://192.168.1.16:3000</a></p>
                </div>
            </body>
            </html>
        """.trimIndent()
        
        val headers = """
            HTTP/1.1 200 OK
            Content-Type: text/html
            Content-Length: ${html.length}
            Connection: close
            
            
        """.trimIndent()
        
        output.write(headers.toByteArray())
        output.write(html.toByteArray())
    }
    
    private fun send404(output: OutputStream) {
        val response = "404 Not Found"
        val headers = """
            HTTP/1.1 404 Not Found
            Content-Type: text/plain
            Content-Length: ${response.length}
            Connection: close
            
            
        """.trimIndent()
        
        output.write(headers.toByteArray())
        output.write(response.toByteArray())
    }
    
    private fun send500(output: OutputStream) {
        val response = "500 Internal Server Error"
        val headers = """
            HTTP/1.1 500 Internal Server Error
            Content-Type: text/plain
            Content-Length: ${response.length}
            Connection: close
            
            
        """.trimIndent()
        
        output.write(headers.toByteArray())
        output.write(response.toByteArray())
    }
    
    companion object {
        private const val TAG = "HttpServer"
    }
}
