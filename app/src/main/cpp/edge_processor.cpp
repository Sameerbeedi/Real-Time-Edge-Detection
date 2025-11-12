#include "edge_processor.h"
#include <android/log.h>

#define LOG_TAG "EdgeProcessor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

EdgeProcessor::EdgeProcessor() 
    : lowThreshold(50.0)
    , highThreshold(150.0)
    , kernelSize(3)
{
    LOGI("EdgeProcessor initialized");
}

EdgeProcessor::~EdgeProcessor() {
    LOGI("EdgeProcessor destroyed");
}

bool EdgeProcessor::processCannyEdge(const cv::Mat& input, cv::Mat& output) {
    try {
        if (input.empty()) {
            LOGE("Input frame is empty");
            return false;
        }
        
        // Convert RGBA to grayscale
        cv::cvtColor(input, tempGray, cv::COLOR_RGBA2GRAY);
        
        // Apply Gaussian blur to reduce noise
        cv::GaussianBlur(tempGray, tempBlurred, cv::Size(5, 5), 1.5);
        
        // Apply Canny edge detection
        cv::Canny(tempBlurred, tempEdges, lowThreshold, highThreshold, kernelSize);
        
        // Convert edges back to RGBA for display
        // White edges on black background
        cv::cvtColor(tempEdges, output, cv::COLOR_GRAY2RGBA);
        
        return true;
    } catch (const cv::Exception& e) {
        LOGE("OpenCV exception in processCannyEdge: %s", e.what());
        return false;
    } catch (const std::exception& e) {
        LOGE("Exception in processCannyEdge: %s", e.what());
        return false;
    }
}

bool EdgeProcessor::applyGrayscale(const cv::Mat& input, cv::Mat& output) {
    try {
        if (input.empty()) {
            LOGE("Input frame is empty");
            return false;
        }
        
        // Convert to grayscale
        cv::cvtColor(input, tempGray, cv::COLOR_RGBA2GRAY);
        
        // Convert back to RGBA
        cv::cvtColor(tempGray, output, cv::COLOR_GRAY2RGBA);
        
        return true;
    } catch (const cv::Exception& e) {
        LOGE("OpenCV exception in applyGrayscale: %s", e.what());
        return false;
    } catch (const std::exception& e) {
        LOGE("Exception in applyGrayscale: %s", e.what());
        return false;
    }
}

void EdgeProcessor::setCannyThresholds(double low, double high) {
    lowThreshold = low;
    highThreshold = high;
    LOGI("Canny thresholds set to: low=%f, high=%f", low, high);
}
