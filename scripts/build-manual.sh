#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:-dev}"
DATE="$(date +%Y-%m-%d)"
TYPST_VERSION="${TYPST_VERSION:-0.15.1}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CACHE_DIR="$REPO_ROOT/.cache/typst"
FONT_DIR="$REPO_ROOT/.cache/fonts/arimo"
OUTPUT_DIR="$REPO_ROOT/target"
MANUAL_DIR="$REPO_ROOT/docs/manual"
WIKI_DIR="$OUTPUT_DIR/manual-wiki"

SNAP=""
[[ "$VERSION" == *"-SNAPSHOT"* ]] && SNAP="-SNAPSHOT"
PDF_NAME="XPman-User-Manual${SNAP}.pdf"

case "$(uname -s)-$(uname -m)" in
    Linux-x86_64)  TYPST_ASSET="typst-x86_64-unknown-linux-musl" ;;
    Darwin-arm64)  TYPST_ASSET="typst-aarch64-apple-darwin" ;;
    Darwin-x86_64) TYPST_ASSET="typst-x86_64-apple-darwin" ;;
    *) echo "Unsupported platform: $(uname -s) $(uname -m)" >&2; exit 1 ;;
esac

TYPST_BIN="$CACHE_DIR/typst-${TYPST_VERSION}/typst"
if [[ ! -x "$TYPST_BIN" ]]; then
    mkdir -p "$CACHE_DIR"
    URL="https://github.com/typst/typst/releases/download/v${TYPST_VERSION}/${TYPST_ASSET}.tar.xz"
    echo "Downloading Typst v${TYPST_VERSION} (${TYPST_ASSET})..."
    curl -fsSL "$URL" | tar -xJ -C "$CACHE_DIR"
    mv "$CACHE_DIR/$TYPST_ASSET" "$CACHE_DIR/typst-${TYPST_VERSION}"
    chmod +x "$TYPST_BIN"
fi
export PATH="$(dirname "$TYPST_BIN"):$PATH"

ARIMO_URL="https://raw.githubusercontent.com/google/fonts/main/ofl/arimo"
if [[ ! -f "$FONT_DIR/Arimo[wght].ttf" ]]; then
    mkdir -p "$FONT_DIR"
    echo "Downloading Arimo fonts..."
    curl -fsSL "$ARIMO_URL/Arimo%5Bwght%5D.ttf" -o "$FONT_DIR/Arimo[wght].ttf"
    curl -fsSL "$ARIMO_URL/Arimo-Italic%5Bwght%5D.ttf" -o "$FONT_DIR/Arimo-Italic[wght].ttf"
fi

mkdir -p "$OUTPUT_DIR"
echo "Building PDF: $OUTPUT_DIR/$PDF_NAME"
pandoc \
    --pdf-engine=typst \
    --pdf-engine-opt="--font-path=$FONT_DIR" \
    --toc \
    --toc-depth=2 \
    --resource-path="$REPO_ROOT" \
    -V "mainfont=Arimo" \
    -M "title=XPman User Manual" \
    -M "subtitle=Version ${VERSION} (${DATE})" \
    -M "author=The XPman Team" \
    -o "$OUTPUT_DIR/$PDF_NAME" \
    "$MANUAL_DIR"/[0-9]*.md

echo "PDF built: $OUTPUT_DIR/$PDF_NAME"

rm -rf "$WIKI_DIR"
mkdir -p "$WIKI_DIR"

for f in "$MANUAL_DIR"/[0-9]*.md; do
    base="$(basename "$f")"
    stripped="${base#[0-9][0-9]-}"
    if [[ "$base" == "00-introduction.md" ]]; then
        stripped="Home.md"
    fi
    sed "s|(assets/|(https://raw.githubusercontent.com/ogerardin/xpman/main/assets/|g" \
        "$f" > "$WIKI_DIR/$stripped"
done

{
    echo "# XPman User Manual"
    echo ""
    echo "- [Home](Home)"
    for f in "$MANUAL_DIR"/[0-9]*.md; do
        base="$(basename "$f")"
        [[ "$base" == "00-introduction.md" ]] && continue
        stripped="${base#[0-9][0-9]-}"
        title="${stripped%.md}"
        display=$(echo "$title" | sed 's/-/ /g' | awk '{for(i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) substr($i,2)}1')
        echo "- [${display}](${title})"
    done
} > "$WIKI_DIR/_Sidebar.md"

echo "Wiki tree built: $WIKI_DIR"
echo "Done."
