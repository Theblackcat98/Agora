package com.newoether.agora.model.profile

import com.newoether.agora.model.ModelId
import com.newoether.agora.util.Constants

/**
 * Registry resolving canonical model IDs to their authoritative [ModelProfile]
 * and capability descriptor.
 */
object ModelProfileRegistry {

    /**
     * Resolves the [ModelProfile] for [modelId].
     *
     * Handles canonical "Provider:modelName" IDs, unprefixed legacy IDs, and
     * custom provider model references, falling back gracefully to inferred or
     * conservative capability profiles for unknown models.
     */
    fun resolve(modelId: String): ModelProfile {
        val parsed = ModelId.parse(modelId)
        val canonicalKey = "${parsed.providerName}:${parsed.modelName.removePrefix("models/")}"
        
        // Exact built-in lookup
        BuiltInModelProfiles.BUILT_IN_PROFILES[canonicalKey]?.let { return it }

        // Also check with raw modelName if different
        if (parsed.modelName != parsed.modelName.removePrefix("models/")) {
            BuiltInModelProfiles.BUILT_IN_PROFILES["${parsed.providerName}:${parsed.modelName}"]?.let { return it }
        }

        // Fuzzy match on known models within the same provider
        val matchingProfile = BuiltInModelProfiles.BUILT_IN_PROFILES.values.firstOrNull {
            it.providerReference.equals(parsed.providerName, ignoreCase = true) &&
                (it.modelName.equals(parsed.modelName, ignoreCase = true) ||
                    parsed.modelName.contains(it.modelName, ignoreCase = true))
        }
        if (matchingProfile != null) {
            return matchingProfile.copy(
                canonicalId = parsed.prefixed,
                providerReference = parsed.providerName,
                modelName = parsed.modelName,
            )
        }

        // Safe fallback
        return BuiltInModelProfiles.createFallbackProfile(
            canonicalId = parsed.prefixed,
            providerReference = parsed.providerName,
            modelName = parsed.modelName,
        )
    }
}
