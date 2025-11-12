# Edge Detection Web Viewer

TypeScript-based web viewer for displaying processed frames from the Android Edge Detection application.

## Prerequisites

- **Node.js** 16+ and npm
- Modern web browser (Chrome, Firefox, Edge, Safari)

## Setup & Development

### 1. Install Dependencies

```powershell
npm install
```

### 2. Development Mode

Run in watch mode for automatic recompilation during development:

```powershell
npm run watch
```

Then open `index.html` directly in your browser, or use a local server.

### 3. Build for Production

Compile TypeScript to JavaScript:

```powershell
npm run build
```

This generates `dist/bundle.js` from `src/index.ts`.

## Deployment Options

### Option A: Local Development Server (Quick Testing)

```powershell
# Using npm serve (if you have it)
npm run serve

# Or use Python (if installed)
python -m http.server 8080

# Or use Node.js http-server
npx http-server -p 8080
```

Open browser to `http://localhost:8080`

### Option B: Deploy to GitHub Pages

1. **Build the project:**
   ```powershell
   npm run build
   ```

2. **Push to GitHub:**
   ```powershell
   git add .
   git commit -m "feat: Add web viewer build"
   git push origin main
   ```

3. **Enable GitHub Pages:**
   - Go to your repository: `https://github.com/Sameerbeedi/Real-Time-Edge-Detection`
   - Navigate to **Settings → Pages**
   - Under **Source**, select `main` branch and `/web` folder (or `/root` if web is at root)
   - Click **Save**

4. **Access your site:**
   - URL will be: `https://sameerbeedi.github.io/Real-Time-Edge-Detection/`

### Option C: Deploy to Netlify

1. **Create `netlify.toml` in web folder:**
   ```toml
   [build]
     publish = "."
     command = "npm run build"
   
   [[redirects]]
     from = "/*"
     to = "/index.html"
     status = 200
   ```

2. **Deploy via Netlify CLI:**
   ```powershell
   npm install -g netlify-cli
   netlify login
   netlify deploy --prod
   ```

3. **Or Deploy via Netlify UI:**
   - Go to [netlify.com](https://netlify.com)
   - Click **Add new site → Import existing project**
   - Connect your GitHub repository
   - Set build command: `npm run build`
   - Set publish directory: `web`
   - Click **Deploy**

### Option D: Deploy to Vercel

```powershell
# Install Vercel CLI
npm install -g vercel

# Deploy
vercel --prod
```

Follow the prompts to link your project and deploy.

### Option E: Host on Firebase Hosting

1. **Install Firebase CLI:**
   ```powershell
   npm install -g firebase-tools
   ```

2. **Initialize Firebase:**
   ```powershell
   firebase login
   firebase init hosting
   ```
   - Select your Firebase project (or create new one)
   - Set public directory to: `web`
   - Configure as single-page app: **No**

3. **Deploy:**
   ```powershell
   firebase deploy --only hosting
   ```

## Usage

### Loading Frames

Currently, the viewer demonstrates frame display with a simulated edge-detected frame:

1. Open the web viewer in your browser
2. Click **"Load Sample Frame"** to display a test frame
3. Click **"Toggle Stats"** to show/hide frame statistics

### Statistics Display

The stats panel shows:
- **Resolution:** Frame dimensions (width × height)
- **FPS:** Frames per second (simulated for demo)
- **Processing Time:** Time taken for edge detection
- **Filter:** Current filter applied (e.g., Canny)

## Features

✅ Canvas-based frame rendering  
✅ FPS counter (simulated)  
✅ Frame statistics overlay  
✅ Responsive design  
✅ TypeScript type safety  
✅ Modular, extensible code  

## Future Integration

This viewer can be extended to receive real-time frames via:

### WebSocket Integration (Recommended)

```typescript
// Example WebSocket client
const ws = new WebSocket('ws://your-android-device:8080');
ws.onmessage = (event) => {
    const frameData = JSON.parse(event.data);
    viewer.updateFrame(frameData);
};
```

### HTTP Polling

```typescript
// Example polling
setInterval(async () => {
    const frame = await fetch('http://your-device-ip:8080/latest-frame');
    const data = await frame.json();
    viewer.updateFrame(data);
}, 100); // Poll every 100ms
```

### File Upload

Add file input to `index.html`:
```html
<input type="file" id="frameUpload" accept="image/*">
```

See `src/index.ts` → `loadFrameFromUrl()` method for HTTP integration example.

## Project Structure

```
web/
├── src/
│   └── index.ts          # Main TypeScript implementation
├── dist/
│   └── bundle.js         # Compiled JavaScript (generated)
├── index.html            # Web interface
├── styles.css            # Styling
├── package.json          # Dependencies and scripts
├── tsconfig.json         # TypeScript configuration
└── README.md             # This file
```

## Development Scripts

| Command | Description |
|---------|-------------|
| `npm install` | Install dependencies |
| `npm run build` | Compile TypeScript to JavaScript |
| `npm run watch` | Watch mode - auto recompile on changes |
| `npm run serve` | Start local development server |

## Troubleshooting

### TypeScript Not Compiling

```powershell
# Reinstall dependencies
Remove-Item -Recurse -Force node_modules
npm install

# Rebuild
npm run build
```

### Port Already in Use

```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F

# Or use a different port
python -m http.server 3000
```

### Browser Cache Issues

- Hard refresh: `Ctrl+Shift+R` (Windows) or `Cmd+Shift+R` (Mac)
- Or clear browser cache in settings

## License

MIT License - See main repository LICENSE file
