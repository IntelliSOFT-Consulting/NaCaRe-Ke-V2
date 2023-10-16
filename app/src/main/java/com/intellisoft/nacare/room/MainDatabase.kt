package com.intellisoft.nacare.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities =
    [OrganizationData::class, EventData::class, ProgramData::class, ElementResponse::class, FacilityEventData::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
public abstract class MainDatabase : RoomDatabase() {

    abstract fun roomDao(): RoomDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: MainDatabase? = null

        fun getDatabase(context: Context): MainDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext, MainDatabase::class.java, "nacare_database"
                    )
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
