package idv.chatea.gldemo.lighting;

public class ComplexLight {
    private float[] light = new float[16];

    public float[] getPosition() {
        return new float[]{light[0], light[1], light[2], light[3]};
    }

    public void setPosition(float x, float y, float z, float w) {
        light[0] = x;
        light[1] = y;
        light[2] = z;
        light[3] = w;
    }

    public float[] getAmbienChannel() {
        return new float[]{light[4], light[5], light[6], light[7]};
    }

    public void setAmbientChannel(float r, float g, float b, float a) {
        light[4] = r;
        light[5] = g;
        light[6] = b;
        light[7] = a;
    }

    public float[] getDiffusionChannel() {
        return new float[]{light[8], light[9], light[10], light[11]};
    }

    public void setDiffusionChannel(float r, float g, float b, float a) {
        light[8] = r;
        light[9] = g;
        light[10] = b;
        light[11] = a;
    }

    public float[] getSpecularChannel() {
        return new float[]{light[12], light[13], light[14], light[15]};
    }

    public void setSpecularChannel(float r, float g, float b, float a) {
        light[12] = r;
        light[13] = g;
        light[14] = b;
        light[15] = a;
    }

    // TODO geometric diffusion channel and specular channel

    public float[] getLight() {
        return light;
    }
}
