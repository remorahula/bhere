package io.getstream.kmp.android.auth

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import io.getstream.kmp.android.AndroidGraph
import io.getstream.kmp.auth.AuthTokens
import kotlinx.coroutines.launch
import net.openid.appauth.*

class AndroidAuthManager(private val context: Context) {

    private val issuerUri = Uri.parse("https://auth.bhere.ee/realms/guard")
    private val clientId = "androidapp"
    private val redirectUri = Uri.parse("io.getstream.kmp.android://oauth2redirect")

    private val authService = AuthorizationService(context)

    fun startLogin(activity: Activity,
                    idpHint: String, // "google" or "apple" (must match Keycloak IdP alias)
                   onError: (Throwable) -> Unit) {
        AuthorizationServiceConfiguration.fetchFromIssuer(issuerUri) { config, ex ->
            if (ex != null) { onError(ex); return@fetchFromIssuer }
            requireNotNull(config)

            val extraParams = mapOf("kc_idp_hint" to idpHint)
            // optional: "prompt" to "login"


            val req = AuthorizationRequest.Builder(
                config,
                clientId,
                ResponseTypeValues.CODE,
                redirectUri
            ).setScope("openid profile email")
                .setAdditionalParameters(extraParams)
                .build()

            val intent = authService.getAuthorizationRequestIntent(req)
            activity.startActivityForResult(intent, RC_AUTH) // (for simplicity; migrate to ActivityResult API later)
        }
    }

    fun handleAuthResponse(data: android.content.Intent?, onDone: () -> Unit, onError: (Throwable) -> Unit) {
        val resp = AuthorizationResponse.fromIntent(data!!)
        val ex = AuthorizationException.fromIntent(data)
        android.util.Log.d("AndroidAuthManager", "handleAuthResponse called. data=$data dataUri=${data?.data}")
        android.util.Log.d("AndroidAuthManager", "auth resp=$resp ex=$ex")


        if (ex != null) { onError(ex); return }
        if (resp == null) { onError(IllegalStateException("No auth response")); return }

        val tokenReq = resp.createTokenExchangeRequest()
        authService.performTokenRequest(tokenReq) { tokenResp, tokenEx ->
            android.util.Log.d("AndroidAuthManager", "tokenResp=$tokenResp tokenEx=$tokenEx")

            if (tokenEx != null) { onError(tokenEx); return@performTokenRequest }
            requireNotNull(tokenResp)
            android.util.Log.d("AndroidAuthManager", "accessToken null? ${tokenResp.accessToken == null}")



            val tokens = AuthTokens(
                accessToken = tokenResp.accessToken.orEmpty(),
                refreshToken = tokenResp.refreshToken,
                expiresAtEpochMs = tokenResp.accessTokenExpirationTime?.toLong()
            )

            Log.d("remopask", "data?.data=$tokens.accessToken}") // the deep link URI if present

            // Save into shared store
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                AndroidGraph.shared.tokenStore.save(tokens)
                onDone()
            }
        }
    }

    companion object { const val RC_AUTH = 9001 }
}