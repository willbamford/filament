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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;

import com.google.android.filament.Camera;
import com.google.android.filament.Colors;
import com.google.android.filament.Engine;
import com.google.android.filament.Entity;
import com.google.android.filament.EntityManager;
import com.google.android.filament.Filament;
import com.google.android.filament.LightManager;
import com.google.android.filament.Renderer;
import com.google.android.filament.Scene;
import com.google.android.filament.Skybox;
import com.google.android.filament.SwapChain;
import com.google.android.filament.TransformManager;
import com.google.android.filament.Viewport;

import com.google.android.filament.android.DisplayHelper;
import com.google.android.filament.android.UiHelper;

public class MainActivity extends Activity
        implements Choreographer.FrameCallback, UiHelper.RendererCallback, View.OnTouchListener {
    static {
        Filament.init();
    }
    private SurfaceView mSurfaceView;
    private UiHelper mUiHelper;
    private DisplayHelper mDisplayHelper;
    private Choreographer mChoreographer;
    private Engine mEngine;
    private SwapChain mSwapChain;
    private com.google.android.filament.View mView;
    private Renderer mRenderer;
    private Camera mCamera;
    private Page mPage;
    private PageMaterials mPageMaterials;
    private Scene mScene;
    private @Entity int mLight;

    private float[] mTouchDownPoint = new float[2];
    private float mTouchDownValue = 0;
    private float mPageAnimationRadians = 0;
    private float mPageAnimationValue = 0;

    @Override
    public void onNativeWindowChanged(Surface surface) {
        if (mSwapChain != null) {
            mEngine.destroySwapChain(mSwapChain);
        }
        mSwapChain = mEngine.createSwapChain(surface);
        mDisplayHelper.attach(mRenderer, mSurfaceView.getDisplay());
    }

    @Override
    public void onDetachedFromSurface() {
        mDisplayHelper.detach();
        if (mSwapChain != null) {
            mEngine.destroySwapChain(mSwapChain);
            mEngine.flushAndWait();
            mSwapChain = null;
        }
    }

    @Override
    public void onResized(int width, int height) {
        float aspect = (float)width / (float)height;
        if (aspect < 1) {
            mCamera.setProjection(70.0, aspect, 1.0, 2000.0, Camera.Fov.VERTICAL);
        } else {
            mCamera.setProjection(60.0, aspect, 1.0, 2000.0, Camera.Fov.HORIZONTAL);
        }
        mView.setViewport(new Viewport(0, 0, width, height));
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        mChoreographer.postFrameCallback(this);

        final float[] transformMatrix = new float[16];
        final double degrees = -Math.toDegrees(mPageAnimationRadians);
        Matrix.setRotateM(transformMatrix, 0, (float)degrees, 0.0f, 1.0f, 0.0f);
        TransformManager tcm = mEngine.getTransformManager();
        tcm.setTransform(tcm.getInstance(mPage.renderable), transformMatrix);

        if (mUiHelper.isReadyToRender() && mRenderer.beginFrame(mSwapChain, frameTimeNanos)) {
            mRenderer.render(mView);
            mRenderer.endFrame();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mSurfaceView = new SurfaceView(this);
        setContentView(mSurfaceView);

        mChoreographer = Choreographer.getInstance();

        mDisplayHelper = new DisplayHelper(this);

        mUiHelper = new UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK);
        mUiHelper.setRenderCallback(this);
        mUiHelper.attachTo(mSurfaceView);

        mEngine = Engine.create();
        mRenderer = mEngine.createRenderer();
        mScene = mEngine.createScene();
        mView = mEngine.createView();
        mCamera = mEngine.createCamera();

        mCamera.lookAt(0, 0, 3, 0, 0, 0, 0, 1, 0);

        mLight = EntityManager.get().create();

        float[] col = Colors.cct(5_500.0f);
        new LightManager.Builder(LightManager.Type.DIRECTIONAL)
                .color(col[0], col[1], col[2])
                .intensity(110_000.0f)
                .direction(0.0f, -0.5f, -1.0f)
                .castShadows(false)
                .build(mEngine, mLight);

        mScene.addEntity(mLight);

        Skybox sky = new Skybox.Builder().color(0.1f, 0.2f, 0.4f, 1.0f).build(mEngine);
        mScene.setSkybox(sky);

        mView.setCamera(mCamera);
        mView.setScene(mScene);

        mPageMaterials = new PageMaterials(mEngine, getAssets());
        mPage = new PageBuilder(mPageMaterials).build(mEngine, EntityManager.get());
        if (mPage == null) {
            throw new IllegalStateException("Unable to build page geometry");
        }

        mScene.addEntity(mPage.renderable);

        mSurfaceView.setOnTouchListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mChoreographer.postFrameCallback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mChoreographer.removeFrameCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChoreographer.removeFrameCallback(this);
        mUiHelper.detach();

        mEngine.destroyEntity(mLight);
        mEngine.destroyEntity(mPage.renderable);
        mEngine.destroyRenderer(mRenderer);
        mEngine.destroyVertexBuffer(mPage.vertexBuffer);
        mEngine.destroyIndexBuffer(mPage.indexBuffer);
        mEngine.destroyMaterialInstance(mPage.material);
        mEngine.destroyMaterial(mPageMaterials.getMaterial());

        mEngine.destroyView(mView);
        mEngine.destroyScene(mScene);
        mEngine.destroyCamera(mCamera);

        EntityManager.get().destroy(mPage.renderable);

        mEngine.destroy();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mTouchDownPoint[0] = event.getX(0);
                mTouchDownPoint[1] = event.getY(0);
                mTouchDownValue = mPageAnimationValue;
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                // Compute a value in [-0.5, +0.5]
                final float dx = (event.getX(0) - mTouchDownPoint[0]) / mSurfaceView.getWidth();
                mPageAnimationValue = Math.min(+0.5f, Math.max(-0.5f, mTouchDownValue + dx));

                // In this demo, we only care about dragging the right hand page leftwards.
                mPageAnimationRadians = mPage.updatePositions(mEngine, -mPageAnimationValue * 3.0f);
                return true;
            }
            case MotionEvent.ACTION_UP: {
                return true;
            }
        }
        return super.onTouchEvent(event);
    }
}