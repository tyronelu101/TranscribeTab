package com.simplu.transcribetab.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TablatureDatabaseDao {

    @Insert
    fun insert(tab: Tablature)

    @Update
    fun update(tab: Tablature)

    @Query("SELECT * from tablature_table WHERE tabId =:key")
    fun get(key: Long): Tablature

    @Query("DELETE  FROM tablature_table WHERE tabId =:key")
    fun clear(key: Long)

    @Query("SELECT * from tablature_table ORDER BY tabId ASC")
    fun getAllTabs(): LiveData<List<Tablature>>

}