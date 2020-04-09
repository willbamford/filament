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

#ifndef TNT_PAGECURL_PAGEBUILDER_H
#define TNT_PAGECURL_PAGEBUILDER_H

#include <math/vec2.h>

#include <utils/EntityManager.h>

#include <pagecurl/Page.h>
#include <pagecurl/PageMaterials.h>

namespace filament {

class Engine;

namespace pagecurl {

class PageBuilder {
public:
    enum class Orientation {
        LEFT,
        RIGHT,
    };

    PageBuilder(PageMaterials* materials);

    PageBuilder& orientation(Orientation orientation) noexcept;
    PageBuilder& size(float width, float height) noexcept;
    PageBuilder& zOrder(float z) noexcept;
    PageBuilder& center(float x, float y) noexcept;
    PageBuilder& meshResolution(int columnCount, int rowCount) noexcept;

    Page* build(Engine& engine, utils::EntityManager* entityManager);

private:
    PageMaterials* mMaterials = nullptr;
    Orientation mOrientation = Orientation::RIGHT; // TODO: this is unused
    math::float2 mSize = { 1, 1 };
    math::float2 mCenter = { 0.5, 0.0 };
    float mZOrder = 0;
    math::int2 mMeshResolution = { 20, 20 };
};

} // namespace pagecurl
} // namespace filament

#endif // TNT_PAGECURL_PAGEBUILDER_H
