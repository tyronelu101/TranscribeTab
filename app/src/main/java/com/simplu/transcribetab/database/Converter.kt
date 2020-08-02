package com.simplu.transcribetab.database

import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplu.transcribetab.edittab.Column
import java.lang.reflect.Type


class Converter {

    @TypeConverter
    fun columnListToString(columns: ArrayList<Column>): String {
        val gson = Gson()
        val type: Type = object : TypeToken<List<Column>?>() {}.type
        val json = gson.toJson(columns, type)
        Log.v("Converter", "String is ${json}")
        return json
    }

    @TypeConverter
    fun stringToColumnList(columnString: String): ArrayList<Column> {
        val gson = Gson()
        val type: Type = object : TypeToken<List<Column>?>() {}.type
        val columns: ArrayList<Column> = gson.fromJson(columnString, type)
        return columns
    }
}