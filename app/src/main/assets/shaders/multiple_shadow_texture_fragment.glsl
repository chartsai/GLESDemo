precision mediump float;
#define MAX_SHADOW_NUMBER 8
uniform vec3 uEyePosition;

struct Material {
    vec4 ambient;
    vec4 diffusion;
    vec4 specular;
    float roughness;
};
uniform Material uMaterial;

uniform sampler2D uTextureSampler;

struct Light {
    vec4 position;
    vec4 ambient;
    vec4 diffusion;
    vec4 specular;
};

struct Shadow {
    mat4 lightVPMatrix;
    // This is a WAR, if put shadowMap as a part of Shadow, then get compiled failed.
//    sampler2D shadowMap;
    Light light;
};
uniform Shadow uShadow[MAX_SHADOW_NUMBER];
uniform int uShadowNumber;
// WAR
uniform sampler2D uShadowMap[MAX_SHADOW_NUMBER];

varying vec3 vPosition;
varying vec3 vNormal;
varying vec2 vTextureCoord;

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
    float factor = dot(vNormal, halfVector);
    if (factor <= 0.0) {
        factor = 0.0;
    } else {
        pow(factor, uMaterial.roughness);
    }
    return uMaterial.specular * factor * light.specular;
}
bool isCovered(in mat4 lightVPMatrix, in sampler2D shadowMap) {
    vec4 positionInLightView = lightVPMatrix * vec4(vPosition, 1.0);
    // normal form the positionInLightView.
    positionInLightView = positionInLightView / positionInLightView.w;
    // view coord is in range [-1, 1], we need to map to texture's coord [0. 1]
    //   => (view coord) * 0.5 + 0.5 = texture coord
    vec3 shadowMapCoord = positionInLightView.xyz * 0.5 + vec3(0.5);
    vec4 textureColor = texture2D(shadowMap, shadowMapCoord.xy);
    if (shadowMapCoord.z >= textureColor.z) {
        return true;
    } else {
        return false;
    }
}
void main() {
    vec4 textureColor = texture2D(uTextureSampler, vTextureCoord);

    vec4 finalColor = vec4(0.0);
    for (int i = 0; i < uShadowNumber && i < MAX_SHADOW_NUMBER; i++) {
        Light light = uShadow[i].light;
        vec4 lightColor = calAmbient(light);
        if (dot(vNormal, light.position.xyz - vPosition) > 0.0) {
            // Check if Light and Triangle are in the same side.
            if (!isCovered(uShadow[i].lightVPMatrix, uShadowMap[i])) {
                // only add diffusion and specular when not covered.
                lightColor = lightColor + calDiffusion(light) + calSpecular(light);
            }
        }
        finalColor = finalColor + lightColor;
    }
    finalColor = textureColor * finalColor;
    if (finalColor.a == 0.0) {
        discard;
    } else {
        gl_FragColor = finalColor;
    }
}
