package idv.chatea.gldemo.shadow;

import idv.chatea.gldemo.lighting.LightingEffectData;

/**
 * DS used to store the necessary data of shadow effect.
 */
public class ShadowEffectData {

    public ShadowEffectData(Shadow shadow, LightingEffectData lightingEffectData, float[] lightVPMatrix) {
        this.shadow = shadow;
        this.lightingEffectData = lightingEffectData;
        this.lightVPMatrix = lightVPMatrix;
    }

    public Shadow shadow;
    public LightingEffectData lightingEffectData;
    public float[] lightVPMatrix;
}
