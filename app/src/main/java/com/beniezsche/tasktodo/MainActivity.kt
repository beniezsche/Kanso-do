package com.beniezsche.tasktodo

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beniezsche.tasktodo.data.User
import com.beniezsche.tasktodo.ui.theme.TasktodoTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            // Initialize Firebase Auth
            auth = Firebase.auth
            val user = Firebase.auth.currentUser
            if (user != null) {
                startActivity(Intent(this, UserListActivity::class.java))
                finish()
            }

            TasktodoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    var isAlreadyAUser by remember { mutableStateOf(true) }

                    if(isAlreadyAUser) {
                        LoginScreen(
                            { email, password ->

                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(this) { task ->
                                        if (task.isSuccessful) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "signInWithEmail:success")
                                            val user = auth.currentUser
                                            startActivity(Intent(this, UserListActivity::class.java))
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                                            Toast.makeText(
                                                baseContext,
                                                "Authentication failed. because: " + task.exception?.message ,
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                    }
                            },
                            {
                                isAlreadyAUser = false
                            }
                        )
                    }
                    else {
                        SignUpScreen (
                            { user, password ->

                                auth.createUserWithEmailAndPassword(user.email, password)
                                    .addOnCompleteListener(this) { task ->
                                        if (task.isSuccessful) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "createUserWithEmail:success")
                                            val firebaseUser = auth.currentUser

                                            user.userId = firebaseUser?.uid!!

                                            pushUserDetailsToDatabase(this, firebaseUser.uid, user)

                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                            Toast.makeText(
                                                this,
                                                "Authentication failed because: " + task.exception?.message,
                                                Toast.LENGTH_SHORT,
                                            ).show()

                                        }
                                    }
                            },
                            {
                                isAlreadyAUser = true
                            })
                    }
                }
            }
        }
    }
}


fun pushUserDetailsToDatabase(context: Context, userId: String, user: User) {

    // Write a message to the database
    val firebaseDatabase = FirebaseDatabase.getInstance("https://tasktodo-7418e-default-rtdb.asia-southeast1.firebasedatabase.app")//.database("https://tasktodo-7418e-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val databaseReference = firebaseDatabase.reference

    databaseReference.child("users").child(userId).setValue(user).addOnSuccessListener {
        context.startActivity(Intent(context, UserListActivity::class.java))
    }

}

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun SignUpScreen(
    onSignUpClicked: (user: User, password: String) -> Unit,
    onSwitchToLogInAction: () -> Unit ) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember {mutableStateOf("")}
    var name by remember {mutableStateOf("")}
    var date by remember {mutableStateOf("03-08-1998")}

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Sign Up",
            style = MaterialTheme.typography.h2,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier
                .fillMaxWidth()

        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            isError = confirmPassword != password,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        Text(
            text = "Date Of Birth",
            color = Color.Gray,
            modifier = Modifier
                .padding(top = 16.dp)
            )

        Spacer(modifier = Modifier.size(5.dp))

        Text(
            text = date,
            fontSize = 18.sp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(5.dp)
                )
                .padding(16.dp)
                .clickable {

                    val mCalendar = Calendar.getInstance()

                    val year: Int = mCalendar.get(Calendar.YEAR)
                    val month: Int = mCalendar.get(Calendar.MONTH)
                    val dayOfMonth: Int = mCalendar.get(Calendar.DAY_OF_MONTH)

                    val datePickerDialog = DatePickerDialog(
                        context,
                        { p0, year, month, dayOfMonth ->

                            val calendar = Calendar.getInstance()
                            calendar[Calendar.YEAR] = year
                            calendar[Calendar.MONTH] = month
                            calendar[Calendar.DAY_OF_MONTH] = dayOfMonth


                            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
                            date = simpleDateFormat.format(calendar.time)

                        }, year, month, dayOfMonth
                    )

                    datePickerDialog.datePicker.maxDate = mCalendar.timeInMillis
                    datePickerDialog.show()
                }
        )

        Button(
            onClick = {

                val user = User()
                user.name = name
                user.email = email
                user.dateOfBirth = date

                if(user.email.isEmpty()) {
                    Toast.makeText(context, "Please provide a proper email", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if(password.isEmpty()) {
                    Toast.makeText(context, "Please provide a proper password", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if(confirmPassword != password) {
                    Toast.makeText(
                        context,
                        "Please make sure you enter the password correctly both times",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                if(name.isEmpty()) {
                    Toast.makeText(
                        context,
                        "Please provide a name",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                onSignUpClicked(user, password)
          },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
        ) {
            Text(text = "Sign Up")
        }
        Text(text = "Already a user? Log in",
            modifier = Modifier
                .clickable(enabled = true) {
                    onSwitchToLogInAction()
                }
        )


    }
}


@Composable
fun LoginScreen(
    onLoginClicked: (email: String, password: String) -> Unit,
    onSwitchToSignUpAction: () -> Unit ) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.h2,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier
                .fillMaxWidth()

        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
        Button(
            onClick = { onLoginClicked(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
        ) {
            Text(text = "Login")
        }
        Text(text = "Not a user? Sign up",
            modifier = Modifier
                .clickable(enabled = true) {
                    onSwitchToSignUpAction()
                }
        )
    }
}