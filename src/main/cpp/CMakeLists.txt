# RaveX Team — native library build (https://github.com/StormDevzz/RaveX)
# Adapted for ThunderHack
#
# Build for current platform:
#   mkdir -p build && cd build
#   cmake .. && cmake --build .
#
# The resulting shared library will be placed at:
#   Linux:   src/main/resources/assets/thunderhack/natives/libthunderhack_optimizer.so
#   Windows: src/main/resources/assets/thunderhack/natives/thunderhack_optimizer.dll

cmake_minimum_required(VERSION 3.14)
project(thunderhack_optimizer CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# Find JNI (requires JAVA_HOME to be set)
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

add_library(thunderhack_optimizer SHARED optimizer.cpp)

# Set output directory directly into the project's resource folder
set_target_properties(thunderhack_optimizer PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY "${CMAKE_SOURCE_DIR}/../resources/assets/thunderhack/natives"
)

# Platform-specific output names
if(WIN32)
    set_target_properties(thunderhack_optimizer PROPERTIES
        OUTPUT_NAME "thunderhack_optimizer"
        SUFFIX ".dll"
    )
else()
    set_target_properties(thunderhack_optimizer PROPERTIES
        OUTPUT_NAME "thunderhack_optimizer"
        SUFFIX ".so"
    )
endif()
