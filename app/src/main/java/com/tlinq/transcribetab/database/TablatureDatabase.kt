package com.tlinq.transcribetab.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [Tablature::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class TablatureDatabase: RoomDatabase() {

    abstract val dao: TablatureDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE:  TablatureDatabase? = null

        fun getInstance(context: Context): TablatureDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TablatureDatabase::class.java,
                        "tablature_database").fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}