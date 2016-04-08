package idv.chatea.gldemo.gles20;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Please check the document of {@link idv.chatea.gldemo.GLES2_Vertex_Buffer_Activity}.
 */
public class MaxwellTriangle_VertexBuffer {
    private static final String VERTEX_CODE =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 aPosition;" +
            "attribute vec4 aColor;" +
            "varying vec4 vColor;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * aPosition;" +
            "  vColor = aColor;" +
            "}";

    private static final String FRAGMENT_CODE =
            "precision mediump float;" +
            "varying vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    /**
     * This vertex data has two part: {{0, 1, 2}, {3, 4, 5, 6}}
     * The first part (first 3 entries) are position data.
     * The second part (last 4 entries) are color data.
     */
    private static final float[] VERTEX_DATA = {
            0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, // center
            0.0f,  0.622008459f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, // top corner
            -0.25f, 0.155502108f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
            -0.5f, -0.311004243f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, // left corner
            0.0f, -0.311004243f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
            0.5f, -0.311004243f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, // right corner
            0.25f, 0.155502108f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
    };

    private static final int POSITION_DATA_SIZE = 3;
    private static final int COLOR_DATA_SIZE = 4;

    private static final int BYTES_PER_FLOAT = Float.SIZE / Byte.SIZE;

    private static final int VERTEX_DATA_STRIDE =
            (POSITION_DATA_SIZE + COLOR_DATA_SIZE) * BYTES_PER_FLOAT;

    private final byte[] mIndexData = {
            0, 1, 2, 3, 4, 5, 6, 1,
    };

    private int mProgram;
    int mMVPMatrixHandler;
    int mPositionHandle;
    int mColorHandler;

    private FloatBuffer mVertexBuffer;
    private ByteBuffer mIndexBuffer;

    public MaxwellTriangle_VertexBuffer() {
        /**
         * Setup the buffer of vertex.
         */
        ByteBuffer vbb = ByteBuffer.allocateDirect(4 * VERTEX_DATA.length);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(VERTEX_DATA);
        mVertexBuffer.position(0);

        /**
         * Setup the buffer of index.
         */
        mIndexBuffer = ByteBuffer.allocateDirect(mIndexData.length);
        mIndexBuffer.order(ByteOrder.nativeOrder());
        mIndexBuffer.put(mIndexData);
        mIndexBuffer.position(0);

        mProgram = Utils.createProgram(VERTEX_CODE, FRAGMENT_CODE);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mColorHandler = GLES20.glGetAttribLocation(mProgram, "aColor");
    }

    public void draw(float[] mvpMatrix) {

        GLES20.glUseProgram(mProgram);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mColorHandler);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mvpMatrix, 0);

        /**
         * We need to move the index of buffer to the first position data.
         */
        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_STRIDE, mVertexBuffer);

        /**
         * We need to move the index of buffer to the first color data.
         */
        mVertexBuffer.position(3);
        GLES20.glVertexAttribPointer(mColorHandler, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_STRIDE, mVertexBuffer);

        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, mIndexData.length, GLES20.GL_UNSIGNED_BYTE, mIndexBuffer);

        GLES20.glDisableVertexAttribArray(mColorHandler);
        GLES20.glDisableVertexAttribArray(mPositionHandle);

        GLES20.glUseProgram(0);
    }
}
