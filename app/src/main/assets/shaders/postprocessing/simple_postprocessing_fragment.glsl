precision mediump float;
uniform sampler2D uColorTexture;
//uniform sampler2D uDepthTexture;

varying vec2 vTextureCoord;
void main() {
    gl_FragColor = texture2D(uColorTexture, vTextureCoord);
}