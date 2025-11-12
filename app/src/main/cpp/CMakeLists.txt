cmake_minimum_required(VERSION 3.22.1)

project("native-lib")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# OpenCV setup - point to the correct native/jni directory
set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/opencv/sdk/native/jni)

# Find OpenCV
find_package(OpenCV REQUIRED)

# Add native library
add_library(native-lib SHARED
    native-lib.cpp
    edge_processor.cpp
)

# Include directories
target_include_directories(native-lib PRIVATE
    ${OpenCV_INCLUDE_DIRS}
)

# Link libraries
target_link_libraries(native-lib
    ${OpenCV_LIBS}
    android
    log
    GLESv2
    EGL
)
