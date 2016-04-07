package idv.chatea.gldemo.firework;

import android.support.annotation.CallSuper;

/**
 * Created by chatea on 2016/2/3.
 * @author Charlie Tsai (chatea)
 */
public abstract class GLFirework {

    private Shader mShader;

    public void bindShader(Shader shader) {
        mShader = shader;
    }

    @CallSuper
    public void render(float[] mvpMatrix) {
        mShader.render(this, mvpMatrix);
    }

    abstract boolean isDead();

    interface Shader<T extends GLFirework> {
        void render(T target, float[] mvpMatrix);
    }
}
