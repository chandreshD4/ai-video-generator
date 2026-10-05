import json
import re
import urllib.request
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import unquote

HOST = "127.0.0.1"
PORT = 8765

ENV_FILE = Path.home() / ".config" / "ai-video-generator" / "pollinations.env"
OUTPUT_DIR = Path.home() / "ai-video-generator" / "backend" / "voiceover" / "output"
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

ALLOWED_MODELS = {
    "openai/tts-1",
    "openai/tts-1-hd",
}


def load_api_key():
    if not ENV_FILE.exists():
        raise RuntimeError("Pollinations API key file not found")

    for line in ENV_FILE.read_text().splitlines():
        if line.startswith("POLLINATIONS_API_KEY="):
            key = line.split("=", 1)[1].strip()
            if key:
                return key

    raise RuntimeError("POLLINATIONS_API_KEY is empty")


def safe_name(value, fallback):
    value = re.sub(r"[^A-Za-z0-9_-]", "_", str(value))
    value = value.strip("_")
    return value[:80] or fallback


class VoiceoverHandler(BaseHTTPRequestHandler):

    def send_json(self, status, data):
        body = json.dumps(data).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):

        if self.path == "/health":
            self.send_json(200, {
                "ok": True,
                "service": "voiceover",
                "port": PORT
            })
            return

        if self.path.startswith("/audio/"):
            filename = unquote(self.path[len("/audio/"):])
            filename = Path(filename).name

            if not filename.endswith(".mp3"):
                self.send_json(400, {
                    "ok": False,
                    "error": "Invalid audio file"
                })
                return

            audio_path = OUTPUT_DIR / filename

            if not audio_path.exists():
                self.send_json(404, {
                    "ok": False,
                    "error": "Audio file not found"
                })
                return

            audio = audio_path.read_bytes()

            self.send_response(200)
            self.send_header("Content-Type", "audio/mpeg")
            self.send_header("Content-Length", str(len(audio)))
            self.send_header("Cache-Control", "no-store")
            self.end_headers()
            self.wfile.write(audio)
            return

        self.send_json(404, {
            "ok": False,
            "error": "Not found"
        })

    def do_POST(self):

        if self.path != "/voiceover":
            self.send_json(404, {
                "ok": False,
                "error": "Not found"
            })
            return

        try:
            length = int(self.headers.get("Content-Length", "0"))
            raw = self.rfile.read(length)
            data = json.loads(raw.decode("utf-8"))

            text = str(data.get("text", "")).strip()
            voice = str(data.get("voice", "nova")).strip() or "nova"
            model = str(data.get("model", "openai/tts-1")).strip()
            project_id = str(data.get("project_id", "default")).strip() or "default"

            if not text:
                self.send_json(400, {
                    "ok": False,
                    "error": "Text is required"
                })
                return

            if len(text) > 12000:
                self.send_json(400, {
                    "ok": False,
                    "error": "Text is too long. Maximum 12000 characters."
                })
                return

            if model not in ALLOWED_MODELS:
                model = "openai/tts-1"

            api_key = load_api_key()

            payload = json.dumps({
                "model": model,
                "voice": voice,
                "input": text,
                "response_format": "mp3"
            }).encode("utf-8")

            request = urllib.request.Request(
                "https://gen.pollinations.ai/v1/audio/speech",
                data=payload,
                headers={
                    "Authorization": f"Bearer {api_key}",
                    "Content-Type": "application/json",
                    "User-Agent": "AIVideoGenerator/1.0"
                },
                method="POST"
            )

            with urllib.request.urlopen(request, timeout=600) as response:
                audio = response.read()

            filename = safe_name(project_id, "default") + ".mp3"
            output_path = OUTPUT_DIR / filename
            output_path.write_bytes(audio)

            self.send_json(200, {
                "ok": True,
                "filename": filename,
                "bytes": len(audio),
                "model": model,
                "voice": voice
            })

        except Exception as exc:
            print(f"[voiceover] ERROR: {exc}")

            self.send_json(500, {
                "ok": False,
                "error": str(exc)
            })

    def log_message(self, format, *args):
        print(f"[voiceover] {format % args}")


if __name__ == "__main__":
    print(f"Voiceover server: http://{HOST}:{PORT}")
    print("Press Ctrl+C to stop.")

    server = ThreadingHTTPServer((HOST, PORT), VoiceoverHandler)
    server.serve_forever()
