package idv.chatea.gldemo.lighting;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import idv.chatea.gldemo.GLES2_Use_GLBuffer_Activity;
import idv.chatea.gldemo.gles20.Utils;

/**
 * Please check the document of {@link GLES2_Use_GLBuffer_Activity}.
 */
public class LightCube {
    private static final String VERTEX_CODE =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec3 aPosition;" +
            "attribute vec3 aNormal;" +
            "uniform vec4 uLightPosition;" +
            "uniform vec4 uAmbient;" +
            "uniform vec4 uDiffusion;" +
            "uniform vec4 uSpecular;" +
            "varying vec4 vLightColor;" +
            "void calDiffusion(inout vec4 diffusion) {" +
            "  vec3 normalizedNormal = normalize(aNormal);" +
            "  vec3 normalizedLightDirection = normalize(uLightPosition.xyz - aPosition);" +
            "  float factor = max(0.0, dot(normalizedNormal, normalizedLightDirection));" +
            "  diffusion = factor * uDiffusion;" +
            "}" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vec4(aPosition, 1.0);" +
            "  vec4 diffusion;" +
            "  calDiffusion(diffusion);" +
            "  vLightColor = uAmbient + diffusion;" +
            "}";

    private static final String FRAGMENT_CODE =
            "precision mediump float;" +
            "uniform vec4 uColor;" +
            "varying vec4 vLightColor;" +
            "void main() {" +
            "  gl_FragColor = uColor * vLightColor;" +
            "}";

    /**
     * Normal data is same as position data.
     */
    private static final float[] VERTEX_DATA = {
            -1, -1,  1, // front left-bottom corner
             1, -1,  1, // front right-bottom corner
            -1,  1,  1, // front left-top corner
             1,  1,  1, // front right-top corner
            -1, -1, -1, // back left-bottom corner
             1, -1, -1, // back right-bottom corner
            -1,  1, -1, // back left-top corner
             1,  1, -1, // back right-top corner
    };

    private static final int BYTES_PER_FLOAT = Float.SIZE / Byte.SIZE;

    private static final int POSITION_DATA_SIZE = 3;

    private static final float[] COLOR = {0.5f, 0.5f, 0.5f, 0.5f};

    private final byte[] INDEX_DATA = {
            0, 1, 2, 2, 1, 3, // front
            2, 3, 7, 2, 7, 6, // top
            4, 0, 6, 6, 0, 2, // left
            1, 5, 3, 3, 5, 7, // right
            4, 5, 0, 0, 5, 1, // bottom
            5, 4, 7, 7, 4, 6, // back
    };

    private int[] mVertexGLBuffer = new int[1];
    private int[] mIndexGLBuffer = new int[1];

    private int mProgram;
    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mNormalHandle;
    private int mLightPositionHandle;
    private int mAmbientHandle;
    private int mDiffusionHandle;
    private int mSpecularHandle;
    private int mColorHandler;

    public LightCube(Context context) {
        ByteBuffer vbb = ByteBuffer.allocateDirect(VERTEX_DATA.length * BYTES_PER_FLOAT);
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

        String vertexCode = Utils.loadFromAssetsFile(context, "shaders/light_vertex.glsl");
        String fragmentCode = Utils.loadFromAssetsFile(context, "shaders/light_fragment.glsl");
//        mProgram = Utils.createProgram(VERTEX_CODE, FRAGMENT_CODE);
        mProgram = Utils.createProgram(vertexCode, fragmentCode);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
        mLightPositionHandle = GLES20.glGetUniformLocation(mProgram, "uLightPosition");
        mAmbientHandle = GLES20.glGetUniformLocation(mProgram, "uAmbient");
        mDiffusionHandle = GLES20.glGetUniformLocation(mProgram, "uDiffusion");
        mSpecularHandle = GLES20.glGetUniformLocation(mProgram, "uSpecular");
        mColorHandler = GLES20.glGetUniformLocation(mProgram, "uColor");
    }

    public void draw(float[] mvpMatrix, Light light) {
        GLES20.glUseProgram(mProgram);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexGLBuffer[0]);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexGLBuffer[0]);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glVertexAttribPointer(mNormalHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glUniform4fv(mLightPositionHandle, 1, light.position, 0);
        GLES20.glUniform4fv(mAmbientHandle, 1, light.ambientChannel, 0);
        GLES20.glUniform4fv(mDiffusionHandle, 1, light.diffusionChannel, 0);
        GLES20.glUniform4fv(mSpecularHandle, 1, light.specularChannel, 0);
        GLES20.glUniform4fv(mColorHandler, 1, COLOR, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, INDEX_DATA.length, GLES20.GL_UNSIGNED_BYTE, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);

        GLES20.glUseProgram(0);
    }
}
