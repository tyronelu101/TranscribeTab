package com.simplu.transcribetab.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class Converter {

    @TypeConverter
    fun columnListToString(columns: HashMap<Int, ArrayList<Array<String>>>): String {
        val gson = Gson()
        val type: Type = object : TypeToken<HashMap<Int, ArrayList<Array<String>>>>() {}.type
        val json = gson.toJson(columns, type)
        return json
    }

    @TypeConverter
    fun stringToColumnList(columnString: String): HashMap<Int, ArrayList<Array<String>>> {
        val gson = Gson()
        val type: Type = object : TypeToken<HashMap<Int, ArrayList<Array<String>>>>() {}.type
        val columns: HashMap<Int, ArrayList<Array<String>>> = gson.fromJson(columnString, type)
        return columns
    }
}