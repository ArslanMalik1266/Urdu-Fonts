package com.urdufonts.app.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.urdufonts.app.`data`.local.entity.StyleEntity
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class StyleDao_Impl(
  __db: RoomDatabase,
) : StyleDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfStyleEntity: EntityInsertAdapter<StyleEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfStyleEntity = object : EntityInsertAdapter<StyleEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `styles` (`id`,`title`,`slug`,`description`,`thumbnailUrl`) VALUES (?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: StyleEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.slug)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpDescription)
        }
        val _tmpThumbnailUrl: String? = entity.thumbnailUrl
        if (_tmpThumbnailUrl == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpThumbnailUrl)
        }
      }
    }
  }

  public override suspend fun insertAll(styles: List<StyleEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfStyleEntity.insert(_connection, styles)
  }

  public override suspend fun getAll(): List<StyleEntity> {
    val _sql: String = "SELECT * FROM styles"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfSlug: Int = getColumnIndexOrThrow(_stmt, "slug")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnailUrl")
        val _result: MutableList<StyleEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: StyleEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpSlug: String
          _tmpSlug = _stmt.getText(_columnIndexOfSlug)
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          _item = StyleEntity(_tmpId,_tmpTitle,_tmpSlug,_tmpDescription,_tmpThumbnailUrl)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun count(): Int {
    val _sql: String = "SELECT COUNT(*) FROM styles"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAll() {
    val _sql: String = "DELETE FROM styles"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
