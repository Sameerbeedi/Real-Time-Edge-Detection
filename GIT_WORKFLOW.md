# Git Commit Workflow Guide

This guide helps you create a proper commit history for evaluation.

## Initial Setup

```powershell
# Initialize git (if not already done)
git init

# Add remote
git remote add origin https://github.com/Sameerbeedi/Real-Time-Edge-Detection-Viewer.git

# Check status
git status
```

## Recommended Commit Sequence

### Phase 1: Project Setup (3 commits)

```powershell
# Commit 1: Initial configuration
git add .gitignore LICENSE README.md OPENCV_SETUP.md
git commit -m "chore: Initial project setup with documentation"

# Commit 2: Gradle build configuration
git add build.gradle.kts settings.gradle.kts gradle.properties gradle/
git commit -m "build: Setup Gradle build system and wrapper"

# Commit 3: Android app module configuration
git add app/build.gradle.kts app/proguard-rules.pro
git commit -m "build: Configure Android app module with NDK and Camera2"
```

### Phase 2: Android Resources (2 commits)

```powershell
# Commit 4: Manifest and permissions
git add app/src/main/AndroidManifest.xml
git commit -m "feat: Add AndroidManifest with camera and OpenGL permissions"

# Commit 5: UI resources
git add app/src/main/res/
git commit -m "feat: Add Android resources (layouts, strings, colors, themes)"
```

### Phase 3: Kotlin Implementation (3 commits)

```powershell
# Commit 6: Main activity with Camera2
git add app/src/main/java/com/example/edgedetectionviewer/MainActivity.kt
git commit -m "feat: Implement MainActivity with Camera2 API integration"

# Commit 7: OpenGL renderer
git add app/src/main/java/com/example/edgedetectionviewer/GLRenderer.kt
git commit -m "feat: Implement OpenGL ES 2.0 renderer with external texture"

# Commit 8: JNI interface
git add app/src/main/java/com/example/edgedetectionviewer/NativeLib.kt
git commit -m "feat: Add JNI interface for native OpenCV processing"
```

### Phase 4: Native C++ Implementation (3 commits)

```powershell
# Commit 9: CMake configuration
git add app/src/main/cpp/CMakeLists.txt
git commit -m "build: Add CMake configuration for native library with OpenCV"

# Commit 10: OpenCV edge processor
git add app/src/main/cpp/edge_processor.h app/src/main/cpp/edge_processor.cpp
git commit -m "feat: Implement EdgeProcessor with Canny edge detection"

# Commit 11: JNI bridge
git add app/src/main/cpp/native-lib.cpp
git commit -m "feat: Implement JNI bridge connecting Kotlin to C++ OpenCV"
```

### Phase 5: TypeScript Web Viewer (3 commits)

```powershell
# Commit 12: Web project setup
git add web/package.json web/tsconfig.json web/README.md
git commit -m "build: Setup TypeScript web viewer project"

# Commit 13: Web UI
git add web/index.html web/styles.css
git commit -m "feat: Add web viewer UI with responsive design and stats panel"

# Commit 14: TypeScript implementation
git add web/src/
git commit -m "feat: Implement TypeScript viewer with Canvas rendering and frame stats"
```

### Phase 6: Final Documentation (1 commit)

```powershell
# Commit 15: Complete documentation
git add README.md
git commit -m "docs: Complete comprehensive README with architecture and setup guide"
```

## Push to GitHub

```powershell
# Push all commits
git branch -M master
git push -u origin master

# Verify on GitHub
# Visit: https://github.com/Sameerbeedi/Real-Time-Edge-Detection-Viewer
```

## View Your Commit History

```powershell
# See all commits
git log --oneline --graph

# See detailed commits
git log --stat
```

## Expected Output

You should see approximately 15 commits with clear, descriptive messages showing:
- Project setup and configuration
- Android implementation (Kotlin + resources)
- Native C++ implementation (JNI + OpenCV)
- Web viewer (TypeScript)
- Documentation

## Tips for Evaluation

✅ **DO:**
- Use conventional commit prefixes (`feat:`, `build:`, `docs:`, `fix:`)
- Keep commits focused on one aspect
- Write clear, descriptive messages
- Commit frequently during development

❌ **DON'T:**
- Make one giant commit with all changes
- Use vague messages like "update" or "changes"
- Commit build artifacts or node_modules
- Commit the OpenCV SDK (too large, excluded in .gitignore)

## Troubleshooting

### "Large files warning"
If Git warns about large files:
```powershell
# Check .gitignore includes OpenCV
grep opencv .gitignore

# Remove accidentally staged large files
git rm --cached -r app/src/main/cpp/opencv/
```

### "Already pushed wrong commits"
If you need to fix commit history:
```powershell
# Reset to a previous commit (WARNING: destructive)
git reset --soft HEAD~3  # Goes back 3 commits, keeps changes

# Or use interactive rebase
git rebase -i HEAD~5  # Edit last 5 commits
```

## Final Checklist

Before submitting, verify:
- [ ] All commits are pushed to GitHub
- [ ] Commit history is clean and descriptive
- [ ] README.md is complete
- [ ] .gitignore excludes build artifacts
- [ ] OpenCV SDK is NOT committed (download separately)
- [ ] Repository is public or shared with evaluators
