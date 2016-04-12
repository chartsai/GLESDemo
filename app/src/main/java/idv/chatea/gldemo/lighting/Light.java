package idv.chatea.gldemo.lighting;

public class Light {
    /**
     * x, y, z, w. if W = 0, it is direction light, otherwise it is position light.
     */
    public float[] position = new float[4];

    public float[] ambientChannel = new float[4];
    public float[] diffusionChannel = new float[4];
    public float[] specularChannel = new float[4];

    // TODO geometric diffusion channel and specular channel
}
