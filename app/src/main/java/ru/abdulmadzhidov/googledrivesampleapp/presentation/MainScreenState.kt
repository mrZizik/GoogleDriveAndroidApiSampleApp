package ru.abdulmadzhidov.googledrivesampleapp.presentation

data class MainScreenState(
    val isAuthed: Boolean = false,
    val files: List<LocalFile> = emptyList(),
    val currentFileContent: String = ""
)

data class LocalFile(
    val name: String,
    val id: String
)
