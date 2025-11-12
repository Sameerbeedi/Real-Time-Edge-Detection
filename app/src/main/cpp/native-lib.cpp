#include <jni.h>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include "edge_processor.h"

#define LOG_TAG "NativeLib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static EdgeProcessor* processor = nullptr;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_example_edgedetectionviewer_NativeLib_initNative(JNIEnv* env, jobject /* this */) {
    LOGI("Initializing native library");
    
    if (processor == nullptr) {
        processor = new EdgeProcessor();
    }
    
    LOGI("OpenCV version: %s", cv::getVersionString().c_str());
    return JNI_TRUE;
}

JNIEXPORT jlong JNICALL
Java_com_example_edgedetectionviewer_NativeLib_processFrame(
    JNIEnv* env,
    jobject /* this */,
    jint width,
    jint height,
    jbyteArray data,
    jbyteArray outputData
) {
    if (processor == nullptr) {
        LOGE("Processor not initialized");
        return -1;
    }
    
    auto startTime = std::chrono::high_resolution_clock::now();
    
    // Get input data
    jbyte* inputBytes = env->GetByteArrayElements(data, nullptr);
    jbyte* outputBytes = env->GetByteArrayElements(outputData, nullptr);
    
    if (inputBytes == nullptr || outputBytes == nullptr) {
        LOGE("Failed to get byte arrays");
        return -1;
    }
    
    // Create OpenCV Mat from input (RGBA format)
    cv::Mat inputMat(height, width, CV_8UC4, (unsigned char*)inputBytes);
    cv::Mat outputMat(height, width, CV_8UC4, (unsigned char*)outputBytes);
    
    // Process frame with Canny edge detection
    bool success = processor->processCannyEdge(inputMat, outputMat);
    
    // Release arrays
    env->ReleaseByteArrayElements(data, inputBytes, JNI_ABORT);
    env->ReleaseByteArrayElements(outputData, outputBytes, 0);
    
    auto endTime = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(endTime - startTime);
    
    return success ? duration.count() : -1;
}

JNIEXPORT jlong JNICALL
Java_com_example_edgedetectionviewer_NativeLib_applyGrayscale(
    JNIEnv* env,
    jobject /* this */,
    jint width,
    jint height,
    jbyteArray data,
    jbyteArray outputData
) {
    if (processor == nullptr) {
        LOGE("Processor not initialized");
        return -1;
    }
    
    auto startTime = std::chrono::high_resolution_clock::now();
    
    jbyte* inputBytes = env->GetByteArrayElements(data, nullptr);
    jbyte* outputBytes = env->GetByteArrayElements(outputData, nullptr);
    
    if (inputBytes == nullptr || outputBytes == nullptr) {
        LOGE("Failed to get byte arrays");
        return -1;
    }
    
    cv::Mat inputMat(height, width, CV_8UC4, (unsigned char*)inputBytes);
    cv::Mat outputMat(height, width, CV_8UC4, (unsigned char*)outputBytes);
    
    bool success = processor->applyGrayscale(inputMat, outputMat);
    
    env->ReleaseByteArrayElements(data, inputBytes, JNI_ABORT);
    env->ReleaseByteArrayElements(outputData, outputBytes, 0);
    
    auto endTime = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(endTime - startTime);
    
    return success ? duration.count() : -1;
}

JNIEXPORT void JNICALL
Java_com_example_edgedetectionviewer_NativeLib_cleanupNative(JNIEnv* env, jobject /* this */) {
    LOGI("Cleaning up native library");
    
    if (processor != nullptr) {
        delete processor;
        processor = nullptr;
    }
}

} // extern "C"
