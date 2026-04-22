#ifndef RAR_EXTRACTOR_H
#define RAR_EXTRACTOR_H

#include "archive_extractor.h"

namespace archive {

class RarExtractor {
public:
    static bool extract(const std::string& rarPath,
                       const std::string& outputPath,
                       ProgressCallback callback = nullptr);
};

} // namespace archive

#endif // RAR_EXTRACTOR_H
