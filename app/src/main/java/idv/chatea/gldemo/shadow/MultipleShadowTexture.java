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

/**
 * A Object who render rectangle texture and it can receive multiple shadows.
 */
public class MultipleShadowTexture {

    // TODO how to determine this value?
    public static final int MAX_NUMBER_OF_SHADOW = 8;

    private static final float[] VERTEX_DATA = {
            -0.5f, -0.5f, 0, 0, 0, 1, 0, 1, // left-bottom corner
             0.5f, -0.5f, 0, 0, 0, 1, 1, 1, // right-bottom corner
            -0.5f,  0.5f, 0, 0, 0, 1, 0, 0, // left-top corner
             0.5f,  0.5f, 0, 0, 0, 1, 1, 0, // right-top corner
    };

    private static final int BYTES_PER_FLOAT = Float.SIZE / Byte.SIZE;

    private static final int POSITION_DATA_SIZE = 3;
    private static final int NORMAL_DATA_SIZE = 3;
    private static final int TEXTURE_COORD_DATA_SIZE = 2;

    private static final int POSITION_OFFSET = 0 * BYTES_PER_FLOAT;
    private static final int NORMAL_OFFSET = POSITION_DATA_SIZE * BYTES_PER_FLOAT;
    private static final int TEXTURE_COORD_OFFSET = (POSITION_DATA_SIZE + NORMAL_DATA_SIZE) * BYTES_PER_FLOAT;

    private static final int VERTEX_DATA_STRIDE =
            (POSITION_DATA_SIZE + NORMAL_DATA_SIZE + TEXTURE_COORD_DATA_SIZE) * BYTES_PER_FLOAT;

    private static final byte[] INDEX_DATA = {
            0, 1, 2, 2, 1, 3,
    };

    protected Material mMaterial = new Material();

    private int[] mVertexGLBuffer = new int[1];
    private int[] mIndexGLBuffer = new int[1];
    private int[] mGLTextures = new int[1];

    private int mProgram;
    private int mVPMatrixHandle;
    private int mMMatrixHandle;
    private int mPositionHandle;
    private int mTextureCoordHandle;
    private int mNormalHandle;

    private int mEyePositionHandle;

    private int mMaterialAmbientHandle;
    private int mMaterialDiffusionHandle;
    private int mMaterialSpecularHandle;
    private int mMaterialRoughnessHandle;

    private int mTextureSamplerHandle;

    private int mShadowNumberHandle;

    public MultipleShadowTexture(Context context, Bitmap bitmap) {
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


        GLES20.glGenTextures(1, mGLTextures, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGLTextures[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        String vertexCode = Utils.loadFromAssetsFile(context, "shaders/shadow_texture_vertex.glsl");
        String fragmentCode = Utils.loadFromAssetsFile(context, "shaders/multiple_shadow_texture_fragment.glsl");
        mProgram = Utils.createProgram(vertexCode, fragmentCode);

        mVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uVPMatrix");
        mMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");

        mEyePositionHandle = GLES20.glGetUniformLocation(mProgram, "uEyePosition");

        mMaterialAmbientHandle = GLES20.glGetUniformLocation(mProgram, "uMaterial.ambient");
        mMaterialDiffusionHandle = GLES20.glGetUniformLocation(mProgram, "uMaterial.diffusion");
        mMaterialSpecularHandle = GLES20.glGetUniformLocation(mProgram, "uMaterial.specular");
        mMaterialRoughnessHandle = GLES20.glGetUniformLocation(mProgram, "uMaterial.roughness");

        mTextureSamplerHandle = GLES20.glGetUniformLocation(mProgram, "uTextureSampler");

        mShadowNumberHandle = GLES20.glGetUniformLocation(mProgram, "uShadowNumber");

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

    public void draw(float[] vpMatrix, float[] moduleMatrix, float[] eyePosition, ShadowEffectData[] shadowEffectDatas) {
        GLES20.glUseProgram(mProgram);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexGLBuffer[0]);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGLTextures[0]);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexGLBuffer[0]);

        GLES20.glUniformMatrix4fv(mVPMatrixHandle, 1, false, vpMatrix, 0);
        GLES20.glUniformMatrix4fv(mMMatrixHandle, 1, false, moduleMatrix, 0);
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_STRIDE, POSITION_OFFSET);
        GLES20.glVertexAttribPointer(mTextureCoordHandle, TEXTURE_COORD_DATA_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_STRIDE, TEXTURE_COORD_OFFSET);
        GLES20.glVertexAttribPointer(mNormalHandle, NORMAL_DATA_SIZE, GLES20.GL_FLOAT, false, VERTEX_DATA_STRIDE, NORMAL_OFFSET);

        GLES20.glUniform3fv(mEyePositionHandle, 1, eyePosition, 0);

        GLES20.glUniform4fv(mMaterialAmbientHandle, 1, mMaterial.ambient, 0);
        GLES20.glUniform4fv(mMaterialDiffusionHandle, 1, mMaterial.diffusion, 0);
        GLES20.glUniform4fv(mMaterialSpecularHandle, 1, mMaterial.specular, 0);
        GLES20.glUniform1f(mMaterialRoughnessHandle, mMaterial.roughness);

        GLES20.glUniform1i(mTextureSamplerHandle, 0);

        for (int i = 0; i < shadowEffectDatas.length && i < MAX_NUMBER_OF_SHADOW; i++) {
            ShadowEffectData shadowEffectData = shadowEffectDatas[i];
            Light light = shadowEffectData.light;

            GLES20.glActiveTexture(GLES20.GL_TEXTURE1 + i);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, shadowEffectData.shadow.getDepthMap());

            int lightVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, String.format("uShadow[%d].lightVPMatrix", i));
            // WAR for glsl: opaque type in array of struct cause shader compiling failed...
//            int shadowDepthMapHandle = GLES20.glGetUniformLocation(mProgram, String.format("uShadow[%d].shadowMap", i));
            int shadowDepthMapHandle = GLES20.glGetUniformLocation(mProgram, String.format("uShadowMap[%d]", i));
            int lightPositionHandle = GLES20.glGetUniformLocation(mProgram, String.format("uShadow[%d].light.position", i));
            int ambientHandle = GLES20.glGetUniformLocation(mProgram, String.format("uShadow[%d].light.ambient", i));
            int diffusionHandle = GLES20.glGetUniformLocation(mProgram, String.format("uShadow[%d].light.diffusion", i));
            int specularHandle = GLES20.glGetUniformLocation(mProgram, String.format("uShadow[%d].light.specular", i));

            GLES20.glUniformMatrix4fv(lightVPMatrixHandle, 1, false, shadowEffectData.lightVPMatrix, 0);
            GLES20.glUniform1i(shadowDepthMapHandle, 1 + i);
            GLES20.glUniform4fv(lightPositionHandle, 1, light.position, 0);
            GLES20.glUniform4fv(ambientHandle, 1, light.ambientChannel, 0);
            GLES20.glUniform4fv(diffusionHandle, 1, light.diffusionChannel, 0);
            GLES20.glUniform4fv(specularHandle, 1, light.specularChannel, 0);
        }
        GLES20.glUniform1i(mShadowNumberHandle, shadowEffectDatas.length);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, INDEX_DATA.length, GLES20.GL_UNSIGNED_BYTE, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
        GLES20.glUseProgram(0);
    }
}
