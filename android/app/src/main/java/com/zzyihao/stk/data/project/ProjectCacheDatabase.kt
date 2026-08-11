package com.zzyihao.stk.data.project

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "project_page_cache")
data class ProjectPageCacheEntity(
    @PrimaryKey val cacheKey: String,
    val payload: String,
    val updatedAt: Long,
)

@Entity(tableName = "project_category_cache")
data class ProjectCategoryCacheEntity(
    @PrimaryKey val categoryId: String,
    val name: String,
    val sortOrder: Int,
    val updatedAt: Long,
)

@Entity(tableName = "project_detail_cache")
data class ProjectDetailCacheEntity(
    @PrimaryKey val projectId: String,
    val payload: String,
    val updatedAt: Long,
)

@Dao
interface ProjectCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePage(entity: ProjectPageCacheEntity)

    @Query("SELECT payload FROM project_page_cache WHERE cacheKey = :key LIMIT 1")
    suspend fun readPage(key: String): String?

    @Query("DELETE FROM project_category_cache")
    suspend fun deleteCategories()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCategories(entities: List<ProjectCategoryCacheEntity>)

    @Query("SELECT * FROM project_category_cache ORDER BY sortOrder, categoryId")
    suspend fun readCategories(): List<ProjectCategoryCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDetail(entity: ProjectDetailCacheEntity)

    @Query("SELECT payload FROM project_detail_cache WHERE projectId = :id LIMIT 1")
    suspend fun readDetail(id: String): String?
}

@Database(
    entities = [ProjectPageCacheEntity::class, ProjectCategoryCacheEntity::class, ProjectDetailCacheEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class ProjectCacheDatabase : RoomDatabase() {
    abstract fun projectCacheDao(): ProjectCacheDao

    companion object {
        @Volatile private var instance: ProjectCacheDatabase? = null

        fun get(context: Context): ProjectCacheDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                ProjectCacheDatabase::class.java,
                "stk-project-cache.db",
            ).build().also { instance = it }
        }
    }
}
