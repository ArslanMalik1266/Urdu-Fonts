package com.urdufonts.app.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.urdufonts.app.`data`.local.entity.FontEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
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
public class FontDao_Impl(
  __db: RoomDatabase,
) : FontDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfFontEntity: EntityInsertAdapter<FontEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfFontEntity = object : EntityInsertAdapter<FontEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `fonts` (`id`,`name`,`slug`,`language`,`description`,`developer`,`fontFamily`,`tags`,`featureImageUrl`,`cardImageUrl`,`fontFileUrl`,`previewFileUrl`,`weightCount`,`categoriesJson`,`stylesJson`,`isPremium`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FontEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.slug)
        statement.bindText(4, entity.language)
        statement.bindText(5, entity.description)
        statement.bindText(6, entity.developer)
        statement.bindText(7, entity.fontFamily)
        statement.bindText(8, entity.tags)
        val _tmpFeatureImageUrl: String? = entity.featureImageUrl
        if (_tmpFeatureImageUrl == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpFeatureImageUrl)
        }
        val _tmpCardImageUrl: String? = entity.cardImageUrl
        if (_tmpCardImageUrl == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpCardImageUrl)
        }
        val _tmpFontFileUrl: String? = entity.fontFileUrl
        if (_tmpFontFileUrl == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpFontFileUrl)
        }
        val _tmpPreviewFileUrl: String? = entity.previewFileUrl
        if (_tmpPreviewFileUrl == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmpPreviewFileUrl)
        }
        statement.bindText(13, entity.weightCount)
        statement.bindText(14, entity.categoriesJson)
        statement.bindText(15, entity.stylesJson)
        val _tmp: Int = if (entity.isPremium) 1 else 0
        statement.bindLong(16, _tmp.toLong())
      }
    }
  }

  public override suspend fun insertAll(fonts: List<FontEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfFontEntity.insert(_connection, fonts)
  }

  public override suspend fun getAll(): List<FontEntity> {
    val _sql: String = "SELECT * FROM fonts"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfSlug: Int = getColumnIndexOrThrow(_stmt, "slug")
        val _columnIndexOfLanguage: Int = getColumnIndexOrThrow(_stmt, "language")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfDeveloper: Int = getColumnIndexOrThrow(_stmt, "developer")
        val _columnIndexOfFontFamily: Int = getColumnIndexOrThrow(_stmt, "fontFamily")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfFeatureImageUrl: Int = getColumnIndexOrThrow(_stmt, "featureImageUrl")
        val _columnIndexOfCardImageUrl: Int = getColumnIndexOrThrow(_stmt, "cardImageUrl")
        val _columnIndexOfFontFileUrl: Int = getColumnIndexOrThrow(_stmt, "fontFileUrl")
        val _columnIndexOfPreviewFileUrl: Int = getColumnIndexOrThrow(_stmt, "previewFileUrl")
        val _columnIndexOfWeightCount: Int = getColumnIndexOrThrow(_stmt, "weightCount")
        val _columnIndexOfCategoriesJson: Int = getColumnIndexOrThrow(_stmt, "categoriesJson")
        val _columnIndexOfStylesJson: Int = getColumnIndexOrThrow(_stmt, "stylesJson")
        val _columnIndexOfIsPremium: Int = getColumnIndexOrThrow(_stmt, "isPremium")
        val _result: MutableList<FontEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FontEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpSlug: String
          _tmpSlug = _stmt.getText(_columnIndexOfSlug)
          val _tmpLanguage: String
          _tmpLanguage = _stmt.getText(_columnIndexOfLanguage)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpDeveloper: String
          _tmpDeveloper = _stmt.getText(_columnIndexOfDeveloper)
          val _tmpFontFamily: String
          _tmpFontFamily = _stmt.getText(_columnIndexOfFontFamily)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpFeatureImageUrl: String?
          if (_stmt.isNull(_columnIndexOfFeatureImageUrl)) {
            _tmpFeatureImageUrl = null
          } else {
            _tmpFeatureImageUrl = _stmt.getText(_columnIndexOfFeatureImageUrl)
          }
          val _tmpCardImageUrl: String?
          if (_stmt.isNull(_columnIndexOfCardImageUrl)) {
            _tmpCardImageUrl = null
          } else {
            _tmpCardImageUrl = _stmt.getText(_columnIndexOfCardImageUrl)
          }
          val _tmpFontFileUrl: String?
          if (_stmt.isNull(_columnIndexOfFontFileUrl)) {
            _tmpFontFileUrl = null
          } else {
            _tmpFontFileUrl = _stmt.getText(_columnIndexOfFontFileUrl)
          }
          val _tmpPreviewFileUrl: String?
          if (_stmt.isNull(_columnIndexOfPreviewFileUrl)) {
            _tmpPreviewFileUrl = null
          } else {
            _tmpPreviewFileUrl = _stmt.getText(_columnIndexOfPreviewFileUrl)
          }
          val _tmpWeightCount: String
          _tmpWeightCount = _stmt.getText(_columnIndexOfWeightCount)
          val _tmpCategoriesJson: String
          _tmpCategoriesJson = _stmt.getText(_columnIndexOfCategoriesJson)
          val _tmpStylesJson: String
          _tmpStylesJson = _stmt.getText(_columnIndexOfStylesJson)
          val _tmpIsPremium: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPremium).toInt()
          _tmpIsPremium = _tmp != 0
          _item = FontEntity(_tmpId,_tmpName,_tmpSlug,_tmpLanguage,_tmpDescription,_tmpDeveloper,_tmpFontFamily,_tmpTags,_tmpFeatureImageUrl,_tmpCardImageUrl,_tmpFontFileUrl,_tmpPreviewFileUrl,_tmpWeightCount,_tmpCategoriesJson,_tmpStylesJson,_tmpIsPremium)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun count(): Int {
    val _sql: String = "SELECT COUNT(*) FROM fonts"
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
    val _sql: String = "DELETE FROM fonts"
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
