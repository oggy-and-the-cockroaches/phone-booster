#include "storage_analyzer.h"
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
#include <algorithm>
#include <queue>
#include <cstring>
#include <cctype>

static std::string getExtension(const std::string& filename) {
    size_t dot = filename.find_last_of('.');
    if (dot == std::string::npos) return "";
    std::string ext = filename.substr(dot + 1);
    for (char& c : ext) c = tolower(c);
    return ext;
}

static bool isImage(const std::string& ext) {
    return ext == "jpg" || ext == "jpeg" || ext == "png" || ext == "webp" ||
           ext == "gif" || ext == "bmp" || ext == "heic" || ext == "dng";
}

static bool isVideo(const std::string& ext) {
    return ext == "mp4" || ext == "mkv" || ext == "mov" || ext == "avi" ||
           ext == "3gp" || ext == "webm" || ext == "flv" || ext == "wmv";
}

static bool isAudio(const std::string& ext) {
    return ext == "mp3" || ext == "wav" || ext == "flac" || ext == "aac" ||
           ext == "m4a" || ext == "ogg" || ext == "opus" || ext == "wma";
}

static bool isDocument(const std::string& ext) {
    return ext == "pdf" || ext == "doc" || ext == "docx" || ext == "xls" ||
           ext == "xlsx" || ext == "ppt" || ext == "pptx" || ext == "txt" ||
           ext == "csv" || ext == "epub";
}

static bool isArchive(const std::string& ext) {
    return ext == "zip" || ext == "rar" || ext == "7z" || ext == "tar" ||
           ext == "gz" || ext == "apk" || ext == "xapk" || ext == "iso";
}

struct LargeFileCompare {
    bool operator()(const LargeFileInfo& a, const LargeFileInfo& b) const {
        return a.sizeBytes > b.sizeBytes; // Min-heap
    }
};

static void scanDirRecursive(const std::string& dirPath,
                             StorageAnalysisResult& result,
                             std::priority_queue<LargeFileInfo, std::vector<LargeFileInfo>, LargeFileCompare>& heap,
                             size_t maxLargeFiles,
                             int depth) {
    if (depth > 25) return; // Prevent excessive recursion

    DIR* dir = opendir(dirPath.c_str());
    if (!dir) return;

    result.totalDirs++;
    struct dirent* entry;
    while ((entry = readdir(dir)) != nullptr) {
        if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) {
            continue;
        }

        std::string fullPath = dirPath + "/" + entry->d_name;
        struct stat st;
        if (lstat(fullPath.c_str(), &st) != 0) {
            continue;
        }

        if (S_ISDIR(st.st_mode)) {
            // Avoid recursion into system or hidden app data that might loop
            if (strcmp(entry->d_name, ".thumbnails") != 0) {
                scanDirRecursive(fullPath, result, heap, maxLargeFiles, depth + 1);
            }
        } else if (S_ISREG(st.st_mode)) {
            uint64_t size = (uint64_t)st.st_size;
            result.totalBytes += size;
            result.totalFiles++;

            std::string ext = getExtension(entry->d_name);
            if (isImage(ext)) result.imagesBytes += size;
            else if (isVideo(ext)) result.videosBytes += size;
            else if (isAudio(ext)) result.audioBytes += size;
            else if (isDocument(ext)) result.docsBytes += size;
            else if (isArchive(ext)) result.archivesBytes += size;
            else result.otherBytes += size;

            // Track largest files (only files > 500 KB)
            if (size > 512 * 1024) {
                LargeFileInfo item = {fullPath, entry->d_name, size, ext};
                if (heap.size() < maxLargeFiles) {
                    heap.push(item);
                } else if (size > heap.top().sizeBytes) {
                    heap.pop();
                    heap.push(item);
                }
            }
        }
    }
    closedir(dir);
}

StorageAnalysisResult StorageAnalyzer::analyzeDirectory(const std::string& rootPath, size_t maxLargeFiles) {
    StorageAnalysisResult result = {0, 0, 0, 0, 0, 0, 0, 0, 0, {}};
    std::priority_queue<LargeFileInfo, std::vector<LargeFileInfo>, LargeFileCompare> heap;

    scanDirRecursive(rootPath, result, heap, maxLargeFiles, 0);

    // Extract sorted large files
    while (!heap.empty()) {
        result.largestFiles.push_back(heap.top());
        heap.pop();
    }
    std::reverse(result.largestFiles.begin(), result.largestFiles.end());

    return result;
}

bool StorageAnalyzer::deleteFileNative(const std::string& path) {
    return unlink(path.c_str()) == 0;
}
