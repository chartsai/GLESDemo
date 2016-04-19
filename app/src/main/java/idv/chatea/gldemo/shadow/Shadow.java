package idv.chatea.gldemo.shadow;

import android.opengl.GLES20;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

public class Shadow {
    private int[] mFBOIds;
    private int[] mDepthMap;

    private boolean[] mCachedColorMask = new boolean[4];
    private boolean[] mCachedDepthMask = new boolean[1];

    /**
     * Must run in GLThread
     */
    public Shadow(int width, int height) {
        if (((EGL10) EGLContext.getEGL()).eglGetCurrentContext().equals(EGL10.EGL_NO_CONTEXT)) {
            // no current context.
            throw new IllegalStateException("There is no EGLContext");
        }

        mFBOIds = new int[1];
        mDepthMap = new int[1];

        // bind depth texture to FBO
        GLES20.glGenFramebuffers(1, mFBOIds, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBOIds[0]);

        GLES20.glGenTextures(1, mDepthMap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDepthMap[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_DEPTH_COMPONENT, width, height, 0, GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_SHORT, null);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, mDepthMap[0], 0);

        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Create GL Framebuffer of shadow failed.");
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public int getDepthMap() {
        return mDepthMap[0];
    }

    /**
     * Must be call before using shadow
     */
    public void startDrawShadowTexture() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBOIds[0]);

        GLES20.glCullFace(GLES20.GL_FRONT);

        GLES20.glGetBooleanv(GLES20.GL_COLOR_WRITEMASK, mCachedColorMask, 0);
        GLES20.glColorMask(false, false, false, false);
        GLES20.glGetBooleanv(GLES20.GL_DEPTH_WRITEMASK, mCachedDepthMask, 0);
        GLES20.glDepthMask(true);
    }

    /**
     * Must be call after using shadow complete
     */
    public void stopDrawShadowTexture() {
        GLES20.glColorMask(mCachedColorMask[0], mCachedColorMask[1], mCachedColorMask[2], mCachedColorMask[3]);
        GLES20.glDepthMask(mCachedDepthMask[0]);

        GLES20.glCullFace(GLES20.GL_BACK);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    protected void finalize() throws Throwable {
        // TODO clear GL resource.
        super.finalize();
    }
}
