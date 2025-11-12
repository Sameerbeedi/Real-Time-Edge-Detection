# OpenCV Setup Instructions

## Download OpenCV Android SDK

1. Visit [OpenCV Releases](https://opencv.org/releases/)
2. Download **OpenCV Android SDK** (version 4.5.0 or later)
3. Extract the downloaded ZIP file

## Install OpenCV in Project

After extraction, you should have a folder structure like:
```
opencv-android-sdk/
├── sdk/
│   ├── native/
│   │   ├── jni/
│   │   │   ├── include/
│   │   │   │   └── opencv2/
│   │   │   └── libs/
│   │   │       ├── arm64-v8a/
│   │   │       │   └── libopencv_java4.so
│   │   │       └── armeabi-v7a/
│   │   │           └── libopencv_java4.so
│   │   └── ...
```

Copy the entire `sdk` folder to:
```
app/src/main/cpp/opencv/sdk/
```

## Verify Installation

After copying, your project structure should be:
```
app/src/main/cpp/
├── opencv/
│   └── sdk/
│       └── native/
│           └── jni/
│               ├── include/
│               └── libs/
├── CMakeLists.txt
├── native-lib.cpp
├── edge_processor.h
└── edge_processor.cpp
```

## Update CMakeLists.txt

The `CMakeLists.txt` is already configured. If you placed OpenCV in a different location, update this line:

```cmake
set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/opencv/sdk/native/jni)
```

## Build the Project

1. Sync Gradle in Android Studio
2. Build → Make Project
3. If successful, you'll see `libopencv_java4.so` copied to `app/build/intermediates/`

## Troubleshooting

### Error: "OpenCV not found"
- Verify the OpenCV SDK path in `CMakeLists.txt`
- Check that `opencv/sdk/native/jni/include/opencv2/` exists
- Clean and rebuild the project

### Error: "Cannot find libopencv_java4.so"
- Check that `.so` files exist in `opencv/sdk/native/jni/libs/arm64-v8a/`
- Verify NDK ABI filters in `app/build.gradle.kts` match available libraries

### Error: "Undefined reference to cv::"
- Ensure OpenCV libraries are linked in `CMakeLists.txt`
- Check `target_link_libraries(native-lib ${OpenCV_LIBS} ...)`

## Alternative: Using OpenCV Manager (Not Recommended)

For a simpler setup (but less control), you can use OpenCV Manager from Google Play. However, this approach is not recommended for this assessment as it doesn't demonstrate NDK integration skills.

## Size Optimization

OpenCV libraries are large (~20-30 MB per ABI). To reduce APK size:

1. Build for specific ABIs only (edit `app/build.gradle.kts`):
```kotlin
ndk {
    abiFilters += listOf("arm64-v8a")  // 64-bit only
}
```

2. Use App Bundle instead of APK for dynamic delivery

3. Consider custom OpenCV build with only required modules
