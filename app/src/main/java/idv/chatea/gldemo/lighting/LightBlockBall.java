package idv.chatea.gldemo.lighting;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import idv.chatea.gldemo.gles20.Utils;

public class LightBlockBall {

    private static final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec3 aPosition;" +
            "attribute vec3 aNormal;" +
            "varying vec3 vPosition;" +
            "uniform vec4 uLightPosition;" +
            "uniform vec4 uAmbient;" +
            "uniform vec4 uDiffusion;" +
            "uniform vec4 uSpecular;" +
            "uniform vec3 uEyePosition;" +
            "varying vec4 vLightColor;" +
            "vec4 calDiffusion() {" +
            "  vec3 normalizedNormal = normalize(aNormal);" +
            "  vec3 normalizedLightDirection = normalize(uLightPosition.xyz - aPosition);" +
            "  float factor = max(0.0, dot(normalizedNormal, normalizedLightDirection));" +
            "  return factor * uDiffusion;" +
            "}" +
            "vec4 calSpecular() {" +
            "  vec3 normalizedNormal = normalize(aNormal);" +
            "  vec3 normalizeLightDirection = normalize(uLightPosition.xyz - aPosition);" +
            "  vec3 normalizedEyeDirection = normalize(uEyePosition - aPosition);" +
            "  vec3 halfVector = normalize(normalizeLightDirection + normalizedEyeDirection);" +
            "  float factor = max(0.0, dot(normalizedNormal, halfVector));" +
            "  return factor * uSpecular;" +
            "}" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vec4(aPosition, 1.0);" +
            "  vPosition = aPosition.xyz;" +
            "  vec4 diffusion = calDiffusion();" +
            "  vec4 specular = calSpecular();" +
            "  vLightColor = uAmbient + diffusion + specular;" +
            "}";

    private static final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform float uRadius;" +
            "varying vec3 vPosition;" +
            "varying vec4 vLightColor;" +
            "void main() {" +
            "  vec3 color;" +
            "  float n = 8.0;" +
            "  float span = 2.0 * uRadius / n;" +
            "  int i = int((vPosition.x + uRadius) / span);" + // add Radius to avoid negative value.
            "  int j = int((vPosition.y + uRadius) / span);" + // add Radius to avoid negative value.
            "  int k = int((vPosition.z + uRadius) / span);" + // add Radius to avoid negative value.
            "  int whichColor = int(mod(float(i + j + k), 2.0));" +
            "  if (whichColor == 1) {" +
            "    color = vec3(0.6788, 0.231, 0.129);" + // Red
            "  } else {" +
            "    color = vec3(1.0, 1.0, 1.0);" + // White
            "  }" +
            "  gl_FragColor = vec4(color, 1.0) * vLightColor;" +
            "}";

    private int[] mVertexGLBuffer = new int[1];

    private int mVertexCount;

    private static final float DEFAULT_BALL_RADIUS = 1.5f;

    private float mBallRadius;

    private int mProgram;
    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mNormalHandle;
    private int mLightPositionHandle;
    private int mAmbientHandle;
    private int mDiffusionHandle;
    private int mSpecularHandle;
    private int mEyePositionHandle;
    private int mRadiusHandle;

    public LightBlockBall() {
        this(DEFAULT_BALL_RADIUS);
    }

    public LightBlockBall(float radius) {
        mBallRadius = radius;
        initVertexData();
        initShader();
    }

    private void initVertexData() {
        ArrayList<Float> vertices = new ArrayList<>();

        /**
         * Ball coordinate system:
         * x = R * sin(a) * cos(b)
         * y = R * sin(a) * sin(b)
         * z = R * cos(a)
         * 0 <= a < 180, 0 <= b <= 360
         */
        final float r = mBallRadius;
        final float angleShift = 10;

        for (int alpha = 0; alpha < 180; alpha += angleShift) {
            for (int beta = 0; beta < 360; beta += angleShift) {
                // top-left
                float[] tl = getXYZ(r, alpha, beta);

                // bottom-left point
                float[] bl = getXYZ(r, alpha + angleShift, beta);

                // top-right point
                float[] tr = getXYZ(r, alpha, beta + angleShift);

                // bottom-right point
                float[] br = getXYZ(r, alpha + angleShift, beta + angleShift);

                addArrayIntoArrayList(vertices, tl);
                addArrayIntoArrayList(vertices, bl);
                addArrayIntoArrayList(vertices, tr);
                addArrayIntoArrayList(vertices, tr);
                addArrayIntoArrayList(vertices, bl);
                addArrayIntoArrayList(vertices, br);
            }
        }

        float[] allVertex = new float[vertices.size()];
        for (int i = 0; i < allVertex.length; i++) {
            allVertex[i] = vertices.get(i);
        }

        mVertexCount = allVertex.length / 3;

        // calculate vertex point.
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * vertices.size());
        buffer.order(ByteOrder.nativeOrder());
        buffer.asFloatBuffer().put(allVertex);
        buffer.position(0);

        GLES20.glGenBuffers(1, mVertexGLBuffer, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexGLBuffer[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, buffer.capacity(), buffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    private float[] getXYZ(float r, float alpha, float beta) {
        float[] result = new float[3];

        float radiansPerAngle = (float) (2 * Math.PI / 360.0);

        float radiansAlpha = alpha * radiansPerAngle;
        float radiansBeta = beta * radiansPerAngle;

        result[0] = (float) (r * Math.sin(radiansAlpha) * Math.cos(radiansBeta));
        result[1] = (float) (r * Math.sin(radiansAlpha) * Math.sin(radiansBeta));
        result[2] = (float) (r * Math.cos(radiansAlpha));

        return result;
    }

    private void addArrayIntoArrayList(ArrayList<Float> arrayList, float[] array) {
        for (float element: array) {
            arrayList.add(element);
        }
    }

    private void initShader() {
        mProgram = Utils.createProgram(vertexShaderCode, fragmentShaderCode);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");

        mLightPositionHandle = GLES20.glGetUniformLocation(mProgram, "uLightPosition");
        mAmbientHandle = GLES20.glGetUniformLocation(mProgram, "uAmbient");
        mDiffusionHandle = GLES20.glGetUniformLocation(mProgram, "uDiffusion");
        mSpecularHandle = GLES20.glGetUniformLocation(mProgram, "uSpecular");
        mEyePositionHandle = GLES20.glGetUniformLocation(mProgram, "uEyePosition");

        mRadiusHandle = GLES20.glGetUniformLocation(mProgram, "uRadius");
    }

    public void draw(float[] mvpMatrix, Light light, float[] eyePosition) {
        boolean cullFace = GLES20.glIsEnabled(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        GLES20.glUseProgram(mProgram);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexGLBuffer[0]);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
//        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
//        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glUniform4fv(mLightPositionHandle, 1, light.position, 0);
        GLES20.glUniform4fv(mAmbientHandle, 1, light.ambientChannel, 0);
        GLES20.glUniform4fv(mDiffusionHandle, 1, light.diffusionChannel, 0);
        GLES20.glUniform4fv(mSpecularHandle, 1, light.specularChannel, 0);
        GLES20.glUniform3fv(mEyePositionHandle, 1, eyePosition, 0);
        GLES20.glUniform1f(mRadiusHandle, mBallRadius);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
        GLES20.glUseProgram(0);

        if (cullFace) {
            GLES20.glEnable(GLES20.GL_CULL_FACE);
        }
    }
}
