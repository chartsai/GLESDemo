package idv.chatea.gldemo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import idv.chatea.gldemo.gles10.Triangle;

public class GLES1_Viewport_Activity extends Activity {

    private enum ViewportType {
        Small,
        Normal,
        Corner,
    }

    private ViewportType mViewportType = ViewportType.Normal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gles1_viewport);

        GLSurfaceView glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(1);
        glSurfaceView.setRenderer(new MyRenderer());
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        Button smallViewport = (Button) findViewById(R.id.smallViewport);
        Button normalViewport = (Button) findViewById(R.id.normalViewport);
        Button cornerViewport = (Button) findViewById(R.id.cornerViewport);

        smallViewport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewportType = ViewportType.Small;
            }
        });

        normalViewport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewportType = ViewportType.Normal;
            }
        });

        cornerViewport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewportType = ViewportType.Corner;
            }
        });
    }

    private class MyRenderer implements GLSurfaceView.Renderer {

        private int mSurfaceWidth;
        private int mSurfaceHeight;

        private Triangle mTriangle;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            mTriangle = new Triangle();

            gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);

            mSurfaceWidth = width;
            mSurfaceHeight = height;

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
            // clear frame

            switch (mViewportType) {
                case Small:
                    gl.glViewport(mSurfaceWidth / 4, mSurfaceHeight / 4, mSurfaceWidth / 2, mSurfaceHeight / 2);
                    break;
                case Normal:
                    gl.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
                    break;
                case Corner:
                    gl.glViewport(0, 0, mSurfaceWidth / 10, mSurfaceHeight / 10);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown ViewportType");
            }

            // Set GL_MODELVIEW transformation mode
            gl.glMatrixMode(GL10.GL_MODELVIEW);

            // reset the matrix to its default state
            gl.glLoadIdentity();

            // When using GL_MODELVIEW, you must set the camera view
            GLU.gluLookAt(gl, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            mTriangle.draw(gl);
        }
    }
}
