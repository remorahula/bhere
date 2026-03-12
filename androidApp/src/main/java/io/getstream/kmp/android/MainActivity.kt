package io.getstream.kmp.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.kmp.Main
import io.getstream.kmp.android.auth.AndroidAuthManager

class MainActivity : ComponentActivity() {

    private lateinit var auth: AndroidAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = AndroidAuthManager(this)

        setContent {
            MaterialTheme {
                AppRoot(
                    onGoogleLogin = { auth.startLogin(this, idpHint = "google", onError = { /* TODO */ }) },
                    onAppleLogin  = { auth.startLogin(this, idpHint = "apple",  onError = { /* TODO */ }) }

                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("MainActivity", "onActivityResult called. requestCode=$requestCode resultCode=$resultCode data=$data")
        Log.d("MainActivity", "data?.data=${data?.data}") // the deep link URI if present
        Log.d("MainActivity", "data?.extras=${data?.extras}")
        if (requestCode == AndroidAuthManager.RC_AUTH) {
            auth.handleAuthResponse(
                data = data,
                onDone = { /* UI reads token existence */ },
                onError = { /* TODO */ }
            )
        }
    }
}