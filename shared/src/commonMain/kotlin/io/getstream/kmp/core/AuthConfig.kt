package io.getstream.kmp.core

object AuthConfig {
    // Must match your AndroidAuthManager issuerUri
    const val ISSUER = "https://auth.bhere.ee/realms/guard"
    const val CLIENT_ID = "androidapp"

    // Keycloak standard token endpoint for a realm issuer
    const val TOKEN_ENDPOINT = "$ISSUER/protocol/openid-connect/token"
}