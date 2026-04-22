#ifndef ARCHIVE_EXTRACTOR_H
#define ARCHIVE_EXTRACTOR_H

#include <jni.h>
#include <string>
#include <functional>

namespace archive {

enum class ArchiveType {
    ZIP,
    RAR,
    SEVEN_ZIP,
    TAR,
    GZ,
    BZ2,
    XZ,
    APK,
    UNKNOWN
};

using ProgressCallback = std::function<void(int progress, const std::string& currentFile)>;

class ArchiveExtractor {
public:
    static ArchiveType detectArchiveType(const std::string& filePath);
    static bool extractArchive(const std::string& archivePath, 
                              const std::string& outputPath,
                              ProgressCallback callback = nullptr);
    
private:
    static bool isZipFile(const std::string& filePath);
    static bool isRarFile(const std::string& filePath);
    static bool is7zFile(const std::string& filePath);
    static bool isTarFile(const std::string& filePath);
    static bool isGzFile(const std::string& filePath);
};

} // namespace archive

#endif // ARCHIVE_EXTRACTOR_H
