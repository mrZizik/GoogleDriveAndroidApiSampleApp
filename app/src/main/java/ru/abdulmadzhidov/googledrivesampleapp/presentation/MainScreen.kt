package ru.abdulmadzhidov.googledrivesampleapp.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.abdulmadzhidov.googledrivesampleapp.ui.theme.GoogleDriveTheme

@Composable
fun AuthScreen(
    onSignInClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Button(onClick = onSignInClicked) {
            Text(text = "Sign In")
        }
    }
}

@Composable
fun MainScreen(
    onLogoutClicked: () -> Unit,
    uploadNewFile: () -> Unit,
    openFile: (String) -> Unit,
    files: List<LocalFile>,
    modifier: Modifier = Modifier,
    fileContent: String
) {
    Column(modifier) {
        Button(onClick = onLogoutClicked) {
            Text(text = "Log Out")
        }
        Button(onClick = uploadNewFile) {
            Text(text = "Upload")
        }

        if (fileContent.isNotEmpty()) {
            Text(text = "File content:")
            Text(
                fileContent
            )
        }

        if (files.isNotEmpty()) {
            Text(text = "Files:")
            files.forEach {
                Button(onClick = { openFile(it.id) }) {
                    Text(text = it.name)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GoogleDriveTheme {
        MainScreen({}, {}, {}, emptyList(), fileContent = "")
    }
}