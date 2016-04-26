package idv.chatea.gldemo.gles20;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RectangleTexture {
    private static final String VERTEX_CODE =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 aPosition;" +
            "attribute vec2 aTextureCoord;" +
            "varying vec2 vTextureCoord;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * aPosition;" +
            "  vTextureCoord = aTextureCoord;" +
            "}";

    private static final String FRAGMENT_CODE =
            "precision mediump float;" +
            "uniform sampler2D uTextureSample;" +
            "varying vec2 vTextureCoord;" +
            "void main() {" +
            "  gl_FragColor = texture2D(uTextureSample, vTextureCoord);" +
            "}";

    private static final float[] VERTEX_DATA = {
            -0.5f, -0.5f, 0, 0, 1, // left-bottom corner
             0.5f, -0.5f, 0, 1, 1, // right-bottom corner
            -0.5f,  0.5f, 0, 0, 0, // left-top corner
             0.5f,  0.5f, 0, 1, 0, // right-top corner
    };

    private static final int BYTES_PER_FLOAT = Float.SIZE / Byte.SIZE;

    private static final int POSITION_DATA_SIZE = 3;
    private static final int TEXTURE_COORD_DATA_SIZE = 2;

    private static final int POSITION_OFFSET = 0 * BYTES_PER_FLOAT;
    private static final int TEXTURE_COORD_OFFSET = POSITION_DATA_SIZE * BYTES_PER_FLOAT;

    private static final int VERTEX_DATA_STRIDE =
            (POSITION_DATA_SIZE + TEXTURE_COORD_DATA_SIZE) * BYTES_PER_FLOAT;

    private static final byte[] INDEX_DATA = {
            0, 1, 2, 2, 1, 3,
    };

    private int[] mVertexGLBuffer = new int[1];
    private int[] mIndexGLBuffer = new int[1];
    private int[] mGLTexture = new int[1];

    private int mProgram;
    private int mMVPMatrixHandler;
    private int mPositionHandle;
    private int mTextureCoordHandler;
    private int mTextureSampleHandler;

    public RectangleTexture(Bitmap bitmap) {
        init();

        setPicture(bitmap);
    }

    public RectangleTexture(int textureId) {
        init();

        mGLTexture[0] = textureId;
    }

    public void setPicture(Bitmap bitmap) {
        if (!GLES20.glIsTexture(mGLTexture[0])) {
            GLES20.glGenTextures(1, mGLTexture, 0);
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGLTexture[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private void init() {
        ByteBuffer vbb = ByteBuffer.allocateDirect(BYTES_PER_FLOAT * VERTEX_DATA.length);
        vbb.order(ByteOrder.nativeOrder());
        vbb.asFloatBuffer().put(VERTEX_DATA);
        vbb.position(0);

        GLES20.glGenBuffers(1, mVertexGLBuffer, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexGLBuffer[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vbb.capacity(), vbb, GLES20.GL_STATIC_DRAW);

        ByteBuffer ibb = ByteBuffer.allocateDirect(INDEX_DATA.length);
        ibb.order(ByteOrder.nativeOrder());
        ibb.put(INDEX_DATA);
        ibb.position(0);

        GLES20.glGenBuffers(1, mIndexGLBuffer, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexGLBuffer[0]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibb.capacity(), ibb, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        mProgram = Utils.createProgram(VERTEX_CODE, FRAGMENT_CODE);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mTextureCoordHandler = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        mTextureSampleHandler = GLES20.glGetUniformLocation(mProgram, "uTextureSample");
    }

    public void draw(float[] mvpMatrix) {

        GLES20.glUseProgram(mProgram);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mTextureCoordHandler);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexGLBuffer[0]);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexGLBuffer[0]);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGLTexture[0]);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mvpMatrix, 0);
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_STRIDE, POSITION_OFFSET);
        GLES20.glVertexAttribPointer(mTextureCoordHandler, TEXTURE_COORD_DATA_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_STRIDE, TEXTURE_COORD_OFFSET);
        GLES20.glUniform1i(mTextureSampleHandler, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, INDEX_DATA.length, GLES20.GL_UNSIGNED_BYTE, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandler);

        GLES20.glUseProgram(0);
    }
}
