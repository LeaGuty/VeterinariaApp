package com.miempresa.veterinaria.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.miempresa.veterinaria.data.dao.VeterinariaDao
import com.miempresa.veterinaria.data.entity.*

@Database(
    entities = [ClienteEntity::class, MascotaEntity::class, ConsultaEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class VeterinariaDatabase : RoomDatabase() {
    abstract fun veterinariaDao(): VeterinariaDao

    companion object {
        @Volatile
        private var INSTANCE: VeterinariaDatabase? = null

        fun getDatabase(context: Context): VeterinariaDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    VeterinariaDatabase::class.java,
                    "veterinaria_db"
                )
                    // .fallbackToDestructiveMigration() // Úsalo solo en desarrollo si cambias mucho la BD
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}