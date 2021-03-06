package com.simplu.transcribetab.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplu.transcribetab.views.TabSection
import java.lang.reflect.Type


class Converter {

    @TypeConverter
    fun columnListToString(columns: LinkedHashMap<Int, TabSection>): String {
        val gson = Gson()
        val type: Type = object : TypeToken<LinkedHashMap<Int, TabSection>>() {}.type
        val json = gson.toJson(columns, type)
        return json
    }

    @TypeConverter
    fun stringToColumnList(columnString: String): LinkedHashMap<Int, TabSection> {
        val gson = Gson()
        val type: Type = object : TypeToken<LinkedHashMap<Int, TabSection>>() {}.type
        val columns: LinkedHashMap<Int, TabSection> = gson.fromJson(columnString, type)
        return columns
    }

}