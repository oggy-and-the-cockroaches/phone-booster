#ifndef CACHE_SCANNER_H
#define CACHE_SCANNER_H

#include <string>
#include <vector>
#include <cstdint>

enum class JunkType {
    APP_CACHE,
    TEMP_FILE,
    THUMBNAIL_CACHE,
    OBSOLETE_APK,
    LOG_FILE,
    EMPTY_FOLDER
};

struct JunkItem {
    std::string path;
    std::string name;
    uint64_t sizeBytes;
    JunkType type;
    std::string description;
};

struct JunkScanResult {
    uint64_t totalJunkBytes;
    uint64_t appCacheBytes;
    uint64_t tempFilesBytes;
    uint64_t thumbnailBytes;
    uint64_t apkJunkBytes;
    uint64_t logFilesBytes;
    std::vector<JunkItem> items;
};

class CacheScanner {
public:
    static JunkScanResult scanJunk(const std::vector<std::string>& searchPaths);
    static uint64_t cleanJunk(const std::vector<std::string>& pathsToDelete);
};

#endif // CACHE_SCANNER_H
