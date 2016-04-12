package idv.chatea.gldemo.gles20;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import idv.chatea.gldemo.objloader.PositionOnlyObjLoader;

public class ObjPositionObject {
    private static final String VERTEX_CODE =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec3 aPosition;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vec4(aPosition, 1.0);" +
            "}";

    private static final String FRAGMENT_CODE =
            "precision mediump float;" +
            "void main() {" +
            "  gl_FragColor = vec4(1.0);" +
            "}";

    private static final int BYTES_PER_INT = Integer.SIZE / Byte.SIZE;
    private static final int BYTES_PER_FLOAT = Float.SIZE / Byte.SIZE;

    private static final int POSITION_DATA_SIZE = 3;
    private static final int TEXTURE_COORD_DATA_SIZE = 0;

    private static final int POSITION_OFFSET = 0 * BYTES_PER_FLOAT;
    private static final int TEXTURE_COORD_OFFSET = POSITION_DATA_SIZE * BYTES_PER_FLOAT;

    private static final int VERTEX_DATA_STRIDE =
            (POSITION_DATA_SIZE + TEXTURE_COORD_DATA_SIZE) * BYTES_PER_FLOAT;

    private int mIndexNumber;

    private int[] mVertexGLBuffer = new int[1];
    private int[] mIndexGLBuffer = new int[1];

    private int mProgram;
    private int mMVPMatrixHandler;
    private int mPositionHandle;

    public ObjPositionObject(PositionOnlyObjLoader.ObjPositionData objData) {
        float[] positionDataData = objData.positions;
        int[] indexData = objData.indices;

        ByteBuffer vbb = ByteBuffer.allocateDirect(BYTES_PER_FLOAT * positionDataData.length);
        vbb.order(ByteOrder.nativeOrder());
        vbb.asFloatBuffer().put(positionDataData);
        vbb.position(0);

        GLES20.glGenBuffers(1, mVertexGLBuffer, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexGLBuffer[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vbb.capacity(), vbb, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);


        ByteBuffer ibb = ByteBuffer.allocateDirect(BYTES_PER_INT * indexData.length);
        ibb.order(ByteOrder.nativeOrder());
        ibb.asIntBuffer().put(indexData);
        ibb.position(0);

        GLES20.glGenBuffers(1, mIndexGLBuffer, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexGLBuffer[0]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibb.capacity(), ibb, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        mIndexNumber = indexData.length;


        mProgram = Utils.createProgram(VERTEX_CODE, FRAGMENT_CODE);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
    }

    public void draw(float[] mvpMatrix) {
        boolean cullFace = GLES20.glIsEnabled(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        GLES20.glUseProgram(mProgram);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexGLBuffer[0]);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexGLBuffer[0]);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mvpMatrix, 0);
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_STRIDE, POSITION_OFFSET);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndexNumber, GLES20.GL_UNSIGNED_INT, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glUseProgram(0);

        if (cullFace) {
            GLES20.glEnable(GLES20.GL_CULL_FACE);
        }
    }
}
