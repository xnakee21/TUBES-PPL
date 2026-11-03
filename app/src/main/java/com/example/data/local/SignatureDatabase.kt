package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.ReferenceSpecimen
import com.example.data.model.VerificationMode
import com.example.data.model.VerificationRecord
import com.example.data.model.VerificationVerdict
import kotlinx.coroutines.flow.Flow

class Converters {
    @TypeConverter
    fun fromVerdict(verdict: VerificationVerdict): String = verdict.name

    @TypeConverter
    fun toVerdict(value: String): VerificationVerdict =
        runCatching { VerificationVerdict.valueOf(value) }.getOrDefault(VerificationVerdict.ASLI)

    @TypeConverter
    fun fromMode(mode: VerificationMode): String = mode.name

    @TypeConverter
    fun toMode(value: String): VerificationMode =
        runCatching { VerificationMode.valueOf(value) }.getOrDefault(VerificationMode.COMPARISON)
}

@Dao
interface VerificationDao {
    @Query("SELECT * FROM verification_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<VerificationRecord>>

    @Query("SELECT * FROM verification_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): VerificationRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: VerificationRecord): Long

    @Delete
    suspend fun deleteRecord(record: VerificationRecord)

    @Query("DELETE FROM verification_records")
    suspend fun clearAll()
}

@Dao
interface ReferenceDao {
    @Query("SELECT * FROM reference_specimens ORDER BY registeredDate DESC")
    fun getAllSpecimens(): Flow<List<ReferenceSpecimen>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecimen(specimen: ReferenceSpecimen): Long

    @Delete
    suspend fun deleteSpecimen(specimen: ReferenceSpecimen)
}

@Database(
    entities = [VerificationRecord::class, ReferenceSpecimen::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SignatureDatabase : RoomDatabase() {
    abstract fun verificationDao(): VerificationDao
    abstract fun referenceDao(): ReferenceDao

    companion object {
        @Volatile
        private var INSTANCE: SignatureDatabase? = null

        fun getInstance(context: Context): SignatureDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SignatureDatabase::class.java,
                    "signature_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
