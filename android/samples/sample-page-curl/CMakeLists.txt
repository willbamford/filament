cmake_minimum_required(VERSION 3.6)

set(FILAMENT_DIR ${FILAMENT_DIST_DIR})

add_library(filament STATIC IMPORTED)
set_target_properties(filament PROPERTIES IMPORTED_LOCATION
        ${FILAMENT_DIR}/lib/${ANDROID_ABI}/libfilament.a)

add_library(backend STATIC IMPORTED)
set_target_properties(backend PROPERTIES IMPORTED_LOCATION
        ${FILAMENT_DIR}/lib/${ANDROID_ABI}/libbackend.a)

add_library(utils STATIC IMPORTED)
set_target_properties(utils PROPERTIES IMPORTED_LOCATION
        ${FILAMENT_DIR}/lib/${ANDROID_ABI}/libutils.a)

add_library(pagecurl STATIC IMPORTED)
set_target_properties(pagecurl PROPERTIES IMPORTED_LOCATION
        ${FILAMENT_DIR}/lib/${ANDROID_ABI}/libpagecurl.a)

set(VERSION_SCRIPT "${CMAKE_CURRENT_SOURCE_DIR}/libpagecurl-jni.map")
set(CMAKE_SHARED_LINKER_FLAGS_RELEASE "${CMAKE_SHARED_LINKER_FLAGS_RELEASE} -Wl,--version-script=${VERSION_SCRIPT}")

add_library(pagecurl-jni SHARED
    src/main/cpp/PageBuilder.cpp
    src/main/cpp/PageMaterials.cpp
    ../../common/CallbackUtils.cpp
    ../../common/NioUtils.cpp
)

# Ordering is significant in the following list. The PRIVATE qualifier prevents transitive deps.
target_link_libraries(pagecurl-jni
    PRIVATE filament
    PRIVATE backend
    PRIVATE pagecurl
    PRIVATE utils
)

target_include_directories(pagecurl-jni PRIVATE
        ..
        ${FILAMENT_DIR}/include
        ../../third_party/robin-map
        ../../libs/utils/include)

set_target_properties(pagecurl-jni PROPERTIES LINK_DEPENDS ${VERSION_SCRIPT})
