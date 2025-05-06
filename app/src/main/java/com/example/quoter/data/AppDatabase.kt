package com.example.quoter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Remove Quote from entities list
@Database(entities = [GeneratedImage::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    // Remove abstract quoteDao function
    // abstract fun quoteDao(): QuoteDao
    abstract fun generatedImageDao(): GeneratedImageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Define the migration from version 1 to 2
        // This migration might need adjustment or removal if Quote was part of version 1
        // For now, keeping it as is, but be aware it might fail if the 'quotes' table doesn't exist in v1
        // or if other schema changes related to Quote removal are needed.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create the generated_images table as it was in version 2
                // This assumes generated_images did not exist in version 1
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `generated_images` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `filePath` TEXT NOT NULL,
                        `prompt` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `isSelectedForRotation` INTEGER NOT NULL DEFAULT 1
                    )
                    """.trimIndent())
                // If the 'quotes' table existed in version 1, you might need to drop it here:
                // db.execSQL("DROP TABLE IF EXISTS `quotes`")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quote_database"
                )
                // Add fallbackToDestructiveMigration for easier schema changes during development
                .fallbackToDestructiveMigration()
                // Only add MIGRATION_1_2
                .addMigrations(MIGRATION_1_2)
                // If schema changes cause issues, consider fallbackToDestructiveMigration during development
                // .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}