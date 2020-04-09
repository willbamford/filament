/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <pagecurl/PageMaterials.h>

#include <filament/Material.h>

#include "page_resources.h"

namespace filament {
namespace pagecurl {

const char* PageMaterials::getParameterName(Parameter parameter) {
    switch (parameter) {
        case Parameter::IMAGE_TEXTURE: return "imageTexture";
        case Parameter::APEX_FLOAT: return "apexFloat";
        case Parameter::THETA_FLOAT: return "thetaFloat";
    }
    return nullptr;
}

PageMaterials::PageMaterials(Engine* engine) {
    mMaterial = Material::Builder()
        .package(PAGE_RESOURCES_PAGECURL_DATA, PAGE_RESOURCES_PAGECURL_SIZE)
        .build(*engine);
}

filament::MaterialInstance* PageMaterials::createInstance() {
    return mMaterial->createInstance();
}

} // namespace pagecurl
} // namespace filament
