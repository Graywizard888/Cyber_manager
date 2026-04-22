#ifndef ZIP_EXTRACTOR_H
#define ZIP_EXTRACTOR_H

#include "archive_extractor.h"

namespace archive {

class ZipExtractor {
public:
    static bool extract(const std::string& zipPath,
                       const std::string& outputPath,
                       ProgressCallback callback = nullptr);
                       
private:
    static bool createDirectoryRecursive(const std::string& path);
    static bool extractFile(void* unzFile, const std::string& outputPath);
};

} // namespace archive

#endif // ZIP_EXTRACTOR_H
