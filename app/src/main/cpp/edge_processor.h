#ifndef EDGE_PROCESSOR_H
#define EDGE_PROCESSOR_H

#include <opencv2/opencv.hpp>
#include <chrono>

class EdgeProcessor {
public:
    EdgeProcessor();
    ~EdgeProcessor();
    
    /**
     * Apply Canny edge detection to input frame
     * @param input Input frame (RGBA format)
     * @param output Output frame (RGBA format)
     * @return true if successful
     */
    bool processCannyEdge(const cv::Mat& input, cv::Mat& output);
    
    /**
     * Convert frame to grayscale
     * @param input Input frame (RGBA format)
     * @param output Output frame (RGBA format)
     * @return true if successful
     */
    bool applyGrayscale(const cv::Mat& input, cv::Mat& output);
    
    /**
     * Set Canny edge detection thresholds
     */
    void setCannyThresholds(double low, double high);
    
private:
    double lowThreshold;
    double highThreshold;
    int kernelSize;
    
    cv::Mat tempGray;
    cv::Mat tempEdges;
    cv::Mat tempBlurred;
};

#endif // EDGE_PROCESSOR_H
