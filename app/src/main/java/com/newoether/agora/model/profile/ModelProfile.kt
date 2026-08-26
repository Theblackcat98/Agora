package com.newoether.agora.model.profile

import com.newoether.agora.model.ModelId
import kotlinx.serialization.Serializable

/**
 * Metadata profile for an LLM model, defining its canonical identity, display name,
 * and authoritative capability descriptor.
 */
@Serializable
data class ModelProfile(
    val canonicalId: String,
    val providerReference: String,
    val modelName: String,
    val displayName: String,
    val capabilities: ModelCapabilities,
    val defaultContextWindow: Int = capabilities.maxContextTokens,
    val defaultMaxOutputTokens: Int? = capabilities.maxOutputTokens,
) {
    val modelId: ModelId get() = ModelId(providerReference, modelName)
}
