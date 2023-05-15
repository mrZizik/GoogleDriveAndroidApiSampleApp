package ru.abdulmadzhidov.googledrivesampleapp.presentation

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.abdulmadzhidov.googledrivesampleapp.readToString
import java.util.Collections
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _mainScreenState = MutableLiveData(MainScreenState())
    val mainScreenState: LiveData<MainScreenState> = _mainScreenState

    fun checkAuthStatus(context: Activity) {
        val lastSigned = GoogleSignIn.getLastSignedInAccount(context)
        val isAuthed = lastSigned != null && !lastSigned.isExpired

        _mainScreenState.value = MainScreenState(
            isAuthed = lastSigned != null && !lastSigned.isExpired
        )

        if (isAuthed) {
            viewModelScope.launch {
                getFilesList(context)
            }
        }
    }

    fun getFilesList(context: Activity) {
        viewModelScope.async(Dispatchers.IO) {
            val credential: GoogleAccountCredential =
                GoogleAccountCredential.usingOAuth2(
                    context,
                    Collections.singleton(DriveScopes.DRIVE_FILE)
                )

            credential.selectedAccount = GoogleSignIn.getLastSignedInAccount(context)?.account

            val googleDriveService: Drive = Drive.Builder(
                NetHttpTransport.Builder().build(),
                GsonFactory(),
                credential
            )
                .setApplicationName("Samples")
                .build()

            val files = googleDriveService.Files().list().execute().files.map {
                LocalFile(
                    name = it.name,
                    id = it.id
                )
            }

            launch(Dispatchers.Main) {
                _mainScreenState.value = _mainScreenState.value?.copy(isAuthed = true, files = files)
            }
        }
    }

    fun openFile(id: String, context: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            val credential: GoogleAccountCredential =
                GoogleAccountCredential.usingOAuth2(
                    context,
                    Collections.singleton(DriveScopes.DRIVE_FILE)
                )

            credential.selectedAccount = GoogleSignIn.getLastSignedInAccount(context)?.account

            val googleDriveService: Drive = Drive.Builder(
                NetHttpTransport.Builder().build(),
                GsonFactory(),
                credential
            )
                .setApplicationName("Samples")
                .build()

            val file = googleDriveService.Files().get(id)?.executeMediaAsInputStream()
            launch(Dispatchers.Main) {
                _mainScreenState.value =
                    _mainScreenState.value?.copy(currentFileContent = file?.readToString() ?: "")
            }
        }
    }

    fun onSuccessSignout() {
        _mainScreenState.value = _mainScreenState.value?.copy(isAuthed = false)
    }

    fun uploadNewFile(context: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            val credential: GoogleAccountCredential =
                GoogleAccountCredential.usingOAuth2(
                    context,
                    Collections.singleton(DriveScopes.DRIVE_FILE)
                )

            credential.selectedAccount = GoogleSignIn.getLastSignedInAccount(context)?.account

            val googleDriveService: Drive = Drive.Builder(
                NetHttpTransport.Builder().build(),
                GsonFactory(),
                credential
            )
                .setApplicationName("Samples")
                .build()

            val name = "testFile" + Random(100).nextInt()

            val metadata: File = File()
                .setParents(Collections.singletonList("root"))
                .setMimeType("text/plain")
                .setName(name)

            java.io.File(context.cacheDir.absolutePath, name).writeText("some random mnemonic here" + Random(100).nextInt())

            val mediaContent = FileContent("text/plain", java.io.File(context.cacheDir.absolutePath + "/" + name))

            val googleFile: File =
                googleDriveService.files().create(metadata, mediaContent)
                    .setFields("id")
                    .execute()

            getFilesList(context)
        }
    }
}