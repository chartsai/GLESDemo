package idv.chatea.gldemo;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import idv.chatea.gldemo.gles10.Square;
import idv.chatea.gldemo.gles10.TextureSquare;

public class GLES1_Texture_Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(1);
        glSurfaceView.setRenderer(new MyRenderer());
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setContentView(glSurfaceView);
    }

    private class MyRenderer implements GLSurfaceView.Renderer {

        private TextureSquare mTextureSquare;
        private Square mSquare;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            mTextureSquare = new TextureSquare(gl, BitmapFactory.decodeResource(getResources(), R.drawable.block));
            mSquare = new Square();

            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);

            // make adjustments for screen ratio
            float ratio = (float) width / height;

            // set matrix to projection mode
            gl.glMatrixMode(GL10.GL_PROJECTION);

            // reset the matrix to its default state
            gl.glLoadIdentity();

            // apply the projection matrix
            gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            // Set GL_MODELVIEW transformation mode
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            // reset the matrix to its default state
            gl.glLoadIdentity();

            // When using GL_MODELVIEW, you must set the camera view
            GLU.gluLookAt(gl, 0, 0, 5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            mTextureSquare.draw(gl);
//            mSquare.draw(gl);
        }
    }
}
