package com.beniezsche.tasktodo

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beniezsche.tasktodo.data.Task
import com.beniezsche.tasktodo.data.User
import com.beniezsche.tasktodo.ui.theme.Dark1000
import com.beniezsche.tasktodo.ui.theme.TasktodoTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class TaskListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = intent.getStringExtra("userId")
        val userName = intent.getStringExtra("userName")

        setContent {
            val sharedPreferences = applicationContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            var isDarkMode by remember { mutableStateOf(false) }
            isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)

            TasktodoTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TaskListLogic(userId = userId!!, userName = userName!!)
                }
            }
        }
    }
}

@Composable
fun TaskListLogic(userId: String, userName: String) {

    var taskList = remember { mutableStateListOf<Task>() }
    var canEditTasks by remember { mutableStateOf(false) }

    var userIdQuery by remember { mutableStateOf(" ") }

    userIdQuery = userId!!

    val firebaseDatabase = FirebaseDatabase.getInstance("https://tasktodo-7418e-default-rtdb.asia-southeast1.firebasedatabase.app")//.database("https://tasktodo-7418e-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val databaseReference = firebaseDatabase.reference

    val query = databaseReference.child("tasks").orderByChild("userId").equalTo(userIdQuery)

    if (Firebase.auth.currentUser?.uid == userIdQuery) {
        canEditTasks = true
    }

    query.addListenerForSingleValueEvent(object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot) {

            taskList.clear()

            for (taskSnapshot in dataSnapshot.children) {

                val task = taskSnapshot.getValue(Task::class.java)
                task?.taskId = taskSnapshot.key.toString()

                taskList.add(task!!)

            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // handle error
        }
    })

    Column (modifier = Modifier.padding(start = 10.dp, end = 10.dp)){

        Spacer(modifier = Modifier.size(20.dp))

        Text(text = "Tasks for $userName",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.size(20.dp))

        if (!taskList.isEmpty()) {

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Add a single item
                for(task in taskList) {
                    item {
                        TaskRow(task = task, canEditTasks = canEditTasks)
                    }
                }
            }

        }
    }

}

@Composable
fun TaskList(
    list: List<Task>,
    canEditTasks: Boolean) {

    Column (modifier = Modifier.padding(start = 10.dp, end = 10.dp)){

        Spacer(modifier = Modifier.size(20.dp))

        Text(text = "Tasks",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.size(20.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Add a single item
            for(task in list) {
                item {
                    TaskRow(task = task, canEditTasks = canEditTasks)
                }
            }
        }
    }


}

@Composable
fun TaskRow(task: Task, canEditTasks: Boolean) {

    var checked by remember { mutableStateOf(false) }

    checked = task.isDone

    Row(verticalAlignment = Alignment.CenterVertically) {

        Checkbox(
            checked = task.isDone,
            enabled = canEditTasks,
            modifier = Modifier.weight(1.0f),
            onCheckedChange = {

//                if (canEditTasks) {
//                    val firebaseDatabase = FirebaseDatabase.getInstance("https://tasktodo-7418e-default-rtdb.asia-southeast1.firebasedatabase.app")//.database("https://tasktodo-7418e-default-rtdb.asia-southeast1.firebasedatabase.app/")
//                    val databaseReference = firebaseDatabase.reference
//
//                    databaseReference.child("tasks").child(task.taskId).child("done").setValue(it)
//                }

                val firebaseDatabase = FirebaseDatabase.getInstance("https://tasktodo-7418e-default-rtdb.asia-southeast1.firebasedatabase.app")//.database("https://tasktodo-7418e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                val databaseReference = firebaseDatabase.reference

                databaseReference.child("tasks").child(task.taskId).child("done").setValue(it)

            }
        )
        Text(
            text = task.task,
            fontSize = 16.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(5.0f),
        )

        if (canEditTasks) {
            Icon(
                imageVector = Icons.Filled.Clear,
                contentDescription = "delete",
                modifier = Modifier
                    .weight(1.0f)
                    .clickable {

                        val firebaseDatabase =
                            FirebaseDatabase.getInstance("https://tasktodo-7418e-default-rtdb.asia-southeast1.firebasedatabase.app")//.database("https://tasktodo-7418e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        val databaseReference = firebaseDatabase.reference

                        databaseReference
                            .child("tasks")
                            .child(task.taskId)
                            .removeValue()
                    }
            )
        }


    }
}

