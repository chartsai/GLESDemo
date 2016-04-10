precision mediump float;
uniform vec4 uColor;
varying vec4 vLightColor;
void main() {
    gl_FragColor = uColor * vLightColor;
}