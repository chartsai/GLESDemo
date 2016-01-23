package idv.chatea.gldemo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import idv.chatea.gldemo.gles10.PointList;

public class GLES1_DrawElements_Activity extends Activity {

    private final String[] mDrawMode = {
            "GL_POINTS",
            "GL_LINE_STRIP",
            "GL_LINE_LOOP",
            "GL_LINES",
            "GL_TRIANGLE_STRIP",
            "GL_TRIANGLE_FAN",
            "GL_TRIANGLES",
    };

    private final int[] mModeList = {
            GL10.GL_POINTS,
            GL10.GL_LINE_STRIP,
            GL10.GL_LINE_LOOP,
            GL10.GL_LINES,
            GL10.GL_TRIANGLE_STRIP,
            GL10.GL_TRIANGLE_FAN,
            GL10.GL_TRIANGLES,
    };

    private int mCurrentMode = mModeList[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gles1_drawelements);

        GLSurfaceView glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(1);
        glSurfaceView.setRenderer(new MyRenderer());
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        Spinner renderModeSpinner = (Spinner) findViewById(R.id.renderModeSpinner);
        renderModeSpinner.setAdapter(new ArrayAdapter(this, android.R.layout.simple_spinner_item, mDrawMode));
        renderModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentMode = mModeList[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        renderModeSpinner.setSelection(0);
    }

    private class MyRenderer implements GLSurfaceView.Renderer {

        private PointList points;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            points = new PointList();

            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

            gl.glPointSize(10.0f);
            gl.glLineWidth(5.0f);
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

            points.draw(gl, mCurrentMode);
        }
    }
}
