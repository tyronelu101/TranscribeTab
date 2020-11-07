package com.simplu.transcribetab.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplu.transcribetab.TabSection
import java.lang.reflect.Type


class Converter {

    @TypeConverter
    fun columnListToString(columns: HashMap<Int, TabSection>): String {
        val gson = Gson()
        val type: Type = object : TypeToken<HashMap<Int, TabSection>>() {}.type
        val json = gson.toJson(columns, type)
        return json
    }

    @TypeConverter
    fun stringToColumnList(columnString: String): HashMap<Int, TabSection> {
        val gson = Gson()
        val type: Type = object : TypeToken<HashMap<Int, TabSection>>() {}.type
        val columns: HashMap<Int, TabSection> = gson.fromJson(columnString, type)
        return columns
    }

    @TypeConverter
    fun timeRangeMapToString(timeRange: LinkedHashMap<Int, Pair<Int, Int>>): String {
        val gson = Gson()
        val type: Type = object : TypeToken<LinkedHashMap<Int, Pair<Int, Int>>>() {}.type
        val json = gson.toJson(timeRange, type)
        return json
    }

    @TypeConverter
    fun stringToTimeRangeMap(timeRangeMapString: String): LinkedHashMap<Int, Pair<Int, Int>> {
        val gson = Gson()
        val type: Type = object : TypeToken<LinkedHashMap<Int, Pair<Int, Int>>>() {}.type
        val timeRangeMap: LinkedHashMap<Int, Pair<Int, Int>> = gson.fromJson(timeRangeMapString, type)
        return timeRangeMap
    }
//
//    @TypeConverter
//    fun sectionToTimeMapString(sectionToTime: HashMap<Int, Int>): String {
//        val gson = Gson()
//        val type: Type = object : TypeToken<HashMap<Int, Int>>() {}.type
//        val json = gson.toJson(sectionToTime, type)
//        return json
//    }
//
//    @TypeConverter
//    fun stringToSectionTimeMap(sectionToTimeString: String): HashMap<Int, Int> {
//        val gson = Gson()
//        val type: Type = object : TypeToken<HashMap<Int, Int>>() {}.type
//        val timeRangeMap: HashMap<Int, Int> = gson.fromJson(sectionToTimeString, type)
//        return timeRangeMap
//    }
}