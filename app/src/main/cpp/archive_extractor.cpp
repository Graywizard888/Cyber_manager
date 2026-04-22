#include "archive_extractor.h"
#include "zip_extractor.h"
#include "rar_extractor.h"
#include "seven_zip_extractor.h"
#include "tar_extractor.h"
#include "utils.h"
#include <fstream>
#include <android/log.h>

#define LOG_TAG "ArchiveExtractor"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace archive {

ArchiveType ArchiveExtractor::detectArchiveType(const std::string& filePath) {
    std::ifstream file(filePath, std::ios::binary);
    if (!file.is_open()) {
        return ArchiveType::UNKNOWN;
    }
    
    unsigned char header[16];
    file.read(reinterpret_cast<char*>(header), sizeof(header));
    file.close();
    
    // ZIP signature: 50 4B 03 04 or 50 4B 05 06 or 50 4B 07 08
    if (header[0] == 0x50 && header[1] == 0x4B) {
        if ((header[2] == 0x03 && header[3] == 0x04) ||
            (header[2] == 0x05 && header[3] == 0x06) ||
            (header[2] == 0x07 && header[3] == 0x08)) {
            return ArchiveType::ZIP;
        }
    }
    
    // RAR signature: 52 61 72 21 1A 07
    if (header[0] == 0x52 && header[1] == 0x61 && header[2] == 0x72 &&
        header[3] == 0x21 && header[4] == 0x1A && header[5] == 0x07) {
        return ArchiveType::RAR;
    }
    
    // 7z signature: 37 7A BC AF 27 1C
    if (header[0] == 0x37 && header[1] == 0x7A && header[2] == 0xBC &&
        header[3] == 0xAF && header[4] == 0x27 && header[5] == 0x1C) {
        return ArchiveType::SEVEN_ZIP;
    }
    
    // GZIP signature: 1F 8B
    if (header[0] == 0x1F && header[1] == 0x8B) {
        return ArchiveType::GZ;
    }
    
    // BZ2 signature: 42 5A 68
    if (header[0] == 0x42 && header[1] == 0x5A && header[2] == 0x68) {
        return ArchiveType::BZ2;
    }
    
    // XZ signature: FD 37 7A 58 5A 00
    if (header[0] == 0xFD && header[1] == 0x37 && header[2] == 0x7A &&
        header[3] == 0x58 && header[4] == 0x5A && header[5] == 0x00) {
        return ArchiveType::XZ;
    }
    
    // TAR (check for "ustar" at offset 257)
    file.open(filePath, std::ios::binary);
    file.seekg(257);
    char tarHeader[5];
    file.read(tarHeader, 5);
    file.close();
    if (std::string(tarHeader, 5) == "ustar") {
        return ArchiveType::TAR;
    }
    
    return ArchiveType::UNKNOWN;
}

bool ArchiveExtractor::extractArchive(const std::string& archivePath,
                                      const std::string& outputPath,
                                      ProgressCallback callback) {
    LOGD("Extracting archive: %s to %s", archivePath.c_str(), outputPath.c_str());
    
    ArchiveType type = detectArchiveType(archivePath);
    
    bool success = false;
    
    switch (type) {
        case ArchiveType::ZIP:
        case ArchiveType::APK:
            LOGD("Detected ZIP/APK archive");
            success = ZipExtractor::extract(archivePath, outputPath, callback);
            break;
            
        case ArchiveType::RAR:
            LOGD("Detected RAR archive");
            success = RarExtractor::extract(archivePath, outputPath, callback);
            break;
            
        case ArchiveType::SEVEN_ZIP:
            LOGD("Detected 7z archive");
            success = SevenZipExtractor::extract(archivePath, outputPath, callback);
            break;
            
        case ArchiveType::TAR:
            LOGD("Detected TAR archive");
            success = TarExtractor::extract(archivePath, outputPath, callback);
            break;
            
        case ArchiveType::GZ:
            LOGD("Detected GZ archive");
            success = TarExtractor::extractGz(archivePath, outputPath, callback);
            break;
            
        case ArchiveType::BZ2:
            LOGD("Detected BZ2 archive");
            success = TarExtractor::extractBz2(archivePath, outputPath, callback);
            break;
            
        case ArchiveType::XZ:
            LOGD("Detected XZ archive");
            success = TarExtractor::extractXz(archivePath, outputPath, callback);
            break;
            
        default:
            LOGE("Unknown or unsupported archive type");
            success = false;
            break;
    }
    
    if (success) {
        LOGD("Archive extraction completed successfully");
    } else {
        LOGE("Archive extraction failed");
    }
    
    return success;
}

} // namespace archive

// JNI Interface
extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_graywizard_filemanager_utils_ArchiveExtractor_extractNative(
    JNIEnv *env,
    jobject thiz,
    jstring archivePath,
    jstring outputPath) {
    
    const char *archivePathStr = env->GetStringUTFChars(archivePath, nullptr);
    const char *outputPathStr = env->GetStringUTFChars(outputPath, nullptr);
    
    bool success = archive::ArchiveExtractor::extractArchive(
        archivePathStr,
        outputPathStr,
        [env, thiz](int progress, const std::string& currentFile) {
            // Call Java callback method if needed
            jclass cls = env->GetObjectClass(thiz);
            jmethodID mid = env->GetMethodID(cls, "onExtractionProgress", "(ILjava/lang/String;)V");
            if (mid != nullptr) {
                jstring jCurrentFile = env->NewStringUTF(currentFile.c_str());
                env->CallVoidMethod(thiz, mid, progress, jCurrentFile);
                env->DeleteLocalRef(jCurrentFile);
            }
        }
    );
    
    env->ReleaseStringUTFChars(archivePath, archivePathStr);
    env->ReleaseStringUTFChars(outputPath, outputPathStr);
    
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL
Java_com_graywizard_filemanager_utils_ArchiveExtractor_getArchiveType(
    JNIEnv *env,
    jobject /* thiz */,
    jstring archivePath) {
    
    const char *archivePathStr = env->GetStringUTFChars(archivePath, nullptr);
    
    archive::ArchiveType type = archive::ArchiveExtractor::detectArchiveType(archivePathStr);
    
    env->ReleaseStringUTFChars(archivePath, archivePathStr);
    
    std::string typeStr;
    switch (type) {
        case archive::ArchiveType::ZIP: typeStr = "ZIP"; break;
        case archive::ArchiveType::RAR: typeStr = "RAR"; break;
        case archive::ArchiveType::SEVEN_ZIP: typeStr = "7Z"; break;
        case archive::ArchiveType::TAR: typeStr = "TAR"; break;
        case archive::ArchiveType::GZ: typeStr = "GZ"; break;
        case archive::ArchiveType::BZ2: typeStr = "BZ2"; break;
        case archive::ArchiveType::XZ: typeStr = "XZ"; break;
        default: typeStr = "UNKNOWN"; break;
    }
    
    return env->NewStringUTF(typeStr.c_str());
}

} // extern "C"
