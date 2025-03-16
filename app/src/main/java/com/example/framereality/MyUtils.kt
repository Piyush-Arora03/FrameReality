package com.example.framereality

import android.content.Context
import android.icu.text.DateFormat
import java.util.Calendar
import android.os.Message
import android.widget.Toast
import com.google.firebase.Timestamp

object MyUtils {

    const val AD_STATUS_AVAILABLE = "AVAILABLE"
    const val AD_STATUS_SOLD = "SOLD"
    const val AD_STATUS_RENTED = "RENTED"

    const val USER_TYPE_GOOGLE = "Google"
    const val USER_TYPE_EMAIL = "Email"
    const val USER_TYPE_PHONE = "Phone"

    const val PROPERTY_TYPE_ANY="Any"
    const val PROPERTY_TYPE_RENT="Rent"
    const val PROPERTY_TYPE_SELL="Sell"

    val propertyTypes= arrayOf("Homes","Plots","Commercial")
    val propertyTypesHomes= arrayOf("House","Flat","Upper Portion","Lower Portion","Farm House","Room","Penthouse")
    val propertyTypesPlots= arrayOf("Residential Plot","Commercial Plot","Agricultural Plot","Industrial Plot","Plot File","Plot Form")
    val propertyTypesCommercial= arrayOf("Office","Shop","Factory","Warehouse","Building","Other")
    val propertyAreaSizeUnit= arrayOf("Square Feet","Square Meter","Acre","Square Yards","Marla")


    fun toast(context: Context,message: String){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }

    fun timestamp(): Long{
        return System.currentTimeMillis()
    }

    fun formatTimestampDate(timestamp: Long) : String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        return android.text.format.DateFormat.format("dd/MM/yyyy",calendar).toString()
    }
}