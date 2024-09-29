package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

object InputScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val input = rememberSaveable { mutableStateOf("") }

        Column(
            modifier = Modifier.fillMaxSize(1f)
                .padding(16.dp)
        ) {
            Text("Paste your json here", fontSize = 12.sp, color = Color.Gray)
            TextField(
                value = input.value,
                onValueChange = { input.value = it },
                modifier = Modifier
                    .background(color = Color.White)
                    .fillMaxWidth(1f)
                    .height(600.dp)
                    .padding(10.dp)
                    .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
            )
            Row(
                modifier = Modifier.fillMaxWidth(1f),

                ) {
                Button(
                    onClick = {
                        navigator.push(TableScreen(input.value))
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Blue,
                        contentColor = Color.White,
                    )
                ) {
                    Text("Show Tabular Format", color = Color.White)
                }
                Spacer(modifier = Modifier.width(30.dp))

                Button(
                    onClick = {
                        navigator.push(ModelClassesScreen(input.value))
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Green,
                        contentColor = Color.White,
                    )
                ) {
                    Text("Generate Model Classes", color = Color.White)
                }

                Spacer(modifier = Modifier.width(30.dp))
                Button(
                    onClick = {
                        input.value = "" // reset text field value
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Red,
                        contentColor = Color.White,
                    )
                ) {
                    Text("Clear", color = Color.White)
                }
            }
        }
    }

}