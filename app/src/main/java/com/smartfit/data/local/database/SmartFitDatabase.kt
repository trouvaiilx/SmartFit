// FILE: app/src/main/java/com/smartfit/data/local/database/SmartFitDatabase.kt

package com.smartfit.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ActivityEntity::class, MealEntity::class, StepCountEntity::class],
    version = 2,
    exportSchema = false
)
abstract class SmartFitDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun mealDao(): MealDao
    abstract fun stepCountDao(): StepCountDao

    companion object {
        @Volatile
        private var INSTANCE: SmartFitDatabase? = null

        fun getDatabase(context: Context): SmartFitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartFitDatabase::class.java,
                    "smartfit_database"
                )
//                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}