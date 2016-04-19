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

import idv.chatea.gldemo.gles20.Skybox;
import idv.chatea.gldemo.lighting.Light;
import idv.chatea.gldemo.lighting.LightObjObject;
import idv.chatea.gldemo.objloader.BasicObjLoader;
import idv.chatea.gldemo.objloader.SmoothObjLoader;
import idv.chatea.gldemo.gles20.Plane;

/**
 * Used to demo the portal view.
 * Useful reference: https://en.wikibooks.org/wiki/OpenGL_Programming/Mini-Portal
 */
public class GLES2_Portal_Activity extends AppCompatActivity {

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

    private static final float[] PORTAL_SIZE = {100, 100, 100};

    /**
     * Customized a Portal class. Use it to simulate the view.
     */
    private static class Portal extends Plane {
        @Override
        public void draw(float[] vpMatrix, float[] moduleMatrix) {
            float[] correctModuleMatrix = new float[16];
            /**
             * Default size of Plane is (1,1,1), scale it as our size
             */
            Matrix.scaleM(correctModuleMatrix, 0, moduleMatrix, 0, PORTAL_SIZE[0], PORTAL_SIZE[1], PORTAL_SIZE[2]);
            /**
             * The original up of plane is +z, make it same as eye direction (-z) first.
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

        private Portal mPortal;
        private float[] mPortalModule1 = new float[16];
        private float[] mPortalModule2 = new float[16];

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glEnable(GLES20.GL_CULL_FACE);

            Context context = GLES2_Portal_Activity.this;

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.skybox);
            mSkybox = new Skybox(bitmap);
            bitmap.recycle();
            Matrix.setIdentityM(mSkyboxModule, 0);
            Matrix.scaleM(mSkyboxModule, 0, mViewDistance - 1f, mViewDistance - 1f, mViewDistance - 1f);


            BasicObjLoader loader = new SmoothObjLoader();
            mTeapot = new LightObjObject(context, loader.loadObjFile(context, "teapot/teapot.obj"));
            Matrix.setIdentityM(mTeapotModule, 0);
            Matrix.translateM(mTeapotModule, 0, 0, -50, 0);

            mLight = new Light();
            mLight.position = new float[]{mViewDistance, mViewDistance, -mViewDistance, 1.0f};
            mLight.ambientChannel = new float[]{0.15f, 0.15f, 0.15f, 1.0f};
            mLight.diffusionChannel = new float[]{0.3f, 0.3f, 0.3f, 1.0f};
            mLight.specularChannel = new float[]{0.55f, 0.55f, 0.55f, 1.0f};

            mPortal = new Portal();
            Matrix.setIdentityM(mPortalModule1, 0);
            Matrix.translateM(mPortalModule1, 0, 0, -100, -20);
            Matrix.rotateM(mPortalModule1, 0, 90, 1, 0, 0);

            Matrix.setIdentityM(mPortalModule2, 0);
            Matrix.translateM(mPortalModule2, 0, 100, 0, 20);
            Matrix.rotateM(mPortalModule2, 0, 90, 0, 1, 0);
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);

            float ratio = (float) width / height;
            Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 2f, mViewDistance * 3);
        }

        @Override
        public void onDrawFrame(GL10 unused) {

            updateEyePosition();

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            Matrix.setLookAtM(mViewMatrix, 0,
                    mEyePoint[0], mEyePoint[1], mEyePoint[2],
                    0f, 0f, 0f,
                    0f, mTheta % 360 < 180 ? 1.0f : -1.0f, 0f);

            float[] mvpMatrix = new float[16];
            float[] vpMatrix = new float[16];

            /**
             * Used to render all portal in foreach loop.
             */
            class PortalData {
                PortalData(float[] c, float[] v) {
                    canvasPortalMatrix = c;
                    viewPortalMatrix = v;
                }
                float[] canvasPortalMatrix;
                float[] viewPortalMatrix;
            }

            PortalData[] pds = new PortalData[2];
            pds[0] = new PortalData(mPortalModule1, mPortalModule2);
            pds[1] = new PortalData(mPortalModule2, mPortalModule1);

            // use stencil to render the image on portal
            GLES20.glEnable(GLES20.GL_STENCIL_TEST);
            GLES20.glClearStencil(0);
            GLES20.glStencilMask(0xFF);
            for (PortalData pd: pds) {
                /**
                 * draw the stencil buffer to specify the drawable area of canvasPortal.
                 */
                GLES20.glClear(GLES20.GL_STENCIL_BUFFER_BIT);

                // Draw on stencil buffer only.
                GLES20.glColorMask(false, false, false, false);
                GLES20.glDepthMask(false);
                GLES20.glStencilFunc(GLES20.GL_NEVER, 1, 0xFF);
                GLES20.glStencilOp(GLES20.GL_REPLACE, GLES20.GL_KEEP, GLES20.GL_KEEP);

                Matrix.multiplyMM(vpMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
                mPortal.draw(vpMatrix, pd.canvasPortalMatrix);

                // allow to draw on the area which stencil buffer is 1
                GLES20.glColorMask(true, true, true, true);
                GLES20.glDepthMask(true);
                GLES20.glStencilFunc(GLES20.GL_EQUAL, 1, 0xFF);
                GLES20.glStencilOp(GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_KEEP);

                /**
                 * Render portal's content
                 */
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);

                float[] portalViewMatrix = getPortalViewMatrix(mViewMatrix, pd.viewPortalMatrix, pd.canvasPortalMatrix);
                Matrix.multiplyMM(vpMatrix, 0, mProjectMatrix, 0, portalViewMatrix, 0);

                mTeapot.draw(vpMatrix, mTeapotModule, mLight, mEyePoint);

                mSkybox.draw(vpMatrix, mSkyboxModule);
            }
            // render on portal complete, disable stencil test.
            GLES20.glDisable(GLES20.GL_STENCIL_TEST);


            Matrix.multiplyMM(vpMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

            /**
             * Only render portal on depth buffer.
             * Overwrite the depth buffer is used to protected the frame,
             * So all pixels in portal will be considered as a plane. (just like a sticker)
             */
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);

            // we draw depth buffer only.
            GLES20.glColorMask(false, false, false, false);

            // draw portals in depth buffer.
            mPortal.draw(vpMatrix, mPortalModule1);
            mPortal.draw(vpMatrix, mPortalModule2);

            GLES20.glColorMask(true, true, true, true);

            /**
             * Render all scene.
             */
            mTeapot.draw(vpMatrix, mTeapotModule, mLight, mEyePoint);

            mSkybox.draw(vpMatrix, mSkyboxModule);
        }

        private float[] getPortalViewMatrix(float[] viewMatrix, float[] viewPortal, float[] canvasPortal) {
            /**
             * Draw the view of viewPortal to canvasPortal.
             *
             * What we need are:
             * 1. Move eye to camera portal.
             * 2. rotate 180 on y, since look inside become outside.
             * 3. Move camera's view to canvas portal.
             *
             * To avoid impact the module matrix, we should do these on View Matrix
             * Assume camera portal is portal 1 and view portal is portal 2, then the equation is:
             * [Translate to portal 2] * [Rotate as port 2] * (reverse direction to look) * [Rotate as port1]^(-1) * [Translate to port1]^(-1)
             * = [Tp2][Rp2] * [R180]* ([Tp1][Rp1])^(-1)
             *
             * => (Move scene to Canvas port) * [R180] * (Move Eye to Camera port)
             */
            float[] portalView = new float[16];

            float[] canvasPort = new float[16];
            float[] cameraPort = new float[16];
            Matrix.setIdentityM(canvasPort, 0);
            Matrix.setIdentityM(cameraPort, 0);

            /** Canvas port = Tp2 * Rp2.  Rp2 is turn right. (look to -z -> look to +x) */
            Matrix.scaleM(canvasPort, 0, canvasPortal, 0, 1, 1, 1);

            /** Camera port = (Tp1 * Rp1)^(-1), Rp1 is turn up. (look to -z -> look to +y) */
            Matrix.scaleM(cameraPort, 0, viewPortal, 0, 1, 1, 1);
            Matrix.invertM(cameraPort, 0, cameraPort, 0);

            Matrix.scaleM(portalView, 0, viewMatrix, 0, 1, 1, 1);
            Matrix.multiplyMM(portalView, 0, portalView, 0, canvasPort, 0);
            Matrix.rotateM(portalView, 0, 180, 0, 1, 0);
            Matrix.multiplyMM(portalView, 0, portalView, 0, cameraPort, 0);

            return portalView;
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
