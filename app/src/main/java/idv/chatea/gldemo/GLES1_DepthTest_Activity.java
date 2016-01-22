package idv.chatea.gldemo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import idv.chatea.gldemo.gles10.Square;
import idv.chatea.gldemo.gles10.Triangle;

public class GLES1_DepthTest_Activity extends Activity {

    private float mTrianglePositionZ;
    private float mSquarePositionZ;

    private boolean mDepthTestEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gles1_depthtest);

        GLSurfaceView glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(1);
        glSurfaceView.setRenderer(new MyRenderer());
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        SeekBar triangleSeekBar = (SeekBar) findViewById(R.id.triangleSeekBar);
        triangleSeekBar.setMax(60);
        triangleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Camera's z position is between -1 and 1.
                // Accuracy: 0.1
                mTrianglePositionZ = (progress / 10.0f - 3);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        triangleSeekBar.setProgress(30);

        SeekBar squareSeekBar = (SeekBar) findViewById(R.id.squareSeekBar);
        squareSeekBar.setMax(60);
        squareSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Camera's z position is between -3 and 3.
                // Accuracy: 0.1
                mSquarePositionZ = (progress / 10.0f - 3);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        squareSeekBar.setProgress(30);

        CheckBox enableDepthTestBox = (CheckBox) findViewById(R.id.enableDepthTest);
        enableDepthTestBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDepthTestEnable = isChecked;
            }
        });
        enableDepthTestBox.setChecked(false);
    }

    private class MyRenderer implements GLSurfaceView.Renderer {

        private Triangle mTriangle;
        private Square mSquare;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            mTriangle = new Triangle();
            mSquare = new Square();

            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);

            float ratio = (float) width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();

            gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();

            GLU.gluLookAt(gl, 0, 0, 5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            if (mDepthTestEnable) {
                gl.glEnable(GL10.GL_DEPTH_TEST);
            } else {
                gl.glDisable(GL10.GL_DEPTH_TEST);
            }

            /** Draw Object **/

            gl.glPushMatrix();
            gl.glTranslatef(0.0f, 0.0f, mTrianglePositionZ);
            gl.glTranslatef(0.25f, 0.0f, 0.0f);
            mTriangle.draw(gl);
            gl.glPopMatrix();

            gl.glPushMatrix();
            gl.glTranslatef(0.0f, 0.0f, mSquarePositionZ);
            gl.glTranslatef(-0.25f, 0.0f, 0.0f);
            mSquare.draw(gl);
            gl.glPopMatrix();
        }
    }
}
