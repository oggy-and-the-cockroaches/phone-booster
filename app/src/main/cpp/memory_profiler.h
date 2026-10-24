#ifndef MEMORY_PROFILER_H
#define MEMORY_PROFILER_H

#include <string>
#include <cstdint>

struct NativeMemInfo {
    uint64_t totalMemKb;
    uint64_t freeMemKb;
    uint64_t availableMemKb;
    uint64_t buffersMemKb;
    uint64_t cachedMemKb;
    uint64_t swapTotalKb;
    uint64_t swapFreeKb;
    double ramUsagePercent;
};

class MemoryProfiler {
public:
    static NativeMemInfo getMemoryInfo();
    static double getCpuUsage();
};

#endif // MEMORY_PROFILER_H
