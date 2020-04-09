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

#ifndef TNT_PAGECURL_PAGEMATERIALS_H
#define TNT_PAGECURL_PAGEMATERIALS_H

#include <utils/Entity.h>

namespace filament {

class Engine;
class Material;
class MaterialInstance;

namespace pagecurl {

class PageMaterials {
public:
    enum class Parameter {
        IMAGE_TEXTURE,
        APEX_FLOAT,
        THETA_FLOAT,
    };

    static const char* getParameterName(Parameter parameter);

    PageMaterials(filament::Engine* engine);
    filament::MaterialInstance* createInstance();

private:
    filament::Material* mMaterial;
};

} // namespace pagecurl
} // namespace filament

#endif // TNT_PAGECURL_PAGEMATERIALS_H
