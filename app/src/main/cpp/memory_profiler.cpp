#include "memory_profiler.h"
#include <fcntl.h>
#include <unistd.h>
#include <cstring>
#include <cstdlib>
#include <cstdio>

NativeMemInfo MemoryProfiler::getMemoryInfo() {
    NativeMemInfo info = {0, 0, 0, 0, 0, 0, 0, 0.0};
    int fd = open("/proc/meminfo", O_RDONLY);
    if (fd < 0) {
        return info;
    }

    char buffer[2048];
    ssize_t bytesRead = read(fd, buffer, sizeof(buffer) - 1);
    close(fd);

    if (bytesRead <= 0) {
        return info;
    }
    buffer[bytesRead] = '\0';

    char line[256];
    char* ptr = buffer;
    while (*ptr != '\0') {
        char* nextLine = strchr(ptr, '\n');
        size_t len = nextLine ? (size_t)(nextLine - ptr) : strlen(ptr);
        if (len >= sizeof(line)) len = sizeof(line) - 1;
        strncpy(line, ptr, len);
        line[len] = '\0';

        char key[64];
        uint64_t val = 0;
        if (sscanf(line, "%63[^:]: %llu", key, (unsigned long long*)&val) == 2) {
            if (strcmp(key, "MemTotal") == 0) info.totalMemKb = val;
            else if (strcmp(key, "MemFree") == 0) info.freeMemKb = val;
            else if (strcmp(key, "MemAvailable") == 0) info.availableMemKb = val;
            else if (strcmp(key, "Buffers") == 0) info.buffersMemKb = val;
            else if (strcmp(key, "Cached") == 0) info.cachedMemKb = val;
            else if (strcmp(key, "SwapTotal") == 0) info.swapTotalKb = val;
            else if (strcmp(key, "SwapFree") == 0) info.swapFreeKb = val;
        }

        if (!nextLine) break;
        ptr = nextLine + 1;
    }

    if (info.availableMemKb == 0 && info.totalMemKb > 0) {
        // Fallback for older kernels without MemAvailable
        info.availableMemKb = info.freeMemKb + info.buffersMemKb + info.cachedMemKb;
    }

    if (info.totalMemKb > 0) {
        uint64_t used = info.totalMemKb - info.availableMemKb;
        info.ramUsagePercent = (double)used * 100.0 / (double)info.totalMemKb;
    }

    return info;
}

double MemoryProfiler::getCpuUsage() {
    int fd = open("/proc/stat", O_RDONLY);
    if (fd < 0) return 0.0;

    char buffer[512];
    ssize_t bytesRead = read(fd, buffer, sizeof(buffer) - 1);
    close(fd);

    if (bytesRead <= 0) return 0.0;
    buffer[bytesRead] = '\0';

    unsigned long long user, nice, system, idle, iowait, irq, softirq, steal;
    if (sscanf(buffer, "cpu %llu %llu %llu %llu %llu %llu %llu %llu",
               &user, &nice, &system, &idle, &iowait, &irq, &softirq, &steal) >= 4) {
        unsigned long long idleTotal = idle + iowait;
        unsigned long long nonIdle = user + nice + system + irq + softirq + steal;
        unsigned long long total = idleTotal + nonIdle;
        if (total > 0) {
            return (double)nonIdle * 100.0 / (double)total;
        }
    }
    return 0.0;
}
