#ifndef PROCESS_SCANNER_H
#define PROCESS_SCANNER_H

#include <vector>
#include <string>
#include <cstdint>

struct NativeProcessInfo {
    int32_t pid;
    std::string name;
    std::string packageName;
    uint64_t rssKb;
    int32_t threads;
    char state;
    bool isUserApp;
};

class ProcessScanner {
public:
    static std::vector<NativeProcessInfo> scanRunningProcesses();
    static bool killProcess(int32_t pid);
};

#endif // PROCESS_SCANNER_H
