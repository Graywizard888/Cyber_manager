#!/bin/bash

# Script to download third-party native libraries

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LIBS_DIR="$SCRIPT_DIR/app/src/main/cpp/third_party"

mkdir -p "$LIBS_DIR"
cd "$LIBS_DIR"

echo "Downloading MiniZip..."
git clone https://github.com/zlib-contrib/minizip.git

echo "Downloading UnRAR..."
wget https://www.rarlab.com/rar/unrarsrc-6.2.12.tar.gz
tar -xzf unrarsrc-6.2.12.tar.gz
mv unrar unrar_temp
mkdir -p unrar
mv unrar_temp/*.cpp unrar_temp/*.hpp unrar/
rm -rf unrar_temp unrarsrc-6.2.12.tar.gz

echo "Downloading LZMA SDK..."
wget https://www.7-zip.org/a/lzma2301.7z
7z x lzma2301.7z -olzma
rm lzma2301.7z

echo "Third-party libraries downloaded successfully!"
