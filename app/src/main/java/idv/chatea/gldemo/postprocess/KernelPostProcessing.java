package idv.chatea.gldemo.postprocess;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import idv.chatea.gldemo.gles20.Utils;

/**
 * This is a texture used to do post processing by kernel (used for convolution).
 */
public class KernelPostProcessing {
    /**
     * Note: The post process image will not be inverse, texture coordinate is normal.
     */
    private static final float[] VERTEX_DATA = {
            -0.5f, -0.5f, 0, 0, 0, // left-bottom corner
             0.5f, -0.5f, 0, 1, 0, // right-bottom corner
            -0.5f,  0.5f, 0, 0, 1, // left-top corner
             0.5f,  0.5f, 0, 1, 1, // right-top corner
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

    private float mFrameWidth;
    private float mFrameHeight;
    /**
     * The default sampling step is 1(pixel).
     */
    private float mSamplingStep = 1f;

    private int[] mVertexGLBuffer = new int[1];
    private int[] mIndexGLBuffer = new int[1];

    private int[] mColorTextureId = new int[1];
    private int[] mDepthTextureId = new int[1];

    private int mProgram;
    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mTextureCoordHandle;
    private int mColorTextureHandle;
    private int mDepthTextureHandle;
    private int mKernelLengthHandle;
    private int mKernelHandle;
    private int mSampleUnitHandle;

    public KernelPostProcessing(Context context, FrameBuffer framebuffer) {
        mFrameWidth = framebuffer.getWidth();
        mFrameHeight = framebuffer.getHeight();

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

        mColorTextureId[0] = framebuffer.getColorTexture();
        mDepthTextureId[0] = framebuffer.getDepthTexture();

        String vertextShaderCode = Utils.loadFromAssetsFile(context, "shaders/postprocessing/kernel_postprocessing_vertex.glsl");
        String fragmentShaderCode = Utils.loadFromAssetsFile(context, "shaders/postprocessing/kernel_postprocessing_fragment.glsl");
        mProgram = Utils.createProgram(vertextShaderCode, fragmentShaderCode);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        mColorTextureHandle = GLES20.glGetUniformLocation(mProgram, "uColorTexture");
        mDepthTextureHandle = GLES20.glGetUniformLocation(mProgram, "uDepthTexture");
        mKernelLengthHandle = GLES20.glGetUniformLocation(mProgram, "uKernelLength");
        mKernelHandle = GLES20.glGetUniformLocation(mProgram, "uKernel");
        mSampleUnitHandle = GLES20.glGetUniformLocation(mProgram, "uSampleUnit");
    }

    /**
     * Set the sampling step for kernel.
     * @param step the sampling step, unit is pixel.
     */
    public void setSamplingStep(float step) {
        mSamplingStep = step;
    }

    public float getSamplingStep() {
        return mSamplingStep;
    }

    public void draw(float[] mvpMatrix, Kernel kernel) {
        GLES20.glUseProgram(mProgram);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexGLBuffer[0]);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexGLBuffer[0]);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_STRIDE, POSITION_OFFSET);
        GLES20.glVertexAttribPointer(mTextureCoordHandle, TEXTURE_COORD_DATA_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_STRIDE, TEXTURE_COORD_OFFSET);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mColorTextureId[0]);
        GLES20.glUniform1i(mColorTextureHandle, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDepthTextureId[0]);
        GLES20.glUniform1i(mDepthTextureHandle, 1);
        GLES20.glUniform1i(mKernelLengthHandle, kernel.getKernelLength());
        float[] kernelMatrix = kernel.getFlattenKernelMatrix();
        GLES20.glUniform1fv(mKernelHandle, kernelMatrix.length, kernelMatrix, 0);
        GLES20.glUniform2f(mSampleUnitHandle, 1.0f / mFrameWidth, 1.0f / mFrameHeight);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, INDEX_DATA.length, GLES20.GL_UNSIGNED_BYTE, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);

        GLES20.glUseProgram(0);
    }
}
