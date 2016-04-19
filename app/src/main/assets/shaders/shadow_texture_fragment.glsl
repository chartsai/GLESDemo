precision mediump float;
uniform vec3 uEyePosition;

struct Light {
    vec4 position;
    vec4 ambient;
    vec4 diffusion;
    vec4 specular;
};

uniform Light uLight;

struct Material {
    vec4 ambient;
    vec4 diffusion;
    vec4 specular;
    float roughness;
};
uniform Material uMaterial;

uniform sampler2D uTextureSampler;

struct Shadow {
    mat4 lightVPMatrix;
    sampler2D shadowMap;
};
uniform Shadow uShadow;

varying vec3 vPosition;
varying vec3 vNormal;
varying vec2 vTextureCoord;

vec4 calAmbient() {
    return uMaterial.ambient * uLight.ambient;
}
vec4 calDiffusion() {
    vec3 normalizedLightDirection = normalize(uLight.position.xyz - vPosition);
    float factor = max(0.0, dot(vNormal, normalizedLightDirection));
    return uMaterial.diffusion * factor * uLight.diffusion;
}
vec4 calSpecular() {
    vec3 normalizeLightDirection = normalize(uLight.position.xyz - vPosition);
    vec3 normalizedEyeDirection = normalize(uEyePosition - vPosition);
    vec3 halfVector = normalize(normalizeLightDirection + normalizedEyeDirection);
    float factor = dot(vNormal, halfVector);
    if (factor <= 0.0) {
        factor = 0.0;
    } else {
        pow(factor, uMaterial.roughness);
    }
    return uMaterial.specular * factor * uLight.specular;
}
bool isCovered() {
    vec4 positionInLightView = uShadow.lightVPMatrix * vec4(vPosition, 1.0);
    // normal form the positionInLightView.
    positionInLightView = positionInLightView / positionInLightView.w;
    // view coord is in range [-1, 1], we need to map to texture's coord [0. 1]
    //   => (view coord) * 0.5 + 0.5 = texture coord
    vec3 shadowMapCoord = positionInLightView.xyz * 0.5 + vec3(0.5);
    vec4 textureColor = texture2D(uShadow.shadowMap, shadowMapCoord.xy);
    if (shadowMapCoord.z >= textureColor.z) {
        return true;
    } else {
        return false;
    }
}
void main() {
    vec4 textureColor = texture2D(uTextureSampler, vTextureCoord);
    vec4 lightColor = calAmbient();
    if (dot(vNormal, uLight.position.xyz - vPosition) > 0.0) {
        // Check if Light and Triangle are in the same side.
        if (!isCovered()) {
            // only add diffusion and specular when not covered.
            lightColor = lightColor + calDiffusion() + calSpecular();
        }
    }
    vec4 finalColor = textureColor * lightColor;
    if (finalColor.a == 0.0) {
        discard;
    } else {
        gl_FragColor = finalColor;
    }
}
