package idv.chatea.gldemo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLES1_PushPop_Activity extends Activity {

    private GLSurfaceView mGLSurfaceView;

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

        mGLSurfaceView = (GLSurfaceView)findViewById(R.id.glSurfaceView);
        mGLSurfaceView.setEGLContextClientVersion(1);
        mGLSurfaceView.setRenderer(new MyRenderer());
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

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

    public static class Square {

        private final FloatBuffer vertexBuffer;
        private final ShortBuffer drawListBuffer;

        // number of coordinates per vertex in this array
        static final int COORDS_PER_VERTEX = 3;
        static float squareCoords[] = {
                -0.5f,  0.5f, 0.0f,   // top left
                -0.5f, -0.5f, 0.0f,   // bottom left
                0.5f, -0.5f, 0.0f,   // bottom right
                0.5f,  0.5f, 0.0f }; // top right

        private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

        float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

        /**
         * Sets up the drawing object data for use in an OpenGL ES context.
         */
        public Square() {
            // initialize vertex byte buffer for shape coordinates
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    squareCoords.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(squareCoords);
            vertexBuffer.position(0);

            // initialize byte buffer for the draw list
            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(drawOrder);
            drawListBuffer.position(0);
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
            gl.glColor4f(       // set color
                    color[0], color[1],
                    color[2], color[3]);
            gl.glVertexPointer( // point to vertex data:
                    COORDS_PER_VERTEX,
                    GL10.GL_FLOAT, 0, vertexBuffer);
            gl.glDrawElements(  // draw shape:
                    GL10.GL_TRIANGLES,
                    drawOrder.length, GL10.GL_UNSIGNED_SHORT,
                    drawListBuffer);

            // Disable vertex array drawing to avoid
            // conflicts with shapes that don't use it
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        }
    }
}
