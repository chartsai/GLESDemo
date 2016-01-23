package idv.chatea.gldemo;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import idv.chatea.gldemo.gles20.Ball;

public class GLES2_Ball_Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(new MyRenderer());
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setContentView(glSurfaceView);
    }

    private class MyRenderer implements GLSurfaceView.Renderer {

        private Ball mBall;

        private float[] mProjectMatrix = new float[16];
        private float[] mViewMatrix = new float[16];

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {

            mBall = new Ball();

            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);

            float ratio = (float) width / height;
            Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 1.5f, 20);

            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }

        @Override
        public void onDrawFrame(GL10 unused) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            Matrix.setLookAtM(mViewMatrix, 0,
                    0f, 0f, 5f,
                    0f, 0f, 0f,
                    0f, 1f, 0);

            float[] mvpMatrix = new float[16];
            Matrix.multiplyMM(mvpMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

            float[] moduleMatrix = new float[16];
            Matrix.setIdentityM(moduleMatrix, 0);

            Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, moduleMatrix, 0);

            mBall.draw(mvpMatrix);
        }
    }
}
