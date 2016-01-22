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

public class GLES1_Triangle_Activity extends Activity {

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

        private Triangle mTriangle;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            mTriangle = new Triangle();

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
            GLU.gluLookAt(gl, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            mTriangle.draw(gl);
        }
    }

    static class Triangle {

        private final FloatBuffer vertexBuffer;

        // number of coordinates per vertex in this array
        static final int COORDS_PER_VERTEX = 3;
        static float triangleCoords[] = {
                // in counterclockwise order:
                0.0f,  0.622008459f, 0.0f,// top
                -0.5f, -0.311004243f, 0.0f,// bottom left
                0.5f, -0.311004243f, 0.0f // bottom right
        };

        float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };

        /**
         * Sets up the drawing object data for use in an OpenGL ES context.
         */
        public Triangle() {
            // initialize vertex byte buffer for shape coordinates
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (number of coordinate values * 4 bytes per float)
                    triangleCoords.length * 4);
            // use the device hardware's native byte order
            bb.order(ByteOrder.nativeOrder());

            // create a floating point buffer from the ByteBuffer
            vertexBuffer = bb.asFloatBuffer();
            // add the coordinates to the FloatBuffer
            vertexBuffer.put(triangleCoords);
            // set the buffer to read the first coordinate
            vertexBuffer.position(0);
        }

        /**
         * Encapsulates the OpenGL ES instructions for drawing this shape.
         *
         * @param gl - The OpenGL ES context in which to draw this shape.
         */
        public void draw(GL10 gl) {
            // Since this shape uses vertex arrays, enable them
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

            // draw the shape
            gl.glColor4f(       // set color:
                    color[0], color[1],
                    color[2], color[3]);
            gl.glVertexPointer( // point to vertex data:
                    COORDS_PER_VERTEX,
                    GL10.GL_FLOAT, 0, vertexBuffer);
            gl.glDrawArrays(    // draw shape:
                    GL10.GL_TRIANGLES, 0,
                    triangleCoords.length / COORDS_PER_VERTEX);

            // Disable vertex array drawing to avoid
            // conflicts with shapes that don't use it
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        }
    }
}
