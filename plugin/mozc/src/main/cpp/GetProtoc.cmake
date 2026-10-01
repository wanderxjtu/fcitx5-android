cmake_minimum_required(VERSION 3.18)

# Define the Protobuf version you want to download
set(PROTOBUF_VERSION "34.1" CACHE STRING "Protobuf version")
set(PROTOBUF_ARCH "linux-x86_64")

# Detect OS and architecture to form the correct download filename

# Construct URL and paths
set(PROTOBUF_URL "https://github.com/protocolbuffers/protobuf/releases/download/v${PROTOBUF_VERSION}/protoc-${PROTOBUF_VERSION}-${PROTOBUF_ARCH}.zip")
set(PROTOBUF_DOWNLOAD_DIR "${CMAKE_BINARY_DIR}/protoc-download")
set(PROTOC_EXECUTABLE_PATH "${PROTOBUF_DOWNLOAD_DIR}/bin/protoc${CMAKE_EXECUTABLE_SUFFIX}")

# Download and extract only if it doesn't already exist in the build directory
if(NOT EXISTS "${PROTOC_EXECUTABLE_PATH}")
    message(STATUS "Downloading protoc v${PROTOBUF_VERSION} for ${PROTOBUF_ARCH}...")
    
    file(DOWNLOAD "${PROTOBUF_URL}" "${PROTOBUF_DOWNLOAD_DIR}/protoc.zip"
         TIMEOUT 120
         STATUS DOWNLOAD_STATUS)
         
    list(GET DOWNLOAD_STATUS 0 STATUS_CODE)
    if(NOT STATUS_CODE EQUAL 0)
        list(GET DOWNLOAD_STATUS 1 ERROR_MESSAGE)
        message(FATAL_ERROR "Failed to download protoc from ${PROTOBUF_URL}: ${ERROR_MESSAGE}")
    endif()

    message(STATUS "Extracting protoc...")
    file(ARCHIVE_EXTRACT
         INPUT "${PROTOBUF_DOWNLOAD_DIR}/protoc.zip"
         DESTINATION "${PROTOBUF_DOWNLOAD_DIR}")
endif()

# Expose WITH_PROTOC globally to the parent scope via CACHE FORCE
set(WITH_PROTOC "${PROTOC_EXECUTABLE_PATH}" CACHE PATH "Path to the protoc compiler" FORCE)

message(STATUS "WITH_PROTOC set to: ${WITH_PROTOC}")
