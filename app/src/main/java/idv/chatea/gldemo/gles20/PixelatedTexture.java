package idv.chatea.gldemo.gles20;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PixelatedTexture {

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
            "uniform sampler2D uTextureLoc;" +
            "uniform vec2 uStep;" +
            "uniform int uSampleInStep;" +
            "varying vec2 vTextureCoord;" +
            "void main() {" +
            "  float sampleRadius = float((uSampleInStep - 1) / 2);" +
            "  vec2 sampledCoord = floor(vTextureCoord / uStep + vec2(0.5)) * uStep;" + // plus 0.5 for round.
            "  vec4 sum = vec4(0.0);" +
            "  vec2 shift = uStep / vec2(float(uSampleInStep));" +
            "  for (float i = -sampleRadius; i <= sampleRadius; i++) {" +
            "    float xCoord = sampledCoord.x + i * shift.x;" +
            "    for (float j = -sampleRadius; j < sampleRadius; j++) {" +
            "      float yCoord = sampledCoord.y + j * shift.y;" +
            "      sum += texture2D(uTextureLoc, vec2(xCoord, yCoord));" +
            "    }" +
            "  }" +
            "  vec4 finalColor = sum / vec4(float(uSampleInStep * uSampleInStep));" +
            "  if (finalColor.a == 0.0) discard;" +
            "  gl_FragColor = finalColor;" +
            "}";

    /**
     * This value is used to sample the fragment color inner pixel.
     * The pixel color is determined by (value) * (value) matrix for average convolution.
     * <br>
     * If value is 1, the pixel color is determined by the center color of pixel.
     * Since convolution use lots of computing power, lease keep this value <= 7.
     */
    private static final int SAMPLE_IN_STEP = 5;

    /**
     * Normalized unit for every edge.
     */
    private static final float EDGE_LENGTH = 4.0f;

    private float[] VERTEX_DATA = {
            -EDGE_LENGTH / 2, -EDGE_LENGTH / 2, 0, 0, 1, // left-bottom corner
             EDGE_LENGTH / 2, -EDGE_LENGTH / 2, 0, 1, 1, // right-bottom corner
            -EDGE_LENGTH / 2,  EDGE_LENGTH / 2, 0, 0, 0, // left-top corner
             EDGE_LENGTH / 2,  EDGE_LENGTH / 2, 0, 1, 0, /// right-top corner
    };

    private static final int BYTES_PER_FLOAT = Float.SIZE / Byte.SIZE;

    private static final int POSITION_DATA_SIZE = 3;
    private static final int TEXTURE_COORD_DATA_SIZE = 2;

    private static final int POSITION_OFFSET = 0 * BYTES_PER_FLOAT;
    private static final int TEXTURE_COORD_OFFSET = POSITION_DATA_SIZE * BYTES_PER_FLOAT;

    private static final int VERTEX_DATA_STRIDE =
            (POSITION_DATA_SIZE + TEXTURE_COORD_DATA_SIZE) * BYTES_PER_FLOAT;

    private static final byte[] INDEX_DATA = {
            0, 1, 2, 1, 2, 3,
    };

    private int[] mVertexGLBuffer = new int[1];
    private int[] mIndexGLBuffer = new int[1];
    private int[] mGLTexture = new int[1];

    private int mProgram;
    private int mMVPMatrixHandler;
    private int mPositionHandle;
    private int mTextureCoordHandler;
    private int mTextureSampleLoc;
    private int mStepHandler;
    private int mSampleInStep;

    private float[] mSteps = {0.01f, 0.01f};

    public PixelatedTexture(Bitmap bitmap) {

        ByteBuffer vbb = ByteBuffer.allocateDirect(BYTES_PER_FLOAT * VERTEX_DATA.length);
        vbb.order(ByteOrder.nativeOrder());
        vbb.asFloatBuffer().put(VERTEX_DATA);
        vbb.position(0);

        GLES20.glGenBuffers(1, mVertexGLBuffer, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexGLBuffer[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vbb.capacity(), vbb, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        ByteBuffer ibb = ByteBuffer.allocateDirect(INDEX_DATA.length);
        ibb.order(ByteOrder.nativeOrder());
        ibb.put(INDEX_DATA);
        ibb.position(0);

        GLES20.glGenBuffers(1, mIndexGLBuffer, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexGLBuffer[0]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibb.capacity(), ibb, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);


        GLES20.glGenTextures(1, mGLTexture, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGLTexture[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);


        mProgram = Utils.createProgram(VERTEX_CODE, FRAGMENT_CODE);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mTextureCoordHandler = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        mTextureSampleLoc = GLES20.glGetUniformLocation(mProgram, "uTextureLoc");
        mStepHandler = GLES20.glGetUniformLocation(mProgram, "uStep");
        mSampleInStep = GLES20.glGetUniformLocation(mProgram, "uSampleInStep");
    }

    private void setVertexData(float widthFactor, float heightFactor) {
        float width = widthFactor * EDGE_LENGTH;
        float height = heightFactor * EDGE_LENGTH;

        /**
         * We only update (x,y) per position, thus the size is 2
         */
        final int sizePerPoint = 2;

        float[] updatedData = {
                -width / 2, -height / 2, // left-bottom
                 width / 2, -height / 2, // right-bottom
                -width / 2,  height / 2, // left-top
                 width / 2,  height / 2, // right-top
        };

        ByteBuffer vbb = ByteBuffer.allocateDirect(BYTES_PER_FLOAT * updatedData.length);
        vbb.order(ByteOrder.nativeOrder());
        vbb.asFloatBuffer().put(updatedData);
        vbb.position(0);

        if (!GLES20.glIsBuffer(mVertexGLBuffer[0])) {
            GLES20.glGenBuffers(1, mVertexGLBuffer, 0);
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexGLBuffer[0]);
        /** Update positions for every point */
        for (int i = 0; i < updatedData.length / sizePerPoint; i++) {
            /** Move to target data position first */
            vbb.position(i * sizePerPoint * BYTES_PER_FLOAT);
            /** shift is i * VERTEX_DATA_STRIDE, read size from updated buffer is 2 float */
            GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, i * VERTEX_DATA_STRIDE, sizePerPoint * BYTES_PER_FLOAT, vbb);
        }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public void setPicture(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float widthFactor, heightFactor;
        if (width > height) {
            widthFactor = width / (float) height;
            heightFactor = 1;
        } else { // height >= width
            widthFactor = 1;
            heightFactor = height / (float) width;
        }
        setVertexData(widthFactor, heightFactor);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGLTexture[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    public void setDensity(int density) {
        float step = 1.0f / density;
        mSteps = new float[]{step, step};
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
        GLES20.glUniform1i(mTextureSampleLoc, 0);
        GLES20.glUniform2fv(mStepHandler, 1, mSteps, 0);
        GLES20.glUniform1i(mSampleInStep, SAMPLE_IN_STEP);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, INDEX_DATA.length, GLES20.GL_UNSIGNED_BYTE, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandler);

        GLES20.glUseProgram(0);
    }
}
