package idv.chatea.gldemo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLES1_ColorTriangle_Activity extends Activity {

    private GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(1);
        mGLSurfaceView.setRenderer(new MyRenderer());
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setContentView(mGLSurfaceView);
    }

    private class MyRenderer implements GLSurfaceView.Renderer {

        private ColorTriangle mTriangle;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mTriangle = new ColorTriangle();
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

            mTriangle.draw(gl);
        }
    }

    static class ColorTriangle {

        private final FloatBuffer vertexBuffer;
        private final FloatBuffer colorBuffer;
        private final ByteBuffer indexBuffer;

        static final int COORDS_PER_VERTEX = 3;
        static float triangleCoords[] = {
                0.0f,  0.622008459f, 0.0f,
                -0.5f, -0.311004243f, 0.0f,
                0.5f, -0.311004243f, 0.0f,
        };

        static final int COLORS_PER_VERTEX = 4;
        static float colors[] = {
                1.0f, 0.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,
        };

        static final byte indices[] = {
                0, 1, 2,
        };

        /**
         * Sets up the drawing object data for use in an OpenGL ES context.
         */
        public ColorTriangle() {
            /** Init Vertices buffer **/
            ByteBuffer vbb = ByteBuffer.allocateDirect(triangleCoords.length * 4);
            vbb.order(ByteOrder.nativeOrder());
            vertexBuffer = vbb.asFloatBuffer();
            vertexBuffer.put(triangleCoords);
            vertexBuffer.position(0);

            /** Init color buffer **/
            ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
            cbb.order(ByteOrder.nativeOrder());
            colorBuffer = cbb.asFloatBuffer();
            colorBuffer.put(colors);
            colorBuffer.position(0);

            /** Init order buffer **/
            indexBuffer = ByteBuffer.allocateDirect(indices.length);
            indexBuffer.order(ByteOrder.nativeOrder());
            indexBuffer.put(indices);
            indexBuffer.position(0);
        }

        public void draw(GL10 gl) {
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

            gl.glVertexPointer(COORDS_PER_VERTEX, GL10.GL_FLOAT, 0, vertexBuffer);
            gl.glColorPointer(COLORS_PER_VERTEX, GL10.GL_FLOAT, 0, colorBuffer);
            gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);

            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        }
    }
}
