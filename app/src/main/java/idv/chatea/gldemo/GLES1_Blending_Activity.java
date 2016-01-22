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

import idv.chatea.gldemo.gles10.ColorfulSquare;

public class GLES1_Blending_Activity extends Activity {

    private float mTrianglePositionZ;
    private float mSquarePositionZ;

    private boolean mDepthTestEnable;
    private boolean mBlendingEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gles1_blending);

        GLSurfaceView glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(1);
        glSurfaceView.setRenderer(new MyRenderer());
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        SeekBar triangleSeekBar = (SeekBar) findViewById(R.id.leftSquare);
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

        SeekBar squareSeekBar = (SeekBar) findViewById(R.id.rightSquare);
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

        CheckBox enableDepthBox = (CheckBox) findViewById(R.id.enableDepthTest);
        enableDepthBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDepthTestEnable = isChecked;
            }
        });
        enableDepthBox.setChecked(false);

        CheckBox enableBlendingBox = (CheckBox) findViewById(R.id.enableBlending);
        enableBlendingBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBlendingEnable = isChecked;
            }
        });
        enableBlendingBox.setChecked(false);
    }

    private class MyRenderer implements GLSurfaceView.Renderer {

        private ColorfulSquare mLeftSquare;
        private ColorfulSquare mRightSquare;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            mLeftSquare = new ColorfulSquare(1, 0, 0, 0.7f);
            mRightSquare = new ColorfulSquare(0, 1, 0, 0.3f);

            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
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

            if (mBlendingEnable) {
                gl.glEnable(GL10.GL_BLEND);
            } else {
                gl.glDisable(GL10.GL_BLEND);
            }

            /** Draw Object **/

            gl.glPushMatrix();
            gl.glTranslatef(0.0f, 0.0f, mTrianglePositionZ);
            gl.glTranslatef(0.25f, 0.0f, 0.0f);
            mLeftSquare.draw(gl);
            gl.glPopMatrix();

            gl.glPushMatrix();
            gl.glTranslatef(0.0f, 0.0f, mSquarePositionZ);
            gl.glTranslatef(-0.25f, 0.0f, 0.0f);
            mRightSquare.draw(gl);
            gl.glPopMatrix();
        }
    }
}
