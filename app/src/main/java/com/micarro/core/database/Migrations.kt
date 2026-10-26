package com.micarro.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE vehicle_documents ADD COLUMN reference_number TEXT")
        db.execSQL("ALTER TABLE vehicle_documents ADD COLUMN file_path TEXT")
        db.execSQL("ALTER TABLE vehicle_documents ADD COLUMN mime_type TEXT")
        db.execSQL(
            "ALTER TABLE vehicle_documents ADD COLUMN reminded_month INTEGER NOT NULL DEFAULT 0"
        )
        db.execSQL(
            "ALTER TABLE vehicle_documents ADD COLUMN reminded_fortnight INTEGER NOT NULL DEFAULT 0"
        )
        db.execSQL(
            "ALTER TABLE vehicle_documents ADD COLUMN reminded_expired INTEGER NOT NULL DEFAULT 0"
        )
    }
}
