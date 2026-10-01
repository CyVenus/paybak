#!/usr/bin/env python3
"""Figma REST API helper for the Paybak design file. Uses the REST API, not the Figma MCP.

Token: a Figma personal access token in ~/.config/paybak/figma_token (one line; never commit it,
never print it). File key: 2SPNUpHlG8bCO62YfwRuRi ("Paybak-iOS").
A small limiter keeps all of this user's processes under 8 REST calls/min (Figma's Tier 1 allows
about 10/min for this seat). Its state lives in the system temp dir (tempfile.gettempdir()). A 429
answer is retried after Retry-After; 5xx answers are retried after 10 s.

Usage:
  figma_rest.py nodes <id,id,...> [--depth N] [--out file.json]
      GET /v1/files/:key/nodes: full node JSON (layout, fills, text, components, annotations and
      prototype links as `interactions`)
  figma_rest.py render <id,id,...> --outdir DIR [--scale 2] [--format png|svg|jpg] [--names names.json]
      GET /v1/images, then downloads every render (batches of 25 ids). --names maps node id -> file
      stem ({"22:55": "welcome1"}); unmapped ids are saved as <id>.<format>.
  figma_rest.py file [--depth N] [--out file.json]
      GET /v1/files/:key?depth=N: the document tree (default depth 2)
Node ids use the colon form (e.g. 22:55). Output filenames replace ':' with '-' (and ';' with '_').
Without --out, JSON is printed to stdout (cut at 200,000 characters).
"""
import fcntl
import json
import os
import sys
import tempfile
import time
import urllib.error
import urllib.parse
import urllib.request

KEY = "2SPNUpHlG8bCO62YfwRuRi"
TOKEN_PATH = os.path.expanduser("~/.config/paybak/figma_token")
STATE = os.path.join(tempfile.gettempdir(), "paybak_figma_rest_slots.json")
LOCK = os.path.join(tempfile.gettempdir(), "paybak_figma_rest_slots.lock")
MAX_CALLS, WINDOW = 8, 60.0

_token = None


def token():
    global _token
    if _token is None:
        try:
            with open(TOKEN_PATH) as f:
                _token = f.read().strip()
        except FileNotFoundError:
            raise SystemExit(f"No Figma token: put a personal access token in {TOKEN_PATH}")
        if not _token:
            raise SystemExit(f"{TOKEN_PATH} is empty")
    return _token


def slot():
    """Blocks until a REST call fits in the shared 8-calls-per-60-s window, then records it."""
    while True:
        with open(LOCK, "a+") as lf:
            fcntl.flock(lf, fcntl.LOCK_EX)
            try:
                try:
                    with open(STATE) as f:
                        st = json.load(f)
                except Exception:
                    st = {"calls": []}
                now = time.time()
                st["calls"] = [t for t in st["calls"] if now - t < WINDOW]
                if len(st["calls"]) < MAX_CALLS:
                    st["calls"].append(now)
                    with open(STATE, "w") as f:
                        json.dump(st, f)
                    return
                wait = WINDOW - (now - min(st["calls"])) + 0.2
            finally:
                fcntl.flock(lf, fcntl.LOCK_UN)
        time.sleep(min(wait, 10))


def api(path, params):
    url = f"https://api.figma.com{path}?{urllib.parse.urlencode(params)}"
    for _ in range(6):
        slot()
        req = urllib.request.Request(url, headers={"X-Figma-Token": token()})
        try:
            with urllib.request.urlopen(req, timeout=180) as r:
                return json.load(r)
        except urllib.error.HTTPError as e:
            if e.code == 429:
                ra = int(e.headers.get("Retry-After", "30") or 30)
                print(f"429 rate limited; waiting {ra}s", file=sys.stderr)
                time.sleep(min(ra, 120))
                continue
            if e.code >= 500:
                time.sleep(10)
                continue
            raise SystemExit(f"HTTP {e.code}: {e.read()[:300]!r}")
    raise SystemExit("gave up after retries")


def fname(i):
    return i.replace(":", "-").replace(";", "_")


def write_json(d, out):
    with open(out, "w") as f:
        json.dump(d, f)
    print("wrote", out, os.path.getsize(out), "bytes")


def get_nodes(ids, depth=None):
    """GET /v1/files/:key/nodes for a list (or comma string) of node ids."""
    params = {"ids": ids if isinstance(ids, str) else ",".join(ids)}
    if depth:
        params["depth"] = str(depth)
    return api(f"/v1/files/{KEY}/nodes", params)


def get_file(depth=2):
    """GET /v1/files/:key?depth=N."""
    return api(f"/v1/files/{KEY}", {"depth": str(depth)})


def render(ids, outdir, fmt="png", scale="2", names=None):
    """Renders node ids with GET /v1/images (25 per call) and downloads them into outdir."""
    ids = [i for i in (ids.split(",") if isinstance(ids, str) else ids) if i]
    names = names or {}
    os.makedirs(outdir, exist_ok=True)
    saved = []
    for k in range(0, len(ids), 25):
        batch = ids[k:k + 25]
        params = {"ids": ",".join(batch), "format": fmt}
        if fmt != "svg":
            params["scale"] = str(scale)
        else:
            params["svg_include_id"] = "false"
            params["svg_simplify_stroke"] = "true"
        d = api(f"/v1/images/{KEY}", params)
        for i, url in (d.get("images") or {}).items():
            if not url:
                print("no render for", i)
                continue
            path = os.path.join(outdir, (names.get(i) or fname(i)) + "." + fmt)
            urllib.request.urlretrieve(url, path)
            saved.append(path)
            print("saved", path)
    return saved


def main():
    a = sys.argv[1:]
    if not a or a[0] in ("-h", "--help"):
        print(__doc__)
        return

    def opt(name, default=None):
        return a[a.index(name) + 1] if name in a and a.index(name) + 1 < len(a) else default

    cmd = a[0]
    if cmd == "nodes" and len(a) > 1:
        d = get_nodes(a[1], opt("--depth"))
        if opt("--out"):
            write_json(d, opt("--out"))
        else:
            print(json.dumps(d)[:200000])
    elif cmd == "file":
        d = get_file(opt("--depth", "2"))
        if opt("--out"):
            write_json(d, opt("--out"))
        else:
            print(json.dumps(d)[:200000])
    elif cmd == "render" and len(a) > 1:
        if not opt("--outdir"):
            raise SystemExit("render needs --outdir DIR")
        names = {}
        if opt("--names"):
            with open(opt("--names")) as f:
                names = json.load(f)
        render(a[1], opt("--outdir"), opt("--format", "png"), opt("--scale", "2"), names)
    else:
        print(__doc__)


if __name__ == "__main__":
    main()
