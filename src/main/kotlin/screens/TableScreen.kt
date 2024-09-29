package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.gson.Gson
import com.google.gson.JsonParser

data class TableScreen(private val jsonInput: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val gson = Gson()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .defaultMinSize(500.dp, 500.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(1f)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = {
                    navigator.pop()
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                }

                Text(
                    text = "Tabular Representation",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }

            DrawTable(
                modifier = Modifier
                    .background(color = Color.White)
                    .fillMaxWidth(),
                parsedJson = parseJson(jsonInput, gson)
            )
        }
    }

    @Composable
    private fun DrawTable(
        modifier: Modifier,
        parsedJson: List<Map<String, Any>>?
    ) {
        val searchText = remember { mutableStateOf(TextFieldValue("")) }
        val showSearchField = remember { mutableStateOf(false) }

        // Capture key events
        Column(modifier = modifier
            .onPreviewKeyEvent {
                if (it.key == Key.F && it.isCtrlPressed) {
                    showSearchField.value = true
                    true
                } else {
                    false
                }
            }) {

            // Display search bar when Ctrl + F is pressed
            if (showSearchField.value) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    TextField(
                        value = searchText.value,
                        onValueChange = { searchText.value = it },
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFEEEEEE))
                            .padding(8.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            showSearchField.value = false
                            searchText.value = TextFieldValue("")
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "close")
                    }
                }
            }

            if (parsedJson == null) {
                Text("JSON Parsing Error", modifier = Modifier.padding(6.dp))
                return@Column
            }

            val keys = parsedJson.first().keys.toList()

            // Header
            Row(modifier = Modifier.fillMaxWidth()) {
                keys.forEach { key ->
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = key,
                        modifier = Modifier
                            .background(color = Color(0xff1d9c11))
                            .weight(1f)
                            .padding(8.dp),
                        fontSize = 12.sp,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }

            // Table Content with Selection and Highlighting
            SelectionContainer {
                Column(
                    modifier = Modifier.fillMaxWidth(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    parsedJson.forEachIndexed { index, row ->
                        val rowColor = if (index % 2 == 0) Color.LightGray else Color.Gray
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            keys.forEach { key ->
                                val cellText = row[key]?.toString() ?: ""
                                val highlightedText = highlightSearchTerm(
                                    text = cellText,
                                    searchTerm = searchText.value.text
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = highlightedText,
                                    modifier = Modifier
                                        .background(color = rowColor)
                                        .weight(1f)
                                        .padding(8.dp),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun parseJson(json: String, gson: Gson): List<Map<String, Any>>? {
        return try {
            val jsonArray = JsonParser.parseString(json).asJsonArray
            jsonArray.map { element ->
                gson.fromJson(element, Map::class.java) as Map<String, Any>
            }
        } catch (e: Exception) {
            null // Handle parsing errors
        }
    }

    @Composable
    private fun highlightSearchTerm(text: String, searchTerm: String): AnnotatedString {
        if (searchTerm.isBlank()) return AnnotatedString(text)

        val startIndex = text.indexOf(searchTerm, ignoreCase = true)
        if (startIndex == -1) return AnnotatedString(text)

        val builder = AnnotatedString.Builder()
        var currentIndex = 0

        while (currentIndex < text.length) {
            val foundIndex = text.indexOf(searchTerm, currentIndex, ignoreCase = true)
            if (foundIndex == -1) {
                builder.append(text.substring(currentIndex))
                break
            }

            if (foundIndex > currentIndex) {
                builder.append(text.substring(currentIndex, foundIndex))
            }

            builder.pushStyle(SpanStyle(background = Color.Yellow))
            builder.append(text.substring(foundIndex, foundIndex + searchTerm.length))
            builder.pop()

            currentIndex = foundIndex + searchTerm.length
        }

        return builder.toAnnotatedString()
    }
}
