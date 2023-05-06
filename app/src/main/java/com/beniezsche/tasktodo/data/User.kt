package com.beniezsche.tasktodo.data

import java.text.SimpleDateFormat
import java.util.*

class User {
    var userId: String = " "
    var name: String = " "
    var email: String = " "
    var dateOfBirth = "01-01-1970"

    fun getAge() : Int {

        val currentCalendar = Calendar.getInstance()
        val dobCalendar = Calendar.getInstance()

        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")

        dobCalendar.time = simpleDateFormat.parse(dateOfBirth) as Date

        val diff = currentCalendar.timeInMillis - dobCalendar.timeInMillis

        val age = diff / 31556952000

        return age.toInt()
    }
}