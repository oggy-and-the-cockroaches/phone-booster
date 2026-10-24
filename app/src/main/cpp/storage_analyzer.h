#ifndef STORAGE_ANALYZER_H
#define STORAGE_ANALYZER_H

#include <string>
#include <vector>
#include <cstdint>

struct LargeFileInfo {
    std::string path;
    std::string name;
    uint64_t sizeBytes;
    std::string extension;
};

struct StorageAnalysisResult {
    uint64_t totalBytes;
    uint64_t imagesBytes;
    uint64_t videosBytes;
    uint64_t audioBytes;
    uint64_t docsBytes;
    uint64_t archivesBytes;
    uint64_t otherBytes;
    uint64_t totalFiles;
    uint64_t totalDirs;
    std::vector<LargeFileInfo> largestFiles;
};

class StorageAnalyzer {
public:
    static StorageAnalysisResult analyzeDirectory(const std::string& rootPath, size_t maxLargeFiles = 25);
    static bool deleteFileNative(const std::string& path);
};

#endif // STORAGE_ANALYZER_H
