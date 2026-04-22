#ifndef SEVEN_ZIP_EXTRACTOR_H
#define SEVEN_ZIP_EXTRACTOR_H

#include "archive_extractor.h"

namespace archive {

class SevenZipExtractor {
public:
    static bool extract(const std::string& sevenZipPath,
                       const std::string& outputPath,
                       ProgressCallback callback = nullptr);
};

} // namespace archive

#endif // SEVEN_ZIP_EXTRACTOR_H
