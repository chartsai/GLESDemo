package idv.chatea.gldemo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLES1_Background_Activity extends Activity {

    // clear colors
    private float mR;
    private float mG;
    private float mB;
    private float mA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gles1_background);

        GLSurfaceView glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(1);
        glSurfaceView.setRenderer(new MyRenderer());
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        Button redButton = (Button) findViewById(R.id.redButton);
        Button greenButton = (Button) findViewById(R.id.greenButton);
        Button blueButton = (Button) findViewById(R.id.blueButton);

        redButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClearColor(1.0f, 0.0f, 0.0f, 1.0f);
            }
        });

        greenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClearColor(0.0f, 1.0f, 0.0f, 1.0f);
            }
        });

        blueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClearColor(0.0f, 0.0f, 1.0f, 1.0f);
            }
        });
    }

    private void setClearColor(float r, float g, float b, float a) {
        this.mR = r;
        this.mG = g;
        this.mB = b;
        this.mA = a;
    }

    private class MyRenderer implements GLSurfaceView.Renderer {

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

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
            // clear frame
            gl.glClearColor(mR, mG, mB, mA);

            // Set GL_MODELVIEW transformation mode
            gl.glMatrixMode(GL10.GL_MODELVIEW);

            // reset the matrix to its default state
            gl.glLoadIdentity();

            // When using GL_MODELVIEW, you must set the camera view
            GLU.gluLookAt(gl, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        }
    }
}
