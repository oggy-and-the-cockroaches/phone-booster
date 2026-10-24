#include "process_scanner.h"
#include <dirent.h>
#include <unistd.h>
#include <signal.h>
#include <fcntl.h>
#include <cstring>
#include <cstdlib>
#include <cstdio>
#include <cctype>

static bool isNumeric(const char* str) {
    if (!str || !*str) return false;
    while (*str) {
        if (!isdigit(*str)) return false;
        str++;
    }
    return true;
}

std::vector<NativeProcessInfo> ProcessScanner::scanRunningProcesses() {
    std::vector<NativeProcessInfo> list;
    DIR* dir = opendir("/proc");
    if (!dir) return list;

    struct dirent* entry;
    while ((entry = readdir(dir)) != nullptr) {
        if (entry->d_type != DT_DIR || !isNumeric(entry->d_name)) {
            continue;
        }

        int pid = atoi(entry->d_name);
        if (pid <= 0) continue;

        char path[256];
        // Read cmdline
        snprintf(path, sizeof(path), "/proc/%d/cmdline", pid);
        int fd = open(path, O_RDONLY);
        std::string cmdline;
        if (fd >= 0) {
            char buffer[512];
            ssize_t n = read(fd, buffer, sizeof(buffer) - 1);
            close(fd);
            if (n > 0) {
                buffer[n] = '\0';
                for (ssize_t i = 0; i < n; i++) {
                    if (buffer[i] == '\0' && i + 1 < n) {
                        buffer[i] = ' ';
                    }
                }
                cmdline = buffer;
            }
        }

        // Read status for VmRSS and Name
        snprintf(path, sizeof(path), "/proc/%d/status", pid);
        fd = open(path, O_RDONLY);
        std::string name;
        uint64_t rssKb = 0;
        int threads = 1;
        char state = '?';

        if (fd >= 0) {
            char buffer[1024];
            ssize_t n = read(fd, buffer, sizeof(buffer) - 1);
            close(fd);
            if (n > 0) {
                buffer[n] = '\0';
                char* line = strtok(buffer, "\n");
                while (line != nullptr) {
                    if (strncmp(line, "Name:", 5) == 0) {
                        char val[128];
                        if (sscanf(line, "Name:\t%127s", val) == 1) {
                            name = val;
                        }
                    } else if (strncmp(line, "State:", 6) == 0) {
                        sscanf(line, "State:\t%c", &state);
                    } else if (strncmp(line, "VmRSS:", 6) == 0) {
                        unsigned long long rss = 0;
                        if (sscanf(line, "VmRSS:\t%llu", &rss) == 1) {
                            rssKb = rss;
                        }
                    } else if (strncmp(line, "Threads:", 8) == 0) {
                        int th = 1;
                        if (sscanf(line, "Threads:\t%d", &th) == 1) {
                            threads = th;
                        }
                    }
                    line = strtok(nullptr, "\n");
                }
            }
        }

        if (name.empty()) {
            continue;
        }

        // Filter out typical kernel threads
        if (cmdline.empty() && (name[0] == '[' || rssKb == 0)) {
            continue;
        }

        std::string pkg = cmdline.empty() ? name : cmdline;
        size_t spacePos = pkg.find(' ');
        if (spacePos != std::string::npos) {
            pkg = pkg.substr(0, spacePos);
        }

        bool isUser = (pkg.find('.') != std::string::npos && pkg.find("system") == std::string::npos);

        NativeProcessInfo p;
        p.pid = pid;
        p.name = name;
        p.packageName = pkg;
        p.rssKb = rssKb;
        p.threads = threads;
        p.state = state;
        p.isUserApp = isUser;

        list.push_back(p);
    }
    closedir(dir);
    return list;
}

bool ProcessScanner::killProcess(int32_t pid) {
    if (pid <= 0) return false;
    return kill(pid, SIGTERM) == 0 || kill(pid, SIGKILL) == 0;
}
