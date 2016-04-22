precision mediump float;
#define MAX_LIGHT_NUMBER 16

struct Light {
    vec4 position;
    vec4 ambient;
    vec4 diffusion;
    vec4 specular;
};
uniform Light uLight[MAX_LIGHT_NUMBER];
uniform int uLightNumber;

struct Material {
    vec4 ambient;
    vec4 diffusion;
    vec4 specular;
    float roughness;
};
uniform Material uMaterial;

uniform vec3 uEyePosition;

varying vec3 vPosition;
varying vec3 vNormal;

vec4 calAmbient(in Light light) {
    return uMaterial.ambient * light.ambient;
}
vec4 calDiffusion(in Light light) {
    vec3 normalizedLightDirection = normalize(light.position.xyz - vPosition);
    float factor = max(0.0, dot(vNormal, normalizedLightDirection));
    return uMaterial.diffusion * factor * light.diffusion;
}
vec4 calSpecular(in Light light) {
    vec3 normalizeLightDirection = normalize(light.position.xyz - vPosition);
    vec3 normalizedEyeDirection = normalize(uEyePosition - vPosition);
    vec3 halfVector = normalize(normalizeLightDirection + normalizedEyeDirection);
    float factor = max(0.0, dot(vNormal, halfVector));
    return uMaterial.specular * factor * light.specular;
}
void main() {
    vec4 lightColor = vec4(0.0);
    for (int i = 0; i < uLightNumber && i < MAX_LIGHT_NUMBER; i++) {
        Light light = uLight[i];
        vec4 ambient = calAmbient(light);
        vec4 diffusion = calDiffusion(light);
        vec4 specular = calSpecular(light);
        lightColor = lightColor + ambient + diffusion + specular;
    }
    gl_FragColor = lightColor;
}
