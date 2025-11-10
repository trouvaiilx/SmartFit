// FILE: app/src/main/java/com/smartfit/data/local/database/SmartFitDatabase.kt

package com.smartfit.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ActivityEntity::class, MealEntity::class, StepCountEntity::class],
    version = 2,
    exportSchema = true
)
abstract class SmartFitDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun mealDao(): MealDao
    abstract fun stepCountDao(): StepCountDao

    companion object {
        @Volatile
        private var INSTANCE: SmartFitDatabase? = null

        // Migration from version 1 to 2 (add your migration logic here)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // If you had schema changes between v1 and v2, add them here
                // For now, this is a placeholder - your schema is already at v2
                // Example:
                // database.execSQL("ALTER TABLE activities ADD COLUMN new_column TEXT")
            }
        }

        fun getDatabase(context: Context): SmartFitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartFitDatabase::class.java,
                    "smartfit_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    // Only use fallbackToDestructiveMigration in development
                    // Remove this in production or add proper migrations
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // For testing purposes
        fun getInMemoryDatabase(context: Context): SmartFitDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                SmartFitDatabase::class.java
            ).build()
        }
    }
}