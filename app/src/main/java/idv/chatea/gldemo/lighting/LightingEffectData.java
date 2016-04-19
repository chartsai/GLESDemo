package idv.chatea.gldemo.lighting;

/**
 * DS used to store the necessary data of lighting effect
 */
public class LightingEffectData {

    public LightingEffectData(Light light, float[] eyePosition) {
        this.light = light;
        this.eyePosition = eyePosition;
    }

    public Light light;
    public float[] eyePosition;
}
