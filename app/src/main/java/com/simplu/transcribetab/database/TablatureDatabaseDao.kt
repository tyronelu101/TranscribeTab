package com.simplu.transcribetab.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TablatureDatabaseDao {

    @Insert
    fun insert(tab: Tablature)

    @Update
    fun update(tab: Tablature)

    @Delete
    fun delete(tab: Tablature)

    @Query("SELECT * from tablature_table WHERE tabId =:key")
    fun get(key: Long): LiveData<Tablature>

    @Query("SELECT * from tablature_table ORDER BY tabId DESC")
    fun getAllTabs(): LiveData<List<Tablature>>


}