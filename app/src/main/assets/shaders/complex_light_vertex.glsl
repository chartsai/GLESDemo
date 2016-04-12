uniform mat4 uMVPMatrix;
attribute vec3 aPosition;
attribute vec3 aNormal;
struct Light {
    vec4 position;
    vec4 ambient;
    vec4 diffusion;
    vec4 specular;
};
uniform Light uLight;
//Light uLight = {
//    vec4(200, 200, 200, 1.0),
//    vec4(0.15, 0.15, 0.15, 1.0),
//    vec4(0.3, 0.3, 0.3, 1.0),
//    vec4(0.4, 0.4, 0.4, 1.0),
//};

uniform vec3 uEyePosition;
varying vec4 vLightColor;
vec4 calDiffusion(in Light light, in vec3 position, in vec3 normal) {
    vec3 normalizedNormal = normalize(normal);
    vec3 normalizedLightDirection = normalize(uLight.position.xyz - position);
    float factor = max(0.0, dot(normalizedNormal, normalizedLightDirection));
    return factor * light.diffusion;
}
vec4 calSpecular(in Light light, in vec3 position, in vec3 normal, in vec3 eyePosition) {
    vec3 normalizedNormal = normalize(normal);
    vec3 normalizeLightDirection = normalize(light.position.xyz - position);
    vec3 normalizedEyeDirection = normalize(eyePosition - position);
    vec3 halfVector = normalize(normalizeLightDirection + normalizedEyeDirection);
    float factor = max(0.0, dot(normalizedNormal, halfVector));
    return factor * light.specular;
}
void main() {
    gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
    vec4 diffusion = calDiffusion(uLight, aPosition, aNormal);
    vec4 specular = calSpecular(uLight, aPosition, aNormal, uEyePosition);
    vLightColor = uLight.ambient + diffusion + specular;
}
