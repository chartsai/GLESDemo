#define MAX_LIGHT_NUMBER 16
uniform mat4 uMVPMatrix;
attribute vec3 aPosition;
attribute vec3 aNormal;
struct Light {
    vec4 position;
    vec4 ambient;
    vec4 diffusion;
    vec4 specular;
};
uniform Light uLight[MAX_LIGHT_NUMBER];
uniform int uLightNumber;

uniform vec3 uEyePosition;
varying vec4 vLightColor;
vec4 calDiffusion(in Light light, in vec3 position, in vec3 normal) {
    vec3 normalizedNormal = normalize(normal);
    vec3 normalizedLightDirection = normalize(light.position.xyz - position);
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

    vec4 lightColor = vec4(0.0);
    for (int i = 0; i < uLightNumber && i < MAX_LIGHT_NUMBER; i++) {
        Light light = uLight[i];
        vec4 diffusion = calDiffusion(light, aPosition, aNormal);
        vec4 specular = calSpecular(light, aPosition, aNormal, uEyePosition);
        lightColor = lightColor + light.ambient + diffusion + specular;
    }
    vLightColor = lightColor;
}
