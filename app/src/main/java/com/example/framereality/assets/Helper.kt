package com.example.framereality.assets

import android.content.Context
import android.util.Log
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.IOException

object Helper {
    var giftList= mutableListOf<Gift>()

    fun loadGifts(context: Context) {
        val jsonString = try {
            context.assets.open("gifts.json").bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.d("Gifts", "Error reading JSON file: ${e.message}")
            e.printStackTrace()
            return
        }
        val listType = object : TypeToken<MutableList<Gift>>() {}.type
        val parsedList: MutableList<Gift> = Gson().fromJson(jsonString, listType)

        // Clear the list and add all elements from the parsed list
        giftList.clear()
        giftList.addAll(parsedList)
    }


    fun getGifts(): MutableList<Gift> {
        Log.d("Gifts", giftList.toString())
        return giftList
    }
}
