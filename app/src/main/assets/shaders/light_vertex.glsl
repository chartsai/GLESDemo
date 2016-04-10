uniform mat4 uMVPMatrix;
attribute vec3 aPosition;
attribute vec3 aNormal;
uniform vec4 uLightPosition;
uniform vec4 uAmbient;
uniform vec4 uDiffusion;
uniform vec4 uSpecular;
uniform vec3 uEyePosition;
varying vec4 vLightColor;
vec4 calDiffusion() {
    vec3 normalizedNormal = normalize(aNormal);
    vec3 normalizedLightDirection = normalize(uLightPosition.xyz - aPosition);
    float factor = max(0.0, dot(normalizedNormal, normalizedLightDirection));
    return factor * uDiffusion;
}
vec4 calSpecular() {
    vec3 normalizedNormal = normalize(aNormal);
    vec3 normalizeLightDirection = normalize(uLightPosition.xyz - aPosition);
    vec3 normalizedEyeDirection = normalize(uEyePosition - aPosition);
    vec3 halfVector = normalize(normalizeLightDirection + normalizedEyeDirection);
    float factor = max(0.0, dot(normalizedNormal, halfVector));
    return factor * uSpecular;
}
void main() {
    gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
    vec4 diffusion = calDiffusion();
    vec4 specular = calSpecular();
    vLightColor = uAmbient + diffusion + specular;
}
