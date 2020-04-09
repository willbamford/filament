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

#include <pagecurl/PageBuilder.h>

#include <filament/Engine.h>
#include <filament/IndexBuffer.h>
#include <filament/MaterialInstance.h>
#include <filament/RenderableManager.h>
#include <filament/VertexBuffer.h>

#include <memory.h>

using namespace filament::math;

static const auto FREE_CALLBACK = [](void* mem, size_t, void*) { free(mem); };

namespace filament {
namespace pagecurl {

PageBuilder::PageBuilder(PageMaterials* materials) : mMaterials(materials) {}

PageBuilder& PageBuilder::orientation(Orientation orientation) noexcept {
    mOrientation = orientation;
    return *this;
}

PageBuilder& PageBuilder::size(float width, float height) noexcept {
    mSize = {width, height};
    return *this;
}

PageBuilder& PageBuilder::zOrder(float z) noexcept {
    mZOrder = z;
    return *this;
}

PageBuilder& PageBuilder::center(float x, float y) noexcept {
    mCenter = {x, y};
    return *this;
}

PageBuilder& PageBuilder::meshResolution(int columnCount, int rowCount) noexcept {
    mMeshResolution = { columnCount, rowCount };
    return *this;
}

Page* PageBuilder::build(Engine& engine, utils::EntityManager* entityManager) {
    const int numColumns = mMeshResolution.x;
    const int numRows = mMeshResolution.y;
    const int numCells = numColumns * numRows;
    const int numIndices = numCells * 6;
    const int numVertices = (numColumns + 1) * (numRows + 1);

    if (mMaterials == nullptr || numIndices > 65535) {
        return nullptr;
    }

    Page page = {};

    const int indexBufferSize = numIndices * 8;
    const int posBufferSize = numVertices * sizeof(float3);
    const int uvBufferSize = numVertices * sizeof(float2);
    const int tanBufferSize = numVertices * 8;

    float3* positions = (float3*) malloc(posBufferSize);
    float2* uvs = (float2*) malloc(uvBufferSize);
    int16_t* tangents = (int16_t*) malloc(tanBufferSize);
    uint16_t* indices = (uint16_t*) malloc(indexBufferSize);

    // Populate index buffer

    page.indexBuffer = IndexBuffer::Builder()
        .indexCount(numIndices)
        .bufferType(IndexBuffer::IndexType::USHORT)
        .build(engine);

    int tindex = 0;
    const auto addTriangle = [&tindex, indices] (int i, int j, int k) {
        indices[tindex * 3 + 0] = i;
        indices[tindex * 3 + 1] = j;
        indices[tindex * 3 + 2] = k;
        tindex++;
    };

    const int vertsPerRow = numColumns + 1;
    for (int row = 0; row < numRows; row++) {
        for (int col = 0; col < numColumns; col++) {
            const int a = (col + 0) + (row + 0) * vertsPerRow;
            const int b = (col + 1) + (row + 0) * vertsPerRow;
            const int c = (col + 0) + (row + 1) * vertsPerRow;
            const int d = (col + 1) + (row + 1) * vertsPerRow;
            addTriangle(a, b, d);
            addTriangle(d, c, a);
        }
    }

    IndexBuffer::BufferDescriptor ib((void*) indices, indexBufferSize, FREE_CALLBACK);
    page.indexBuffer->setBuffer(engine, std::move(ib));

    // Populate vertex buffer

    using Type = VertexBuffer::AttributeType;

    page.vertexBuffer = VertexBuffer::Builder()
        .bufferCount(3)
        .vertexCount(numVertices)
        .attribute(VertexAttribute::POSITION, 0, Type::FLOAT3)
        .attribute(VertexAttribute::UV0, 1, Type::FLOAT2)
        .attribute(VertexAttribute::TANGENTS, 2, Type::SHORT4)
        .normalized(VertexAttribute::TANGENTS)
        .build(engine);

    int vindex = 0;
    auto addVertex = [&vindex, positions, uvs](float x, float y, float z, float u, float v) {
        positions[vindex * 3 + 0] = x;
        positions[vindex * 3 + 1] = y;
        positions[vindex * 3 + 2] = z;
        uvs[vindex * 2 + 0] = u;
        uvs[vindex * 2 + 1] = v;
        vindex++;
    };

    for (int row = 0; row <= numRows; row++) {
        for (int col = 0; col <= numColumns; col++) {
            const float u = (float) col / numColumns;
            const float v = (float) row / numRows;
            const float x = u * mSize.x + mCenter.x - mSize.x / 2.0f;
            const float y = v * mSize.y + mCenter.y - mSize.y / 2.0f;
            addVertex(x, y, mZOrder, u, v);
        }
    }

    auto faceNormals = std::make_unique<float3[]>(numCells * 2);
    auto smoothNormals = std::make_unique<float3[]>(numVertices);

    // TODO...

    VertexBuffer::BufferDescriptor pbd((void*) positions, posBufferSize, FREE_CALLBACK);
    page.vertexBuffer->setBufferAt(engine, 0, std::move(pbd));

    VertexBuffer::BufferDescriptor ubd((void*) uvs, uvBufferSize, FREE_CALLBACK);
    page.vertexBuffer->setBufferAt(engine, 1, std::move(ubd));

    VertexBuffer::BufferDescriptor tbd((void*) tangents, tanBufferSize, FREE_CALLBACK);
    page.vertexBuffer->setBufferAt(engine, 2, std::move(tbd));

    page.material = mMaterials->createInstance();
    page.material->setParameter("baseColor", float4 {1, 1, 1, 1});
    page.material->setParameter("roughness", 0.0f);
    page.material->setParameter("metallic", 0.0f);

    page.renderable = entityManager->create();

    using PrimitiveType = RenderableManager::PrimitiveType;

    RenderableManager::Builder(1)
        .boundingBox({ {0.5, 0.0, 0.0}, {0.5, 0.5, 0.5} })
        .material(0, page.material)
        .geometry(0, PrimitiveType::TRIANGLES, page.vertexBuffer, page.indexBuffer)
        .build(engine, page.renderable);

    return new Page(page);
}

} // namespace pagecurl
} // namespace filament
