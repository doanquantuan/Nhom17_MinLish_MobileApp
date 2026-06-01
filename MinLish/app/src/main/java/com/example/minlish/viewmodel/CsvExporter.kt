package com.example.minlish.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.minlish.data.model.Vocabulary
import java.io.File

object CsvExporter {

    fun exportVocabulary(
        context: Context,
        fileName: String,
        vocabularies: List<Vocabulary>
    ): Uri? {

        val file = File(
            context.getExternalFilesDir(null),
            "$fileName.csv"
        )

        file.bufferedWriter().use { writer ->
            // Header: Word, Meaning, WordType, Pronunciation, Example, Collocation, Note
            writer.appendLine("Word,Meaning,WordType,Pronunciation,Example,Collocation,Note")

            vocabularies.forEach { vocab ->
                val line = listOf(
                    vocab.word,
                    vocab.meaning,
                    vocab.wordType,
                    vocab.pronunciation,
                    vocab.example,
                    vocab.collocation,
                    vocab.note
                ).joinToString(",") { 
                    "\"${it.replace("\"", "\"\"")}\"" 
                }
                writer.appendLine(line)
            }
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}
