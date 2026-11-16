package com.example.turret

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class PresetDao_Impl(
  __db: RoomDatabase,
) : PresetDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPreset: EntityInsertAdapter<Preset>

  private val __deleteAdapterOfPreset: EntityDeleteOrUpdateAdapter<Preset>

  private val __updateAdapterOfPreset: EntityDeleteOrUpdateAdapter<Preset>
  init {
    this.__db = __db
    this.__insertAdapterOfPreset = object : EntityInsertAdapter<Preset>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `presets` (`id`,`name`,`pan`,`tilt`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Preset) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.pan.toLong())
        statement.bindLong(4, entity.tilt.toLong())
      }
    }
    this.__deleteAdapterOfPreset = object : EntityDeleteOrUpdateAdapter<Preset>() {
      protected override fun createQuery(): String = "DELETE FROM `presets` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Preset) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfPreset = object : EntityDeleteOrUpdateAdapter<Preset>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `presets` SET `id` = ?,`name` = ?,`pan` = ?,`tilt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Preset) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.pan.toLong())
        statement.bindLong(4, entity.tilt.toLong())
        statement.bindLong(5, entity.id)
      }
    }
  }

  public override suspend fun insert(preset: Preset): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfPreset.insert(_connection, preset)
  }

  public override suspend fun delete(preset: Preset): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfPreset.handle(_connection, preset)
  }

  public override suspend fun update(preset: Preset): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfPreset.handle(_connection, preset)
  }

  public override fun observeAll(): Flow<List<Preset>> {
    val _sql: String = "SELECT * FROM presets ORDER BY id DESC"
    return createFlow(__db, false, arrayOf("presets")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfPan: Int = getColumnIndexOrThrow(_stmt, "pan")
        val _columnIndexOfTilt: Int = getColumnIndexOrThrow(_stmt, "tilt")
        val _result: MutableList<Preset> = mutableListOf()
        while (_stmt.step()) {
          val _item: Preset
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpPan: Int
          _tmpPan = _stmt.getLong(_columnIndexOfPan).toInt()
          val _tmpTilt: Int
          _tmpTilt = _stmt.getLong(_columnIndexOfTilt).toInt()
          _item = Preset(_tmpId,_tmpName,_tmpPan,_tmpTilt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
