#include "tar_extractor.h"
#include "utils.h"
#include <android/log.h>
#include <fstream>
#include <sstream>
#include <zlib.h>
#include <sys/stat.h>

#define LOG_TAG "TarExtractor"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace archive {

struct TarHeader {
    char name[100];
    char mode[8];
    char uid[8];
    char gid[8];
    char size[12];
    char mtime[12];
    char checksum[8];
    char typeflag;
    char linkname[100];
    char magic[6];
    char version[2];
    char uname[32];
    char gname[32];
    char devmajor[8];
    char devminor[8];
    char prefix[155];
    char padding[12];
};

static long parseOctal(const char* str, size_t len) {
    long result = 0;
    for (size_t i = 0; i < len && str[i] != '\0' && str[i] != ' '; i++) {
        result = result * 8 + (str[i] - '0');
    }
    return result;
}

bool TarExtractor::extractTarStream(std::istream& stream,
                                    const std::string& outputPath,
                                    ProgressCallback callback) {
    Utils::createDirectoryRecursive(outputPath);
    
    TarHeader header;
    int fileCount = 0;
    
    while (stream.read(reinterpret_cast<char*>(&header), sizeof(TarHeader))) {
        // Check for end of archive
        if (header.name[0] == '\0') {
            break;
        }
        
        std::string fileName = header.name;
        if (header.prefix[0] != '\0') {
            fileName = std::string(header.prefix) + "/" + fileName;
        }
        
        std::string fullPath = outputPath + "/" + fileName;
        
        long fileSize = parseOctal(header.size, sizeof(header.size));
        long mode = parseOctal(header.mode, sizeof(header.mode));
        
        LOGD("Extracting: %s (size: %ld)", fileName.c_str(), fileSize);
        
        switch (header.typeflag) {
            case '0':  // Regular file
            case '\0': // Old regular file
            {
                // Create parent directory
                size_t lastSlash = fullPath.find_last_of('/');
                if (lastSlash != std::string::npos) {
                    Utils::createDirectoryRecursive(fullPath.substr(0, lastSlash));
                }
                
                // Extract file
                std::ofstream outFile(fullPath, std::ios::binary);
                if (outFile.is_open()) {
                    char buffer[512];
                    long remaining = fileSize;
                    
                    while (remaining > 0) {
                        long toRead = std::min(remaining, 512L);
                        stream.read(buffer, 512);
                        outFile.write(buffer, toRead);
                        remaining -= toRead;
                    }
                    
                    outFile.close();
                    chmod(fullPath.c_str(), mode);
                    
                    fileCount++;
                    if (callback) {
                        callback(0, fileName);  // Progress can't be determined without file count
                    }
                } else {
                    LOGE("Failed to create file: %s", fullPath.c_str());
                    // Skip file data
                    stream.seekg((fileSize + 511) & ~511, std::ios::cur);
                }
                break;
            }
            
            case '5':  // Directory
            {
                Utils::createDirectoryRecursive(fullPath);
                break;
            }
            
            case '2':  // Symbolic link
            case '1':  // Hard link
            {
                // Skip for now
                LOGD("Skipping link: %s", fileName.c_str());
                break;
            }
            
            default:
            {
                LOGD("Unknown type flag: %c for %s", header.typeflag, fileName.c_str());
                // Skip file data
                if (fileSize > 0) {
                    stream.seekg((fileSize + 511) & ~511, std::ios::cur);
                }
                break;
            }
        }
    }
    
    return true;
}

bool TarExtractor::extract(const std::string& tarPath,
                          const std::string& outputPath,
                          ProgressCallback callback) {
    LOGD("Extracting TAR: %s", tarPath.c_str());
    
    std::ifstream file(tarPath, std::ios::binary);
    if (!file.is_open()) {
        LOGE("Failed to open TAR file");
        return false;
    }
    
    bool result = extractTarStream(file, outputPath, callback);
    file.close();
    
    return result;
}

bool TarExtractor::extractGz(const std::string& gzPath,
                            const std::string& outputPath,
                            ProgressCallback callback) {
    LOGD("Extracting GZ: %s", gzPath.c_str());
    
    gzFile gzf = gzopen(gzPath.c_str(), "rb");
    if (gzf == nullptr) {
        LOGE("Failed to open GZ file");
        return false;
    }
    
    // Decompress to memory stream
    std::stringstream memStream(std::ios::binary | std::ios::in | std::ios::out);
    char buffer[8192];
    int bytesRead;
    
    while ((bytesRead = gzread(gzf, buffer, sizeof(buffer))) > 0) {
        memStream.write(buffer, bytesRead);
    }
    
    gzclose(gzf);
    
    // Check if it's a tar.gz
    memStream.seekg(0);
    TarHeader header;
    memStream.read(reinterpret_cast<char*>(&header), sizeof(TarHeader));
    
    if (header.magic[0] == 'u' && header.magic[1] == 's' &&
        header.magic[2] == 't' && header.magic[3] == 'a' && header.magic[4] == 'r') {
        // It's a tar.gz, extract as TAR
        memStream.seekg(0);
        return extractTarStream(memStream, outputPath, callback);
    } else {
        // Plain GZ file, write decompressed content
        Utils::createDirectoryRecursive(outputPath);
        
        std::string fileName = gzPath.substr(gzPath.find_last_of('/') + 1);
        if (fileName.size() > 3 && fileName.substr(fileName.size() - 3) == ".gz") {
            fileName = fileName.substr(0, fileName.size() - 3);
        }
        
        std::string outputFile = outputPath + "/" + fileName;
        std::ofstream outFile(outputFile, std::ios::binary);
        
        if (outFile.is_open()) {
            memStream.seekg(0);
            outFile << memStream.rdbuf();
            outFile.close();
            
            if (callback) {
                callback(100, fileName);
            }
            
            return true;
        }
    }
    
    return false;
}

bool TarExtractor::extractBz2(const std::string& bz2Path,
                             const std::string& outputPath,
                             ProgressCallback callback) {
    // For BZ2 support, you would use libbz2
    // Similar implementation to extractGz but using BZ2 functions
    LOGE("BZ2 extraction not yet implemented");
    return false;
}

bool TarExtractor::extractXz(const std::string& xzPath,
                            const std::string& outputPath,
                            ProgressCallback callback) {
    // For XZ support, you would use liblzma
    // Similar implementation to extractGz but using XZ functions
    LOGE("XZ extraction not yet implemented");
    return false;
}

} // namespace archive
