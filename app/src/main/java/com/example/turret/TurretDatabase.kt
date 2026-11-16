package com.example.turret

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "presets")
data class Preset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val pan: Int,
    val tilt: Int
)

@Dao
interface PresetDao {
    @Query("SELECT * FROM presets ORDER BY id DESC")
    fun observeAll(): Flow<List<Preset>>

    @Insert
    suspend fun insert(preset: Preset)

    @Update
    suspend fun update(preset: Preset)

    @Delete
    suspend fun delete(preset: Preset)
}

@Database(entities = [Preset::class], version = 1, exportSchema = false)
abstract class TurretDatabase : RoomDatabase() {
    abstract fun presetDao(): PresetDao
}
