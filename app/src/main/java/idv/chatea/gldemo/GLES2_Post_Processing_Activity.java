package idv.chatea.gldemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import idv.chatea.gldemo.gles20.Skybox;
import idv.chatea.gldemo.lighting.Light;
import idv.chatea.gldemo.lighting.LightObjObject;
import idv.chatea.gldemo.objloader.BasicObjLoader;
import idv.chatea.gldemo.objloader.SmoothObjLoader;
import idv.chatea.gldemo.postprocess.FrameBuffer;
import idv.chatea.gldemo.postprocess.Kernel;
import idv.chatea.gldemo.postprocess.KernelPostProcessing;
import idv.chatea.gldemo.shadow.Shadow;
import idv.chatea.gldemo.shadow.ShadowEffectData;
import idv.chatea.gldemo.shadow.ShadowTexture;

/**
 * Used to demo how to post-processing your scene.
 * The key point is render on Framebuffer object (FBO) and then do image processing on the texture.
 *
 * @see GLES2_Image_Processing_Activity
 */
public class GLES2_Post_Processing_Activity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;
    private MyRenderer mRenderer;

    private Spinner mEffectSelectorSpinner;

    private static final String[] KERNEL_EFFECT_LIST = {
            "Identity",
            "Edge Detection 1",
            "Edge Detection 2",
            "Edge Detection 3",
            "Sharpen",
            "Box Blur",
            "Gaussian Blur",
            "5x5 Unsharp",
    };

    private static final Kernel[] KERNELS = new Kernel[8];
    static {
        // identity
        KERNELS[0] = new Kernel(new float[] {0, 1, 0}, new float[] {0, 1, 0});
        // edge detection 1
        KERNELS[1] = new Kernel(new float[] {1, 0, -1}, new float[] {1, 0, -1});
        // edge detection 2
        KERNELS[2] = new Kernel(3);
        KERNELS[2].setValue(0, 1, 1);
        KERNELS[2].setValue(1, 0, 1);
        KERNELS[2].setValue(1, 1, -4);
        KERNELS[2].setValue(1, 2, 1);
        KERNELS[2].setValue(2, 1, 1);
        // edge detection 3
        KERNELS[3] = new Kernel(3);
        KERNELS[3].setAll(-1);
        KERNELS[3].setValue(1, 1, 8);
        // Sharpen
        KERNELS[4] = new Kernel(3);
        KERNELS[4].setValue(0, 1, -1);
        KERNELS[4].setValue(1, 0, -1);
        KERNELS[4].setValue(1, 1, 5);
        KERNELS[4].setValue(1, 2, -1);
        KERNELS[4].setValue(2, 1, -1);
        // Box Blur
        KERNELS[5] = new Kernel(3);
        KERNELS[5].setAll(1);
        KERNELS[5].setConstFactor(1 / 9f);

        // Gaussian Blur
        KERNELS[6] = new Kernel(new float[] {1, 2, 1}, new float[] {1, 2, 1});
        KERNELS[6].setConstFactor(1 / 16f);
        // 5x5 Unsharp"
        KERNELS[7] = new Kernel(new float[] {1, 4, 6, 4, 1}, new float[] {1, 4, 6, 4, 1});
        KERNELS[7].setValue(2, 2, -476);
        KERNELS[7].setConstFactor(-1 / 256f);
    }

    private float mPreviousX;
    private float mPreviousY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gles2_post_processing);
        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        mGLSurfaceView.setEGLContextClientVersion(2);
        // Just in case... explicitly specify using alpha bit.
        mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        // explicitly specify the buffer size.
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 8, 8);
        mGLSurfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR);
        mRenderer = new MyRenderer();
        mGLSurfaceView.setRenderer(mRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        mEffectSelectorSpinner = (Spinner) findViewById(R.id.effectSelector);
        mEffectSelectorSpinner.setAdapter(new ArrayAdapter(this, android.R.layout.simple_spinner_item, KERNEL_EFFECT_LIST));
        mEffectSelectorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                mGLSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mRenderer.setKernel(KERNELS[position]);
                        mGLSurfaceView.requestRender();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mEffectSelectorSpinner.setSelection(0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                final float dx = x - mPreviousX;
                final float dy = y - mPreviousY;
                mRenderer.handleDrag(dx, dy);
                break;
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    private static final float[] DESKTOP_SIZE = {300, 300, 300};

    /**
     * Customized a Mirror class. Use it to simulate the view.
     */

    private static class Desktop extends ShadowTexture {

        public Desktop(Context context, Bitmap bitmap) {
            super(context, bitmap);
        }

        @Override
        public void draw(float[] vpMatrix, float[] moduleMatrix, float[] eyePosition, ShadowEffectData shadowEffectData) {
            float[] correctModuleMatrix = new float[16];
            /**
             * Default size of Plane is (1,1,1), scale it as our size
             */
            Matrix.scaleM(correctModuleMatrix, 0, moduleMatrix, 0, DESKTOP_SIZE[0], DESKTOP_SIZE[1], DESKTOP_SIZE[2]);
            /**
             * The original normal of plane is +z, make it as +y
             */
            Matrix.rotateM(correctModuleMatrix, 0, -90, 1, 0, 0);
            super.draw(vpMatrix, correctModuleMatrix, eyePosition, shadowEffectData);
            Matrix.rotateM(correctModuleMatrix, 0, 180, 1, 0, 0);
            super.draw(vpMatrix, correctModuleMatrix, eyePosition, shadowEffectData);
        }
    }

    private class MyRenderer implements GLSurfaceView.Renderer {
        private float[] mProjectMatrix = new float[16];
        private float[] mViewMatrix = new float[16];

        private static final float MOVEMENT_FACTOR_THETA = 180.0f / 320;
        private static final float MOVEMENT_FACTOR_PHI = 90.0f / 320;

        private float[] mEyePoint = new float[3];
        private float mViewDistance = 400;
        private float mTheta = 85;
        private float mPhi = 0;

        private Skybox mSkybox;
        private float[] mSkyboxModule = new float[16];

        private LightObjObject mTeapot;
        private float[] mTeapotModule = new float[16];

        private Light mLight;

        private Shadow mShadow;

        private Desktop mDesktop;
        private float[] mDesktopModule = new float[16];

        private FrameBuffer mPreProcessingFrame;

        private float[] mPostMVP = new float[16];
        private KernelPostProcessing mKernelPostProcessing;
        private Kernel mKernel;

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glEnable(GLES20.GL_CULL_FACE);

            Context context = GLES2_Post_Processing_Activity.this;

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.skybox);
            mSkybox = new Skybox(bitmap);
            bitmap.recycle();
            Matrix.setIdentityM(mSkyboxModule, 0);
            Matrix.rotateM(mSkyboxModule, 0, 45, 0, 1, 0);
            Matrix.scaleM(mSkyboxModule, 0, mViewDistance - 1f, mViewDistance - 1f, mViewDistance - 1f);

            BasicObjLoader loader = new SmoothObjLoader();
            mTeapot = new LightObjObject(context, loader.loadObjFile(context, "teapot/teapot.obj"));
            Matrix.setIdentityM(mTeapotModule, 0);
            Matrix.translateM(mTeapotModule, 0, 0, 0, 0);
            Matrix.rotateM(mTeapotModule, 0, -60, 0, 1, 0);

            mLight = new Light();
            mLight.position = new float[]{mViewDistance, mViewDistance, -mViewDistance, 1.0f};
            mLight.ambientChannel = new float[]{0.3f, 0.3f, 0.3f, 1.0f};
            mLight.diffusionChannel = new float[]{0.5f, 0.5f, 0.5f, 1.0f};
            mLight.specularChannel = new float[]{0.55f, 0.55f, 0.55f, 1.0f};

            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tiles);
            mDesktop = new Desktop(context, bitmap);
            bitmap.recycle();
            Matrix.setIdentityM(mDesktopModule, 0);
        }

        public void setKernel(Kernel kernel) {
            mKernel = kernel;
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);

            float ratio = (float) width / height;
            Matrix.frustumM(mProjectMatrix, 0, -ratio * mViewDistance / 4, ratio * mViewDistance / 4,
                    -1 * mViewDistance / 4, 1 * mViewDistance / 4, mViewDistance, mViewDistance * 3);

            mShadow = new Shadow(width, height);
            mPreProcessingFrame = new FrameBuffer(width, height);

            float[] mPostProcessingProject = new float[16];
            float[] mPostProcessingView = new float[16];
            float[] mPostProcessingModule = new float[16];

            /** Setup postprocessing view */

            Matrix.orthoM(mPostProcessingProject, 0, -width / 2, width / 2, -height / 2, height / 2, 0, 2);
            Matrix.setLookAtM(mPostProcessingView, 0,
                    0, 0, 1,
                    0, 0, 0,
                    0, 1, 0);
            Matrix.setIdentityM(mPostProcessingModule, 0);
            Matrix.scaleM(mPostProcessingModule, 0, width, height, 1);

            Matrix.multiplyMM(mPostMVP, 0, mPostProcessingProject, 0, mPostProcessingView, 0);
            Matrix.multiplyMM(mPostMVP, 0, mPostMVP, 0, mPostProcessingModule, 0);

            mKernelPostProcessing = new KernelPostProcessing(GLES2_Post_Processing_Activity.this, mPreProcessingFrame);
            mKernelPostProcessing.setSamplingStep(1);
        }

        @Override
        public void onDrawFrame(GL10 unused) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);

            float[] rotateMatrix = new float[16];
            Matrix.setRotateM(rotateMatrix, 0, 1, 0, 1, 0);
            Matrix.multiplyMM(mTeapotModule, 0, rotateMatrix, 0, mTeapotModule, 0);
            Matrix.multiplyMM(mDesktopModule, 0, rotateMatrix, 0, mDesktopModule, 0);

            updateEyePosition();

            /**
             * Prepare common rendering resources
             */
            Matrix.setLookAtM(mViewMatrix, 0,
                    mEyePoint[0], mEyePoint[1], mEyePoint[2],
                    0f, 0f, 0f,
                    0f, mTheta % 360 < 180 ? 1.0f : -1.0f, 0f);

            float[] vpMatrix = new float[16];

            Matrix.multiplyMM(vpMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

            /**
             * Prepare lighting and shadowing resources
             */
            float[] lightVPMatrix = new float[16];
            float[] lightViewMatrix = new float[16];

            Matrix.setLookAtM(lightViewMatrix, 0,
                    mLight.position[0], mLight.position[1], mLight.position[2],
                    0, 0, 0,
                    0f, mTheta % 360 < 180? 1.0f : -1.0f, 0f);

            Matrix.multiplyMM(lightVPMatrix, 0, mProjectMatrix, 0, lightViewMatrix, 0);

            mShadow.startDrawShadowTexture();

            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);

            // render to shadow texture.
            mTeapot.draw(lightVPMatrix, mTeapotModule, mLight, mLight.position);

            mShadow.stopDrawShadowTexture();

            /**
             * Render all scene, on framebuffer.
             */
            mPreProcessingFrame.bind();
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            mTeapot.draw(vpMatrix, mTeapotModule, mLight, mEyePoint);

            // Desktop use shadow.
            mDesktop.draw(vpMatrix, mDesktopModule, mEyePoint, new ShadowEffectData(mShadow, mLight, lightVPMatrix));

            mSkybox.draw(vpMatrix, mSkyboxModule);
            mPreProcessingFrame.unbind();

            /**
             * post-processing the texture on framebuffer.
             */
            if (mKernel == null) {
                return;
            }
            boolean[] depthTested = new boolean[1];
            GLES20.glGetBooleanv(GLES20.GL_DEPTH_TEST, depthTested, 0);
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);

            mKernelPostProcessing.draw(mPostMVP, mKernel);

            if (depthTested[0]) {
                GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            }
        }

        public void handleDrag(final float dx, final float dy) {
            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mTheta -= MOVEMENT_FACTOR_THETA * dy;
                    while (mTheta < 0) {
                        mTheta += 360;
                    }
                    mTheta = mTheta % 360;

                    mPhi -= MOVEMENT_FACTOR_PHI * dx * (mTheta < 180? 1: -1);
                    while (mPhi < 0) {
                        mPhi += 360;
                    }
                    mPhi = mPhi % 360;
                }
            });
        }

        private void updateEyePosition() {
            float theta = mTheta % 360;
            float phi = mPhi % 360;

            double radianceTheta = theta * Math.PI / 180;
            double radiancePhi = phi * Math.PI / 180;

            mEyePoint[0] = (float) (2 * mViewDistance * Math.sin(radianceTheta) * Math.sin(radiancePhi));
            mEyePoint[1] = (float) (2 * mViewDistance * Math.cos(radianceTheta));
            mEyePoint[2] = (float) (2 * mViewDistance * Math.sin(radianceTheta) * Math.cos(radiancePhi));
        }
    }
}
