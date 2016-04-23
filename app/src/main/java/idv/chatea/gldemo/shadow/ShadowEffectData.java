package idv.chatea.gldemo.shadow;

import idv.chatea.gldemo.lighting.Light;

/**
 * DS used to store the necessary data of shadow effect.
 */
public class ShadowEffectData {

    public ShadowEffectData(Shadow shadow, Light light, float[] lightVPMatrix) {
        this.shadow = shadow;
        this.light = light;
        this.lightVPMatrix = lightVPMatrix;
    }

    public Shadow shadow;
    public Light light;
    public float[] lightVPMatrix;
}
