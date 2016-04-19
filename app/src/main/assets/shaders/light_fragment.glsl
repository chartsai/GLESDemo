precision mediump float;
uniform vec4 uLightPosition;
uniform vec4 uAmbient;
uniform vec4 uDiffusion;
uniform vec4 uSpecular;
uniform vec3 uEyePosition;

struct Material {
    vec4 ambient;
    vec4 diffusion;
    vec4 specular;
    float roughness;
};
uniform Material uMaterial;
uniform vec4 uColor;

varying vec3 vPosition;
varying vec3 vNormal;

vec4 calAmbient() {
    return uMaterial.ambient * uAmbient;
}
vec4 calDiffusion() {
    vec3 normalizedLightDirection = normalize(uLightPosition.xyz - vPosition);
    float factor = max(0.0, dot(vNormal, normalizedLightDirection));
    return uMaterial.diffusion * factor * uDiffusion;
}
vec4 calSpecular() {
    vec3 normalizeLightDirection = normalize(uLightPosition.xyz - vPosition);
    vec3 normalizedEyeDirection = normalize(uEyePosition - vPosition);
    vec3 halfVector = normalize(normalizeLightDirection + normalizedEyeDirection);
    float factor = dot(vNormal, halfVector);
    if (factor <= 0.0) {
        factor = 0.0;
    } else {
        pow(factor, uMaterial.roughness);
    }
    return uMaterial.specular * factor * uSpecular;
}
void main() {
    vec4 lightColor = calAmbient() + calDiffusion() + calSpecular();
    gl_FragColor = uColor * lightColor;
}
