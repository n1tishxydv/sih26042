"""Interactive Web Live Stream & Controller for SIH26042 Android Emulator."""
import http.server
import socketserver
import subprocess
import urllib.parse
import sys
import os

PORT = 8088
ADB = r"C:\Users\Nitish kumar\AppData\Local\Android\Sdk\platform-tools\adb.exe"

HTML_PAGE = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>SIH26042 Android Live Preview & Controller</title>
    <style>
        body {
            background-color: #0f172a;
            color: #f8fafc;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            margin: 0;
            padding: 20px;
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        .header {
            text-align: center;
            margin-bottom: 16px;
        }
        .status-badge {
            background: #10b981;
            color: #022c22;
            padding: 4px 12px;
            border-radius: 9999px;
            font-weight: 600;
            font-size: 13px;
            display: inline-block;
            margin-bottom: 8px;
        }
        .main-container {
            display: flex;
            gap: 24px;
            background: #1e293b;
            padding: 20px;
            border-radius: 16px;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5);
            border: 1px solid #334155;
        }
        .phone-frame {
            position: relative;
            width: 360px;
            height: 800px;
            background: #000;
            border-radius: 36px;
            overflow: hidden;
            border: 8px solid #334155;
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.4);
            cursor: pointer;
        }
        #screen-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
        }
        .controls-panel {
            width: 320px;
            display: flex;
            flex-direction: column;
            gap: 12px;
        }
        .btn {
            background: #2563eb;
            color: white;
            border: none;
            padding: 12px 16px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.2s;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
        }
        .btn:hover {
            background: #1d4ed8;
        }
        .btn-secondary {
            background: #475569;
        }
        .btn-secondary:hover {
            background: #334155;
        }
        .btn-success {
            background: #059669;
        }
        .btn-success:hover {
            background: #047857;
        }
        .nav-bar {
            display: flex;
            gap: 8px;
            margin-top: 8px;
        }
        .nav-btn {
            flex: 1;
            padding: 10px;
            font-size: 16px;
        }
        .info-card {
            background: #0f172a;
            border-radius: 8px;
            padding: 12px;
            font-size: 13px;
            color: #94a3b8;
            line-height: 1.5;
            border: 1px solid #334155;
        }
    </style>
</head>
<body>
    <div class="header">
        <span class="status-badge">● LIVE ANDROID RUNTIME ACTIVE</span>
        <h1 style="margin: 4px 0 0 0; font-size: 24px;">SIH26042 Co-Teacher Live Preview</h1>
        <p style="margin: 4px 0; color: #94a3b8; font-size: 14px;">Click anywhere on the screen below to interact with the real Android application!</p>
    </div>

    <div class="main-container">
        <div class="phone-frame" id="phone" onclick="handleClick(event)">
            <img id="screen-img" src="/screen.png" alt="Android Screen" />
        </div>

        <div class="controls-panel">
            <h3 style="margin: 0 0 4px 0;">Hardware Navigation</h3>
            <div class="nav-bar">
                <button class="btn btn-secondary nav-btn" onclick="sendKey(4)">◀ Back</button>
                <button class="btn btn-secondary nav-btn" onclick="sendKey(3)">● Home</button>
                <button class="btn btn-secondary nav-btn" onclick="sendKey(187)">■ Apps</button>
            </div>

            <h3 style="margin: 16px 0 4px 0;">Quick Screen Jump</h3>
            <button class="btn btn-success" onclick="launchApp()">🚀 Relaunch Co-Teacher</button>
            <button class="btn" onclick="tapCoordinate(283, 1800)">📚 NIPUN Lessons</button>
            <button class="btn" onclick="tapCoordinate(797, 1800)">📝 NIPUN Worksheets</button>
            <button class="btn" onclick="tapCoordinate(283, 2135)">🎴 FLN Flashcards</button>
            <button class="btn" onclick="tapCoordinate(540, 595)">🧰 Teacher Toolkit</button>
            <button class="btn" onclick="tapCoordinate(540, 740)">🎙️ Live Classroom Mic</button>
            <button class="btn btn-secondary" onclick="tapCoordinate(540, 890)">⚖️ SIH Judge Mode</button>

            <div class="info-card" style="margin-top: auto;">
                <strong>App Package:</strong> org.sih26042.coteacher<br>
                <strong>Emulator:</strong> Android 16 (API 36, x86_64)<br>
                <strong>Resolution:</strong> 1080 x 2400 px<br>
                <strong>Offline Mode:</strong> 100% Active
            </div>
        </div>
    </div>

    <script>
        const img = document.getElementById('screen-img');
        const phone = document.getElementById('phone');

        function refreshScreen() {
            const nextImg = new Image();
            nextImg.onload = () => {
                img.src = nextImg.src;
                setTimeout(refreshScreen, 800);
            };
            nextImg.onerror = () => {
                setTimeout(refreshScreen, 1500);
            };
            nextImg.src = '/screen.png?t=' + Date.now();
        }

        function handleClick(event) {
            const rect = phone.getBoundingClientRect();
            const clickX = event.clientX - rect.left;
            const clickY = event.clientY - rect.top;
            
            // Map 360x800 to real 1080x2400 display coordinates
            const targetX = Math.round((clickX / rect.width) * 1080);
            const targetY = Math.round((clickY / rect.height) * 2400);

            tapCoordinate(targetX, targetY);
        }

        function tapCoordinate(x, y) {
            fetch('/tap?x=' + x + '&y=' + y, { method: 'POST' })
                .then(() => setTimeout(refreshScreen, 300));
        }

        function sendKey(code) {
            fetch('/key?code=' + code, { method: 'POST' })
                .then(() => setTimeout(refreshScreen, 300));
        }

        function launchApp() {
            fetch('/launch', { method: 'POST' })
                .then(() => setTimeout(refreshScreen, 600));
        }

        // Start screen streaming
        setTimeout(refreshScreen, 500);
    </script>
</body>
</html>
"""

class LivePreviewHandler(http.server.BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        pass  # Quiet logging

    def do_GET(self):
        parsed = urllib.parse.urlparse(self.path)
        if parsed.path == "/" or parsed.path == "/index.html":
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.end_headers()
            self.wfile.write(HTML_PAGE.encode("utf-8"))
        elif parsed.path == "/screen.png":
            # Capture real frame from adb
            try:
                result = subprocess.run([ADB, "exec-out", "screencap", "-p"], capture_output=True, check=True)
                png_bytes = result.stdout
                self.send_response(200)
                self.send_header("Content-Type", "image/png")
                self.send_header("Cache-Control", "no-cache, no-store, must-revalidate")
                self.send_header("Content-Length", str(len(png_bytes)))
                self.end_headers()
                self.wfile.write(png_bytes)
            except Exception as e:
                self.send_response(500)
                self.end_headers()
        else:
            self.send_response(404)
            self.end_headers()

    def do_POST(self):
        parsed = urllib.parse.urlparse(self.path)
        params = urllib.parse.parse_qs(parsed.query)
        if parsed.path == "/tap":
            x = params.get("x", ["540"])[0]
            y = params.get("y", ["1200"])[0]
            subprocess.run([ADB, "shell", "input", "tap", x, y])
            self.send_response(200)
            self.end_headers()
        elif parsed.path == "/key":
            code = params.get("code", ["4"])[0]
            subprocess.run([ADB, "shell", "input", "keyevent", code])
            self.send_response(200)
            self.end_headers()
        elif parsed.path == "/launch":
            subprocess.run([ADB, "shell", "am", "start", "-n", "org.sih26042.coteacher/.MainActivity"])
            self.send_response(200)
            self.end_headers()
        else:
            self.send_response(404)
            self.end_headers()

if __name__ == "__main__":
    print(f"Starting SIH26042 Live Preview Server on http://127.0.0.1:{PORT}...")
    server = socketserver.TCPServer(("127.0.0.1", PORT), LivePreviewHandler)
    server.serve_forever()
