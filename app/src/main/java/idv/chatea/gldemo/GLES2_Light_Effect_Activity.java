package idv.chatea.gldemo;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import idv.chatea.gldemo.lighting.Light;
import idv.chatea.gldemo.lighting.LightBlockBall;

/**
 * The light position is fixed at (x,y,z) = (20, 20, 20)
 */
public class GLES2_Light_Effect_Activity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;
    private MyRenderer mRenderer;

    private SeekBar mAmbientRedSeekBar;
    private SeekBar mAmbientGreenSeekBar;
    private SeekBar mAmbientBlueSeekBar;
    private SeekBar mDiffusionRedSeekBar;
    private SeekBar mDiffusionGreenSeekBar;
    private SeekBar mDiffusionBlueSeekBar;
    private SeekBar mSpecularRedSeekBar;
    private SeekBar mSpecularGreenSeekBar;
    private SeekBar mSpecularBlueSeekBar;

    private float mPreviousY;
    private float mPreviousX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gles2_light_effect);

        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);

        mGLSurfaceView.setEGLContextClientVersion(2);
        mRenderer = new MyRenderer();
        mGLSurfaceView.setRenderer(mRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        mGLSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
        });

        mAmbientRedSeekBar = (SeekBar) findViewById(R.id.ambientRedSeekBar);
        mAmbientGreenSeekBar = (SeekBar) findViewById(R.id.ambientGreenSeekBar);
        mAmbientBlueSeekBar = (SeekBar) findViewById(R.id.ambientBlueSeekBar);
        mDiffusionRedSeekBar = (SeekBar) findViewById(R.id.diffusionRedSeekBar);
        mDiffusionGreenSeekBar = (SeekBar) findViewById(R.id.diffusionGreenSeekBar);
        mDiffusionBlueSeekBar = (SeekBar) findViewById(R.id.diffusionBlueSeekBar);
        mSpecularRedSeekBar = (SeekBar) findViewById(R.id.specularRedSeekBar);
        mSpecularGreenSeekBar = (SeekBar) findViewById(R.id.specularGreenSeekBar);
        mSpecularBlueSeekBar = (SeekBar) findViewById(R.id.specularBlueSeekBar);
    }

    private class MyRenderer implements GLSurfaceView.Renderer {
        private float[] mProjectMatrix = new float[16];
        private float[] mViewMatrix = new float[16];

        private static final float MOVEMENT_FACTOR_THETA = 180.0f / 320;
        private static final float MOVEMENT_FACTOR_PHI = 90.0f / 320;

        private float[] mEyePoint = new float[3];
        private float mViewDistance = 5;
        private float mTheta = 90;
        private float mPhi = 0;

        private LightBlockBall mBall;
        private Light mLight;

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);

            Context context = GLES2_Light_Effect_Activity.this;

            mBall = new LightBlockBall();

            mLight = new Light();
            mLight.position = new float[]{20, 20, 20, 1.0f};
            mLight.ambientChannel = new float[]{0.15f, 0.15f, 0.15f, 1.0f};
            mLight.diffusionChannel = new float[]{0.3f, 0.3f, 0.3f, 1.0f};
            mLight.specularChannel = new float[]{0.4f, 0.4f, 0.4f, 1.0f};
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);

            float ratio = (float) width / height;
            Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 2f, mViewDistance * 2);
        }

        @Override
        public void onDrawFrame(GL10 unused) {

            updateLightColor();

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

            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, moduleMatrix, 0);
            mBall.draw(mvpMatrix, mLight, mEyePoint);
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

        private void updateLightColor() {
            mLight.ambientChannel[0] = mAmbientRedSeekBar.getProgress() / 100.f;
            mLight.ambientChannel[1] = mAmbientGreenSeekBar.getProgress() / 100.f;
            mLight.ambientChannel[2] = mAmbientBlueSeekBar.getProgress() / 100.f;

            mLight.diffusionChannel[0] = mDiffusionRedSeekBar.getProgress() / 100.f;
            mLight.diffusionChannel[1] = mDiffusionGreenSeekBar.getProgress() / 100.f;
            mLight.diffusionChannel[2] = mDiffusionBlueSeekBar.getProgress() / 100.f;

            mLight.specularChannel[0] = mSpecularRedSeekBar.getProgress() / 100.f;
            mLight.specularChannel[1] = mSpecularGreenSeekBar.getProgress() / 100.f;
            mLight.specularChannel[2] = mSpecularBlueSeekBar.getProgress() / 100.f;
        }
    }
}
