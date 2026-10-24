#include <jni.h>
#include <string>
#include <sstream>
#include <vector>
#include "memory_profiler.h"
#include "process_scanner.h"
#include "storage_analyzer.h"
#include "cache_scanner.h"

static std::string escapeJson(const std::string& s) {
    std::ostringstream o;
    for (char c : s) {
        switch (c) {
            case '"': o << "\\\""; break;
            case '\\': o << "\\\\"; break;
            case '\b': o << "\\b"; break;
            case '\f': o << "\\f"; break;
            case '\n': o << "\\n"; break;
            case '\r': o << "\\r"; break;
            case '\t': o << "\\t"; break;
            default:
                if ('\x00' <= c && c <= '\x1f') {
                    // Control character
                } else {
                    o << c;
                }
        }
    }
    return o.str();
}

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_example_myapplication_data_NativeBridge_isNativeActive(JNIEnv *env, jobject thiz) {
    return JNI_TRUE;
}

JNIEXPORT jlongArray JNICALL
Java_com_example_myapplication_data_NativeBridge_getNativeMemInfo(JNIEnv *env, jobject thiz) {
    NativeMemInfo info = MemoryProfiler::getMemoryInfo();
    jlong values[8];
    values[0] = (jlong)info.totalMemKb;
    values[1] = (jlong)info.freeMemKb;
    values[2] = (jlong)info.availableMemKb;
    values[3] = (jlong)info.buffersMemKb;
    values[4] = (jlong)info.cachedMemKb;
    values[5] = (jlong)info.swapTotalKb;
    values[6] = (jlong)info.swapFreeKb;
    values[7] = (jlong)(info.ramUsagePercent * 100.0);

    jlongArray result = env->NewLongArray(8);
    env->SetLongArrayRegion(result, 0, 8, values);
    return result;
}

JNIEXPORT jdouble JNICALL
Java_com_example_myapplication_data_NativeBridge_getCpuUsagePercent(JNIEnv *env, jobject thiz) {
    return (jdouble)MemoryProfiler::getCpuUsage();
}

JNIEXPORT jstring JNICALL
Java_com_example_myapplication_data_NativeBridge_scanProcessesNative(JNIEnv *env, jobject thiz) {
    std::vector<NativeProcessInfo> processes = ProcessScanner::scanRunningProcesses();
    std::ostringstream ss;
    ss << "[";
    for (size_t i = 0; i < processes.size(); ++i) {
        const auto& p = processes[i];
        if (i > 0) ss << ",";
        ss << "{"
           << "\"pid\":" << p.pid << ","
           << "\"name\":\"" << escapeJson(p.name) << "\","
           << "\"packageName\":\"" << escapeJson(p.packageName) << "\","
           << "\"rssKb\":" << p.rssKb << ","
           << "\"threads\":" << p.threads << ","
           << "\"state\":\"" << p.state << "\","
           << "\"isUserApp\":" << (p.isUserApp ? "true" : "false")
           << "}";
    }
    ss << "]";
    return env->NewStringUTF(ss.str().c_str());
}

JNIEXPORT jboolean JNICALL
Java_com_example_myapplication_data_NativeBridge_killProcessNative(JNIEnv *env, jobject thiz, jint pid) {
    return ProcessScanner::killProcess((int32_t)pid) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL
Java_com_example_myapplication_data_NativeBridge_scanStorageNative(JNIEnv *env, jobject thiz, jstring jpath, jint maxLarge) {
    const char* pathStr = env->GetStringUTFChars(jpath, nullptr);
    std::string rootPath = pathStr ? pathStr : "";
    if (pathStr) env->ReleaseStringUTFChars(jpath, pathStr);

    StorageAnalysisResult res = StorageAnalyzer::analyzeDirectory(rootPath, (size_t)maxLarge);
    std::ostringstream ss;
    ss << "{"
       << "\"totalBytes\":" << res.totalBytes << ","
       << "\"imagesBytes\":" << res.imagesBytes << ","
       << "\"videosBytes\":" << res.videosBytes << ","
       << "\"audioBytes\":" << res.audioBytes << ","
       << "\"docsBytes\":" << res.docsBytes << ","
       << "\"archivesBytes\":" << res.archivesBytes << ","
       << "\"otherBytes\":" << res.otherBytes << ","
       << "\"totalFiles\":" << res.totalFiles << ","
       << "\"totalDirs\":" << res.totalDirs << ","
       << "\"largestFiles\":[";

    for (size_t i = 0; i < res.largestFiles.size(); ++i) {
        const auto& f = res.largestFiles[i];
        if (i > 0) ss << ",";
        ss << "{"
           << "\"path\":\"" << escapeJson(f.path) << "\","
           << "\"name\":\"" << escapeJson(f.name) << "\","
           << "\"sizeBytes\":" << f.sizeBytes << ","
           << "\"extension\":\"" << escapeJson(f.extension) << "\""
           << "}";
    }
    ss << "]}";

    return env->NewStringUTF(ss.str().c_str());
}

JNIEXPORT jstring JNICALL
Java_com_example_myapplication_data_NativeBridge_scanJunkNative(JNIEnv *env, jobject thiz, jobjectArray jpaths) {
    std::vector<std::string> paths;
    int count = env->GetArrayLength(jpaths);
    for (int i = 0; i < count; i++) {
        jstring str = (jstring)env->GetObjectArrayElement(jpaths, i);
        const char* cstr = env->GetStringUTFChars(str, nullptr);
        if (cstr) {
            paths.push_back(cstr);
            env->ReleaseStringUTFChars(str, cstr);
        }
        env->DeleteLocalRef(str);
    }

    JunkScanResult res = CacheScanner::scanJunk(paths);
    std::ostringstream ss;
    ss << "{"
       << "\"totalJunkBytes\":" << res.totalJunkBytes << ","
       << "\"appCacheBytes\":" << res.appCacheBytes << ","
       << "\"tempFilesBytes\":" << res.tempFilesBytes << ","
       << "\"thumbnailBytes\":" << res.thumbnailBytes << ","
       << "\"apkJunkBytes\":" << res.apkJunkBytes << ","
       << "\"logFilesBytes\":" << res.logFilesBytes << ","
       << "\"items\":[";

    for (size_t i = 0; i < res.items.size(); ++i) {
        const auto& it = res.items[i];
        if (i > 0) ss << ",";
        ss << "{"
           << "\"path\":\"" << escapeJson(it.path) << "\","
           << "\"name\":\"" << escapeJson(it.name) << "\","
           << "\"sizeBytes\":" << it.sizeBytes << ","
           << "\"type\":" << static_cast<int>(it.type) << ","
           << "\"description\":\"" << escapeJson(it.description) << "\""
           << "}";
    }
    ss << "]}";

    return env->NewStringUTF(ss.str().c_str());
}

JNIEXPORT jlong JNICALL
Java_com_example_myapplication_data_NativeBridge_cleanJunkNative(JNIEnv *env, jobject thiz, jobjectArray jpaths) {
    std::vector<std::string> paths;
    int count = env->GetArrayLength(jpaths);
    for (int i = 0; i < count; i++) {
        jstring str = (jstring)env->GetObjectArrayElement(jpaths, i);
        const char* cstr = env->GetStringUTFChars(str, nullptr);
        if (cstr) {
            paths.push_back(cstr);
            env->ReleaseStringUTFChars(str, cstr);
        }
        env->DeleteLocalRef(str);
    }

    return (jlong)CacheScanner::cleanJunk(paths);
}

JNIEXPORT jboolean JNICALL
Java_com_example_myapplication_data_NativeBridge_deleteFileNative(JNIEnv *env, jobject thiz, jstring jpath) {
    const char* pathStr = env->GetStringUTFChars(jpath, nullptr);
    std::string path = pathStr ? pathStr : "";
    if (pathStr) env->ReleaseStringUTFChars(jpath, pathStr);
    return StorageAnalyzer::deleteFileNative(path) ? JNI_TRUE : JNI_FALSE;
}

}
