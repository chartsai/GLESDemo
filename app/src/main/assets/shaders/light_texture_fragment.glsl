precision mediump float;
uniform sampler2D uTextureSampler;
varying vec4 vLightColor;
varying vec2 vTextureCoord;
void main() {
    vec4 textureColor = texture2D(uTextureSampler, vTextureCoord);
    vec4 finalColor = textureColor * vLightColor;
    if (finalColor.a == 0.0) {
      discard;
    } else {
        gl_FragColor = finalColor;
    }
}