package idv.chatea.gldemo.gles20;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import idv.chatea.gldemo.GLES2_Use_GLBuffer_Activity;

/**
 * Please check the document of {@link GLES2_Use_GLBuffer_Activity}.
 */
public class MaxwellTriangle_GLBuffer {
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

    /**
     * position data is the first.
     * The origin unit of offset is float, we need to convert it to the byte.
     */
    private static final int POSITION_OFFSET = 0 * BYTES_PER_FLOAT;
    /**
     * color data is followed by the position data.
     * The origin unit of offset is float, we need to convert it to the byte.
     */
    private static final int COLOR_OFFSET = POSITION_DATA_SIZE * BYTES_PER_FLOAT;
    /**
     * The size of every complete vertex data. (including position and color)
     * The origin unit of size is float, we need to convert it to the byte.
     */
    private static final int VERTEX_DATA_STRIDE =
            (POSITION_DATA_SIZE + COLOR_DATA_SIZE) * BYTES_PER_FLOAT;

    private final byte[] mIndexData = {
            0, 1, 2, 3, 4, 5, 6, 1,
    };

    /**
     * the GL buffer used to store vertex data.
     */
    private int[] mVertexGLBuffer = new int[1];
    /**
     * the GL buffer used to store index data.
     */
    private int[] mIndexGLBuffer = new int[1];

    private int mProgram;
    int mMVPMatrixHandler;
    int mPositionHandle;
    int mColorHandler;

    public MaxwellTriangle_GLBuffer() {
        ByteBuffer vbb = ByteBuffer.allocateDirect(4 * VERTEX_DATA.length);
        vbb.order(ByteOrder.nativeOrder());
        /**
         * we need to put vertex data, which is float[].
         * So convert as FloatBuffer to put.
         */
        vbb.asFloatBuffer().put(VERTEX_DATA);
        vbb.position(0);

        /**
         * We bind a generated buffer to GL_ARRAY_BUFFER, and put data into GL_ARRAY_BUFFER.
         * This means, the data is put into the generated buffer.
         *
         * **Important**, glBindBuffer **ONLY** accept {@link ByteBuffer}. We can not give
         * other buffer types (for instance, FloatBuffer or IntegerBuffer) to it.
         */
        GLES20.glGenBuffers(1, mVertexGLBuffer, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexGLBuffer[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vbb.capacity(), vbb, GLES20.GL_STATIC_DRAW);

        ByteBuffer ibb = ByteBuffer.allocateDirect(mIndexData.length);
        ibb.order(ByteOrder.nativeOrder());
        ibb.put(mIndexData);
        ibb.position(0);

        GLES20.glGenBuffers(1, mIndexGLBuffer, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexGLBuffer[0]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibb.capacity(), ibb, GLES20.GL_STATIC_DRAW);


        /**
         * We have setup all of our buffer. Disable them to avoid other one modifying/using them.
         * When we want to use these buffer again, we will bind them again. (See {@link #draw})
         */
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        mProgram = Utils.createProgram(VERTEX_CODE, FRAGMENT_CODE);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mColorHandler = GLES20.glGetAttribLocation(mProgram, "aColor");
    }

    public void draw(float[] mvpMatrix) {

        GLES20.glUseProgram(mProgram);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mColorHandler);

        /**
         * Bind the buffer we setup before.
         * When GL_ARRAY_BUFFER is bound, glVertexAttribPointer will use offset instead of buffer
         * pointer in the last argument.
         * This offset is used to shift the selected data in bound buffer.
         *
         * When GL_ELEMENT_ARRAY_BUFFER is bound, glDrawElements will use offset instead of buffer
         * pointer in the last argument.
         * This offset is used to shift the selected data in bound buffer.
         */
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexGLBuffer[0]);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexGLBuffer[0]);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mvpMatrix, 0);

        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_STRIDE, POSITION_OFFSET);

        GLES20.glVertexAttribPointer(mColorHandler, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_STRIDE, COLOR_OFFSET);

        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, mIndexData.length, GLES20.GL_UNSIGNED_BYTE, 0);

        /**
         * The buffer have been used, unbind them to avoid other one modifying/using them.
         */
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES20.glDisableVertexAttribArray(mColorHandler);
        GLES20.glDisableVertexAttribArray(mPositionHandle);

        GLES20.glUseProgram(0);
    }
}
