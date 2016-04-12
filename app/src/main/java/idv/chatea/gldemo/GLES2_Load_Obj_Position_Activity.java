package idv.chatea.gldemo;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import idv.chatea.gldemo.gles20.ObjPositionObject;
import idv.chatea.gldemo.objloader.PositionOnlyObjLoader;

/**
 * TODO load normal (with lighting effects) and load texture
 */
public class GLES2_Load_Obj_Position_Activity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;
    private MyRenderer mRenderer;

    private float mPreviousY;
    private float mPreviousX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mRenderer = new MyRenderer();
        mGLSurfaceView.setRenderer(mRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
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

    private class MyRenderer implements GLSurfaceView.Renderer {
        private float[] mProjectMatrix = new float[16];
        private float[] mViewMatrix = new float[16];

        private static final float MOVEMENT_FACTOR_THETA = 180.0f / 320;
        private static final float MOVEMENT_FACTOR_PHI = 90.0f / 320;

        private float[] mEyePoint = new float[3];
        private float mViewDistance = 200.0f;
        private float mTheta = 90;
        private float mPhi = 0;

        private ObjPositionObject mObjObject;

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            GLES20.glClearColor(0.4f, 0.4f, 0.4f, 1.0f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);

            PositionOnlyObjLoader loader = new PositionOnlyObjLoader();

            mObjObject = new ObjPositionObject(loader.loadObjFile(GLES2_Load_Obj_Position_Activity.this, "teapot/teapot.obj"));
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);

            float ratio = (float) width / height;
            Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 2f, mViewDistance * 2);
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
            Matrix.multiplyMM(vpMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

            float[] moduleMatrix = new float[16];
            Matrix.setIdentityM(moduleMatrix, 0);
            Matrix.translateM(moduleMatrix, 0, 0, -50, 0);

            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, moduleMatrix, 0);
            mObjObject.draw(mvpMatrix);
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
                    mGLSurfaceView.requestRender();
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
