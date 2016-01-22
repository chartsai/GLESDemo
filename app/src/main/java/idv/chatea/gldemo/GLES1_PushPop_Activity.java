package idv.chatea.gldemo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import idv.chatea.gldemo.gles10.Square;
import idv.chatea.gldemo.gles10.Triangle;

public class GLES1_PushPop_Activity extends Activity {

    private float mTrianglePositionX;
    private float mSquarePositionX;

    private ProjectionMode mProjectionMode = ProjectionMode.Perspective;
    private enum ProjectionMode {
        Orthogonal,
        Perspective,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gles1_pushpop);

        GLSurfaceView glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(1);
        glSurfaceView.setRenderer(new MyRenderer());
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        SeekBar triangleSeekBar = (SeekBar) findViewById(R.id.triangleSeekBar);
        triangleSeekBar.setMax(20);
        triangleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Camera's z position is between -1 and 1.
                // Accuracy: 0.1
                mTrianglePositionX = (progress / 10.0f - 1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        triangleSeekBar.setProgress(5);

        SeekBar squareSeekBar = (SeekBar) findViewById(R.id.squareSeekBar);
        squareSeekBar.setMax(20);
        squareSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Camera's z position is between -1 and 1.
                // Accuracy: 0.1
                mSquarePositionX = (progress / 10.0f - 1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        squareSeekBar.setProgress(15);

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

            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();

            GLU.gluLookAt(gl, 0, 0, 5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            /** Draw Object **/

            gl.glPushMatrix();
            gl.glTranslatef(mTrianglePositionX, 0.0f, 0.0f);
            mTriangle.draw(gl);
            gl.glPopMatrix();

            gl.glPushMatrix();
            gl.glTranslatef(mSquarePositionX, 0.0f, 0.0f);
            mSquare.draw(gl);
            gl.glPopMatrix();
        }
    }
}
