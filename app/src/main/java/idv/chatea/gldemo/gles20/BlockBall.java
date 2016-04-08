package idv.chatea.gldemo.gles20;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class BlockBall {

    private static final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "varying vec3 aPosition;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  aPosition = vec3(vPosition.x, vPosition.y, vPosition.z);" +
            "}";

    private static final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform float uRadius;" +
            "varying vec3 aPosition;" +
            "void main() {" +
            "  vec3 color;" +
            "  float n = 8.0;" +
            "  float span = 2.0 * uRadius / n;" +
            "  int i = int((aPosition.x + uRadius) / span);" + // add Radius to avoid negative value.
            "  int j = int((aPosition.y + uRadius) / span);" + // add Radius to avoid negative value.
            "  int k = int((aPosition.z + uRadius) / span);" + // add Radius to avoid negative value.
            "  int whichColor = int(mod(float(i + j + k), 2.0));" +
            "  if (whichColor == 1) {" +
            "    color = vec3(0.6788, 0.231, 0.129);" + // Red
            "  } else {" +
            "    color = vec3(1.0, 1.0, 1.0);" + // White
            "  }" +
            "  gl_FragColor = vec4(color, 1.0);" +
            "}";

    private int mProgram;

    private FloatBuffer mVertexBuffer;
    private int mVertexCount;

    private static final float DEFAULT_BALL_RADIUS = 1.5f;

    private float mBallRadius;

    public BlockBall() {
        this(DEFAULT_BALL_RADIUS);
    }

    public BlockBall(float radius) {
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
        mVertexBuffer = buffer.asFloatBuffer();
        mVertexBuffer.put(allVertex);
        mVertexBuffer.position(0);
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
    }

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram);

        int mMVPMatrixHandler = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mvpMatrix, 0);

        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

        int mRadiusHandle = GLES20.glGetUniformLocation(mProgram, "uRadius");
        GLES20.glUniform1f(mRadiusHandle, mBallRadius);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glUseProgram(0);
    }
}
