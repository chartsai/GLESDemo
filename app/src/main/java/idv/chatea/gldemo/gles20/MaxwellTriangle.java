package idv.chatea.gldemo.gles20;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import idv.chatea.gldemo.GLES2_Use_GLBuffer_Activity;

/**
 * Please check the document of {@link GLES2_Use_GLBuffer_Activity}.
 */
public class MaxwellTriangle {
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
    private static final float[] POSITION_DATA = {
            0.0f, 0.0f, 0.0f, // center
            0.0f,  0.622008459f, 0.0f, // top corner
            -0.25f, 0.155502108f, 0.0f,
            -0.5f, -0.311004243f, 0.0f, // left corner
            0.0f, -0.311004243f, 0.0f,
            0.5f, -0.311004243f, 0.0f, // right corner
            0.25f, 0.155502108f, 0.0f,
    };

    private static final float[] COLOR_DATA = {
            1.0f, 1.0f, 1.0f, 1.0f, // center
            1.0f, 0.0f, 0.0f, 1.0f, // top corner
            1.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f, // left corner
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f, // right corner
            1.0f, 0.0f, 1.0f, 1.0f,
    };

    private static final int POSITION_DATA_SIZE = 3;
    private static final int COLOR_DATA_SIZE = 4;

    private final byte[] mIndexData = {
            0, 1, 2, 3, 4, 5, 6, 1,
    };

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;
    private ByteBuffer mIndexBuffer;

    private int mProgram;
    int mMVPMatrixHandler;
    int mPositionHandle;
    int mColorHandler;

    public MaxwellTriangle() {
        ByteBuffer pbb = ByteBuffer.allocateDirect(4 * POSITION_DATA.length);
        pbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = pbb.asFloatBuffer().put(POSITION_DATA);
        mVertexBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(4 * COLOR_DATA.length);
        cbb.order(ByteOrder.nativeOrder());
        mColorBuffer = cbb.asFloatBuffer().put(COLOR_DATA);
        mColorBuffer.position(0);

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
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glVertexAttribPointer(mColorHandler, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, 0, mColorBuffer);

        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, mIndexData.length, GLES20.GL_UNSIGNED_BYTE, mIndexBuffer);

        GLES20.glDisableVertexAttribArray(mColorHandler);
        GLES20.glDisableVertexAttribArray(mPositionHandle);

        GLES20.glUseProgram(0);
    }
}
