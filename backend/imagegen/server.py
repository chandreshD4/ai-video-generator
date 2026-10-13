import json
import os
import re
import uuid
import urllib.request
import urllib.error
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import quote

HOST = "127.0.0.1"
PORT = 8766
ENV_FILE = Path.home() / ".config" / "ai-video-generator" / "pollinations.env"
OUTPUT_DIR = Path.home() / "ai-video-generator" / "backend" / "imagegen" / "output"
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

MAX_PROMPT_LENGTH = 2000
MAX_RESPONSE_BYTES = 20 * 1024 * 1024


def load_api_key():
    if not ENV_FILE.exists():
        raise RuntimeError("Pollinations API key file not found")

    for line in ENV_FILE.read_text().splitlines():
        if line.startswith("POLLINATIONS_API_KEY="):
            key = line.split("=", 1)[1].strip()
            if key:
                return key

    raise RuntimeError("Pollinations API key is empty")


def send_json(handler, status, data):
    body = json.dumps(data).encode("utf-8")
    handler.send_response(status)
    handler.send_header("Content-Type", "application/json; charset=utf-8")
    handler.send_header("Content-Length", str(len(body)))
    handler.send_header("Cache-Control", "no-store")
    handler.end_headers()
    handler.wfile.write(body)


class ImageGenHandler(BaseHTTPRequestHandler):
    def log_message(self, fmt, *args):
        print("ImageGen:", fmt % args)

    def do_GET(self):
        if self.path == "/health":
            send_json(self, 200, {
                "status": "ok",
                "service": "imagegen",
                "port": PORT
            })
            return

        match = re.fullmatch(r"/image/([a-f0-9]{32}\.(?:png|jpg|webp))", self.path)
        if match:
            filename = match.group(1)
            image_path = OUTPUT_DIR / filename
            if not image_path.is_file():
                send_json(self, 404, {"error": "Image not found"})
                return

            content_type = {
                ".png": "image/png",
                ".jpg": "image/jpeg",
                ".webp": "image/webp"
            }[image_path.suffix]

            image_bytes = image_path.read_bytes()
            self.send_response(200)
            self.send_header("Content-Type", content_type)
            self.send_header("Content-Length", str(len(image_bytes)))
            self.send_header("Cache-Control", "no-store")
            self.end_headers()
            self.wfile.write(image_bytes)
            return

        send_json(self, 404, {"error": "Endpoint not found"})

    def do_POST(self):
        if self.path != "/generate":
            send_json(self, 404, {"error": "Endpoint not found"})
            return

        try:
            length = int(self.headers.get("Content-Length", "0"))
            if length <= 0 or length > 16384:
                send_json(self, 400, {"error": "Invalid request size"})
                return

            data = json.loads(self.rfile.read(length).decode("utf-8"))
            prompt = str(data.get("prompt", "")).strip()

            if not prompt:
                send_json(self, 400, {"error": "Prompt is required"})
                return

            aspect_ratio = data.get("aspect_ratio", "1:1")
            dimensions = {"1:1": (1024, 1024), "9:16": (768, 1365), "16:9": (1365, 768)}
            if aspect_ratio not in dimensions:
                send_json(self, 400, {"error": "Invalid aspect_ratio"})
                return
            width, height = dimensions[aspect_ratio]

            if len(prompt) > MAX_PROMPT_LENGTH:
                send_json(self, 400, {"error": "Prompt is too long"})
                return

            api_key = load_api_key()
            image_url = (
                "https://gen.pollinations.ai/image/"
                + quote(prompt, safe="")
                + f"?width={width}&height={height}"
            )

            request = urllib.request.Request(
                image_url,
                headers={
                    "Authorization": "Bearer " + api_key,
                    "Accept": "image/*",
                    "User-Agent": "AI-Video-Generator/1.0"
                },
                method="GET"
            )

            with urllib.request.urlopen(request, timeout=180) as response:
                content_type = response.headers.get("Content-Type", "")
                if not content_type.lower().startswith("image/"):
                    send_json(self, 502, {
                        "error": "Image API returned a non-image response"
                    })
                    return

                image_bytes = response.read(MAX_RESPONSE_BYTES + 1)

            if not image_bytes or len(image_bytes) > MAX_RESPONSE_BYTES:
                send_json(self, 502, {"error": "Image is empty or too large"})
                return

            # Verify the returned file is a supported image format.
            if not (
                image_bytes.startswith(b"\x89PNG\r\n\x1a\n")
                or image_bytes.startswith(b"\xff\xd8\xff")
                or image_bytes.startswith(b"RIFF")
            ):
                send_json(self, 502, {"error": "Invalid image data"})
                return

            extension = (
                ".png" if image_bytes.startswith(b"\x89PNG\r\n\x1a\n")
                else ".jpg" if image_bytes.startswith(b"\xff\xd8\xff")
                else ".webp"
            )
            filename = uuid.uuid4().hex + extension
            output_path = OUTPUT_DIR / filename
            output_path.write_bytes(image_bytes)

            send_json(self, 200, {
                "status": "generated",
                "filename": filename,
                "path": str(output_path),
                "bytes": len(image_bytes)
            })

        except urllib.error.HTTPError as exc:
            # Do not return upstream response bodies that might contain details.
            send_json(self, 502, {
                "error": "Image API request failed",
                "upstream_status": exc.code
            })
        except urllib.error.URLError:
            send_json(self, 502, {"error": "Could not connect to image API"})
        except (ValueError, UnicodeDecodeError):
            send_json(self, 400, {"error": "Invalid JSON request"})
        except TimeoutError:
            send_json(self, 504, {"error": "Image generation timed out"})
        except Exception as exc:
            print("Image generation error:", type(exc).__name__)
            send_json(self, 500, {"error": "Image generation failed"})


if __name__ == "__main__":
    server = ThreadingHTTPServer((HOST, PORT), ImageGenHandler)
    print(f"AI Image Generator server running on http://{HOST}:{PORT}")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nStopping image server")
    finally:
        server.server_close()
