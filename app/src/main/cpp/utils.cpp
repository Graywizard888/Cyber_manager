#include "utils.h"
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>
#include <errno.h>
#include <android/log.h>

#define LOG_TAG "Utils"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

namespace archive {

bool Utils::createDirectoryRecursive(const std::string& path) {
    if (path.empty()) {
        return false;
    }
    
    // Check if already exists
    struct stat st;
    if (stat(path.c_str(), &st) == 0) {
        return S_ISDIR(st.st_mode);
    }
    
    // Create parent directories
    size_t pos = 0;
    while ((pos = path.find('/', pos + 1)) != std::string::npos) {
        std::string subPath = path.substr(0, pos);
        if (!subPath.empty() && stat(subPath.c_str(), &st) != 0) {
            if (mkdir(subPath.c_str(), 0755) != 0 && errno != EEXIST) {
                LOGD("Failed to create directory: %s", subPath.c_str());
                return false;
            }
        }
    }
    
    // Create final directory
    if (mkdir(path.c_str(), 0755) != 0 && errno != EEXIST) {
        return false;
    }
    
    return true;
}

bool Utils::fileExists(const std::string& path) {
    struct stat st;
    return stat(path.c_str(), &st) == 0;
}

std::string Utils::getFileName(const std::string& path) {
    size_t lastSlash = path.find_last_of('/');
    if (lastSlash != std::string::npos) {
        return path.substr(lastSlash + 1);
    }
    return path;
}

std::string Utils::getFileExtension(const std::string& path) {
    size_t lastDot = path.find_last_of('.');
    if (lastDot != std::string::npos && lastDot < path.length() - 1) {
        return path.substr(lastDot + 1);
    }
    return "";
}

long Utils::getFileSize(const std::string& path) {
    struct stat st;
    if (stat(path.c_str(), &st) == 0) {
        return st.st_size;
    }
    return -1;
}

} // namespace archive
