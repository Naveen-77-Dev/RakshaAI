package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AnalysisEventDao
import com.example.data.local.dao.IncidentDao
import com.example.data.local.dao.ScamPatternDao
import com.example.data.local.dao.TrustedContactDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.AnalysisEventEntity
import com.example.data.local.entity.IncidentEntity
import com.example.data.local.entity.ScamPatternEntity
import com.example.data.local.entity.TrustedContactEntity
import com.example.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        TrustedContactEntity::class,
        AnalysisEventEntity::class,
        IncidentEntity::class,
        ScamPatternEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun trustedContactDao(): TrustedContactDao
    abstract fun analysisEventDao(): AnalysisEventDao
    abstract fun incidentDao(): IncidentDao
    abstract fun scamPatternDao(): ScamPatternDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "raksha_ai_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
