"""Sirve la pagina de instalacion y el APK en la red local."""

import argparse
import os
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer


class ApkHandler(SimpleHTTPRequestHandler):
    extensions_map = {
        **getattr(SimpleHTTPRequestHandler, "extensions_map", {}),
        ".apk": "application/vnd.android.package-archive",
        ".html": "text/html; charset=utf-8",
        ".js": "text/javascript; charset=utf-8",
    }

    def end_headers(self):
        self.send_header("Cache-Control", "no-store")
        super().end_headers()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--dir", required=True)
    parser.add_argument("--port", type=int, default=8765)
    args = parser.parse_args()
    directory = os.path.abspath(args.dir)
    handler = partial(ApkHandler, directory=directory)
    server = ThreadingHTTPServer(("0.0.0.0", args.port), handler)
    print(f"SIRVIENDO {directory} en el puerto {args.port}", flush=True)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()


if __name__ == "__main__":
    main()
