uniform mat4 uVPMatrix;
uniform mat4 uMMatrix;
attribute vec3 aPosition;
attribute vec2 aTextureCoord;
attribute vec3 aNormal;

varying vec3 vPosition;
varying vec3 vNormal;
varying vec2 vTextureCoord;

void main() {
    vec4 position = uMMatrix * vec4(aPosition, 1.0);
    gl_Position = uVPMatrix * position;

    vPosition = position.xyz;
    vNormal = normalize((uMMatrix * vec4(aNormal, 0.0)).xyz);
    vTextureCoord = aTextureCoord;
}
