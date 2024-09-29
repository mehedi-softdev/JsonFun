package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

data class ModelClassesScreen(private val jsonInput: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val clipboardManager = LocalClipboardManager.current

        val generatedClasses = remember { mutableStateOf("") }
        val className = remember { mutableStateOf(TextFieldValue("")) }

        Column(
            modifier = Modifier.fillMaxWidth(1f).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    navigator.pop()
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                }
                TextField(
                    value = className.value,
                    onValueChange = { className.value = it },
                    maxLines = 1,
                    singleLine = true,
                    placeholder = { Text("Enter Class Name") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    label = {
                        Text("Enter Class Name")
                    }
                )
                Button(
                    onClick = {
                        generatedClasses.value = try {
                            val jsonElement = JsonParser.parseString(jsonInput)
                            generateKotlinDataClasses(jsonElement, className.value.text.ifBlank { "GeneratedClass" })
                        } catch (e: Exception) {
                            "Invalid JSON format"
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(text = "Load")
                }
            }

            if (generatedClasses.value.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generated Kotlin Classes:", fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
                    SelectionContainer {
                        Column(
                            modifier = Modifier
                                .background(color = Color(0xFF2B2B2B))
                                .fillMaxWidth(1f)
                                .padding(16.dp)
                        ) {
                            displayGeneratedClass(generatedClasses.value)

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    clipboardManager.setText(buildAnnotatedString { append(generatedClasses.value) })
                                },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 8.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.ThumbUp, contentDescription = "Copy")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy")
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun displayGeneratedClass(generatedClass: String) {
        generatedClass.lines().forEach { line ->
            // Change colors based on content of each line
            when {
                line.startsWith("data class") -> {
                    Text(text = line, color = Color(0xFF55B5DB), fontSize = 14.sp) // Class name color
                }
                line.contains("@SerializedName") -> {
                    Text(line, color = Color(0xFF9FCA56), fontSize = 14.sp) // Annotation color
                }
                line.contains("val") -> {
                    Text(line, color = Color(0xFFE6CD69), fontSize = 14.sp) // Variable declaration color
                }
                else -> {
                    Text(line, color = Color.White, fontSize = 14.sp) // Default color
                }
            }
        }
    }

    private fun generateKotlinDataClasses(jsonElement: JsonElement, className: String): String {
        // trim spaces from class name
        val localClassName = className.replace(" ", "")
        val builder = StringBuilder()
        when {
            jsonElement.isJsonObject -> processJsonObject(jsonElement.asJsonObject, localClassName, builder)
            jsonElement.isJsonArray -> {
                val firstElement = jsonElement.asJsonArray.firstOrNull()
                if (firstElement != null && firstElement.isJsonObject) {
                    processJsonObject(firstElement.asJsonObject, localClassName, builder)
                }
            }
        }
        return builder.toString()
    }

    private fun processJsonObject(jsonObject: JsonObject, className: String, builder: StringBuilder) {
        builder.append("data class $className(\n")
        jsonObject.entrySet().forEach { entry ->
            val originalName = entry.key
            val camelCaseName = convertToCamelCase(originalName)
            val fieldType = determineKotlinType(entry.value, camelCaseName.replaceFirstChar(Char::uppercaseChar))
            builder.append("    @SerializedName(\"$originalName\")")
            builder.append(" val $camelCaseName: $fieldType,\n")
        }
        builder.append(")")

        jsonObject.entrySet().forEach { entry ->
            if (entry.value.isJsonObject) {
                processJsonObject(entry.value.asJsonObject, entry.key.replaceFirstChar(Char::uppercaseChar), builder)
            } else if (entry.value.isJsonArray && entry.value.asJsonArray.firstOrNull()?.isJsonObject == true) {
                processJsonObject(
                    entry.value.asJsonArray.first().asJsonObject,
                    entry.key.replaceFirstChar(Char::uppercaseChar),
                    builder
                )
            }
        }
    }

    private fun determineKotlinType(jsonElement: JsonElement, nestedClassName: String): String {
        return when {
            jsonElement.isJsonObject -> nestedClassName
            jsonElement.isJsonArray -> {
                val arrayType = if (jsonElement.asJsonArray.size() > 0) {
                    determineKotlinType(jsonElement.asJsonArray.first(), nestedClassName)
                } else {
                    "Any"
                }
                "List<$arrayType>"
            }

            jsonElement.isJsonPrimitive -> {
                val primitive = jsonElement.asJsonPrimitive
                when {
                    primitive.isBoolean -> "Boolean"
                    primitive.isNumber -> if (primitive.toString().contains(".")) "Double" else "Int"
                    primitive.isString -> "String"
                    else -> "String"
                }
            }

            jsonElement.isJsonNull -> "Any?"
            else -> "Any"
        }
    }

    private fun convertToCamelCase(input: String): String {
        return input.split("_", " ").joinToString("") { it.capitalize() }.replaceFirstChar(Char::lowercaseChar)
    }
}
