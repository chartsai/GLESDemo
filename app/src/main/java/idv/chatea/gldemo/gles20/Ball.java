package idv.chatea.gldemo.gles20;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class Ball {

    private static final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private static final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 uColor;" +
            "void main() {" +
            "  gl_FragColor = uColor;" +
            "}";

    private int mProgram;

    private FloatBuffer mVertexBuffer;
    private int mVertexCount;

    private static final float DEFAULT_BALL_RADIUS = 1.5f;

    private float mBallRadius;

    private float[] mColor = {0.2f, 0.8f, 0.3f, 1.0f};

    public Ball() {
        this(DEFAULT_BALL_RADIUS);
    }

    public Ball(float radius) {
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
                float[] bl = getXYZ(r, alpha - angleShift, beta);

                // top-right point
                float[] tr = getXYZ(r, alpha, beta + angleShift);

                // bottom-right point
                float[] br = getXYZ(r, alpha - angleShift, beta + angleShift);

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

        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "uColor");
        GLES20.glUniform4fv(mColorHandle, 1, mColor, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
        GLES20.glUseProgram(0);
    }
}
