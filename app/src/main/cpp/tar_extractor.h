#ifndef TAR_EXTRACTOR_H
#define TAR_EXTRACTOR_H

#include "archive_extractor.h"

namespace archive {

class TarExtractor {
public:
    static bool extract(const std::string& tarPath,
                       const std::string& outputPath,
                       ProgressCallback callback = nullptr);
                       
    static bool extractGz(const std::string& gzPath,
                         const std::string& outputPath,
                         ProgressCallback callback = nullptr);
                         
    static bool extractBz2(const std::string& bz2Path,
                          const std::string& outputPath,
                          ProgressCallback callback = nullptr);
                          
    static bool extractXz(const std::string& xzPath,
                         const std::string& outputPath,
                         ProgressCallback callback = nullptr);
private:
    static bool extractTarStream(std::istream& stream,
                                 const std::string& outputPath,
                                 ProgressCallback callback);
};

} // namespace archive

#endif // TAR_EXTRACTOR_H
