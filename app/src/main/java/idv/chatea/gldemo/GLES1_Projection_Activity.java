package idv.chatea.gldemo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import idv.chatea.gldemo.gles10.Triangle;

public class GLES1_Projection_Activity extends Activity {

    private float mCameraPositionZ;

    private ProjectionMode mProjectionMode = ProjectionMode.Perspective;
    private enum ProjectionMode {
        Orthogonal,
        Perspective,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gles1_projection);

        GLSurfaceView glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(1);
        glSurfaceView.setRenderer(new MyRenderer());
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        final TextView textView = (TextView) findViewById(R.id.progressText);

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Camera's z position is between -5 and 5.
                // Accuracy: 0.1
                mCameraPositionZ = (progress/10.0f - 5);
                String positionString = String.format("camera at (0,0,%.1f), look to (0,0,0)", mCameraPositionZ);
                textView.setText(positionString);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBar.setProgress(50);

        Button orthogonalButton = (Button) findViewById(R.id.orthogonalButton);
        Button perspectiveButton = (Button) findViewById(R.id.perspectiveButton);

        orthogonalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProjectionMode = ProjectionMode.Orthogonal;
            }
        });

        perspectiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProjectionMode = ProjectionMode.Perspective;
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

            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);

            mSurfaceWidth = width;
            mSurfaceHeight = height;
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            float ratio = (float) mSurfaceWidth / mSurfaceHeight;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();

            switch (mProjectionMode) {
                case Perspective:
                    gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);
                    break;
                case Orthogonal:
                    gl.glOrthof(-ratio, ratio, -1, 1, 3, 7);
                    break;
                default:
                    throw new IllegalStateException("Projection Mode is not set");
            }

            // Set GL_MODELVIEW transformation mode
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            // reset the matrix to its default state
            gl.glLoadIdentity();

            // When using GL_MODELVIEW, you must set the camera view
            GLU.gluLookAt(gl, 0, 0, mCameraPositionZ, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            mTriangle.draw(gl);
        }
    }
}
