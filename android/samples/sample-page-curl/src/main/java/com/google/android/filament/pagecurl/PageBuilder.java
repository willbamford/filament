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

package com.google.android.filament.pagecurl;

import com.google.android.filament.Engine;
import com.google.android.filament.EntityManager;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.RenderableManager;
import com.google.android.filament.VertexBuffer;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

@SuppressWarnings("WeakerAccess")
public class PageBuilder {
    private final PageMaterials mMaterials;
    private final float[] mSize = new float[] { 1.0f, 1.0f };
    private final float[] mCenter = new float[] { 0.5f, 0.0f };
    private final int[] mMeshResolution = new int[] { 20, 20 };

    private Orientation mOrientation = Orientation.RIGHT;
    private float mZOrder = 0;

    public enum Orientation {
        LEFT,
        RIGHT,
    }

    public PageBuilder(PageMaterials materials) {
        this.mMaterials = materials;
    }

    public PageBuilder orientation(Orientation orientation) {
        mOrientation = orientation;
        return this;
    }

    public PageBuilder size(float width, float height) {
        mSize[0] = width;
        mSize[1] = height;
        return this;
    }

    public PageBuilder zOrder(float z) {
        mZOrder = z;
        return this;
    }

    public PageBuilder center(float x, float y) {
        mCenter[0] = x;
        mCenter[1] = y;
        return this;
    }

    public PageBuilder meshResolution(int columnCount, int rowCount) {
        mMeshResolution[0] = columnCount;
        mMeshResolution[1] = rowCount;
        return this;
    }

    Page build(Engine engine, EntityManager entityManager) {
        final int numColumns = mMeshResolution[0];
        final int numRows = mMeshResolution[1];
        final int numCells = numColumns * numRows;
        final int numIndices = numCells * 6;
        final int numVertices = (numColumns + 1) * (numRows + 1);

        if (mMaterials == null || numVertices >= Short.MAX_VALUE) {
            return null;
        }

        FloatBuffer positions = FloatBuffer.allocate(numVertices * 3);
        FloatBuffer uvs = FloatBuffer.allocate(numVertices * 2);
        FloatBuffer normals = FloatBuffer.allocate(numVertices * 3);
        FloatBuffer tangents = FloatBuffer.allocate(numVertices * 4);
        ShortBuffer indices = ShortBuffer.allocate(numIndices * 2);

        Page page = new Page();

        page.indexBuffer = new IndexBuffer.Builder()
                .indexCount(numIndices)
                .bufferType(IndexBuffer.Builder.IndexType.USHORT)
                .build(engine);

        final int vertsPerRow = numColumns + 1;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numColumns; col++) {
                final int a = col       + row       * vertsPerRow;
                final int b = (col + 1) + row       * vertsPerRow;
                final int c = col       + (row + 1) * vertsPerRow;
                final int d = (col + 1) + (row + 1) * vertsPerRow;
                indices.put((short)a);
                indices.put((short)b);
                indices.put((short)d);
                indices.put((short)d);
                indices.put((short)c);
                indices.put((short)a);
            }
        }
        indices.rewind();
        page.indexBuffer.setBuffer(engine, indices);

        page.vertexBuffer = new VertexBuffer.Builder()
                .bufferCount(3)
                .vertexCount(numVertices)
                .attribute(VertexBuffer.VertexAttribute.POSITION, 0, VertexBuffer.AttributeType.FLOAT3)
                .attribute(VertexBuffer.VertexAttribute.UV0, 1, VertexBuffer.AttributeType.FLOAT2)
                .attribute(VertexBuffer.VertexAttribute.TANGENTS, 2, VertexBuffer.AttributeType.FLOAT4)
                .build(engine);

        for (int row = 0; row <= numRows; row++) {
            for (int col = 0; col <= numColumns; col++) {
                final float u = (float) col / numColumns;
                final float v = (float) row / numRows;
                final float x = u * mSize[0] + mCenter[0] - mSize[0] / 2.0f;
                final float y = v * mSize[1] + mCenter[1] - mSize[1] / 2.0f;
                positions.put(x);
                positions.put(y);
                positions.put(mZOrder);
                uvs.put(u);
                uvs.put(v);
                tangents.put(0);
                tangents.put(0);
                tangents.put(0);
                tangents.put(1);
            }
        }

        positions.flip();
        uvs.flip();
        tangents.flip();

        page.vertexBuffer.setBufferAt(engine, 0, positions);
        page.vertexBuffer.setBufferAt(engine, 1, uvs);
        page.vertexBuffer.setBufferAt(engine, 2, tangents);

        page.positions = positions;
        page.uvs = uvs;
        page.normals = normals;
        page.tangents = tangents;

        page.material = mMaterials.createInstance();
        page.renderable = entityManager.create();

        new RenderableManager.Builder(1)
                .culling(false)
                .material(0, page.material)
                .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, page.vertexBuffer, page.indexBuffer)
                .castShadows(false)
                .receiveShadows(false)
                .build(engine, page.renderable);

        return page;
    }
}