package com.urdufonts.app.`data`.local.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.urdufonts.app.`data`.local.dao.CategoryDao
import com.urdufonts.app.`data`.local.dao.CategoryDao_Impl
import com.urdufonts.app.`data`.local.dao.FontDao
import com.urdufonts.app.`data`.local.dao.FontDao_Impl
import com.urdufonts.app.`data`.local.dao.StyleDao
import com.urdufonts.app.`data`.local.dao.StyleDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _fontDao: Lazy<FontDao> = lazy {
    FontDao_Impl(this)
  }

  private val _categoryDao: Lazy<CategoryDao> = lazy {
    CategoryDao_Impl(this)
  }

  private val _styleDao: Lazy<StyleDao> = lazy {
    StyleDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(2, "9d715c3b9bf221374a2a7a5762854e56", "344829dca2aa1676f80bf3d4662452c8") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `fonts` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `slug` TEXT NOT NULL, `language` TEXT NOT NULL, `description` TEXT NOT NULL, `developer` TEXT NOT NULL, `fontFamily` TEXT NOT NULL, `tags` TEXT NOT NULL, `featureImageUrl` TEXT, `cardImageUrl` TEXT, `fontFileUrl` TEXT, `previewFileUrl` TEXT, `weightCount` TEXT NOT NULL, `categoriesJson` TEXT NOT NULL, `stylesJson` TEXT NOT NULL, `isPremium` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER NOT NULL, `title` TEXT NOT NULL, `slug` TEXT NOT NULL, `description` TEXT, `thumbnailUrl` TEXT, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `styles` (`id` INTEGER NOT NULL, `title` TEXT NOT NULL, `slug` TEXT NOT NULL, `description` TEXT, `thumbnailUrl` TEXT, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '9d715c3b9bf221374a2a7a5762854e56')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `fonts`")
        connection.execSQL("DROP TABLE IF EXISTS `categories`")
        connection.execSQL("DROP TABLE IF EXISTS `styles`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsFonts: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFonts.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFonts.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFonts.put("slug", TableInfo.Column("slug", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFonts.put("language", TableInfo.Column("language", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFonts.put("description", TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFonts.put("developer", TableInfo.Column("developer", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFonts.put("fontFamily", TableInfo.Column("fontFamily", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFonts.put("tags", TableInfo.Column("tags", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFonts.put("featureImageUrl", TableInfo.Column("featureImageUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFonts.put("cardImageUrl", TableInfo.Column("cardImageUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFonts.put("fontFileUrl", TableInfo.Column("fontFileUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFonts.put("previewFileUrl", TableInfo.Column("previewFileUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFonts.put("weightCount", TableInfo.Column("weightCount", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFonts.put("categoriesJson", TableInfo.Column("categoriesJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFonts.put("stylesJson", TableInfo.Column("stylesJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFonts.put("isPremium", TableInfo.Column("isPremium", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFonts: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesFonts: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoFonts: TableInfo = TableInfo("fonts", _columnsFonts, _foreignKeysFonts, _indicesFonts)
        val _existingFonts: TableInfo = read(connection, "fonts")
        if (!_infoFonts.equals(_existingFonts)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |fonts(com.urdufonts.app.data.local.entity.FontEntity).
              | Expected:
              |""".trimMargin() + _infoFonts + """
              |
              | Found:
              |""".trimMargin() + _existingFonts)
        }
        val _columnsCategories: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCategories.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("slug", TableInfo.Column("slug", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("description", TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("thumbnailUrl", TableInfo.Column("thumbnailUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCategories: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCategories: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCategories: TableInfo = TableInfo("categories", _columnsCategories, _foreignKeysCategories, _indicesCategories)
        val _existingCategories: TableInfo = read(connection, "categories")
        if (!_infoCategories.equals(_existingCategories)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |categories(com.urdufonts.app.data.local.entity.CategoryEntity).
              | Expected:
              |""".trimMargin() + _infoCategories + """
              |
              | Found:
              |""".trimMargin() + _existingCategories)
        }
        val _columnsStyles: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsStyles.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStyles.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStyles.put("slug", TableInfo.Column("slug", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStyles.put("description", TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStyles.put("thumbnailUrl", TableInfo.Column("thumbnailUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysStyles: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesStyles: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoStyles: TableInfo = TableInfo("styles", _columnsStyles, _foreignKeysStyles, _indicesStyles)
        val _existingStyles: TableInfo = read(connection, "styles")
        if (!_infoStyles.equals(_existingStyles)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |styles(com.urdufonts.app.data.local.entity.StyleEntity).
              | Expected:
              |""".trimMargin() + _infoStyles + """
              |
              | Found:
              |""".trimMargin() + _existingStyles)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "fonts", "categories", "styles")
  }

  public override fun clearAllTables() {
    super.performClear(false, "fonts", "categories", "styles")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(FontDao::class, FontDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(CategoryDao::class, CategoryDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(StyleDao::class, StyleDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun fontDao(): FontDao = _fontDao.value

  public override fun categoryDao(): CategoryDao = _categoryDao.value

  public override fun styleDao(): StyleDao = _styleDao.value
}
