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

import androidx.annotation.NonNull;

import com.google.android.filament.Engine;
import com.google.android.filament.IndirectLight;
import com.google.android.filament.Skybox;
import com.google.android.filament.Texture;
import java.nio.Buffer;

public final class KtxLoader {
    public static final class Options {
        private boolean srgb;
        public final boolean getSrgb() {
            return this.srgb;
        }
        public final void setSrgb(boolean var1) {
            this.srgb = var1;
        }
    }

    @NonNull
    public final Texture createTexture(@NonNull Engine engine, @NonNull Buffer buffer, @NonNull KtxLoader.Options options) {
        long nativeEngine = engine.getNativeObject();
        long nativeTexture = 0; // this.nCreateTexture(nativeEngine, buffer, buffer.remaining(), options.getSrgb());
        return new Texture(nativeTexture);
    }

    @NonNull
    public final IndirectLight createIndirectLight(@NonNull Engine engine, @NonNull Buffer buffer, @NonNull KtxLoader.Options options) {
        long nativeEngine = engine.getNativeObject();
        long nativeIndirectLight = 0; // this.nCreateIndirectLight(nativeEngine, buffer, buffer.remaining(), options.getSrgb());
        return new IndirectLight(nativeIndirectLight);
    }

    @NonNull
    public final Skybox createSkybox(@NonNull Engine engine, @NonNull Buffer buffer, @NonNull KtxLoader.Options options) {
        long nativeEngine = engine.getNativeObject();
        long nativeSkybox = 0; // this.nCreateSkybox(nativeEngine, buffer, buffer.remaining(), options.getSrgb());
        return new Skybox(nativeSkybox);
    }
}
