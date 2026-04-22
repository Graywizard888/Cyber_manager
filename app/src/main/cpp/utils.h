#ifndef UTILS_H
#define UTILS_H

#include <string>

namespace archive {

class Utils {
public:
    static bool createDirectoryRecursive(const std::string& path);
    static bool fileExists(const std::string& path);
    static std::string getFileName(const std::string& path);
    static std::string getFileExtension(const std::string& path);
    static long getFileSize(const std::string& path);
};

} // namespace archive

#endif // UTILS_H
