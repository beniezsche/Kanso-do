package com.beniezsche.tasktodo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.beniezsche.tasktodo.data.Task
import com.beniezsche.tasktodo.data.User
import com.beniezsche.tasktodo.ui.theme.TasktodoTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AddTaskActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val sharedPreferences = applicationContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            var isDarkMode by remember { mutableStateOf(false) }
            isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)

            TasktodoTheme(darkTheme = isDarkMode) {
                Surface (
                    modifier = Modifier.fillMaxSize()
                        ) {
                    AddTask(
                        { Toast.makeText(baseContext, "Task has been added successfully", Toast.LENGTH_SHORT).show() },
                        { Toast.makeText(baseContext, "Task addition failed, please try again.", Toast.LENGTH_SHORT).show() }
                    )
                }
            }
        }
    }
}

@Composable
fun AddTask(
    onTaskAdded: () -> Unit,
    onTaskFailed: () -> Unit
) {

    val context = LocalContext.current

    val lightBlue = Color(0xffd8e6ff)

    var currentText by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {

        Spacer(modifier = Modifier.size(20.dp))

        Text("Write Task",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.size(20.dp))

        TextField(
            placeholder = {
                Text(
                    text = "Fix plumbing",
                    color = Color.LightGray)
            },
            value = currentText,
            onValueChange = { currentText = it },
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.White,
                cursorColor = Color.Black,
                textColor = Color.Black,
                disabledLabelColor = lightBlue,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.size(20.dp))

        Button(
            onClick = {

                if(currentText.trim().isEmpty()) {
                    Toast.makeText(context, "Please add some valid text before adding", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Initialize Firebase Auth
                val firebaseUser = Firebase.auth.currentUser
                if (firebaseUser != null) {
                    val firebaseDatabase = FirebaseDatabase.getInstance("https://tasktodo-7418e-default-rtdb.asia-southeast1.firebasedatabase.app")//.database("https://tasktodo-7418e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    val databaseReference = firebaseDatabase.reference

                    val task = Task()
                    task.userId = firebaseUser.uid
                    task.task = currentText

                    databaseReference.child("tasks").push().setValue(task).addOnSuccessListener {
                        currentText = " "
                        onTaskAdded()
                    }.addOnFailureListener {
                        onTaskFailed()
                    }
                }

            }
        ) {
            Text(
                text = "Add Task",
                color = Color.White,
                modifier = Modifier.padding(5.dp))
        }
    }
}
