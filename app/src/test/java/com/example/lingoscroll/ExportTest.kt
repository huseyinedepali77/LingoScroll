package com.example.lingoscroll

import com.example.lingoscroll.data.LearningContent
import org.junit.Test
import java.io.File

class ExportTest {

    private fun escapeJson(text: String): String {
        return text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    @Test
    fun exportQuestionsToJson() {
        val allQuestions = LearningContent.diagnosticQuestions + LearningContent.practiceItems
        val jsonBuilder = StringBuilder()
        jsonBuilder.append("[\n")

        for (i in allQuestions.indices) {
            val item = allQuestions[i]
            jsonBuilder.append("  {\n")
            jsonBuilder.append("    \"id\": ${item.id},\n")
            jsonBuilder.append("    \"type\": \"${item.type.name}\",\n")
            jsonBuilder.append("    \"level\": \"${item.level.name}\",\n")
            jsonBuilder.append("    \"phrase\": \"${escapeJson(item.phrase)}\",\n")
            jsonBuilder.append("    \"translation\": \"${escapeJson(item.translation)}\",\n")
            jsonBuilder.append("    \"context\": \"${escapeJson(item.context)}\",\n")
            
            val opts = item.options.joinToString("\", \"") { escapeJson(it) }
            jsonBuilder.append("    \"options\": [${if (opts.isNotEmpty()) "\"$opts\"" else ""}],\n")
            jsonBuilder.append("    \"correctAnswer\": \"${escapeJson(item.correctAnswer)}\",\n")
            jsonBuilder.append("    \"category\": \"${item.category}\",\n")
            
            val vars = item.variations.joinToString("\", \"") { escapeJson(it) }
            jsonBuilder.append("    \"variations\": [${if (vars.isNotEmpty()) "\"$vars\"" else ""}]\n")
            
            if (i < allQuestions.size - 1) {
                jsonBuilder.append("  },\n")
            } else {
                jsonBuilder.append("  }\n")
            }
        }
        jsonBuilder.append("]\n")

        val assetsDir = File("app/src/main/assets")
        if (!assetsDir.exists()) {
            assetsDir.mkdirs()
        }
        val outputFile = File(assetsDir, "offline_questions.json")
        outputFile.writeText(jsonBuilder.toString())
        println("Exported ${allQuestions.size} questions successfully to ${outputFile.absolutePath}")
    }
}
