package com.beniezsche.tasktodo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beniezsche.tasktodo.data.User
import com.beniezsche.tasktodo.ui.theme.Purple200
import com.beniezsche.tasktodo.ui.theme.Purple400
import com.beniezsche.tasktodo.ui.theme.TasktodoTheme
import com.beniezsche.tasktodo.ui.theme.Typography
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.time.format.TextStyle
import java.util.Collections

class UserListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val sharedPreferences = applicationContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            var isDarkMode by remember { mutableStateOf(false) }
            isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)

            TasktodoTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    var userList by remember { mutableStateOf(listOf<User>()) }

                    val firebaseDatabase = FirebaseDatabase.getInstance("https://tasktodo-7418e-default-rtdb.asia-southeast1.firebasedatabase.app")//.database("https://tasktodo-7418e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    val databaseReference = firebaseDatabase.reference

                    databaseReference.child("users").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            val listRes: MutableList<User> = ArrayList()

                            for (dataValues in snapshot.children) {
                                val user: User = dataValues.getValue(User::class.java)!!
                                listRes.add(user)
                            }

                            userList = listRes
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@UserListActivity, error.message, Toast.LENGTH_SHORT).show()
                        }

                    })

                    if (userList.isNotEmpty())
                        UserListScreen (
                            list = bringCurrentUserToTop(userList, Firebase.auth.currentUser!!.uid)
                        ) {
                            sharedPreferences.edit().putBoolean("isDarkMode", it).commit()
                            isDarkMode = it
                        }
                }
            }
        }
    }
}

fun bringCurrentUserToTop(list: List<User>, currentUserId: String) : List<User> {

    var currentUserIndex = 0

    for ((index, item) in list.withIndex()) {
        if (item.userId == currentUserId) {
            currentUserIndex = index
            break
        }
    }

    Collections.swap(list, 0, currentUserIndex)

    return list

}

@Composable
fun UserListScreen(
    list: List<User>,
    onDarkModeChanged: (isDarkMode: Boolean) -> Unit
) {

    val context = LocalContext.current

    val sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    var isDarkModeOn by remember { mutableStateOf(false) }
    isDarkModeOn = sharedPreferences.getBoolean("isDarkMode", false)


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 10.dp)) {

        Spacer(modifier = Modifier.size(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(text = "Users",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.5f))

            Text(
                text = "Dark Mode: ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.weight(0.3f)
            )

            Switch(
                checked = isDarkModeOn,
                onCheckedChange = {
                    isDarkModeOn = it
                    onDarkModeChanged(isDarkModeOn)
                  },
                modifier = Modifier.weight(0.2f))

        }


        Spacer(modifier = Modifier.size(20.dp))

        UserDetailsRow(user = list[0], backgroundColor = Purple200, textColor = Color.White)
        
        Spacer(modifier = Modifier.size(20.dp))
        
        LazyColumn(modifier = Modifier.fillMaxSize()){

            for(user in list.subList(1, list.size)) {
                item {
                    UserDetailsRow(user = user)
                }
            }
        }
    }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = {
                context.startActivity(Intent(context, AddTaskActivity::class.java))
            }
            ,modifier = Modifier
                .size(size = 50.dp)
                .offset(0.dp, (-25).dp)) {

            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Task", tint = Color.White)

        }
    }
}

@Composable
fun UserDetailsRow(user: User, backgroundColor: Color = Color.White, textColor: Color = Color.Black) {

    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(10.dp),
        backgroundColor = backgroundColor,
        modifier = Modifier
            .padding(bottom = 5.dp, top = 5.dp)
            .border(1.dp, backgroundColor, RoundedCornerShape(10.dp))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .clickable {
                    val intent = Intent(context, TaskListActivity::class.java)
                    intent.putExtra("userId", user.userId)
                    intent.putExtra("userName", user.name)
                    context.startActivity(intent)
                }) {

            Column(modifier = Modifier
            ) {
                Text(
                    text = user.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text =  "Email: " + user.email,
                    color = textColor,
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Light,
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp
                    ),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.size(4.dp))

                Text(
                    text =  "DOB: " + user.dateOfBirth,
                    color = textColor,
                    style = Typography.body2,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.size(4.dp))

                Text(
                    text =  "Age: " + user.getAge().toString(),
                    color = textColor,
                    style = Typography.body2,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }


}
