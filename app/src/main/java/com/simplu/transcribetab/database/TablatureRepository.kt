package com.simplu.transcribetab.database

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TablatureRepository(private val tabDb: TablatureDatabase) {

    fun getAllTab(): LiveData<List<Tablature>> {
        return tabDb.dao.getAllTabs()
    }

    suspend fun insert(tab: Tablature) {
        withContext(Dispatchers.IO) {
            tabDb.dao.insert(tab)
        }
    }

    suspend fun update(tab: Tablature) {
        withContext(Dispatchers.IO) {
            tabDb.dao.update(tab)
        }
    }

    suspend fun delete(tab: Tablature) {
        withContext(Dispatchers.IO) {
            tabDb.dao.delete(tab)
        }
    }

}