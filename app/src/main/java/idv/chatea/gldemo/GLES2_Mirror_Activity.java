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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import idv.chatea.gldemo.gles20.Plane;
import idv.chatea.gldemo.gles20.Skybox;
import idv.chatea.gldemo.lighting.Light;
import idv.chatea.gldemo.lighting.LightObjObject;
import idv.chatea.gldemo.objloader.BasicObjLoader;
import idv.chatea.gldemo.objloader.SmoothObjLoader;

/**
 * Used to demo the mirror view.
 * Mirror can be think as a special case of portal view.
 * Useful reference: https://en.wikibooks.org/wiki/OpenGL_Programming/Mini-Portal
 */
public class GLES2_Mirror_Activity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;
    private MyRenderer mRenderer;

    private float mPreviousY;
    private float mPreviousX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(2);
        // Just in case... explicitly specify using alpha bit.
        mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        // explicitly specify using stencil buffer.
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        mRenderer = new MyRenderer();
        mGLSurfaceView.setRenderer(mRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setContentView(mGLSurfaceView);
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

    private static final float[] MIRROR_SIZE = {100, 100, 100};

    /**
     * Customized a Mirror class. Use it to simulate the view.
     */
    private static class Mirror extends Plane {
        /**
         * Normal vector is vector so the w component should be 0.
         */
        private static float[] NORMAL_VECTOR = {0, 0, -1, 0};

        @Override
        public void draw(float[] vpMatrix, float[] modouleMatrix) {
            float[] correctModuleMatrix = new float[16];
            /**
             * Default size of Plane is (1,1,1), scale it as our size
             */
            Matrix.scaleM(correctModuleMatrix, 0, modouleMatrix, 0, MIRROR_SIZE[0], MIRROR_SIZE[1], MIRROR_SIZE[2]);
            /**
             * The original normal of plane is +z, make it same as eye direction (-z) first.
             */
            Matrix.rotateM(correctModuleMatrix, 0, 180, 1, 0, 0);
            super.draw(vpMatrix, correctModuleMatrix);
        }
    }

    private class MyRenderer implements GLSurfaceView.Renderer {
        private float[] mProjectMatrix = new float[16];
        private float[] mViewMatrix = new float[16];

        private static final float MOVEMENT_FACTOR_THETA = 180.0f / 320;
        private static final float MOVEMENT_FACTOR_PHI = 90.0f / 320;

        private float[] mEyePoint = new float[3];
        private float mViewDistance = 400;
        private float mTheta = 90;
        private float mPhi = 0;

        private Skybox mSkybox;
        private float[] mSkyboxModule = new float[16];

        private LightObjObject mTeapot;
        private float[] mTeapotModule = new float[16];

        private Light mLight;

        private Mirror mMirror;
        private float[] mMirrorModule1 = new float[16];
        private float[] mMirrorModule2 = new float[16];
        private float[] mMirrorModule3 = new float[16];

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glEnable(GLES20.GL_CULL_FACE);

            Context context = GLES2_Mirror_Activity.this;

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.skybox);
            mSkybox = new Skybox(bitmap);
            bitmap.recycle();
            Matrix.setIdentityM(mSkyboxModule, 0);
            Matrix.scaleM(mSkyboxModule, 0, mViewDistance - 1f, mViewDistance - 1f, mViewDistance - 1f);

            BasicObjLoader loader = new SmoothObjLoader();
            mTeapot = new LightObjObject(context, loader.loadObjFile(context, "teapot/teapot.obj"));
            Matrix.setIdentityM(mTeapotModule, 0);
            Matrix.translateM(mTeapotModule, 0, 0, -50, 0);
            Matrix.rotateM(mTeapotModule, 0, 30, 0, 1, 0);

            mLight = new Light();
            mLight.position = new float[]{mViewDistance, mViewDistance, -mViewDistance, 1.0f};
            mLight.ambientChannel = new float[]{0.15f, 0.15f, 0.15f, 1.0f};
            mLight.diffusionChannel = new float[]{0.3f, 0.3f, 0.3f, 1.0f};
            mLight.specularChannel = new float[]{0.55f, 0.55f, 0.55f, 1.0f};

            mMirror = new Mirror();
            // bottom
            Matrix.setIdentityM(mMirrorModule1, 0);
            Matrix.translateM(mMirrorModule1, 0, 20, -100, 0);
            Matrix.rotateM(mMirrorModule1, 0, 90, 1, 0, 0);

            // back
            Matrix.setIdentityM(mMirrorModule2, 0);
            Matrix.translateM(mMirrorModule2, 0, -30, 50, -120);
            Matrix.rotateM(mMirrorModule2, 0, 180, 0, 1, 0);

            // right
            Matrix.setIdentityM(mMirrorModule3, 0);
            Matrix.translateM(mMirrorModule3, 0, 100, 0, 20);
            Matrix.rotateM(mMirrorModule3, 0, 90, 0, 1, 0);
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);

            float ratio = (float) width / height;
            Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 2f, mViewDistance * 5);
        }

        @Override
        public void onDrawFrame(GL10 unused) {

            updateEyePosition();

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            Matrix.setLookAtM(mViewMatrix, 0,
                    mEyePoint[0], mEyePoint[1], mEyePoint[2],
                    0f, 0f, 0f,
                    0f, mTheta % 360 < 180 ? 1.0f : -1.0f, 0f);

            float[] vpMatrix = new float[16];

            class MirrorData {
                MirrorData(float[] m) {
                    mirrorModule = m;
                }
                float[] mirrorModule;
            }

            MirrorData[] mds = new MirrorData[3];
            mds[0] = new MirrorData(mMirrorModule1);
            mds[1] = new MirrorData(mMirrorModule2);
            mds[2] = new MirrorData(mMirrorModule3);

            // use stencil to render the image on mirror
            GLES20.glEnable(GLES20.GL_STENCIL_TEST);
            GLES20.glClearStencil(0);
            GLES20.glStencilMask(0xFF);
            for (MirrorData md: mds) {
                /**
                 * draw the stencil buffer to specify the drawable area of mirror.
                 */
                GLES20.glClear(GLES20.GL_STENCIL_BUFFER_BIT);

                // Draw on stencil buffer only.
                GLES20.glColorMask(false, false, false, false);
                GLES20.glDepthMask(false);
                GLES20.glStencilFunc(GLES20.GL_NEVER, 1, 0xFF);
                GLES20.glStencilOp(GLES20.GL_REPLACE, GLES20.GL_KEEP, GLES20.GL_KEEP);

                Matrix.multiplyMM(vpMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
                mMirror.draw(vpMatrix, md.mirrorModule);

                // allow to draw on the area which stencil buffer is 1
                GLES20.glColorMask(true, true, true, true);
                GLES20.glDepthMask(true);
                GLES20.glStencilFunc(GLES20.GL_EQUAL, 1, 0xFF);
                GLES20.glStencilOp(GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_KEEP);

                /**
                 * Render mirror's content
                 */
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
                float[] mirrorViewMatrix = getMirrorViewMatrix(mViewMatrix, md.mirrorModule);
                Matrix.multiplyMM(vpMatrix, 0, mProjectMatrix, 0, mirrorViewMatrix, 0);

                // In mirror case, the front face should reverse.
                GLES20.glFrontFace(GLES20.GL_CW);

                mTeapot.draw(vpMatrix, mTeapotModule, mLight, mEyePoint);

                mSkybox.draw(vpMatrix, mSkyboxModule);

                GLES20.glFrontFace(GLES20.GL_CCW);
            }
            // render on mirror complete, disable stencil test.
            GLES20.glDisable(GLES20.GL_STENCIL_TEST);


            Matrix.multiplyMM(vpMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

            /**
             * Only render mirror on depth buffer.
             * Overwrite the depth buffer is used to protected the frame,
             * So all pixels in mirror will be considered as a plane. (just like a sticker)
             */
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);

            // we draw depth buffer only.
            GLES20.glColorMask(false, false, false, false);

            // draw mirrors in depth buffer.
            mMirror.draw(vpMatrix, mMirrorModule1);
            mMirror.draw(vpMatrix, mMirrorModule2);
            mMirror.draw(vpMatrix, mMirrorModule3);

            GLES20.glColorMask(true, true, true, true);

            /**
             * Render all scene.
             */
            mTeapot.draw(vpMatrix, mTeapotModule, mLight, mEyePoint);

            mSkybox.draw(vpMatrix, mSkyboxModule);
        }

        private float[] getMirrorViewMatrix(float[] viewMatrix, float[] mirror) {
            /**
             * What we need is:
             * 1. Move eye to mirror's.
             * 2. Do reflection to mirror. This can be done by Householder transformation.
             * 3. Move eye back to original position. (a.k.a move scene to original position)
             */
            float[] mirrorView = new float[16];

            float[] backMatrix = new float[16];
            float[] toMatrix = new float[16];
            Matrix.setIdentityM(backMatrix, 0);
            Matrix.setIdentityM(toMatrix, 0);

            /** Module matrix of mirror can move eye back */
            Matrix.scaleM(backMatrix, 0, mirror, 0, 1, 1, 1);

            /** reflect to mirror */
            /**
             * Householder transformation: H = I - 2 * v * v^T, where v is the mirror's normal vector.
             * We already rotated the scene by module matrix, so don't transform normal again.
             */
            float[] houseHolderMatrix = new float[16];
            for (int c = 0; c < 4; c++) {
                for (int r = 0; r < 4; r++) {
                    houseHolderMatrix[4 * c + r] = Mirror.NORMAL_VECTOR[r] * Mirror.NORMAL_VECTOR[c];
                }
            }
            float[] identity = new float[16];
            Matrix.setIdentityM(identity, 0);
            for (int i = 0; i < 16; i++) {
                houseHolderMatrix[i] = identity[i] - 2 * houseHolderMatrix[i];
            }

            /** inverse of module matrix is used to move eye */
            Matrix.invertM(toMatrix, 0, mirror, 0);

            Matrix.scaleM(mirrorView, 0, viewMatrix, 0, 1, 1, 1);
            Matrix.multiplyMM(mirrorView, 0, mirrorView, 0, backMatrix, 0);
            Matrix.multiplyMM(mirrorView, 0, mirrorView, 0, houseHolderMatrix, 0);
            Matrix.multiplyMM(mirrorView, 0, mirrorView, 0, toMatrix, 0);

            return mirrorView;
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

            mEyePoint[0] = (float) (mViewDistance * Math.sin(radianceTheta) * Math.sin(radiancePhi));
            mEyePoint[1] = (float) (mViewDistance * Math.cos(radianceTheta));
            mEyePoint[2] = (float) (mViewDistance * Math.sin(radianceTheta) * Math.cos(radiancePhi));
        }
    }
}
