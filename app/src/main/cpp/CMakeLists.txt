cmake_minimum_required(VERSION 3.22.1)

project("tdjni_bridge")

add_library(
    tdjni
    SHARED
    tdjni_bridge.cpp
)

target_link_libraries(
    tdjni
    log
)
