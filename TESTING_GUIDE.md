# 📱 Testing on Android Phone - Complete Guide

## Prerequisites Checklist
- [ ] Android phone with Android 7.0+ (API 24+)
- [ ] USB cable
- [ ] Android Studio installed on your PC
- [ ] OpenCV SDK downloaded and placed in `app/src/main/cpp/opencv/`

---

## Option 1: Deploy via Android Studio (Recommended)

### Step 1: Enable Developer Options on Your Phone

1. **Open Settings** on your Android phone
2. Scroll to **About Phone** (or **About Device**)
3. Find **Build Number**
4. **Tap Build Number 7 times** rapidly
5. You'll see: "You are now a developer!"

### Step 2: Enable USB Debugging

1. Go back to **Settings**
2. Find **Developer Options** (usually in System or Advanced)
3. Toggle **Developer Options ON**
4. Scroll down and toggle **USB Debugging ON**
5. (Optional) Enable **Install via USB** if available

### Step 3: Connect Your Phone to PC

1. **Connect phone to PC** using USB cable
2. On your phone, you'll see a popup: **"Allow USB Debugging?"**
3. Check **"Always allow from this computer"**
4. Tap **OK**

### Step 4: Open Project in Android Studio

```powershell
# Navigate to project directory
cd D:\Users\sameer\Downloads\flam
```

1. Open **Android Studio**
2. Click **File → Open**
3. Select the `flam` folder
4. Wait for Gradle sync to complete

### Step 5: Verify Connection

In Android Studio:
1. Look at the **device dropdown** (top toolbar, next to Run button)
2. You should see your phone model listed
3. If not showing:
   ```powershell
   # Check device connection
   adb devices
   ```
   Should show:
   ```
   List of devices attached
   XXXXXXXXXXXXX    device
   ```

### Step 6: Build and Run

1. **Click the green Run button** (▶️) or press `Shift+F10`
2. Select your phone from device list
3. Click **OK**
4. Wait for build to complete (first build takes 5-10 minutes)
5. App will automatically install and launch on your phone

---

## Option 2: Deploy via Command Line (Gradle)

### Using PowerShell

```powershell
# Navigate to project directory
cd D:\Users\sameer\Downloads\flam

# Build debug APK
.\gradlew assembleDebug

# Install on connected device
.\gradlew installDebug

# Or do both in one command
.\gradlew installDebug

# Launch the app
adb shell am start -n com.example.edgedetectionviewer/.MainActivity
```

**APK Location:** `app\build\outputs\apk\debug\app-debug.apk`

---

## Option 3: Install APK Manually

### Build the APK

```powershell
cd D:\Users\sameer\Downloads\flam
.\gradlew assembleDebug
```

### Transfer APK to Phone

**Method A: Via USB Cable**
1. Copy `app\build\outputs\apk\debug\app-debug.apk`
2. Paste to phone's **Downloads** folder

**Method B: Via ADB**
```powershell
adb install app\build\outputs\apk\debug\app-debug.apk
```

**Method C: Via Email/Cloud**
1. Email the APK to yourself
2. Open on phone and download
3. Install from Downloads

### Install on Phone

1. Open **File Manager** on phone
2. Navigate to **Downloads**
3. Tap **app-debug.apk**
4. If prompted "Install unknown apps", allow from this source
5. Tap **Install**
6. Tap **Open**

---

## 🧪 Testing the App

### Test 1: Camera Permission & Feed
1. Launch app
2. Grant **Camera permission** when prompted
3. ✅ Verify: Live camera feed displays

### Test 2: Toggle Mode (OpenCV Processing)
1. Tap **"Toggle Mode"** button
2. ✅ Verify: Mode changes to "Processed Mode"
3. ✅ Verify: Canny edge detection is visible (white edges on black)
4. Tap button again
5. ✅ Verify: Returns to raw camera feed

### Test 3: OpenGL Shader Effects
1. Tap **"Shader: Normal"** button repeatedly
2. ✅ Verify effects cycle through:
   - Normal → Grayscale → Invert → Sepia → Edge Enhance → (repeat)
3. ✅ Verify: Smooth 30+ FPS rendering

### Test 4: FPS Counter
1. Check bottom-left corner
2. ✅ Verify: FPS counter updates every second
3. ✅ Verify: Shows 15-30 FPS (device dependent)

### Test 5: HTTP Server
1. Note the Toast notification: "HTTP Server started on port 8080"
2. Find your phone's IP address:
   - Go to **Settings → Wi-Fi**
   - Tap connected network
   - Note **IP address** (e.g., 192.168.1.XX)

3. On your PC, open browser:
   ```
   http://192.168.1.XX:8080
   ```
4. ✅ Verify: See server homepage with endpoints

5. Test frame endpoint:
   ```
   http://192.168.1.XX:8080/latest-frame
   ```
6. ✅ Verify: JSON response with Base64 frame data

---

## 🌐 Testing Web Viewer Integration

### Step 1: Start Web Viewer (PC)

```powershell
cd D:\Users\sameer\Downloads\flam\web
npm run build
npx http-server -p 3000 --cors
```

### Step 2: Connect Web Viewer to Android

1. Open browser: `http://localhost:3000`
2. In **Server URL** field, enter your phone's IP:
   ```
   http://192.168.1.XX:8080
   ```
3. Click **"Connect to Android"**
4. ✅ Verify: Frame loads in web viewer

### Step 3: Test Live Feed

1. Click **"Start Live Feed"**
2. ✅ Verify: Frames update continuously (~10 FPS)
3. ✅ Verify: Stats panel shows real-time data
4. Click **"Stop Live Feed"**
5. ✅ Verify: Polling stops

---

## 🐛 Troubleshooting

### Phone Not Detected

**Problem:** Device not showing in Android Studio

**Solutions:**
```powershell
# Check USB debugging is enabled
adb devices

# Restart ADB server
adb kill-server
adb start-server
adb devices

# Check USB driver (Windows)
# Install Google USB Driver via Android Studio:
# Tools → SDK Manager → SDK Tools → Google USB Driver
```

### Build Errors

**Problem:** OpenCV linking errors

**Solution:**
1. Verify OpenCV SDK path in `app/src/main/cpp/CMakeLists.txt`
2. Check OpenCV files exist:
   ```powershell
   ls app\src\main\cpp\opencv\sdk\native\jni\include
   ```

**Problem:** Gradle build fails

**Solution:**
```powershell
# Clean and rebuild
.\gradlew clean
.\gradlew assembleDebug --stacktrace
```

### Camera Not Working

**Problem:** Black screen or camera not starting

**Solutions:**
1. Check camera permission granted
2. Close other apps using camera
3. Restart app
4. Check logcat:
   ```powershell
   adb logcat | findstr EdgeDetection
   ```

### HTTP Server Not Accessible

**Problem:** Can't connect to `http://192.168.1.XX:8080`

**Solutions:**
1. **Verify same WiFi network:**
   - Phone and PC must be on same network
   
2. **Check phone's firewall:**
   - Some phones block incoming connections
   
3. **Test with phone's browser first:**
   - Open `http://localhost:8080` on phone
   - Should see server homepage
   
4. **Check IP address:**
   ```powershell
   # Ping phone from PC
   ping 192.168.1.XX
   ```

### Low FPS

**Problem:** FPS counter shows < 10 FPS

**Solutions:**
1. Close background apps on phone
2. Use in well-lit environment
3. Reduce frame resolution in code
4. Disable unused shader effects

---

## 📊 Performance Benchmarks

### Expected Performance:
- **FPS:** 15-30 (device dependent)
- **Processing Time:** 20-50ms per frame
- **Memory:** < 200 MB RAM
- **Battery:** Moderate drain during use

### High-End Devices (Snapdragon 8xx, 2022+)
- 25-30 FPS consistently
- Processing: 15-25ms

### Mid-Range Devices (Snapdragon 7xx, 2020+)
- 20-25 FPS
- Processing: 25-40ms

### Budget Devices (Snapdragon 6xx, older)
- 15-20 FPS
- Processing: 40-60ms

---

## 📸 Taking Screenshots for Documentation

### During Testing:

1. **Raw Mode Screenshot:**
   - Ensure "Raw Mode" is displayed
   - Press **Volume Down + Power** (or device-specific combo)

2. **Processed Mode Screenshot:**
   - Tap "Toggle Mode"
   - Show Canny edge detection
   - Take screenshot

3. **Shader Effects:**
   - Cycle through each shader
   - Take screenshot of each effect

4. **FPS Counter:**
   - Show FPS counter visible at bottom

### Recording Demo Video:

**Option A: Phone's Built-in Recorder**
1. Open **Screen Recorder** (varies by device)
2. Start recording
3. Launch app and demonstrate features
4. Stop recording

**Option B: ADB Screen Recording**
```powershell
# Record 30 seconds at 4 Mbps
adb shell screenrecord --time-limit 30 --bit-rate 4000000 /sdcard/demo.mp4

# Pull video to PC
adb pull /sdcard/demo.mp4 .
```

---

## ✅ Testing Checklist

Before submission, verify:
- [ ] App installs without errors
- [ ] Camera permission requested and granted
- [ ] Live camera feed displays (Raw Mode)
- [ ] Toggle button switches to Processed Mode
- [ ] Canny edge detection visible
- [ ] FPS counter displays and updates
- [ ] Shader button cycles through 5 effects
- [ ] HTTP server starts (Toast notification)
- [ ] Server accessible from PC browser
- [ ] Web viewer can fetch frames
- [ ] Live feed works in web viewer
- [ ] App doesn't crash during 5 minutes of use
- [ ] Screenshots taken for documentation

---

## 🎯 Next Steps After Testing

1. ✅ Test all features on physical device
2. 📸 Take screenshots and record demo
3. 🔧 Fix any bugs found
4. 📝 Update README with actual screenshots
5. 🗂️ Create proper Git commit history
6. 🚀 Push to GitHub
7. 📧 Submit for evaluation

---

**Good luck with testing!** 🚀
