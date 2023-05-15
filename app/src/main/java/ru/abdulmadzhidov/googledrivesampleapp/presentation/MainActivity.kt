package ru.abdulmadzhidov.googledrivesampleapp.presentation

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.AndroidEntryPoint
import ru.abdulmadzhidov.googledrivesampleapp.ui.theme.GoogleDriveTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var client: GoogleSignInClient
    private val mainViewModel: MainViewModel by viewModels()

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .addOnSuccessListener {
                        mainViewModel.getFilesList(this@MainActivity)
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                    }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initGoogleSignin()
        setContent {
            GoogleDriveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        mainViewModel.mainScreenState.observeAsState().value?.let {
                            if (it.isAuthed) {
                                MainScreen(
                                    onLogoutClicked = ::logoutClicked,
                                    uploadNewFile = { mainViewModel.uploadNewFile(this@MainActivity) },
                                    openFile = { mainViewModel.openFile(it, this@MainActivity) },
                                    files = it.files,
                                    fileContent = it.currentFileContent
                                )
                            } else {
                                AuthScreen(
                                    onSignInClicked = ::googleLogin
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initGoogleSignin() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken()
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()

        client = GoogleSignIn.getClient(this@MainActivity, signInOptions)
        mainViewModel.checkAuthStatus(this)
    }

    private fun googleLogin() {
        startForResult.launch(client.signInIntent)
    }

    private fun logoutClicked() {
        client.signOut().addOnSuccessListener {
            mainViewModel.onSuccessSignout()
        }
    }
}

