# Building the Android Project

## ⭐ Recommended Method: Android Studio

Since Gradle wrapper files are not fully set up, the **easiest and most reliable** way to build your project is through **Android Studio**.

### Steps:

1. **Open Android Studio**

2. **Open the Project**
   - Click **File → Open**
   - Navigate to: `D:\Users\sameer\Downloads\flam`
   - Click **OK**

3. **Wait for Gradle Sync**
   - Android Studio will automatically:
     - Download Gradle wrapper
     - Sync project with Gradle files
     - Download dependencies
     - Index project files
   - This takes 2-5 minutes on first open

4. **Build the Project**
   - Click **Build → Make Project** (or press `Ctrl+F9`)
   - Or click **Build → Build Bundle(s) / APK(s) → Build APK(s)**

5. **Run on Device**
   - Connect your Android phone via USB
   - Click the green **Run** button (▶️) or press `Shift+F10`
   - Select your device
   - App will build, install, and launch automatically

---

## 🔧 Alternative: Fix Gradle Wrapper (Advanced)

If you want to use command line, you need to set up the Gradle wrapper properly:

### Option A: Let Android Studio Fix It

1. Open project in Android Studio
2. It will automatically download and configure Gradle wrapper
3. After that, you can use `.\gradlew.bat` from command line

### Option B: Manual Setup

1. **Install Gradle System-Wide**
   - Download from: https://gradle.org/releases/
   - Or use Chocolatey: `choco install gradle`

2. **Generate Wrapper**
   ```powershell
   cd D:\Users\sameer\Downloads\flam
   gradle wrapper --gradle-version 8.4
   ```

3. **Then Build**
   ```powershell
   .\gradlew.bat build
   # Or for debug APK:
   .\gradlew.bat assembleDebug
   ```

---

## ⚡ Quick Build Commands (After Gradle Setup)

```powershell
# Build debug APK
.\gradlew.bat assembleDebug

# Build and install on connected device
.\gradlew.bat installDebug

# Clean build
.\gradlew.bat clean assembleDebug

# Run tests
.\gradlew.bat test

# Build release APK (unsigned)
.\gradlew.bat assembleRelease
```

APK Location:
- Debug: `app\build\outputs\apk\debug\app-debug.apk`
- Release: `app\build\outputs\apk\release\app-release.apk`

---

## 🐛 Troubleshooting

### "gradlew is not recognized"

**Solution:** Use Android Studio to open the project first. It will set up Gradle wrapper automatically.

### "OpenCV not found" during build

**Solution:** Already fixed! OpenCV SDK is installed at `app/src/main/cpp/opencv/sdk/`

### Build fails with dependency errors

**Solution:**
```powershell
# Clear Gradle cache
Remove-Item -Recurse -Force .gradle
# Reopen in Android Studio and sync
```

### Java/JDK not found

**Solution:**
- Android Studio includes JDK
- Or set JAVA_HOME environment variable:
  ```powershell
  $env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
  ```

---

## 📱 Testing on Phone

Once built via Android Studio:

1. **Enable USB Debugging** on phone (see TESTING_GUIDE.md)
2. **Connect phone** via USB
3. **Click Run (▶️)** in Android Studio
4. **App installs and launches** automatically

That's it! Android Studio handles everything.

---

## 🎯 Summary

**For this assessment, use Android Studio:**
- ✅ Easiest and most reliable
- ✅ Handles Gradle setup automatically
- ✅ Better error messages
- ✅ Built-in device management
- ✅ One-click deploy to phone
- ✅ Debugging tools included

**Don't waste time on command-line Gradle issues!**
