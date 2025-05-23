cmake_minimum_required(VERSION 3.22.1)
project(spirv_backend)

set(CMAKE_CXX_STANDARD 14)

if ("${SPIRV_BACKEND}EMPTY" STREQUAL "EMPTY")
    set (SPIRV_BACKEND "${CMAKE_SOURCE_DIR}")
    message("SPIRV_BACKEND=${SPIRV_BACKEND}")
endif()

if ("${SHARED_BACKEND}EMPTY" STREQUAL "EMPTY")
    set (SHARED_BACKEND "${CMAKE_SOURCE_DIR}/../shared")
    message("SHARED_BACKEND=${SHARED_BACKEND}")
endif()

message("SPIRV")

include_directories(
    ${SPIRV_BACKEND}/include
    ${SHARED_BACKEND}/include
    ${SPIRV_INCLUDE_DIR}
)
link_directories(
    ${CMAKE_BINARY_DIR}
)

add_library(spirv_backend SHARED
    ${SHARED_BACKEND}/cpp/shared.cpp
    ${SPIRV_BACKEND}/cpp/spirv_backend.cpp
    ${SHARED_BACKEND}/cpp/strutil.cpp
    ${SHARED_BACKEND}/include/strutil.h
)

add_executable(spirv_info
    ${SPIRV_BACKEND}/cpp/info.cpp
)

target_link_libraries(spirv_info
    spirv_backend
)


