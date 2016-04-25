package idv.chatea.gldemo.postprocess;

import android.opengl.GLES20;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

/**
 * FrameBuffer provide a FBO with 1 ColorTexture and 1 DepthTexture.
 * This is useful to store as result of pre-processing, then used for post processing.
 *
 * The color is argb per 8 bit and the depth is 8 bit.
 */
public class FrameBuffer {
    private float mWidth;
    private float mHeight;

    private int[] mFBOIds;
    private int[] mColorTextureIds;
    private int[] mDepthTextureIds;
    // TODO stencil texture.

    /**
     * Must run in GLThread
     */
    public FrameBuffer(int width, int height) {
        if (((EGL10) EGLContext.getEGL()).eglGetCurrentContext().equals(EGL10.EGL_NO_CONTEXT)) {
            // no current context.
            throw new IllegalStateException("There is no EGLContext");
        }

        mWidth = width;
        mHeight = height;

        mFBOIds = new int[1];
        mColorTextureIds = new int[1];
        mDepthTextureIds = new int[1];

        // bind depth texture to FBO
        GLES20.glGenFramebuffers(1, mFBOIds, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBOIds[0]);

        GLES20.glGenTextures(1, mColorTextureIds, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mColorTextureIds[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glGenTextures(1, mDepthTextureIds, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDepthTextureIds[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_DEPTH_COMPONENT, width, height, 0, GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_SHORT, null);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mColorTextureIds[0], 0);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, mDepthTextureIds[0], 0);

        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Create GL Framebuffer of shadow failed.");
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public float getWidth() {
        return mWidth;
    }

    public float getHeight() {
        return mHeight;
    }

    public int getColorTexture() {
        return mColorTextureIds[0];
    }

    public int getDepthTexture() {
        return mDepthTextureIds[0];
    }

    /**
     * Must be call before using shadow
     */
    public void bind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBOIds[0]);
    }

    /**
     * Must be call after using shadow complete
     */
    public void unbind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    protected void finalize() throws Throwable {
        // TODO clear GL resource.
        super.finalize();
    }
}
