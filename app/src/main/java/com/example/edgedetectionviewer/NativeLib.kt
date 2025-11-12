package com.example.edgedetectionviewer

object NativeLib {
    
    /**
     * Initialize OpenCV and native resources
     */
    external fun initNative(): Boolean
    
    /**
     * Process frame with Canny edge detection
     * @param width frame width
     * @param height frame height
     * @param data input frame data (RGBA format)
     * @param outputData output buffer for processed frame
     * @return processing time in milliseconds
     */
    external fun processFrame(width: Int, height: Int, data: ByteArray, outputData: ByteArray): Long
    
    /**
     * Apply grayscale conversion
     */
    external fun applyGrayscale(width: Int, height: Int, data: ByteArray, outputData: ByteArray): Long
    
    /**
     * Cleanup native resources
     */
    external fun cleanupNative()
}
