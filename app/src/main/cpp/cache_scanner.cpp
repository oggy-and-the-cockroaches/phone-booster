#include "cache_scanner.h"
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
#include <cstring>
#include <cctype>

static std::string getExt(const std::string& name) {
    size_t dot = name.find_last_of('.');
    if (dot == std::string::npos) return "";
    std::string ext = name.substr(dot + 1);
    for (char& c : ext) c = tolower(c);
    return ext;
}

static uint64_t getDirSize(const std::string& dirPath, int depth) {
    if (depth > 10) return 0;
    DIR* dir = opendir(dirPath.c_str());
    if (!dir) return 0;

    uint64_t total = 0;
    struct dirent* entry;
    while ((entry = readdir(dir)) != nullptr) {
        if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) continue;
        std::string full = dirPath + "/" + entry->d_name;
        struct stat st;
        if (lstat(full.c_str(), &st) != 0) continue;

        if (S_ISDIR(st.st_mode)) {
            total += getDirSize(full, depth + 1);
        } else if (S_ISREG(st.st_mode)) {
            total += (uint64_t)st.st_size;
        }
    }
    closedir(dir);
    return total;
}

static void scanDirectoryForJunk(const std::string& currentPath, JunkScanResult& result, int depth) {
    if (depth > 15) return;
    DIR* dir = opendir(currentPath.c_str());
    if (!dir) return;

    struct dirent* entry;
    while ((entry = readdir(dir)) != nullptr) {
        if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) continue;
        std::string name = entry->d_name;
        std::string fullPath = currentPath + "/" + name;

        struct stat st;
        if (lstat(fullPath.c_str(), &st) != 0) continue;

        if (S_ISDIR(st.st_mode)) {
            if (name == "cache" || name == "CodeCache" || name == ".cache") {
                uint64_t size = getDirSize(fullPath, 0);
                if (size > 0) {
                    result.appCacheBytes += size;
                    result.totalJunkBytes += size;
                    result.items.push_back({fullPath, name, size, JunkType::APP_CACHE, "Application Cache Folder"});
                }
            } else if (name == ".thumbnails" || name == "thumbnails") {
                uint64_t size = getDirSize(fullPath, 0);
                if (size > 0) {
                    result.thumbnailBytes += size;
                    result.totalJunkBytes += size;
                    result.items.push_back({fullPath, name, size, JunkType::THUMBNAIL_CACHE, "Gallery Thumbnails"});
                }
            } else {
                scanDirectoryForJunk(fullPath, result, depth + 1);
            }
        } else if (S_ISREG(st.st_mode)) {
            uint64_t size = (uint64_t)st.st_size;
            std::string ext = getExt(name);

            if (ext == "tmp" || ext == "temp" || ext == "bak" || ext == "swp") {
                result.tempFilesBytes += size;
                result.totalJunkBytes += size;
                result.items.push_back({fullPath, name, size, JunkType::TEMP_FILE, "Temporary File"});
            } else if (ext == "log") {
                result.logFilesBytes += size;
                result.totalJunkBytes += size;
                result.items.push_back({fullPath, name, size, JunkType::LOG_FILE, "System / App Log File"});
            } else if (ext == "thumb" || name.rfind(".thumbdata", 0) == 0) {
                result.thumbnailBytes += size;
                result.totalJunkBytes += size;
                result.items.push_back({fullPath, name, size, JunkType::THUMBNAIL_CACHE, "Thumbnail Database"});
            } else if (ext == "apk" && currentPath.find("Download") != std::string::npos) {
                result.apkJunkBytes += size;
                result.totalJunkBytes += size;
                result.items.push_back({fullPath, name, size, JunkType::OBSOLETE_APK, "Downloaded APK Package"});
            }
        }
    }
    closedir(dir);
}

JunkScanResult CacheScanner::scanJunk(const std::vector<std::string>& searchPaths) {
    JunkScanResult result = {0, 0, 0, 0, 0, 0, {}};
    for (const auto& path : searchPaths) {
        scanDirectoryForJunk(path, result, 0);
    }
    return result;
}

static uint64_t removeRecursive(const std::string& path) {
    struct stat st;
    if (lstat(path.c_str(), &st) != 0) return 0;

    if (S_ISDIR(st.st_mode)) {
        DIR* dir = opendir(path.c_str());
        if (!dir) return 0;
        uint64_t freed = 0;
        struct dirent* entry;
        while ((entry = readdir(dir)) != nullptr) {
            if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) continue;
            freed += removeRecursive(path + "/" + entry->d_name);
        }
        closedir(dir);
        rmdir(path.c_str());
        return freed;
    } else {
        uint64_t size = (uint64_t)st.st_size;
        if (unlink(path.c_str()) == 0) {
            return size;
        }
    }
    return 0;
}

uint64_t CacheScanner::cleanJunk(const std::vector<std::string>& pathsToDelete) {
    uint64_t totalFreed = 0;
    for (const auto& path : pathsToDelete) {
        totalFreed += removeRecursive(path);
    }
    return totalFreed;
}
