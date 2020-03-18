void addEmissive(const MaterialInputs material, inout vec4 color) {
    #if defined(MATERIAL_HAS_EMISSIVE)
    // The emissive property applies independently of the shading model
    // It is defined as a color + exposure compensation
    highp vec4 emissive = material.emissive;
    highp float attenuation = computePreExposedIntensity(
    pow(2.0, frameUniforms.ev100 + emissive.w - 3.0), frameUniforms.exposure);
    color.rgb += emissive.rgb * attenuation;
    #endif
}

/**
 * Evaluates unlit materials. In this lighting model, only the base color and
 * emissive properties are taken into account:
 *
 * finalColor = baseColor + emissive
 *
 * The emissive factor is only applied if the fragment passes the optional
 * alpha test.
 *
 * When the shadowMultiplier property is enabled on the material, the final
 * color is multiplied by the inverse light visibility to apply a shadow.
 * This is mostly useful in AR to cast shadows on unlit transparent shadow
 * receiving planes.
 */
vec4 evaluateMaterial(const MaterialInputs material) {
    vec4 color = material.baseColor;

#if defined(BLEND_MODE_MASKED)
    if (color.a < getMaskThreshold()) {
        discard;
    }
#endif

    addEmissive(material, color);

#if defined(HAS_DIRECTIONAL_LIGHTING)
#if defined(HAS_SHADOWING)
    float s = 1.0f;
    /*
    if (frameUniforms.directionalShadows) {
        s *= shadow(light_shadowMap, 0u, getLightSpacePosition());
    }
     */
#if defined(HAS_DYNAMIC_LIGHTING)
    for (uint l = 0u; l < uint(MAX_SHADOW_CASTING_SPOTS); l++) {
        uint shadowBits = shadowUniforms.shadow[l].x;
        bool castsShadows = bool(shadowBits & 0x1u);
        uint shadowIndex = ((shadowBits & 0x01Eu) >> 1u);
        uint shadowLayer = ((shadowBits & 0x1E0u) >> 5u);

        if (castsShadows) {
            s *= shadow(light_shadowMap, shadowLayer, getSpotLightSpacePosition(l));
        }
    }
#endif
    color *= 1.0 - s;
#else
    color = vec4(0.0);
#endif
#elif defined(HAS_SHADOW_MULTIPLIER)
    color = vec4(0.0);
#endif

    return color;
}
