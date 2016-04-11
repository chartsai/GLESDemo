precision mediump float;
uniform sampler2D uTextureSampler;
varying vec4 vLightColor;
varying vec2 vTextureCoord;
void main() {
    vec4 textureColor = texture2D(uTextureSampler, vTextureCoord);
    gl_FragColor = textureColor * vLightColor;
}