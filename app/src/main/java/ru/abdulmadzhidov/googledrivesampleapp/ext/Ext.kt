package ru.abdulmadzhidov.googledrivesampleapp

import java.io.BufferedReader
import java.io.InputStream

fun InputStream.readToString(): String {
    val reader = BufferedReader(this.reader())
    val content = StringBuilder()
    reader.use { reader ->
        var line = reader.readLine()
        while (line != null) {
            content.append(line)
            line = reader.readLine()
        }
    }

    return content.toString()
}