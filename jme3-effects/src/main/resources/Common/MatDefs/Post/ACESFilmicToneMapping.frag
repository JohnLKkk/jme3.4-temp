#extension GL_ARB_texture_multisample : enable
#import "Common/ShaderLib/GLSLCompat.glsllib"

uniform float m_Exposure;

varying vec2 texCoord;

// ACES Filmic ToneMapping

vec3 RRTAndODTFit( vec3 v ) {

    vec3 a = v * ( v + 0.0245786 ) - 0.000090537;
    vec3 b = v * ( 0.983729 * v + 0.4329510 ) + 0.238081;
    return a / b;

}

vec3 ACESFilmicToneMapping( vec3 color ) {

    // sRGB => XYZ => D65_2_D60 => AP1 => RRT_SAT
    const mat3 ACESInputMat = mat3(
    vec3( 0.59719, 0.07600, 0.02840 ), // transposed from source
    vec3( 0.35458, 0.90834, 0.13383 ),
    vec3( 0.04823, 0.01566, 0.83777 )
    );

    // ODT_SAT => XYZ => D60_2_D65 => sRGB
    const mat3 ACESOutputMat = mat3(
    vec3(  1.60475, -0.10208, -0.00327 ), // transposed from source
    vec3( -0.53108,  1.10813, -0.07276 ),
    vec3( -0.07367, -0.00605,  1.07602 )
    );

    color = ACESInputMat * color;

    // Apply RRT and ODT
    color = RRTAndODTFit( color );

    color = ACESOutputMat * color;

    // Clamp to [0, 1]
    return clamp( color, 0, 1 );

}

#ifdef NUM_SAMPLES

uniform sampler2DMS m_Texture;

vec4 ToneMap_TextureACESFilmic() {
    float exp = m_Exposure / 0.6f;
    ivec2 iTexC = ivec2(texCoord * vec2(textureSize(m_Texture)));
    vec4 color = vec4(0.0);
    for (int i = 0; i < NUM_SAMPLES; i++) {
        vec4 hdrColor = texelFetch(m_Texture, iTexC, i);
        vec3 ldrColor = ACESFilmicToneMapping(hdrColor.rgb);
        color += vec4(ldrColor, hdrColor.a);
    }
    color.rgb /= ACESFilmicToneMapping(vec3(exp));
    return color / float(NUM_SAMPLES);
}

#else

uniform sampler2D m_Texture;

vec4 ToneMap_TextureACESFilmic() {
    float exp = m_Exposure / 0.6f;
    vec4 texVal = texture2D(m_Texture, texCoord);
    return vec4(ACESFilmicToneMapping(texVal.rgb * exp) * exp, texVal.a);
}

#endif

void main() {
    gl_FragColor = ToneMap_TextureACESFilmic();
}