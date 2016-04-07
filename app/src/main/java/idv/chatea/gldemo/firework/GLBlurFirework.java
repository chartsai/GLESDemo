package idv.chatea.gldemo.firework;

import android.opengl.GLES20;
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
 * Created by chatea on 2016/2/1.
 * @author Charlie Tsai (chatea)
 */
public class GLBlurFirework extends GLFirework {
    /**
     * How long of this effect.
     * Unit is Frame.
     * When the frame count is large thant mDuration, this instance is died.
     */
    private final long mDuration;
    private long mFrameCount;

    private float[] mModuleMatrix = new float[16];

    private float mAlpha;
    private float[] mColor;
    /**
     * A start offset to defer the effect.
     */
    private long mDelay;

    private int mLinesNumber;
    private int mParticleNumber;

    private IntBuffer mIndexBuffer;
    private FloatBuffer mSpeedBuffer;
    private FloatBuffer mThetasBuffer;
    private FloatBuffer mPhisBuffer;
    private FloatBuffer mRandomOffsetsBuffer;
    private FloatBuffer mFadeOutBuffer;

    private float mMass;
    private float mThicknesses;

    public GLBlurFirework(long duration, float x, float y, float z,
                          float mass, float radius, int density, float thickness) {
        mDuration = duration;
        mFrameCount = 0;
        setPosition(x, y, z);
        setupParticles(radius, density);
        setAlpha(1.0f);
        setColor(0.0f, 1.0f, 1.0f, 1.0f);
        setMass(mass);
        setThickness(thickness);
        setDelay(0);
    }

    private void setupParticles(float radius, int density) {
        final int curveAccuracy = 10;
        final float blurStep = 0.97f;
        Random r = new Random();

        mLinesNumber = density * (density / 2) * (curveAccuracy - 1);
        mParticleNumber = density * (density / 2) * curveAccuracy;

        int[] indices = new int[mLinesNumber * 2];
        float[] speeds = new float[mParticleNumber];
        float[] thetas = new float[mParticleNumber];
        float[] phis = new float[mParticleNumber];
        float[] randomOffsets = new float[mParticleNumber * 3];
        float[] fadeOuts = new float[mParticleNumber];

        final float angleStep = 360.0f / density;

        int indexCounter = 0;
        int particleCounter = 0;
        for (int j = 0; j < density / 2; j++) {
            float theta = j * angleStep % 360;
            for (int i = 0; i < density; i++) {
                float phi = i * angleStep % 360;

                float[] offset = {r.nextFloat() - 0.5f, r.nextFloat() - 0.5f, r.nextFloat() - 0.5f};
                float gaussianRadius = radius + radius * 0.1f * (float) r.nextGaussian();

                float factor = 1.0f;
                for (int k = 0; k < curveAccuracy; k++) {

                    if (k != curveAccuracy - 1) {
                        indices[indexCounter++] = particleCounter;
                        indices[indexCounter++] = particleCounter + 1;
                    }
                    speeds[particleCounter] = gaussianRadius;
                    thetas[particleCounter] = theta;
                    phis[particleCounter] = phi;
                    randomOffsets[particleCounter * 3] = offset[0];
                    randomOffsets[particleCounter * 3 + 1] = offset[1];
                    randomOffsets[particleCounter * 3 + 2] = offset[2];
                    fadeOuts[particleCounter] = factor;

                    particleCounter++;

                    factor *= blurStep;
                }
            }
        }

        ByteBuffer bb;
        bb = ByteBuffer.allocateDirect(indices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mIndexBuffer = bb.asIntBuffer();
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);

        bb = ByteBuffer.allocateDirect(speeds.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mSpeedBuffer = bb.asFloatBuffer();
        mSpeedBuffer.put(speeds);
        mSpeedBuffer.position(0);

        bb = ByteBuffer.allocateDirect(thetas.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mThetasBuffer = bb.asFloatBuffer();
        mThetasBuffer.put(thetas);
        mThetasBuffer.position(0);

        bb = ByteBuffer.allocateDirect(phis.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mPhisBuffer = bb.asFloatBuffer();
        mPhisBuffer.put(phis);
        mPhisBuffer.position(0);

        bb = ByteBuffer.allocateDirect(randomOffsets.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mRandomOffsetsBuffer = bb.asFloatBuffer();
        mRandomOffsetsBuffer.put(randomOffsets);
        mRandomOffsetsBuffer.position(0);

        bb = ByteBuffer.allocateDirect(fadeOuts.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mFadeOutBuffer = bb.asFloatBuffer();
        mFadeOutBuffer.put(fadeOuts);
        mFadeOutBuffer.position(0);
    }

    public void setAlpha(float alpha) {
        this.mAlpha = alpha;
    }

    public void setPosition(float x, float y, float z) {
        float[] newModuleMatrix = new float[16];
        Matrix.setIdentityM(newModuleMatrix, 0);
        Matrix.translateM(newModuleMatrix, 0, x, y, z);
        mModuleMatrix = newModuleMatrix;
    }

    public void setColor(float r, float g, float b, float a) {
        this.mColor = new float[] {r, g, b, a};
    }

    public void setMass(float mass) {
        this.mMass = mass;
    }

    public void setThickness(float thickness) {
        this.mThicknesses = thickness;
    }

    public void setDelay(long delay) {
        mDelay = delay;
    }

    @Override
    public void render(float[] vpMatrix) {
        if (mDelay > 0) {
            // skip this frame due to defer. mDelay count down.
            mDelay--;
            return;
        }

        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, mModuleMatrix, 0);

        super.render(mvpMatrix);
        mFrameCount++;
    }

    @Override
    public boolean isDead() {
        return mFrameCount >= mDuration;
    }

    public static class Shader implements GLFirework.Shader<GLBlurFirework> {

        private static final String VERTEX_SHADER_CODE =
                "precision lowp float;" +
                "uniform mediump float uProgress;" +
                "uniform mat4 uMVPMatrix;" +
                "attribute float aSpeed;" +
                "attribute float aTheta;" +
                "attribute float aPhi;" +
                "attribute vec3 aOffset;" +
                "attribute float aFadeOut;" +
                "uniform float uMass;" +
                "varying float vFadeOut;" +
                "void main() {" +
                "  float radiansTheta = radians(aTheta);" +
                "  float radiansPhi = radians(aPhi);" +
                "  float xPos = sin(radiansTheta) * cos(radiansPhi);" +
                "  float yPos = sin(radiansTheta) * sin(radiansPhi);" +
                "  float zPos = cos(radiansTheta);" +
                "  yPos = yPos - uMass * 0.5 * 9.8 * uProgress * uProgress;" +
                "  vec3 position = aSpeed * vec3(xPos, yPos, zPos) + aOffset / 2.0f;" +
                "  position = position * aFadeOut;" +
                "  vec4 pos = vec4(sqrt(sqrt(uProgress)) * position, 1.0f);" +
                "  gl_Position = uMVPMatrix * pos;" +
                "  vFadeOut = aFadeOut;" +
                "}";

        private static final String FRAGMENT_SHADER_CODE =
                "precision lowp float;" +
                "uniform mediump float uProgress;" +
                "uniform vec4 uColor;" +
                "uniform float uAlpha;" +
                "varying float vFadeOut;" +
                "void main() {" +
                "  vec4 finalColor = uColor * uAlpha * (1.0 - uProgress * uProgress);" +
                "  gl_FragColor = finalColor * vFadeOut * vFadeOut;" +
                "}";

        private int mGLProgram;

        public Shader() {
            setupShaderProgram();
        }

        private void setupShaderProgram() {
            mGLProgram = MyGLUtils.createProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);
        }

        public void render(GLBlurFirework firework, float[] mvpMatrix) {

            float progress = (float) (firework.mFrameCount - firework.mDelay) / firework.mDuration;

            GLES20.glUseProgram(mGLProgram);

            int progressHandle = GLES20.glGetUniformLocation(mGLProgram, "uProgress");
            GLES20.glUniform1f(progressHandle, progress);

            int mvpHandle = GLES20.glGetUniformLocation(mGLProgram, "uMVPMatrix");
            GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0);

            int speedHandle = GLES20.glGetAttribLocation(mGLProgram, "aSpeed");
            GLES20.glEnableVertexAttribArray(speedHandle);
            GLES20.glVertexAttribPointer(speedHandle, 1, GLES20.GL_FLOAT, false, 0, firework.mSpeedBuffer);

            int thetaHandle = GLES20.glGetAttribLocation(mGLProgram, "aTheta");
            GLES20.glEnableVertexAttribArray(thetaHandle);
            GLES20.glVertexAttribPointer(thetaHandle, 1, GLES20.GL_FLOAT, false, 0, firework.mThetasBuffer);

            int phiHandle = GLES20.glGetAttribLocation(mGLProgram, "aPhi");
            GLES20.glEnableVertexAttribArray(phiHandle);
            GLES20.glVertexAttribPointer(phiHandle, 1, GLES20.GL_FLOAT, false, 0, firework.mPhisBuffer);

            int offsetHandle = GLES20.glGetAttribLocation(mGLProgram, "aOffset");
            GLES20.glEnableVertexAttribArray(offsetHandle);
            GLES20.glVertexAttribPointer(offsetHandle, 3, GLES20.GL_FLOAT, false, 0, firework.mRandomOffsetsBuffer);

            int fadeOutHandler = GLES20.glGetAttribLocation(mGLProgram, "aFadeOut");
            GLES20.glEnableVertexAttribArray(fadeOutHandler);
            GLES20.glVertexAttribPointer(fadeOutHandler, 1, GLES20.GL_FLOAT, false, 0, firework.mFadeOutBuffer);

            int massHandler = GLES20.glGetUniformLocation(mGLProgram, "uMass");
            GLES20.glUniform1f(massHandler, firework.mMass);

            int mColorHandle = GLES20.glGetUniformLocation(mGLProgram, "uColor");
            GLES20.glUniform4fv(mColorHandle, 1, firework.mColor, 0);

            int mAlphaHandle = GLES20.glGetUniformLocation(mGLProgram, "uAlpha");
            GLES20.glUniform1f(mAlphaHandle, firework.mAlpha);

            GLES20.glLineWidth(firework.mThicknesses);

            GLES20.glDrawElements(GLES20.GL_LINES, firework.mLinesNumber, GLES20.GL_UNSIGNED_INT, firework.mIndexBuffer);

            GLES20.glDisableVertexAttribArray(speedHandle);
            GLES20.glDisableVertexAttribArray(thetaHandle);
            GLES20.glDisableVertexAttribArray(phiHandle);
            GLES20.glDisableVertexAttribArray(offsetHandle);
            GLES20.glEnableVertexAttribArray(fadeOutHandler);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glDisable(GLES20.GL_TEXTURE_2D);
            GLES20.glUseProgram(0);
        }
    }
}
