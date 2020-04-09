cmake_minimum_required(VERSION 3.10)
project(pagecurl C ASM)

set(TARGET pagecurl)
set(PUBLIC_HDR_DIR include)
set(RESOURCE_DIR ${CMAKE_CURRENT_BINARY_DIR})

if (CMAKE_CROSSCOMPILING)
    include(${IMPORT_EXECUTABLES})
endif()

# ==================================================================================================
# Build materials
# ==================================================================================================
set(MATERIAL_SRCS
    materials/pagecurl.mat
)

set(RESOURCE_BINS)
foreach (material_src ${MATERIAL_SRCS})
    get_filename_component(basename "${material_src}" NAME_WE)
    set(output_path "${CMAKE_CURRENT_BINARY_DIR}/${basename}.filamat")
    set(input_path "${CMAKE_CURRENT_SOURCE_DIR}/${material_src}")
    add_custom_command(
            OUTPUT ${output_path}
            COMMAND matc ${MATC_BASE_FLAGS} -o ${output_path} ${input_path}
            MAIN_DEPENDENCY ${input_path}
            DEPENDS matc
            COMMENT "Compiling material ${material_src} to ${output_path}"
    )
    list(APPEND RESOURCE_BINS ${output_path})
endforeach()

# ==================================================================================================
# Build resources library
# ==================================================================================================
get_resgen_vars(${RESOURCE_DIR} page_resources)

add_custom_command(
        OUTPUT ${RESGEN_OUTPUTS}
        COMMAND resgen ${RESGEN_FLAGS} ${RESOURCE_BINS}
        DEPENDS resgen ${RESOURCE_BINS}
)

if (DEFINED RESGEN_SOURCE_FLAGS)
    set_source_files_properties(${RESGEN_SOURCE} PROPERTIES COMPILE_FLAGS ${RESGEN_SOURCE_FLAGS})
endif()

set(DUMMY_SRC "${RESOURCE_DIR}/dummy.c")
add_custom_command(OUTPUT ${DUMMY_SRC} COMMAND echo "//" > ${DUMMY_SRC})

add_library(page_resources ${DUMMY_SRC} ${RESGEN_SOURCE})
target_include_directories(page_resources PUBLIC ${RESOURCE_DIR})

# ==================================================================================================
# C++ source and headers
# ==================================================================================================
set(PUBLIC_HDRS
        include/pagecurl/Page.h
        include/pagecurl/PageBuilder.h
        include/pagecurl/PageMaterials.h
)

set(SRCS
        src/PageBuilder.cpp
        src/PageMaterials.cpp
)

# ==================================================================================================
# Include and target definitions
# ==================================================================================================
add_library(${TARGET} STATIC ${PUBLIC_HDRS} ${SRCS})
target_link_libraries(${TARGET} PUBLIC math utils filament page_resources)
target_include_directories(${TARGET} PUBLIC ${PUBLIC_HDR_DIR})

# ==================================================================================================
# Compiler flags
# ==================================================================================================
if (MSVC)
    target_compile_options(${TARGET} PRIVATE $<$<CONFIG:Release>:/fp:fast>)
else()
    target_compile_options(${TARGET} PRIVATE $<$<CONFIG:Release>:-ffast-math>)
    target_compile_options(${TARGET} PRIVATE -Wno-deprecated-register)
endif()

if (LINUX)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC")
endif()

# ==================================================================================================
# Installation
# ==================================================================================================
install(TARGETS ${TARGET} ARCHIVE DESTINATION lib/${DIST_DIR})
install(DIRECTORY ${PUBLIC_HDR_DIR}/pagecurl DESTINATION include)
