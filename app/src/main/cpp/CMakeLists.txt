cmake_minimum_required(VERSION 3.22.1)

project("boostmax_native")

add_library(
    boostmax_native
    SHARED
    native-bridge.cpp
    memory_profiler.cpp
    process_scanner.cpp
    storage_analyzer.cpp
    cache_scanner.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    boostmax_native
    ${log-lib}
)
