# Edge Detection Web Viewer

TypeScript-based web viewer for displaying processed frames from the Android Edge Detection application.

## Quick Start

1. Install dependencies:
```bash
npm install
```

2. Build TypeScript:
```bash
npm run build
```

3. Serve locally:
```bash
npm run serve
```

4. Open browser to `http://localhost:8080`

## Development

Watch mode for automatic recompilation:
```bash
npm run watch
```

## Features

- Canvas-based frame rendering
- FPS counter (simulated)
- Frame statistics overlay
- Responsive design
- TypeScript type safety

## Future Integration

This viewer can be extended to receive frames via:
- WebSocket connection
- HTTP polling
- File upload
- REST API

See `src/index.ts` → `loadFrameFromUrl()` method for HTTP integration example.
