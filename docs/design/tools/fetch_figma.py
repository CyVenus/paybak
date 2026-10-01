#!/usr/bin/env python3
"""Re-downloads the Paybak Figma file into docs/design/.figma-cache/ (gitignored) via the REST API.

It writes, in this order:
  file.json            the document tree at depth 1 (the page list)
  structure.json       every selected page at depth 2 (page -> sections -> frames/components)
  index.json           one row per frame/component: page, section, id, name, type
  INDEX.md             the same as a readable list with render and node-JSON paths (same format
                       as docs/design/figma-index.md)
  notes.md             the designer notes: the TEXT nodes that sit directly in each section, verbatim
                       (same format as docs/design/designer-notes.md; the "under «frame»" attribution
                       is a geometric guess: the text's left edge lines up with a frame above it)
  nodes/<page>.json    full node JSON per page (pages with more than 8 sections are split into
                       <page>-a.json, <page>-b.json, ...; the components page 3:3 gives 3-3-a/b)
  renders/<id>.png     2x renders of every screen frame (every page except the components page;
                       "↳ … (overlay)" prototype helpers are skipped)
  components/<id>.png  2x renders of every component frame on the components page (not the icons)
  svg/<id>.svg         SVG exports of the Icons (5:2) and Illustrations & Art (7:2) sections

Node ids in file names use '-' for ':' (22:55 -> 22-55). Every call goes through figma_rest.py, so the
token comes from ~/.config/paybak/figma_token and the 8-calls-per-minute limiter applies. A full run
is about 30 REST calls (a few minutes); the nodes step downloads roughly 50 MB.

Usage (from anywhere):
  python3 docs/design/tools/fetch_figma.py                 # everything, every page
  python3 docs/design/tools/fetch_figma.py --pages 3:5,77:101
                                                           # only these page ids (the structure,
                                                           # index and notes cover only them too)
  python3 docs/design/tools/fetch_figma.py --skip nodes,components,svg
                                                           # skip steps: nodes, renders, components, svg
  python3 docs/design/tools/fetch_figma.py --offline       # no API calls: rebuild index.json, INDEX.md
                                                           # and notes.md from the cached structure.json
  python3 docs/design/tools/fetch_figma.py --cache DIR     # write somewhere else than .figma-cache/
"""
import json
import os
import sys

TOOLS = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, TOOLS)
sys.dont_write_bytecode = True  # keep tools/ free of __pycache__
import figma_rest  # noqa: E402  (same folder)

DEFAULT_CACHE = os.path.join(os.path.dirname(TOOLS), ".figma-cache")
COMPONENTS_PAGE = "3:3"     # "02 Components"
ICONS_SECTION = "5:2"       # "Icons": SVG only
ART_SECTION = "7:2"         # "Illustrations & Art": SVG and PNG
FRAME_TYPES = ("FRAME", "COMPONENT", "COMPONENT_SET", "INSTANCE", "SECTION")
SECTIONS_PER_CALL = 8


def fname(i):
    return figma_rest.fname(i)


def is_components_page(pid, doc):
    return pid == COMPONENTS_PAGE or "components" in doc.get("name", "").lower()


def is_overlay_helper(node):
    return node["name"].startswith("↳") and "overlay" in node["name"].lower()


def sections(doc):
    """Top-level children of a page. A frame placed straight on the canvas becomes its own section."""
    for s in doc.get("children", []):
        if s["type"] == "SECTION":
            yield s, s.get("children", [])
        elif s["type"] in FRAME_TYPES:
            yield {"id": s["id"], "name": "(page)"}, [s]


def node_chunks(pid, doc):
    """[(file name, [top-level ids])] for the nodes step."""
    ids = [s["id"] for s in doc.get("children", []) if s["type"] in FRAME_TYPES]
    stem = pid.replace(":", "-")
    if len(ids) <= SECTIONS_PER_CALL:
        return [(stem + ".json", ids)] if ids else []
    return [(f"{stem}-{chr(ord('a') + k // SECTIONS_PER_CALL)}.json", ids[k:k + SECTIONS_PER_CALL])
            for k in range(0, len(ids), SECTIONS_PER_CALL)]


def build_index(st):
    """index rows plus the id lists for the render steps."""
    index, screens, comps, svgs = [], [], [], []
    for pid, n in st["nodes"].items():
        doc = n["document"]
        comp_page = is_components_page(pid, doc)
        chunk_of = {i: f for f, ids in node_chunks(pid, doc) for i in ids}
        for s, children in sections(doc):
            for c in children:
                if c["type"] not in FRAME_TYPES:
                    continue
                row = {"page": doc["name"], "pageId": pid, "section": s["name"], "sectionId": s["id"],
                       "id": c["id"], "name": c["name"], "type": c["type"]}
                paths = []
                if comp_page:
                    if s["id"] != ICONS_SECTION:
                        comps.append(c["id"])
                        paths.append(f"components/{fname(c['id'])}.png")
                    if s["id"] in (ICONS_SECTION, ART_SECTION):
                        svgs.append(c["id"])
                        if s["id"] == ICONS_SECTION:
                            paths.insert(0, "")
                        paths.append(f"svg/{fname(c['id'])}.svg")
                elif not is_overlay_helper(c):
                    screens.append(c["id"])
                    paths.append(f"renders/{fname(c['id'])}.png")
                row["files"] = " · ".join(paths)
                row["nodes"] = "nodes/" + chunk_of.get(s["id"], chunk_of.get(c["id"], "?"))
                index.append(row)
    return index, screens, comps, svgs


def write_index_md(index, path):
    lines = ["# Figma frame index (regenerated by tools/fetch_figma.py)", "",
             "Columns: page | section | frame/component name | node id | 2x render / SVG | node JSON "
             "(paths relative to .figma-cache/)", ""]
    for r in index:
        lines.append(f"- {r['page']} | {r['section']} | {r['name']} | `{r['id']}` | {r['files']} | {r['nodes']}")
    with open(path, "w") as f:
        f.write("\n".join(lines) + "\n")


def _box(n):
    b = n.get("absoluteBoundingBox") or {}
    return b.get("x", 0), b.get("y", 0), b.get("width", 0), b.get("height", 0)


def _owner(text, frames):
    tx, ty, _, _ = _box(text)
    best = None
    for f in frames:
        fx, fy, fw, fh = _box(f)
        gap = ty - (fy + fh)
        if abs(tx - fx) <= 10 and gap >= 0 and (best is None or gap < best[0]):
            best = (gap, f)
    return best[1] if best else None


def write_notes_md(st, path):
    lines = ["# Designer notes (verbatim), per page and section"]
    for pid, n in st["nodes"].items():
        for s, children in sections(n["document"]):
            texts = [c for c in children if c["type"] == "TEXT"]
            if not texts:
                continue
            frames = [c for c in children if c["type"] in FRAME_TYPES]
            lines += ["", f"## {s['name']} ({s['id']})"]
            for t in texts:
                f = _owner(t, frames)
                tag = f"under «{f['name']}» ({f['id']})" if f else "section note"
                lines.append(f"- [{tag}] {t.get('characters', '')}")
    with open(path, "w") as f:
        f.write("\n".join(lines) + "\n")


def main():
    a = sys.argv[1:]
    if "-h" in a or "--help" in a:
        print(__doc__)
        return

    def opt(name, default=None):
        return a[a.index(name) + 1] if name in a and a.index(name) + 1 < len(a) else default

    cache = os.path.abspath(opt("--cache", DEFAULT_CACHE))
    skip = set(filter(None, (opt("--skip", "") or "").split(",")))
    os.makedirs(cache, exist_ok=True)
    structure = os.path.join(cache, "structure.json")

    if "--offline" in a:
        with open(structure) as f:
            st = json.load(f)
    else:
        # 1) pages, then every selected page at depth 2
        tree = figma_rest.get_file(1)
        figma_rest.write_json(tree, os.path.join(cache, "file.json"))
        pages = [p["id"] for p in tree["document"].get("children", []) if p.get("type") == "CANVAS"]
        if opt("--pages"):
            wanted = opt("--pages").split(",")
            pages = [p for p in pages if p in wanted]
        if not pages:
            raise SystemExit("no pages selected")
        st = figma_rest.get_nodes(pages, depth=2)
        figma_rest.write_json(st, structure)

    index, screens, comps, svgs = build_index(st)
    with open(os.path.join(cache, "index.json"), "w") as f:
        json.dump(index, f, indent=1, ensure_ascii=False)
    write_index_md(index, os.path.join(cache, "INDEX.md"))
    write_notes_md(st, os.path.join(cache, "notes.md"))
    print("wrote index.json, INDEX.md, notes.md:", len(index), "frames")
    if "--offline" in a:
        return

    # 2) full node JSON per page (large pages split into chunks of sections)
    if "nodes" not in skip:
        os.makedirs(os.path.join(cache, "nodes"), exist_ok=True)
        for pid, n in st["nodes"].items():
            for name, ids in node_chunks(pid, n["document"]):
                figma_rest.write_json(figma_rest.get_nodes(ids), os.path.join(cache, "nodes", name))
    # 3) 2x renders of every screen frame and every component frame
    if "renders" not in skip and screens:
        figma_rest.render(screens, os.path.join(cache, "renders"), "png", "2")
    if "components" not in skip and comps:
        figma_rest.render(comps, os.path.join(cache, "components"), "png", "2")
    # 4) SVG exports of the icons and the illustrations/art
    if "svg" not in skip and svgs:
        figma_rest.render(svgs, os.path.join(cache, "svg"), "svg")
    print("DONE", len(screens), "screens,", len(comps), "component frames,", len(svgs), "svg ->", cache)


if __name__ == "__main__":
    main()
