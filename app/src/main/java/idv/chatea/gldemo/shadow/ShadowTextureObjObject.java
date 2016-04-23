package idv.chatea.gldemo.shadow;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import idv.chatea.gldemo.gles20.Utils;
import idv.chatea.gldemo.lighting.Light;
import idv.chatea.gldemo.lighting.Material;
import idv.chatea.gldemo.objloader.BasicObjLoader;

public class ShadowTextureObjObject {
    private static final int BYTES_PER_FLOAT = Float.SIZE / Byte.SIZE;

    private static final int POSITION_DATA_SIZE = BasicObjLoader.ObjData.POSITION_SIZE;
    private static final int TEXTURE_COORD_DATA_SIZE = BasicObjLoader.ObjData.TEXTURE_COORD_SIZE;
    private static final int NORMAL_DATA_SIZE = BasicObjLoader.ObjData.NORMAL_SIZE;

    private static final int POSITION_OFFSET = BasicObjLoader.ObjData.POSITION_OFFSET * BYTES_PER_FLOAT;
    private static final int TEXTURE_COORD_OFFSET = BasicObjLoader.ObjData.TEXTURE_COORD_OFFSET * BYTES_PER_FLOAT;
    private static final int NORMAL_OFFSET = BasicObjLoader.ObjData.NORMAL_OFFSET * BYTES_PER_FLOAT;
    private static final int VERTEX_DATA_STRIDE = BasicObjLoader.ObjData.VERTEX_DATA_SIZE * BYTES_PER_FLOAT;

    private int mVertexNumber;

    private int[] mVertexGLBuffer = new int[1];
    private int[] mGLTextures = new int[1]; // TODO change number of texture.

    protected Material mMaterial = new Material();

    private int mProgram;
    private int mVPMatrixHandle;
    private int mMMatrixHandle;
    private int mPositionHandle;
    private int mTextureCoordHandle;
    private int mNormalHandle;

    private int mEyePositionHandle;
    private int mLightPositionHandle;
    private int mAmbientHandle;
    private int mDiffusionHandle;
    private int mSpecularHandle;

    private int mMaterialAmbientHandle;
    private int mMaterialDiffusionHandle;
    private int mMaterialSpecularHandle;
    private int mMaterialRoughnessHandle;

    private int mTextureSamplerHandle;

    private int mLightVPMatrixHandle;
    private int mShadowDepthMapHandle;

    // TODO read bitmap from objData.
    public ShadowTextureObjObject(Context context, BasicObjLoader.ObjData objData, Bitmap bitmap) {
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

        GLES20.glGenTextures(1, mGLTextures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGLTextures[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        String vertexCode = Utils.loadFromAssetsFile(context, "shaders/shadow_texture_vertex.glsl");
        String fragmentCode = Utils.loadFromAssetsFile(context, "shaders/shadow_texture_fragment.glsl");
        mProgram = Utils.createProgram(vertexCode, fragmentCode);

        mVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uVPMatrix");
        mMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");

        mEyePositionHandle = GLES20.glGetUniformLocation(mProgram, "uEyePosition");
        mLightPositionHandle = GLES20.glGetUniformLocation(mProgram, "uLight.position");
        mAmbientHandle = GLES20.glGetUniformLocation(mProgram, "uLight.ambient");
        mDiffusionHandle = GLES20.glGetUniformLocation(mProgram, "uLight.diffusion");
        mSpecularHandle = GLES20.glGetUniformLocation(mProgram, "uLight.specular");

        mMaterialAmbientHandle = GLES20.glGetUniformLocation(mProgram, "uMaterial.ambient");
        mMaterialDiffusionHandle = GLES20.glGetUniformLocation(mProgram, "uMaterial.diffusion");
        mMaterialSpecularHandle = GLES20.glGetUniformLocation(mProgram, "uMaterial.specular");
        mMaterialRoughnessHandle = GLES20.glGetUniformLocation(mProgram, "uMaterial.roughness");

        mTextureSamplerHandle = GLES20.glGetUniformLocation(mProgram, "uTextureSampler");

        mLightVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uShadow.lightVPMatrix");
        mShadowDepthMapHandle = GLES20.glGetUniformLocation(mProgram, "uShadow.shadowMap");


        /** Setup material */
        mMaterial.ambient = new float[] {1, 1, 1, 1};
        mMaterial.diffusion = new float[] {0.3f, 0.3f, 0.3f, 0.3f};
        mMaterial.specular = new float[] {0.5f, 0.5f, 0.5f, 0.5f};
        mMaterial.roughness = 10f;
    }

    public int getTexture() {
        return mGLTextures[0];
    }

    public void setMaterial(Material material) {
        mMaterial = material;
    }

    public Material getMaterial() {
        return mMaterial;
    }

    public void draw(float[] vpMatrix, float[] moduleMatrix, float[] eyePosition, ShadowEffectData shadowEffectData) {
        Light light = shadowEffectData.light;

        boolean cullFace = GLES20.glIsEnabled(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        GLES20.glUseProgram(mProgram);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexGLBuffer[0]);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGLTextures[0]);

        GLES20.glUniformMatrix4fv(mVPMatrixHandle, 1, false, vpMatrix, 0);
        GLES20.glUniformMatrix4fv(mMMatrixHandle, 1, false, moduleMatrix, 0);
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_STRIDE, POSITION_OFFSET);
        GLES20.glVertexAttribPointer(mTextureCoordHandle, TEXTURE_COORD_DATA_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_STRIDE, TEXTURE_COORD_OFFSET);
        GLES20.glVertexAttribPointer(mNormalHandle, NORMAL_DATA_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_STRIDE, NORMAL_OFFSET);

        GLES20.glUniform3fv(mEyePositionHandle, 1, eyePosition, 0);
        GLES20.glUniform4fv(mLightPositionHandle, 1, light.position, 0);
        GLES20.glUniform4fv(mAmbientHandle, 1, light.ambientChannel, 0);
        GLES20.glUniform4fv(mDiffusionHandle, 1, light.diffusionChannel, 0);
        GLES20.glUniform4fv(mSpecularHandle, 1, light.specularChannel, 0);

        GLES20.glUniform4fv(mMaterialAmbientHandle, 1, mMaterial.ambient, 0);
        GLES20.glUniform4fv(mMaterialDiffusionHandle, 1, mMaterial.diffusion, 0);
        GLES20.glUniform4fv(mMaterialSpecularHandle, 1, mMaterial.specular, 0);
        GLES20.glUniform1f(mMaterialRoughnessHandle, mMaterial.roughness);

        GLES20.glUniform1i(mTextureSamplerHandle, 0);

        GLES20.glUniformMatrix4fv(mLightVPMatrixHandle, 1, false, shadowEffectData.lightVPMatrix, 0);
        GLES20.glUniform1i(mShadowDepthMapHandle, 1);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexNumber);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
        GLES20.glUseProgram(0);

        if (cullFace) {
            GLES20.glEnable(GLES20.GL_CULL_FACE);
        }
    }
}
