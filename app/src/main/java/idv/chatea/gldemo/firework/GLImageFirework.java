package idv.chatea.gldemo.firework;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

/**
 * The GLFirework is the class which contain the Firework data only.
 * The GLFirework.Shader does rendering only. <br>
 * GLFirework.Shader doesn't have state, thus you only need to create one GLFirework.Shader,
 * and use it to render all of the GLFirework. <br>
 * Created by chatea on 2016/2/3.
 * @author Charlie Tsai (chatea)
 */
public class GLImageFirework extends GLFirework {
    /**
     * How long of this effect.
     * Unit is Frame.
     * When the frame count is large thant mDuration, this instance is died.
     */
    private final long mDuration;
    private long mFrameCount;

    private float[] mTranslationMatrix = new float[16];
    private float[] mRotatoinMatrix = new float[16];

    private float mAlpha;
    private float[] mSize;
    private float[] mColor;

    private int mParticleNumber;

    private IntBuffer mIndexBuffer;
    private FloatBuffer mUVBuffer;
    private FloatBuffer mRandomOffsetsBuffer;
    private FloatBuffer mThicknessesBuffer;

    private float mMass;

    public GLImageFirework(long duration, float x, float y, float z,
                           float mass, float width, float height, int density, float thickness) {
        mDuration = duration;
        mFrameCount = 0;
        setSize(width, height);
        setPosition(x, y, z);
        setupParticles(density, thickness);
        setAlpha(1.0f);
        setColor(0.0f, 1.0f, 1.0f, 1.0f);
        setMass(mass);
    }

    private void setupParticles(int density, float baseThickness) {
        Random r = new Random();

        mParticleNumber = density * density;

        int[] indices = new int[mParticleNumber];
        float[] uvs = new float[mParticleNumber * 2];
        float[] randomOffsets = new float[mParticleNumber * 3];
        float[] thicknesses = new float[mParticleNumber];

        int indexCounter = 0;
        int particleCounter = 0;
        for (int i = 0; i < density; i++) {
            float xStep = 1.0f / density;
            for (int j = 0; j < density; j++) {
                float yStep = 1.0f / density;

                float x = i * xStep;
                float y = j * yStep;

                indices[indexCounter++] = particleCounter;
                uvs[particleCounter * 2 + 0] = x;
                uvs[particleCounter * 2 + 1] = y;
                randomOffsets[particleCounter * 3] = (float) r.nextGaussian() / 4;
                randomOffsets[particleCounter * 3 + 1] = (float) r.nextGaussian() / 4;
                randomOffsets[particleCounter * 3 + 2] = 2 * (float) r.nextGaussian();
                thicknesses[particleCounter] = baseThickness;

                particleCounter++;
            }
        }

        ByteBuffer bb;
        bb = ByteBuffer.allocateDirect(indices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mIndexBuffer = bb.asIntBuffer();
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);

        bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mUVBuffer = bb.asFloatBuffer();
        mUVBuffer.put(uvs);
        mUVBuffer.position(0);

        bb = ByteBuffer.allocateDirect(randomOffsets.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mRandomOffsetsBuffer = bb.asFloatBuffer();
        mRandomOffsetsBuffer.put(randomOffsets);
        mRandomOffsetsBuffer.position(0);

        bb = ByteBuffer.allocateDirect(thicknesses.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mThicknessesBuffer = bb.asFloatBuffer();
        mThicknessesBuffer.put(thicknesses);
        mThicknessesBuffer.position(0);
    }

    public void setSize(float width, float height) {
        this.mSize = new float[] {width, height};
    }

    public void setAlpha(float alpha) {
        this.mAlpha = alpha;
    }

    public void setPosition(float x, float y, float z) {
        float[] newModuleMatrix = new float[16];
        Matrix.setIdentityM(newModuleMatrix, 0);
        Matrix.translateM(newModuleMatrix, 0, x, y, z);
        mTranslationMatrix = newModuleMatrix;
    }

    /**
     * Used to set the rotation of image.
     * @param xAngle the rotation angle around x-axis.
     * @param yAngle the rotation angle around y-axis.
     * @param zAngle the rotation angle around z-axis.
     */
    public void setRotation(float xAngle, float yAngle, float zAngle) {
        Matrix.setRotateEulerM(mRotatoinMatrix, 0, xAngle, yAngle, zAngle);
    }

    public void setColor(float r, float g, float b, float a) {
        this.mColor = new float[] {r, g, b, a};
    }

    public void setMass(float mass) {
        this.mMass = mass;
    }

    @Override
    public void render(float[] vpMatrix) {
        float[] mvpMatrix = new float[16];
        float[] moduleMatrix = new float[16];
        Matrix.multiplyMM(moduleMatrix, 0, mTranslationMatrix, 0, mRotatoinMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, moduleMatrix, 0);

        super.render(mvpMatrix);
        mFrameCount++;
    }

    @Override
    public boolean isDead() {
        return mFrameCount >= mDuration;
    }

    public static class Shader implements GLFirework.Shader<GLImageFirework> {

        private static final String VERTEX_SHADER_CODE =
                "precision lowp float;" +
                "uniform mediump float uProgress;" +
                "uniform mat4 uMVPMatrix;" +
                "uniform sampler2D uTexture;" +
                "uniform vec2 uSize;" +
                "attribute vec2 aUV;" +
                "attribute vec3 aOffset;" +
                "uniform float uMass;" +
                "attribute float aThickness;" +
                "varying vec4 aTextureColor;" +
                "void main() {" +
                "  gl_PointSize = aThickness;" +
                "  aTextureColor = texture2D(uTexture, vec2(aUV.x, 1.0f - aUV.y));" +
                "  vec2 center = vec2(aUV.x - 0.5f, aUV.y - 0.5f);" +
                "  vec3 position = vec3(uSize * center, 0.0f) + aOffset;" +
                "  position.y = position.y - uMass * 0.5f * 9.8f * uProgress * uProgress;" +
                "  vec4 pos = vec4(sqrt(sqrt(uProgress)) * position, 1.0f);" +
                "  gl_Position = uMVPMatrix * pos;" +
                "}";

        private static final String FRAGMENT_SHADER_CODE =
                "precision lowp float;" +
                "uniform mediump float uProgress;" +
                "uniform vec4 uColor;" +
                "uniform float uAlpha;" +
                "varying vec4 aTextureColor;" +
                "void main() {" +
                "  gl_FragColor = aTextureColor * uColor * uAlpha * (1.0f - uProgress * uProgress);" +
                "}";

        private int[] mTextureId;
        private int mGLProgram;

        public Shader(Bitmap bitmap) {
            setupTexture(bitmap);
            setupShaderProgram();
        }

        private void setupTexture(Bitmap bitmap) {
            mTextureId = new int[1];
            GLES20.glGenTextures(mTextureId.length, mTextureId, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        }

        private void setupShaderProgram() {
            mGLProgram = MyGLUtils.createProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);
        }

        public void render(GLImageFirework firework, float[] mvpMatrix) {

            float progress = (float) firework.mFrameCount / firework.mDuration;

            GLES20.glUseProgram(mGLProgram);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);
            GLES20.glEnable(GLES20.GL_TEXTURE_2D);

            int progressHandle = GLES20.glGetUniformLocation(mGLProgram, "uProgress");
            GLES20.glUniform1f(progressHandle, progress);

            int mvpHandle = GLES20.glGetUniformLocation(mGLProgram, "uMVPMatrix");
            GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0);

            int pointTexture = GLES20.glGetUniformLocation(mGLProgram, "uTexture");
            GLES20.glUniform1i(pointTexture, 0);

            int sizeHandle = GLES20.glGetUniformLocation(mGLProgram, "uSize");
            GLES20.glUniform2fv(sizeHandle, 1, firework.mSize, 0);

            int uvHandle = GLES20.glGetAttribLocation(mGLProgram, "aUV");
            GLES20.glEnableVertexAttribArray(uvHandle);
            GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, 0, firework.mUVBuffer);

            int offsetHandle = GLES20.glGetAttribLocation(mGLProgram, "aOffset");
            GLES20.glEnableVertexAttribArray(offsetHandle);
            GLES20.glVertexAttribPointer(offsetHandle, 3, GLES20.GL_FLOAT, false, 0, firework.mRandomOffsetsBuffer);

            int massHandler = GLES20.glGetUniformLocation(mGLProgram, "uMass");
            GLES20.glUniform1f(massHandler, firework.mMass);

            int thicknessHandle = GLES20.glGetAttribLocation(mGLProgram, "aThickness");
            GLES20.glEnableVertexAttribArray(thicknessHandle);
            GLES20.glVertexAttribPointer(thicknessHandle, 1, GLES20.GL_FLOAT, false, 0, firework.mThicknessesBuffer);

            int mColorHandle = GLES20.glGetUniformLocation(mGLProgram, "uColor");
            GLES20.glUniform4fv(mColorHandle, 1, firework.mColor, 0);

            int mAlphaHandle = GLES20.glGetUniformLocation(mGLProgram, "uAlpha");
            GLES20.glUniform1f(mAlphaHandle, firework.mAlpha);

            GLES20.glDrawElements(GLES20.GL_POINTS, firework.mParticleNumber, GLES20.GL_UNSIGNED_INT, firework.mIndexBuffer);

            GLES20.glDisableVertexAttribArray(uvHandle);
            GLES20.glDisableVertexAttribArray(offsetHandle);
            GLES20.glDisableVertexAttribArray(thicknessHandle);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glDisable(GLES20.GL_TEXTURE_2D);
            GLES20.glUseProgram(0);
        }
    }
}
