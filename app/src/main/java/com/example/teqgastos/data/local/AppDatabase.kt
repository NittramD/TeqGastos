package com.example.teqgastos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.teqgastos.data.local.dao.AuditoriaDao
import com.example.teqgastos.data.local.dao.CatalogDao
import com.example.teqgastos.data.local.dao.GastoDao
import com.example.teqgastos.data.local.dao.RendicionDao
import com.example.teqgastos.data.local.entity.AuditoriaEntity
import com.example.teqgastos.data.local.entity.CategoriaGastoEntity
import com.example.teqgastos.data.local.entity.CentroCostoEntity
import com.example.teqgastos.data.local.entity.GastoEntity
import com.example.teqgastos.data.local.entity.RendicionEntity

@Database(
    entities = [
        RendicionEntity::class,
        GastoEntity::class,
        CategoriaGastoEntity::class,
        CentroCostoEntity::class,
        AuditoriaEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rendicionDao(): RendicionDao
    abstract fun gastoDao(): GastoDao
    abstract fun catalogDao(): CatalogDao
    abstract fun auditoriaDao(): AuditoriaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "teqgastos_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
