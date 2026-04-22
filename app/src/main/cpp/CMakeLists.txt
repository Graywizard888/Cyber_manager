cmake_minimum_required(VERSION 3.22.1)

project("archive_extractor")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra -O3")

# Include directories
include_directories(
    ${CMAKE_SOURCE_DIR}
    ${CMAKE_SOURCE_DIR}/third_party/minizip
    ${CMAKE_SOURCE_DIR}/third_party/unrar
    ${CMAKE_SOURCE_DIR}/third_party/lzma/C
)

# MiniZip sources for ZIP extraction
set(MINIZIP_SRC
    third_party/minizip/ioapi.c
    third_party/minizip/unzip.c
    third_party/minizip/zip.c
)

# UnRAR sources for RAR extraction
set(UNRAR_SRC
    third_party/unrar/rar.cpp
    third_party/unrar/strlist.cpp
    third_party/unrar/strfn.cpp
    third_party/unrar/pathfn.cpp
    third_party/unrar/smallfn.cpp
    third_party/unrar/global.cpp
    third_party/unrar/file.cpp
    third_party/unrar/filefn.cpp
    third_party/unrar/filcreat.cpp
    third_party/unrar/archive.cpp
    third_party/unrar/arcread.cpp
    third_party/unrar/unicode.cpp
    third_party/unrar/system.cpp
    third_party/unrar/isnt.cpp
    third_party/unrar/crypt.cpp
    third_party/unrar/crc.cpp
    third_party/unrar/rawread.cpp
    third_party/unrar/encname.cpp
    third_party/unrar/resource.cpp
    third_party/unrar/match.cpp
    third_party/unrar/timefn.cpp
    third_party/unrar/rdwrfn.cpp
    third_party/unrar/consio.cpp
    third_party/unrar/options.cpp
    third_party/unrar/errhnd.cpp
    third_party/unrar/rarvm.cpp
    third_party/unrar/secpassword.cpp
    third_party/unrar/rijndael.cpp
    third_party/unrar/getbits.cpp
    third_party/unrar/sha1.cpp
    third_party/unrar/sha256.cpp
    third_party/unrar/blake2s.cpp
    third_party/unrar/hash.cpp
    third_party/unrar/extinfo.cpp
    third_party/unrar/extract.cpp
    third_party/unrar/volume.cpp
    third_party/unrar/list.cpp
    third_party/unrar/find.cpp
    third_party/unrar/unpack.cpp
    third_party/unrar/headers.cpp
    third_party/unrar/threadpool.cpp
    third_party/unrar/rs16.cpp
    third_party/unrar/cmddata.cpp
    third_party/unrar/ui.cpp
)

# LZMA SDK sources for 7z extraction
set(LZMA_SRC
    third_party/lzma/C/7zAlloc.c
    third_party/lzma/C/7zArcIn.c
    third_party/lzma/C/7zBuf.c
    third_party/lzma/C/7zBuf2.c
    third_party/lzma/C/7zCrc.c
    third_party/lzma/C/7zCrcOpt.c
    third_party/lzma/C/7zDec.c
    third_party/lzma/C/7zFile.c
    third_party/lzma/C/7zStream.c
    third_party/lzma/C/Bcj2.c
    third_party/lzma/C/Bra.c
    third_party/lzma/C/Bra86.c
    third_party/lzma/C/BraIA64.c
    third_party/lzma/C/CpuArch.c
    third_party/lzma/C/Delta.c
    third_party/lzma/C/LzFind.c
    third_party/lzma/C/Lzma2Dec.c
    third_party/lzma/C/Lzma2Enc.c
    third_party/lzma/C/LzmaDec.c
    third_party/lzma/C/LzmaEnc.c
    third_party/lzma/C/Ppmd7.c
    third_party/lzma/C/Ppmd7Dec.c
    third_party/lzma/C/Ppmd7Enc.c
    third_party/lzma/C/Sha256.c
    third_party/lzma/C/Xz.c
    third_party/lzma/C/XzCrc64.c
    third_party/lzma/C/XzDec.c
    third_party/lzma/C/XzEnc.c
)

# Main library sources
set(MAIN_SRC
    archive_extractor.cpp
    zip_extractor.cpp
    rar_extractor.cpp
    seven_zip_extractor.cpp
    tar_extractor.cpp
    utils.cpp
)

# Create the library
add_library(${CMAKE_PROJECT_NAME} SHARED
    ${MAIN_SRC}
    ${MINIZIP_SRC}
    ${UNRAR_SRC}
    ${LZMA_SRC}
)

# Compiler definitions
target_compile_definitions(${CMAKE_PROJECT_NAME} PRIVATE
    _FILE_OFFSET_BITS=64
    _LARGEFILE64_SOURCE
    RARDLL
    SILENT
    _7ZIP_ST
)

# Link libraries
target_link_libraries(${CMAKE_PROJECT_NAME}
    android
    log
    z
)

# Strip symbols in release builds
if(CMAKE_BUILD_TYPE STREQUAL "Release")
    add_custom_command(TARGET ${CMAKE_PROJECT_NAME} POST_BUILD
        COMMAND ${CMAKE_STRIP} --strip-unneeded $<TARGET_FILE:${CMAKE_PROJECT_NAME}>
    )
endif()
