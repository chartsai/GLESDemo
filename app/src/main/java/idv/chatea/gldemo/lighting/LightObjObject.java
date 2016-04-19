package idv.chatea.gldemo.lighting;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import idv.chatea.gldemo.gles20.Utils;
import idv.chatea.gldemo.objloader.BasicObjLoader;

public class LightObjObject {
    private static final int BYTES_PER_FLOAT = Float.SIZE / Byte.SIZE;

    private static final int POSITION_DATA_SIZE = BasicObjLoader.ObjData.POSITION_SIZE;
    private static final int TEXTURE_COORD_DATA_SIZE = BasicObjLoader.ObjData.TEXTURE_COORD_SIZE;
    private static final int NORMAL_DATA_SIZE = BasicObjLoader.ObjData.NORMAL_SIZE;

    private static final int POSITION_OFFSET = BasicObjLoader.ObjData.POSITION_OFFSET * BYTES_PER_FLOAT;
    private static final int TEXTURE_COORD_OFFSET = BasicObjLoader.ObjData.TEXTURE_COORD_OFFSET * BYTES_PER_FLOAT;
    private static final int NORMAL_OFFSET = BasicObjLoader.ObjData.NORMAL_OFFSET * BYTES_PER_FLOAT;
    private static final int VERTEX_DATA_STRIDE = BasicObjLoader.ObjData.VERTEX_DATA_SIZE * BYTES_PER_FLOAT;

    private static final float[] COLOR = {0.5f, 0.5f, 0.5f, 0.5f};

    private int mVertexNumber;

    private int[] mVertexGLBuffer = new int[1];

    protected Material mMaterial = new Material();

    private int mProgram;
    private int mVPMatrixHandle;
    private int mMMatrixHandle;
    private int mPositionHandle;
    private int mNormalHandle;
    private int mLightPositionHandle;
    private int mAmbientHandle;
    private int mDiffusionHandle;
    private int mSpecularHandle;

    private int mMaterialAmbientHandler;
    private int mMaterialDiffusionHandler;
    private int mMaterialSpecularHandler;
    private int mMaterialRoughnessHandler;

    private int mEyePositionHandle;
    private int mColorHandler;

    public LightObjObject(Context context, BasicObjLoader.ObjData objData) {
        if (!objData.hasNormal) {
            BasicObjLoader.manualCalculateNormal(objData);
        }
        float[] vertexData = objData.vertexData;

        ByteBuffer vbb = ByteBuffer.allocateDirect(BYTES_PER_FLOAT * vertexData.length);
        vbb.order(ByteOrder.nativeOrder());
        vbb.asFloatBuffer().put(vertexData);
        vbb.position(0);

        GLES20.glGenBuffers(1, mVertexGLBuffer, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexGLBuffer[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vbb.capacity(), vbb, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        mVertexNumber = objData.vertexNumber;

        String vertexCode = Utils.loadFromAssetsFile(context, "shaders/light_vertex.glsl");
        String fragmentCode = Utils.loadFromAssetsFile(context, "shaders/light_fragment.glsl");
        mProgram = Utils.createProgram(vertexCode, fragmentCode);

        mVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uVPMatrix");
        mMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
        mLightPositionHandle = GLES20.glGetUniformLocation(mProgram, "uLightPosition");
        mAmbientHandle = GLES20.glGetUniformLocation(mProgram, "uAmbient");
        mDiffusionHandle = GLES20.glGetUniformLocation(mProgram, "uDiffusion");
        mSpecularHandle = GLES20.glGetUniformLocation(mProgram, "uSpecular");

        mMaterialAmbientHandler = GLES20.glGetUniformLocation(mProgram, "uMaterial.ambient");
        mMaterialDiffusionHandler = GLES20.glGetUniformLocation(mProgram, "uMaterial.diffusion");
        mMaterialSpecularHandler = GLES20.glGetUniformLocation(mProgram, "uMaterial.specular");
        mMaterialRoughnessHandler = GLES20.glGetUniformLocation(mProgram, "uMaterial.roughness");

        mEyePositionHandle = GLES20.glGetUniformLocation(mProgram, "uEyePosition");
        mColorHandler = GLES20.glGetUniformLocation(mProgram, "uColor");
    }

    public void draw(float[] vpMatrix, float[] moduleMatrix, Light light, float[] eyePosition) {
        boolean cullFace = GLES20.glIsEnabled(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        GLES20.glUseProgram(mProgram);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexGLBuffer[0]);

        GLES20.glUniformMatrix4fv(mVPMatrixHandle, 1, false, vpMatrix, 0);
        GLES20.glUniformMatrix4fv(mMMatrixHandle, 1, false, moduleMatrix, 0);
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_STRIDE, POSITION_OFFSET);
        GLES20.glVertexAttribPointer(mNormalHandle, NORMAL_DATA_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_STRIDE, NORMAL_OFFSET);
        GLES20.glUniform4fv(mLightPositionHandle, 1, light.position, 0);
        GLES20.glUniform4fv(mAmbientHandle, 1, light.ambientChannel, 0);
        GLES20.glUniform4fv(mDiffusionHandle, 1, light.diffusionChannel, 0);
        GLES20.glUniform4fv(mSpecularHandle, 1, light.specularChannel, 0);
        GLES20.glUniform4fv(mMaterialAmbientHandler, 1, mMaterial.ambient, 0);
        GLES20.glUniform4fv(mMaterialDiffusionHandler, 1, mMaterial.diffusion, 0);
        GLES20.glUniform4fv(mMaterialSpecularHandler, 1, mMaterial.specular, 0);
        GLES20.glUniform1f(mMaterialRoughnessHandler, mMaterial.roughness);
        GLES20.glUniform3fv(mEyePositionHandle, 1, eyePosition, 0);
        GLES20.glUniform4fv(mColorHandler, 1, COLOR, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexNumber);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
        GLES20.glUseProgram(0);

        if (cullFace) {
            GLES20.glEnable(GLES20.GL_CULL_FACE);
        }
    }
}
