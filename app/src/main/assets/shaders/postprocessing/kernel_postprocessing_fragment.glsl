precision highp float;
#define MAX_KERNEL_LENGTH 16
#define MAX_KERNEL_SIZE MAX_KERNEL_LENGTH * MAX_KERNEL_LENGTH
uniform sampler2D uColorTexture;
uniform sampler2D uDepthTexture;

uniform int uKernelLength;
uniform float uKernel[MAX_KERNEL_SIZE];
uniform vec2 uSampleUnit;

varying vec2 vTextureCoord;
void main() {
    int halfLength = uKernelLength / 2;
    vec4 colorSum = vec4(0.0);
    for (int i = 0; i < uKernelLength; i++) {
        for (int j = 0; j < uKernelLength; j++) {
            vec2 textureCoord = vTextureCoord + vec2(float(i - halfLength), float(j - halfLength)) * uSampleUnit;
            colorSum += texture2D(uColorTexture, textureCoord) * uKernel[i + j * uKernelLength];
        }
    }
    gl_FragColor = colorSum;
}